package org.baderlab.csplugins.enrichmentmap.task;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import java.util.Optional;

import org.baderlab.csplugins.enrichmentmap.model.DataSet;
import org.baderlab.csplugins.enrichmentmap.model.DataSetFiles;
import org.baderlab.csplugins.enrichmentmap.model.EMCreationParameters;
import org.baderlab.csplugins.enrichmentmap.model.EMCreationParameters.Method;
import org.baderlab.csplugins.enrichmentmap.model.EMCreationParameters.SimilarityMetric;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentResultFilterParams.NESFilter;
import org.baderlab.csplugins.enrichmentmap.model.LegacySupport;
import org.baderlab.csplugins.enrichmentmap.parsers.ParseDavidEnrichmentResults;
import org.cytoscape.work.TaskMonitor;
import org.junit.Test;

public class LoadDavidResultTest {

	private TaskMonitor taskMonitor = mock(TaskMonitor.class);
	
	
	
	@Test
	public void testLoadDavidResult_withoutexpression() throws Exception{
		//for a dataset we require genesets, an expression file (optional), enrichment results
		String testDavidResultsFileName = "src/test/resources/org/baderlab/csplugins/enrichmentmap/task/david_output/12hr_David_Output.txt";
		
		DataSetFiles files = new DataSetFiles();		
		files.setEnrichmentFileName1(testDavidResultsFileName);
		
		//set the method to David
		double similarityCutoff = 0.25;
		double pvalue = 0.005;
		double qvalue = 0.005; // 5.0 X 10-3
		EMCreationParameters params = new EMCreationParameters(Method.Specialized, "EM1_", pvalue, qvalue, NESFilter.ALL, Optional.empty(), SimilarityMetric.JACCARD, similarityCutoff, 0.5);
	
		//create an new enrichment Map
		EnrichmentMap em = new EnrichmentMap("TestEM", params);
		
		//Load data set
		//create a dataset
		DataSet dataset = new DataSet(em, LegacySupport.DATASET1, files);		
		em.addDataSet(LegacySupport.DATASET1, dataset);
				
		//create a DatasetTask
		ParseDavidEnrichmentResults  enrichmentResultsFilesTask = new ParseDavidEnrichmentResults(dataset);
        enrichmentResultsFilesTask.run(taskMonitor); 

        CreateDummyExpressionTask dummyExpressionTask = new CreateDummyExpressionTask(dataset);
		dummyExpressionTask.run(taskMonitor);	
		
		em.filterGenesets();
		
		InitializeGenesetsOfInterestTask genesets_init = new InitializeGenesetsOfInterestTask(em);
        genesets_init.run(taskMonitor);
        
        ComputeSimilarityTask similarities = new ComputeSimilarityTask(em);
        similarities.run(taskMonitor);

				
		//check to see if the dataset loaded - there should be 215 genesets
		assertEquals(215, dataset.getSetofgenesets().getGenesets().size());
		//there should also be 215 enrichments (the genesets are built from the txt file)
		assertEquals(215, dataset.getEnrichments().getEnrichments().size());
		//there should be 7 genesets in the enrichments of interest
		assertEquals(7, dataset.getGenesetsOfInterest().getGenesets().size());
		//there should be 7 * 6 edges
		assertEquals((7 * 6)/2,em.getGenesetSimilarity().size());
		//there should be a total of 366 genes
		assertEquals(414, em.getNumberOfGenes());
		//there should be 43 genes in the geneset "nucleolus"
		assertEquals(114, em.getAllGenesets().get("ACETYLATION").getGenes().size());

		//make sure the dummy expression has values for all the genes
		assertEquals(414, dataset.getExpressionSets().getNumGenes());
		assertEquals(414,dataset.getDatasetGenes().size()); 
	}
	
