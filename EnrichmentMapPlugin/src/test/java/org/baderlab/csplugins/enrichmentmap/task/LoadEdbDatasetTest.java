package org.baderlab.csplugins.enrichmentmap.task;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import org.baderlab.csplugins.enrichmentmap.EnrichmentMapParameters;
import org.baderlab.csplugins.enrichmentmap.model.DataSet;
import org.baderlab.csplugins.enrichmentmap.model.DataSetFiles;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.parsers.GMTFileReaderTask;
import org.baderlab.csplugins.enrichmentmap.parsers.ParseEDBEnrichmentResults;
import org.cytoscape.work.TaskMonitor;
import org.junit.Test;

public class LoadEdbDatasetTest {


	private TaskMonitor taskMonitor = mock(TaskMonitor.class);
	
	
	@Test
	public void testEdbLoad() throws Exception{
		EnrichmentMapParameters params = new EnrichmentMapParameters();
		
		//for a dataset we require genesets, an expression file (optional), enrichment results
		String testEdbResultsFileName = "src/test/resources/org/baderlab/csplugins/enrichmentmap/task/LoadDataset/GSEA_example_results/edb/results.edb";
		String testgmtFileName = "src/test/resources/org/baderlab/csplugins/enrichmentmap/task/LoadDataset/GSEA_example_results/edb/gene_sets.gmt";
		String testrnkFileName = "src/test/resources/org/baderlab/csplugins/enrichmentmap/task/LoadDataset/GSEA_example_results/edb/Expressionfile.rnk";		
		
		DataSetFiles files = new DataSetFiles();		
		files.setEnrichmentFileName1(testEdbResultsFileName);
		files.setGMTFileName(testgmtFileName);
		files.setRankedFile(testrnkFileName);
		params.addFiles(EnrichmentMap.DATASET1, files);
		
		//set the method to gsea
		params.setMethod(EnrichmentMapParameters.method_GSEA);
		params.setSimilarityMetric(EnrichmentMapParameters.SM_JACCARD);
		params.setSimilarityCutOff(0.5);
		params.setPvalue(1.0);
		params.setQvalue(1.0); 
	
		//create an new enrichment Map
		EnrichmentMap em = new EnrichmentMap(params);
		
		//Load data set
		//create a dataset
		DataSet dataset = new DataSet(em, EnrichmentMap.DATASET1,files);		
		em.addDataset(EnrichmentMap.DATASET1, dataset);

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
