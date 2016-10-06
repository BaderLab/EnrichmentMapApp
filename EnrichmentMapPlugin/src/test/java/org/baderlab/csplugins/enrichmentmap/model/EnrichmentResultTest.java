package org.baderlab.csplugins.enrichmentmap.model;


import static org.junit.Assert.assertEquals;

import org.junit.Test;

/*
 * An enrichment Result is the Parent class for different types of enrichment results - GSEA or generic results
 * An enrichment result (whether it is generic or gsea) needs to minimally have a name, description, pvalue and source
 * (source can be none)
 */
//TODO:Need to fork generic result and create additional types BingoResult, DavidResult

public class EnrichmentResultTest {
	
	//create empty enrichment results
	@Test
	public void testCreateEmptyEnrichmentResult(){
		
		String name = "APOPTOSIS INDUCED DNA FRAGMENTATION%REACTOME%REACT_1213.4";
		String description = "Apoptosis induced DNA fragmentation";
		double pvalue = (0.01);
		
		//create a new GeneSet
		EnrichmentResult results = new EnrichmentResult(name, description, pvalue);
				
		assertEquals("APOPTOSIS INDUCED DNA FRAGMENTATION%REACTOME%REACT_1213.4", results.getName());
		assertEquals("Apoptosis induced DNA fragmentation", results.getDescription());
		
		assertEquals(0.01, results.getPvalue(), 0.0);
		assertEquals("REACTOME", results.getSource().get());
		assertEquals("APOPTOSIS INDUCED DNA FRAGMENTATION%REACTOME%REACT_1213.4\t0.01\n", results.toString());
		
	}

}
