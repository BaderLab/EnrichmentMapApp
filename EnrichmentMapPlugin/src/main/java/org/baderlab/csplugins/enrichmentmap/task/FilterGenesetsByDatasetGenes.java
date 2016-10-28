package org.baderlab.csplugins.enrichmentmap.task;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.baderlab.csplugins.enrichmentmap.model.DataSet;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

public class FilterGenesetsByDatasetGenes extends AbstractTask {

	private EnrichmentMap map;

	public FilterGenesetsByDatasetGenes(EnrichmentMap map) {
		this.map = map;
	}

	/*
	 * Filter all the genesets by the dataset genes. If there are multiple sets
	 * of genesets make sure to filter by the specific dataset genes.
	 */
	public void filterGenesets(TaskMonitor taskMonitor) {
		Map<String, DataSet> datasets = map.getDatasets();
		for(String k : datasets.keySet()) {
			taskMonitor.setStatusMessage("Filtering Data Set: " + k);
			DataSet current_set = datasets.get(k);
			
			//only filter the genesets if dataset genes are not null or empty
			Set<Integer> datasetGenes = current_set.getDatasetGenes();
			if(datasetGenes != null && !datasetGenes.isEmpty()) {
				current_set.getSetofgenesets().filterGenesets(datasetGenes);
			} else {
				System.out.println("Dataset Genes is empty, because expression and ranks not provided: " + current_set.getName());
			}
		}

		//check to make sure that after filtering there are still genes in the genesets
		//if there aren't any genes it could mean that the IDs don't match or it could mean none
		//of the genes in the expression file are in the specified genesets.
		if(!map.checkGenesets())
			throw new IllegalThreadStateException("No genes in the expression file are found in the GMT file ");

		//if there are two dataset check to see if they have the same set of genes
		if(datasetsAreDistinct(datasets.values())) {
			map.getParams().setDistinctExpressionSets(true);
		}
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		taskMonitor.setStatusMessage("Filtering Gene Sets");
		filterGenesets(taskMonitor);
		taskMonitor.setStatusMessage("");
	}
	
	
	private static boolean datasetsAreDistinct(Collection<DataSet> datasets) {
		Set<Set<Integer>> uniqueGeneSets = new HashSet<>();
		for(DataSet dataset : datasets) {
			Set<Integer> genes = dataset.getDatasetGenes();
			uniqueGeneSets.add(genes);
			if(uniqueGeneSets.size() > 1) {
				return true;
			}
		}
		return false;
	}
	
	
	
	

}
