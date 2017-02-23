package org.baderlab.csplugins.enrichmentmap.model;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.nio.file.Paths;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;

import org.baderlab.csplugins.enrichmentmap.model.EMCreationParameters.SimilarityMetric;
import org.baderlab.csplugins.enrichmentmap.model.EMDataSet.Method;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentResultFilterParams.NESFilter;
import org.baderlab.csplugins.enrichmentmap.model.io.ModelSerializer;
import org.baderlab.csplugins.enrichmentmap.task.BaseNetworkTest;
import org.jukito.JukitoRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.common.collect.ImmutableSet;

@RunWith(JukitoRunner.class)
public class ModelSerializerTest extends BaseNetworkTest {
	
	private static final String PATH = "src/test/resources/org/baderlab/csplugins/enrichmentmap/task/EMandPA/";
	
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
		dataset1files.setExpressionFileName(PATH + "FakeExpression.txt");
		dataset1files.setEnrichmentFileName1(PATH + "fakeEnrichments.txt");
		dataset1files.setRankedFile(PATH + "FakeRank.rnk");  
		
		EMCreationParameters params = new EMCreationParameters("ModelSerializer_", 0.1, 0.1, NESFilter.ALL, Optional.empty(), SimilarityMetric.JACCARD, 0.1, 0.1);
		params.setGlobalGmtFile(Paths.get(PATH + "gene_sets.gmt"));
		
		Map<Long, EnrichmentMap> maps = emManager.getAllEnrichmentMaps();
	    assertEquals(0, maps.size());
	    
	    buildEnrichmentMap(params, dataset1files, Method.Generic, LegacySupport.DATASET1);
	    
	    maps = emManager.getAllEnrichmentMaps();
	    assertEquals(1, maps.size());
	    EnrichmentMap expectedEM = emManager.getAllEnrichmentMaps().values().iterator().next();
	    expectedEM.addSignatureDataSet(
	    		new EMSignatureDataSet("Sig1", new GeneSet("SG1", "desc1", ImmutableSet.of(1,2,3,4)))
	    );
	    expectedEM.addSignatureDataSet(
	    		new EMSignatureDataSet("Sig2", new GeneSet("SG2", "desc2", ImmutableSet.of(1,2,3,4,5,6)))
	    );
	}
	

	@Test
	public void testModelSerializer(EnrichmentMapManager emManager) {
		EnrichmentMap expectedEM = emManager.getAllEnrichmentMaps().values().iterator().next();
	    assertNotNull(expectedEM);
	    
	    // If we serialize, then deserialize, we should get back what we started with
	    String json = ModelSerializer.serialize(expectedEM);
	    System.out.println(json.length());
	    EnrichmentMap roundTripEM = ModelSerializer.deserialize(json);
	    
	    assertEnrichmentMapEquals(expectedEM, roundTripEM);
	}
	
	private static void assertEnrichmentMapEquals(EnrichmentMap expected, EnrichmentMap actual) {
		assertEquals(expected.getNetworkID(), actual.getNetworkID());
		assertEquals(expected.getNumberOfGenes(), actual.getNumberOfGenes());
		assertEMCreationParametersEquals(expected.getParams(), actual.getParams());
		assertSetOfGeneSetsEquals(expected.getGlobalGenesets(), actual.getGlobalGenesets());
		assertMapsEqual(ModelSerializerTest::assertSignatureDataSetsEquals, expected.getSignatureDataSets(), actual.getSignatureDataSets());
//		assertMapsEqual(ModelSerializerTest::assertGenesetSimilarityEquals, expected.getGenesetSimilarity(), actual.getGenesetSimilarity());
		assertMapsEqual(ModelSerializerTest::assertDataSetEquals, expected.getDataSets(), actual.getDataSets());
	}

	private static void assertEMCreationParametersEquals(EMCreationParameters expected, EMCreationParameters actual) {
		assertEquals(expected.getAttributePrefix(), actual.getAttributePrefix());
		assertEquals(expected.getPvalue(), actual.getPvalue(), 0.0);
		assertEquals(expected.getQvalue(), actual.getQvalue(), 0.0);
		assertEquals(expected.getMinExperiments(), actual.getMinExperiments());
		assertEquals(expected.getNESFilter(), actual.getNESFilter());
		assertEquals(expected.getSimilarityMetric(), actual.getSimilarityMetric());
		assertEquals(expected.getSimilarityCutoff(), actual.getSimilarityCutoff(), 0.0);
		assertEquals(expected.getCombinedConstant(), actual.getCombinedConstant(), 0.0);
		assertEquals(expected.getGreatFilter(), actual.getGreatFilter());
		assertEquals(expected.isFDR(), actual.isFDR());
		assertEquals(expected.isEMgmt(), actual.isEMgmt());
		assertEquals(expected.getQvalueMin(), actual.getQvalueMin(), 0.0);
		assertEquals(expected.getPvalueMin(), actual.getPvalueMin(), 0.0);
		assertEquals(expected.getEnrichmentEdgeType(), actual.getEnrichmentEdgeType()); // MKTODO remove this field
		assertEquals(expected.getGlobalGmtFile(), actual.getGlobalGmtFile());
	}
	
	private static void assertSignatureDataSetsEquals(EMSignatureDataSet expected, EMSignatureDataSet actual) {
		assertEquals(expected.getName(), actual.getName());
		assertGeneSetEquals(expected.getGeneSet(), actual.getGeneSet());
	}
	
	private static void assertGeneSetEquals(GeneSet expected, GeneSet actual) {
		assertEquals(expected.getName(), actual.getName());
		assertEquals(expected.getDescription(), actual.getDescription());
		assertEquals(expected.getGenes(), actual.getGenes());
		assertEquals(expected.getSource(), actual.getSource());
	}
	
