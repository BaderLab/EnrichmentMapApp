package org.baderlab.csplugins.enrichmentmap.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.Map;

import org.baderlab.csplugins.enrichmentmap.TestUtils;
import org.baderlab.csplugins.enrichmentmap.model.EMDataSet.Method;
import org.baderlab.csplugins.enrichmentmap.parsers.GMTFileReaderTask;
import org.baderlab.csplugins.enrichmentmap.task.ComputeSimilarityTaskParallel;
import org.baderlab.csplugins.enrichmentmap.util.Baton;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.work.TaskMonitor;
import org.jukito.JukitoRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.inject.Provider;

@RunWith(JukitoRunner.class)
public class GeneSetSimilarityTest {
	
	EnrichmentMap map;
	EMDataSet dataset;

	private TaskMonitor taskMonitor = mock(TaskMonitor.class);
	private CyServiceRegistrar serviceRegistrar = TestUtils.mockServiceRegistrar();
	
	@Before
	public void before(Provider<EnrichmentMapParameters> empFactory) throws Exception {
		//load Genesets from the gmt file associated with this test
		String testDataFileName = "src/test/resources/org/baderlab/csplugins/enrichmentmap/model/Genesetstestfile.gmt";
        
        //create a new instance of the parameters
        EnrichmentMapParameters params = empFactory.get();      
        //set gmt file name 
        params.getFiles().get(LegacySupport.DATASET1).setGMTFileName(testDataFileName);
        
        //Create a new Enrichment map
        map = new EnrichmentMap(params.getCreationParameters(), serviceRegistrar);
        Method method = EnrichmentMapParameters.stringToMethod(params.getMethod());
        DataSetFiles files = params.getFiles().get(LegacySupport.DATASET1);
        dataset = map.createDataSet(LegacySupport.DATASET1, method, files);

        //set up task
        GMTFileReaderTask task = new GMTFileReaderTask(dataset);
        task.run(taskMonitor);
        
        this.dataset.setGeneSetsOfInterest(this.dataset.getSetOfGeneSets());
    }
	
