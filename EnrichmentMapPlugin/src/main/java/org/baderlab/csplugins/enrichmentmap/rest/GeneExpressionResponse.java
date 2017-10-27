package org.baderlab.csplugins.enrichmentmap.rest;

public class GeneExpressionResponse {

	private String geneName;
	private float[] values;
	
	public GeneExpressionResponse(String geneName, float[] values) {
		this.geneName = geneName;
		this.values = values;
	}
	
	public String getGeneName() {
		return geneName;
	}
	
	public float[] getValues() {
		return values;
	}
	
}
