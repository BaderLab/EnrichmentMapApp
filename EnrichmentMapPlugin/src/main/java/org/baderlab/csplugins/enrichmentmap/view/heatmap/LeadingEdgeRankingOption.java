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
import org.baderlab.csplugins.enrichmentmap.model.EMDataSet.Method;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentResult;
import org.baderlab.csplugins.enrichmentmap.model.GSEAResult;
import org.baderlab.csplugins.enrichmentmap.model.GeneExpression;
import org.baderlab.csplugins.enrichmentmap.model.Rank;
import org.baderlab.csplugins.enrichmentmap.model.Ranking;
import org.baderlab.csplugins.enrichmentmap.parsers.DetermineEnrichmentResultFileReader;
import org.baderlab.csplugins.enrichmentmap.view.heatmap.table.RankValue;

public class LeadingEdgeRankingOption implements RankingOption {
	
	private final EMDataSet dataset;
	private final String geneSetName;
	
	private double scoreAtMax;
	private int rankAtMax;
	private final String ranksName = Ranking.GSEARanking; // MKTODO will this ever be different?
	
	
	public LeadingEdgeRankingOption(EMDataSet dataset, String geneSetName) {
		assert dataset.getMethod() == Method.GSEA;
		this.dataset = dataset;
		this.geneSetName = geneSetName;
	}
	
	@Override
	public String toString() {
		String name = dataset.getName();
		return "GSEA Ranks: " + abbreviate(name, 25);
	}
	
	private static String abbreviate(String s, int maxLength) {
		s = String.valueOf(s); // null check
		if(s.length() > maxLength) {
			s = s.substring(0, maxLength) + "...";
		}
		return s;
	}
	
	@Override
	public CompletableFuture<Map<Integer,RankValue>> computeRanking(Collection<String> genes) {
		initializeLeadingEdge();
		
		int topRank = getTopRank();
		boolean isNegative = isNegativeGS();
		
		Map<Integer,GeneExpression> expressions = dataset.getExpressionSets().getExpressionMatrix();
		Ranking ranking = dataset.getExpressionSets().getRanksByName(ranksName);
		
		Integer[] ranksSubset = new Integer[expressions.size()];
		HashMap<Integer, ArrayList<Integer>> rank2keys = new HashMap<Integer, ArrayList<Integer>>();
		
		int n = 0;
		Map<Integer, Rank> currentRanks = ranking.getRanking();
		for(Integer key : expressions.keySet()) {
			if (currentRanks.containsKey(key)) {
				ranksSubset[n] = currentRanks.get(key).getRank();
			} else {
				ranksSubset[n] = -1;
			}
			rank2keys.computeIfAbsent(ranksSubset[n], k -> new ArrayList<>()).add(key);
			n++;
		}
		
		Map<Integer,RankValue> result = new HashMap<>();
		
		int previous = -1;
		boolean significant = false;

		for (int m = 0; m < ranksSubset.length; m++) {
			//if the current gene doesn't have a rank then don't show it
			if (ranksSubset[m] == -1)
				continue;
			if (ranksSubset[m] == previous)
				continue;

			previous = ranksSubset[m];
			
			significant = false;
			if (ranksSubset[m] <= topRank && !isNegative && topRank != 0 && topRank != -1)
				significant = true;
			else if (ranksSubset[m] >= topRank && isNegative && topRank != 0 && topRank != -1)
				significant = true;

			List<Integer> keys = rank2keys.get(ranksSubset[m]);
			
			for(Integer key : keys) {
				result.put(key, new RankValue(currentRanks.get(key).getRank(), significant));
			}
		}
		
		// Remove genes that we don't need
		EnrichmentMap em = dataset.getMap();
		Set<Integer> currentGenes = genes.stream().map(em::getHashFromGene).collect(Collectors.toSet());
		result.keySet().retainAll(currentGenes);
		
		List<RankValue> rankValueList = new ArrayList<>(result.values());
		rankValueList.sort(Comparator.comparing(RankValue::getRank).reversed());
		
		// Normalize the ranks so they are of the form 1,2,3,4...
		for(int i = 0; i < rankValueList.size(); i++) {
			rankValueList.get(i).setRank(i+1);
		}
		
		return CompletableFuture.completedFuture(result);
	}
	
	
	/**
	 * Collates the current selected nodes genes to represent the expression of
	 * the genes that are in all the selected nodes. and sets the expression
	 * sets (both if there are two datasets)
	 */
	private void initializeLeadingEdge() {
		Map<String,EnrichmentResult> results = dataset.getEnrichments().getEnrichments();
		GSEAResult result = (GSEAResult) results.get(geneSetName);
		scoreAtMax = result.getScoreAtMax();
		if(scoreAtMax == DetermineEnrichmentResultFileReader.DefaultScoreAtMax) {
			scoreAtMax = result.getNES();
		}
		rankAtMax = result.getRankAtMax();
	}
	
	
	private int getTopRank() {
		int topRank = rankAtMax + 3; // MKTODO why?
		if(scoreAtMax < 0) {
			topRank = dataset.getExpressionSets().getRanksByName(ranksName).getMaxRank() - topRank;
		}
		return topRank;
	}

	private boolean isNegativeGS() {
		return scoreAtMax < 0;
	}

}
