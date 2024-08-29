package org.baderlab.csplugins.enrichmentmap.model;

/**
 * Common interface for different levels of compression.
 */
public interface ExpressionData {
	
	EMDataSet getDataSet(int col);

	double getValue(int geneID, int col, Compress compress, Transform transform);

	String getName(int col);

	public default Phenotype getPhenotype(int col) {
		return null;
	};

	int getSize();
}
