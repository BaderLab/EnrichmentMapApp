package org.baderlab.csplugins.enrichmentmap.commands;

import java.util.Arrays;
import java.util.List;

import org.baderlab.csplugins.enrichmentmap.commands.tunables.NetworkTunable;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ContainsTunables;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskMonitor;

import com.google.inject.Inject;

public class GetDataSetNamesCommandTask extends AbstractTask implements ObservableTask {

	@ContainsTunables @Inject
	public NetworkTunable networkTunable;

	private List<String> results;
	
	
	@Override
	public void run(TaskMonitor tm) {
		EnrichmentMap map = networkTunable.getEnrichmentMap();
		if(map == null)
			throw new IllegalArgumentException("Network is not an Enrichment Map.");
		
		results = map.getDataSetNames();
	}
	
	
	@Override
	public List<Class<?>> getResultClasses() {
		return Arrays.asList(String.class, List.class);
	}
	
	@Override
	public <R> R getResults(Class<? extends R> type) {
		if(String.class.equals(type)) {
			return type.cast(String.join(",", results));
		} else if(List.class.equals(type)) {
			return type.cast(results);
		}
		return null;
	}

}
