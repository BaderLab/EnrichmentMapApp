package org.baderlab.csplugins.enrichmentmap.model;


import junit.framework.TestCase;

/*
 * An enrichment Result is the Parent class for different types of enrichment results - GSEA or generic results
 * An enrichment result (whether it is generic or gsea) needs to minimally have a name, description, pvalue and source
 * (source can be none)
 */
//TODO:Need to fork generic result and create additional types BingoResult, DavidResult

public class EnrichmentResultTest extends TestCase {
	
	public void setUp() throws Exception {

    }
	
	//create empty enrichment results
	public void testCreateEmptyEnrichmentResult(){
		//create a new GeneSet
		EnrichmentResult results = new EnrichmentResult();
		
		results.setName("APOPTOSIS INDUCED DNA FRAGMENTATION%REACTOME%REACT_1213.4");
		results.setDescription("Apoptosis induced DNA fragmentation");
		results.setPvalue(0.01);
				
		assertEquals("APOPTOSIS INDUCED DNA FRAGMENTATION%REACTOME%REACT_1213.4", results.getName());
		assertEquals("Apoptosis induced DNA fragmentation", results.getDescription());
		
		assertEquals(0.01, results.getPvalue());
		assertEquals("REACTOME", results.getSource());
		assertEquals("APOPTOSIS INDUCED DNA FRAGMENTATION%REACTOME%REACT_1213.4\t0.01\n", results.toString());
		
	}

}
