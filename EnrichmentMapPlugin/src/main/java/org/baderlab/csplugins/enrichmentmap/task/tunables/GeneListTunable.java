package org.baderlab.csplugins.enrichmentmap.task.tunables;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.view.heatmap.GSEALeadingEdgeRankingOption;

public class GeneListTunable {
	
	private final EnrichmentMap map;
	private List<String> genes;
	private List<String> selectedGenes;
	private List<GSEALeadingEdgeRankingOption> leadingEdgeRanks;
	
	
	public GeneListTunable(EnrichmentMap map, List<String> genes, List<GSEALeadingEdgeRankingOption> leadingEdgeRanks) {
		this.map = map;
		this.genes = Objects.requireNonNull(genes);
		this.leadingEdgeRanks = leadingEdgeRanks == null ? Collections.emptyList() : leadingEdgeRanks;
		this.selectedGenes = genes;
	}
	
	public List<GSEALeadingEdgeRankingOption> getLeadingEdgeRanks() {
		return leadingEdgeRanks;
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
	
	public EnrichmentMap getEnrichmentMap() {
		return map;
	}

}
