package org.baderlab.csplugins.enrichmentmap.model;

import junit.framework.TestCase;

/*
 * A Geneset is a basic element of the Enrichment map
 * A Geneset consists of name, description and a list of genes
 * *Optional* - gene set can also have a source specified.  The only way to specify
 * 		a source is through the format of the name. If the geneset name can be divided
 * 		into tokens using the tokenizer "%" then the second token is taken to be the source.
 * The list of genes is converted when loaded to a set of integers to speed up search, filter and other operations
 * and minimize amount of memory required to store them.
 */

public class GeneSetTest extends TestCase {

	public void setUp() throws Exception {

    }
	
	public void testCreateEmptyGeneSet(){
		//create a new GeneSet
		GeneSet gs = new GeneSet("Gene Set 1", "fake geneset");
		
		gs.addGene(10);
		gs.addGene(12);
		gs.addGene(-1);
		gs.addGene(0);
		//check if it handles duplicates
		gs.addGene(10);
		
		assertEquals("Gene Set 1", gs.getName());
		assertEquals("fake geneset", gs.getDescription());
		
		assertEquals(4, gs.getGenes().size());
		
	}
	
	public void testCreateGenesetFromStringArray(){
		
		//String Array is what we use to create a saved geneset in cytoscape
		String[] saved_gs = new String[7];
		
		//the first object in the array is the hashmap key, which is the name of the 
		//geneset.
		saved_gs[0] = "Gene Set 1";
		saved_gs[1] = "Gene Set 1";
		saved_gs[2] = "fake geneset";
		saved_gs[3] = "10";
		saved_gs[4] = "12";
		saved_gs[5] = "-1";
		saved_gs[6] = "0";
		
		GeneSet gs = new GeneSet(saved_gs);
		
		assertEquals("Gene Set 1", gs.getName());
		assertEquals("fake geneset", gs.getDescription());
		
		assertEquals(4, gs.getGenes().size());
		assertEquals("Gene Set 1\tfake geneset\t0\t10\t12\t-1\t", gs.toString());
		
		//test equals function
		GeneSet gs2 = new GeneSet("Gene Set 1", "fake geneset");
		
		gs2.addGene(10);
		gs2.addGene(12);
		gs2.addGene(-1);
		
		assertEquals(false, gs.equals(gs2));
		
		gs2.addGene(0);
		assertEquals(true, gs.equals(gs2));
	}
	
	public void testImbeddedSource(){
			
		//create a new GeneSet from the structure used in the internally generated gene set files
		GeneSet gs = new GeneSet("alanine biosynthesis II%HumanCyc%ALANINE-SYN2-PWY", "alanine biosynthesis II");
		
		assertEquals("alanine biosynthesis II%HumanCyc%ALANINE-SYN2-PWY", gs.getName());
		assertEquals("alanine biosynthesis II", gs.getDescription());
		assertEquals("HumanCyc", gs.getSource());
		
	}
}
