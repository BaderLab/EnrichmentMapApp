package org.baderlab.csplugins.enrichmentmap.task;

import static org.mockito.Mockito.mock;

import org.baderlab.csplugins.enrichmentmap.EnrichmentMapParameters;
import org.baderlab.csplugins.enrichmentmap.StreamUtil;
import org.baderlab.csplugins.enrichmentmap.model.DataSet;
import org.baderlab.csplugins.enrichmentmap.model.DataSetFiles;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.parsers.EnrichmentResultFileReaderTask;
import org.baderlab.csplugins.enrichmentmap.parsers.ExpressionFileReaderTask;
import org.baderlab.csplugins.enrichmentmap.parsers.GMTFileReaderTask;
import org.cytoscape.work.TaskMonitor;

import junit.framework.TestCase;

public class LoadDatasetTaskTest extends TestCase {
	
	private TaskMonitor taskMonitor = mock(TaskMonitor.class);
	private StreamUtil streamUtil = new StreamUtil();
	
	
    public void setUp() throws Exception {
    		
    	}

    public void testLoadDataset1GSEAResult_withexpression() throws Exception{
    		EnrichmentMapParameters params = new EnrichmentMapParameters();
		
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
		params.addFiles(EnrichmentMap.DATASET1, files);
		
		//create an new enrichment Map
		EnrichmentMap em = new EnrichmentMap(params);
		
		//create a dataset
		DataSet dataset = new DataSet(em, EnrichmentMap.DATASET1,files);
		
		//load Data
		GMTFileReaderTask task = new GMTFileReaderTask(dataset,(org.cytoscape.io.util.StreamUtil)streamUtil);
	    task.run(taskMonitor);
	    
		EnrichmentResultFileReaderTask enrichmentResultsFilesTask = new EnrichmentResultFileReaderTask(dataset,(org.cytoscape.io.util.StreamUtil)streamUtil);
        enrichmentResultsFilesTask.run(taskMonitor); 
        
      //load expression file
        ExpressionFileReaderTask exptask = new ExpressionFileReaderTask(dataset,(org.cytoscape.io.util.StreamUtil)streamUtil);
        exptask.run(taskMonitor);
        		
		//check to see if the dataset loaded
		assertEquals(193, dataset.getSetofgenesets().getGenesets().size());
		assertEquals(14, dataset.getEnrichments().getEnrichments().size());
		assertEquals(41, dataset.getDatasetGenes().size());
		assertEquals(41, dataset.getExpressionSets().getNumGenes());
		
		
    }
}
