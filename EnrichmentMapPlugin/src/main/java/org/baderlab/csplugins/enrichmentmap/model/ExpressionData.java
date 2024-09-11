package org.baderlab.csplugins.enrichmentmap.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;

import org.baderlab.csplugins.enrichmentmap.model.Phenotype.Type;

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
	
	
	
	/**
	 * Special case. There is more than one dataset in the map, and they have the same expressions,
	 * but the pheontypes are different. We want to show the phenotypes for the selected data sets
	 * but we don't want to repeat all the expression data.
	 */
	static boolean commonExpressionsButDifferentPhenotypes(EnrichmentMap map) {
		return map != null && map.isCommonExpressionValues() && !phenotypesAreCommon(map);
	}
	
	
	/**
	 * These are the phenotypes entered into the "Phenotypes" fields in the creation dialog.
	 * They need to be highlighted in the table.
	 */
	static List<Phenotype> getPhenotypesToHighlight(List<EMDataSet> datasets) {
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
	
	static List<Phenotype> getPhenotypesToHighlight(EMDataSet dataset) {
		return getPhenotypesToHighlight(List.of(dataset));
	}
	
	
	/**
	 * Returns unique classes from the class file in the same order as the file.
	 */
	static LinkedHashSet<String> getPhenotypesFromClassFile(EMDataSet dataset) {
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
	
	
	static boolean phenotypesAreCommon(EnrichmentMap map) {
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
}
