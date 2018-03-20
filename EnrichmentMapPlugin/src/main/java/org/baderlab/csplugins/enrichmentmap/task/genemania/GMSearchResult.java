package org.baderlab.csplugins.enrichmentmap.task.genemania;

import java.io.Serializable;
import java.util.Collection;

public class GMSearchResult implements Serializable {
	
	private static final long serialVersionUID = -795122082632063250L;
	
	private GMOrganism organism;
	private String combiningMethod;
	private Collection<GMGene> genes;
	private Long network;

	public GMOrganism getOrganism() {
		return organism;
	}

	public void setOrganism(GMOrganism organism) {
		this.organism = organism;
	}

	public String getCombiningMethod() {
		return combiningMethod;
	}

	public void setCombiningMethod(String combiningMethod) {
		this.combiningMethod = combiningMethod;
	}

	public Collection<GMGene> getGenes() {
		return genes;
	}

	public void setGenes(Collection<GMGene> genes) {
		this.genes = genes;
	}

	public Long getNetwork() {
		return network;
	}

	public void setNetwork(Long network) {
		this.network = network;
	}
}
