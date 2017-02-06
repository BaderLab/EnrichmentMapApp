package org.baderlab.csplugins.enrichmentmap.view.heatmap;

import org.baderlab.csplugins.enrichmentmap.PropertyManager;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.util.ListSingleSelection;

@Deprecated
public class HeatMapHierarchicalClusterQuestionTask extends AbstractTask implements ObservableTask {

	//@Tunable(description="<html>The combination of the selected gene sets contains more than 1000 genes.<BR>  Clustering will take a while.<BR>  Would you like to cluster anyways?")                                              	
	//@Tunable(description="The combination of the selected gene sets contains more than 1000 genes.  Clustering will take a while.  Would you like to cluster anyways?")                                              		
	//public boolean cluster;
	@Tunable(description = "<html>The combination of the selected gene sets contains more than 1000 genes.<BR>  Clustering will take a while.  Would you like to cluster anyways?")
	public ListSingleSelection<String> clusterResponse;

	private static String cluster = "Cluster results anyways";
	private static String no_sort = "Do not cluster the results";

	private int numConditions = 0;
	private int numConditions2 = 0;

	private HeatMapPanel heatmapPanel;
	private EnrichmentMap map;
	private HeatMapParameters hmParams;
	
	private PropertyManager propertyManager;

	public HeatMapHierarchicalClusterQuestionTask(int numConditions, int numConditions2, HeatMapPanel heatmapPanel, EnrichmentMap map, HeatMapParameters hmParams, PropertyManager propertyManager) {
		this.numConditions = numConditions;
		this.numConditions2 = numConditions2;
		this.heatmapPanel = heatmapPanel;
		this.map = map;
		this.hmParams = hmParams;
		this.propertyManager = propertyManager;

		clusterResponse = new ListSingleSelection<>(no_sort, cluster);
	}

	@Override
	public void run(TaskMonitor arg0) throws Exception {
		if(clusterResponse.getSelectedValue().equals(cluster)) {
			HeatMapHierarchicalClusterTask clusterTask = new HeatMapHierarchicalClusterTask(numConditions, numConditions2, heatmapPanel, map, hmParams, propertyManager);
			insertTasksAfterCurrentTask(clusterTask);
		} else
			hmParams.setSort(HeatMapParameters.Sort.NONE);

	}

	public void run() throws Exception {
		if(clusterResponse.getSelectedValue().equals(cluster)) {
			HeatMapHierarchicalClusterTask clusterTask = new HeatMapHierarchicalClusterTask(numConditions, numConditions2, heatmapPanel, map, hmParams, propertyManager);
			insertTasksAfterCurrentTask(clusterTask);
		} else
			hmParams.setSort(HeatMapParameters.Sort.NONE);

	}

	public <R> R getResults(Class<? extends R> arg0) {
		// TODO Auto-generated method stub
		return null;
	}

}