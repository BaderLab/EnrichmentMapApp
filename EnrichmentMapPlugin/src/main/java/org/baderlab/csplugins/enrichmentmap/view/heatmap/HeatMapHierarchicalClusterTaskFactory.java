package org.baderlab.csplugins.enrichmentmap.view.heatmap;

import org.baderlab.csplugins.enrichmentmap.PropertyManager;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;

@Deprecated
public class HeatMapHierarchicalClusterTaskFactory implements TaskFactory {

	private int numConditions = 0;
	private int numConditions2 = 0;

	private HeatMapPanel heatmapPanel;
	private EnrichmentMap map;
	private HeatMapParameters hmParams;
	private PropertyManager propertyManager;

	public HeatMapHierarchicalClusterTaskFactory(int numConditions, int numConditions2, HeatMapPanel heatmapPanel, EnrichmentMap map, HeatMapParameters hmParams, PropertyManager propertyManager) {
		this.numConditions = numConditions;
		this.numConditions2 = numConditions2;
		this.heatmapPanel = heatmapPanel;
		this.map = map;
		this.hmParams = hmParams;
		this.propertyManager = propertyManager;
	}

	public TaskIterator createTaskIterator() {

		TaskIterator clusterIterator = new TaskIterator();

		int size = 0;
		int hierarchicalClusterMax = 1000;
		if(this.heatmapPanel.getCurrentExpressionSet() != null)
			size += this.heatmapPanel.getCurrentExpressionSet().size();
		if(this.heatmapPanel.getCurrentExpressionSet2() != null)
			size += this.heatmapPanel.getCurrentExpressionSet2().size();

		//if there are too many genes then check that the user wants to do clustering
		if(size > hierarchicalClusterMax) {
			HeatMapHierarchicalClusterQuestionTask clusterquesttask = new HeatMapHierarchicalClusterQuestionTask(numConditions, numConditions2, heatmapPanel, map, hmParams, propertyManager);
			clusterIterator.append(clusterquesttask);
		} else {
			HeatMapHierarchicalClusterTask clustertask = new HeatMapHierarchicalClusterTask(numConditions, numConditions2, heatmapPanel, map, hmParams, propertyManager);
			clusterIterator.append(clustertask);
		}

		return clusterIterator;
	}

	public boolean isReady() {
		// TODO Auto-generated method stub
		return true;
	}

}
