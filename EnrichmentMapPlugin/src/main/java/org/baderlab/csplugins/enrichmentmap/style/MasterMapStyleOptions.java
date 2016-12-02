package org.baderlab.csplugins.enrichmentmap.style;

import java.util.Collection;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.baderlab.csplugins.enrichmentmap.model.DataSet;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.cytoscape.view.model.CyNetworkView;

public class MasterMapStyleOptions {
	
	private final CyNetworkView networkView;
	private final EnrichmentMap map;
	private final Predicate<DataSet> filter;
	
	/**
	 * It is assumed that all the given DataSets come from the same EnrichmentMap.
	 */
	public MasterMapStyleOptions(CyNetworkView networkView, EnrichmentMap map, Predicate<DataSet> filter) {
		this.networkView = networkView;
		this.map = map;
		this.filter = filter;
	}

	public MasterMapStyleOptions(CyNetworkView networkView, EnrichmentMap map) {
		this(networkView, map, x -> true);
	}
	
	
	public CyNetworkView getNetworkView() {
		return networkView;
	}
	
	public Collection<DataSet> getDataSets() {
		return map.getDatasetList().stream().filter(filter).collect(Collectors.toList());
	}
	
	public EnrichmentMap getEnrichmentMap() {
		return map;
	}
	
	public String getAttributePrefix() {
		return map.getParams().getAttributePrefix();
	}
	
}