	@Test
	public void testJaccardCalculations() throws Exception {
		//set the parameters in the params to use jaccard coeffecient calculation
		map.getParams().setSimilarityMetric(EMCreationParameters.SimilarityMetric.JACCARD);
		//set the cutoff to the max
		map.getParams().setSimilarityCutoff(0);
		
		Baton<Map<SimilarityKey, GenesetSimilarity>> baton = new Baton<>();
		ComputeSimilarityTaskParallel sim_task = new ComputeSimilarityTaskParallel(map, baton.consumer());		
		sim_task.run(taskMonitor);
		
		Map<SimilarityKey,GenesetSimilarity> similarities = baton.supplier().get();
		assertEquals(15, similarities.size());
		
		//check the gene set similarity between APOPTOSIS INDUCED DNA FRAGMENTATION%REACTOME%REACT_1213.4
		//and APOPTOSIS%REACTOME%REACT_578.5
		GenesetSimilarity similarity;
		
		 // Set A has 13 elements, Set B has 142 elements, They have 13 elements in common
		Double jaccard = 13.0/142.0;
		similarity = similarities.get(new SimilarityKey("APOPTOSIS INDUCED DNA FRAGMENTATION%REACTOME%REACT_1213.4", "APOPTOSIS%REACTOME%REACT_578.5", "Geneset_Overlap", 0));
		assertEquals(jaccard, similarity.getSimilarityCoeffecient(), 0.0);
		assertEquals(13, similarity.getSizeOfOverlap());
		if(similarity.getGeneset1Name().equals("APOPTOSIS INDUCED DNA FRAGMENTATION%REACTOME%REACT_1213.4")) {
			assertEquals("APOPTOSIS INDUCED DNA FRAGMENTATION%REACTOME%REACT_1213.4", similarity.getGeneset1Name());
			assertEquals("APOPTOSIS%REACTOME%REACT_578.5", similarity.getGeneset2Name());
		} else {
			assertEquals("APOPTOSIS INDUCED DNA FRAGMENTATION%REACTOME%REACT_1213.4", similarity.getGeneset2Name());
			assertEquals("APOPTOSIS%REACTOME%REACT_578.5", similarity.getGeneset1Name());
		}
			
		// Set A has 13 elements, Set B has 9 elements, They have 1 elements in common
		jaccard = 1.0/21.0;
		similarity = similarities.get(new SimilarityKey("APOPTOSIS INDUCED DNA FRAGMENTATION%REACTOME%REACT_1213.4", "APOPTOTIC CLEAVAGE OF CELL ADHESION PROTEINS%REACTOME%REACT_13579.1", "Geneset_Overlap", 0));
		assertEquals(jaccard,similarity.getSimilarityCoeffecient(),0.0);
		assertEquals(1, similarity.getSizeOfOverlap());
		assertTrue(similarity.getOverlappingGenes().contains(map.getHashFromGene("CASP3")));
		if("APOPTOSIS INDUCED DNA FRAGMENTATION%REACTOME%REACT_1213.4".equals(similarity.getGeneset1Name())) {
			assertEquals("APOPTOSIS INDUCED DNA FRAGMENTATION%REACTOME%REACT_1213.4", similarity.getGeneset1Name());
			assertEquals("APOPTOTIC CLEAVAGE OF CELL ADHESION PROTEINS%REACTOME%REACT_13579.1", similarity.getGeneset2Name());
		} else {
			assertEquals("APOPTOSIS INDUCED DNA FRAGMENTATION%REACTOME%REACT_1213.4", similarity.getGeneset2Name());
			assertEquals("APOPTOTIC CLEAVAGE OF CELL ADHESION PROTEINS%REACTOME%REACT_13579.1", similarity.getGeneset1Name());
		}

		// Set A has 13 elements, Set B has 7 elements, They have 1 elements in common
		jaccard = 1.0/19.0;
		similarity = similarities.get(new SimilarityKey("APOPTOSIS INDUCED DNA FRAGMENTATION%REACTOME%REACT_1213.4", "APOPTOTIC FACTOR-MEDIATED RESPONSE%REACTOME%REACT_963.2", "Geneset_Overlap", 0));
		assertEquals(jaccard,similarity.getSimilarityCoeffecient(),0.0);
		assertEquals(1, similarity.getSizeOfOverlap());
		assertTrue(similarity.getOverlappingGenes().contains(map.getHashFromGene("CASP3")));
		if("APOPTOSIS INDUCED DNA FRAGMENTATION%REACTOME%REACT_1213.4".equals(similarity.getGeneset1Name())) {
			assertEquals("APOPTOSIS INDUCED DNA FRAGMENTATION%REACTOME%REACT_1213.4",similarity.getGeneset1Name());
			assertEquals("APOPTOTIC FACTOR-MEDIATED RESPONSE%REACTOME%REACT_963.2", similarity.getGeneset2Name());
		} else {
			assertEquals("APOPTOSIS INDUCED DNA FRAGMENTATION%REACTOME%REACT_1213.4",similarity.getGeneset2Name());
			assertEquals("APOPTOTIC FACTOR-MEDIATED RESPONSE%REACTOME%REACT_963.2", similarity.getGeneset1Name());
		}
	}
	
