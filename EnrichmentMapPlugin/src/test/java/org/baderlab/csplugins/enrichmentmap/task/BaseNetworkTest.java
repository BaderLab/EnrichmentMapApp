package org.baderlab.csplugins.enrichmentmap.task;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.baderlab.csplugins.enrichmentmap.ApplicationModule;
import org.baderlab.csplugins.enrichmentmap.CytoscapeServiceModule.Continuous;
import org.baderlab.csplugins.enrichmentmap.CytoscapeServiceModule.Discrete;
import org.baderlab.csplugins.enrichmentmap.CytoscapeServiceModule.Passthrough;
import org.baderlab.csplugins.enrichmentmap.LogSilenceRule;
import org.baderlab.csplugins.enrichmentmap.SerialTestTaskManager;
import org.baderlab.csplugins.enrichmentmap.TestUtils;
import org.baderlab.csplugins.enrichmentmap.model.DataSetFiles;
import org.baderlab.csplugins.enrichmentmap.model.DataSetParameters;
import org.baderlab.csplugins.enrichmentmap.model.EMCreationParameters;
import org.baderlab.csplugins.enrichmentmap.model.EMCreationParameters.EdgeStrategy;
import org.baderlab.csplugins.enrichmentmap.model.EMCreationParameters.SimilarityMetric;
import org.baderlab.csplugins.enrichmentmap.model.EMDataSet;
import org.baderlab.csplugins.enrichmentmap.model.EMDataSet.Method;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMapManager;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentResultFilterParams.NESFilter;
import org.baderlab.csplugins.enrichmentmap.model.LegacySupport;
import org.baderlab.csplugins.enrichmentmap.model.PostAnalysisParameters;
import org.baderlab.csplugins.enrichmentmap.model.SetOfGeneSets;
import org.baderlab.csplugins.enrichmentmap.parsers.GMTFileReaderTask;
import org.baderlab.csplugins.enrichmentmap.task.InitializeGenesetsOfInterestTask.MissingGenesetStrategy;
import org.baderlab.csplugins.enrichmentmap.task.postanalysis.PASimilarityTaskParallel;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNetworkTableManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTableFactory;
import org.cytoscape.model.NetworkTestSupport;
import org.cytoscape.model.TableTestSupport;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.session.CySession;
import org.cytoscape.session.CySessionManager;
import org.cytoscape.util.swing.OpenBrowser;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.RenderingEngineManager;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.presentation.property.values.CyColumnIdentifierFactory;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.mappings.ContinuousMapping;
import org.cytoscape.work.FinishStatus;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskIterator;
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

@RunWith(JukitoRunner.class)
public abstract class BaseNetworkTest {

	@Rule public TestRule logSilenceRule = new LogSilenceRule();
	
	private CyServiceRegistrar serviceRegistrar = TestUtils.mockServiceRegistrar();
	
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
		}
	}

	// If you get a Guice error when initializing your test that a cytoscape service 
	// can't be bound then just add it here, even if its not actually used here.
	// OR You can explicitly bind the service in the TestModule above.
	@Inject private RenderingEngineManager renderingEngineManager;
	@Inject private CyColumnIdentifierFactory columnIdentifierFactory;
	@Inject private CySwingApplication cySwingApplication;
	@Inject private @Continuous  VisualMappingFunctionFactory cmFactory;
	@Inject private @Discrete    VisualMappingFunctionFactory dmFactory;
	@Inject private @Passthrough VisualMappingFunctionFactory pmFactory;
	
    @Inject private CyApplicationManager applicationManager;
    @Inject private CyNetworkViewManager networkViewManager;
    @Inject private OpenBrowser openBrowser;
    @Inject private EnrichmentMapManager emManager;
    
    @Inject private CreateEnrichmentMapTaskFactory.Factory masterMapTaskFactoryFactory;
    @Inject private PASimilarityTaskParallel.Factory buildDiseaseSignatureTaskFactory;
    
    
	@Before
	public void before(CySessionManager sessionManager) {
		CySession emptySession = new CySession.Builder().build();
		when(sessionManager.getCurrentSession()).thenReturn(emptySession);
	}
	
	
	protected void buildEnrichmentMap(EMCreationParameters params, DataSetParameters ...datasets) { //, DataSetFiles datasetFiles, Method method, String datasetName) {
		List<DataSetParameters> dataSets = Arrays.asList(datasets);
		CreateEnrichmentMapTaskFactory taskFactory = masterMapTaskFactoryFactory.create(params, dataSets);
		TaskIterator taskIterator = taskFactory.createTaskIterator(MissingGenesetStrategy.FAIL_IMMEDIATELY);
		
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
	   	testTaskManager.ignoreTask(CreateEMViewTask.class);
	   	testTaskManager.execute(taskIterator, observer);
	}
	
	protected void runPostAnalysis(CyNetwork emNetwork, PostAnalysisParameters.Builder builder, String gmtFilePath, String dataSetName) throws Exception {
		// Set up mocks
		when(applicationManager.getCurrentNetwork()).thenReturn(emNetwork);
		CyNetworkView networkViewMock = mock(CyNetworkView.class);
		when(applicationManager.getCurrentNetworkView()).thenReturn(networkViewMock);
		when(networkViewManager.getNetworkViews(emNetwork)).thenReturn(Arrays.asList(networkViewMock));
		@SuppressWarnings("unchecked")
		View<CyNode> nodeViewMock = Mockito.mock(View.class);
		
		when(networkViewMock.getNodeView(Matchers.<CyNode>anyObject())).thenReturn(nodeViewMock);
		when(nodeViewMock.getVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION)).thenReturn(Double.valueOf(0.0));
		
		EnrichmentMap map = emManager.getEnrichmentMap(emNetwork.getSUID());
		assertNotNull(map);
		
		// Load the gene-sets from the file
		SerialTestTaskManager testTaskManager = new SerialTestTaskManager();
		File file = new File(gmtFilePath);
		
		SetOfGeneSets setOfGeneSets = new SetOfGeneSets();
		GMTFileReaderTask gmtTask = new GMTFileReaderTask(map, file.toString(), setOfGeneSets);
		gmtTask.run(null);
