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
import org.baderlab.csplugins.enrichmentmap.parsers.ParseDavidEnrichmentResults;
import org.baderlab.csplugins.enrichmentmap.util.Baton;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.work.TaskMonitor;
import org.junit.Test;

public class LoadDavidResultTest {

	private CyServiceRegistrar serviceRegistrar = TestUtils.mockServiceRegistrar();
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
		EMCreationParameters params = 
			new EMCreationParameters("EM1_", pvalue, qvalue, NESFilter.ALL, Optional.empty(), true, SimilarityMetric.JACCARD, similarityCutoff, 0.5, EdgeStrategy.AUTOMATIC);
	
		//create an new enrichment Map
		EnrichmentMap em = new EnrichmentMap(params, serviceRegistrar);
		
		//Load data set
		//create a dataset
		EMDataSet dataset = em.createDataSet(LegacySupport.DATASET1, Method.Specialized, files);
				
		//create a DatasetTask
		ParseDavidEnrichmentResults  enrichmentResultsFilesTask = new ParseDavidEnrichmentResults(dataset);
        enrichmentResultsFilesTask.run(taskMonitor); 

        CreateDummyExpressionTask dummyExpressionTask = new CreateDummyExpressionTask(dataset);
		dummyExpressionTask.run(taskMonitor);	
		
		em.filterGenesets();
		
		InitializeGenesetsOfInterestTask genesets_init = new InitializeGenesetsOfInterestTask(em);
        genesets_init.run(taskMonitor);
        
        Baton<Map<SimilarityKey, GenesetSimilarity>> baton = new Baton<>();
		ComputeSimilarityTaskParallel similarities = new ComputeSimilarityTaskParallel(em, baton.consumer());	
        similarities.run(taskMonitor);

				
		//check to see if the dataset loaded - there should be 215 genesets
		assertEquals(215, dataset.getSetOfGeneSets().getGeneSets().size());
		//there should also be 215 enrichments (the genesets are built from the txt file)
		assertEquals(215, dataset.getEnrichments().getEnrichments().size());
		//there should be 7 genesets in the enrichments of interest
		assertEquals(7, dataset.getGeneSetsOfInterest().getGeneSets().size());
		//there should be 7 * 6 edges
		assertEquals(11, baton.supplier().get().size());
		//there should be a total of 366 genes
		assertEquals(414, em.getNumberOfGenes());
		//there should be 43 genes in the geneset "nucleolus"
		assertEquals(114, em.getAllGeneSets().get("ACETYLATION").getGenes().size());

		//make sure the dummy expression has values for all the genes
		assertEquals(414, dataset.getExpressionSets().getNumGenes());
		assertEquals(414,dataset.getExpressionGenes().size()); 
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
		EMCreationParameters params = 
			new EMCreationParameters("EM1_", pvalue, qvalue, NESFilter.ALL, Optional.empty(), true, SimilarityMetric.JACCARD, similarityCutoff, 0.5, EdgeStrategy.AUTOMATIC);
		
		//create an new enrichment Map
		EnrichmentMap em = new EnrichmentMap(params, serviceRegistrar);
		
		//Load first dataset
		//create a dataset
		EMDataSet dataset = em.createDataSet(LegacySupport.DATASET1, Method.Specialized, files);
				
		//create a DatasetTask
		ParseDavidEnrichmentResults enrichmentResultsFilesTask = new ParseDavidEnrichmentResults(dataset);
		enrichmentResultsFilesTask.run(taskMonitor); 

		       
		
		//Load second dataset
		//create a dataset
		EMDataSet dataset2 = em.createDataSet(LegacySupport.DATASET2, Method.Specialized, files2);
		
		//create a DatasetTask
		ParseDavidEnrichmentResults enrichmentResultsFiles2Task = new ParseDavidEnrichmentResults(dataset2);
		enrichmentResultsFiles2Task.run(taskMonitor);

		// check to see if the two datasets are distinct
		if (!((dataset.getExpressionGenes().containsAll(dataset2.getExpressionGenes()))
		   && (dataset2.getExpressionGenes().containsAll(dataset.getExpressionGenes()))))
			em.setDistinctExpressionSets(true);

		CreateDummyExpressionTask dummyExpressionTask = new CreateDummyExpressionTask(dataset);
		dummyExpressionTask.run(taskMonitor);
		CreateDummyExpressionTask dummyExpressionTask2 = new CreateDummyExpressionTask(dataset2);
		dummyExpressionTask2.run(taskMonitor);

		em.filterGenesets();

		InitializeGenesetsOfInterestTask genesets_init = new InitializeGenesetsOfInterestTask(em);
		genesets_init.run(taskMonitor);

		// check to see if the dataset loaded - there should be 215 genesets
		assertEquals(215, dataset.getSetOfGeneSets().getGeneSets().size());
		// there should also be 215 enrichments (the genesets are built from the txt file)
		assertEquals(215, dataset.getEnrichments().getEnrichments().size());
		// there should be 7 genesets in the enrichments of interest
		assertEquals(7, dataset.getGeneSetsOfInterest().getGeneSets().size());

		// there should be 114 genes in the geneset "acetylation"
		assertEquals(114, em.getDataSet(LegacySupport.DATASET1).getSetOfGeneSets().getGeneSets().get("ACETYLATION").getGenes().size());
//		assertEquals(114, em.getAllGenesets().get("ACETYLATION").getGenes().size());

		dataset2 = em.getDataSet(LegacySupport.DATASET2);
		// check the stats for dataset2
		// check to see if the dataset loaded - there should be 263 genesets
		assertEquals(263, dataset2.getSetOfGeneSets().getGeneSets().size());
		// there should also be 263 enrichments (the genesets are built from the bgo file)
		assertEquals(263, dataset2.getEnrichments().getEnrichments().size());
		// there should be 0 genesets in the enrichments of interest
		assertEquals(0, dataset2.getGeneSetsOfInterest().getGeneSets().size());

		// make sure the dummy expression has values for all the genes
		assertEquals(367, dataset2.getExpressionSets().getNumGenes());
		assertEquals(367, dataset2.getExpressionGenes().size());

		// there should be 20 edges (2 edges for every node because of the distinct expresison sets)
		// assertEquals((7*6),em.getGenesetSimilarity().size()); there should be a total of 366 genes
		assertEquals(661, em.getNumberOfGenes());

	}
		
}