	@Test
	public void testOverlapCalculations() throws Exception{
		//set the parameters in the params to use jaccard coeffecient calculation
		map.getParams().setSimilarityMetric(EMCreationParameters.SimilarityMetric.OVERLAP);
		//set the cutoff to the max
		map.getParams().setSimilarityCutoff(0);
		
		Baton<Map<SimilarityKey, GenesetSimilarity>> baton = new Baton<>();
		ComputeSimilarityTaskParallel sim_task = new ComputeSimilarityTaskParallel(map, baton.consumer());		
		sim_task.run(taskMonitor);
		
		Map<SimilarityKey,GenesetSimilarity> similarities = baton.supplier().get();
		
		assertEquals(15, similarities.size());
		
		//SetOfGeneSets gs_set = map.getDataset(EnrichmentMap.DATASET1).getSetofgenesets();
		
		//check the gene set similarity between APOPTOSIS INDUCED DNA FRAGMENTATION%REACTOME%REACT_1213.4
		//and APOPTOSIS%REACTOME%REACT_578.5
		GenesetSimilarity similarity;
		
		 // Set A has 13 elements, Set B has 142 elements, They have 13 elements in common
		Double overlap = 13.0/13.0;
		similarity = similarities.get(new SimilarityKey("APOPTOSIS INDUCED DNA FRAGMENTATION%REACTOME%REACT_1213.4", "APOPTOSIS%REACTOME%REACT_578.5", "Geneset_Overlap", 0));
		assertEquals(overlap,similarity.getSimilarityCoeffecient(),0.0);
		assertEquals(13, similarity.getSizeOfOverlap());
		if("APOPTOSIS INDUCED DNA FRAGMENTATION%REACTOME%REACT_1213.4".equals(similarity.getGeneset1Name())) {
			assertEquals("APOPTOSIS INDUCED DNA FRAGMENTATION%REACTOME%REACT_1213.4", similarity.getGeneset1Name());
			assertEquals("APOPTOSIS%REACTOME%REACT_578.5", similarity.getGeneset2Name());
		} else {
			assertEquals("APOPTOSIS INDUCED DNA FRAGMENTATION%REACTOME%REACT_1213.4", similarity.getGeneset2Name());
			assertEquals("APOPTOSIS%REACTOME%REACT_578.5", similarity.getGeneset1Name());
		}
		
		// Set A has 13 elements, Set B has 9 elements, They have 1 elements in common
		overlap = 1.0/9.0;
		similarity = similarities.get(new SimilarityKey("APOPTOSIS INDUCED DNA FRAGMENTATION%REACTOME%REACT_1213.4", "APOPTOTIC CLEAVAGE OF CELL ADHESION PROTEINS%REACTOME%REACT_13579.1", "Geneset_Overlap", 0));
		assertEquals(overlap,similarity.getSimilarityCoeffecient(),0.0);
		assertEquals(1, similarity.getSizeOfOverlap());
		assertTrue(similarity.getOverlappingGenes().contains(map.getHashFromGene("CASP3")));
		if("APOPTOSIS INDUCED DNA FRAGMENTATION%REACTOME%REACT_1213.4".equals(similarity.getGeneset1Name())) {
			assertEquals("APOPTOSIS INDUCED DNA FRAGMENTATION%REACTOME%REACT_1213.4",similarity.getGeneset1Name());
			assertEquals("APOPTOTIC CLEAVAGE OF CELL ADHESION PROTEINS%REACTOME%REACT_13579.1", similarity.getGeneset2Name());
		} else {
			assertEquals("APOPTOSIS INDUCED DNA FRAGMENTATION%REACTOME%REACT_1213.4",similarity.getGeneset2Name());
			assertEquals("APOPTOTIC CLEAVAGE OF CELL ADHESION PROTEINS%REACTOME%REACT_13579.1", similarity.getGeneset1Name());
		}
		
		// Set A has 13 elements, Set B has 7 elements, They have 1 elements in common
		overlap = 1.0/7.0;
		similarity = similarities.get(new SimilarityKey("APOPTOSIS INDUCED DNA FRAGMENTATION%REACTOME%REACT_1213.4", "APOPTOTIC FACTOR-MEDIATED RESPONSE%REACTOME%REACT_963.2", "Geneset_Overlap", 0));
		assertEquals(overlap,similarity.getSimilarityCoeffecient(),0.0);
		assertEquals(1, similarity.getSizeOfOverlap());
		assertTrue(similarity.getOverlappingGenes().contains(map.getHashFromGene("CASP3")));
		if("APOPTOSIS INDUCED DNA FRAGMENTATION%REACTOME%REACT_1213.4".equals(similarity.getGeneset1Name())) {
			assertEquals("APOPTOSIS INDUCED DNA FRAGMENTATION%REACTOME%REACT_1213.4",similarity.getGeneset1Name());
			assertEquals("APOPTOTIC FACTOR-MEDIATED RESPONSE%REACTOME%REACT_963.2", similarity.getGeneset2Name());
		} else {
			assertEquals("APOPTOSIS INDUCED DNA FRAGMENTATION%REACTOME%REACT_1213.4",similarity.getGeneset2Name());
			assertEquals("APOPTOTIC FACTOR-MEDIATED RESPONSE%REACTOME%REACT_963.2", similarity.getGeneset1Name());
		}
	}

