package org.baderlab.csplugins.enrichmentmap.model;

import java.util.List;
import java.util.NavigableMap;
import java.util.Optional;
import java.util.TreeMap;

public class Uncompressed implements ExpressionData {

	private final ExpressionCache expressionCache;
	private final NavigableMap<Integer, EMDataSet> colToDataSet = new TreeMap<>();
	private final int expressionCount;
	
	public Uncompressed(List<EMDataSet> datasets, ExpressionCache expressionCache) {
		this.expressionCache = expressionCache;
		int rangeFloor = 0;
		colToDataSet.put(0, null);

		for (EMDataSet dataset : datasets) {
			GeneExpressionMatrix matrix = dataset.getExpressionSets();
			colToDataSet.put(rangeFloor, dataset);
			rangeFloor += matrix.getNumConditions() - 2;
		}

		expressionCount = rangeFloor;
	}
	
	@Override
	public EMDataSet getDataSet(int idx) {
		return colToDataSet.floorEntry(idx).getValue();
	}
	
	private int getIndexInDataSet(int idx) {
		int start = colToDataSet.floorKey(idx);
		
		return idx - start;
	}
	
	@Override
	public double getValue(int geneID, int idx, Compress compress, Transform transform) {
		EMDataSet dataset = getDataSet(idx);
		
		return expressionCache.getExpression(geneID, dataset, transform, getIndexInDataSet(idx));
	}

	@Override
	public String getName(int idx) {
		EMDataSet dataset = getDataSet(idx);
		String[] columns = dataset.getExpressionSets().getColumnNames();
		int index = getIndexInDataSet(idx) + 2;
		
		return columns[index];
	}

	@Override
	public int getSize() {
		return expressionCount;
	}

	@Override
	public Optional<String> getPhenotype(int idx) {
		EMDataSet dataset = getDataSet(idx);
		int index = getIndexInDataSet(idx);
		String[] classes = dataset.getEnrichments().getPhenotypes();
		
		if (classes != null && index < classes.length)
			return Optional.ofNullable(classes[index]);
		
		return Optional.empty();
	}
}
