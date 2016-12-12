package org.baderlab.csplugins.enrichmentmap.task;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import java.util.Optional;

import org.baderlab.csplugins.enrichmentmap.TestUtils;
import org.baderlab.csplugins.enrichmentmap.model.DataSet;
import org.baderlab.csplugins.enrichmentmap.model.DataSet.Method;
import org.baderlab.csplugins.enrichmentmap.model.DataSetFiles;
import org.baderlab.csplugins.enrichmentmap.model.EMCreationParameters;
import org.baderlab.csplugins.enrichmentmap.model.EMCreationParameters.SimilarityMetric;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentResultFilterParams.NESFilter;
import org.baderlab.csplugins.enrichmentmap.model.LegacySupport;
import org.baderlab.csplugins.enrichmentmap.parsers.GMTFileReaderTask;
import org.baderlab.csplugins.enrichmentmap.parsers.ParseEDBEnrichmentResults;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.work.TaskMonitor;
import org.junit.Test;

public class LoadEdbDatasetTest {

	private CyServiceRegistrar serviceRegistrar = TestUtils.mockServiceRegistrar();
	private TaskMonitor taskMonitor = mock(TaskMonitor.class);
	
	@Test
	public void testEdbLoad() throws Exception{
		//for a dataset we require genesets, an expression file (optional), enrichment results
		String testEdbResultsFileName = "src/test/resources/org/baderlab/csplugins/enrichmentmap/task/LoadDataset/GSEA_example_results/edb/results.edb";
		String testgmtFileName        = "src/test/resources/org/baderlab/csplugins/enrichmentmap/task/LoadDataset/GSEA_example_results/edb/gene_sets.gmt";
		String testrnkFileName        = "src/test/resources/org/baderlab/csplugins/enrichmentmap/task/LoadDataset/GSEA_example_results/edb/Expressionfile.rnk";		
		
		DataSetFiles files = new DataSetFiles();		
		files.setEnrichmentFileName1(testEdbResultsFileName);
		files.setGMTFileName(testgmtFileName);
		files.setRankedFile(testrnkFileName);
		
		//set the method to gsea
		double similarityCutoff = 0.5;
		double pvalue = 1.0;
		double qvalue = 1.0;
		EMCreationParameters params = new EMCreationParameters("EM1_", pvalue, qvalue, NESFilter.ALL, Optional.empty(), SimilarityMetric.JACCARD, similarityCutoff, 0.5);
	
		//create an new enrichment Map
		EnrichmentMap em = new EnrichmentMap(params, serviceRegistrar);
		
		//Load data set
		//create a dataset
		DataSet dataset = em.createDataSet(LegacySupport.DATASET1, Method.GSEA, files);

		//create a DatasetTask
		//create a DatasetTask
		//load Data
		GMTFileReaderTask task = new GMTFileReaderTask(dataset);
	    task.run(taskMonitor);

	    ParseEDBEnrichmentResults enrichmentResultsFilesTask = new ParseEDBEnrichmentResults(dataset);
        enrichmentResultsFilesTask.run(taskMonitor); 
        
        //create dummy expression
        CreateDummyExpressionTask dummyExpressionTask = new CreateDummyExpressionTask(dataset);
		dummyExpressionTask.run(taskMonitor);		        
		
		em.filterGenesets();
		
		InitializeGenesetsOfInterestTask genesets_init = new InitializeGenesetsOfInterestTask(em);
        genesets_init.run(taskMonitor);
        
        ComputeSimilarityTask similarities = new ComputeSimilarityTask(em);
        similarities.run(taskMonitor);
		
        
        //check to see if the dataset loaded
        //although the original analysis had 193 genesets because this is loaded from
        //edb version it only stores the genesets that overlapped with the dataset analyzed.
      	assertEquals(14, dataset.getSetofgenesets().getGenesets().size());
      	assertEquals(14, dataset.getEnrichments().getEnrichments().size());
      	assertEquals(41, dataset.getDatasetGenes().size());
      	assertEquals(41, dataset.getExpressionSets().getNumGenes());
	}
}
