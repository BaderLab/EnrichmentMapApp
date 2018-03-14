package org.baderlab.csplugins.enrichmentmap.style;

import java.util.List;
import java.util.stream.Collectors;

import org.baderlab.csplugins.enrichmentmap.model.AbstractDataSet;
import org.baderlab.csplugins.enrichmentmap.model.AssociatedApp;
import org.baderlab.csplugins.enrichmentmap.model.Compress;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.model.ExpressionData;
import org.baderlab.csplugins.enrichmentmap.model.Transform;
import org.cytoscape.view.model.CyNetworkView;

public class AssociatedStyleOptions {

	private final CyNetworkView networkView;
	private final EnrichmentMap map;
	private final Transform transform;
	private final Compress compress;
	private final ExpressionData expressionData;
	private final ChartOptions chartOptions;
	private final AssociatedApp associatedApp;
	
	public AssociatedStyleOptions(
			CyNetworkView networkView,
			EnrichmentMap map,
			Transform transform,
			Compress compress,
			ExpressionData expressionData,
			ChartOptions chartOptions,
			AssociatedApp associatedApp
	) {
		this.networkView = networkView;
		this.map = map;
		this.transform = transform;
		this.compress = compress;
		this.expressionData = expressionData;
		this.chartOptions = chartOptions;
		this.associatedApp = associatedApp;
	}

	public CyNetworkView getNetworkView() {
		return networkView;
	}
	
	public List<AbstractDataSet> getDataSets() {
		return map.getDataSetList().stream().collect(Collectors.toList());
	}
	
	public EnrichmentMap getEnrichmentMap() {
		return map;
	}
	
	public Transform getTransform() {
		return transform;
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
	
	public AssociatedApp getAssociatedApp() {
		return associatedApp;
	}
}
