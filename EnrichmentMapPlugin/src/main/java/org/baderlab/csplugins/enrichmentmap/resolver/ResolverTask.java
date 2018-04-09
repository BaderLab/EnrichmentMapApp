package org.baderlab.csplugins.enrichmentmap.resolver;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.baderlab.csplugins.enrichmentmap.model.DataSetParameters;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskMonitor;

public class ResolverTask extends AbstractTask implements ObservableTask, CancelStatus {

	private final List<Path> paths = new ArrayList<>();
	private final List<DataSetParameters> results = new ArrayList<>();
	
	public ResolverTask(Path root) {
		paths.add(root);
	}
	
	public ResolverTask(File root) {
		this(root.toPath());
	}
	
	public ResolverTask(List<File> files) {
		for(File file : files) 
			paths.add(file.toPath());
	}
	
	@Override
	public void run(TaskMonitor tm) {
		tm.setTitle("EnrichmentMap");
		tm.setStatusMessage("Scanning Folder for Data Sets");
		
		// If its a single folder we will scan all the subfolders 1 level deep
		if(paths.size() == 1) {
			Path path = paths.get(0);
			if(Files.isDirectory(path)) {
				for(File subdirectory : path.toFile().listFiles(File::isDirectory)) {
					paths.add(subdirectory.toPath());
				}
			}
		}
		
		List<Path> miscFiles = new ArrayList<>();
		
		for(Path path : paths) {
			if(cancelled)
				break;
			try {
				if(Files.isDirectory(path)) {
					List<DataSetParameters> dataSets = DataSetResolver.guessDataSets(path, this);
					results.addAll(dataSets);
				} else {
					miscFiles.add(path);
				}
			} catch(Exception e) {
				throw new RuntimeException("Error while resolving path: " + path, e);
			}
		}
		
		try {
			if(!miscFiles.isEmpty()) {
				List<DataSetParameters> dataSets = DataSetResolver.guessDataSets(miscFiles, this);
				results.addAll(dataSets);
			}
		} catch(Exception e) {
			throw new RuntimeException("Error while resolving paths", e);
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
