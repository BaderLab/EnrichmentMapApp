package org.baderlab.csplugins.enrichmentmap.autoannotate.model;

import java.util.List;

public class LabelOptions {
	
	private int maxWords;
	private List<Integer> wordSizeThresholds;
	private double[] labelPosition;
	private int sameClusterBonus;
	private int centralityBonus;
	
	public LabelOptions(int maxWords, List<Integer> wordSizeThresholds,
			double[] labelPosition, int sameClusterBonus,
			int centralityBonus) {
		super();
		this.maxWords = maxWords;
		this.wordSizeThresholds = wordSizeThresholds;
		this.labelPosition = labelPosition;
		this.sameClusterBonus = sameClusterBonus;
		this.centralityBonus = centralityBonus;
	}

	public int getMaxWords() {
		return maxWords;
	}

	public void setMaxWords(int maxWords) {
		this.maxWords = maxWords;
	}

	public List<Integer> getWordSizeThresholds() {
		return wordSizeThresholds;
	}

	public void setWordSizeThresholds(List<Integer> wordSizeThresholds) {
		this.wordSizeThresholds = wordSizeThresholds;
	}

	public double[] getLabelPosition() {
		return labelPosition;
	}

	public void setLabelPosition(double[] labelPosition) {
		this.labelPosition = labelPosition;
	}

	public int getSameClusterBonus() {
		return sameClusterBonus;
	}

	public void setSameClusterBonus(int sameClusterBonus) {
		this.sameClusterBonus = sameClusterBonus;
	}

	public int getCentralityBonus() {
		return centralityBonus;
	}

	public void setCentralityBonus(int centralityBonus) {
		this.centralityBonus = centralityBonus;
	}
	
}