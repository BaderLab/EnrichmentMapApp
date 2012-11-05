package org.baderlab.csplugins.enrichmentmap;

import java.util.HashMap;

public class Ranking {
	
	//constants for names of Ranking set
	public final static String GSEARanking = "GSEARanking";
	public final static String RankingLOADED = "RankingLOADED";
	
	//Set of Ranks
	//key - gene hash key
	//value - Rank
	private HashMap<Integer, Rank> ranking;
	
	//hashes for easy conversion between geneid and rank
	private HashMap<Integer, Integer> gene2rank;
	private HashMap<Integer, Integer> rank2gene;
	
	//File associated with this ranking set
    private String filename;
	
	public Ranking(){
		ranking = new HashMap<Integer,Rank>();
		gene2rank = new HashMap<Integer,Integer>();
		rank2gene = new HashMap<Integer,Integer>();
	}

	public HashMap<Integer, Rank> getRanking() {
		return ranking;
	}

	public void setRanking(HashMap<Integer, Rank> ranking) {
		this.ranking = ranking;
	}

	public HashMap<Integer, Integer> getGene2rank() {
		return gene2rank;
	}

	public void setGene2rank(HashMap<Integer, Integer> gene2rank) {
		this.gene2rank = gene2rank;
	}

	public HashMap<Integer, Integer> getRank2gene() {
		return rank2gene;
	}

	public void setRank2gene(HashMap<Integer, Integer> rank2gene) {
		this.rank2gene = rank2gene;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}
	
	public void addRank(Integer gene, Rank rank){
		
		ranking.put(gene,  rank);
		
		gene2rank.put(gene, rank.getRank());
		rank2gene.put(rank.getRank(), gene);
	}
}