	@Test
	public void testLoad2DavidResult_withoutexpression() throws Exception{
		//for a dataset we require genesets, an expression file (optional), enrichment results
		String testDavidResultsFileName = "src/test/resources/org/baderlab/csplugins/enrichmentmap/task/david_output/12hr_David_Output.txt";
		
		DataSetFiles files = new DataSetFiles();		
		files.setEnrichmentFileName1(testDavidResultsFileName);
		
		//for a dataset we require genesets, an expression file (optional), enrichment results
		String testDavidResultsFileName2 = "src/test/resources/org/baderlab/csplugins/enrichmentmap/task/david_output/24hr_David_Output.txt";
				
		DataSetFiles files2 = new DataSetFiles();		
		files2.setEnrichmentFileName1(testDavidResultsFileName2);
		
		//set the method to David
		double similarityCutoff = 0.25;
		double pvalue = 0.005;
		double qvalue = 0.005; // 5.0 X 10-3
		EMCreationParameters params = new EMCreationParameters(Method.Specialized, "EM1_", pvalue, qvalue, NESFilter.ALL, Optional.empty(), SimilarityMetric.JACCARD, similarityCutoff, 0.5);
		
		//create an new enrichment Map
		EnrichmentMap em = new EnrichmentMap("TestEM", params);
		
		//Load first dataset
		//create a dataset
		DataSet dataset = new DataSet(em, LegacySupport.DATASET1, files);		
		em.addDataSet(LegacySupport.DATASET1, dataset);
				
		//create a DatasetTask
		ParseDavidEnrichmentResults enrichmentResultsFilesTask = new ParseDavidEnrichmentResults(dataset);
		enrichmentResultsFilesTask.run(taskMonitor); 

		       
		
		//Load second dataset
		//create a dataset
		DataSet dataset2 = new DataSet(em, LegacySupport.DATASET2, files2);		
		em.addDataSet(LegacySupport.DATASET2, dataset2);
		
		//create a DatasetTask
		ParseDavidEnrichmentResults enrichmentResultsFiles2Task = new ParseDavidEnrichmentResults(dataset2);
		enrichmentResultsFiles2Task.run(taskMonitor);

		// check to see if the two datasets are distinct
		if (!((dataset.getDatasetGenes().containsAll(dataset2.getDatasetGenes()))
		   && (dataset2.getDatasetGenes().containsAll(dataset.getDatasetGenes()))))
			params.setDistinctExpressionSets(true);

		CreateDummyExpressionTask dummyExpressionTask = new CreateDummyExpressionTask(dataset);
		dummyExpressionTask.run(taskMonitor);
		CreateDummyExpressionTask dummyExpressionTask2 = new CreateDummyExpressionTask(dataset2);
		dummyExpressionTask2.run(taskMonitor);

		em.filterGenesets();

		InitializeGenesetsOfInterestTask genesets_init = new InitializeGenesetsOfInterestTask(em);
		genesets_init.run(taskMonitor);

		ComputeSimilarityTask similarities = new ComputeSimilarityTask(em);
		similarities.run(taskMonitor);

		// check to see if the dataset loaded - there should be 215 genesets
		assertEquals(215, dataset.getSetofgenesets().getGenesets().size());
		// there should also be 215 enrichments (the genesets are built from the txt file)
		assertEquals(215, dataset.getEnrichments().getEnrichments().size());
		// there should be 7 genesets in the enrichments of interest
		assertEquals(7, dataset.getGenesetsOfInterest().getGenesets().size());

		// there should be 114 genes in the geneset "acetylation"
		assertEquals(114, em.getDataset(LegacySupport.DATASET1).getSetofgenesets().getGenesets().get("ACETYLATION").getGenes().size());
//		assertEquals(114, em.getAllGenesets().get("ACETYLATION").getGenes().size());

		dataset2 = em.getDataset(LegacySupport.DATASET2);
		// check the stats for dataset2
		// check to see if the dataset loaded - there should be 263 genesets
		assertEquals(263, dataset2.getSetofgenesets().getGenesets().size());
		// there should also be 263 enrichments (the genesets are built from the bgo file)
		assertEquals(263, dataset2.getEnrichments().getEnrichments().size());
		// there should be 0 genesets in the enrichments of interest
		assertEquals(0, dataset2.getGenesetsOfInterest().getGenesets().size());

		// make sure the dummy expression has values for all the genes
		assertEquals(367, dataset2.getExpressionSets().getNumGenes());
		assertEquals(367, dataset2.getDatasetGenes().size());

		// there should be 20 edges (2 edges for every node because of the distinct expresison sets)
		// assertEquals((7*6),em.getGenesetSimilarity().size()); there should be a total of 366 genes
		assertEquals(661, em.getNumberOfGenes());

	}
		
}
