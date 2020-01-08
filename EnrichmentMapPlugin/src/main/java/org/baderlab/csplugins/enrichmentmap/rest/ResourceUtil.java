package org.baderlab.csplugins.enrichmentmap.rest;

import java.util.Optional;

import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMapManager;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;

import com.google.inject.Inject;

public class ResourceUtil {

	@Inject private EnrichmentMapManager emManager;
	@Inject private CyNetworkManager networkManager;
	
	
	public Optional<EnrichmentMap> getEnrichmentMap(String network) {
		try {
			long suid = Long.parseLong(network);
			return Optional.ofNullable(emManager.getEnrichmentMap(suid));
		} catch(NumberFormatException e) {
			Optional<Long> suid = getNetworkByName(network);
			return suid.map(emManager::getEnrichmentMap);
		}
	}
	
	private Optional<Long> getNetworkByName(String name) {
		for(CyNetwork network : networkManager.getNetworkSet()) {
			String netName = network.getRow(network).get(CyNetwork.NAME, String.class);
			if(name.equals(netName)) {
				return Optional.of(network.getSUID());
			}
		}
		return Optional.empty();
	}
}
