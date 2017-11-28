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
	
	
	public int getGeneUniverse(EnrichmentMap map, String dataset, int userDefinedUniverseSize) {
		GeneExpressionMatrix expressionSets = map.getDataSet(dataset).getExpressionSets();
		switch(this) {
			default:
			case GMT:
				return map.getNumberOfGenes();
			case EXPRESSION_SET:
				return expressionSets.getExpressionUniverse();
			case INTERSECTION:
				return expressionSets.getExpressionMatrix().size();
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