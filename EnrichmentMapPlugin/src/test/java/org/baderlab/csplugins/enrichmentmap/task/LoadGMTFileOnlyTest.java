package org.baderlab.csplugins.enrichmentmap.task;

import junit.framework.TestCase;

import org.baderlab.csplugins.enrichmentmap.EnrichmentMapParameters;
import org.baderlab.csplugins.enrichmentmap.model.DataSet;
import org.baderlab.csplugins.enrichmentmap.model.DataSetFiles;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;

public class LoadGMTFileOnlyTest extends TestCase {
	public void setUp() throws Exception {
		
	}
	
	public void testGMTOnly(){
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
		LoadDataSetTask load_task = new LoadDataSetTask(dataset);
				
		load_task.run();
		
		em.filterGenesets();
		
		InitializeGenesetsOfInterestTask genesets_init = new InitializeGenesetsOfInterestTask(em);
        genesets_init.run();
        
        ComputeSimilarityTask similarities = new ComputeSimilarityTask(em);
        similarities.run();
				
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
