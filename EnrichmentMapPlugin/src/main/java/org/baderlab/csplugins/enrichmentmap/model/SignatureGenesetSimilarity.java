package org.baderlab.csplugins.enrichmentmap.model;

import java.util.Set;

public class SignatureGenesetSimilarity extends GenesetSimilarity {

	// Hypergeometric
	private double hypergeomPValue = -1.0;
	private int hypergeomU;
	private int hypergeomN;
	private int hypergeomK;
	private int hypergeomM;

	// Mann-Whitney U
	private double mannWhitPValueTwoSided;
	private double mannWhitPValueGreater;
	private double mannWhitPValueLess;

	// flag that indicates if either of the gene sets has no ranks (and therefore we cannot calculated mann-whit
	private boolean mannWhitMissingRanks;
	
	private boolean passesCutoff = true;
		
		
	public SignatureGenesetSimilarity(String geneset1Name, String geneset2Name, double similarityCoeffecient,
			String interactionType, Set<Integer> overlappingGenes, String datasetName) {
		super(geneset1Name, geneset2Name, similarityCoeffecient, interactionType, overlappingGenes, datasetName);
	}
	
	public void setPassesCutoff(boolean passes) {
		this.passesCutoff = passes;
	}
	
	public boolean getPassesCutoff() {
		return passesCutoff;
	}

	public void setHypergeomPValue(double hypergeomPValue) {
		this.hypergeomPValue = hypergeomPValue;
	}

	public double getHypergeomPValue() {
		return hypergeomPValue;
	}
	
	/**
	 * Get the N value used in the Hypergeometric test. 'U' denotes the size of
	 * the universe (i.e. the size of the union of all gene sets)
	 */
	public int getHypergeomU() {
		return hypergeomU;
	}

	/**
	 * Set the N value used in the Hypergeometric test. 'U' denotes the size of
	 * the universe (i.e. the size of the union of all gene sets)
	 */
	public void setHypergeomU(int value) {
		this.hypergeomU = value;
	}

	/**
	 * Get the n value used in the Hypergeometric test. 'n' denotes the size of
	 * the sample being taken from the universe (i.e. the size of the signature gene set)
	 */
	public int getHypergeomN() {
		return hypergeomN;
	}

	/**
	 * Set the n value used in the Hypergeometric test. 'n' denotes the size of
	 * the sample being taken from the universe (i.e. the size of the signature gene set)
	 */
	public void setHypergeomN(int value) {
		this.hypergeomN = value;
	}

	/**
	 * Get the k value used in the Hypergeometric test. 'k' denotes the number of
	 * successes in the sample (i.e. the size of the overlap)
	 */
	public int getHypergeomK() {
		return hypergeomK;
	}

	/**
	 * Set the k value used in the Hypergeometric test. 'k' denotes the number of
	 * successes in the sample (i.e. the size of the overlap)
	 */
	public void setHypergeomK(int value) {
		this.hypergeomK = value;
	}

	/**
	 * Get the m value used in the Hypergeometric test. 'm' denotes the total
	 * number of successes in the universe (i.e. the size of the enriched gene set)
	 */
	public int getHypergeomM() {
		return hypergeomM;
	}

	/**
	 * Set the m value used in the Hypergeometric test. 'm' denotes the total
	 * number of successes in the universe (i.e. the size of the enriched gene set)
	 */
	public void setHypergeomM(int value) {
		this.hypergeomM = value;
	}

	public double getMannWhitPValueTwoSided() {
		return mannWhitPValueTwoSided;
	}

	public void setMannWhitPValueTwoSided(double value) {
		this.mannWhitPValueTwoSided = value;
	}

	public double getMannWhitPValueGreater() {
		return mannWhitPValueGreater;
	}

	public void setMannWhitPValueGreater(double value) {
		this.mannWhitPValueGreater = value;
	}

	public double getMannWhitPValueLess() {
		return mannWhitPValueLess;
	}

	public void setMannWhitPValueLess(double value) {
		this.mannWhitPValueLess = value;
	}

	public boolean isMannWhitMissingRanks() {
		return mannWhitMissingRanks;
	}

	public void setMannWhitMissingRanks(boolean value) {
		this.mannWhitMissingRanks = value;
	}
}
