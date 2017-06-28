package org.baderlab.csplugins.enrichmentmap.integration.task;

import static org.junit.Assert.*;

import java.io.File;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

import javax.inject.Inject;

import org.baderlab.csplugins.enrichmentmap.integration.BaseIntegrationTest;
import org.baderlab.csplugins.enrichmentmap.integration.SerialTestTaskManager;
import org.baderlab.csplugins.enrichmentmap.integration.SessionFile;
import org.baderlab.csplugins.enrichmentmap.integration.TestUtils;
import org.baderlab.csplugins.enrichmentmap.model.DataSetFiles;
import org.baderlab.csplugins.enrichmentmap.model.EMCreationParameters;
import org.baderlab.csplugins.enrichmentmap.model.EMCreationParameters.GreatFilter;
import org.baderlab.csplugins.enrichmentmap.model.EMCreationParameters.SimilarityMetric;
import org.baderlab.csplugins.enrichmentmap.model.EMDataSet;
import org.baderlab.csplugins.enrichmentmap.model.EMDataSet.Method;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMapManager;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentResult;
import org.baderlab.csplugins.enrichmentmap.model.GSEAResult;
import org.baderlab.csplugins.enrichmentmap.model.GeneExpression;
import org.baderlab.csplugins.enrichmentmap.model.GeneExpressionMatrix;
import org.baderlab.csplugins.enrichmentmap.model.GeneSet;
import org.baderlab.csplugins.enrichmentmap.model.Rank;
import org.baderlab.csplugins.enrichmentmap.model.Ranking;
import org.baderlab.csplugins.enrichmentmap.model.SetOfEnrichmentResults;
import org.baderlab.csplugins.enrichmentmap.model.io.SessionModelIO;
import org.baderlab.csplugins.enrichmentmap.style.EMStyleBuilder;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.task.read.OpenSessionTaskFactory;
import org.cytoscape.work.TaskIterator;
import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.runners.MethodSorters;

