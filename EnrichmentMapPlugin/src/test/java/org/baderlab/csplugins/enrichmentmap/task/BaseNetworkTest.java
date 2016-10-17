package org.baderlab.csplugins.enrichmentmap.task;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.baderlab.csplugins.enrichmentmap.ApplicationModule;
import org.baderlab.csplugins.enrichmentmap.ApplicationModule.Edges;
import org.baderlab.csplugins.enrichmentmap.ApplicationModule.Nodes;
import org.baderlab.csplugins.enrichmentmap.LogSilenceRule;
import org.baderlab.csplugins.enrichmentmap.PropertyManager;
import org.baderlab.csplugins.enrichmentmap.SerialTestTaskManager;
import org.baderlab.csplugins.enrichmentmap.actions.LoadSignatureSetsActionListener;
import org.baderlab.csplugins.enrichmentmap.heatmap.HeatMapPanel;
import org.baderlab.csplugins.enrichmentmap.model.EMCreationParameters;
import org.baderlab.csplugins.enrichmentmap.model.DataSet;
import org.baderlab.csplugins.enrichmentmap.model.DataSetFiles;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMapManager;
import org.baderlab.csplugins.enrichmentmap.model.LegacySupport;
import org.baderlab.csplugins.enrichmentmap.model.PostAnalysisParameters;
import org.baderlab.csplugins.enrichmentmap.view.ParametersPanel;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNetworkTableManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTableFactory;
import org.cytoscape.model.NetworkTestSupport;
import org.cytoscape.model.TableTestSupport;
import org.cytoscape.session.CySession;
import org.cytoscape.session.CySessionManager;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.work.FinishStatus;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.TaskObserver;
import org.jukito.JukitoModule;
import org.jukito.JukitoRunner;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mockito;

import com.google.inject.Inject;
import com.google.inject.util.Providers;

@RunWith(JukitoRunner.class)
public abstract class BaseNetworkTest {

	@Rule public TestRule logSilenceRule = new LogSilenceRule();
	
	public static class TestModule extends JukitoModule {
		@Override
		protected void configureTest() {
			NetworkTestSupport networkTestSupport = new NetworkTestSupport();
			TableTestSupport tableTestSupport = new TableTestSupport();
			bind(CyNetworkFactory.class).toInstance(networkTestSupport.getNetworkFactory());
			bind(CyNetworkTableManager.class).toInstance(networkTestSupport.getNetworkTableManager());
			bind(CyNetworkManager.class).toInstance(networkTestSupport.getNetworkManager());
			bind(CyTableFactory.class).toInstance(tableTestSupport.getTableFactory());
			
			// I guess Jukito doesn't respect the @Singleton annotation :(
			bind(EnrichmentMapManager.class).asEagerSingleton();
			
			// Bind all AssistedInjection factories
			install(ApplicationModule.createFactoryModule());
						
			// MKTODO get rid of these fields in EnrichmentMapManager
			bind(ParametersPanel.class).toProvider(Providers.of(null));
			bind(HeatMapPanel.class).annotatedWith(Edges.class).toProvider(Providers.of(null));
			bind(HeatMapPanel.class).annotatedWith(Nodes.class).toProvider(Providers.of(null));
		}
	}
    
    @Inject private CyApplicationManager applicationManager;
    @Inject private EnrichmentMapManager emManager;
    @Inject private PropertyManager propertyManager;
    
    @Inject private EnrichmentMapBuildMapTaskFactory.Factory enrichmentMapBuildMapTaskFactoryFactory;
    @Inject private LoadSignatureSetsActionListener.Factory  loadSignatureSetsActionListenerFactory;
    @Inject private BuildDiseaseSignatureTask.Factory buildDiseaseSignatureTaskFactory;
    
    
	@Before
	public void before(CySessionManager sessionManager) {
		CySession emptySession = new CySession.Builder().build();
		when(sessionManager.getCurrentSession()).thenReturn(emptySession);
	}
	
	
	protected void buildEnrichmentMap(EMCreationParameters params, DataSetFiles datasetFiles, String datasetName) {
		String prefix = params.getAttributePrefix();
		String name = prefix + LegacySupport.EM_NAME;
		
		EnrichmentMap map = new EnrichmentMap(name, params);
		DataSet dataset = new DataSet(map, datasetName, datasetFiles);
		map.addDataSet(datasetName, dataset);
		
	   	EnrichmentMapBuildMapTaskFactory buildmap = enrichmentMapBuildMapTaskFactoryFactory.create(map);
	    
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
	
	protected void runPostAnalysis(CyNetwork emNetwork, PostAnalysisParameters.Builder builder) throws Exception {
		// Set up mocks
		when(applicationManager.getCurrentNetwork()).thenReturn(emNetwork);
		CyNetworkView networkViewMock = mock(CyNetworkView.class);
		when(applicationManager.getCurrentNetworkView()).thenReturn(networkViewMock);
		@SuppressWarnings("unchecked")
		View<CyNode> nodeViewMock = Mockito.mock(View.class);
		
		when(networkViewMock.getNodeView(Matchers.<CyNode>anyObject())).thenReturn(nodeViewMock);
		when(nodeViewMock.getVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION)).thenReturn(Double.valueOf(0.0));
		
		EnrichmentMap map = emManager.getEnrichmentMap(emNetwork.getSUID());
		assertNotNull(map);
		
		// Load the gene-sets from the file
		SerialTestTaskManager testTaskManager = new SerialTestTaskManager();
		LoadSignatureSetsActionListener loader = loadSignatureSetsActionListenerFactory.create(builder.getSignatureGMTFileName(), new FilterMetric.None());
		loader.setTaskManager(testTaskManager);
		
		loader.setGeneSetCallback(builder::setSignatureGenesets);
		loader.setLoadedSignatureSetsCallback(builder::addSelectedSignatureSetNames);

		loader.actionPerformed(null);
		
		PostAnalysisParameters paParams = builder.build();
		
		// Run post-analysis
		BuildDiseaseSignatureTask signatureTask = buildDiseaseSignatureTaskFactory.create(map, paParams);
		signatureTask.run(mock(TaskMonitor.class));
	}
}
