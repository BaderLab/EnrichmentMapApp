package org.baderlab.csplugins.enrichmentmap.task.tunables;

import java.util.Collections;
import java.util.List;

public class GeneListTunable {
	
	private List<String> genes;
	private List<String> selectedGenes;
	private List<String> leadingEdgeGenes;
	
	
	public GeneListTunable(List<String> genes) {
		this.genes = genes;
		this.selectedGenes = genes;
		this.leadingEdgeGenes = Collections.emptyList();
	}
	
	public void setLeadingEdgeGenes(List<String> genes) {
		this.leadingEdgeGenes = genes;
	}
	
	public List<String> getLeadingEdgeGenes() {
		return leadingEdgeGenes;
	}
	
	public void setSelectedGenes(List<String> selectedGenes) {
		this.selectedGenes = selectedGenes;
	}
	
	public List<String> getSelectedGenes() {
		return selectedGenes;
	}

	public List<String> getGenes() {
		return genes;
	}

}
