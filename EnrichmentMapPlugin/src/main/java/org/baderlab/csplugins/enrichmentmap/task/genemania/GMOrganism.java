package org.baderlab.csplugins.enrichmentmap.task.genemania;

import java.io.Serializable;

public class GMOrganism implements Serializable {

	private static final long serialVersionUID = -4488165932347985569L;
	
	private long taxonomyId;
	private String scientificName;
	private String abbreviatedName;
	private String commonName;

	public GMOrganism() {
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

	public String getAbbreviatedName() {
		return abbreviatedName;
	}

	public void setAbbreviatedName(String abbreviatedName) {
		this.abbreviatedName = abbreviatedName;
	}

	public String getCommonName() {
		return commonName;
	}

	public void setCommonName(String commonName) {
		this.commonName = commonName;
	}

	@Override
	public String toString() {
		return scientificName;
	}
}