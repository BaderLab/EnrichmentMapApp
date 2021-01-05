package org.baderlab.csplugins.enrichmentmap.view.heatmap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.baderlab.csplugins.enrichmentmap.model.EMDataSet;
import org.baderlab.csplugins.enrichmentmap.model.EMDataSet.Method;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentResult;
import org.baderlab.csplugins.enrichmentmap.model.GSEAResult;
import org.baderlab.csplugins.enrichmentmap.model.Ranking;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTableUtil;

import com.google.inject.Inject;

public class RankingOptionFactory {

	@Inject private CyNetworkManager networkManager;
	
	
	public List<RankingOption> getDataSetRankOptions(EnrichmentMap map) {
		CyNetwork network = networkManager.getNetwork(map.getNetworkID());
		if (network == null)
			return Collections.emptyList();
		
		List<CyNode> selectedNodes = CyTableUtil.getNodesInState(network, CyNetwork.SELECTED, true);
		List<CyEdge> selectedEdges = CyTableUtil.getEdgesInState(network, CyNetwork.SELECTED, true);
		
		return getDataSetRankOptions(map, network, selectedNodes, selectedEdges);
	}
	
	
	public List<RankingOption> getDataSetRankOptions(EnrichmentMap map, List<CyNode> nodes, List<CyEdge> edges) {
		CyNetwork network = networkManager.getNetwork(map.getNetworkID());
		if (network == null)
			return Collections.emptyList();
		return getDataSetRankOptions(map, network, nodes, edges);
	}
	
	
	public List<RankingOption> getDataSetRankOptions(EnrichmentMap map, CyNetwork network, List<CyNode> nodes, List<CyEdge> edges) {
		List<RankingOption> options = new ArrayList<>();
		for(EMDataSet dataset : map.getDataSetList()) {
			if(nodes.size() == 1 && edges.isEmpty() && dataset.getMethod() == Method.GSEA) {
				String geneSetName = network.getRow(nodes.get(0)).get(CyNetwork.NAME, String.class);
				Map<String,EnrichmentResult> results = dataset.getEnrichments().getEnrichments();
				EnrichmentResult result = results.get(geneSetName);
				if(result instanceof GSEAResult) {
					GSEAResult gseaResult = (GSEAResult) result; 
					Map<String,Ranking> ranks = dataset.getRanks();
					ranks.forEach((name, ranking) -> {
						options.add(new GSEALeadingEdgeRankingOption(dataset, gseaResult, name));
					});
				} else {
					Map<String,Ranking> ranks = dataset.getRanks();
					ranks.forEach((name, ranking) -> {
						options.add(new BasicRankingOption(ranking, dataset, name));
					});
				}
			} else {
				Map<String,Ranking> ranks = dataset.getRanks();
				ranks.forEach((name, ranking) -> {
					options.add(new BasicRankingOption(ranking, dataset, name));
				});
			}
		}
		return options;
	}
	
	
	public List<GSEALeadingEdgeRankingOption> getGSEADataSetSetRankOptions(EnrichmentMap map) {
		List<RankingOption> options = getDataSetRankOptions(map);
		
		List<GSEALeadingEdgeRankingOption> gseaOptions = new ArrayList<>();
		for(RankingOption option : options) {
			if(option instanceof GSEALeadingEdgeRankingOption) {
				gseaOptions.add((GSEALeadingEdgeRankingOption)option);
			}
		}
		return gseaOptions;
	}
	
	
}
