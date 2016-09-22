package org.baderlab.csplugins.enrichmentmap.mastermap.model;

public enum CutoffType {
	
	JACCARD("Jaccard"),
	OVERLAP("Overlap"),
	COMBINED("Jaccard+Overlap Combined");
	
	private final String display;
	
	private CutoffType(String display) {
		this.display = display;
	}
	
	public String getDisplay() {
		return display;
	}
}
