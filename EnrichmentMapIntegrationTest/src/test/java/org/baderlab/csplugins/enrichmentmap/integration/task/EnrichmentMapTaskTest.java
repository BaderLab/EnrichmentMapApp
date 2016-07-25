package org.baderlab.csplugins.enrichmentmap.integration.task;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import org.baderlab.csplugins.enrichmentmap.EnrichmentMapParameters;
import org.baderlab.csplugins.enrichmentmap.integration.BaseIntegrationTest;
import org.baderlab.csplugins.enrichmentmap.integration.EdgeSimilarities;
import org.baderlab.csplugins.enrichmentmap.integration.TestUtils;
import org.baderlab.csplugins.enrichmentmap.model.DataSetFiles;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.task.EnrichmentMapBuildMapTaskFactory;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.io.util.StreamUtil;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTableFactory;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.session.CySessionManager;
import org.cytoscape.task.edit.MapTableToNetworkTablesTaskFactory;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.cytoscape.work.SynchronousTaskManager;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.swing.DialogTaskManager;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.junit.PaxExam;

@RunWith(PaxExam.class)
public class EnrichmentMapTaskTest extends BaseIntegrationTest {
	
	private static final String PATH = "/org/baderlab/csplugins/enrichmentmap/task/EMandPA/";
	
	
	@Inject private CyNetworkManager networkManager;
	@Inject private CyNetworkFactory networkFactory;
	@Inject private CyTableFactory tableFactory;
	@Inject private CyApplicationManager applicationManager;
	@Inject private CyTableManager tableManager;
	@Inject private CySessionManager sessionManager;
	@Inject private CyNetworkViewManager networkViewManager;
	@Inject private CyNetworkViewFactory networkViewFactory;
	@Inject private VisualMappingManager visualMappingManager;
	@Inject private VisualStyleFactory visualStyleFactory;
	@Inject private VisualMappingFunctionFactory vmfFactoryContinuous;
	@Inject private VisualMappingFunctionFactory vmfFactoryDiscrete;
	@Inject private VisualMappingFunctionFactory vmfFactoryPassthrough;
	@Inject private DialogTaskManager dialog;
	@Inject private CyLayoutAlgorithmManager layoutManager;
	@Inject private MapTableToNetworkTablesTaskFactory mapTableToNetworkTable;
//	@Inject private CySwingApplication swingApplication ;
	@Inject private StreamUtil streamUtil;
	@Inject private SynchronousTaskManager<?> taskManager;
	
	
	private String createTempFile(String fileName) throws IOException {
		int dot = fileName.indexOf('.');
		String prefix = fileName.substring(0, dot);
		String suffix = fileName.substring(dot+1);
		File tempFile = File.createTempFile(prefix, suffix);
		Files.copy(getClass().getResourceAsStream(PATH + prefix + "." + suffix), tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
		return tempFile.getAbsolutePath();
	}
	
	protected void buildEnrichmentMap(EnrichmentMapParameters emParams) {
		EnrichmentMap map = new EnrichmentMap(emParams);
	   	EnrichmentMapBuildMapTaskFactory buildmap = new EnrichmentMapBuildMapTaskFactory(map,  
	        			applicationManager, null /*swingApplication*/, networkManager, networkViewManager,
	        			networkViewFactory, networkFactory, tableFactory,
	        			tableManager, visualMappingManager, visualStyleFactory,
	        			vmfFactoryContinuous, vmfFactoryDiscrete, vmfFactoryPassthrough, 
	        			dialog, streamUtil, layoutManager, mapTableToNetworkTable);
	    
	   	TaskIterator taskIterator = buildmap.createTaskIterator();
	   	
//	    // make sure the task iterator completes
//	    TaskObserver observer = new TaskObserver() {
//			public void taskFinished(ObservableTask task) { }
//			public void allFinished(FinishStatus finishStatus) {
//				if(finishStatus == null)
//					fail();
//				if(finishStatus.getType() != FinishStatus.Type.SUCCEEDED)
//					throw new AssertionError("TaskIterator Failed", finishStatus.getException());
//			}
//		};

	   	taskManager.execute(taskIterator);
//	   	SerialTestTaskManager testTaskManager = new SerialTestTaskManager();
//	   	testTaskManager.ignoreTask(VisualizeEnrichmentMapTask.class);
//	   	testTaskManager.execute(taskIterator, observer);
//	   	testTaskManager.execute(taskIterator);
	}
	
	
	@Test
	public void test_1_EnrichmentMapBuildMapTask() throws Exception {
		String geneSetsFile = createTempFile("gene_sets.gmt");
		String expressionFile = createTempFile("FakeExpression.txt");
		String enrichmentFile = createTempFile("fakeEnrichments.txt");
		String rankFile = createTempFile("FakeRank.rnk");
		
		System.out.println(geneSetsFile);
		
		
		EnrichmentMapParameters emParams = new EnrichmentMapParameters(sessionManager, streamUtil, applicationManager);
		emParams.setMethod(EnrichmentMapParameters.method_generic);
		DataSetFiles dataset1files = new DataSetFiles();
		dataset1files.setGMTFileName(geneSetsFile);  
		dataset1files.setExpressionFileName(expressionFile);
		dataset1files.setEnrichmentFileName1(enrichmentFile);
		dataset1files.setRankedFile(rankFile);  
		emParams.addFiles(EnrichmentMap.DATASET1, dataset1files);
		
	    buildEnrichmentMap(emParams);
	   	
	   	// Assert the network is as expected
	   	Set<CyNetwork> networks = networkManager.getNetworkSet();
	   	assertEquals(1, networks.size());
	   	CyNetwork network = networks.iterator().next();
	   	
	   	Map<String,CyNode> nodes = TestUtils.getNodes(network);
	   	assertEquals(4, nodes.size());
	   	assertTrue(nodes.containsKey("BOTTOM8_PLUS100"));
	   	assertTrue(nodes.containsKey("MIDDLE8_PLUS100"));
	   	assertTrue(nodes.containsKey("TOP8_PLUS100"));
	   	assertTrue(nodes.containsKey("TOP1_PLUS100"));
	   	
	   	EdgeSimilarities edges = TestUtils.getEdgeSimilarities(network);
	   	
	   	assertEquals(6, edges.size());
	   	assertTrue(edges.containsEdge("MIDDLE8_PLUS100", "Geneset_Overlap", "BOTTOM8_PLUS100"));
	   	assertTrue(edges.containsEdge("TOP8_PLUS100", "Geneset_Overlap", "MIDDLE8_PLUS100"));
	   	assertTrue(edges.containsEdge("TOP8_PLUS100", "Geneset_Overlap", "BOTTOM8_PLUS100"));
	   	assertTrue(edges.containsEdge("TOP1_PLUS100", "Geneset_Overlap", "TOP8_PLUS100"));
	   	assertTrue(edges.containsEdge("TOP1_PLUS100", "Geneset_Overlap", "MIDDLE8_PLUS100"));
	   	assertTrue(edges.containsEdge("TOP1_PLUS100", "Geneset_Overlap" ,"BOTTOM8_PLUS100"));
	}
	

}
