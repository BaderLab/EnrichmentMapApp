package org.baderlab.csplugins.enrichmentmap.model;

import java.util.HashMap;
import java.util.HashSet;

import org.baderlab.csplugins.enrichmentmap.EnrichmentMapParameters;
import org.baderlab.csplugins.enrichmentmap.parsers.GMTFileReaderTask;

import junit.framework.TestCase;

/*
 * A Set of Genesets comes from a gmt file
 * 
 */

public class SetOfGenesetsTest extends TestCase {
		
		EnrichmentMap map;
		DataSet dataset;
	
		public void setUp() throws Exception {
			//load Genesets from the gmt file associated with this test
			String testDataFileName = "src/test/resources/org/baderlab/csplugins/enrichmentmap/model/Genesetstestfile.gmt";
	        
	        //create a new instance of the parameters
	        EnrichmentMapParameters params = new EnrichmentMapParameters();        
	        //set gmt file name 
	        params.getDatasetFiles().get(EnrichmentMap.DATASET1).setGMTFileName(testDataFileName);
	        
	        //Create a new Enrichment map
	        map = new EnrichmentMap(params);
	                
	        //get the default dataset
	        dataset = map.getDataset(EnrichmentMap.DATASET1);

	        //set up task
	        GMTFileReaderTask task = new GMTFileReaderTask(dataset);

	        //read in file
	        task.run();
	    }
		
		public void testGSSetVar(){
			
			SetOfGeneSets gs_set = map.getDataset(EnrichmentMap.DATASET1).getSetofgenesets();
			
			assertEquals("src/test/resources/org/baderlab/csplugins/enrichmentmap/model/Genesetstestfile.gmt", gs_set.getFilename());
			
			//make sure there are 5 genesets
			assertEquals(6, gs_set.getGenesets().size());
			
			//check that there is 1 source
			assertEquals(1,gs_set.getGenesetTypes().size());
			//check that the source is Reactome
			assertTrue(gs_set.getGenesetTypes().contains("Reactome".toUpperCase()));			
			
			
		}
		
		public void testGSFilter(){
			
			SetOfGeneSets gs_set = map.getDataset(EnrichmentMap.DATASET1).getSetofgenesets();			
			
			//get the genes to hash so we can create our own dataset genes to filter by
			HashMap<String, Integer> genes = map.getGenes();
			
			HashSet<Integer> datasetgenes = new HashSet<Integer>();
			
			//define a dataset							
			if(genes.containsKey("HIST1H1B"))
				datasetgenes.add(genes.get("HIST1H1B"));
			if(genes.containsKey("HIST1H1A"))
				datasetgenes.add(genes.get("HIST1H1A"));
			if(genes.containsKey("HIST1H1C"))
				datasetgenes.add(genes.get("HIST1H1C"));
			if(genes.containsKey("HIST1H1D"))
				datasetgenes.add(genes.get("HIST1H1D"));
			if(genes.containsKey("HIST1H1E"))
				datasetgenes.add(genes.get("HIST1H1E"));
			
			assertEquals(5, datasetgenes.size());
			
			//filter the gene sets by the newly formed dataset.
			gs_set.filterGenesets(datasetgenes);
			
			assertEquals(5,gs_set.getGeneSetByName("APOPTOSIS INDUCED DNA FRAGMENTATION%REACTOME%REACT_1213.4").getGenes().size());
			assertEquals(5,gs_set.getGeneSetByName("APOPTOSIS%REACTOME%REACT_578.5").getGenes().size());
			assertEquals(0,gs_set.getGeneSetByName("APOPTOTIC CLEAVAGE OF CELL ADHESION PROTEINS%REACTOME%REACT_13579.1").getGenes().size());
			assertEquals(0,gs_set.getGeneSetByName("APOPTOTIC CLEAVAGE OF CELLULAR PROTEINS%REACTOME%REACT_107.4").getGenes().size());
			assertEquals(5,gs_set.getGeneSetByName("APOPTOTIC EXECUTION PHASE%REACTOME%REACT_995.6").getGenes().size());
			assertEquals(0,gs_set.getGeneSetByName("APOPTOTIC FACTOR-MEDIATED RESPONSE%REACTOME%REACT_963.2").getGenes().size());
		
		}
		

}
