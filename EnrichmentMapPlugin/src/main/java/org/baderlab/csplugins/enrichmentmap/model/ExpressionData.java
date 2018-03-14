package org.baderlab.csplugins.enrichmentmap.model;

import java.util.Optional;

/**
 * Common interface for different levels of compression.
 */
public interface ExpressionData {
	
	EMDataSet getDataSet(int col);

	double getValue(int geneID, int col, Compress compress);

	String getName(int col);

	public default Optional<String> getPhenotype(int col) {
		return Optional.empty();
	};

	int getSize();
}
