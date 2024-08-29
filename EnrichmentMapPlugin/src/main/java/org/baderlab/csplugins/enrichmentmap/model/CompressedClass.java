package org.baderlab.csplugins.enrichmentmap.model;

import java.util.ArrayList;
import java.util.Iterator;
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
		
		// Special case. There is more than one dataset in the map, and they have different phenotypes,
		// but the expression values are the same and we don't want to repeat them in the table.
		if(map != null && map.isCommonExpressionValues() && !phenotypesAreCommon(map)) {
			// we only get passed one dataset because expressions are the same
			var dataset = datasets.get(0); 
			var classPhenos = getPhenotypesFromClassFile(dataset);
			var highlightPhenos = getPhenotypesToHighlight(map.getDataSetList()); // get the phenotypes from all the data sets
			addHeaders(dataset, classPhenos, highlightPhenos);
			
		} else {
			for(var dataset : datasets) {
				var classPhenos = getPhenotypesFromClassFile(dataset);
				var highlightPhenos = getPhenotypesToHighlight(List.of(dataset));
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
	
	/**
	 * These are the phenotypes entered into the "Phenotypes" fields in the creation dialo
	 * They need to be highlighted in the table.
	 */
	private static List<Phenotype> getPhenotypesToHighlight(List<EMDataSet> datasets) {
		List<Phenotype> phenos = new ArrayList<>();
		
		for(var dataset : datasets) {
			var enrichments = dataset.getEnrichments();
			
			String pheno1 = enrichments.getPhenotype1();
			if(pheno1 != null) {
				phenos.add(new Phenotype(dataset, pheno1, Type.POSITIVE));
			}
			String pheno2 = enrichments.getPhenotype2();
			if(pheno2 != null) {
				phenos.add(new Phenotype(dataset, pheno2, Type.NEGATIVE));
			}
		}
		
		return phenos;
	}
	
	/**
	 * Returns unique classes from the class file in the same order as the file.
	 */
	private static LinkedHashSet<String> getPhenotypesFromClassFile(EMDataSet dataset) {
		var enrichments = dataset.getEnrichments();
		LinkedHashSet<String> uniquePhenos = new LinkedHashSet<>();
		
		String[] phenotypes = enrichments.getPhenotypes();
		if(phenotypes != null) {
			for(String pheno : phenotypes) {
				if(pheno != null) {
					uniquePhenos.add(pheno);
				}
			}
		}
		
		return uniquePhenos;
	}
	
	
	private static boolean phenotypesAreCommon(EnrichmentMap map) {
		Iterator<EMDataSet> iter = map.getDataSets().values().iterator();
		SetOfEnrichmentResults r = iter.next().getEnrichments();
		String p1 = r.getPhenotype1();
		String p2 = r.getPhenotype2();
		
		while(iter.hasNext()) {
			SetOfEnrichmentResults r2 = iter.next().getEnrichments();
			if(!p1.equals(r2.getPhenotype1()))
				return false;
			if(!p2.equals(r2.getPhenotype2()))
				return false;
		}
		
		return true;
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
