package org.baderlab.csplugins.enrichmentmap.task;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.baderlab.csplugins.enrichmentmap.model.EMDataSet;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.model.GeneSet;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

public class FilterGenesetsByDatasetGenes extends AbstractTask {

	private EnrichmentMap map;

	public FilterGenesetsByDatasetGenes(EnrichmentMap map) {
		this.map = map;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		taskMonitor.setStatusMessage("Filtering Gene Sets");
		filterGenesets(taskMonitor);
		taskMonitor.setStatusMessage("");
	}
	
	/*
	 * Filter all the genesets by the dataset genes. If there are multiple sets
	 * of genesets make sure to filter by the specific dataset genes.
	 */
	public void filterGenesets(TaskMonitor taskMonitor) {
		Map<String, EMDataSet> datasets = map.getDataSets();
		for(String k : datasets.keySet()) {
			taskMonitor.setStatusMessage("Filtering Data Set: " + k);
			EMDataSet current_set = datasets.get(k);
			
			//only filter the genesets if dataset genes are not null or empty
			// MKTODO yeah but we fill the data set with Dummy expressions, so dataSet genes will never by empty right?
			Set<Integer> datasetGenes = current_set.getDataSetGenes();
			if(datasetGenes != null && !datasetGenes.isEmpty()) {
				current_set.getGeneSetsOfInterest().filterGeneSets(datasetGenes);
			} else {
				System.out.println("Dataset Genes is empty, because expression and ranks not provided: " + current_set.getName());
			}
		}

		// check to make sure that after filtering there are still genes in the genesets
		// if there aren't any genes it could mean that the IDs don't match or it could mean none
		// of the genes in the expression file are in the specified genesets.
		if(!anyGenesLeftAfterFiltering(datasets.values()))
			throw new IllegalThreadStateException("No genes in the expression file are found in the GMT file ");

		// if there are multiple datasets check to see if they have the same set of genes
		if(datasetsAreDistinct(datasets.values())) {
			map.setDistinctExpressionSets(true);
		}
	}

	
	private static boolean datasetsAreDistinct(Collection<EMDataSet> datasets) {
		Set<Set<Integer>> uniqueGeneSets = new HashSet<>();
		for(EMDataSet dataset : datasets) {
			Set<Integer> genes = dataset.getDataSetGenes();
			uniqueGeneSets.add(genes);
			if(uniqueGeneSets.size() > 1) {
				return true;
			}
		}
		return false;
	}
	
	
	/**
	 * Check to see that there are genes in the filtered genesets If the ids do
	 * not match up, after a filtering there will be no genes in any of the genesets
	 * 
	 * @return true if Genesets have genes, return false if all the genesets are empty
	 */
	private static boolean anyGenesLeftAfterFiltering(Collection<EMDataSet> datasets) {
		for(EMDataSet dataset : datasets) {
			Map<String, GeneSet> genesets = dataset.getGeneSetsOfInterest().getGeneSets();
			for(GeneSet geneset : genesets.values()) {
				Set<Integer> genesetGenes = geneset.getGenes();
				//if there is at least one gene in any of the genesets then the ids match.
				if(!genesetGenes.isEmpty()) {
					return true;
				}
			}
		}
		return false;
	}

}
