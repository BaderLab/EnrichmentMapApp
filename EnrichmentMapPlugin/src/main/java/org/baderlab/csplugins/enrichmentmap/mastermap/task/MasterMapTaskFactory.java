package org.baderlab.csplugins.enrichmentmap.mastermap.task;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

import org.baderlab.csplugins.enrichmentmap.mastermap.model.GSEAResults;
import org.baderlab.csplugins.enrichmentmap.mastermap.model.GSEAResultsFolder;
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
		
		CompletableFuture<GSEAResults.Builder> gseaBuilderFuture = CompletableFuture.completedFuture(new GSEAResults.Builder());
		
		for(Path path : gseaResultsFolders) {
			
			GMTFileParserTask gmtTask = new GMTFileParserTask(path);
			GMTFileParserTask rnkTask = new GMTFileParserTask(path);
			GMTFileParserTask edbTask = new GMTFileParserTask(path);
			
			tasks.append(gmtTask);
			tasks.append(rnkTask);
			tasks.append(edbTask);
			
			GSEAResultsFolder.Builder folderBuilder = new GSEAResultsFolder.Builder();
			
			gseaBuilderFuture = 
				CompletableFuture.allOf(
					gmtTask.ask().thenApply(folderBuilder::setGeneSets),
					rnkTask.ask().thenApply(folderBuilder::setGeneSets),
					edbTask.ask().thenApply(folderBuilder::setGeneSets)
				)
				.thenApply(v -> folderBuilder.build())
				.thenCombine(gseaBuilderFuture, (f, gseaBuilder) -> gseaBuilder.addResultsFolder(f));
			
		}
		
		Future<GSEAResults> gseaFuture = gseaBuilderFuture.thenApply(GSEAResults.Builder::build);
		GSEAResultsDisplayTask displayTask = new GSEAResultsDisplayTask(gseaFuture);
		tasks.append(displayTask);
		
		return tasks;
	}

}
