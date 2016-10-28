package org.baderlab.csplugins.enrichmentmap.style;

import java.util.Collection;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.baderlab.csplugins.enrichmentmap.model.DataSet;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;

public class MasterMapStyleOptions {

	private final EnrichmentMap map;
	private final Predicate<DataSet> filter;
	
	/**
	 * It is assumed that all the given DataSets come from the same EnrichmentMap.
	 */
	public MasterMapStyleOptions(EnrichmentMap map, Predicate<DataSet> filter) {
		this.map = map;
		this.filter = filter;
	}

	public MasterMapStyleOptions(EnrichmentMap map) {
		this(map, x -> true);
	}
	
	public Collection<DataSet> getDataSets() {
		return map.getDatasetList().stream().filter(filter).collect(Collectors.toList());
	}
	
	public EnrichmentMap getEnrichmentMap() {
		return map;
	}
	
}
