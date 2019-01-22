package org.baderlab.csplugins.enrichmentmap.task.tunables;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class GeneListTunable {
	
	private List<String> genes;
	private List<String> selectedGenes;
	private Set<String> leadingEdgeGenes;
	
	
	public GeneListTunable(List<String> genes, Set<String> leadingEdge) {
		this.genes = Objects.requireNonNull(genes);
		this.leadingEdgeGenes = leadingEdge == null ? Collections.emptySet() : leadingEdge;
		this.selectedGenes = genes;
	}
	
	public void setLeadingEdgeGenes(Set<String> genes) {
		this.leadingEdgeGenes = genes;
	}
	
	public Set<String> getLeadingEdgeGenes() {
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
