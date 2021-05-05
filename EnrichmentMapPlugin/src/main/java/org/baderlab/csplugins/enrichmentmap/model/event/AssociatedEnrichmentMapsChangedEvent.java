package org.baderlab.csplugins.enrichmentmap.model.event;

import java.util.Map;

import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMapManager;
import org.cytoscape.event.AbstractCyEvent;

public class AssociatedEnrichmentMapsChangedEvent extends AbstractCyEvent<EnrichmentMapManager> {

	private final EnrichmentMap map;
	private final Map<Long,EnrichmentMap> associatedMaps;
	
	public AssociatedEnrichmentMapsChangedEvent(EnrichmentMapManager source, EnrichmentMap map, Map<Long,EnrichmentMap> associatedMaps) {
		super(source, AssociatedEnrichmentMapsChangedListener.class);
		this.map = map;
		this.associatedMaps = associatedMaps;
	}
	
	public EnrichmentMap getEnrichmentMap() {
		return map;
	}
	
	public Map<Long,EnrichmentMap> getAssociatedMaps() {
		return associatedMaps;
	}
}
