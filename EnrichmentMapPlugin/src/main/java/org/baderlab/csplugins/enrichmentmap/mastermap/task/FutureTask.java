package org.baderlab.csplugins.enrichmentmap.mastermap.task;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

public abstract class FutureTask<T> extends AbstractTask {
	
	private final Path filePath;
	private final CompletableFuture<T> future;
	private final String datasetName;
	
	
	public FutureTask(Path filePath, String datasetName) {
		this.filePath = filePath;
		this.future = new CompletableFuture<>();
		this.datasetName = datasetName;
	}

	public CompletableFuture<T> ask() {
		return future;
	}
	
	
	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		taskMonitor.setTitle("Processing " + datasetName);
		taskMonitor.setProgress(0.0);
		taskMonitor.setStatusMessage("Parsing EDB");
		
		parse(filePath, future);
	    
	    taskMonitor.setProgress(1.0);
	}

	
	public abstract void parse(Path filePath, CompletableFuture<T> future) throws Exception;
}
