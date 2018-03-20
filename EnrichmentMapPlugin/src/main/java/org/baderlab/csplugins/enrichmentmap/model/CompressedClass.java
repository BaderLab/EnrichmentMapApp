package org.baderlab.csplugins.enrichmentmap.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.tuple.Pair;

public class CompressedClass implements ExpressionData {

	private final ExpressionCache expressionCache;
	private List<Pair<EMDataSet, String>> headers = new ArrayList<>();
	
	public CompressedClass(List<EMDataSet> datasets, ExpressionCache expressionCache) {
		this.expressionCache = expressionCache;
		
		for (EMDataSet dataset : datasets) {
			SetOfEnrichmentResults enrichments = dataset.getEnrichments();
			String pheno1 = enrichments.getPhenotype1();
			String pheno2 = enrichments.getPhenotype2();
			
			if (pheno1 != null)
				headers.add(Pair.of(dataset, pheno1));
			if (pheno2 != null)
				headers.add(Pair.of(dataset, pheno2));
		}
	}
	
	@Override
	public EMDataSet getDataSet(int idx) {
		return headers.get(idx).getLeft();
	}

	@Override
	public String getName(int idx) {
		return headers.get(idx).getRight();
	}
	
	@Override
	public Optional<String> getPhenotype(int idx) {
		return Optional.of(getName(idx));
	}
	
	@Override
	public double getValue(int geneID, int idx, Compress compress) {
		EMDataSet dataset = getDataSet(idx);
		String pheno = getName(idx);

		String[] phenotypes = dataset.getEnrichments().getPhenotypes();

		if (phenotypes == null || phenotypes.length == 0)
			return Double.NaN;

		Optional<float[]> optExpr = expressionCache.getExpressions(dataset, geneID);

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
			default:	           return Double.NaN;
		}
	}

	@Override
	public int getSize() {
		return headers.size();
	}
}
