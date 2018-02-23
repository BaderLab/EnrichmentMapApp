package org.baderlab.csplugins.enrichmentmap.model;

/**
 * The "gene universe", typically used as a parameter to 
 * the Hypergeometric test when running post-analysis.
 */
public enum UniverseType {
	
	GMT, 
	EXPRESSION_SET, 
	INTERSECTION, 
	USER_DEFINED;
	
	
	public int getGeneUniverse(EnrichmentMap map, String datasetName, int userDefinedUniverseSize) {
		EMDataSet dataset = map.getDataSet(datasetName);
		switch(this) {
			default:
			case GMT:
				return dataset.getGeneSetGenes().size(); // number of un-filtered genes from the original GMT file (GMT)
			case EXPRESSION_SET:
				return dataset.getExpressionSets().getExpressionUniverse();
			case INTERSECTION:
				return dataset.getExpressionSets().getExpressionMatrix().size();
			case USER_DEFINED:
				return userDefinedUniverseSize;
		}
	}
	
	public int getGeneUniverse(EnrichmentMap map, String dataset) {
		if(this == UniverseType.USER_DEFINED)
			throw new IllegalArgumentException();
		return getGeneUniverse(map, dataset, 0);
	}
	
}