import com.google.inject.Injector;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class LegacySessionLoadTest extends BaseIntegrationTest {

	private static final String PATH = "/LegacySessionLoadTest/";

	@Rule public TestName testName = new TestName();
	
	@Inject private OpenSessionTaskFactory openSessionTF;
	@Inject private CyNetworkManager networkManager;
	@Inject private Injector injector;
	
	@Before
	public void loadSessionFile() throws Exception {
		java.lang.reflect.Method testMethod = getClass().getMethod(testName.getMethodName());
		SessionFile annotation = testMethod.getAnnotation(SessionFile.class);
		String fileName = annotation.value();
		assertNotNull(fileName);
		assertNotNull(openSessionTF);
		File sessionFile = TestUtils.createTempFile(PATH, fileName);
		TaskIterator tasks = openSessionTF.createTaskIterator(sessionFile);
		SerialTestTaskManager taskManager = new SerialTestTaskManager();
		taskManager.execute(tasks);
	}
	
	@After
	public void resetManager() {
		// Don't save the model on shutdown, we want to do that in a test
		EnrichmentMapManager emManager = injector.getInstance(EnrichmentMapManager.class);
		emManager.reset();
	}
	
	
	private EnrichmentMap getEnrichmentMap() {
		EnrichmentMapManager emManager = injector.getInstance(EnrichmentMapManager.class);
		Map<Long, EnrichmentMap> maps = emManager.getAllEnrichmentMaps();
		assertEquals(1, maps.size());
		EnrichmentMap map = maps.values().iterator().next();
		return map;
	}
	
	private static void assertEndsWith(String expected, String ending) {
		String message = "Expected '" + expected + "' to end with '" + ending +"'";
		assertTrue(message, expected.endsWith(ending));
	}
	
	
	@Test
	@SessionFile("em_session_2.2.cys")
	public void test_1_LoadedLegacyData() throws Exception {
		EnrichmentMap map = getEnrichmentMap();
		assertEquals("EM1_Enrichment Map", map.getName());
		
		CyNetwork network = networkManager.getNetwork(map.getNetworkID());
		assertNotNull(network);
		
		assertEquals(1, map.getDataSetCount());
		assertEquals(14067, map.getNumberOfGenes());
		assertEquals(14067, map.getAllGenes().size());
		
		// Number of edges: 3339 - that's how many geneset similarity objects there should be!!!
		
		CyTable edgeTable = network.getDefaultEdgeTable();
		
		assertEquals(3339, edgeTable.getRowCount()); 
		
		EMCreationParameters params = map.getParams();
		String prefix = params.getAttributePrefix();
		assertEquals("EM1_", prefix);
		assertEquals(0.5, params.getCombinedConstant(), 0.0);
		assertFalse(params.isEMgmt());
		assertEquals("Geneset_Overlap", params.getEnrichmentEdgeType());
		assertTrue(params.isFDR());
		assertEquals(GreatFilter.HYPER, params.getGreatFilter());
		assertEquals(0.005, params.getPvalue(), 0.0);
		assertEquals(1.0, params.getPvalueMin(), 0.0);
		assertEquals(0.1, params.getQvalue(), 0.0);
		assertEquals(1.0, params.getQvalueMin(), 0.0);
		assertEquals(0.5, params.getSimilarityCutoff(), 0.0);
		assertEquals(SimilarityMetric.OVERLAP, params.getSimilarityMetric());
//		assertFalse(params.isDistinctExpressionSets());
		
		String geneset1 = "RESOLUTION OF SISTER CHROMATID COHESION%REACTOME%REACT_150425.2";
		String geneset2 = "CHROMOSOME, CENTROMERIC REGION%GO%GO:0000775";
		Collection<CyRow> rows = edgeTable.getMatchingRows(CyNetwork.NAME, geneset1 + " (Geneset_Overlap) " + geneset2);
		assertEquals(1, rows.size());
		CyRow row = rows.iterator().next();
		assertEquals("Geneset_Overlap", row.get(CyEdge.INTERACTION, String.class));
		assertEquals(0.6097560975609756, EMStyleBuilder.Columns.EDGE_SIMILARITY_COEFF.get(row, prefix), 0.0);
		
		EMDataSet dataset = map.getDataSet("Dataset 1");
		assertNotNull(dataset);
		assertSame(map, dataset.getMap());
		assertEquals(Method.GSEA, dataset.getMethod());
		assertEquals(12653, dataset.getDataSetGenes().size());
		assertEquals(389, dataset.getGeneSetsOfInterest().getGeneSets().size());
//		assertEquals(17259, dataset.getSetofgenesets().getGenesets().size()); // MKTODO why? what is this used for
		assertEndsWith(dataset.getSetOfGeneSets().getFilename(), "Human_GO_AllPathways_no_GO_iea_April_15_2013_symbol.gmt");
		for(long suid : dataset.getNodeSuids()) {
			assertNotNull(network.getNode(suid));
		}
		
		GeneSet geneset = dataset.getGeneSetsOfInterest().getGeneSets().get("NCRNA PROCESSING%GO%GO:0034470");
		assertEquals(88, geneset.getGenes().size());
		assertEquals("NCRNA PROCESSING%GO%GO:0034470", geneset.getName());
		assertEquals("ncRNA processing", geneset.getDescription());
		assertEquals(Optional.of("GO"), geneset.getSource());
		
		SetOfEnrichmentResults enrichments = dataset.getEnrichments();
		assertEquals(4756, enrichments.getEnrichments().size());
		assertEndsWith(enrichments.getFilename1(), "gsea_report_for_ES12_1473194913081.xls");
		assertEndsWith(enrichments.getFilename2(), "gsea_report_for_NT12_1473194913081.xls");
		assertEquals("ES12", enrichments.getPhenotype1());
		assertEquals("NT12", enrichments.getPhenotype2());
		
		EnrichmentResult result = enrichments.getEnrichments().get("RIBONUCLEOSIDE TRIPHOSPHATE BIOSYNTHETIC PROCESS%GO%GO:0009201");
		assertTrue(result instanceof GSEAResult);
		GSEAResult gseaResult = (GSEAResult) result;
		assertEquals("RIBONUCLEOSIDE TRIPHOSPHATE BIOSYNTHETIC PROCESS%GO%GO:0009201", gseaResult.getName());
		assertEquals(0.42844063, gseaResult.getES(), 0.0);
		assertEquals(0.45225498, gseaResult.getFdrqvalue(), 0.0);
		assertEquals(1.0, gseaResult.getFwerqvalue(), 0.0);
		assertEquals(23, gseaResult.getGsSize());
		assertEquals(1.1938541, gseaResult.getNES(), 0.0);
		assertEquals(0.2457786, gseaResult.getPvalue(), 0.0);
		assertEquals(4689, gseaResult.getRankAtMax());
		assertEquals(Optional.of("GO"), gseaResult.getSource());

		GeneExpressionMatrix expressions = dataset.getExpressionSets();
		assertEquals(20326, expressions.getExpressionUniverse());
		assertEquals(3.686190609, expressions.getClosesttoZero(), 0.0);
//		assertEndsWith(expressions.getFilename(), "MCF7_ExprMx_v2_names.gct");
		assertEquals(15380.42388, expressions.getMaxExpression(), 0.0);
		assertEquals(3.686190609, expressions.getMinExpression(), 0.0);
		assertEquals(20, expressions.getNumConditions());
		
		assertEquals(12653, expressions.getExpressionMatrix().size());
		
		GeneExpression expression = expressions.getExpressionMatrix().get(0);
		assertEquals("MOCOS", expression.getName());
		assertEquals("MOCOS (molybdenum cofactor sulfurase)", expression.getDescription());
		assertEquals(18, expression.getExpression().length);
		
		Ranking ranking = expressions.getRanks().get("GSEARanking");
		assertEquals(12653, ranking.getAllRanks().size());
		assertEquals(12653, ranking.getRanking().size());
		
		Rank rank = ranking.getRanking().get(0);
		assertEquals("MOCOS", rank.getName());
		assertEquals(1238, rank.getRank().intValue());
		assertEquals(0.54488367, rank.getScore(), 0.0);
		
		DataSetFiles files = dataset.getDataSetFiles();
		assertEndsWith(files.getClassFile(), "ES_NT.cls");
		assertEndsWith(files.getEnrichmentFileName1(), "gsea_report_for_ES12_1473194913081.xls");
		assertEndsWith(files.getEnrichmentFileName2(), "gsea_report_for_NT12_1473194913081.xls");
//		assertEndsWith(files.getExpressionFileName(), "MCF7_ExprMx_v2_names.gct");
		assertEndsWith(files.getGMTFileName(), "Human_GO_AllPathways_no_GO_iea_April_15_2013_symbol.gmt");
		assertEndsWith(files.getGseaHtmlReportFile(), "estrogen_treatment_12hr_gsea_enrichment_results.Gsea.1473194913081/index.html");
		assertEndsWith(files.getRankedFile(), "ranked_gene_list_ES12_versus_NT12_1473194913081.xls");
		assertEquals("ES12", files.getPhenotype1());
		assertEquals("NT12", files.getPhenotype2());
	}
	
	
	@Test
	@SessionFile("em_session_2.2_twodataset.cys")
	public void test_2_LoadTwoDataSetLegacySession() {
		EnrichmentMap map = getEnrichmentMap();
		assertEquals(2, map.getDataSetCount());
		assertEquals(11445, map.getAllGenes().size());
		
		CyNetwork network = networkManager.getNetwork(map.getNetworkID());
		Map<String, CyEdge> edges = TestUtils.getEdges(network);
		
		assertEquals(12, edges.size());
		assertTrue(edges.containsKey("BOTTOM8_PLUS100 (Geneset_Overlap_set2) MIDDLE8_PLUS100"));
		assertTrue(edges.containsKey("BOTTOM8_PLUS100 (Geneset_Overlap_set2) TOP8_PLUS100"));
		assertTrue(edges.containsKey("MIDDLE8_PLUS100 (Geneset_Overlap_set2) TOP8_PLUS100"));
		assertTrue(edges.containsKey("MIDDLE8_PLUS100 (Geneset_Overlap_set2) TOP1_PLUS100"));
		assertTrue(edges.containsKey("TOP8_PLUS100 (Geneset_Overlap_set2) TOP1_PLUS100"));
		assertTrue(edges.containsKey("TOP8_PLUS100 (Geneset_Overlap_set1) TOP1_PLUS100"));
		assertTrue(edges.containsKey("BOTTOM8_PLUS100 (Geneset_Overlap_set2) TOP1_PLUS100"));
		assertTrue(edges.containsKey("BOTTOM8_PLUS100 (Geneset_Overlap_set1) TOP1_PLUS100"));
		assertTrue(edges.containsKey("BOTTOM8_PLUS100 (Geneset_Overlap_set1) MIDDLE8_PLUS100"));
		assertTrue(edges.containsKey("MIDDLE8_PLUS100 (Geneset_Overlap_set1) TOP1_PLUS100"));
		assertTrue(edges.containsKey("BOTTOM8_PLUS100 (Geneset_Overlap_set1) TOP8_PLUS100"));
		assertTrue(edges.containsKey("MIDDLE8_PLUS100 (Geneset_Overlap_set1) TOP8_PLUS100"));
	}
	
	
	@Test
	@SessionFile("em_session_2.2_twodataset_pa.cys")
	public void test_2_LoadTwoDataSetWithPostAnalysisLegacySession() {
		EnrichmentMap map = getEnrichmentMap();
		assertEquals(2, map.getDataSetCount());
		assertEquals(11445, map.getAllGenes().size());
		
		CyNetwork network = networkManager.getNetwork(map.getNetworkID());
		Map<String, CyEdge> edges = TestUtils.getEdges(network);
		
		assertEquals(15, edges.size());
		assertTrue(edges.containsKey("PA_TOP8_MIDDLE8_BOTTOM8 (sig_set1) BOTTOM8_PLUS100"));
		assertTrue(edges.containsKey("PA_TOP8_MIDDLE8_BOTTOM8 (sig_set1) TOP8_PLUS100"));
		assertTrue(edges.containsKey("PA_TOP8_MIDDLE8_BOTTOM8 (sig_set1) MIDDLE8_PLUS100"));
	}
	
	
	@Ignore
	@SessionFile("em_session_2.2.cys")
	public void testSavingSession() throws Exception {
		EnrichmentMapManager emManager = injector.getInstance(EnrichmentMapManager.class);
		Map<Long, EnrichmentMap> maps = emManager.getAllEnrichmentMaps();
		assertEquals(1, maps.size());
		
		SessionModelIO listener = injector.getInstance(SessionModelIO.class);
		listener.saveModel();
	}
	

}
