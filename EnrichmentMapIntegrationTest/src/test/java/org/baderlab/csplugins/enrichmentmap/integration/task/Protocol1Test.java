package org.baderlab.csplugins.enrichmentmap.integration.task;

import static org.junit.Assert.assertTrue;

import java.io.File;

import javax.inject.Inject;

import org.baderlab.csplugins.enrichmentmap.EnrichmentMapParameters;
import org.baderlab.csplugins.enrichmentmap.commands.BuildEnrichmentMapTuneableTask;
import org.baderlab.csplugins.enrichmentmap.integration.BaseIntegrationTest;
import org.baderlab.csplugins.enrichmentmap.integration.SerialTestTaskManager;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.io.util.StreamUtil;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyTableFactory;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.session.CySessionManager;
import org.cytoscape.task.edit.MapTableToNetworkTablesTaskFactory;
import org.cytoscape.task.read.LoadNetworkFileTaskFactory;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.cytoscape.work.TaskIterator;
import org.junit.Test;


public class Protocol1Test extends BaseIntegrationTest {

	private static final String PATH = "/Protocol1Test/";
	
	@Inject private CySessionManager sessionManager;
	@Inject private StreamUtil streamUtil;
	@Inject private CyApplicationManager applicationManager;
	@Inject private CyNetworkManager networkManager;
	@Inject private CyNetworkViewManager networkViewManager;
	@Inject private CyNetworkViewFactory networkViewFactory;
	@Inject private CyNetworkFactory networkFactory;
	@Inject private CyTableFactory tableFactory;
	@Inject private CyTableManager tableManager;
	@Inject private VisualMappingManager visualMappingManager;
	@Inject private VisualStyleFactory visualStyleFactory;
	@Inject private VisualMappingFunctionFactory vmfFactoryContinuous;
	@Inject private VisualMappingFunctionFactory vmfFactoryDiscrete;
	@Inject private VisualMappingFunctionFactory vmfFactoryPassthrough;
	@Inject private CyLayoutAlgorithmManager layoutManager;
	@Inject private MapTableToNetworkTablesTaskFactory mapTableToNetworkTable;
	
	@Inject private LoadNetworkFileTaskFactory loadNetworkFileTaskFactory;
	
	
	@Test
	public void testProtocol1() throws Exception {
		BuildEnrichmentMapTuneableTask task = new BuildEnrichmentMapTuneableTask(sessionManager,
				streamUtil, applicationManager, null /*swingApplication*/, networkManager, networkViewManager,
				networkViewFactory, networkFactory, tableFactory, tableManager, visualMappingManager,
				visualStyleFactory, vmfFactoryContinuous, vmfFactoryDiscrete, vmfFactoryPassthrough, layoutManager,
				mapTableToNetworkTable);
		
		File enrichmentFile = createTempFile(PATH, "gprofiler_results_mesenonly_ordered_computedinR.txt");
		assertTrue(enrichmentFile.exists());
		
		task.analysisType.setSelectedValue(EnrichmentMapParameters.method_generic);
		task.coeffecients.setSelectedValue(EnrichmentMapParameters.SM_JACCARD);
		task.enrichmentsDataset1 = enrichmentFile;
		task.pvalue = 1.0;
		task.qvalue = 0.00001;
		task.similaritycutoff = 0.25;
		
		SerialTestTaskManager taskManager = new SerialTestTaskManager();
		taskManager.execute(new TaskIterator(task));
		
		// Assert the network was created
	   	CyNetwork generatedNetwork = assertAndGetOnlyNetwork();
		CyNetwork expectedNetwork  = importNetworkFromFile(PATH, "protocol_1_expected.xgmml");
		
		assertNetworksEqual(expectedNetwork, generatedNetwork);
	}
}
