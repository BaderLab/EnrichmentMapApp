package org.baderlab.csplugins.enrichmentmap.model;

/**
 * Parameters used for specifying filters and cutoffs for post-analysis.
 */
public class PostAnalysisFilterParameters {
	
	private final PostAnalysisFilterType type;
	private final double value;
	

	public PostAnalysisFilterParameters(PostAnalysisFilterType type, double value) {
		this.type = type;
		this.value = value;
	}
	
	public PostAnalysisFilterParameters(PostAnalysisFilterType type) {
		this(type, type.defaultValue);
	}
	
	public PostAnalysisFilterType getType() {
		return type;
	}

	public double getValue() {
		return value;
	}
}
