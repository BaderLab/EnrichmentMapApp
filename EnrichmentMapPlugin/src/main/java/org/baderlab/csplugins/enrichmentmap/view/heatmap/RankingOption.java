package org.baderlab.csplugins.enrichmentmap.view.heatmap;

import java.awt.Color;
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
	Optional<String> getNameInDataSet();
	
	
	/** Text to be used in the JTable header. The given string will be passed to JLabel.setText(), so basic html is allowed. */
	String getTableHeaderText();
	
	
	/** Text to be used in the PDF export table header. The given string will be split on newlines. */
	String getPdfHeaderText();
	
	default Color getColor() {
		return null;
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
			public Optional<String> getNameInDataSet() {
				return Optional.empty();
			}
			public String getTableHeaderText() {
				return toString();
			}
			public String getPdfHeaderText() {
				return toString();
			}
		};
	}

}
