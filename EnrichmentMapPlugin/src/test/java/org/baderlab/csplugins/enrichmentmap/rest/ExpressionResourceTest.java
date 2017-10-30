package org.baderlab.csplugins.enrichmentmap.rest;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.ws.rs.core.Response;

import org.baderlab.csplugins.enrichmentmap.TestUtils;
import org.baderlab.csplugins.enrichmentmap.model.DataSetFiles;
import org.baderlab.csplugins.enrichmentmap.model.EMCreationParameters;
import org.baderlab.csplugins.enrichmentmap.model.EMCreationParameters.EdgeStrategy;
import org.baderlab.csplugins.enrichmentmap.model.EMCreationParameters.SimilarityMetric;
import org.baderlab.csplugins.enrichmentmap.model.EMDataSet.Method;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMapManager;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentResultFilterParams.NESFilter;
import org.baderlab.csplugins.enrichmentmap.model.LegacySupport;
import org.baderlab.csplugins.enrichmentmap.resolver.DataSetParameters;
import org.baderlab.csplugins.enrichmentmap.task.BaseNetworkTest;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.jukito.JukitoRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;


@RunWith(JukitoRunner.class)
public class ExpressionResourceTest extends BaseNetworkTest {

	private static final String PATH = "src/test/resources/org/baderlab/csplugins/enrichmentmap/task/EMandPA/";
	
	private EnrichmentMap map;
	
	@Before
	public void setUp(EnrichmentMapManager emManager) {
		DataSetFiles dataset1files = new DataSetFiles();
		dataset1files.setGMTFileName(PATH + "gene_sets.gmt");  
		dataset1files.setExpressionFileName(PATH + "FakeExpression.txt");
		dataset1files.setEnrichmentFileName1(PATH + "fakeEnrichments.txt");
		dataset1files.setRankedFile(PATH + "FakeRank.rnk");  
		
		EMCreationParameters params = 
			new EMCreationParameters("ModelSerializer_", 0.1, 0.1, NESFilter.ALL, Optional.empty(), true, 
					SimilarityMetric.JACCARD, 0.1, 0.1, EdgeStrategy.AUTOMATIC);
		
		Map<Long, EnrichmentMap> maps = emManager.getAllEnrichmentMaps();
	    assertEquals(0, maps.size());
	    
	    buildEnrichmentMap(params, new DataSetParameters(LegacySupport.DATASET1, Method.Generic, dataset1files));
	    
	    maps = emManager.getAllEnrichmentMaps();
	    assertEquals(1, maps.size());
	    
	    map = emManager.getAllEnrichmentMaps().values().iterator().next();
	}
	
	@After
	public void tearDown(EnrichmentMapManager emManager) {
		emManager.reset();
	}
	
	
	@Test
	public void testExpressionDataForNetwork_BadRequest(EnrichmentMapResource resource) {
		Response response = resource.getExpressionDataForNetwork("-1");
		assertNotNull(response);
		assertEquals(404, response.getStatus());
	}
	
	@Test
	public void testExpressionDataForNode_BadNetworkRequest(EnrichmentMapResource resource) {
		Response response = resource.getExpressionDataForNode("-1", 99);
		assertNotNull(response);
		assertEquals(404, response.getStatus());
	}
	
	@Test
	public void testExpressionDataForNode_BadNodeRequest(EnrichmentMapResource resource, CyNetworkManager networkManager) {
		CyNetwork network = networkManager.getNetwork(map.getNetworkID());
		assertNotNull(network);
		Response response = resource.getExpressionDataForNode(String.valueOf(map.getNetworkID()), 99);
		assertNotNull(response);
		assertEquals(404, response.getStatus());
	}
	
	@Test
	public void testModelData_BadRequest(EnrichmentMapResource resource) {
		Response response = resource.getModelData("-1");
		assertNotNull(response);
		assertEquals(404, response.getStatus());
	}
	
	
	@Test
	public void testExpressionDataForNetwork(EnrichmentMapResource resource, CyNetworkManager networkManager) {
		CyNetwork network = networkManager.getNetwork(map.getNetworkID());
		assertNotNull(network);
		
		Response response = resource.getExpressionDataForNetwork(String.valueOf(map.getNetworkID()));
		assertNotNull(response);
		assertEquals(200, response.getStatus());
		
		ExpressionDataResponse expressionData = (ExpressionDataResponse) response.getEntity();
		List<DataSetExpressionResponse> dataSetList = expressionData.getDataSetExpressionList();
		assertEquals(1, dataSetList.size());
		
		DataSetExpressionResponse dataSetResponse = dataSetList.get(0);
		assertEquals(Arrays.asList("Exp1A","Exp1B","Exp1C","Exp2A","Exp2B","Exp2C"), dataSetResponse.getColumnNames());
		assertEquals(6, dataSetResponse.getNumConditions());
		assertEquals(Arrays.asList(LegacySupport.DATASET1), dataSetResponse.getDataSets());
		assertEquals(869, dataSetResponse.getExpressionUniverse());
		
		List<GeneExpressionResponse> expressions = dataSetResponse.getExpressions();
		assertEquals(818, expressions.size());
		
		Map<String,List<GeneExpressionResponse>> grouped = expressions.stream().collect(Collectors.groupingBy(GeneExpressionResponse::getGeneName));
		GeneExpressionResponse geneExpression = grouped.get("A10").get(0);
		assertEquals("A10", geneExpression.getGeneName());
		float[] values = geneExpression.getValues();
		assertArrayEquals(new float[] {900,880,900,630,600,630}, values, 0.0f);
	}
	
	
	@Test
	public void testExpressionDataForNode(EnrichmentMapResource resource, CyNetworkManager networkManager) {
		CyNetwork network = networkManager.getNetwork(map.getNetworkID());
		assertNotNull(network);
		
		Map<String,CyNode> nodes = TestUtils.getNodes(network);
		CyNode node = nodes.get("TOP1_PLUS100");
		
		Response response = resource.getExpressionDataForNode(String.valueOf(map.getNetworkID()), node.getSUID());
		assertNotNull(response);
		assertEquals(200, response.getStatus());
		
		ExpressionDataResponse expressionData = (ExpressionDataResponse) response.getEntity();
		List<DataSetExpressionResponse> dataSetList = expressionData.getDataSetExpressionList();
		assertEquals(1, dataSetList.size());
		
		DataSetExpressionResponse dataSetResponse = dataSetList.get(0);
		assertEquals(Arrays.asList("Exp1A","Exp1B","Exp1C","Exp2A","Exp2B","Exp2C"), dataSetResponse.getColumnNames());
		assertEquals(6, dataSetResponse.getNumConditions());
		assertEquals(Arrays.asList(LegacySupport.DATASET1), dataSetResponse.getDataSets());
		assertEquals(869, dataSetResponse.getExpressionUniverse());
		
		List<GeneExpressionResponse> expressions = dataSetResponse.getExpressions();
		assertEquals(105, expressions.size());
	}
	
}
