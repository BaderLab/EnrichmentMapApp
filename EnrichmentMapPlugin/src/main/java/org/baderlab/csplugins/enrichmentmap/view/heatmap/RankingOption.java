package org.baderlab.csplugins.enrichmentmap.view.heatmap;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.baderlab.csplugins.enrichmentmap.view.heatmap.table.RankValue;

public interface RankingOption {
	
	/** Value to be displayed in the combo box */
	String toString();
	
	default String getName() {
		return toString();
	}
	
	/**
	 * Asynchronously compute the rankings.
	 * @return Map where keys are geneIDs and value is the rank.
	 */
	CompletableFuture<Map<Integer,RankValue>> computeRanking(Collection<String> genes);
	
	
	public static RankingOption none() {
		return new RankingOption() {
			public String toString() {
				return "None";
			}
			public CompletableFuture<Map<Integer,RankValue>> computeRanking(Collection<String> genes) {
				return CompletableFuture.completedFuture(null);
			}
		};
	}

}
