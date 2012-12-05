package org.baderlab.csplugins.enrichmentmap.task;

import org.baderlab.csplugins.enrichmentmap.EnrichmentMapParameters;
import org.baderlab.csplugins.enrichmentmap.model.DataSet;
import org.baderlab.csplugins.enrichmentmap.model.DataSetFiles;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;

import junit.framework.TestCase;

public class LoadBingoResultsTest extends TestCase{
	public void setUp() throws Exception {
		
	}

	public void testLoadBingoResult_withoutexpression(){
		EnrichmentMapParameters params = new EnrichmentMapParameters();
	
		//for a dataset we require genesets, an expression file (optional), enrichment results
		String testBingoResultsFileName = "src/test/resources/org/baderlab/csplugins/enrichmentmap/task/bingo_output/12Hr_topgenes.bgo";
		
		DataSetFiles files = new DataSetFiles();		
		files.setEnrichmentFileName1(testBingoResultsFileName);
		params.addFiles(EnrichmentMap.DATASET1, files);
		
		//set the method to Bingo
		params.setMethod(EnrichmentMapParameters.method_DAVID);
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
				
		//create a DatasetTask
		LoadDataSetTask load_task = new LoadDataSetTask(dataset);
				
		load_task.run();
		
		em.filterGenesets();
		
		InitializeGenesetsOfInterestTask genesets_init = new InitializeGenesetsOfInterestTask(em);
        genesets_init.run();
        
        ComputeSimilarityTask similarities = new ComputeSimilarityTask(em);
        similarities.run();

				
		//check to see if the dataset loaded - there should be 74 genesets
		assertEquals(74, dataset.getSetofgenesets().getGenesets().size());
		//there should also be 74 enrichments (the genesets are built from the bgo file)
		assertEquals(74, dataset.getEnrichments().getEnrichments().size());
		//there should be 11 genesets in the enrichments of interest
		assertEquals(5, dataset.getGenesetsOfInterest().getGenesets().size());
		//there should be 5 edges
		
		
	}
}
