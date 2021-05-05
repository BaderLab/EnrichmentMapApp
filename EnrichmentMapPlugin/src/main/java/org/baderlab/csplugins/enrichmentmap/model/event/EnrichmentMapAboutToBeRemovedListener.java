package org.baderlab.csplugins.enrichmentmap.model.event;

import org.cytoscape.event.CyListener;

public interface EnrichmentMapAboutToBeRemovedListener extends CyListener {

	public void handleEvent(EnrichmentMapAboutToBeRemovedEvent e);
	
}
