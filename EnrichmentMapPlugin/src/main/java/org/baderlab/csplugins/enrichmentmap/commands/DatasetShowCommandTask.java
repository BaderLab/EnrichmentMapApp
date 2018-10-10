package org.baderlab.csplugins.enrichmentmap.commands;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.baderlab.csplugins.enrichmentmap.commands.tunables.DatasetListTunable;
import org.baderlab.csplugins.enrichmentmap.commands.tunables.NetworkTunable;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.view.control.ControlPanelMediator;
import org.baderlab.csplugins.enrichmentmap.view.control.io.ViewParams;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ContainsTunables;
import org.cytoscape.work.TaskMonitor;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

public class DatasetShowCommandTask extends AbstractTask {

	@Inject private ControlPanelMediator controlPanelMediator;
	
	private final boolean show;
	
	@ContainsTunables @Inject
	public NetworkTunable networkTunable;
	
	@ContainsTunables @Inject
	public DatasetListTunable datasetListTunable;
	
	
	public static interface Factory {
		DatasetShowCommandTask create(boolean show);
	}
	
	@Inject
	public DatasetShowCommandTask(@Assisted boolean show) {
		this.show = show;
	}
	
	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		CyNetworkView networkView = networkTunable.getNetworkView();
		EnrichmentMap map = networkTunable.getEnrichmentMap();
		if(networkView == null || map == null)
			throw new IllegalArgumentException("network is not an EnrichmentMap network");

		Set<String> dataSetNames = datasetListTunable.getDataSetNames(map);
		if(dataSetNames.isEmpty())
			throw new IllegalArgumentException("no valid data sets");
		
		Map<Long,ViewParams> viewParamsMap = controlPanelMediator.getAllViewParams();
		ViewParams params = viewParamsMap.get(networkView.getSUID());
		
		Set<String> filteredOutNames = new HashSet<>(params.getFilteredOutDataSets());
		if(show) {
			filteredOutNames.removeAll(dataSetNames);
		} else {
			filteredOutNames.addAll(dataSetNames);
		}
		
		params.setFilteredOutDataSets(filteredOutNames);
		controlPanelMediator.reset(params);
	}

}
