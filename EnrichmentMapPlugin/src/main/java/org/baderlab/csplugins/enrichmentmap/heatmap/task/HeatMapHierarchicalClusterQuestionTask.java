package org.baderlab.csplugins.enrichmentmap.heatmap.task;

import java.util.HashMap;

import org.baderlab.csplugins.enrichmentmap.EnrichmentMapParameters;
import org.baderlab.csplugins.enrichmentmap.heatmap.HeatMapParameters;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.model.GeneExpression;
import org.baderlab.csplugins.enrichmentmap.view.HeatMapPanel;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;

public class HeatMapHierarchicalClusterQuestionTask extends AbstractTask implements ObservableTask {
	
	@Tunable(description="The combination of the selected gene sets contains more than 1000 genes.  Clustering will take a while.\n  Click OK to cluster set anyways and Cancel to revert to no sorting.")                                              	
	public boolean cluster;
	

    private int numConditions = 0;
    private int numConditions2 = 0;
    
    private HeatMapPanel heatmapPanel;
    private EnrichmentMap map;
    private EnrichmentMapParameters params;
    private HeatMapParameters hmParams;
    
    private TaskMonitor taskMonitor;
	            
	 public HeatMapHierarchicalClusterQuestionTask(int numConditions,
			int numConditions2,
			HeatMapPanel heatmapPanel,
			EnrichmentMap map) {
		super();
		this.numConditions = numConditions;
		this.numConditions2 = numConditions2;
		this.heatmapPanel = heatmapPanel;
		this.map = map;
		this.params = map.getParams();
		this.hmParams = this.params.getHmParams();
	}
				
	@Override
	public void run(TaskMonitor arg0) throws Exception {
		if(cluster){
			HeatMapHierarchicalClusterTask clusterTask = new HeatMapHierarchicalClusterTask(this.numConditions,this.numConditions2,this.heatmapPanel,this.map);
			this.insertTasksAfterCurrentTask(clusterTask);
		}
		else
			hmParams.setSort(HeatMapParameters.Sort.NONE);
			
	}
	
	public void run() throws Exception {
		if(cluster){
			HeatMapHierarchicalClusterTask clusterTask = new HeatMapHierarchicalClusterTask(this.numConditions,this.numConditions2,this.heatmapPanel,this.map);
			this.insertTasksAfterCurrentTask(clusterTask);
		}
		else
			hmParams.setSort(HeatMapParameters.Sort.NONE);						
		
	}

	public <R> R getResults(Class<? extends R> arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	
	
}
