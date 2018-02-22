package org.baderlab.csplugins.enrichmentmap.style;

import java.util.Collection;
import java.util.stream.Collectors;

import org.baderlab.csplugins.enrichmentmap.model.AbstractDataSet;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.view.heatmap.HeatMapParams;
import org.cytoscape.view.model.CyNetworkView;

public class GMStyleOptions {

	private final CyNetworkView networkView;
	private final EnrichmentMap map;
	private final HeatMapParams params;
	private final ChartOptions chartOptions;
	
	public GMStyleOptions(CyNetworkView networkView, EnrichmentMap map, HeatMapParams params,
			ChartOptions chartOptions) {
		super();
		this.networkView = networkView;
		this.map = map;
		this.params = params;
		this.chartOptions = chartOptions;
	}

	public CyNetworkView getNetworkView() {
		return networkView;
	}
	
	public Collection<AbstractDataSet> getDataSets() {
		return map.getDataSetList().stream().collect(Collectors.toList());
	}
	
	public EnrichmentMap getEnrichmentMap() {
		return map;
	}
	
	public HeatMapParams getHeatMapParams() {
		return params;
	}
	
	public ChartOptions getChartOptions() {
		return chartOptions;
	}
}
