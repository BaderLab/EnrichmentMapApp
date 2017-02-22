package org.baderlab.csplugins.enrichmentmap.view.heatmap.table;

import java.util.HashMap;
import java.util.Map;

import org.baderlab.csplugins.enrichmentmap.model.Rank;
import org.baderlab.csplugins.enrichmentmap.model.Ranking;

import com.google.common.collect.ComparisonChain;

public class RankValue implements Comparable<RankValue> {

	private final Rank rank;
	private final boolean significant;
	
	public RankValue(Rank rank, boolean significant) {
		this.rank = rank;
		this.significant = significant;
	}

	public Rank getRank() {
		return rank;
	}

	public boolean isSignificant() {
		return significant;
	}
	
	@Override
	public int compareTo(RankValue other) {
		return ComparisonChain.start()
				.compare(significant, other.significant)
				.compare(rank.getRank(), other.rank.getRank())
				.result();
	}
	
	
	public static Map<Integer,RankValue> createBasic(Ranking ranking) {
		Map<Integer,RankValue> basic = new HashMap<>();
		for(Map.Entry<Integer,Rank> entry : ranking.getRanking().entrySet()) {
			basic.put(entry.getKey(), new RankValue(entry.getValue(), true));
		}
		return basic;
	}

	
}
