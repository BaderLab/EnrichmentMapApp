package org.baderlab.csplugins.enrichmentmap.task;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.baderlab.csplugins.enrichmentmap.CytoscapeServiceModule.Continuous;
import org.baderlab.csplugins.enrichmentmap.PropertyManager;
import org.baderlab.csplugins.enrichmentmap.TestUtils;
import org.baderlab.csplugins.enrichmentmap.model.DataSetFiles;
import org.baderlab.csplugins.enrichmentmap.model.EMCreationParameters;
import org.baderlab.csplugins.enrichmentmap.model.EMCreationParameters.EdgeStrategy;
import org.baderlab.csplugins.enrichmentmap.model.EMCreationParameters.SimilarityMetric;
import org.baderlab.csplugins.enrichmentmap.model.EMDataSet.Method;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentResultFilterParams.NESFilter;
import org.baderlab.csplugins.enrichmentmap.model.LegacySupport;
import org.baderlab.csplugins.enrichmentmap.model.PostAnalysisFilterParameters;
import org.baderlab.csplugins.enrichmentmap.model.PostAnalysisFilterType;
import org.baderlab.csplugins.enrichmentmap.model.PostAnalysisParameters;
import org.baderlab.csplugins.enrichmentmap.model.PostAnalysisParameters.UniverseType;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
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
    	builder.setAnalysisType(PostAnalysisParameters.AnalysisType.KNOWN_SIGNATURE);
    	builder.setUniverseType(UniverseType.USER_DEFINED);
		builder.setUserDefinedUniverseSize(11445);
		builder.setSignatureGMTFileName(PATH + "PA_top8_middle8_bottom8.gmt");
		builder.setAttributePrefix("EM1_");
		return builder;
	}
	
	@Test
	public void _setup(PropertyManager pm, CyApplicationManager applicationManager, CyNetworkManager networkManager) {
		EMCreationParameters params = new EMCreationParameters("EM1_", 
				PropertyManager.P_VALUE.defaultValue, PropertyManager.Q_VALUE.defaultValue, NESFilter.ALL, Optional.empty(), 
				SimilarityMetric.JACCARD, LegacySupport.jaccardCutOff_default, LegacySupport.combinedConstant_default, EdgeStrategy.AUTOMATIC);
		
		DataSetFiles dataset1files = new DataSetFiles();
		dataset1files.setGMTFileName(PATH + "gene_sets.gmt");  
		dataset1files.setExpressionFileName(PATH + "FakeExpression.txt");
		dataset1files.setEnrichmentFileName1(PATH + "fakeEnrichments.txt");
		dataset1files.setRankedFile(PATH + "FakeRank.rnk");  
		
	    buildEnrichmentMap(params, dataset1files, Method.Generic, LegacySupport.DATASET1);
	    
	    // Assert the network is as expected
	   	Set<CyNetwork> networks = networkManager.getNetworkSet();
	   	assertEquals(1, networks.size());
	   	emNetwork = networks.iterator().next();
	}
	
	@Test
	public void test_1_FilterType_Number(@Continuous VisualMappingFunctionFactory cmFactory) throws Exception {
		mockContinuousMappingFactory(cmFactory);
		
		PostAnalysisParameters.Builder builder = getBuilder();
		
		PostAnalysisFilterParameters rankTest = new PostAnalysisFilterParameters(PostAnalysisFilterType.NUMBER, 5);
		builder.setRankTestParameters(rankTest);
		
		runPostAnalysis(emNetwork, builder, LegacySupport.DATASET1);
	   	
	   	Map<String,CyEdge> edges = TestUtils.getEdges(emNetwork);
	   	assertEquals(9, edges.size());
	   	
	   	CyEdge edge = edges.get("PA_TOP8_MIDDLE8_BOTTOM8 (sig_Dataset 1) MIDDLE8_PLUS100");
	   	assertNotNull(edge);
	   	assertEquals(8, emNetwork.getRow(edge).get("EM1_k_intersection", Integer.class).intValue());
	   	assertEquals(PostAnalysisFilterType.NUMBER.toString(), emNetwork.getRow(edge).get("EM1_Overlap_cutoff", String.class));
	   	
	   	edge = edges.get("PA_TOP8_MIDDLE8_BOTTOM8 (sig_Dataset 1) BOTTOM8_PLUS100");
	   	assertNotNull(edge);
	   	assertEquals(8, emNetwork.getRow(edge).get("EM1_k_intersection", Integer.class).intValue());
	   	assertEquals(PostAnalysisFilterType.NUMBER.toString(), emNetwork.getRow(edge).get("EM1_Overlap_cutoff", String.class));
	   	
	   	edge = edges.get("PA_TOP8_MIDDLE8_BOTTOM8 (sig_Dataset 1) TOP8_PLUS100");
	   	assertNotNull(edge);
	   	assertEquals(8, emNetwork.getRow(edge).get("EM1_k_intersection", Integer.class).intValue());
	   	assertEquals(PostAnalysisFilterType.NUMBER.toString(), emNetwork.getRow(edge).get("EM1_Overlap_cutoff", String.class));
	   	
	   	edge = edges.get("PA_TOP8_MIDDLE8_BOTTOM8 (sig_Dataset 1) TOP1_PLUS100");
	   	assertNull(edge);
	}
	
	@Test
	public void test_2_FilterType_Percent(@Continuous VisualMappingFunctionFactory cmFactory) throws Exception {
		mockContinuousMappingFactory(cmFactory);
		
		PostAnalysisParameters.Builder builder = getBuilder();
		
		PostAnalysisFilterParameters rankTest = new PostAnalysisFilterParameters(PostAnalysisFilterType.PERCENT, 7);
		builder.setRankTestParameters(rankTest);
		
		runPostAnalysis(emNetwork, builder, LegacySupport.DATASET1);
		
		assertEquals(12, emNetwork.getEdgeCount());
		
	   	Map<String,CyEdge> edges = TestUtils.getSignatureEdges(emNetwork, "EM1_", "PA_top8_middle8_bottom8(1)");
	   	assertEquals(3, edges.size());
	   	
	   	CyEdge edge = edges.get("PA_TOP8_MIDDLE8_BOTTOM8 (sig_Dataset 1) MIDDLE8_PLUS100");
	   	assertNotNull(edge);
	   	assertEquals(8, emNetwork.getRow(edge).get("EM1_k_intersection", Integer.class).intValue());
	   	assertEquals(PostAnalysisFilterType.PERCENT.toString(), emNetwork.getRow(edge).get("EM1_Overlap_cutoff", String.class));
	   	
	   	edge = edges.get("PA_TOP8_MIDDLE8_BOTTOM8 (sig_Dataset 1) BOTTOM8_PLUS100");
	   	assertNotNull(edge);
	   	assertEquals(8, emNetwork.getRow(edge).get("EM1_k_intersection", Integer.class).intValue());
	   	assertEquals(PostAnalysisFilterType.PERCENT.toString(), emNetwork.getRow(edge).get("EM1_Overlap_cutoff", String.class));
	   	
	   	edge = edges.get("PA_TOP8_MIDDLE8_BOTTOM8 (sig_Dataset 1) TOP8_PLUS100");
	   	assertNotNull(edge);
	   	assertEquals(8, emNetwork.getRow(edge).get("EM1_k_intersection", Integer.class).intValue());
	   	assertEquals(PostAnalysisFilterType.PERCENT.toString(), emNetwork.getRow(edge).get("EM1_Overlap_cutoff", String.class));
	   	
	   	edge = edges.get("PA_TOP8_MIDDLE8_BOTTOM8 (sig_Dataset 1) TOP1_PLUS100");
	   	assertNull(edge);
	}
	
	@Test
	public void test_3_FilterType_Specific(@Continuous VisualMappingFunctionFactory cmFactory) throws Exception {
		mockContinuousMappingFactory(cmFactory);
		
		PostAnalysisParameters.Builder builder = getBuilder();
		
		PostAnalysisFilterParameters rankTest = new PostAnalysisFilterParameters(PostAnalysisFilterType.SPECIFIC, 25);
		builder.setRankTestParameters(rankTest);
		
		runPostAnalysis(emNetwork, builder, LegacySupport.DATASET1);
		
		assertEquals(15, emNetwork.getEdgeCount());
		
		Map<String,CyEdge> edges = TestUtils.getSignatureEdges(emNetwork, "EM1_", "PA_top8_middle8_bottom8(2)");
	   	assertEquals(3, edges.size());
	   	
	   	CyEdge edge = edges.get("PA_TOP8_MIDDLE8_BOTTOM8 (sig_Dataset 1) MIDDLE8_PLUS100");
	   	assertNotNull(edge);
	   	assertEquals(8, emNetwork.getRow(edge).get("EM1_k_intersection", Integer.class).intValue());
	   	assertEquals(PostAnalysisFilterType.SPECIFIC.toString(), emNetwork.getRow(edge).get("EM1_Overlap_cutoff", String.class));
	   	
	   	edge = edges.get("PA_TOP8_MIDDLE8_BOTTOM8 (sig_Dataset 1) BOTTOM8_PLUS100");
	   	assertNotNull(edge);
	   	assertEquals(8, emNetwork.getRow(edge).get("EM1_k_intersection", Integer.class).intValue());
	   	assertEquals(PostAnalysisFilterType.SPECIFIC.toString(), emNetwork.getRow(edge).get("EM1_Overlap_cutoff", String.class));
	   	
	   	edge = edges.get("PA_TOP8_MIDDLE8_BOTTOM8 (sig_Dataset 1) TOP8_PLUS100");
	   	assertNotNull(edge);
	   	assertEquals(8, emNetwork.getRow(edge).get("EM1_k_intersection", Integer.class).intValue());
	   	assertEquals(PostAnalysisFilterType.SPECIFIC.toString(), emNetwork.getRow(edge).get("EM1_Overlap_cutoff", String.class));
	   	
	   	edge = edges.get("PA_TOP8_MIDDLE8_BOTTOM8 (sig_Dataset 1) TOP1_PLUS100");
	   	assertNull(edge);
	}
}
