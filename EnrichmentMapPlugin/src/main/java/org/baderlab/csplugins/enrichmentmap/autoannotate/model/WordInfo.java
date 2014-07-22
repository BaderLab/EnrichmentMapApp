package org.baderlab.csplugins.enrichmentmap.autoannotate.model;

public class WordInfo implements Comparable<WordInfo>{
	
	public String word;
	public int size;
	public int cluster;
	public int number;

	public WordInfo(String word, int size, int cluster, int number) {
		this.word = word;
		this.size = size;
		this.cluster = cluster;
		this.number = number;
	}

	@Override
	public int compareTo(WordInfo otherWordInfo) {
		return (int) Math.signum(otherWordInfo.size - size); // Sorts descending
	}
}
