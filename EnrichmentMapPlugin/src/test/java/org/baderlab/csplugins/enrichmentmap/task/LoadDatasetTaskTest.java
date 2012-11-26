package org.baderlab.csplugins.enrichmentmap.task;

import org.baderlab.csplugins.enrichmentmap.EnrichmentMapParameters;
import org.baderlab.csplugins.enrichmentmap.model.DataSet;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;

import junit.framework.TestCase;

public class LoadDatasetTaskTest extends TestCase {

    public void setUp() throws Exception {
    		
    	}

    public void testLoadDataset1GSEAResult_withexpression(){
    		EnrichmentMapParameters params = new EnrichmentMapParameters();
		
		//for a dataset we require genesets, an expression file (optional), enrichment results
		String testGMTFileName = "src/test/resources/org/baderlab/csplugins/enrichmentmap/task/LoadDataset/gs_apop_mouse.gmt";
		String testExpressionFileName = "src/test/resources/org/baderlab/csplugins/enrichmentmap/task/LoadDataset/Expressiontestfile.gct";
		String testGSEAResults1FileName = "src/test/resources/org/baderlab/csplugins/enrichmentmap/task/LoadDataset/GSEA_enrichments1.xls";
		String testGSEAResults2FileName = "src/test/resources/org/baderlab/csplugins/enrichmentmap/task/LoadDataset/GSEA_enrichments2.xls";
  	           
		params.setGMTFileName(testGMTFileName);
		params.setExpressionFileName1(testExpressionFileName);
		params.setEnrichmentDataset1FileName1(testGSEAResults1FileName);
		params.setEnrichmentDataset1FileName2(testGSEAResults2FileName);
		
		//create an new enrichment Map
		EnrichmentMap em = new EnrichmentMap(params);
		
		//create a dataset
		DataSet dataset = new DataSet(em);
		
		//create a DatasetTask
		LoadDataSetTask task = new LoadDataSetTask(dataset);
		
		task.run();
		
		//check to see if the dataset loaded
		assertEquals(193, dataset.getSetofgenesets().getGenesets().size());
		assertEquals(14, dataset.getEnrichments().getEnrichments().size());
		assertEquals(41, dataset.getDatasetGenes().size());
		assertEquals(41, dataset.getExpressionSets().getNumGenes());
		
		
    }
}
