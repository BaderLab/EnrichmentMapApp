package org.baderlab.csplugins.enrichmentmap.mastermap.task;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

import org.baderlab.csplugins.enrichmentmap.mastermap.model.GSEAResults;
import org.baderlab.csplugins.enrichmentmap.mastermap.model.GSEAResultsFolder;
import org.baderlab.csplugins.enrichmentmap.task.TitleTask;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

public class MasterMapTaskFactory extends AbstractTaskFactory {

	private List<Path> gseaResultsFolders;
	
	
	public static interface Factory {
		MasterMapTaskFactory create(List<Path> gseaResultsFolders);
	}
	
	@Inject
	public MasterMapTaskFactory(@Assisted List<Path> gseaResultsFolders) {
		this.gseaResultsFolders = gseaResultsFolders;
	}
	
	
	@Override
	public TaskIterator createTaskIterator() {
		TaskIterator tasks = new TaskIterator();
		if(gseaResultsFolders.isEmpty())
			return tasks;
		
		tasks.append(new TitleTask("Building MasterMap"));
		
		CompletableFuture<GSEAResults.Builder> gseaBuilderFuture = CompletableFuture.completedFuture(new GSEAResults.Builder());
		
		for(Path path : gseaResultsFolders) {
			// MKTODO test on windows!
			
			String datasetName = getDatasetName(path);
			GMTFileParserTask gmtTask = new GMTFileParserTask(path.resolve(Paths.get("edb/gene_sets.gmt")), datasetName);
			EDBFileParserTask edbTask = new EDBFileParserTask(path.resolve(Paths.get("edb/results.edb")),   datasetName);
			
			tasks.append(gmtTask);
			tasks.append(edbTask);
			
			GSEAResultsFolder.Builder folderBuilder = new GSEAResultsFolder.Builder();
			
			gseaBuilderFuture = 
				CompletableFuture.allOf(
					gmtTask.ask().thenApply(folderBuilder::setGeneSets),
					edbTask.ask().thenApply(folderBuilder::setEnrichments)
				)
				.thenApply(v -> folderBuilder.build())
				.thenCombine(gseaBuilderFuture, (f, gseaBuilder) -> gseaBuilder.addResultsFolder(f));
		}
		
		Future<GSEAResults> gseaFuture = gseaBuilderFuture.thenApply(GSEAResults.Builder::build);
		GSEAResultsDisplayTask displayTask = new GSEAResultsDisplayTask(gseaFuture);
		tasks.append(displayTask);
		
		return tasks;
	}
	
	private String getDatasetName(Path folder) {
		String folderName = folder.getFileName().toString();
		int dotIndex = folderName.indexOf('.');
		if(dotIndex == -1)
			return folderName;
		else
			return folderName.substring(0, dotIndex);
	}

}
