package org.baderlab.csplugins.enrichmentmap.view.postanalysis;

import java.util.Objects;

import org.baderlab.csplugins.enrichmentmap.model.GeneSet;

public class SigGeneSetDescriptor {

	private final GeneSet geneSet;
	private final double mostSimilar;
	private final boolean passes;
	
	private boolean wanted = true;
	
	public SigGeneSetDescriptor(GeneSet geneSet, double mostSimilar, boolean passes) {
		this.geneSet = Objects.requireNonNull(geneSet);
		this.mostSimilar = mostSimilar;
		this.passes = passes;
	}
	
	public boolean isWanted() {
		return wanted;
	}

	public void setWanted(boolean wanted) {
		this.wanted = wanted;
	}
	
	public String getName() {
		return geneSet.getName();
	}

	public int getGeneCount() {
		return geneSet.getGenes().size();
	}
	
	public double getMostSimilar() {
		return mostSimilar;
	}
	
	public GeneSet getGeneSet() {
		return geneSet;
	}
	
	public boolean passes() {
		return passes;
	}
}