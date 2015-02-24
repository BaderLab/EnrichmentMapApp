package org.baderlab.csplugins.enrichmentmap.autoannotate.task;

import javax.swing.JTable;

import org.baderlab.csplugins.enrichmentmap.autoannotate.AutoAnnotationManager;
import org.baderlab.csplugins.enrichmentmap.autoannotate.AutoAnnotationUtils;
import org.baderlab.csplugins.enrichmentmap.autoannotate.model.AnnotationSet;
import org.baderlab.csplugins.enrichmentmap.autoannotate.model.Cluster;
import org.baderlab.csplugins.enrichmentmap.autoannotate.model.ClusterTableModel;
import org.baderlab.csplugins.enrichmentmap.autoannotate.task.cluster.DrawClusterLabelTask;
import org.baderlab.csplugins.enrichmentmap.autoannotate.task.cluster.UpdateClusterLabelTask;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;

public class UpdateAnnotationTask extends AbstractTask{
	private AnnotationSet annotationSet;
		
	public UpdateAnnotationTask(AnnotationSet annotationSet) {
		super();
		this.annotationSet = annotationSet;
	}

	public void updateAction() {
		AutoAnnotationManager autoAnnotationManager = AutoAnnotationManager.getInstance();
		CyNetworkView selectedView = annotationSet.getView();
		CyNetwork selectedNetwork = selectedView.getModel();
		JTable clusterTable = autoAnnotationManager.getAnnotationPanel().getClusterTable(annotationSet);

		for (CyRow row : selectedNetwork.getDefaultNodeTable().getAllRows()) {
			row.set(CyNetwork.SELECTED, false);
		}
		annotationSet.updateCoordinates();
		String annotationSetName = annotationSet.getName();
		Long clusterTableSUID = selectedNetwork.getDefaultNetworkTable().getRow(selectedNetwork.getSUID()).get(annotationSetName, Long.class);
		CyTable clusterSetTable = autoAnnotationManager.getTableManager().getTable(clusterTableSUID);
		
		TaskIterator currentTasks = new TaskIterator();
		for (Cluster cluster : annotationSet.getClusterMap().values()) {
			AutoAnnotationUtils.updateNodeCentralities(cluster);
			// Update the text label of the selected cluster
			String previousLabel = cluster.getLabel();
			currentTasks.append(new UpdateClusterLabelTask(cluster, clusterSetTable));
			if (previousLabel != cluster.getLabel()) {
				// Cluster table needs to be updated with new label
				clusterTable.updateUI();
				cluster.eraseText();

				DrawClusterLabelTask drawlabel = new DrawClusterLabelTask(cluster);
				currentTasks.append(drawlabel);
						
			}
			if (cluster.coordinatesChanged()) {
				// Redraw cluster if necessary
				cluster.erase();
				
				// Redraw selected clusters
				VisualizeClusterAnnotationTaskFactory visualizeCluster = new VisualizeClusterAnnotationTaskFactory(cluster);
				currentTasks.append(visualizeCluster.createTaskIterator());

			}
		}
		//run all the tasks
		AutoAnnotationManager.getInstance().getDialogTaskManager().execute(currentTasks);
		
		// Update the table if the value has changed (WordCloud has been updated)
		((ClusterTableModel)clusterTable.getModel()).updateTable(annotationSet.getClusterMap());
		
		clusterTable.clearSelection();
	}
	
	@Override
	public void run(TaskMonitor arg0) throws Exception {
		updateAction();
		
	}

}
