package org.baderlab.csplugins.enrichmentmap.task;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.baderlab.csplugins.enrichmentmap.model.DataSetFiles;
import org.baderlab.csplugins.enrichmentmap.model.EMCreationParameters;
import org.baderlab.csplugins.enrichmentmap.model.EMCreationParameters.EdgeStrategy;
import org.baderlab.csplugins.enrichmentmap.model.EMCreationParameters.SimilarityMetric;
import org.baderlab.csplugins.enrichmentmap.model.EMDataSet;
import org.baderlab.csplugins.enrichmentmap.model.EMDataSet.Method;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMapManager;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentResultFilterParams.NESFilter;
import org.baderlab.csplugins.enrichmentmap.model.GeneExpression;
import org.baderlab.csplugins.enrichmentmap.resolver.DataSetParameters;
import org.jukito.JukitoRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.common.collect.ImmutableSet;

@RunWith(JukitoRunner.class)
public class GprofilerDummyExpressionTest extends BaseNetworkTest {

	private static final String PATH = "src/test/resources/org/baderlab/csplugins/enrichmentmap/task/gprofiler/";
	
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
		files.setGMTFileName(PATH + "Supplementary_Table5_hsapiens.pathways.NAME.gmt");
		files.setEnrichmentFileName1(PATH + "Supplementary_Table4_gprofiler_results.txt");

		EMCreationParameters params = new EMCreationParameters("Gprofiler", 1.0, 0.1, NESFilter.ALL,
				Optional.empty(), false, SimilarityMetric.JACCARD, 0.1, 0.1, EdgeStrategy.AUTOMATIC);

		Map<Long, EnrichmentMap> maps = emManager.getAllEnrichmentMaps();
		assertEquals(0, maps.size());

		buildEnrichmentMap(params, new DataSetParameters("gprofiler", Method.Generic, files));

		maps = emManager.getAllEnrichmentMaps();
		assertEquals(1, maps.size());

		EnrichmentMap map = emManager.getAllEnrichmentMaps().values().iterator().next();
		assertEquals(1, map.getDataSetCount());
	}

	@After
	public void tearDown(EnrichmentMapManager emManager) {
		emManager.reset();
	}

	
	@Test
	public void testGprofilerDummyExpressions(EnrichmentMapManager emManager) {
		EnrichmentMap map = emManager.getAllEnrichmentMaps().values().iterator().next();
		EMDataSet dataSet = map.getDataSet("gprofiler");
		Map<Integer,GeneExpression> expressions = dataSet.getExpressionSets().getExpressionMatrix();
		
		Set<String> reac5655302gmt = ImmutableSet.copyOf(new String[] {"FGF2","ERLIN2","PIK3CA","GAB1","FGFR1OP2","FGF9","FGF8","ZMYM2","GAB2","NRAS","FRS2","KRAS","FGF17","FGF5","STAT1","BAG4","HRAS","STAT5B","STAT5A","FGF1","FGF23","PIK3R1","MYO18A","GRB2","FGF4","TRIM24","CPSF6","SOS1","STAT3","FGFR1","CNTRL","FGF20","FGF6","BCR","PLCG1","LRRFIP1","CUX1","FGFR1OP"});
		Set<String> reac5655302enr = ImmutableSet.copyOf(new String[] {"PIK3CA","KRAS","PIK3R1","NRAS"});
		
		// test all the genes in the gmt are loaded
		assertEquals(reac5655302gmt.size(), dataSet.getGeneSetGenes().size());
		
		// all the genes from the enr file should have dummy expressions
		for(String gene : reac5655302enr) {
			GeneExpression expression = expressions.get(map.getHashFromGene(gene));
			assertNotNull(expression);
			float[] vals = expression.getExpression();
			assertEquals(1, vals.length);
			assertEquals(CreateDummyExpressionTask.DEFAULT_VAL, vals[0], 0.0f);
		}
		
		// all the genes from the gmt that are not in the enr should not have expressions
		for(String gene : reac5655302gmt) {
			if(!reac5655302enr.contains(gene)) {
				Integer geneKey = map.getHashFromGene(gene);
				assertNotNull(geneKey);
				GeneExpression expression = expressions.get(geneKey);
				assertNull(expression);
			}
		}
	}
	
}
