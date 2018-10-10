package org.baderlab.csplugins.enrichmentmap.commands;

import java.util.Collection;

import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMapManager;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.work.Tunable;

import com.google.inject.Inject;

public class NetworkTunable {
	
	@Inject private CyApplicationManager applicationManager;
	@Inject private CyNetworkViewManager networkViewManager;
	@Inject private EnrichmentMapManager emManager;
	
	@Tunable
	public CyNetwork network;
	
	
	public CyNetwork getNetwork() {
		if(network == null)
			return applicationManager.getCurrentNetwork();
		return null;
	}
	
	public CyNetworkView getNetworkView() {
		CyNetwork network = getNetwork();
		if(network == null) {
			return null;
		}
		
		Collection<CyNetworkView> networkViews = networkViewManager.getNetworkViews(network);
		if(networkViews == null || networkViews.isEmpty())
			return null;
		return networkViews.iterator().next();
	}
	
	public EnrichmentMap getEnrichmentMap() {
		CyNetwork network = getNetwork();
		if(network == null)
			return null;
		return emManager.getEnrichmentMap(getNetwork().getSUID()); 
	}
	
	
	
}
