package org.baderlab.csplugins.enrichmentmap.task;

import org.baderlab.csplugins.enrichmentmap.EnrichmentMapParameters;
import org.baderlab.csplugins.enrichmentmap.model.DataSet;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.cytoscape.io.util.StreamUtil;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;

public class LoadInputDataTaskFactory implements TaskFactory{

	private TaskIterator loadIterator;
	
	private EnrichmentMap map;
	private StreamUtil streamUtil;
			
	public LoadInputDataTaskFactory(EnrichmentMap map, StreamUtil streamUtil) {
		super();
		this.map = map;
		this.streamUtil = streamUtil;
	}

	public void load(){
		//Load in the first dataset
		//call it Dataset 1.
		DataSet dataset = map.getDataset(EnrichmentMap.DATASET1);    		
		
		//Get all user parameters
		EnrichmentMapParameters params = map.getParams();
		
		//Load Dataset
			LoadDataSetTask loaddata = new LoadDataSetTask(dataset,streamUtil);
			loadIterator.append(loaddata.getIterator());
			
			if(map.getParams().isTwoDatasets() && map.getDatasets().containsKey(EnrichmentMap.DATASET2)){
				DataSet dataset2 = map.getDataset(EnrichmentMap.DATASET2);
				
				LoadDataSetTask loaddataset2 = new LoadDataSetTask(dataset2,streamUtil);
				loadIterator.append(loaddataset2.getIterator());
    				params.setData2(true);
    			
    			//check to see if the two datasets are distinct
    			if(!(
    					(dataset.getDatasetGenes().containsAll(dataset2.getDatasetGenes())) && 
    					(dataset2.getDatasetGenes().containsAll(dataset.getDatasetGenes()))
    					))
    				params.setTwoDistinctExpressionSets(true);
    				
				
			}
	}
	
	public TaskIterator createTaskIterator() {
		// TODO Auto-generated method stub
		this.loadIterator = new TaskIterator();
		load();
		return this.loadIterator;
	}

	public boolean isReady() {
		// TODO Auto-generated method stub
		return true;
	}

}
