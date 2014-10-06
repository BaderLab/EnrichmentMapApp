package org.baderlab.csplugins.enrichmentmap.autoannotate.task;

import java.util.List;

import org.baderlab.csplugins.enrichmentmap.EnrichmentMapUtils;
import org.baderlab.csplugins.enrichmentmap.autoannotate.AutoAnnotationManager;
import org.baderlab.csplugins.enrichmentmap.autoannotate.AutoAnnotationParameters;
import org.baderlab.csplugins.enrichmentmap.autoannotate.model.AnnotationSet;
import org.baderlab.csplugins.enrichmentmap.autoannotate.model.Cluster;
import org.baderlab.csplugins.enrichmentmap.autoannotate.view.AutoAnnotationPanel;
import org.cytoscape.model.CyTable;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;

/**
 * Created by
 * User: arkadyark
 * Date: June 17, 2014
 * Time: 11:43 AM
 */

public class AutoAnnotationTaskFactory implements TaskFactory {

	private AutoAnnotationParameters params;
	
	private boolean layout;
	
	public AutoAnnotationTaskFactory(AutoAnnotationParameters params,boolean layout) {
		this.params = params;
		
		this.layout = layout;
	}
	
	public TaskIterator createTaskIterator() {
		TaskIterator currentTasks = new TaskIterator();
		
		AutoAnnotationPanel annotationPanel = AutoAnnotationManager.getInstance().getAnnotationPanel();
		
		annotationPanel.setAnnotating(true);
		
		
		EnrichmentMapUtils.setOverrideHeatmapRevalidation(true);
		
		AnnotationSet annotationSet = new AnnotationSet(params.getName(),params.getNetworkView(),params.getClusterColumnName(), params.getAnnotateColumnName());
		RunClustermakerTask clustertask = new RunClustermakerTask(params);
		currentTasks.append(clustertask);
    	
    	if (layout && params.getNetwork().getDefaultNodeTable().getColumn(params.getClusterColumnName()).getType() != List.class) {
    		// Can't group layout with fuzzy clusters
    		LayoutNetworkTask layouttask = new LayoutNetworkTask(annotationSet,params);
    		currentTasks.append(layouttask);
    	}
    	
    	AnnotateClustersTask annotateClusters = new AnnotateClustersTask(annotationSet, params);
    	currentTasks.append(annotateClusters);
    	
   	
		Long clusterTableSUID = params.getNetwork().getDefaultNetworkTable().getRow(params.getNetwork().getSUID()).get(params.getName(), Long.class);
    	CyTable clusterSetTable = AutoAnnotationManager.getInstance().getTableManager().getTable(clusterTableSUID);

    	// Generate the labels for the clusters
    	for (Cluster cluster : annotationSet.getClusterMap().values()) {
    		currentTasks.append(new UpdateClusterLabelTask(cluster, clusterSetTable));
    	}
    	
    	//Add groups if groups was selected
    	if (annotationSet.usingGroups()) currentTasks.append(new CreateGroupsTask(annotationSet,params));
    	
    	// Add these clusters to the table on the annotationPanel
    	annotationPanel.addClusters(annotationSet,params);
    	annotationPanel.updateSelectedView(params.getNetworkView());
		AutoAnnotationManager.getInstance().getWestPanel().setSelectedIndex(AutoAnnotationManager.getInstance().getWestPanel().indexOfComponent(annotationPanel));
		EnrichmentMapUtils.setOverrideHeatmapRevalidation(false);
		
		// Let the panel know annotating is finished
		annotationPanel.setAnnotating(false);
				
		return currentTasks;
	}
	
	
	public boolean isReady() {
		return true;
	}
	
}
