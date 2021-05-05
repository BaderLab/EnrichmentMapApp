package org.baderlab.csplugins.enrichmentmap.model.event;

import org.cytoscape.event.CyListener;

public interface EnrichmentMapAddedListener extends CyListener {
	
	public void handleEvent(EnrichmentMapAddedEvent e);

}
