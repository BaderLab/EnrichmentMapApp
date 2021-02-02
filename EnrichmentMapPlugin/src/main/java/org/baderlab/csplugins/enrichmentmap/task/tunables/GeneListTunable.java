package org.baderlab.csplugins.enrichmentmap.task.tunables;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.baderlab.csplugins.enrichmentmap.model.EMDataSet;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.view.heatmap.GSEALeadingEdgeRankingOption;

public class GeneListTunable {
	
	private final EnrichmentMap map;
	private List<String> genes;
	private List<String> selectedGenes;
	private List<GSEALeadingEdgeRankingOption> leadingEdgeRanks;
	
	private List<String> expressionGenes;
	
	
	public GeneListTunable(
			EnrichmentMap map, 
			List<String> genes, 
			List<String> selectedGenes, 
			List<GSEALeadingEdgeRankingOption> leadingEdgeRanks)
	{
		this.map = map;
		this.genes = Objects.requireNonNull(genes);
		this.leadingEdgeRanks = leadingEdgeRanks == null ? Collections.emptyList() : leadingEdgeRanks;
		this.selectedGenes = selectedGenes == null ? Collections.emptyList() : selectedGenes;
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

	public List<String> getExpressionGenes() {
		if(expressionGenes == null) {
			expressionGenes = genes.stream().filter(gene -> hasExpressions(map, gene)).collect(Collectors.toList());
		}
		return expressionGenes;
	}
	
	private static boolean hasExpressions(EnrichmentMap map, String gene) {
		int geneId = map.getHashFromGene(gene);
		for(EMDataSet ds : map.getDataSetList()) {
			if(ds.getExpressionGenes().contains(geneId)) {
				return true;
			}
		}
		return false;
	}
}
