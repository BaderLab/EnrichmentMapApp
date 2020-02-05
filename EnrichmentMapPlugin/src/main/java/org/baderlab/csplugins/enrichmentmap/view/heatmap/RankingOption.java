package org.baderlab.csplugins.enrichmentmap.view.heatmap;

import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface RankingOption {
	
	/** Value to be displayed in the combo box */
	String toString();
	
	default String getName() {
		return toString();
	}
	
	/** If the ranking comes directly from a DataSet, then get the ranking's name in the DataSet. */
	default Optional<String> getNameInDataSet() {
		return Optional.empty();
	}
	
	/** Text to be used in the JTable header. The given string will be passed to JLabel.setText(), so basic html is allowed. */
	default String getTableHeaderText() {
		return toString();
	}
	
	/** Text to be used in the PDF export table header. The given string will be split on newlines. */
	default String getPdfHeaderText() {
		return toString();
	}
	
	/**
	 * Asynchronously compute the rankings.
	 * @return Map where keys are geneIDs and value is the rank.
	 */
	CompletableFuture<Optional<RankingResult>> computeRanking(Collection<Integer> genes);
	
	
	public static RankingOption none() {
		return new RankingOption() {
			public String toString() {
				return "None";
			}
			public CompletableFuture<Optional<RankingResult>> computeRanking(Collection<Integer> genes) {
				return CompletableFuture.completedFuture(Optional.empty());
			}
		};
	}

}
