package org.baderlab.csplugins.enrichmentmap.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Ranking {

	//constants for names of Ranking set
	public final static String GSEARanking = "GSEARanking";
	public final static String RankingLOADED = "RankingLOADED";

	//Set of Ranks
	//key - gene hash key
	//value - Rank
	private Map<Integer, Rank> ranking;

	//hashes for easy conversion between geneid and rank
	private Map<Integer, Integer> gene2rank;
	private Map<Integer, Integer> rank2gene;

	// Lazily computed values
	//hash for easy conversion between geneid and score
	private Map<Integer, Double> gene2score = null;
	//array for storing scores of all genes in map
	private double[] scores = null;

	//File associated with this ranking set
	private String filename;

	public Ranking() {
		ranking = new HashMap<Integer, Rank>();
		gene2rank = new HashMap<Integer, Integer>();
		rank2gene = new HashMap<Integer, Integer>();
	}

	public Map<Integer, Rank> getRanking() {
		return ranking;
	}

	public void setRanking(HashMap<Integer, Rank> ranking) {
		this.ranking = ranking;
		for(Iterator<Integer> i = ranking.keySet().iterator(); i.hasNext();) {
			Integer cur = (Integer) i.next();
			gene2rank.put(cur, ranking.get(cur).getRank());
			rank2gene.put(ranking.get(cur).getRank(), cur);
		}
		invalidateLazyValues();
	}

	public Map<Integer, Integer> getGene2rank() {
		return gene2rank;
	}

	public void setGene2rank(HashMap<Integer, Integer> gene2rank) {
		this.gene2rank = gene2rank;
		if(this.rank2gene == null || this.rank2gene.isEmpty()) {
			for(Iterator<Integer> i = gene2rank.keySet().iterator(); i.hasNext();) {
				Integer cur = (Integer) i.next();
				rank2gene.put(gene2rank.get(cur), cur);
				ranking.put(cur, new Rank(cur.toString(), 0.0, gene2rank.get(cur)));
			}
		}
		invalidateLazyValues();
	}

	public Map<Integer, Integer> getRank2gene() {
		return rank2gene;
	}

	public void setRank2gene(HashMap<Integer, Integer> rank2gene) {
		this.rank2gene = rank2gene;
		if(this.gene2rank == null || this.gene2rank.isEmpty()) {
			for(Iterator<Integer> i = rank2gene.keySet().iterator(); i.hasNext();) {
				Integer cur = (Integer) i.next();
				gene2rank.put(rank2gene.get(cur), cur);
				ranking.put(rank2gene.get(cur), new Rank(rank2gene.get(cur).toString(), 0.0, cur));
			}
		}
		invalidateLazyValues();
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public void addRank(Integer gene, Rank rank) {
		ranking.put(gene, rank);
		gene2rank.put(gene, rank.getRank());
		rank2gene.put(rank.getRank(), gene);
		invalidateLazyValues();
	}

	public int getMaxRank() {
		return Collections.max(rank2gene.keySet());
	}

	public String toString() {
		StringBuffer paramVariables = new StringBuffer();
		paramVariables.append(filename + "%fileName\t" + filename + "\n");
		return paramVariables.toString();
	}

	/**
	 * Get gene2score hash
	 * 
	 * @return HashMap gene2score
	 */
	public Map<Integer, Double> getGene2Score() {
		if(gene2score == null) {
			gene2score = new HashMap<>();
			for(Map.Entry<Integer, Rank> entry : ranking.entrySet()) {
				gene2score.put(entry.getKey(), entry.getValue().getScore());
			}
		}
		return gene2score;
	}

	/**
	 * Get scores array (elements are in no particualr order)
	 * 
	 * @return double[] scores
	 */
	public double[] getScores() {
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

	private void invalidateLazyValues() {
		gene2score = null;
		scores = null;
	}

}
