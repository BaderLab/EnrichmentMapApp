package org.baderlab.csplugins.enrichmentmap.model;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import java.util.HashSet;
import java.util.Set;

import org.baderlab.csplugins.enrichmentmap.TestUtils;
import org.baderlab.csplugins.enrichmentmap.model.EMDataSet.Method;
import org.baderlab.csplugins.enrichmentmap.parsers.GMTFileReaderTask;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.work.TaskMonitor;
import org.jukito.JukitoRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.inject.Provider;

@RunWith(JukitoRunner.class)
public class SetOfGenesetsTest {
	
	private CyServiceRegistrar serviceRegistrar = TestUtils.mockServiceRegistrar();
	private TaskMonitor taskMonitor = mock(TaskMonitor.class);
	EnrichmentMap map;
	EMDataSet dataset;

	@Before
	public void before(Provider<EnrichmentMapParameters> empFactory) throws Exception {
		//load Genesets from the gmt file associated with this test
		String testDataFileName = "src/test/resources/org/baderlab/csplugins/enrichmentmap/model/Genesetstestfile.gmt";
        
        //create a new instance of the parameters
        EnrichmentMapParameters params = empFactory.get();        
        //set gmt file name 
        params.getFiles().get(LegacySupport.DATASET1).setGMTFileName(testDataFileName);
        
        //Create a new Enrichment map
        map = new EnrichmentMap(params.getCreationParameters(), serviceRegistrar);
                
        Method method = EnrichmentMapParameters.stringToMethod(params.getMethod());
        DataSetFiles files = params.getFiles().get(LegacySupport.DATASET1);
        dataset = map.createDataSet(LegacySupport.DATASET1, method, files);
        
        //set up task
        GMTFileReaderTask task = new GMTFileReaderTask(dataset);
        task.run(taskMonitor);
    }
	
	@Test
	public void testGSSetVar(){
		
		SetOfGeneSets gs_set = map.getDataSet(LegacySupport.DATASET1).getSetOfGeneSets();
		
		assertEquals("src/test/resources/org/baderlab/csplugins/enrichmentmap/model/Genesetstestfile.gmt", gs_set.getFilename());
		
		//make sure there are 5 genesets
		assertEquals(6, gs_set.getGeneSets().size());
	}
	
	@Test
	public void testGSFilter(){
		
		SetOfGeneSets gs_set = map.getDataSet(LegacySupport.DATASET1).getSetOfGeneSets();			
		
		//get the genes to hash so we can create our own dataset genes to filter by
		
		Set<Integer> datasetgenes = new HashSet<>();
		
		//define a dataset							
		if(map.containsGene("HIST1H1B"))
			datasetgenes.add(map.getHashFromGene("HIST1H1B"));
		if(map.containsGene("HIST1H1A"))
			datasetgenes.add(map.getHashFromGene("HIST1H1A"));
		if(map.containsGene("HIST1H1C"))
			datasetgenes.add(map.getHashFromGene("HIST1H1C"));
		if(map.containsGene("HIST1H1D"))
			datasetgenes.add(map.getHashFromGene("HIST1H1D"));
		if(map.containsGene("HIST1H1E"))
			datasetgenes.add(map.getHashFromGene("HIST1H1E"));
		
		assertEquals(5, datasetgenes.size());
		
		//filter the gene sets by the newly formed dataset.
		gs_set.filterGeneSets(datasetgenes);
		
		assertEquals(5,gs_set.getGeneSetByName("APOPTOSIS INDUCED DNA FRAGMENTATION%REACTOME%REACT_1213.4").getGenes().size());
		assertEquals(5,gs_set.getGeneSetByName("APOPTOSIS%REACTOME%REACT_578.5").getGenes().size());
		assertEquals(0,gs_set.getGeneSetByName("APOPTOTIC CLEAVAGE OF CELL ADHESION PROTEINS%REACTOME%REACT_13579.1").getGenes().size());
		assertEquals(0,gs_set.getGeneSetByName("APOPTOTIC CLEAVAGE OF CELLULAR PROTEINS%REACTOME%REACT_107.4").getGenes().size());
		assertEquals(5,gs_set.getGeneSetByName("APOPTOTIC EXECUTION PHASE%REACTOME%REACT_995.6").getGenes().size());
		assertEquals(0,gs_set.getGeneSetByName("APOPTOTIC FACTOR-MEDIATED RESPONSE%REACTOME%REACT_963.2").getGenes().size());
	
	}
	

}
