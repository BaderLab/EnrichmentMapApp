package org.baderlab.csplugins.enrichmentmap.model;

import java.util.Set;

import org.baderlab.csplugins.enrichmentmap.model.EMCreationParameters.SimilarityMetric;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkTableManager;

import com.google.inject.Inject;

/**
 * Temporary repository for methods that work with legacy EnrichmentMaps.
 * Eventually all this code should be deprecated and/or removed.
 */
public class LegacySupport {
	
	public static final String EM_NAME = "Enrichment Map";
	
	public static final String DATASET1 = "Dataset 1";
	public static final String DATASET2 = "Dataset 2";
	
	public static final double jaccardCutOff_default = 0.25;
	public static final double overlapCutOff_default = 0.5;
	public static final double combinedCutoff_default = 0.375;
	public static final double combinedConstant_default = 0.5;
	public static final SimilarityMetric similarityMetric_default = SimilarityMetric.OVERLAP;
	
	
	@Inject private EnrichmentMapManager emManager;
	@Inject private CyNetworkTableManager networkTableManager;

	
	public String getNextStylePrefix() {
		Set<CyNetwork> networks = networkTableManager.getNetworkSet();

		if(networks == null || networks.isEmpty()) {
			return "EM1_";
		} else {
			int maxPrefix = 0;
			
			for(CyNetwork network : networks) {
				Long suid = network.getSUID();
				if(emManager.isEnrichmentMap(suid)) {
					EnrichmentMap map = emManager.getEnrichmentMap(suid);
					int prefix = getPrefix(map);
					if(prefix > 0) {
						maxPrefix = Math.max(maxPrefix, prefix);
					}
				}
			}
			
			maxPrefix++;
			String prefix = "EM" + maxPrefix + "_";
			System.out.println("Style prefix: '" + prefix + "'");
			return prefix;
		}
	}
	
	private static int getPrefix(EnrichmentMap map) {
		EMCreationParameters params = map.getParams();
		String prefix = params.getStylePrefix();
		prefix = prefix.replace("EM", "");
		prefix = prefix.replace("_", "");
		try {
			return Integer.parseInt(prefix);
		} catch(NumberFormatException e) { 
			return -1;
		}
	}
	
	public static boolean isLegacyEnrichmentMap(EnrichmentMap map) {
		if(map == null)
			return false;
		return  
				(map.getDataSetCount() == 1 && map.getDataSet(DATASET1) != null)
			||  (map.getDataSetCount() == 2 && map.getDataSet(DATASET1) != null && map.getDataSet(DATASET2) != null);
	}
	
	public static boolean isLegacyTwoDatasets(EnrichmentMap map) {
		if(map == null)
			return false;
		return map.getDataSetCount() == 2 && map.getDataSet(DATASET1) != null && map.getDataSet(DATASET2) != null;
	}
}
