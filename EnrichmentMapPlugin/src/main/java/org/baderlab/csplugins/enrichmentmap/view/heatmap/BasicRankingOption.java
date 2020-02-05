package org.baderlab.csplugins.enrichmentmap.view.heatmap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.baderlab.csplugins.enrichmentmap.model.EMDataSet;
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
	public Optional<String> getNameInDataSet() {
		return Optional.of(rankingName);
	}
	
	@Override
	public String toString() {
		return "Ranks: " + rankingName + " - " + dataset.getName();
	}
	
	@Override
	public String getTableHeaderText() {
		String r = SwingUtil.abbreviate(rankingName, 11);
		String d = SwingUtil.abbreviate(dataset.getName(), 11);
		
		if(r.equals(d))
			return "<html>Ranks<br>" + r + "</html>";
		else
			return "<html>" + r + "<br>" + d + "</html>";
	}
	
	@Override
	public String getPdfHeaderText() {
		String r = SwingUtil.abbreviate(rankingName, 11);
		String d = SwingUtil.abbreviate(dataset.getName(), 11);
		
		if(r.equals(d))
			return "Ranks\n" + r;
		else
			return r + "\n" + d;
	}
	
	@Override
	public CompletableFuture<Optional<RankingResult>> computeRanking(Collection<Integer> genes) {
		Map<Integer,RankValue> result = new HashMap<>();
		
		for(Map.Entry<Integer,Rank> entry : ranking.getRanking().entrySet()) {
			Rank rank = entry.getValue();
			result.put(entry.getKey(), new RankValue(rank, false));
		}
		
		// Remove genes that we don't need
		result.keySet().retainAll(genes);
		
		normalizeRanks(result);
		
		return CompletableFuture.completedFuture(Optional.of(new RankingResult(result, false)));
	}
	
	
	public static void normalizeRanks(Map<Integer,RankValue> result) {
		List<RankValue> rankValueList = new ArrayList<>(result.values());
		rankValueList.sort(Comparator.comparing(RankValue::getRank));
		
		// Normalize the ranks so they are of the form 1,2,3,4...
		for(int i = 0; i < rankValueList.size(); i++) {
			rankValueList.get(i).setRank(i+1);
		}
	}
}
