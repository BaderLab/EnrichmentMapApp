package org.baderlab.csplugins.enrichmentmap.view.heatmap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.baderlab.csplugins.enrichmentmap.view.heatmap.table.RankValue;

public class RankingResult {

	public enum SortSuggestion {
		ASC, DESC, NONE
	}
	
	private final Map<Integer,RankValue> ranking;
	private final boolean isGsea; // there might be significant genes (leading edge)
	
	private SortSuggestion sortSuggestion;

	
	public RankingResult(Map<Integer,RankValue> ranking, boolean isGsea) {
		this.ranking = ranking == null ? Collections.emptyMap() : ranking;
		this.isGsea = isGsea;
	}
	
	public static RankingResult empty() {
		return new RankingResult(Collections.emptyMap(), false);
	}
	
	public Map<Integer,RankValue> getRanking() {
		return ranking;
	}

	public SortSuggestion getSortSuggestion() {
		if(sortSuggestion == null) {
			sortSuggestion = computeSortSuggestion();
		}
		return sortSuggestion;
	}
	
	
	private SortSuggestion computeSortSuggestion() {
		if(!isGsea)
			return SortSuggestion.NONE;
		if(ranking.isEmpty())
			return SortSuggestion.NONE;
		
		List<RankValue> ranks = new ArrayList<>(ranking.values());
		ranks.sort(Comparator.comparing(RankValue::getRank));
		
		// If the leading edge is at the top
		if(ranks.get(0).isSignificant())
			return SortSuggestion.ASC;
		
		// If the leading edgs is at the bottom
		if(ranks.get(ranks.size()-1).isSignificant())
			return SortSuggestion.DESC;
		
		return SortSuggestion.NONE;
	}
}
