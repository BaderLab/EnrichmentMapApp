package org.baderlab.csplugins.enrichmentmap.task;

import org.baderlab.csplugins.enrichmentmap.EnrichmentMapParameters;
import org.baderlab.csplugins.enrichmentmap.model.DataSet;
import org.baderlab.csplugins.enrichmentmap.model.DataSetFiles;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;

import junit.framework.TestCase;

public class LoadEdbDatasetTest extends TestCase {
	public void setUp() throws Exception {
		
	}
	
	public void testEdbLoad(){
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
		LoadDataSetTask load_task = new LoadDataSetTask(dataset);
				
		load_task.run();
		
		em.filterGenesets();
		
		InitializeGenesetsOfInterestTask genesets_init = new InitializeGenesetsOfInterestTask(em);
        genesets_init.run();
        
        ComputeSimilarityTask similarities = new ComputeSimilarityTask(em);
        similarities.run();
        
      //check to see if the dataset loaded
        //although the original analysis had 193 genesets because this is loaded from
        //edb version it only stores the genesets that overlapped with the dataset analyzed.
      	assertEquals(14, dataset.getSetofgenesets().getGenesets().size());
      	assertEquals(14, dataset.getEnrichments().getEnrichments().size());
      	assertEquals(41, dataset.getDatasetGenes().size());
      	assertEquals(41, dataset.getExpressionSets().getNumGenes());

				
	}
}
