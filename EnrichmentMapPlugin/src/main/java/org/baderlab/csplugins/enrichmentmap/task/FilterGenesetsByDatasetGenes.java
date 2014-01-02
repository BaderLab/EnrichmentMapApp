package org.baderlab.csplugins.enrichmentmap.task;

import java.util.HashMap;
import java.util.Iterator;

import org.baderlab.csplugins.enrichmentmap.model.DataSet;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

public class FilterGenesetsByDatasetGenes extends AbstractTask {

	private EnrichmentMap map;
		
	public FilterGenesetsByDatasetGenes(EnrichmentMap map) {
		super();
		this.map = map;
	}


	/*
     * Filter all the genesets by the dataset genes
     * If there are multiple sets of genesets make sure to filter by the specific dataset genes
     */
    public void filterGenesets(){
    	HashMap<String, DataSet> datasets = this.map.getDatasets();
    	for(Iterator<String> k = datasets.keySet().iterator(); k.hasNext();){
        	DataSet current_set = datasets.get(k.next());
        	//only filter the genesets if dataset genes are not null or empty
        	if(current_set.getDatasetGenes() != null && !current_set.getDatasetGenes().isEmpty())
        		current_set.getSetofgenesets().filterGenesets(current_set.getDatasetGenes());
        	
    	}
    	
    	//check to make sure that after filtering there are still genes in the genesets
        //if there aren't any genes it could mean that the IDs don't match or it could mean none
        //of the genes in the expression file are in the specified genesets.
        if(!map.checkGenesets())
                throw new IllegalThreadStateException("No genes in the expression file are found in the GMT file ");

    }


	@Override
	public void run(TaskMonitor arg0) throws Exception {
		
		
	}

}