//		PAMostSimilarTaskParallel filterTask = new PAMostSimilarTaskParallel(map, setOfGeneSets, builder.getRankTestParameters());
//		filterTask.run(null);
//		List<String> selectedGeneSetNames = filterTask.getPassingGeneSetNames();
//		
//		System.out.println("runPostAnalysis");
//		System.out.println(setOfGeneSets.size());
//		System.out.println(selectedGeneSetNames.size());
		
		builder.setLoadedGMTGeneSets(setOfGeneSets);
		builder.addSelectedGeneSetNames(setOfGeneSets.getGeneSetNames());
		
		PostAnalysisParameters paParams = builder.build();
		
		// Run post-analysis
		EMDataSet dataSet = map.getDataSet(dataSetName);
		PASimilarityTaskParallel signatureTask = buildDiseaseSignatureTaskFactory.create(paParams, map, Arrays.asList(dataSet));
		testTaskManager = new SerialTestTaskManager();
		testTaskManager.execute(new TaskIterator(signatureTask));
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes", "deprecation" })
	protected void mockContinuousMappingFactory(VisualMappingFunctionFactory cmFactory) {
		ContinuousMapping<Double, Double> cm = mock(ContinuousMapping.class);
		when(cm.getMappedValue(Matchers.<CyRow>anyObject())).thenReturn(-1.0);
		when(cmFactory.createVisualMappingFunction(
				Matchers.<String>anyObject(),
				Matchers.<Class>anyObject(),
				Matchers.<VisualProperty>anyObject())
		).thenReturn(cm);
	}
	
	
	/**
	 * Create a basic enrichment map network with 4 nodes for simple tests.
	 */
	public EnrichmentMap createBasicNetwork() {
		String PATH = "src/test/resources/org/baderlab/csplugins/enrichmentmap/task/EMandPA/";
		DataSetFiles dataset1files = new DataSetFiles();
		dataset1files.setGMTFileName(PATH + "gene_sets.gmt");  
		dataset1files.setExpressionFileName(PATH + "FakeExpression.txt");
		dataset1files.setEnrichmentFileName1(PATH + "fakeEnrichments.txt");
		dataset1files.setRankedFile(PATH + "FakeRank.rnk");  
		
		EMCreationParameters params = 
			new EMCreationParameters("Basic_", null, 0.1, 0.1, NESFilter.ALL, Optional.empty(), true, 
					SimilarityMetric.JACCARD, 0.1, 0.1, EdgeStrategy.AUTOMATIC);
		
		Map<Long, EnrichmentMap> maps = emManager.getAllEnrichmentMaps();
	    assertEquals(0, maps.size());
	    
	    buildEnrichmentMap(params, new DataSetParameters(LegacySupport.DATASET1, Method.Generic, dataset1files));
	    
	    maps = emManager.getAllEnrichmentMaps();
	    assertEquals(1, maps.size());
	    
	    return emManager.getAllEnrichmentMaps().values().iterator().next();
	}
}
