package org.baderlab.csplugins.enrichmentmap.commands;

import java.awt.Color;
import java.util.List;
import java.util.Map;

import org.baderlab.csplugins.enrichmentmap.commands.tunables.DatasetColorTunable;
import org.baderlab.csplugins.enrichmentmap.commands.tunables.NetworkTunable;
import org.baderlab.csplugins.enrichmentmap.model.EMDataSet;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.style.EMStyleBuilder.StyleUpdateScope;
import org.baderlab.csplugins.enrichmentmap.style.EMStyleOptions;
import org.baderlab.csplugins.enrichmentmap.view.control.ControlPanelMediator;
import org.baderlab.csplugins.enrichmentmap.view.heatmap.HeatMapMediator;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ContainsTunables;
import org.cytoscape.work.TaskMonitor;

import com.google.inject.Inject;
import com.google.inject.Provider;

public class DatasetColorCommandTask extends AbstractTask {

	@Inject private Provider<HeatMapMediator> heatMapMediatorProvider;
	@Inject private ControlPanelMediator controlPanelMediator;
	
	@ContainsTunables @Inject
	public NetworkTunable networkTunable;
	
	@ContainsTunables @Inject
	public DatasetColorTunable datasetColorTunable;
	
	@Override
	public void run(TaskMonitor tm) throws Exception {
		tm.setTitle("Change Data Set Colors");
		tm.setProgress(0.1);
		
		CyNetworkView netView = networkTunable.getNetworkView();
		EnrichmentMap map = networkTunable.getEnrichmentMap();
		if(netView == null || map == null)
			throw new IllegalArgumentException("network is not an EnrichmentMap network");
		
		Map<String,Color> colors = datasetColorTunable.getColors();
		if(colors.isEmpty())
			throw new IllegalArgumentException("no colors specified");
		
		List<EMDataSet> datasetList = map.getDataSetList();
		
		for(var entry : colors.entrySet()) {
			String dsName = entry.getKey();
			Color color = entry.getValue();
			
			EMDataSet ds = map.getDataSet(dsName);
			if(ds != null) {
				ds.setColor(color);
				tm.setStatusMessage("Color changed for dataset: '" + dsName + "'");
			} else {
				try {
					int index = Integer.parseInt(dsName);
					ds = datasetList.get(index);
					ds.setColor(color);
					tm.setStatusMessage("Color changed for dataset: '" + dsName + "'");
				} catch(NumberFormatException | IndexOutOfBoundsException e) {
					tm.setStatusMessage("Dataset not found: '" + dsName + "'");
				}
			}
		};
		
		// update panels
		controlPanelMediator.updateDataSetList(networkTunable.getNetworkView());
		heatMapMediatorProvider.get().reset();

		// update visual style
		EMStyleOptions styleOptions = controlPanelMediator.createStyleOptions(netView);
		controlPanelMediator.applyVisualStyle(styleOptions, StyleUpdateScope.ONLY_DATASETS);
		heatMapMediatorProvider.get().reset();
		
		tm.setProgress(1.0);
	}
}
