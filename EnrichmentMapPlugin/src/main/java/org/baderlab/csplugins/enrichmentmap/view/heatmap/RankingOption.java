package org.baderlab.csplugins.enrichmentmap.view.heatmap;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.baderlab.csplugins.enrichmentmap.model.Ranking;
import org.baderlab.csplugins.enrichmentmap.view.heatmap.table.RankValue;

public interface RankingOption {
	
	/** Value to be displayed in the combo box */
	String toString();
	
	/**
	 * Asynchronously compute the rankings.
	 * @return Map where keys are geneIDs and value is the rank.
	 */
	CompletableFuture<Map<Integer,RankValue>> computeRanking();
	
	
	public static RankingOption fromExisting(String name, Ranking ranking) {
		
		return new RankingOption() {
			public String toString() {
				return name;
			}
			public CompletableFuture<Map<Integer,RankValue>> computeRanking() {
				return CompletableFuture.completedFuture(RankValue.createBasic(ranking));
			}
		};
	}
	
	
	public static RankingOption none() {
		return new RankingOption() {
			public String toString() {
				return "None";
			}
			public CompletableFuture<Map<Integer,RankValue>> computeRanking() {
				return CompletableFuture.completedFuture(null);
			}
		};
	}

}
