package org.baderlab.csplugins.enrichmentmap.style;

import java.util.Collection;
import java.util.stream.Collectors;

import org.baderlab.csplugins.enrichmentmap.model.AbstractDataSet;
import org.baderlab.csplugins.enrichmentmap.model.Compress;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.model.ExpressionData;
import org.cytoscape.view.model.CyNetworkView;

public class AssociatedStyleOptions {

	private final CyNetworkView networkView;
	private final EnrichmentMap map;
	private final Compress compress;
	private final ExpressionData expressionData;
	private final ChartOptions chartOptions;
	
	public AssociatedStyleOptions(
			CyNetworkView networkView,
			EnrichmentMap map,
			Compress compress,
			ExpressionData expressionData,
			ChartOptions chartOptions
	) {
		this.networkView = networkView;
		this.map = map;
		this.compress = compress;
		this.expressionData = expressionData;
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
	
	public Compress getCompress() {
		return compress;
	}
	
	public ExpressionData getExpressionData() {
		return expressionData;
	}
	
	public ChartOptions getChartOptions() {
		return chartOptions;
	}
}
