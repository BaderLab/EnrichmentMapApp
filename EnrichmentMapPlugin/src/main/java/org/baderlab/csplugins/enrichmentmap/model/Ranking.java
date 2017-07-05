package org.baderlab.csplugins.enrichmentmap.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class Ranking {

	//constants for names of Ranking set
	public final static String GSEARanking = "GSEARanking";
	public final static String RankingLOADED = "RankingLOADED";

	//Set of Ranks
	//key - gene hash key
	//value - Rank
	private Map<Integer, Rank> ranking = new HashMap<>(); 

	// Lazily computed views
	//hash for easy conversion between geneid and score
	private transient Map<Integer, Integer> rank2gene = null;
	private transient Map<Integer, Double> gene2score = null;
	//array for storing scores of all genes in map
	private double[] scores = null;

	
	public boolean isEmpty() {
		return ranking.isEmpty();
	}
	
	public boolean contains(int gene) {
		return ranking.containsKey(gene);
	}

	public Rank getRank(int gene) {
		return ranking.get(gene);
	}
	
	public void addRank(Integer gene, Rank rank) {
		ranking.put(gene, rank);
		invalidateLazyValues();
	}

	public int getMaxRank() {
		return Collections.max(getAllRanks());
	}

	public Set<Integer> getAllRanks() {
		return ranking.values().stream().map(Rank::getRank).collect(Collectors.toSet());
	}
	
	public Map<Integer,Rank> getRanking() {
		return ranking;
	}
	
	
	/**
	 * Get gene2score hash
	 * 
	 * @return HashMap gene2score
	 */
	private synchronized Map<Integer, Double> getGene2Score() {
		if(gene2score == null) {
			gene2score = new HashMap<>();
			ranking.forEach((gene,rank) -> gene2score.put(gene, rank.getScore()));
		}
		return gene2score;
	}
	
	public Double getScore(int gene) {
		return getGene2Score().get(gene);
	}

	
	private synchronized Map<Integer,Integer> getRank2Gene() {
		if(rank2gene == null) {
			rank2gene = new HashMap<>();
			ranking.forEach((gene,rank) -> rank2gene.put(rank.getRank(), gene));
		}
		return rank2gene;
	}
	
	public int getGene(int rank) {
		return getRank2Gene().get(rank);
	}
	
	public boolean containsRank(int rank) {
		return getRank2Gene().containsKey(rank);
	}
	
	/**
	 * Get scores array (elements are in no particualr order)
	 * 
	 * @return double[] scores
	 */
	public synchronized double[] getScores() {
		if(scores == null) {
			Map<Integer, Double> gene2score = getGene2Score();
			scores = new double[gene2score.size()];
			int i = 0;
			for(Double score : gene2score.values()) {
				scores[i++] = score;
			}
		}
		return scores;
	}

	private synchronized void invalidateLazyValues() {
		rank2gene = null;
		gene2score = null;
		scores = null;
	}

}
