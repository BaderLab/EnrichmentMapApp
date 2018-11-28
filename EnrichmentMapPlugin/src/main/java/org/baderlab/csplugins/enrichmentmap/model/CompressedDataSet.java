package org.baderlab.csplugins.enrichmentmap.model;

import java.util.List;
import java.util.Optional;

public class CompressedDataSet implements ExpressionData {
	
	private final ExpressionCache expressionCache;
	private final List<EMDataSet> datasets;
	private final boolean isDistinctExpressionSets;
	
	public CompressedDataSet(List<EMDataSet> datasets, ExpressionCache expressionCache, boolean isDistinctExpressionSets) {
		this.datasets = datasets;
		this.expressionCache = expressionCache;
		this.isDistinctExpressionSets = isDistinctExpressionSets;
	}
	
	@Override
	public EMDataSet getDataSet(int idx) {
		return datasets.get(idx);
	}
	
	@Override
	public double getValue(int geneID, int idx, Compress compress, Transform transform) {
		EMDataSet dataset = getDataSet(idx);
		Optional<float[]> expression = expressionCache.getExpressions(geneID, dataset, transform);
		
		if (compress == null || !expression.isPresent())
			return Float.NaN;
		
		switch (compress) {
			case DATASET_MEDIAN:	return GeneExpression.median(expression.get());
			case DATASET_MAX:	return GeneExpression.max(expression.get());
			case DATASET_MIN:	return GeneExpression.min(expression.get());
			default:				return Float.NaN;
		}
	}

	@Override
	public String getName(int idx) {
		EMDataSet dataset = getDataSet(idx);
		return isDistinctExpressionSets ? dataset.getName() : "Expressions";
	}

	@Override
	public int getSize() {
		return datasets.size();
	}
}
