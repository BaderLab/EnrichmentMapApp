package org.baderlab.csplugins.enrichmentmap.task.genemania;

import java.io.Serializable;

public class GMGene implements Serializable {

	private static final long serialVersionUID = -4637681171522174342L;

	private String symbol;
	private String queryTerm;
	private boolean queryGene;
	private String description;
	private double score;

	public String getSymbol() {
		return symbol;
	}

	public void setSymbol(String symbol) {
		this.symbol = symbol;
	}

	public String getQueryTerm() {
		return queryTerm;
	}

	public void setQuerySymbol(String querySymbol) {
		this.queryTerm = querySymbol;
	}

	public boolean isQueryGene() {
		return queryGene;
	}

	public void setQueryGene(boolean queryGene) {
		this.queryGene = queryGene;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public double getScore() {
		return score;
	}

	public void setScore(double score) {
		this.score = score;
	}

	@Override
	public String toString() {
		return symbol;
	}
}
