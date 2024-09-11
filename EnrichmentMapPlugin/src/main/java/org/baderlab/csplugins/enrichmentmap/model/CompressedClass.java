package org.baderlab.csplugins.enrichmentmap.model;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;

import org.baderlab.csplugins.enrichmentmap.model.Phenotype.Type;

public class CompressedClass implements ExpressionData {

	private final ExpressionCache expressionCache;
	private List<Phenotype> headers = new ArrayList<>();
	
	/**
	 * If the expressions are the same for multiple data sets then we will only get one data set passed in
	 * to the constructor, even if the map has more than one data set.
	 */
	public CompressedClass(EnrichmentMap map, List<EMDataSet> datasets, ExpressionCache expressionCache) {
		this.expressionCache = expressionCache;
		
		// Special case. There is more than one dataset in the map, and they have the same expressions,
		// but the pheontypes are different. We want to show the phenotypes for the selected data sets
		// but we don't want to repeat all the expression data.
		if(ExpressionData.commonExpressionsButDifferentPhenotypes(map)) {
			// we only get passed one dataset because expressions are the same
			var dataset = datasets.get(0); 
			var classPhenos = ExpressionData.getPhenotypesFromClassFile(dataset);
			var highlightPhenos = ExpressionData.getPhenotypesToHighlight(map.getDataSetList()); // get the phenotypes from all the data sets
			addHeaders(dataset, classPhenos, highlightPhenos);
			
		} else {
			for(var dataset : datasets) {
				var classPhenos = ExpressionData.getPhenotypesFromClassFile(dataset);
				var highlightPhenos = ExpressionData.getPhenotypesToHighlight(dataset);
				addHeaders(dataset, classPhenos, highlightPhenos);
			}
		}
	}
	
	
	private void addHeaders(EMDataSet mainDataSet, LinkedHashSet<String> classPhenos, List<Phenotype> highlightPhenos) {
		// move highlighted phenotypes to the front
		for(Phenotype pheno : highlightPhenos) {
			if(classPhenos.contains(pheno.getName())) {
				headers.add(pheno);
			}
		}
		for(Phenotype pheno : highlightPhenos) {
			classPhenos.remove(pheno.getName());
		}
		
		for(String phenoName : classPhenos) {
			headers.add(new Phenotype(mainDataSet, phenoName, Type.OTHER));
		}
	}
	
	
	@Override
	public EMDataSet getDataSet(int idx) {
		return headers.get(idx).getDataSet();
	}

	@Override
	public String getName(int idx) {
		return getPhenotype(idx).getName();
	}
	
	@Override
	public Phenotype getPhenotype(int idx) {
		return headers.get(idx);
	}
	
	@Override
	public double getValue(int geneID, int idx, Compress compress, Transform transform) {
		EMDataSet dataset = getDataSet(idx);
		String pheno = getName(idx);

		String[] phenotypes = dataset.getEnrichments().getPhenotypes();
		if (phenotypes == null || phenotypes.length == 0)
			return Double.NaN;

		Optional<float[]> optExpr = expressionCache.getExpressions(geneID, dataset, transform);
		if (!optExpr.isPresent())
			return Double.NaN;

		float[] expressions = optExpr.get();
		if (expressions.length == 0 || expressions.length != phenotypes.length)
			return Double.NaN;

		int size = 0;
		for (int i = 0; i < expressions.length; i++) {
			if (pheno.equals(phenotypes[i]))
				size++;
		}

		float[] vals = new float[size];
		int vi = 0;

		for (int i = 0; i < expressions.length; i++) {
			if (pheno.equals(phenotypes[i]))
				vals[vi++] = expressions[i];
		}
		
		switch (compress) {
			case CLASS_MEDIAN: return GeneExpression.median(vals);
			case CLASS_MAX:    return GeneExpression.max(vals);
			case CLASS_MIN:    return GeneExpression.min(vals);
			default:           return Double.NaN;
		}
	}

	@Override
	public int getSize() {
		return headers.size();
	}
}
