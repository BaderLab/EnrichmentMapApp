package org.baderlab.csplugins.enrichmentmap.view.heatmap;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.baderlab.csplugins.enrichmentmap.view.heatmap.table.RankValue;

public class HierarchicalClusterRankingOption implements RankingOption {

	@Override
	public String toString() {
		return "Hierarchical Cluster";
	}

	@Override
	public CompletableFuture<Map<Integer,RankValue>> computeRanking() {
		// TODO Auto-generated method stub
		return null;
	}

}
