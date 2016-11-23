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
import org.baderlab.csplugins.enrichmentmap.parsers.ExpressionFileReaderTask;
import org.baderlab.csplugins.enrichmentmap.parsers.GMTFileReaderTask;
import org.baderlab.csplugins.enrichmentmap.parsers.ParseGSEAEnrichmentResults;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.work.TaskMonitor;
import org.junit.Test;

public class LoadDatasetTaskTest {
	
	private CyServiceRegistrar serviceRegistrar = TestUtils.mockServiceRegistrar();
	private TaskMonitor taskMonitor = mock(TaskMonitor.class);
	
	@Test
    public void testLoadDataset1GSEAResult_withexpression() throws Exception{
		//for a dataset we require genesets, an expression file (optional), enrichment results
		String testGMTFileName = "src/test/resources/org/baderlab/csplugins/enrichmentmap/task/LoadDataset/gs_apop_mouse.gmt";
		String testExpressionFileName = "src/test/resources/org/baderlab/csplugins/enrichmentmap/task/LoadDataset/Expressiontestfile.gct";
		String testGSEAResults1FileName = "src/test/resources/org/baderlab/csplugins/enrichmentmap/task/LoadDataset/GSEA_enrichments1.xls";
		String testGSEAResults2FileName = "src/test/resources/org/baderlab/csplugins/enrichmentmap/task/LoadDataset/GSEA_enrichments2.xls";
  	    
		DataSetFiles files = new DataSetFiles();
		files.setGMTFileName(testGMTFileName);
		files.setExpressionFileName(testExpressionFileName);
		files.setEnrichmentFileName1(testGSEAResults1FileName);
		files.setEnrichmentFileName2(testGSEAResults2FileName);
		
		EMCreationParameters params = new EMCreationParameters("EM1_", 0.1, 0.1, NESFilter.ALL, Optional.empty(), SimilarityMetric.JACCARD, 0.1, 0.1);
		
		//create an new enrichment Map
		EnrichmentMap em = new EnrichmentMap(params, serviceRegistrar);
		
		//create a dataset
		DataSet dataset = new DataSet(em, LegacySupport.DATASET1, Method.Generic, files);
		em.addDataSet(LegacySupport.DATASET1, dataset);
		
		//load Data
		GMTFileReaderTask task = new GMTFileReaderTask(dataset);
	    task.run(taskMonitor);
	    
	    ParseGSEAEnrichmentResults enrichmentResultsFilesTask = new ParseGSEAEnrichmentResults(dataset);
        enrichmentResultsFilesTask.run(taskMonitor); 
        
        //load expression file
        ExpressionFileReaderTask exptask = new ExpressionFileReaderTask(dataset);
        exptask.run(taskMonitor);
        		
		//check to see if the dataset loaded
		assertEquals(193, dataset.getSetofgenesets().getGenesets().size());
		assertEquals(14, dataset.getEnrichments().getEnrichments().size());
		assertEquals(41, dataset.getDatasetGenes().size());
		assertEquals(41, dataset.getExpressionSets().getNumGenes());
    }
}