//	private static void assertGenesetSimilarityEquals(GenesetSimilarity expected, GenesetSimilarity actual) {
//		assertEquals(expected.getGeneset1_Name(), actual.getGeneset1_Name());
//		assertEquals(expected.getGeneset2_Name(), actual.getGeneset2_Name());
//		assertEquals(expected.getInteractionType(), actual.getInteractionType());
//		assertEquals(expected.getSimilarity_coeffecient(), actual.getSimilarity_coeffecient(), 0.0);
//		assertEquals(expected.getHypergeom_pvalue(), actual.getHypergeom_pvalue(), 0.0);
//		assertEquals(expected.getHypergeom_N(), actual.getHypergeom_N());
//		assertEquals(expected.getHypergeom_n(), actual.getHypergeom_n());
//		assertEquals(expected.getHypergeom_k(), actual.getHypergeom_k());
//		assertEquals(expected.getHypergeom_m(), actual.getHypergeom_m());
//		assertEquals(expected.getMann_Whit_pValue_twoSided(), actual.getMann_Whit_pValue_twoSided(), 0.0);
//		assertEquals(expected.getMann_Whit_pValue_greater(), actual.getMann_Whit_pValue_greater(), 0.0);
//		assertEquals(expected.getMann_Whit_pValue_less(), actual.getMann_Whit_pValue_less(), 0.0);
//		assertEquals(expected.isMannWhitMissingRanks(), actual.isMannWhitMissingRanks());
//		assertEquals(expected.getOverlapping_genes(), actual.getOverlapping_genes());
//		assertEquals(expected.getEnrichment_set(), actual.getEnrichment_set()); // MKTODO remove this field
//	}
	
	private static void assertSetOfGeneSetsEquals(SetOfGeneSets expected, SetOfGeneSets actual) {
		assertEquals(expected.getName(), actual.getName());
		assertEquals(expected.getFilename(), actual.getFilename());
		assertMapsEqual(ModelSerializerTest::assertGeneSetEquals, expected.getGeneSets(), actual.getGeneSets());
	}
	
	private static void assertDataSetEquals(EMDataSet expected, EMDataSet actual) {
//		assertNotNull(actual.getMap()); // MKTODO GSON can't serialize circular references, need to restore parent pointer manually
		assertEquals(expected.getName(), actual.getName());
		assertEquals(expected.getMethod(), actual.getMethod());
		assertEquals(expected.isDummyExpressionData(), actual.isDummyExpressionData());
		assertDataSetFilesEquals(expected.getDataSetFiles(), actual.getDataSetFiles());
		assertSetOfGeneSetsEquals(expected.getSetOfGeneSets(), actual.getSetOfGeneSets());
		assertSetOfGeneSetsEquals(expected.getGeneSetsOfInterest(), actual.getGeneSetsOfInterest());
		assertMapsEqual(Objects::equals, expected.getNodeSuids(), actual.getNodeSuids());
		assertEquals(expected.getDataSetGenes(), actual.getDataSetGenes());
		assertSetOfEnrichmentResultsEquals(expected.getEnrichments(), actual.getEnrichments());
		assertGeneExpressionMatrixEquals(expected.getExpressionSets(), actual.getExpressionSets());
	}
	
	private static void assertDataSetFilesEquals(DataSetFiles expected, DataSetFiles actual) {
		assertEquals(expected.getGMTFileName(), actual.getGMTFileName());
		assertEquals(expected.getExpressionFileName(), actual.getExpressionFileName());
		assertEquals(expected.getEnrichmentFileName1(), actual.getEnrichmentFileName1());
		assertEquals(expected.getEnrichmentFileName2(), actual.getEnrichmentFileName2());
		assertEquals(expected.getRankedFile(), actual.getRankedFile());
		assertEquals(expected.getClassFile(), actual.getClassFile());
		assertArrayEquals(expected.getTemp_class1(), actual.getTemp_class1());
		assertEquals(expected.getPhenotype1(), actual.getPhenotype1());
		assertEquals(expected.getPhenotype2(), actual.getPhenotype2());
		assertEquals(expected.getGseaHtmlReportFile(), actual.getGseaHtmlReportFile());
	}
	
	private static void assertSetOfEnrichmentResultsEquals(SetOfEnrichmentResults expected, SetOfEnrichmentResults actual) {
		assertEquals(expected.getName(), actual.getName());
		assertEquals(expected.getFilename1(), actual.getFilename1());
		assertEquals(expected.getFilename2(), actual.getFilename2());
		assertEquals(expected.getPhenotype1(), actual.getPhenotype1());
		assertEquals(expected.getPhenotype2(), actual.getPhenotype2());
		assertMapsEqual(ModelSerializerTest::assertEnrichmentResultEquals, expected.getEnrichments(), actual.getEnrichments());
	}
	
	private static void assertEnrichmentResultEquals(EnrichmentResult expectedInter, EnrichmentResult actualInter) {
		// buildEnrichmentMap() uses GenericResult
		assertTrue(actualInter.getClass().getSimpleName(), actualInter instanceof GenericResult);
		GenericResult expected = (GenericResult) expectedInter;
		GenericResult actual = (GenericResult) actualInter;
		assertEquals(expected.getName(), actual.getName());
		assertEquals(expected.getDescription(), actual.getDescription());
		assertEquals(expected.getPvalue(), actual.getPvalue(), 0.0);
		assertEquals(expected.getGsSize(), actual.getGsSize());
		assertEquals(expected.getSource(), actual.getSource());
		assertEquals(expected.getFdrqvalue(), actual.getFdrqvalue(), 0.0);
		assertEquals(expected.getNES(), actual.getNES(), 0.0);
	}
	
	private static void assertGeneExpressionMatrixEquals(GeneExpressionMatrix expected, GeneExpressionMatrix actual) {
		assertArrayEquals(expected.getColumnNames(), actual.getColumnNames());
		assertEquals(expected.getNumConditions(), actual.getNumConditions());
		assertEquals(expected.getExpressionUniverse(), actual.getExpressionUniverse());
		assertMapsEqual(ModelSerializerTest::assertGeneExpressionEquals, expected.getExpressionMatrix(), actual.getExpressionMatrix());
		assertMapsEqual(ModelSerializerTest::assertGeneExpressionEquals, expected.getExpressionMatrix_rowNormalized(), actual.getExpressionMatrix_rowNormalized());
		assertEquals(expected.getMaxExpression(), actual.getMaxExpression(), 0.0);
		assertEquals(expected.getMinExpression(), actual.getMinExpression(), 0.0);
		assertEquals(expected.getClosesttoZero(), actual.getClosesttoZero(), 0.0);
		assertArrayEquals(expected.getPhenotypes(), actual.getPhenotypes());
		assertEquals(expected.getPhenotype1(), actual.getPhenotype1());
		assertEquals(expected.getPhenotype2(), actual.getPhenotype2());
		assertMapsEqual(ModelSerializerTest::assertRankingEquals, expected.getRanks(), actual.getRanks());
		assertEquals(expected.getFilename(), actual.getFilename());
	}
	
	private static void assertRankingEquals(Ranking expected, Ranking actual) {
		assertEquals(expected.getFilename(), actual.getFilename());
		assertArrayEquals(expected.getScores(), actual.getScores(), 0.0);
		assertMapsEqual(ModelSerializerTest::assertRankEquals, expected.getRanking(), actual.getRanking());
	}
	
	private static void assertRankEquals(Rank expected, Rank actual) {
		assertEquals(expected.getName(), actual.getName());
		assertEquals(expected.getScore(), actual.getScore());
		assertEquals(expected.getRank(), actual.getRank());
	}
	
	private static void assertGeneExpressionEquals(GeneExpression expected, GeneExpression actual) {
		assertEquals(expected.getName(), actual.getName());
		assertEquals(expected.getDescription(), actual.getDescription());
		assertArrayEquals(expected.getExpression(), actual.getExpression());
		assertArrayEquals(expected.getRow(), actual.getRow());
	}
	
	private static <K,V> void assertMapsEqual(BiConsumer<V,V> valueAsserter, Map<K,V> expected, Map<K,V> actual) {
		assertFalse(actual.isEmpty());
		assertEquals(expected.keySet(), actual.keySet());
		for(K key : expected.keySet()) {
			V expectedValue = expected.get(key);
			V actualValue = actual.get(key);
			valueAsserter.accept(expectedValue, actualValue);
		}
	}
}
