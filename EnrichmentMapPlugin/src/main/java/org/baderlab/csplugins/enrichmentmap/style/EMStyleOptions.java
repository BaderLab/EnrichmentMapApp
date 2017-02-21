package org.baderlab.csplugins.enrichmentmap.style;

import java.util.Collection;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.baderlab.csplugins.enrichmentmap.model.EMDataSet;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.cytoscape.view.model.CyNetworkView;

public class EMStyleOptions {
	
	private final CyNetworkView networkView;
	private final EnrichmentMap map;
	private final Predicate<EMDataSet> filter;
	private final boolean postAnalysis;
	private final boolean publicationReady;
	
	/**
	 * It is assumed that all the given DataSets come from the same EnrichmentMap.
	 */
	public EMStyleOptions(CyNetworkView networkView, EnrichmentMap map, Predicate<EMDataSet> filter,
			 boolean postAnalysis, boolean publicationReady) {
		this.networkView = networkView;
		this.map = map;
		this.filter = filter;
		this.postAnalysis = postAnalysis;
		this.publicationReady = publicationReady;
	}
	
	public EMStyleOptions(CyNetworkView networkView, EnrichmentMap map) {
		this(networkView, map, x -> true, false, false);
	}
	
	public CyNetworkView getNetworkView() {
		return networkView;
	}
	
	public Collection<EMDataSet> getDataSets() {
		return map.getDatasetList().stream().filter(filter).collect(Collectors.toList());
	}
	
	public EnrichmentMap getEnrichmentMap() {
		return map;
	}
	
	public String getAttributePrefix() {
		return map.getParams().getAttributePrefix();
	}
	
	public boolean isPublicationReady() {
		return publicationReady;
	}

	public boolean isPostAnalysis() {
		return postAnalysis;
	}
}
