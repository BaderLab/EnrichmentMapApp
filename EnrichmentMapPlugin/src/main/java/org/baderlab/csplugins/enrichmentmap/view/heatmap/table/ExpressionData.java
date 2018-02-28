package org.baderlab.csplugins.enrichmentmap.view.heatmap.table;

import java.util.Optional;

import org.baderlab.csplugins.enrichmentmap.model.EMDataSet;

/**
 * Common interface for different levels of compression.
 */
public interface ExpressionData {
	
	EMDataSet getDataSet(int col);

	double getValue(int geneID, int col);

	String getName(int col);

	public default Optional<String> getPhenotype(int col) {
		return Optional.empty();
	};

	int getSize();
}
