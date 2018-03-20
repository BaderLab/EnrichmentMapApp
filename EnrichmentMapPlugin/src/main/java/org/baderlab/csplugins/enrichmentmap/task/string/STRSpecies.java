package org.baderlab.csplugins.enrichmentmap.task.string;

import java.io.Serializable;

public class STRSpecies implements Serializable {

	private static final long serialVersionUID = 8696720316867220538L;
	
	private long taxonomyId;
	private String scientificName;

	public STRSpecies() {
	}

	public long getTaxonomyId() {
		return taxonomyId;
	}

	public void setTaxonomyId(long taxonomyId) {
		this.taxonomyId = taxonomyId;
	}

	public String getScientificName() {
		return scientificName;
	}

	public void setScientificName(String scientificName) {
		this.scientificName = scientificName;
	}

	@Override
	public String toString() {
		return scientificName;
	}
}
