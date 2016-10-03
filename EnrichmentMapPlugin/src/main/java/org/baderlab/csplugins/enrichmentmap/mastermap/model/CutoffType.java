package org.baderlab.csplugins.enrichmentmap.mastermap.model;

public enum CutoffType {
	
	JACCARD("Jaccard"),
	OVERLAP("Overlap"),
	COMBINED("Jaccard+Overlap Combined");
	
	public final String display;
	
	private CutoffType(String display) {
		this.display = display;
	}
}
