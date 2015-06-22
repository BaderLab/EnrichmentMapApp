package org.baderlab.csplugins.enrichmentmap.heatmap.task;

import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.view.HeatMapPanel;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;

public class HeatMapHierarchicalClusterTaskFactory implements TaskFactory {
	

    private int numConditions = 0;
    private int numConditions2 = 0;
    
    private HeatMapPanel heatmapPanel;
    private EnrichmentMap map;
    private CySwingApplication swingApplication;

	public HeatMapHierarchicalClusterTaskFactory(CySwingApplication swingApplication, int numConditions,
			int numConditions2, HeatMapPanel heatmapPanel, EnrichmentMap map) {
		this.swingApplication = swingApplication;
		this.numConditions = numConditions;
		this.numConditions2 = numConditions2;
		this.heatmapPanel = heatmapPanel;
		this.map = map;
	}

	public TaskIterator createTaskIterator() {

		TaskIterator clusterIterator = new TaskIterator();
		
		int size =0;
		int hierarchicalClusterMax = 1000;
		if(this.heatmapPanel.getCurrentExpressionSet() != null)
			size += this.heatmapPanel.getCurrentExpressionSet().size();
		if(this.heatmapPanel.getCurrentExpressionSet2() != null)
			size += this.heatmapPanel.getCurrentExpressionSet2().size();
		       				
		System.out.println("size: " + size + " hierarchicalClusterMax: " + hierarchicalClusterMax);
		//if there are too many genes then check that the user wants to do clustering
		if(size > hierarchicalClusterMax){
			HeatMapHierarchicalClusterQuestionTask clusterquesttask = new HeatMapHierarchicalClusterQuestionTask(swingApplication, this.numConditions,this.numConditions2,this.heatmapPanel,this.map);
			clusterIterator.append(clusterquesttask);
		}
		else{
			HeatMapHierarchicalClusterTask clustertask = new HeatMapHierarchicalClusterTask(this.numConditions,this.numConditions2,this.heatmapPanel,this.map);
			clusterIterator.append(clustertask);
		}
		
		return clusterIterator;
	}

	public boolean isReady() {
		// TODO Auto-generated method stub
		return true;
	}

}
