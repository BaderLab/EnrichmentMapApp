package org.baderlab.csplugins.enrichmentmap.view.heatmap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.baderlab.csplugins.enrichmentmap.model.EMDataSet;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.model.Rank;
import org.baderlab.csplugins.enrichmentmap.model.Ranking;
import org.baderlab.csplugins.enrichmentmap.view.heatmap.table.RankValue;
import org.baderlab.csplugins.enrichmentmap.view.util.SwingUtil;

public class BasicRankingOption implements RankingOption {

	private final String rankingName;
	private final EMDataSet dataset;
	private final Ranking ranking;
	
	public BasicRankingOption(Ranking ranking, EMDataSet dataset, String rankingName) {
		this.rankingName = rankingName;
		this.dataset = dataset;
		this.ranking = ranking;
	}
	
	@Override
	public String toString() {
		return "Ranks: " + SwingUtil.abbreviate(rankingName, 15) + 
				" - " + SwingUtil.abbreviate(dataset.getName(), 15);
	}
	
	@Override
	public CompletableFuture<Map<Integer, RankValue>> computeRanking(Collection<String> genes) {
		Map<Integer,RankValue> result = new HashMap<>();
		
		for(Map.Entry<Integer,Rank> entry : ranking.getRanking().entrySet()) {
			result.put(entry.getKey(), new RankValue(entry.getValue().getRank(), false));
		}
		
		// Remove genes that we don't need
		EnrichmentMap em = dataset.getMap();
		Set<Integer> currentGenes = genes.stream().map(em::getHashFromGene).collect(Collectors.toSet());
		result.keySet().retainAll(currentGenes);
		
		normalizeRanks(result);
		
		return CompletableFuture.completedFuture(result);
	}
	
	
	public static void normalizeRanks(Map<Integer,RankValue> result) {
		List<RankValue> rankValueList = new ArrayList<>(result.values());
		rankValueList.sort(Comparator.comparing(RankValue::getRank).reversed());
		
		// Normalize the ranks so they are of the form 1,2,3,4...
		for(int i = 0; i < rankValueList.size(); i++) {
			rankValueList.get(i).setRank(i+1);
		}
	}
}
