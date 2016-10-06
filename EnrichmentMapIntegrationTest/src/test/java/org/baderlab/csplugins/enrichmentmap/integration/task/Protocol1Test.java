package org.baderlab.csplugins.enrichmentmap.integration.task;

import static org.junit.Assert.assertTrue;

import java.io.File;

import javax.inject.Inject;

import org.baderlab.csplugins.enrichmentmap.AfterInjectionModule;
import org.baderlab.csplugins.enrichmentmap.ApplicationModule;
import org.baderlab.csplugins.enrichmentmap.CytoscapeServiceModule;
import org.baderlab.csplugins.enrichmentmap.commands.BuildEnrichmentMapTuneableTask;
import org.baderlab.csplugins.enrichmentmap.integration.BaseIntegrationTest;
import org.baderlab.csplugins.enrichmentmap.integration.SerialTestTaskManager;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMapParameters;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.TaskIterator;
import org.junit.Test;
import org.ops4j.peaberry.osgi.OSGiModule;
import org.osgi.framework.BundleContext;

import com.google.inject.Guice;
import com.google.inject.Injector;


public class Protocol1Test extends BaseIntegrationTest {

	private static final String PATH = "/Protocol1Test/";
	
	// injected by Pax Exam
	@Inject private BundleContext bc;
	
	@Test
	public void testProtocol1() throws Exception {
		System.out.println("Protocol1Test.testProtocol1()");
		
		Injector injector = Guice.createInjector(new OSGiModule(bc), new AfterInjectionModule(), new CytoscapeServiceModule(), new ApplicationModule());
		
		BuildEnrichmentMapTuneableTask task = injector.getInstance(BuildEnrichmentMapTuneableTask.class);
		
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
