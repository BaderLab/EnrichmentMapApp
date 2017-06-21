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

	
	/**
	 * The attribute prefix is based on the number of nextworks in cytoscape.
	 * make attribute prefix independent of cytoscape
	 */
	public String getNextAttributePrefix() {
		Set<CyNetwork> networks = networkTableManager.getNetworkSet();

		if(networks == null || networks.isEmpty()) {
			return "EM1_";
		}
		else {
			// how many enrichment maps are there?
			int max_prefix = 0;
			// go through all the networks, check to see if they are enrichment maps
			// if they are then calculate the max EM_# and use the max number + 1 for the current attributes
			for(CyNetwork current_network : networks) {
				Long networkId = current_network.getSUID();
				if(emManager.isEnrichmentMap(networkId)) {// fails
					EnrichmentMap tmpMap = emManager.getEnrichmentMap(networkId);
					String tmpPrefix = tmpMap.getParams().getAttributePrefix();
					tmpPrefix = tmpPrefix.replace("EM", "");
					tmpPrefix = tmpPrefix.replace("_", "");
					int tmpNum = Integer.parseInt(tmpPrefix);
					if(tmpNum > max_prefix) {
						max_prefix = tmpNum;
					}
				}
			}
			return "EM" + (max_prefix + 1) + "_";
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
