package org.baderlab.csplugins.enrichmentmap.task;

import static org.mockito.Mockito.mock;
import junit.framework.TestCase;

import org.baderlab.csplugins.enrichmentmap.EnrichmentMapParameters;
import org.baderlab.csplugins.enrichmentmap.StreamUtil;
import org.baderlab.csplugins.enrichmentmap.model.DataSet;
import org.baderlab.csplugins.enrichmentmap.model.DataSetFiles;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.parsers.GMTFileReaderTask;
import org.cytoscape.work.TaskMonitor;

public class LoadGMTFileOnlyTest extends TestCase {


	private TaskMonitor taskMonitor = mock(TaskMonitor.class);
	private StreamUtil streamUtil = new StreamUtil();
	
	
	public void setUp() throws Exception {
		
	}
	
	public void testGMTOnly() throws Exception{
		EnrichmentMapParameters params = new EnrichmentMapParameters();
	
		//for a dataset we require genesets, an expression file (optional), enrichment results
		String testGmtFileName = "src/test/resources/org/baderlab/csplugins/enrichmentmap/task/genesets_subset.gmt";
		
		DataSetFiles files = new DataSetFiles();		
		files.setGMTFileName(testGmtFileName);
		params.addFiles(EnrichmentMap.DATASET1, files);
	
		//create an new enrichment Map
		EnrichmentMap em = new EnrichmentMap(params);
		
		//Load data set
		//create a dataset
		DataSet dataset = new DataSet(em, EnrichmentMap.DATASET1,files);		
		em.addDataset(EnrichmentMap.DATASET1, dataset);
				
		//create a DatasetTask
				//set up task
		GMTFileReaderTask task = new GMTFileReaderTask(dataset,(org.cytoscape.io.util.StreamUtil)streamUtil);
	    task.run(taskMonitor);

	    //create dummy expression
	    CreateDummyExpressionTask dummyExpressionTask = new CreateDummyExpressionTask(dataset);
		dummyExpressionTask.run(taskMonitor);
				
		em.filterGenesets();
				
		InitializeGenesetsOfInterestTask genesets_init = new InitializeGenesetsOfInterestTask(em);
		genesets_init.run(taskMonitor);
		        
		ComputeSimilarityTask similarities = new ComputeSimilarityTask(em);
		similarities.run(taskMonitor);

				
		//check to see if the dataset loaded - there should be 36 genesets
		assertEquals(36, dataset.getSetofgenesets().getGenesets().size());
		//there should be (36 * 35)/2 edges (geneset similarities)
		assertEquals((36*35)/2, em.getGenesetSimilarity().size());
		//there should be 523 genes
		assertEquals(523, em.getNumberOfGenes());
		assertEquals(523, dataset.getExpressionSets().getNumGenes());
		assertEquals(523, dataset.getDatasetGenes().size());
		
		assertEquals(3,dataset.getExpressionSets().getNumConditions());
	}

}
