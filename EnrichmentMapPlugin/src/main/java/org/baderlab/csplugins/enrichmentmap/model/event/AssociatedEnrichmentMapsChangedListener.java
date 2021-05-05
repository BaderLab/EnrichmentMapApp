package org.baderlab.csplugins.enrichmentmap.model.event;

import org.cytoscape.event.CyListener;

public interface AssociatedEnrichmentMapsChangedListener extends CyListener {
	
	public void handleEvent(AssociatedEnrichmentMapsChangedEvent e);
	
}
