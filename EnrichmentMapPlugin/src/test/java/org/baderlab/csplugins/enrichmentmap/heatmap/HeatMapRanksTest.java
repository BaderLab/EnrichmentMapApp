package org.baderlab.csplugins.enrichmentmap.heatmap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.baderlab.csplugins.enrichmentmap.model.DataSetFiles;
import org.baderlab.csplugins.enrichmentmap.model.EMCreationParameters;
import org.baderlab.csplugins.enrichmentmap.model.EMCreationParameters.SimilarityMetric;
import org.baderlab.csplugins.enrichmentmap.model.EMDataSet;
import org.baderlab.csplugins.enrichmentmap.model.EMDataSet.Method;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMapManager;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentResultFilterParams.NESFilter;
import org.baderlab.csplugins.enrichmentmap.model.GeneSet;
import org.baderlab.csplugins.enrichmentmap.model.LegacySupport;
import org.baderlab.csplugins.enrichmentmap.model.Ranking;
import org.baderlab.csplugins.enrichmentmap.task.BaseNetworkTest;
import org.baderlab.csplugins.enrichmentmap.view.heatmap.GSEALeadingEdgeRankingOption;
import org.baderlab.csplugins.enrichmentmap.view.heatmap.RankingOption;
import org.baderlab.csplugins.enrichmentmap.view.heatmap.table.RankValue;
import org.jukito.JukitoRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.common.collect.HashBiMap;
import com.google.common.io.Files;

@RunWith(JukitoRunner.class)
public class HeatMapRanksTest extends BaseNetworkTest {
	
	private static final String PATH = "src/test/resources/org/baderlab/csplugins/enrichmentmap/task/tutorial/";
	
	// All subclasses of BaseNetworkTest must do this
	public static class TestModule extends BaseNetworkTest.TestModule {
		@Override
		protected void configureTest() {
			super.configureTest();
		}
	}
	
	
	@Before
	public void setUp(EnrichmentMapManager emManager) {
		DataSetFiles files = new DataSetFiles();
		files.setGMTFileName(PATH + "Human_GO_AllPathways_no_GO_iea_April_15_2013_symbol.gmt");  
		files.setExpressionFileName(PATH + "MCF7_ExprMx_v2_names.gct");
		files.setEnrichmentFileName1(PATH + "gsea_report_for_ES12_1473194913081.xls");
		files.setEnrichmentFileName2(PATH + "gsea_report_for_NT12_1473194913081.xls");
		files.setRankedFile(PATH + "ranked_gene_list_ES12_versus_NT12_1473194913081.xls");  
		files.setClassFile(PATH + "ES_NT.cls");
		
		EMCreationParameters params = new EMCreationParameters("HeatMapRanks_", 0.005, 0.1, NESFilter.ALL, Optional.empty(), SimilarityMetric.OVERLAP, 0.5, 0.5);
		
		Map<Long, EnrichmentMap> maps = emManager.getAllEnrichmentMaps();
	    assertEquals(0, maps.size());
	    
	    buildEnrichmentMap(params, files, Method.GSEA, LegacySupport.DATASET1);
	    
	    maps = emManager.getAllEnrichmentMaps();
	    assertEquals(1, maps.size());
	}
	
	@After
	public void tearDown(EnrichmentMapManager emManager) {
		emManager.reset();
	}
	
	
	private static List<Integer> getGeneOrderFromFile(EnrichmentMap map, String path) throws Exception {
		List<String> geneNames = Files.readLines(new File(path), Charset.forName("UTF8"));
		return geneNames.stream().map(map::getHashFromGene).collect(Collectors.toList());
	}
	
	@Test
	public void testLeadingEdge(EnrichmentMapManager emManager) throws Exception {
		final String geneSetName = "ENVELOPE%GO%GO:0031975";
		final int leadingEdgeSize = 170;
		
		// Sanity test
		EnrichmentMap map = emManager.getAllEnrichmentMaps().values().iterator().next();
		EMDataSet dataset = map.getDataSet(LegacySupport.DATASET1);
		GeneSet gs = dataset.getGeneSetsOfInterest().getGeneSets().get(geneSetName);
		assertNotNull(gs);
		
		// Run the ranking
		RankingOption rankingOption = new GSEALeadingEdgeRankingOption(dataset, geneSetName, Ranking.GSEARanking);
		Map<Integer,RankValue> ranks = rankingOption.computeRanking(gs.getGenes()).get();
		assertEquals(454, ranks.size());
		
		// Convert to useful collections
		Map<RankValue,Integer> rankToGeneId = HashBiMap.create(ranks).inverse();
		List<RankValue> sortedRanks = ranks.values().stream().sorted().collect(Collectors.toList());
		
		// Test leading edge
		for(int i = 0; i < sortedRanks.size(); i++) {
			RankValue v = sortedRanks.get(i);
			assertTrue(v.isSignificant() == i < leadingEdgeSize);
		}
		
		// Test genes are the same
		List<Integer> expectedGeneOrder = getGeneOrderFromFile(map, PATH + "gene_order_leading_edge.txt");
		List<Integer> actualGeneOrder = sortedRanks.stream().map(rankToGeneId::get).collect(Collectors.toList());
		assertEquals(expectedGeneOrder, actualGeneOrder);
	}
	
	
}
