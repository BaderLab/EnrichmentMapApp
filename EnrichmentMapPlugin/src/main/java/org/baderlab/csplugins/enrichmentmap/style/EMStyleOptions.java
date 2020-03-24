package org.baderlab.csplugins.enrichmentmap.style;

import java.util.Collection;
import java.util.Objects;

import org.baderlab.csplugins.enrichmentmap.model.AbstractDataSet;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.cytoscape.view.model.CyNetworkView;

public class EMStyleOptions {
	
	private final CyNetworkView networkView;
	private final EnrichmentMap map;
	private final ChartOptions chartOptions;
	private boolean postAnalysis;
	private final boolean publicationReady;
	private final Collection<? extends AbstractDataSet> dataSets;
	
	/**
	 * It is assumed that all the given DataSets come from the same EnrichmentMap.
	 */
	public EMStyleOptions(CyNetworkView networkView, EnrichmentMap map, Collection<? extends AbstractDataSet> dataSets,
			ChartOptions chartOptions, boolean postAnalysis, boolean publicationReady) {
		this.networkView = Objects.requireNonNull(networkView);
		this.map = Objects.requireNonNull(map);
		this.dataSets = Objects.requireNonNull(dataSets);
		this.chartOptions = chartOptions;
		this.postAnalysis = postAnalysis;
		this.publicationReady = publicationReady;
	}
	
	public EMStyleOptions(CyNetworkView networkView, EnrichmentMap map) {
		this(networkView, map, map.getDataSetList(), null, false, false);
	}
	
	public CyNetworkView getNetworkView() {
		return networkView;
	}
	
	public Collection<? extends AbstractDataSet> getDataSets() {
		return dataSets;
	}
	
	public EnrichmentMap getEnrichmentMap() {
		return map;
	}
	
	public String getAttributePrefix() {
		return map.getParams().getAttributePrefix();
	}
	
	public ChartOptions getChartOptions() {
		return chartOptions;
	}
	
	public boolean isPublicationReady() {
		return publicationReady;
	}

	public boolean isPostAnalysis() {
		return postAnalysis;
	}
	
	public void setPostAnalysis(boolean postAnalysis) {
		this.postAnalysis = postAnalysis;
	}
}
