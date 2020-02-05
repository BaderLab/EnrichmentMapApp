package org.baderlab.csplugins.enrichmentmap.view.heatmap.table;

import javax.annotation.Nullable;

import org.baderlab.csplugins.enrichmentmap.model.Rank;

/**
 * This is a wrapper object for a "rank" that is computed by a RankingOption.
 * We need this object to track which genes are significant (leading edge), 
 * and to influence the sorting order in the JTable.
 */
public class RankValue {

	public static final RankValue EMPTY = new RankValue(null, null, false);
	
	
	private @Nullable Integer rank;
	private @Nullable Double score;
	private final boolean significant;
	
	public RankValue(@Nullable Integer rank, @Nullable Double score, boolean significant) {
		this.rank = rank;
		this.score = score;
		this.significant = significant;
	}
	
	public RankValue(Rank rank, boolean significant) {
		this.rank = rank.getRank();
		this.score = rank.getScore();
		this.significant = significant;
	}

	public @Nullable Integer getRank() {
		return rank;
	}
	
	public void setRank(Integer rank) {
		this.rank = rank;
	}

	public @Nullable Double getScore() {
		return score;
	}
	
	public boolean isSignificant() {
		return significant;
	}
	
}
