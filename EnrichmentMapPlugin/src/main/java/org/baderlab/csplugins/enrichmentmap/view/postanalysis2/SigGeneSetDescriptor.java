package org.baderlab.csplugins.enrichmentmap.view.postanalysis2;

import java.util.Objects;

import org.baderlab.csplugins.enrichmentmap.model.GeneSet;

public class SigGeneSetDescriptor {

	private boolean wanted = true;
	
	private final GeneSet geneSet;
	private final int maxOverlap;
	
//	private final double smallestHypergeomPValue;
//	private final double smallestMannWhitPValue;
	
	
	public SigGeneSetDescriptor(GeneSet geneSet, int maxOverlap) {
		this.geneSet = Objects.requireNonNull(geneSet);
		this.maxOverlap = maxOverlap;
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
	
	public int getMaxOverlap() {
		return maxOverlap;
	}
}
