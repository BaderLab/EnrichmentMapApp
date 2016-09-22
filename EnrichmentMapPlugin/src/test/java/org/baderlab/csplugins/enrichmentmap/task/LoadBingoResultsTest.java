package org.baderlab.csplugins.enrichmentmap.task;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import org.baderlab.csplugins.enrichmentmap.StreamUtil;
import org.baderlab.csplugins.enrichmentmap.model.DataSet;
import org.baderlab.csplugins.enrichmentmap.model.DataSetFiles;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMapParameters;
import org.baderlab.csplugins.enrichmentmap.parsers.ParseBingoEnrichmentResults;
import org.cytoscape.work.TaskMonitor;
import org.junit.Test;

public class LoadBingoResultsTest {
	
	private TaskMonitor taskMonitor = mock(TaskMonitor.class);
	private StreamUtil streamUtil = new StreamUtil();
	

	@Test
	public void testLoadBingoResult_withoutexpression() throws Exception{
		EnrichmentMapParameters params = new EnrichmentMapParameters();
	
		//for a dataset we require genesets, an expression file (optional), enrichment results
		String testBingoResultsFileName = "src/test/resources/org/baderlab/csplugins/enrichmentmap/task/bingo_output/12Hr_topgenes.bgo";
		
		DataSetFiles files = new DataSetFiles();		
		files.setEnrichmentFileName1(testBingoResultsFileName);
		params.addFiles(EnrichmentMap.DATASET1, files);
		
		//set the method to Bingo
		params.setMethod(EnrichmentMapParameters.method_Specialized);
		params.setSimilarityMetric(EnrichmentMapParameters.SM_JACCARD);
		params.setSimilarityCutOff(0.25);
		params.setPvalue(0.00005);
		params.setQvalue(0.00000005); // 5.0 X 10-8
	
		//create an new enrichment Map
		EnrichmentMap em = new EnrichmentMap(params);
		
		//Load data set
		//create a dataset
		DataSet dataset = new DataSet(em, EnrichmentMap.DATASET1,files);		
		em.addDataset(EnrichmentMap.DATASET1, dataset);				
		
		ParseBingoEnrichmentResults enrichmentResultsFilesTask = new ParseBingoEnrichmentResults(dataset);
        enrichmentResultsFilesTask.run(taskMonitor); 
        
        CreateDummyExpressionTask dummyExpressionTask = new CreateDummyExpressionTask(dataset);
		dummyExpressionTask.run(taskMonitor);
        
		em.filterGenesets();
		
		InitializeGenesetsOfInterestTask genesets_init = new InitializeGenesetsOfInterestTask(em);
		genesets_init.run(taskMonitor);  
        
        ComputeSimilarityTask similarities = new ComputeSimilarityTask(em);
        similarities.run(taskMonitor);

				
		//check to see if the dataset loaded - there should be 74 genesets
		assertEquals(74, dataset.getSetofgenesets().getGenesets().size());
		//there should also be 74 enrichments (the genesets are built from the bgo file)
		assertEquals(74, dataset.getEnrichments().getEnrichments().size());
		//there should be 11 genesets in the enrichments of interest
		assertEquals(5, dataset.getGenesetsOfInterest().getGenesets().size());
		//there should be 6 edges
		assertEquals(10,em.getGenesetSimilarity().size());
		//there should be a total of 366 genes
		assertEquals(446, em.getGenes().size());
		//there should be 43 genes in the geneset "nucleolus"
		assertEquals(43, em.getAllGenesets().get("NUCLEOLUS").getGenes().size());

		//make sure the dummy expression has values for all the genes
		assertEquals(446, dataset.getExpressionSets().getNumGenes());
		assertEquals(446,dataset.getDatasetGenes().size()); 
	}
	
	
	@Test
	public void testLoad2BingoResult_withoutexpression() throws Exception{
		EnrichmentMapParameters params = new EnrichmentMapParameters();
	
		//for a dataset we require genesets, an expression file (optional), enrichment results
		String testBingoResultsFileName = "src/test/resources/org/baderlab/csplugins/enrichmentmap/task/bingo_output/12Hr_topgenes.bgo";
		
		DataSetFiles files = new DataSetFiles();		
		files.setEnrichmentFileName1(testBingoResultsFileName);
		params.addFiles(EnrichmentMap.DATASET1, files);
		
		//for a dataset we require genesets, an expression file (optional), enrichment results
		String testBingoResultsFileName2 = "src/test/resources/org/baderlab/csplugins/enrichmentmap/task/bingo_output/24Hr_topgenes.bgo";
				
		DataSetFiles files2 = new DataSetFiles();		
		files2.setEnrichmentFileName1(testBingoResultsFileName2);
		params.addFiles(EnrichmentMap.DATASET2, files2);
		
		//set the method to Bingo
		params.setMethod(EnrichmentMapParameters.method_Specialized);
		params.setSimilarityMetric(EnrichmentMapParameters.SM_JACCARD);
		params.setSimilarityCutOff(0.25);
		params.setPvalue(0.00005);
		params.setQvalue(0.00000005); // 5.0 X 10-8
	
		//create an new enrichment Map
		EnrichmentMap em = new EnrichmentMap(params);
		
		//Load first dataset
		//create a dataset
		DataSet dataset = new DataSet(em, EnrichmentMap.DATASET1,files);		
		em.addDataset(EnrichmentMap.DATASET1, dataset);

		ParseBingoEnrichmentResults  enrichmentResultsFilesTask = new ParseBingoEnrichmentResults(dataset);
        enrichmentResultsFilesTask.run(taskMonitor); 
		
		//Load second dataset
		//create a dataset
		DataSet dataset2 = new DataSet(em, EnrichmentMap.DATASET2,files2);		
		em.addDataset(EnrichmentMap.DATASET2, dataset2);						
		//create a DatasetTask
		
		ParseBingoEnrichmentResults  enrichmentResultsFiles2Task = new ParseBingoEnrichmentResults(dataset2);
        enrichmentResultsFiles2Task.run(taskMonitor); 
		
		CreateDummyExpressionTask dummyExpressionTask = new CreateDummyExpressionTask(dataset);
		dummyExpressionTask.run(taskMonitor);
			
		CreateDummyExpressionTask dummyExpressionTask2 = new CreateDummyExpressionTask(dataset2);
		dummyExpressionTask2.run(taskMonitor);
		//check to see if the two datasets are distinct
		if(!((dataset.getDatasetGenes().containsAll(dataset2.getDatasetGenes())) && 
					(dataset2.getDatasetGenes().containsAll(dataset.getDatasetGenes()))))
				params.setTwoDistinctExpressionSets(true);	
				
		em.filterGenesets();
				
		InitializeGenesetsOfInterestTask genesets_init = new InitializeGenesetsOfInterestTask(em);
		genesets_init.run(taskMonitor);  
		        
		ComputeSimilarityTask similarities = new ComputeSimilarityTask(em);
		similarities.run(taskMonitor);

        dataset = em.getDataset(EnrichmentMap.DATASET1);
		//get the stats for the first dataset		
		//check to see if the dataset loaded - there should be 74 genesets
		assertEquals(74, dataset.getSetofgenesets().getGenesets().size());
		//there should also be 74 enrichments (the genesets are built from the bgo file)
		assertEquals(74, dataset.getEnrichments().getEnrichments().size());
		//there should be 11 genesets in the enrichments of interest
		assertEquals(5, dataset.getGenesetsOfInterest().getGenesets().size());
		//there should be 43 genes in the geneset "nucleolus"
		assertEquals(43, dataset.getSetofgenesets().getGenesets().get("NUCLEOLUS").getGenes().size());
		//make sure the dummy expression has values for all the genes
		assertEquals(446, dataset.getExpressionSets().getNumGenes());
		assertEquals(446,dataset.getDatasetGenes().size());
		
		dataset2 = em.getDataset(EnrichmentMap.DATASET2);
		//check the stats for dataset2
		//check to see if the dataset loaded - there should be 74 genesets
		assertEquals(87, dataset2.getSetofgenesets().getGenesets().size());
		//there should also be 74 enrichments (the genesets are built from the bgo file)
		assertEquals(87, dataset2.getEnrichments().getEnrichments().size());
		//there should be 11 genesets in the enrichments of interest
		assertEquals(2, dataset2.getGenesetsOfInterest().getGenesets().size());
		//there should be 43 genes in the geneset "nucleolus"
		assertEquals(318, dataset2.getSetofgenesets().getGenesets().get("INTRACELLULAR").getGenes().size());
		//make sure the dummy expression has values for all the genes
		assertEquals(398, dataset2.getExpressionSets().getNumGenes());
		assertEquals(398,dataset2.getDatasetGenes().size());
		
		//there should be 20 edges (2 edges for every node because of the distinct expresison sets)
		//assertEquals(24,em.getGenesetSimilarity().size());
		//there should be a total of 366 genes
		assertEquals(704, em.getGenes().size());
	}
}