	@Test
	public void testCombindCalculations() throws Exception{
		double combined_constant = 0.5;
		
		//set the parameters in the params to use jaccard coeffecient calculation
		map.getParams().setSimilarityMetric(EMCreationParameters.SimilarityMetric.COMBINED);
		//set the cutoff to the max
		map.getParams().setSimilarityCutoff(0);
		map.getParams().setCombinedConstant(combined_constant);
		
		Baton<Map<SimilarityKey, GenesetSimilarity>> baton = new Baton<>();
		ComputeSimilarityTaskParallel sim_task = new ComputeSimilarityTaskParallel(map, baton.consumer());
		sim_task.run(taskMonitor);
		
		Map<SimilarityKey,GenesetSimilarity> similarities = baton.supplier().get();
		
		assertEquals(15, similarities.size());
		
		//SetOfGeneSets gs_set = map.getDataset(EnrichmentMap.DATASET1).getSetofgenesets();
		
		//check the gene set similarity between APOPTOSIS INDUCED DNA FRAGMENTATION%REACTOME%REACT_1213.4
		//and APOPTOSIS%REACTOME%REACT_578.5
		GenesetSimilarity similarity;
		
		 // Set A has 13 elements, Set B has 142 elements, They have 13 elements in common
		Double combined = (combined_constant * (13.0/13.0)) + ((1-combined_constant) * (13.0/142.0));
		similarity = similarities.get(new SimilarityKey("APOPTOSIS INDUCED DNA FRAGMENTATION%REACTOME%REACT_1213.4", "APOPTOSIS%REACTOME%REACT_578.5", "Geneset_Overlap", 0));
		assertEquals(combined,similarity.getSimilarityCoeffecient(),0.0);
		assertEquals(13, similarity.getSizeOfOverlap());
		if("APOPTOSIS INDUCED DNA FRAGMENTATION%REACTOME%REACT_1213.4".equals(similarity.getGeneset1Name())) {
			assertEquals("APOPTOSIS INDUCED DNA FRAGMENTATION%REACTOME%REACT_1213.4", similarity.getGeneset1Name());
			assertEquals("APOPTOSIS%REACTOME%REACT_578.5", similarity.getGeneset2Name());
		} else {
			assertEquals("APOPTOSIS INDUCED DNA FRAGMENTATION%REACTOME%REACT_1213.4", similarity.getGeneset2Name());
			assertEquals("APOPTOSIS%REACTOME%REACT_578.5", similarity.getGeneset1Name());
		}
		
		// Set A has 13 elements, Set B has 9 elements, They have 1 elements in common
		combined = (combined_constant * (1.0/9.0)) + ((1-combined_constant) * (1.0/21.0));
		similarity = similarities.get(new SimilarityKey("APOPTOSIS INDUCED DNA FRAGMENTATION%REACTOME%REACT_1213.4", "APOPTOTIC CLEAVAGE OF CELL ADHESION PROTEINS%REACTOME%REACT_13579.1", "Geneset_Overlap", 0));
		assertEquals(combined,similarity.getSimilarityCoeffecient(),0.0);
		assertEquals(1, similarity.getSizeOfOverlap());
		assertTrue(similarity.getOverlappingGenes().contains(map.getHashFromGene("CASP3")));
		if("APOPTOSIS INDUCED DNA FRAGMENTATION%REACTOME%REACT_1213.4".equals(similarity.getGeneset1Name())) {
			assertEquals("APOPTOSIS INDUCED DNA FRAGMENTATION%REACTOME%REACT_1213.4", similarity.getGeneset1Name());
			assertEquals("APOPTOTIC CLEAVAGE OF CELL ADHESION PROTEINS%REACTOME%REACT_13579.1", similarity.getGeneset2Name());
		} else {
			assertEquals("APOPTOSIS INDUCED DNA FRAGMENTATION%REACTOME%REACT_1213.4", similarity.getGeneset2Name());
			assertEquals("APOPTOTIC CLEAVAGE OF CELL ADHESION PROTEINS%REACTOME%REACT_13579.1", similarity.getGeneset1Name());
		}
		
		
		// Set A has 13 elements, Set B has 7 elements, They have 1 elements in common
		combined = (combined_constant * (1.0/7.0)) + ((1-combined_constant) * (1.0/19.0));;
		similarity = similarities.get(new SimilarityKey("APOPTOSIS INDUCED DNA FRAGMENTATION%REACTOME%REACT_1213.4", "APOPTOTIC FACTOR-MEDIATED RESPONSE%REACTOME%REACT_963.2", "Geneset_Overlap", 0));
		assertEquals(combined,similarity.getSimilarityCoeffecient(),0.0);
		assertEquals(1, similarity.getSizeOfOverlap());
		assertTrue(similarity.getOverlappingGenes().contains(map.getHashFromGene("CASP3")));
		if("APOPTOSIS INDUCED DNA FRAGMENTATION%REACTOME%REACT_1213.4".equals(similarity.getGeneset1Name())) {
			assertEquals("APOPTOSIS INDUCED DNA FRAGMENTATION%REACTOME%REACT_1213.4",similarity.getGeneset1Name());
			assertEquals("APOPTOTIC FACTOR-MEDIATED RESPONSE%REACTOME%REACT_963.2", similarity.getGeneset2Name());
		} else {
			assertEquals("APOPTOSIS INDUCED DNA FRAGMENTATION%REACTOME%REACT_1213.4",similarity.getGeneset2Name());
			assertEquals("APOPTOTIC FACTOR-MEDIATED RESPONSE%REACTOME%REACT_963.2", similarity.getGeneset1Name());
		}
		
	}
}
