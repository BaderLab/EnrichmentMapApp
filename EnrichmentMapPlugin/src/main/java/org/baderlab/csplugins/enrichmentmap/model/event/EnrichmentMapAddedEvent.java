package org.baderlab.csplugins.enrichmentmap.model.event;

import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMapManager;
import org.cytoscape.event.AbstractCyEvent;


public class EnrichmentMapAddedEvent extends AbstractCyEvent<EnrichmentMapManager> {

	private final EnrichmentMap map;
	
	public EnrichmentMapAddedEvent(EnrichmentMapManager source, EnrichmentMap map) {
		super(source, EnrichmentMapAddedListener.class);
		this.map = map;
	}
	
	public EnrichmentMap getEnrichmentMap() {
		return map;
	}
}
