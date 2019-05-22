package org.baderlab.csplugins.enrichmentmap.commands;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.baderlab.csplugins.enrichmentmap.model.DataSetFiles;
import org.baderlab.csplugins.enrichmentmap.model.DataSetParameters;
import org.baderlab.csplugins.enrichmentmap.resolver.ResolverTask;
import org.baderlab.csplugins.enrichmentmap.util.NullTaskMonitor;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.SynchronousTaskManager;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.json.JSONResult;

import com.google.gson.Gson;
import com.google.inject.Inject;

public class MastermapListCommandTask extends AbstractTask implements ObservableTask {

	@Tunable(required=true, description="Absolute path to a folder containing the data files. "
			+ "The files will be scanned and automatically grouped into data sets. Sub-folders will be scanned up to one level deep.")
	public File rootFolder;
	
	@Inject private SynchronousTaskManager<?> taskManager;
	
	private List<DataSetParameters> results;
	
	
	@Override
	public void run(TaskMonitor tm) throws Exception {
		if(tm == null)
			tm = new NullTaskMonitor();
		tm.setStatusMessage("Running EnrichmentMap Mastermap List Task");
		
		if(rootFolder == null || !rootFolder.exists()) {
			throw new IllegalArgumentException("rootFolder is invalid: " + rootFolder);
		}
		
		// Scan root folder (note: throws exception if no data sets were found)
		ResolverTask resolverTask = new ResolverTask(rootFolder);
		taskManager.execute(new TaskIterator(resolverTask)); // blocks
		results = resolverTask.getDataSetResults();
		
		tm.setStatusMessage("Resolved " + results.size() + " data sets.");
	}
	
	@Override
	public List<Class<?>> getResultClasses() {
		return Arrays.asList(String.class, JSONResult.class);
	}
	
	@Override
	public <R> R getResults(Class<? extends R> type) {
		if(String.class.equals(type)) {
			return type.cast(getStringResult());
		}
		if(JSONResult.class.equals(type)) {
			return type.cast(getJSONResult());
		}
		return null;
	}
	
	
	private String getStringResult() {
		StringBuilder sb = new StringBuilder();
		for(DataSetParameters dsp : results) {
			DataSetFiles files = dsp.getFiles();
			sb.append("Data Set Name: ").append(dsp.getName()).append("\n");
			sb.append("Method: ").append(dsp.getMethod()).append("\n");
			sb.append("Gmt: ").append(files.getGMTFileName()).append("\n");
			sb.append("Enrichments 1: ").append(files.getEnrichmentFileName1()).append("\n");
			sb.append("Enrichments 2: ").append(files.getEnrichmentFileName2()).append("\n");
			sb.append("Expressions:").append(files.getExpressionFileName()).append("\n");
			sb.append("Ranks: ").append(files.getRankedFile()).append("\n");
			sb.append("Classes: ").append(files.getClassFile()).append("\n");
			sb.append("Phenotype 1: ").append(files.getPhenotype1()).append("\n");
			sb.append("Phenotype 2: ").append(files.getPhenotype2()).append("\n");
			sb.append("\n");
		}
		return sb.toString();
	}
	
	private JSONResult getJSONResult() {
		return () -> new Gson().toJson(results);
	}
}