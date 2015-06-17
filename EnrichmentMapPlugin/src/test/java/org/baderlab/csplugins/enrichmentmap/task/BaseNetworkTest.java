package org.baderlab.csplugins.enrichmentmap.task;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.Map;

import org.baderlab.csplugins.enrichmentmap.EnrichmentMapManager;
import org.baderlab.csplugins.enrichmentmap.EnrichmentMapParameters;
import org.baderlab.csplugins.enrichmentmap.LogSilenceRule;
import org.baderlab.csplugins.enrichmentmap.PostAnalysisParameters;
import org.baderlab.csplugins.enrichmentmap.SerialTestTaskManager;
import org.baderlab.csplugins.enrichmentmap.StreamUtil;
import org.baderlab.csplugins.enrichmentmap.actions.LoadSignatureSetsActionListener;
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
import org.junit.Rule;
import org.junit.rules.TestRule;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public abstract class BaseNetworkTest {

	protected StreamUtil streamUtil = new StreamUtil();
	
	@Rule public TestRule logSilenceRule = new LogSilenceRule();

	protected NetworkTestSupport networkTestSupport = new NetworkTestSupport();
	protected TableTestSupport tableTestSupport = new TableTestSupport();
	
	protected CyNetworkManager networkManager = networkTestSupport.getNetworkManager();
	protected CyNetworkFactory networkFactory = networkTestSupport.getNetworkFactory();
	protected CyTableFactory tableFactory = tableTestSupport.getTableFactory();
    
    @Mock protected CyApplicationManager applicationManager;
	@Mock protected CyTableManager tableManager;
	@Mock protected CySessionManager sessionManager;
	@Mock protected CyNetworkViewManager networkViewManager;
	@Mock protected CyNetworkViewFactory networkViewFactory;
	@Mock protected VisualMappingManager visualMappingManager;
	@Mock protected VisualStyleFactory visualStyleFactory;
	@Mock protected VisualMappingFunctionFactory vmfFactoryContinuous;
	@Mock protected VisualMappingFunctionFactory vmfFactoryDiscrete;
	@Mock protected VisualMappingFunctionFactory vmfFactoryPassthrough;
	@Mock protected DialogTaskManager dialog;
	@Mock protected CyLayoutAlgorithmManager layoutManager;
	@Mock protected MapTableToNetworkTablesTaskFactory mapTableToNetworkTable;
    @Mock protected CyEventHelper eventHelper;
    @Mock protected CySwingApplication swingApplication;
    
	
	
	
	
	@Before
	public void before() {
		MockitoAnnotations.initMocks(this);
		CySession emptySession = new CySession.Builder().build();
		when(sessionManager.getCurrentSession()).thenReturn(emptySession);
	}
	
	
	protected Map<String,CyNode> getNodes(CyNetwork network) {
		Map<String,CyNode> nodes = new HashMap<>();
	   	for(CyNode node : network.getNodeList()) {
	   		nodes.put(network.getRow(node).get("name", String.class), node);
	   	}
	   	return nodes;
	}
	
	protected Map<String,CyEdge> getEdges(CyNetwork network) {
		Map<String,CyEdge> edges = new HashMap<>();
	   	for(CyEdge edge : network.getEdgeList()) {
	   		edges.put(network.getRow(edge).get("name", String.class), edge);
	   	}
	   	return edges;
	}
	
	
	protected void buildEnrichmentMap(EnrichmentMapParameters emParams) {
		EnrichmentMap map = new EnrichmentMap(emParams);
	   	EnrichmentMapBuildMapTaskFactory buildmap = new EnrichmentMapBuildMapTaskFactory(map,  
	        			applicationManager, swingApplication, networkManager, networkViewManager,
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
	
	protected void runPostAnalysis(CyNetwork emNetwork, PostAnalysisParameters paParams) throws Exception {
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
}
