package org.baderlab.csplugins.enrichmentmap.model;

import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.TreeMap;

import org.baderlab.csplugins.enrichmentmap.model.PhenotypeHighlight.Highlight;


public class Uncompressed implements ExpressionData {

	private final EnrichmentMap map;
	private final ExpressionCache expressionCache;
	private final NavigableMap<Integer, EMDataSet> colToDataSet = new TreeMap<>();
	private final int expressionCount;
	
	private final boolean commonButDiffPheno;
	private final Map<String,PhenotypeHighlight> highlightPhenos;
	
	public Uncompressed(EnrichmentMap map, List<EMDataSet> datasets, ExpressionCache expressionCache) {
		this.map = map;
		this.expressionCache = expressionCache;
		
		commonButDiffPheno = ExpressionData.commonExpressionsButDifferentPhenotypes(map);
		if(commonButDiffPheno) {
			this.highlightPhenos = ExpressionData.getPhenotypesToHighlight(map.getDataSetList()); 
		} else {
			this.highlightPhenos = null;
		}
		
		int rangeFloor = 0;
		colToDataSet.put(0, null);

		for(EMDataSet dataset : datasets) {
			GeneExpressionMatrix matrix = dataset.getExpressionSets();
			colToDataSet.put(rangeFloor, dataset);
			rangeFloor += matrix.getNumConditions() - 2;
		}

		expressionCount = rangeFloor;
	}
	
	@Override
	public boolean commonButDiffPheno() {
		return commonButDiffPheno;
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
		int indexInDataSet = getIndexInDataSet(idx);
		return expressionCache.getExpression(geneID, dataset, transform, indexInDataSet);
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
	public PhenotypeHighlight getHighlight(int idx) {
		EMDataSet dataset = getDataSet(idx);
		int index = getIndexInDataSet(idx);
		
		var enrichments = dataset.getEnrichments();
		String[] classes = enrichments.getPhenotypes();
		
		if(classes == null || index >= classes.length)
			return null;
		
		String pheno = classes[index];
		if(pheno == null) // being defensive, don't think this can actually happen
			return null;
		
		if(commonButDiffPheno) {
			var highlight = highlightPhenos.get(pheno);
			if (highlight == null) {
				return new PhenotypeHighlight(map.getDataSetList(), pheno, Highlight.NONE);
			} else {
				return highlight;
			}
			
		} else {
			Highlight highlight;
			if(Objects.equals(pheno, enrichments.getPhenotype1())) {
				highlight = Highlight.POSITIVE;
			} else if(Objects.equals(pheno, enrichments.getPhenotype2())) {
				highlight = Highlight.NEGATIVE;
			} else {
				highlight = Highlight.NONE;
			}
			return new PhenotypeHighlight(dataset, pheno, highlight);
		}
	}
	
}
