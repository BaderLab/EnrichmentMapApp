package org.baderlab.csplugins.enrichmentmap.model;

import java.util.HashMap;

import org.baderlab.csplugins.enrichmentmap.EnrichmentMapParameters;
import org.baderlab.csplugins.enrichmentmap.parsers.GMTFileReaderTask;
import org.baderlab.csplugins.enrichmentmap.task.ComputeSimilarityTask;

import junit.framework.TestCase;

public class GeneSetSimilarityTest extends TestCase {
	
	EnrichmentMap map;
	DataSet dataset;

	public void setUp() throws Exception {
		//load Genesets from the gmt file associated with this test
		String testDataFileName = "src/test/resources/org/baderlab/csplugins/enrichmentmap/model/Genesetstestfile.gmt";
        
        //create a new instance of the parameters
        EnrichmentMapParameters params = new EnrichmentMapParameters();        
        //set gmt file name 
        params.getDatasetFiles().get(EnrichmentMap.DATASET1).setGMTFileName(testDataFileName);
        
        //Create a new Enrichment map
        map = new EnrichmentMap(params);
                
        //get the default dataset
        dataset = map.getDataset(EnrichmentMap.DATASET1);

        //set up task
        GMTFileReaderTask task = new GMTFileReaderTask(dataset);

        //read in file
        task.run();
    }
	
