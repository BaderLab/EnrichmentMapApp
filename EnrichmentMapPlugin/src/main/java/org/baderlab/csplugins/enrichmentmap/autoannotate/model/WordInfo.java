package org.baderlab.csplugins.enrichmentmap.autoannotate.model;

public class WordInfo implements Comparable<WordInfo>{
	
	private String word;
	private int size;
	private int cluster;
	private int number;

	public String getWord() {
		return word;
	}

	public void setWord(String word) {
		this.word = word;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public int getCluster() {
		return cluster;
	}

	public void setCluster(int cluster) {
		this.cluster = cluster;
	}

	public int getNumber() {
		return number;
	}

	public void setNumber(int number) {
		this.number = number;
	}

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
