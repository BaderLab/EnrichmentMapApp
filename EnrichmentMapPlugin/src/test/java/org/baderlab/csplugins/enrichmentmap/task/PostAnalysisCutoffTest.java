package org.baderlab.csplugins.enrichmentmap.task;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Map;
import java.util.Set;

import org.baderlab.csplugins.enrichmentmap.StreamUtil;
import org.baderlab.csplugins.enrichmentmap.TestUtils;
import org.baderlab.csplugins.enrichmentmap.model.DataSetFiles;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMapParameters;
import org.baderlab.csplugins.enrichmentmap.model.FilterParameters;
import org.baderlab.csplugins.enrichmentmap.model.FilterType;
import org.baderlab.csplugins.enrichmentmap.model.PostAnalysisParameters;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.session.CySessionManager;
import org.jukito.JukitoRunner;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

/**
 * Test FilterTypes: NUMBER, PERCENT, SPECIFIC.
 * @author mkucera
 */
@RunWith(JukitoRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class PostAnalysisCutoffTest extends BaseNetworkTest {

	private static final String PATH = "src/test/resources/org/baderlab/csplugins/enrichmentmap/task/EMandPA/";
	
	public static class TestModule extends BaseNetworkTest.TestModule {
		@Override
		protected void configureTest() {
			super.configureTest();
		}
	}
	
	
	private static CyNetwork emNetwork;
	
	private PostAnalysisParameters.Builder getBuilder() {
		PostAnalysisParameters.Builder builder = new PostAnalysisParameters.Builder();
    	builder.setSignatureDataSet(EnrichmentMap.DATASET1);
    	builder.setSignatureRankFile(EnrichmentMap.DATASET1);
    	builder.setAnalysisType(PostAnalysisParameters.AnalysisType.KNOWN_SIGNATURE);
		builder.setUniverseSize(11445);
		builder.setSignatureGMTFileName(PATH + "PA_top8_middle8_bottom8.gmt");
		builder.setAttributePrefix("EM1_");
		
		return builder;
	}
	
	
	@Test
	public void _setup(CySessionManager sessionManager, CyApplicationManager applicationManager, CyNetworkManager networkManager) {
		EnrichmentMapParameters emParams = new EnrichmentMapParameters(sessionManager, new StreamUtil(), applicationManager);
		emParams.setMethod(EnrichmentMapParameters.method_generic);
		DataSetFiles dataset1files = new DataSetFiles();
		dataset1files.setGMTFileName(PATH + "gene_sets.gmt");  
		dataset1files.setExpressionFileName(PATH + "FakeExpression.txt");
		dataset1files.setEnrichmentFileName1(PATH + "fakeEnrichments.txt");
		dataset1files.setRankedFile(PATH + "FakeRank.rnk");  
		emParams.addFiles(EnrichmentMap.DATASET1, dataset1files);
		emParams.setAttributePrefix("EM1_");
		
	    buildEnrichmentMap(emParams);
	    
	    // Assert the network is as expected
	   	Set<CyNetwork> networks = networkManager.getNetworkSet();
	   	assertEquals(1, networks.size());
	   	emNetwork = networks.iterator().next();
	}
	 
	
	@Test
	public void test_1_FilterType_Number() throws Exception {
		PostAnalysisParameters.Builder builder = getBuilder();
		
		FilterParameters rankTest = new FilterParameters(FilterType.NUMBER, 5);
		builder.setRankTestParameters(rankTest);
		
		runPostAnalysis(emNetwork, builder);
	   	
	   	Map<String,CyEdge> edges = TestUtils.getEdges(emNetwork);
	   	assertEquals(9, edges.size());
	   	
	   	CyEdge edge = edges.get("PA_TOP8_MIDDLE8_BOTTOM8 (sig) MIDDLE8_PLUS100");
	   	assertNotNull(edge);
	   	assertEquals(8, emNetwork.getRow(edge).get("EM1_k_intersection", Integer.class).intValue());
	   	assertEquals(FilterType.NUMBER.toString(), emNetwork.getRow(edge).get("EM1_Overlap_cutoff", String.class));
	   	
	   	edge = edges.get("PA_TOP8_MIDDLE8_BOTTOM8 (sig) BOTTOM8_PLUS100");
	   	assertNotNull(edge);
	   	assertEquals(8, emNetwork.getRow(edge).get("EM1_k_intersection", Integer.class).intValue());
	   	assertEquals(FilterType.NUMBER.toString(), emNetwork.getRow(edge).get("EM1_Overlap_cutoff", String.class));
	   	
	   	edge = edges.get("PA_TOP8_MIDDLE8_BOTTOM8 (sig) TOP8_PLUS100");
	   	assertNotNull(edge);
	   	assertEquals(8, emNetwork.getRow(edge).get("EM1_k_intersection", Integer.class).intValue());
	   	assertEquals(FilterType.NUMBER.toString(), emNetwork.getRow(edge).get("EM1_Overlap_cutoff", String.class));
	   	
	   	edge = edges.get("PA_TOP8_MIDDLE8_BOTTOM8 (sig) TOP1_PLUS100");
	   	assertNull(edge);
	}
	
	@Test
	public void test_2_FilterType_Percent() throws Exception {
		PostAnalysisParameters.Builder builder = getBuilder();
		
		FilterParameters rankTest = new FilterParameters(FilterType.PERCENT, 7);
		builder.setRankTestParameters(rankTest);
		
		runPostAnalysis(emNetwork, builder);
		
	   	Map<String,CyEdge> edges = TestUtils.getEdges(emNetwork);
	   	assertEquals(9, edges.size());
	   	
	   	CyEdge edge = edges.get("PA_TOP8_MIDDLE8_BOTTOM8 (sig) MIDDLE8_PLUS100");
	   	assertNotNull(edge);
	   	assertEquals(8, emNetwork.getRow(edge).get("EM1_k_intersection", Integer.class).intValue());
	   	assertEquals(FilterType.PERCENT.toString(), emNetwork.getRow(edge).get("EM1_Overlap_cutoff", String.class));
	   	
	   	edge = edges.get("PA_TOP8_MIDDLE8_BOTTOM8 (sig) BOTTOM8_PLUS100");
	   	assertNotNull(edge);
	   	assertEquals(8, emNetwork.getRow(edge).get("EM1_k_intersection", Integer.class).intValue());
	   	assertEquals(FilterType.PERCENT.toString(), emNetwork.getRow(edge).get("EM1_Overlap_cutoff", String.class));
	   	
	   	edge = edges.get("PA_TOP8_MIDDLE8_BOTTOM8 (sig) TOP8_PLUS100");
	   	assertNotNull(edge);
	   	assertEquals(8, emNetwork.getRow(edge).get("EM1_k_intersection", Integer.class).intValue());
	   	assertEquals(FilterType.PERCENT.toString(), emNetwork.getRow(edge).get("EM1_Overlap_cutoff", String.class));
	   	
	   	edge = edges.get("PA_TOP8_MIDDLE8_BOTTOM8 (sig) TOP1_PLUS100");
	   	assertNull(edge);
	}
	
	@Test
	public void test_3_FilterType_Specific() throws Exception {		
		PostAnalysisParameters.Builder builder = getBuilder();
		
		FilterParameters rankTest = new FilterParameters(FilterType.SPECIFIC, 25);
		builder.setRankTestParameters(rankTest);
		
		runPostAnalysis(emNetwork, builder);
		
	   	Map<String,CyEdge> edges = TestUtils.getEdges(emNetwork);
	   	assertEquals(9, edges.size());
	   	
	   	CyEdge edge = edges.get("PA_TOP8_MIDDLE8_BOTTOM8 (sig) MIDDLE8_PLUS100");
	   	assertNotNull(edge);
	   	assertEquals(8, emNetwork.getRow(edge).get("EM1_k_intersection", Integer.class).intValue());
	   	assertEquals(FilterType.SPECIFIC.toString(), emNetwork.getRow(edge).get("EM1_Overlap_cutoff", String.class));
	   	
	   	edge = edges.get("PA_TOP8_MIDDLE8_BOTTOM8 (sig) BOTTOM8_PLUS100");
	   	assertNotNull(edge);
	   	assertEquals(8, emNetwork.getRow(edge).get("EM1_k_intersection", Integer.class).intValue());
	   	assertEquals(FilterType.SPECIFIC.toString(), emNetwork.getRow(edge).get("EM1_Overlap_cutoff", String.class));
	   	
	   	edge = edges.get("PA_TOP8_MIDDLE8_BOTTOM8 (sig) TOP8_PLUS100");
	   	assertNotNull(edge);
	   	assertEquals(8, emNetwork.getRow(edge).get("EM1_k_intersection", Integer.class).intValue());
	   	assertEquals(FilterType.SPECIFIC.toString(), emNetwork.getRow(edge).get("EM1_Overlap_cutoff", String.class));
	   	
	   	edge = edges.get("PA_TOP8_MIDDLE8_BOTTOM8 (sig) TOP1_PLUS100");
	   	assertNull(edge);
	}
}
