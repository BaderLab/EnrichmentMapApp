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
import org.baderlab.csplugins.enrichmentmap.parsers.GMTFileReaderTask;
import org.baderlab.csplugins.enrichmentmap.util.Baton;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.work.TaskMonitor;
import org.junit.Test;

public class LoadGMTFileOnlyTest {

	private CyServiceRegistrar serviceRegistrar = TestUtils.mockServiceRegistrar();
	private TaskMonitor taskMonitor = mock(TaskMonitor.class);
	
	@Test
	public void testGMTOnly() throws Exception{
		//for a dataset we require genesets, an expression file (optional), enrichment results
		String testGmtFileName = "src/test/resources/org/baderlab/csplugins/enrichmentmap/task/genesets_subset.gmt";
		
		DataSetFiles files = new DataSetFiles();		
		files.setGMTFileName(testGmtFileName);
	
		//create an new enrichment Map
		double similarityCutoff = 0.5;
		double pvalue = 1.0;
		double qvalue = 1.0;
		EMCreationParameters params = 
			new EMCreationParameters("EM1_", pvalue, qvalue, NESFilter.ALL, Optional.empty(), true, SimilarityMetric.JACCARD, similarityCutoff, 0.5, EdgeStrategy.AUTOMATIC);
	
		EnrichmentMap em = new EnrichmentMap(params, serviceRegistrar);
		
		//Load data set
		//create a dataset
		EMDataSet dataset = em.createDataSet(LegacySupport.DATASET1, Method.GSEA, files);
				
		//create a DatasetTask
				//set up task
		GMTFileReaderTask task = new GMTFileReaderTask(dataset);
	    task.run(taskMonitor);
	    
	    dataset.setGeneSetsOfInterest(dataset.getSetOfGeneSets());
	    
	    //create dummy expression
	    CreateDummyExpressionTask dummyExpressionTask = new CreateDummyExpressionTask(dataset);
		dummyExpressionTask.run(taskMonitor);
				
		em.filterGenesets();
				
		InitializeGenesetsOfInterestTask genesets_init = new InitializeGenesetsOfInterestTask(em);
		genesets_init.run(taskMonitor);
		        
		Baton<Map<SimilarityKey, GenesetSimilarity>> baton = new Baton<>();
		ComputeSimilarityTaskParallel similarities = new ComputeSimilarityTaskParallel(em, baton.consumer());	
		similarities.run(taskMonitor);

				
		//check to see if the dataset loaded - there should be 36 genesets
		assertEquals(36, dataset.getSetOfGeneSets().getGeneSets().size());
		//there should be (36 * 35)/2 edges (geneset similarities)
		assertEquals(18, baton.supplier().get().size());
		//there should be 523 genes
		assertEquals(523, em.getNumberOfGenes());
//		assertEquals(523, dataset.getExpressionSets().getNumGenes());
//		assertEquals(523, dataset.getExpressionGenes().size());
		
		assertEquals(3,dataset.getExpressionSets().getNumConditions());
	}

}
