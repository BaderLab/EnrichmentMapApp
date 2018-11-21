package org.baderlab.csplugins.enrichmentmap.integration.task;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Set;

import javax.inject.Inject;

import org.baderlab.csplugins.enrichmentmap.commands.EMBuildCommandTask;
import org.baderlab.csplugins.enrichmentmap.integration.BaseIntegrationTest;
import org.baderlab.csplugins.enrichmentmap.integration.SerialTestTaskManager;
import org.baderlab.csplugins.enrichmentmap.integration.TestUtils;
import org.baderlab.csplugins.enrichmentmap.model.EMCreationParameters.EdgeStrategy;
import org.baderlab.csplugins.enrichmentmap.model.EMCreationParameters.SimilarityMetric;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMapParameters;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.TaskIterator;
import org.junit.Test;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Injector;


public class Protocol1Test extends BaseIntegrationTest {

	private static final String PATH = "/Protocol1Test/";
	
	@Inject private Injector injector;
	
	@Test
	public void testProtocol1() throws Exception {
		EMBuildCommandTask task = injector.getInstance(EMBuildCommandTask.class);
		
		File enrichmentFile = TestUtils.createTempFile(PATH, "gprofiler_results_mesenonly_ordered_computedinR.txt");
		assertTrue(enrichmentFile.exists());
		
		task.analysisType.setSelectedValue(EnrichmentMapParameters.method_generic);
		task.filterArgs.coefficients.setSelectedValue(SimilarityMetric.JACCARD.name());
//		task.filterArgs.coeffecients.setSelectedValue(SimilarityMetric.JACCARD.name());
		task.enrichmentsDataset1 = enrichmentFile;
		task.filterArgs.pvalue = 1.0;
		task.filterArgs.qvalue = 0.00001;
		task.filterArgs.similaritycutoff = 0.25;
		task.filterArgs.edgeStrategy.setSelectedValue(EdgeStrategy.COMPOUND.name());
		task.filterArgs.attributePrefix = "EM1_";
		
		SerialTestTaskManager taskManager = new SerialTestTaskManager();
		taskManager.execute(new TaskIterator(task));
		
		// Assert the network was created
	   	CyNetwork generatedNetwork = assertAndGetOnlyNetwork();
		CyNetwork expectedNetwork  = importNetworkFromFile(PATH, "protocol_1_expected.xgmml");
		
		Set<String> columnsToIgnore = ImmutableSet.of("EM1_ENR_SET", "EM1_ENRICHMENT_SET", "EM1_Data Set");
		assertNetworksEqual(expectedNetwork, generatedNetwork, columnsToIgnore);
	}
}
