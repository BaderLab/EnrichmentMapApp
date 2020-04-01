package org.baderlab.csplugins.enrichmentmap.resolver;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.ArrayList;
import java.util.List;

import org.baderlab.csplugins.enrichmentmap.model.DataSetParameters;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskMonitor;

public class DataSetResolverTask extends AbstractTask implements ObservableTask {

	private final List<Path> paths = new ArrayList<>();
	private final List<DataSetParameters> results = new ArrayList<>();
	
	private PathMatcher matcher = null;
	
	
	public DataSetResolverTask(Path root) {
		paths.add(root);
	}
	
	public DataSetResolverTask(File root) {
		this(root.toPath());
	}
	
	public DataSetResolverTask(List<File> files) {
		for(File file : files) 
			paths.add(file.toPath());
	}
	
	public void setPathMatcher(PathMatcher matcher) {
		this.matcher = matcher;
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
					Path subDirPath = subdirectory.toPath();
					if(matcher == null || matcher.matches(subDirPath.getFileName())) {
						paths.add(subDirPath);
					}
				}
			}
		}
		
		List<Path> miscFiles = new ArrayList<>();
		
		for(Path path : paths) {
			if(cancelled) {
				results.clear();
				return;
			}
			try {
				if(Files.isDirectory(path)) {
					List<DataSetParameters> dataSets = DataSetResolver.guessDataSets(path, () -> cancelled);
					results.addAll(dataSets);
				} else {
					miscFiles.add(path);
				}
			} catch(Exception e) {
				throw new RuntimeException("Error while resolving path: " + path, e);
			}
		}
		
		try {
			if(cancelled) {
				results.clear();
				return;
			}
			if(!miscFiles.isEmpty()) {
				List<DataSetParameters> dataSets = DataSetResolver.guessDataSets(miscFiles, () -> cancelled);
				results.addAll(dataSets);
			}
		} catch(Exception e) {
			throw new RuntimeException("Error while resolving paths", e);
		}
	}
	
	@Override
	public <R> R getResults(Class<? extends R> type) {
		if(List.class.equals(type)) {
			return type.cast(getDataSetResults());
		}
		return null;
	}
	
	public List<DataSetParameters> getDataSetResults() {
		return new ArrayList<>(results);
	}

}
