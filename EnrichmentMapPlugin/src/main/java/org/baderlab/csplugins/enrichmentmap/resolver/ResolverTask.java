package org.baderlab.csplugins.enrichmentmap.resolver;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskMonitor;

public class ResolverTask extends AbstractTask implements ObservableTask, CancelStatus {

	private final Path root;
	private List<DataSetParameters> results;
	
	public ResolverTask(Path root) {
		this.root = root;
	}
	
	public ResolverTask(File root) {
		this(root.toPath());
	}
	
	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		taskMonitor.setTitle("EnrichmentMap");
		taskMonitor.setStatusMessage("Scanning Folder for Data Sets");
		
		results = DataSetResolver.guessDataSets(root, this);
		if(results.isEmpty() && !cancelled) {
			throw new RuntimeException("No Data Sets found under: " + root);
		}
	}
	
	@Override
	public <R> R getResults(Class<? extends R> type) {
		if(List.class.equals(type)) {
			return type.cast(results);
		}
		return null;
	}

	@Override
	public boolean isCancelled() {
		return cancelled;
	}
	
}