	public void testJaccardCalculations(){
		
		//set the parameters in the params to use jaccard coeffecient calculation
		map.getParams().setSimilarityMetric(EnrichmentMapParameters.SM_JACCARD);
		//set the cutoff to the max
		map.getParams().setSimilarityCutOff(0);
		
		ComputeSimilarityTask sim_task = new ComputeSimilarityTask(map);
		
		sim_task.run();
		
		HashMap<String,GenesetSimilarity> similarities = map.getGenesetSimilarity();
		
		assertEquals(15, similarities.size());
		
		SetOfGeneSets gs_set = map.getDataset(EnrichmentMap.DATASET1).getSetofgenesets();
		HashMap<String,Integer> genes = map.getGenes();
		
		//check the gene set similarity between APOPTOSIS INDUCED DNA FRAGMENTATION%REACTOME%REACT_1213.4
		//and APOPTOSIS%REACTOME%REACT_578.5
		GenesetSimilarity similarity;
		
		 // Set A has 13 elements, Set B has 142 elements, They have 13 elements in common
		Double jaccard = 13.0/142.0;
		if(similarities.containsKey("APOPTOSIS INDUCED DNA FRAGMENTATION%REACTOME%REACT_1213.4 (Geneset_Overlap) APOPTOSIS%REACTOME%REACT_578.5")){			
			similarity = similarities.get("APOPTOSIS INDUCED DNA FRAGMENTATION%REACTOME%REACT_1213.4 (Geneset_Overlap) APOPTOSIS%REACTOME%REACT_578.5");
			assertEquals(jaccard,similarity.getSimilarity_coeffecient());
			assertEquals("APOPTOSIS INDUCED DNA FRAGMENTATION%REACTOME%REACT_1213.4", similarity.getGeneset1_Name());
			assertEquals("APOPTOSIS%REACTOME%REACT_578.5", similarity.getGeneset2_Name());
			assertEquals(13, similarity.getSizeOfOverlap());
			
		}
		else{
			similarity = similarities.get("APOPTOSIS%REACTOME%REACT_578.5 (Geneset_Overlap) APOPTOSIS INDUCED DNA FRAGMENTATION%REACTOME%REACT_1213.4");
			assertEquals(jaccard,similarity.getSimilarity_coeffecient());
			assertEquals("APOPTOSIS INDUCED DNA FRAGMENTATION%REACTOME%REACT_1213.4", similarity.getGeneset2_Name());
			assertEquals("APOPTOSIS%REACTOME%REACT_578.5", similarity.getGeneset1_Name());
			assertEquals(13, similarity.getSizeOfOverlap());
		}
		// Set A has 13 elements, Set B has 9 elements, They have 1 elements in common
		jaccard = 1.0/21.0;
		if(similarities.containsKey("APOPTOSIS INDUCED DNA FRAGMENTATION%REACTOME%REACT_1213.4 (Geneset_Overlap) APOPTOTIC CLEAVAGE OF CELL ADHESION PROTEINS%REACTOME%REACT_13579.1")){
			similarity = similarities.get("APOPTOSIS INDUCED DNA FRAGMENTATION%REACTOME%REACT_1213.4 (Geneset_Overlap) APOPTOTIC CLEAVAGE OF CELL ADHESION PROTEINS%REACTOME%REACT_13579.1");
			assertEquals(jaccard,similarity.getSimilarity_coeffecient());
			assertEquals("APOPTOSIS INDUCED DNA FRAGMENTATION%REACTOME%REACT_1213.4",similarity.getGeneset1_Name());
			assertEquals("APOPTOTIC CLEAVAGE OF CELL ADHESION PROTEINS%REACTOME%REACT_13579.1", similarity.getGeneset2_Name());
			assertEquals(1, similarity.getSizeOfOverlap());
			assertTrue(similarity.getOverlapping_genes().contains(genes.get("CASP3")));
		}
		else{
			similarity = similarities.get("APOPTOSIS INDUCED DNA FRAGMENTATION%REACTOME%REACT_1213.4 (Geneset_Overlap) APOPTOTIC CLEAVAGE OF CELL ADHESION PROTEINS%REACTOME%REACT_13579.1");
			assertEquals(jaccard,similarity.getSimilarity_coeffecient());
			assertEquals("APOPTOSIS INDUCED DNA FRAGMENTATION%REACTOME%REACT_1213.4",similarity.getGeneset2_Name());
			assertEquals("APOPTOTIC CLEAVAGE OF CELL ADHESION PROTEINS%REACTOME%REACT_13579.1", similarity.getGeneset1_Name());
			assertEquals(1, similarity.getSizeOfOverlap());
			assertTrue(similarity.getOverlapping_genes().contains(genes.get("CASP3")));
		}
		// Set A has 13 elements, Set B has 7 elements, They have 1 elements in common
		jaccard = 1.0/19.0;
		if(similarities.containsKey("APOPTOSIS INDUCED DNA FRAGMENTATION%REACTOME%REACT_1213.4 (Geneset_Overlap) APOPTOTIC FACTOR-MEDIATED RESPONSE%REACTOME%REACT_963.2")){
			similarity = similarities.get("APOPTOSIS INDUCED DNA FRAGMENTATION%REACTOME%REACT_1213.4 (Geneset_Overlap) APOPTOTIC FACTOR-MEDIATED RESPONSE%REACTOME%REACT_963.2");
			assertEquals(jaccard,similarity.getSimilarity_coeffecient());
			assertEquals("APOPTOSIS INDUCED DNA FRAGMENTATION%REACTOME%REACT_1213.4",similarity.getGeneset1_Name());
			assertEquals("APOPTOTIC FACTOR-MEDIATED RESPONSE%REACTOME%REACT_963.2", similarity.getGeneset2_Name());
			assertEquals(1, similarity.getSizeOfOverlap());
			assertTrue(similarity.getOverlapping_genes().contains(genes.get("CASP3")));
		}
		else{
			similarity = similarities.get("APOPTOTIC FACTOR-MEDIATED RESPONSE%REACTOME%REACT_963.2 (Geneset_Overlap) APOPTOSIS INDUCED DNA FRAGMENTATION%REACTOME%REACT_1213.4");
			assertEquals(jaccard,similarity.getSimilarity_coeffecient());
			assertEquals("APOPTOSIS INDUCED DNA FRAGMENTATION%REACTOME%REACT_1213.4",similarity.getGeneset2_Name());
			assertEquals("APOPTOTIC FACTOR-MEDIATED RESPONSE%REACTOME%REACT_963.2", similarity.getGeneset1_Name());
			assertEquals(1, similarity.getSizeOfOverlap());
			assertTrue(similarity.getOverlapping_genes().contains(genes.get("CASP3")));
		}
				
	}
	
