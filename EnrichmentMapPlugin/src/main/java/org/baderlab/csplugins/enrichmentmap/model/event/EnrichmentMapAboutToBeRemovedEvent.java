package org.baderlab.csplugins.enrichmentmap.model.event;

import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMapManager;
import org.cytoscape.event.AbstractCyEvent;
import org.cytoscape.view.model.CyNetworkView;

public class EnrichmentMapAboutToBeRemovedEvent extends AbstractCyEvent<EnrichmentMapManager> {

	private final EnrichmentMap map;
	private final CyNetworkView networkView;
	
	public EnrichmentMapAboutToBeRemovedEvent(EnrichmentMapManager source, EnrichmentMap map, CyNetworkView networkView) {
		super(source, EnrichmentMapAboutToBeRemovedListener.class);
		this.map = map;
		this.networkView = networkView;
	}
	
	public EnrichmentMap getEnrichmentMap() {
		return map;
	}
	
	public CyNetworkView getNetworkView() {
		return networkView;
	}
}
