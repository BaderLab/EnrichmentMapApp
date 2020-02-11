package org.baderlab.csplugins.enrichmentmap.task;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import java.util.Map;
import java.util.Optional;

import org.baderlab.csplugins.enrichmentmap.TestUtils;
import org.baderlab.csplugins.enrichmentmap.model.DataSetFiles;
import org.baderlab.csplugins.enrichmentmap.model.EMCreationParameters;
import org.baderlab.csplugins.enrichmentmap.model.EMCreationParameters.EdgeStrategy;
import org.baderlab.csplugins.enrichmentmap.model.EMCreationParameters.SimilarityMetric;
import org.baderlab.csplugins.enrichmentmap.model.EMDataSet;
import org.baderlab.csplugins.enrichmentmap.model.EMDataSet.Method;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentResultFilterParams.NESFilter;
import org.baderlab.csplugins.enrichmentmap.model.GenesetSimilarity;
import org.baderlab.csplugins.enrichmentmap.model.LegacySupport;
import org.baderlab.csplugins.enrichmentmap.model.SimilarityKey;
import org.baderlab.csplugins.enrichmentmap.parsers.ParseBingoEnrichmentResults;
import org.baderlab.csplugins.enrichmentmap.util.Baton;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.work.TaskMonitor;
import org.junit.Test;

public class LoadBingoResultsTest {
	
	private CyServiceRegistrar serviceRegistrar = TestUtils.mockServiceRegistrar();
	private TaskMonitor taskMonitor = mock(TaskMonitor.class);
	
