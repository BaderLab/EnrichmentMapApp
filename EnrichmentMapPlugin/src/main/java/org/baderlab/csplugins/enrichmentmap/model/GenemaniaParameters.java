package org.baderlab.csplugins.enrichmentmap.model;

import org.cytoscape.model.CyNetwork;

public class GenemaniaParameters {

	// the SUID of the genemania network
	private final CyNetwork network;

	public GenemaniaParameters(CyNetwork network) {
		this.network = network;
	}

	public CyNetwork getNetwork() {
		return network;
	}

	
}
