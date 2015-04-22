package org.baderlab.csplugins.enrichmentmap.task;


import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.baderlab.csplugins.enrichmentmap.EnrichmentMapManager;
import org.baderlab.csplugins.enrichmentmap.EnrichmentMapParameters;
import org.baderlab.csplugins.enrichmentmap.PostAnalysisParameters;
import org.baderlab.csplugins.enrichmentmap.PostAnalysisParameters.FilterMetric;
import org.baderlab.csplugins.enrichmentmap.SerialTestTaskManager;
import org.baderlab.csplugins.enrichmentmap.StreamUtil;
import org.baderlab.csplugins.enrichmentmap.actions.LoadSignatureSetsActionListener;
import org.baderlab.csplugins.enrichmentmap.model.DataSetFiles;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.view.PostAnalysisInputPanel;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTableFactory;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.model.NetworkTestSupport;
import org.cytoscape.model.TableTestSupport;
import org.cytoscape.session.CySession;
import org.cytoscape.session.CySessionManager;
import org.cytoscape.task.edit.MapTableToNetworkTablesTaskFactory;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.cytoscape.work.FinishStatus;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.TaskObserver;
import org.cytoscape.work.swing.DialogTaskManager;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;


@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class EnrichmentMapBuildMapTaskTest {
	
	private static final String PATH = "src/test/resources/org/baderlab/csplugins/enrichmentmap/task/EMandPA/";
	
	private StreamUtil streamUtil = new StreamUtil();
	
	private NetworkTestSupport networkTestSupport = new NetworkTestSupport();
	private TableTestSupport tableTestSupport = new TableTestSupport();
	
    private CyNetworkManager networkManager = networkTestSupport.getNetworkManager();
    private CyNetworkFactory networkFactory = networkTestSupport.getNetworkFactory();
    private CyTableFactory tableFactory = tableTestSupport.getTableFactory();
    
    @Mock private CyApplicationManager applicationManager;
	@Mock private CyTableManager tableManager;
	@Mock private CySessionManager sessionManager;
	@Mock private CyNetworkViewManager networkViewManager;
	@Mock private CyNetworkViewFactory networkViewFactory;
	@Mock private VisualMappingManager visualMappingManager;
	@Mock private VisualStyleFactory visualStyleFactory;
	@Mock private VisualMappingFunctionFactory vmfFactoryContinuous;
	@Mock private VisualMappingFunctionFactory vmfFactoryDiscrete;
	@Mock private VisualMappingFunctionFactory vmfFactoryPassthrough;
	@Mock private DialogTaskManager dialog;
	@Mock private CyLayoutAlgorithmManager layoutManager;
	@Mock private MapTableToNetworkTablesTaskFactory mapTableToNetworkTable;
    @Mock private CyEventHelper eventHelper;
    @Mock private CySwingApplication swingApplication;
    
	
	private static CyNetwork emNetwork;
	
	
	@Before
	public void before() {
		MockitoAnnotations.initMocks(this);
		CySession emptySession = new CySession.Builder().build();
		when(sessionManager.getCurrentSession()).thenReturn(emptySession);
	}
	
	
	private Map<String,CyNode> getNodes(CyNetwork network) {
		Map<String,CyNode> nodes = new HashMap<>();
	   	for(CyNode node : network.getNodeList()) {
	   		nodes.put(network.getRow(node).get("name", String.class), node);
	   	}
	   	return nodes;
	}
	
	private Map<String,CyEdge> getEdges(CyNetwork network) {
		Map<String,CyEdge> edges = new HashMap<>();
	   	for(CyEdge edge : network.getEdgeList()) {
	   		edges.put(network.getRow(edge).get("name", String.class), edge);
	   	}
	   	return edges;
	}
	
	
	private void buildEnrichmentMap(EnrichmentMapParameters emParams) {
		EnrichmentMap map = new EnrichmentMap(emParams);
	   	EnrichmentMapBuildMapTaskFactory buildmap = new EnrichmentMapBuildMapTaskFactory(map,  
	        			applicationManager, networkManager, networkViewManager,
	        			networkViewFactory, networkFactory, tableFactory,
	        			tableManager, visualMappingManager, visualStyleFactory,
	        			vmfFactoryContinuous, vmfFactoryDiscrete, vmfFactoryPassthrough, 
	        			dialog, streamUtil, layoutManager, mapTableToNetworkTable);
	    
	   	TaskIterator taskIterator = buildmap.createTaskIterator();
	   	
	    // make sure the task iterator completes
	    TaskObserver observer = new TaskObserver() {
			public void taskFinished(ObservableTask task) { }
			public void allFinished(FinishStatus finishStatus) {
				if(finishStatus == null)
					fail();
				if(finishStatus.getType() != FinishStatus.Type.SUCCEEDED)
					throw new AssertionError("TaskIterator Failed", finishStatus.getException());
			}
		};

	   	SerialTestTaskManager testTaskManager = new SerialTestTaskManager();
	   	testTaskManager.ignoreTask(VisualizeEnrichmentMapTask.class);
	   	testTaskManager.execute(taskIterator, observer);
	}
	
	
	@Test
	public void test_1_EnrichmentMapBuildMapTask() {
		EnrichmentMapParameters emParams = new EnrichmentMapParameters(sessionManager, streamUtil, applicationManager);
		emParams.setMethod(EnrichmentMapParameters.method_generic);
		DataSetFiles dataset1files = new DataSetFiles();
		dataset1files.setGMTFileName(PATH + "gene_sets.gmt");  
		dataset1files.setExpressionFileName(PATH + "FakeExpression.txt");
		dataset1files.setEnrichmentFileName1(PATH + "fakeEnrichments.txt");
		dataset1files.setRankedFile(PATH + "FakeRank.rnk");  
		emParams.addFiles(EnrichmentMap.DATASET1, dataset1files);
		
	    buildEnrichmentMap(emParams);
	   	
	   	// Assert the network is as expected
	   	Set<CyNetwork> networks = networkManager.getNetworkSet();
	   	assertEquals(1, networks.size());
	   	CyNetwork network = networks.iterator().next();
	   	
	   	Map<String,CyNode> nodes = getNodes(network);
	   	assertEquals(4, nodes.size());
	   	assertTrue(nodes.containsKey("BOTTOM8_PLUS100"));
	   	assertTrue(nodes.containsKey("MIDDLE8_PLUS100"));
	   	assertTrue(nodes.containsKey("TOP8_PLUS100"));
	   	assertTrue(nodes.containsKey("TOP1_PLUS100"));
	   	
	   	Map<String,CyEdge> edges = getEdges(network);
	   	assertEquals(6, edges.size());
	   	assertTrue(edges.containsKey("MIDDLE8_PLUS100 (Geneset_Overlap) BOTTOM8_PLUS100"));
	   	assertTrue(edges.containsKey("TOP8_PLUS100 (Geneset_Overlap) MIDDLE8_PLUS100"));
	   	assertTrue(edges.containsKey("TOP8_PLUS100 (Geneset_Overlap) BOTTOM8_PLUS100"));
	   	assertTrue(edges.containsKey("TOP1_PLUS100 (Geneset_Overlap) TOP8_PLUS100"));
	   	assertTrue(edges.containsKey("TOP1_PLUS100 (Geneset_Overlap) MIDDLE8_PLUS100"));
	   	assertTrue(edges.containsKey("TOP1_PLUS100 (Geneset_Overlap) BOTTOM8_PLUS100"));
	   	
	   	// Make the network available to the subsequent test methods (requires test methods are run in order)
	   	emNetwork = network;
	}
	
	
	
	private void runPostAnalysis(PostAnalysisParameters paParams) throws Exception {
		// Set up mocks
		when(applicationManager.getCurrentNetwork()).thenReturn(emNetwork);
		CyNetworkView networkViewMock = mock(CyNetworkView.class);
		when(applicationManager.getCurrentNetworkView()).thenReturn(networkViewMock);
		@SuppressWarnings("unchecked")
		View<CyNode> nodeViewMock = mock(View.class);
		when(networkViewMock.getNodeView(Matchers.<CyNode>anyObject())).thenReturn(nodeViewMock);
		when(nodeViewMock.getVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION)).thenReturn(Double.valueOf(0.0));
		
		EnrichmentMap map = EnrichmentMapManager.getInstance().getMap(emNetwork.getSUID());
		assertNotNull(map);
		
		PostAnalysisInputPanel inputPanel = mock(PostAnalysisInputPanel.class);
		when(inputPanel.getPaParams()).thenReturn(paParams);
		
		// Load the gene-sets from the file
		SerialTestTaskManager testTaskManager = new SerialTestTaskManager();
		LoadSignatureSetsActionListener loadSignatureSetsActionListener 
			= new LoadSignatureSetsActionListener(inputPanel, swingApplication, applicationManager, testTaskManager, streamUtil);
		loadSignatureSetsActionListener.setSelectAll(true);
		loadSignatureSetsActionListener.actionPerformed(null);
		
		// Run post-analysis
		BuildDiseaseSignatureTask signatureTask = new BuildDiseaseSignatureTask(map, paParams, 
					sessionManager, streamUtil, applicationManager, 
					eventHelper, swingApplication);
		signatureTask.run(mock(TaskMonitor.class));
	}
	
	
	/**
	 * Run post-analysis with the default mann-whitney test.
	 * Uses the network that was created by the previous test method.
	 */
	@Test
	public void test_2_PostAnalysisMannWhitney() throws Exception {
		PostAnalysisParameters paParams = new PostAnalysisParameters();
    	paParams.setSignature_dataSet(EnrichmentMap.DATASET1);
    	paParams.setSignature_rankFile(EnrichmentMap.DATASET1);
    	paParams.setFilter(false);
		paParams.setKnownSignature(true);
		paParams.setSignatureHub(false);
		paParams.setUniverseSize(11445);
		paParams.setSignatureGMTFileName(PATH + "PA_top8_middle8_bottom8.gmt");
		
		runPostAnalysis(paParams);
		// Assert that post-analysis created the new nodes correctly
		
		Map<String,CyNode> nodes = getNodes(emNetwork);
	   	assertEquals(5, nodes.size());
	   	assertTrue(nodes.containsKey("PA_TOP8_MIDDLE8_BOTTOM8"));
	   	
	   	Map<String,CyEdge> edges = getEdges(emNetwork);
	   	assertEquals(8, edges.size());
	   	
	   	CyEdge edge1 = edges.get("PA_TOP8_MIDDLE8_BOTTOM8 (sig) TOP8_PLUS100");
	   	assertNotNull(edge1);
	   	assertEquals(1.40E-6, emNetwork.getRow(edge1).get("EM1_Overlap_Mann_Whit_pVal", Double.class), 0.001);
	   	
	   	CyEdge edge2 = edges.get("PA_TOP8_MIDDLE8_BOTTOM8 (sig) BOTTOM8_PLUS100");
	   	assertNotNull(edge2);
	   	assertEquals(1.40E-6, emNetwork.getRow(edge2).get("EM1_Overlap_Mann_Whit_pVal", Double.class), 0.001);
	}
	
	
	/**
	 * Run post-analysis again, but with hypergeometric test this time.
	 * The result should keep the 2 edges that were created by the previous run
	 * plus add two new edges.
	 */
	@Test
	public void test_3_PostAnalysisHypergeometric_overlap() throws Exception {
		PostAnalysisParameters paParams = new PostAnalysisParameters();
    	paParams.setSignature_dataSet(EnrichmentMap.DATASET1);
    	paParams.setSignature_rankFile(EnrichmentMap.DATASET1);
    	paParams.setFilter(false);
		paParams.setKnownSignature(true);
		paParams.setSignatureHub(false);
		paParams.setUniverseSize(11445);
		paParams.setSignature_Hypergeom_Cutoff(0.25);
		paParams.setSignatureGMTFileName(PATH + "PA_top8_middle8_bottom8.gmt");
		
		paParams.setSignature_rankTest(FilterMetric.HYPERGEOM);
		
		runPostAnalysis(paParams);
		// Assert that post-analysis created the new nodes correctly
		
		Map<String,CyNode> nodes = getNodes(emNetwork);
	   	assertEquals(5, nodes.size());
	   	assertTrue(nodes.containsKey("PA_TOP8_MIDDLE8_BOTTOM8"));
	   	
	   	Map<String,CyEdge> edges = getEdges(emNetwork);
	   	assertEquals(10, edges.size());
	   	
	   	CyEdge edge1 = edges.get("PA_TOP8_MIDDLE8_BOTTOM8 (sig) TOP8_PLUS100");
	   	assertNotNull(edge1);
	   	assertEquals(1.40E-6,  emNetwork.getRow(edge1).get("EM1_Overlap_Mann_Whit_pVal", Double.class), 0.001);
	   	assertEquals(4.21E-11, emNetwork.getRow(edge1).get("EM1_Overlap_Hypergeom_pVal", Double.class), 0.001);
	   	
	   	CyEdge edge2 = edges.get("PA_TOP8_MIDDLE8_BOTTOM8 (sig) BOTTOM8_PLUS100");
	   	assertNotNull(edge2);
	   	assertEquals(1.40E-6,  emNetwork.getRow(edge2).get("EM1_Overlap_Mann_Whit_pVal", Double.class), 0.001);
	   	assertEquals(4.21E-11, emNetwork.getRow(edge2).get("EM1_Overlap_Hypergeom_pVal", Double.class), 0.001);
	   	
	   	CyEdge edge3 = edges.get("PA_TOP8_MIDDLE8_BOTTOM8 (sig) MIDDLE8_PLUS100");
	   	assertNotNull(edge3);
		assertNull(emNetwork.getRow(edge3).get("EM1_Overlap_Mann_Whit_pVal", Double.class));
	   	assertEquals(4.21E-11, emNetwork.getRow(edge3).get("EM1_Overlap_Hypergeom_pVal", Double.class), 0.001);
	   	
	   	CyEdge edge4 = edges.get("PA_TOP8_MIDDLE8_BOTTOM8 (sig) TOP1_PLUS100");
	   	assertNotNull(edge4);
	   	assertNull(emNetwork.getRow(edge4).get("EM1_Overlap_Mann_Whit_pVal", Double.class));
	   	assertEquals(0.19, emNetwork.getRow(edge4).get("EM1_Overlap_Hypergeom_pVal", Double.class), 0.01);
	}

}