	public void testOverlapCalculations(){
		//set the parameters in the params to use jaccard coeffecient calculation
		map.getParams().setSimilarityMetric(EnrichmentMapParameters.SM_OVERLAP);
		//set the cutoff to the max
		map.getParams().setSimilarityCutOff(0);
		
		ComputeSimilarityTask sim_task = new ComputeSimilarityTask(map);
		
		sim_task.run();
		
		HashMap<String,GenesetSimilarity> similarities = map.getGenesetSimilarity();
		
		assertEquals(15, similarities.size());
		
		SetOfGeneSets gs_set = map.getDataset(EnrichmentMap.DATASET1).getSetofgenesets();
		HashMap<String,Integer> genes = map.getGenes();
		
		//check the gene set similarity between APOPTOSIS INDUCED DNA FRAGMENTATION%REACTOME%REACT_1213.4
		//and APOPTOSIS%REACTOME%REACT_578.5
		GenesetSimilarity similarity;
		
		 // Set A has 13 elements, Set B has 142 elements, They have 13 elements in common
		Double overlap = 13.0/13.0;
		if(similarities.containsKey("APOPTOSIS INDUCED DNA FRAGMENTATION%REACTOME%REACT_1213.4 (Geneset_Overlap) APOPTOSIS%REACTOME%REACT_578.5")){			
			similarity = similarities.get("APOPTOSIS INDUCED DNA FRAGMENTATION%REACTOME%REACT_1213.4 (Geneset_Overlap) APOPTOSIS%REACTOME%REACT_578.5");
			assertEquals(overlap,similarity.getSimilarity_coeffecient());
			assertEquals("APOPTOSIS INDUCED DNA FRAGMENTATION%REACTOME%REACT_1213.4", similarity.getGeneset1_Name());
			assertEquals("APOPTOSIS%REACTOME%REACT_578.5", similarity.getGeneset2_Name());
			assertEquals(13, similarity.getSizeOfOverlap());
			
		}
		else{
			similarity = similarities.get("APOPTOSIS%REACTOME%REACT_578.5 (Geneset_Overlap) APOPTOSIS INDUCED DNA FRAGMENTATION%REACTOME%REACT_1213.4");
			assertEquals(overlap,similarity.getSimilarity_coeffecient());
			assertEquals("APOPTOSIS INDUCED DNA FRAGMENTATION%REACTOME%REACT_1213.4", similarity.getGeneset2_Name());
			assertEquals("APOPTOSIS%REACTOME%REACT_578.5", similarity.getGeneset1_Name());
			assertEquals(13, similarity.getSizeOfOverlap());
		}
		// Set A has 13 elements, Set B has 9 elements, They have 1 elements in common
		overlap = 1.0/9.0;
		if(similarities.containsKey("APOPTOSIS INDUCED DNA FRAGMENTATION%REACTOME%REACT_1213.4 (Geneset_Overlap) APOPTOTIC CLEAVAGE OF CELL ADHESION PROTEINS%REACTOME%REACT_13579.1")){
			similarity = similarities.get("APOPTOSIS INDUCED DNA FRAGMENTATION%REACTOME%REACT_1213.4 (Geneset_Overlap) APOPTOTIC CLEAVAGE OF CELL ADHESION PROTEINS%REACTOME%REACT_13579.1");
			assertEquals(overlap,similarity.getSimilarity_coeffecient());
			assertEquals("APOPTOSIS INDUCED DNA FRAGMENTATION%REACTOME%REACT_1213.4",similarity.getGeneset1_Name());
			assertEquals("APOPTOTIC CLEAVAGE OF CELL ADHESION PROTEINS%REACTOME%REACT_13579.1", similarity.getGeneset2_Name());
			assertEquals(1, similarity.getSizeOfOverlap());
			assertTrue(similarity.getOverlapping_genes().contains(genes.get("CASP3")));
		}
		else{
			similarity = similarities.get("APOPTOSIS INDUCED DNA FRAGMENTATION%REACTOME%REACT_1213.4 (Geneset_Overlap) APOPTOTIC CLEAVAGE OF CELL ADHESION PROTEINS%REACTOME%REACT_13579.1");
			assertEquals(overlap,similarity.getSimilarity_coeffecient());
			assertEquals("APOPTOSIS INDUCED DNA FRAGMENTATION%REACTOME%REACT_1213.4",similarity.getGeneset2_Name());
			assertEquals("APOPTOTIC CLEAVAGE OF CELL ADHESION PROTEINS%REACTOME%REACT_13579.1", similarity.getGeneset1_Name());
			assertEquals(1, similarity.getSizeOfOverlap());
			assertTrue(similarity.getOverlapping_genes().contains(genes.get("CASP3")));
		}
		// Set A has 13 elements, Set B has 7 elements, They have 1 elements in common
		overlap = 1.0/7.0;
		if(similarities.containsKey("APOPTOSIS INDUCED DNA FRAGMENTATION%REACTOME%REACT_1213.4 (Geneset_Overlap) APOPTOTIC FACTOR-MEDIATED RESPONSE%REACTOME%REACT_963.2")){
			similarity = similarities.get("APOPTOSIS INDUCED DNA FRAGMENTATION%REACTOME%REACT_1213.4 (Geneset_Overlap) APOPTOTIC FACTOR-MEDIATED RESPONSE%REACTOME%REACT_963.2");
			assertEquals(overlap,similarity.getSimilarity_coeffecient());
			assertEquals("APOPTOSIS INDUCED DNA FRAGMENTATION%REACTOME%REACT_1213.4",similarity.getGeneset1_Name());
			assertEquals("APOPTOTIC FACTOR-MEDIATED RESPONSE%REACTOME%REACT_963.2", similarity.getGeneset2_Name());
			assertEquals(1, similarity.getSizeOfOverlap());
			assertTrue(similarity.getOverlapping_genes().contains(genes.get("CASP3")));
		}
		else{
			similarity = similarities.get("APOPTOTIC FACTOR-MEDIATED RESPONSE%REACTOME%REACT_963.2 (Geneset_Overlap) APOPTOSIS INDUCED DNA FRAGMENTATION%REACTOME%REACT_1213.4");
			assertEquals(overlap,similarity.getSimilarity_coeffecient());
			assertEquals("APOPTOSIS INDUCED DNA FRAGMENTATION%REACTOME%REACT_1213.4",similarity.getGeneset2_Name());
			assertEquals("APOPTOTIC FACTOR-MEDIATED RESPONSE%REACTOME%REACT_963.2", similarity.getGeneset1_Name());
			assertEquals(1, similarity.getSizeOfOverlap());
			assertTrue(similarity.getOverlapping_genes().contains(genes.get("CASP3")));
		}
		
	}