	@Test
	public void testLoadBingoResult_withoutexpression() throws Exception{
		//for a dataset we require genesets, an expression file (optional), enrichment results
		String testBingoResultsFileName = "src/test/resources/org/baderlab/csplugins/enrichmentmap/task/bingo_output/12Hr_topgenes.bgo";
		
		DataSetFiles files = new DataSetFiles();		
		files.setEnrichmentFileName1(testBingoResultsFileName);
		
		//set the method to Bingo
		double pvalue = 0.00005;
		double qvaule = 0.00000005; // 5.0 X 10-8
		double similarityCutoff = 0.25;
		EMCreationParameters params = 
			new EMCreationParameters("EM1_", null, pvalue, qvaule, NESFilter.ALL, Optional.empty(), true, false, SimilarityMetric.JACCARD, similarityCutoff, 0.5, EdgeStrategy.AUTOMATIC);
		//create an new enrichment Map
		EnrichmentMap em = new EnrichmentMap(params, serviceRegistrar);
		EMDataSet dataset = em.createDataSet(LegacySupport.DATASET1, Method.Specialized, files);				
		
		ParseBingoEnrichmentResults enrichmentResultsFilesTask = new ParseBingoEnrichmentResults(dataset);
        enrichmentResultsFilesTask.run(taskMonitor); 
        
        CreateDummyExpressionTask dummyExpressionTask = new CreateDummyExpressionTask(dataset);
		dummyExpressionTask.run(taskMonitor);
        
		em.filterGenesets();
		
		InitializeGenesetsOfInterestTask genesets_init = new InitializeGenesetsOfInterestTask(em);
		genesets_init.run(taskMonitor);  
        
		
		Baton<Map<SimilarityKey, GenesetSimilarity>> baton = new Baton<>();
		ComputeSimilarityTaskParallel similarities = new ComputeSimilarityTaskParallel(em, baton.consumer());	
        similarities.run(taskMonitor);

				
		//check to see if the dataset loaded - there should be 74 genesets
		assertEquals(74, dataset.getSetOfGeneSets().getGeneSets().size());
		//there should also be 74 enrichments (the genesets are built from the bgo file)
		assertEquals(74, dataset.getEnrichments().getEnrichments().size());
		//there should be 11 genesets in the enrichments of interest
		assertEquals(5, dataset.getGeneSetsOfInterest().getGeneSets().size());
		//there should be 6 edges
		assertEquals(6, baton.supplier().get().size());
		//there should be a total of 366 genes
		assertEquals(446, em.getNumberOfGenes());
		//there should be 43 genes in the geneset "nucleolus"
		assertEquals(43, em.getAllGeneSets().get("NUCLEOLUS").getGenes().size());

		//make sure the dummy expression has values for all the genes
		assertEquals(446, dataset.getExpressionSets().getNumGenes());
		assertEquals(446,dataset.getExpressionGenes().size()); 
	}
	
	
	@Test
	public void testLoad2BingoResult_withoutexpression() throws Exception{
	
		//for a dataset we require genesets, an expression file (optional), enrichment results
		String testBingoResultsFileName = "src/test/resources/org/baderlab/csplugins/enrichmentmap/task/bingo_output/12Hr_topgenes.bgo";
		
		DataSetFiles files = new DataSetFiles();		
		files.setEnrichmentFileName1(testBingoResultsFileName);
		
		//for a dataset we require genesets, an expression file (optional), enrichment results
		String testBingoResultsFileName2 = "src/test/resources/org/baderlab/csplugins/enrichmentmap/task/bingo_output/24Hr_topgenes.bgo";
				
		DataSetFiles files2 = new DataSetFiles();		
		files2.setEnrichmentFileName1(testBingoResultsFileName2);
		
		//set the method to Bingo
		double pvalue = 0.00005;
		double qvaule = 0.00000005; // 5.0 X 10-8
		double similarityCutoff = 0.25;
		EMCreationParameters params = 
			new EMCreationParameters("EM1_", null, pvalue, qvaule, NESFilter.ALL, Optional.empty(), true, false,
					SimilarityMetric.JACCARD, similarityCutoff, 0.5, EdgeStrategy.AUTOMATIC);
		
		//create an new enrichment Map
		EnrichmentMap em = new EnrichmentMap(params, serviceRegistrar);
		EMDataSet dataset = em.createDataSet(LegacySupport.DATASET1, Method.Specialized, files);				
		
		ParseBingoEnrichmentResults  enrichmentResultsFilesTask = new ParseBingoEnrichmentResults(dataset);
        enrichmentResultsFilesTask.run(taskMonitor); 
		
		//Load second dataset
		//create a dataset
		EMDataSet dataset2 = em.createDataSet(LegacySupport.DATASET2, Method.Specialized, files2);						
		//create a DatasetTask
		
		ParseBingoEnrichmentResults  enrichmentResultsFiles2Task = new ParseBingoEnrichmentResults(dataset2);
        enrichmentResultsFiles2Task.run(taskMonitor); 
		
		CreateDummyExpressionTask dummyExpressionTask = new CreateDummyExpressionTask(dataset);
		dummyExpressionTask.run(taskMonitor);
			
		CreateDummyExpressionTask dummyExpressionTask2 = new CreateDummyExpressionTask(dataset2);
		dummyExpressionTask2.run(taskMonitor);
		//check to see if the two datasets are distinct
		if(!((dataset.getExpressionGenes().containsAll(dataset2.getExpressionGenes())) && 
					(dataset2.getExpressionGenes().containsAll(dataset.getExpressionGenes()))))
				em.setDistinctExpressionSets(true);	
				
		em.filterGenesets();
				
		InitializeGenesetsOfInterestTask genesets_init = new InitializeGenesetsOfInterestTask(em);
		genesets_init.run(taskMonitor);  
		        
//		ComputeSimilarityTask similarities = new ComputeSimilarityTask(em);
//		similarities.run(taskMonitor);

        dataset = em.getDataSet(LegacySupport.DATASET1);
		//get the stats for the first dataset		
		//check to see if the dataset loaded - there should be 74 genesets
		assertEquals(74, dataset.getSetOfGeneSets().getGeneSets().size());
		//there should also be 74 enrichments (the genesets are built from the bgo file)
		assertEquals(74, dataset.getEnrichments().getEnrichments().size());
		//there should be 11 genesets in the enrichments of interest
		assertEquals(5, dataset.getGeneSetsOfInterest().getGeneSets().size());
		//there should be 43 genes in the geneset "nucleolus"
		assertEquals(43, dataset.getSetOfGeneSets().getGeneSets().get("NUCLEOLUS").getGenes().size());
		//make sure the dummy expression has values for all the genes
		assertEquals(446, dataset.getExpressionSets().getNumGenes());
		assertEquals(446,dataset.getExpressionGenes().size());
		
		dataset2 = em.getDataSet(LegacySupport.DATASET2);
		//check the stats for dataset2
		//check to see if the dataset loaded - there should be 74 genesets
		assertEquals(87, dataset2.getSetOfGeneSets().getGeneSets().size());
		//there should also be 74 enrichments (the genesets are built from the bgo file)
		assertEquals(87, dataset2.getEnrichments().getEnrichments().size());
		//there should be 11 genesets in the enrichments of interest
		assertEquals(2, dataset2.getGeneSetsOfInterest().getGeneSets().size());
		//there should be 43 genes in the geneset "nucleolus"
		assertEquals(318, dataset2.getSetOfGeneSets().getGeneSets().get("INTRACELLULAR").getGenes().size());
		//make sure the dummy expression has values for all the genes
		assertEquals(398, dataset2.getExpressionSets().getNumGenes());
		assertEquals(398,dataset2.getExpressionGenes().size());
		
		//there should be 20 edges (2 edges for every node because of the distinct expresison sets)
		//assertEquals(24,em.getGenesetSimilarity().size());
		//there should be a total of 366 genes
		assertEquals(704, em.getNumberOfGenes());
	}
}
