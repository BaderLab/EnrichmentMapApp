package org.baderlab.csplugins.enrichmentmap.view.heatmap.table;

import java.util.Comparator;

import javax.annotation.Nullable;

/**
 * This is a wrapper object for a "rank" that is computed by a RankingOption.
 * We need this object to track which genes are significant (leading edge), 
 * and to influence the sorting order in the JTable.
 */
public class RankValue implements Comparable<RankValue> {

	public static final RankValue EMPTY = new RankValue(null, null, false);
	
	
	private Integer rank; // May be null
	private final boolean significant;
	private @Nullable Double score;
	
	public RankValue(Integer rank, @Nullable Double score, boolean significant) {
		this.rank = rank;
		this.significant = significant;
		this.score = score;
	}

	public Integer getRank() {
		return rank;
	}
	
	public void setRank(Integer rank) {
		this.rank = rank;
	}

	public boolean isSignificant() {
		return significant;
	}
	
	public @Nullable Double getScore() {
		return score;
	}
	
	
	/**
	 * The DefaultRowSorter always sorts nulls first, I want empty ranks to be sorted last.
	 * Therefore instead of using null to represent an empty rank, we use a RankValue object 
	 * with a null rank field, then do the comparison here. 
	 */
	@Override
	public int compareTo(RankValue other) {
		return rankComparator.compare(rank, other.rank);
	}
	
	private static final Comparator<Integer> rankComparator = Comparator.nullsLast(Comparator.naturalOrder());
	
}