	public void testCombindCalculations(){
		double combined_constant = 0.5;
		
		//set the parameters in the params to use jaccard coeffecient calculation
		map.getParams().setSimilarityMetric(EnrichmentMapParameters.SM_COMBINED);
		//set the cutoff to the max
		map.getParams().setSimilarityCutOff(0);
		map.getParams().setCombinedConstant(combined_constant);
		
		ComputeSimilarityTask sim_task = new ComputeSimilarityTask(map);
		
		sim_task.run();
		
		HashMap<String,GenesetSimilarity> similarities = map.getGenesetSimilarity();
		
		assertEquals(15, similarities.size());
		
		SetOfGeneSets gs_set = map.getDataset(EnrichmentMap.DATASET1).getSetofgenesets();
		HashMap<String,Integer> genes = map.getGenes();
		
		//check the gene set similarity between APOPTOSIS INDUCED DNA FRAGMENTATION%REACTOME%REACT_1213.4
		//and APOPTOSIS%REACTOME%REACT_578.5
		GenesetSimilarity similarity;
		
		 // Set A has 13 elements, Set B has 142 elements, They have 13 elements in common
		Double combined = (combined_constant * (13.0/13.0)) + ((1-combined_constant) * (13.0/142.0));
		if(similarities.containsKey("APOPTOSIS INDUCED DNA FRAGMENTATION%REACTOME%REACT_1213.4 (Geneset_Overlap) APOPTOSIS%REACTOME%REACT_578.5")){			
			similarity = similarities.get("APOPTOSIS INDUCED DNA FRAGMENTATION%REACTOME%REACT_1213.4 (Geneset_Overlap) APOPTOSIS%REACTOME%REACT_578.5");
			assertEquals(combined,similarity.getSimilarity_coeffecient());
			assertEquals("APOPTOSIS INDUCED DNA FRAGMENTATION%REACTOME%REACT_1213.4", similarity.getGeneset1_Name());
			assertEquals("APOPTOSIS%REACTOME%REACT_578.5", similarity.getGeneset2_Name());
			assertEquals(13, similarity.getSizeOfOverlap());
			
		}
		else{
			similarity = similarities.get("APOPTOSIS%REACTOME%REACT_578.5 (Geneset_Overlap) APOPTOSIS INDUCED DNA FRAGMENTATION%REACTOME%REACT_1213.4");
			assertEquals(combined,similarity.getSimilarity_coeffecient());
			assertEquals("APOPTOSIS INDUCED DNA FRAGMENTATION%REACTOME%REACT_1213.4", similarity.getGeneset2_Name());
			assertEquals("APOPTOSIS%REACTOME%REACT_578.5", similarity.getGeneset1_Name());
			assertEquals(13, similarity.getSizeOfOverlap());
		}
		// Set A has 13 elements, Set B has 9 elements, They have 1 elements in common
		combined = (combined_constant * (1.0/9.0)) + ((1-combined_constant) * (1.0/21.0));
		if(similarities.containsKey("APOPTOSIS INDUCED DNA FRAGMENTATION%REACTOME%REACT_1213.4 (Geneset_Overlap) APOPTOTIC CLEAVAGE OF CELL ADHESION PROTEINS%REACTOME%REACT_13579.1")){
			similarity = similarities.get("APOPTOSIS INDUCED DNA FRAGMENTATION%REACTOME%REACT_1213.4 (Geneset_Overlap) APOPTOTIC CLEAVAGE OF CELL ADHESION PROTEINS%REACTOME%REACT_13579.1");
			assertEquals(combined,similarity.getSimilarity_coeffecient());
			assertEquals("APOPTOSIS INDUCED DNA FRAGMENTATION%REACTOME%REACT_1213.4",similarity.getGeneset1_Name());
			assertEquals("APOPTOTIC CLEAVAGE OF CELL ADHESION PROTEINS%REACTOME%REACT_13579.1", similarity.getGeneset2_Name());
			assertEquals(1, similarity.getSizeOfOverlap());
			assertTrue(similarity.getOverlapping_genes().contains(genes.get("CASP3")));
		}
		else{
			similarity = similarities.get("APOPTOSIS INDUCED DNA FRAGMENTATION%REACTOME%REACT_1213.4 (Geneset_Overlap) APOPTOTIC CLEAVAGE OF CELL ADHESION PROTEINS%REACTOME%REACT_13579.1");
			assertEquals(combined,similarity.getSimilarity_coeffecient());
			assertEquals("APOPTOSIS INDUCED DNA FRAGMENTATION%REACTOME%REACT_1213.4",similarity.getGeneset2_Name());
			assertEquals("APOPTOTIC CLEAVAGE OF CELL ADHESION PROTEINS%REACTOME%REACT_13579.1", similarity.getGeneset1_Name());
			assertEquals(1, similarity.getSizeOfOverlap());
			assertTrue(similarity.getOverlapping_genes().contains(genes.get("CASP3")));
		}
		// Set A has 13 elements, Set B has 7 elements, They have 1 elements in common
		combined = (combined_constant * (1.0/7.0)) + ((1-combined_constant) * (1.0/19.0));;
		if(similarities.containsKey("APOPTOSIS INDUCED DNA FRAGMENTATION%REACTOME%REACT_1213.4 (Geneset_Overlap) APOPTOTIC FACTOR-MEDIATED RESPONSE%REACTOME%REACT_963.2")){
			similarity = similarities.get("APOPTOSIS INDUCED DNA FRAGMENTATION%REACTOME%REACT_1213.4 (Geneset_Overlap) APOPTOTIC FACTOR-MEDIATED RESPONSE%REACTOME%REACT_963.2");
			assertEquals(combined,similarity.getSimilarity_coeffecient());
			assertEquals("APOPTOSIS INDUCED DNA FRAGMENTATION%REACTOME%REACT_1213.4",similarity.getGeneset1_Name());
			assertEquals("APOPTOTIC FACTOR-MEDIATED RESPONSE%REACTOME%REACT_963.2", similarity.getGeneset2_Name());
			assertEquals(1, similarity.getSizeOfOverlap());
			assertTrue(similarity.getOverlapping_genes().contains(genes.get("CASP3")));
		}
		else{
			similarity = similarities.get("APOPTOTIC FACTOR-MEDIATED RESPONSE%REACTOME%REACT_963.2 (Geneset_Overlap) APOPTOSIS INDUCED DNA FRAGMENTATION%REACTOME%REACT_1213.4");
			assertEquals(combined,similarity.getSimilarity_coeffecient());
			assertEquals("APOPTOSIS INDUCED DNA FRAGMENTATION%REACTOME%REACT_1213.4",similarity.getGeneset2_Name());
			assertEquals("APOPTOTIC FACTOR-MEDIATED RESPONSE%REACTOME%REACT_963.2", similarity.getGeneset1_Name());
			assertEquals(1, similarity.getSizeOfOverlap());
			assertTrue(similarity.getOverlapping_genes().contains(genes.get("CASP3")));
		}
		
	}
}
