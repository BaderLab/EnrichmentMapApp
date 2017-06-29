package org.baderlab.csplugins.enrichmentmap.model;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

import org.baderlab.csplugins.enrichmentmap.model.EMCreationParameters.EdgeStrategy;
import org.baderlab.csplugins.enrichmentmap.model.EMCreationParameters.SimilarityMetric;
import org.baderlab.csplugins.enrichmentmap.model.EMDataSet.Method;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentResultFilterParams.NESFilter;
import org.baderlab.csplugins.enrichmentmap.model.io.ModelSerializer;
import org.baderlab.csplugins.enrichmentmap.resolver.DataSetParameters;
import org.baderlab.csplugins.enrichmentmap.task.BaseNetworkTest;
import org.jukito.JukitoRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

@RunWith(JukitoRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ExpressionSharingTest extends BaseNetworkTest {

	private static final String PATH = "src/test/resources/org/baderlab/csplugins/enrichmentmap/task/EMandPA/";
	private static final String EXPRESSION_KEY = PATH + "FakeExpression.txt";
	
	// All subclasses of BaseNetworkTest must do this
	public static class TestModule extends BaseNetworkTest.TestModule {
		@Override
		protected void configureTest() {
			super.configureTest();
		}
	}
	
	
	@Before
	public void setUp(EnrichmentMapManager emManager) {
		DataSetFiles dataset1files = new DataSetFiles();
		dataset1files.setGMTFileName(PATH + "gene_sets.gmt");  
		dataset1files.setExpressionFileName(EXPRESSION_KEY);
		dataset1files.setEnrichmentFileName1(PATH + "fakeEnrichments.txt");
		dataset1files.setRankedFile(PATH + "FakeRank.rnk");  
		
		DataSetFiles dataset2files = new DataSetFiles();
		dataset2files.setGMTFileName(PATH + "gene_sets.gmt");  
		dataset2files.setExpressionFileName(EXPRESSION_KEY);
		dataset2files.setEnrichmentFileName1(PATH + "fakeEnrichments.txt");
		dataset2files.setRankedFile(PATH + "FakeRank2.rnk");  
		
		EMCreationParameters params = 
			new EMCreationParameters("ExpressionSharing_", 0.1, 0.1, NESFilter.ALL, Optional.empty(), 
					SimilarityMetric.JACCARD, 0.1, 0.1, EdgeStrategy.AUTOMATIC);
		
		Map<Long, EnrichmentMap> maps = emManager.getAllEnrichmentMaps();
	    assertEquals(0, maps.size());
	    
	    buildEnrichmentMap(params, 
	    	new DataSetParameters(LegacySupport.DATASET1, Method.Generic, dataset1files),
	    	new DataSetParameters(LegacySupport.DATASET2, Method.Generic, dataset2files)
	    );
	    
	    maps = emManager.getAllEnrichmentMaps();
	    assertEquals(1, maps.size());
	    
	    EnrichmentMap map = emManager.getAllEnrichmentMaps().values().iterator().next();
	    assertEquals(2, map.getDataSetCount());
	}
	
	@After
	public void tearDown(EnrichmentMapManager emManager) {
		emManager.reset();
	}
	
	/**
	 * Test that there is only a single expression matrix shared by the two datasets.
	 */
	@Test
	public void testSharedExpressionSets(EnrichmentMapManager emManager) {
		EnrichmentMap map = emManager.getAllEnrichmentMaps().values().iterator().next();
	    assertExpressionSetsSame(map);
	    
	    EMDataSet ds1 = map.getDataSet(LegacySupport.DATASET1);
	    EMDataSet ds2 = map.getDataSet(LegacySupport.DATASET2);
	    
	    assertFalse(ds1.getDataSetGenes().isEmpty());
	    assertFalse(ds2.getDataSetGenes().isEmpty());
	    
	    // Make sure the Ranks are separate
	    double[] s1 = ds1.getRanks().get(LegacySupport.DATASET1).getScores();
	    double[] s2 = ds2.getRanks().get(LegacySupport.DATASET2).getScores();
	    assertFalse(Arrays.equals(s1, s2));
	}

	/**
	 * Test that after serialization/deserialization there is only a single 
	 * expression matrix shared by the two datasets.
	 */
	@Test
	public void testSharedExpressionSetsSerialization(EnrichmentMapManager emManager) {
		EnrichmentMap map = emManager.getAllEnrichmentMaps().values().iterator().next();
	    assertNotNull(map);
	    
	    // If we serialize, then deserialize, we should get back what we started with
	    String json = ModelSerializer.serialize(map);
	    EnrichmentMap roundTripEM = ModelSerializer.deserialize(json);
	    
	    assertExpressionSetsSame(roundTripEM);
	}
	
	
	private void assertExpressionSetsSame(EnrichmentMap map) {
		assertNotNull(map);
	    
	    GeneExpressionMatrix matrix = map.getExpressionMatrix(EXPRESSION_KEY);
	    assertNotNull(matrix);
	    
	    EMDataSet ds1 = map.getDataSet(LegacySupport.DATASET1);
	    EMDataSet ds2 = map.getDataSet(LegacySupport.DATASET2);
	    
	    GeneExpressionMatrix m1 = ds1.getExpressionSets();
	    GeneExpressionMatrix m2 = ds2.getExpressionSets();
	    
	    assertSame(matrix, m1);
	    assertSame(matrix, m2);
	    assertSame(m1, m2); // yeah I know this line isn't necessary, I'm paranoid ok
	}
	
}


