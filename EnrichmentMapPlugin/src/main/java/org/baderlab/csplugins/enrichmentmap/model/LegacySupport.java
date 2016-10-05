package org.baderlab.csplugins.enrichmentmap.model;

import java.util.Set;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.session.CySessionManager;

import com.google.inject.Inject;

/**
 * Temporary repositor for methods that work with legacy EnrichmentMaps.
 * Eventually all this code shoudl be deprecated and/or removed.
 */
public class LegacySupport {
	
	public static final String EM_NAME = "Enrichment Map";
	
	
	@Inject private EnrichmentMapManager emManager;
	@Inject private CySessionManager sessionManager;

	
	/**
	 * The attribute prefix is based on the number of nextworks in cytoscape.
	 * make attribute prefix independent of cytoscape
	 */
	public String getNextAttributePrefix() {
		Set<CyNetwork> networks = sessionManager.getCurrentSession().getNetworks();

		if(networks == null || networks.isEmpty()) {
			return "EM1_";
		}
		else {
			// how many enrichment maps are there?
			int max_prefix = 0;
			// go through all the networks, check to see if they are enrichment
			// maps
			// if they are then calculate the max EM_# and use the max number +
			// 1 for the
			// current attributes
			for(CyNetwork current_network : networks) {
				Long networkId = current_network.getSUID();
				if(emManager.isEnrichmentMap(networkId)) {// fails
					EnrichmentMap tmpMap = emManager.getEnrichmentMap(networkId);
					String tmpPrefix = tmpMap.getParams().getAttributePrefix();
					tmpPrefix = tmpPrefix.replace("EM", "");
					tmpPrefix = tmpPrefix.replace("_", "");
					int tmpNum = Integer.parseInt(tmpPrefix);
					if(tmpNum > max_prefix) {
						max_prefix = tmpNum;
					}
				}
			}
			return "EM" + (max_prefix + 1) + "_";
		}
	}
}
