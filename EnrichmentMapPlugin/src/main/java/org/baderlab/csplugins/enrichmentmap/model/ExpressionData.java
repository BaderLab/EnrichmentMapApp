package org.baderlab.csplugins.enrichmentmap.model;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import org.baderlab.csplugins.enrichmentmap.model.PhenotypeHighlight.Highlight;


/**
 * Common interface for ways of representing (compressing) expression data for use in the HeatMap.
 */
public interface ExpressionData {
	
	/**
	 * number of columns
	 */
	int getSize();
	
	/**
	 * column name
	 */
	String getName(int col); 
	
	/**
	 * Returns expression value for given gene and column.
	 */
	double getValue(int geneID, int col, Compress compress, Transform transform);

	/**
	 * Returns a data set that contains the expression value. Note, if multiple data
	 * sets have the same expression values, only one will be returned.
	 */
	EMDataSet getDataSet(int col);
	
	/**
	 * Info on how to highlight a column based on the classes and phenotypes.
	 * Should not return null.
	 */
	PhenotypeHighlight getHighlight(int col);

	
	boolean commonButDiffPheno();
	
	
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
	
	
	/**
	 * These are the phenotypes entered into the "Phenotypes" fields in the creation dialog.
	 * They need to be highlighted in the table.
	 */
	static Map<String,PhenotypeHighlight> getPhenotypesToHighlight(List<EMDataSet> datasets) {
		Map<String,PhenotypeHighlight> phenoMap = new LinkedHashMap<>();
		
		for (var dataset : datasets) {
			var enrichments = dataset.getEnrichments();

			String phenoNamePos = enrichments.getPhenotype1();
			if(phenoNamePos != null) {
				var phenoHighlight = new PhenotypeHighlight(dataset, phenoNamePos, Highlight.POSITIVE);
				var existingPheno = phenoMap.get(phenoNamePos);
				phenoMap.put(phenoNamePos, phenoHighlight.merge(existingPheno));
			}
			
			String phenoNameNeg = enrichments.getPhenotype2();
			if (phenoNameNeg != null) {
				var phenoHighlight = new PhenotypeHighlight(dataset, phenoNameNeg, Highlight.NEGATIVE);
				var existingPheno = phenoMap.get(phenoNameNeg);
				phenoMap.put(phenoNameNeg, phenoHighlight.merge(existingPheno));
			}
		}
		
		return phenoMap;
	}
	
	static Map<String,PhenotypeHighlight> getPhenotypesToHighlight(EMDataSet dataset) {
		return getPhenotypesToHighlight(List.of(dataset));
	}
	
	
	
	/**
	 * Special case. There is more than one dataset in the map, and they have the same expressions,
	 * but the pheontypes are different. We want to show the phenotypes for the selected data sets
	 * but we don't want to repeat all the expression data.
	 */
	static boolean commonExpressionsButDifferentPhenotypes(EnrichmentMap map) {
		return map != null && map.isCommonExpressionValues() && !phenotypesAreCommon(map);
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
}
