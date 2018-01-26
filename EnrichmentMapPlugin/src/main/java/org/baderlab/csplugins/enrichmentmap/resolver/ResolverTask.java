package org.baderlab.csplugins.enrichmentmap.resolver;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskMonitor;

public class ResolverTask extends AbstractTask implements ObservableTask, CancelStatus {

	private final List<Path> folders = new ArrayList<>();
	private final List<DataSetParameters> results = new ArrayList<>();
	
	public ResolverTask(Path root) {
		folders.add(root);
	}
	
	public ResolverTask(File root) {
		this(root.toPath());
	}
	
	public ResolverTask(List<File> files) {
		for(File file : files) 
			folders.add(file.toPath());
	}
	
	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		taskMonitor.setTitle("EnrichmentMap");
		taskMonitor.setStatusMessage("Scanning Folder for Data Sets");
		
		for(Path path : folders) {
			if(cancelled)
				break;
			
			try {
				if(Files.isDirectory(path)) {
					List<DataSetParameters> dataSets = DataSetResolver.guessDataSets(path, (CancelStatus)this);
					results.addAll(dataSets);
				}
			} catch(Exception e) {
				throw new RuntimeException("Error while resolving path: " + path, e);
			}
		}
	}
	
	@Override
	public <R> R getResults(Class<? extends R> type) {
		if(List.class.equals(type)) {
			return type.cast(results);
		}
		return null;
	}
	
	public List<DataSetParameters> getDataSetResults() {
		return results;
	}

	@Override
	public boolean isCancelled() {
		return cancelled;
	}
	
}
