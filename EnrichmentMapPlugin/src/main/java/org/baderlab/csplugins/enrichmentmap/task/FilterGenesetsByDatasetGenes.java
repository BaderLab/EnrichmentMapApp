package org.baderlab.csplugins.enrichmentmap.task;

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
	public void filterGenesets() {
		Map<String, DataSet> datasets = map.getDatasets();
		for(String k : datasets.keySet()) {
			DataSet current_set = datasets.get(k);
			
			//only filter the genesets if dataset genes are not null or empty
			Set<Integer> datasetGenes = current_set.getDatasetGenes();
			if(datasetGenes != null && !datasetGenes.isEmpty()) {
				current_set.getSetofgenesets().filterGenesets(datasetGenes);
			}
		}

		//check to make sure that after filtering there are still genes in the genesets
		//if there aren't any genes it could mean that the IDs don't match or it could mean none
		//of the genes in the expression file are in the specified genesets.
		if(!map.checkGenesets())
			throw new IllegalThreadStateException("No genes in the expression file are found in the GMT file ");

		//if there are two dataset check to see if they have the same set of genes
		if(datasets.size() > 1) {
			Set<Integer> dataset1_genes = datasets.get(EnrichmentMap.DATASET1).getDatasetGenes();
			Set<Integer> dataset2_genes = datasets.get(EnrichmentMap.DATASET2).getDatasetGenes();

			if(!dataset1_genes.equals(dataset2_genes)) {
				map.getParams().setTwoDistinctExpressionSets(true);
			}
		}
	}

	@Override
	public void run(TaskMonitor arg0) throws Exception {
		filterGenesets();
	}

}
