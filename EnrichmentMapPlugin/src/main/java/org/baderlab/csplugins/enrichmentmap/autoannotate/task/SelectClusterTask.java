package org.baderlab.csplugins.enrichmentmap.autoannotate.task;

import java.awt.Color;
import java.util.ArrayList;

import org.baderlab.csplugins.enrichmentmap.autoannotate.AutoAnnotationManager;
import org.baderlab.csplugins.enrichmentmap.autoannotate.model.Cluster;
import org.cytoscape.command.CommandExecutorTaskFactory;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.SynchronousTaskManager;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;

public class SelectClusterTask extends AbstractTask{
	
	private Cluster cluster;
	//if selection is true then select cluster.  If selection is false then de-select cluster
	private boolean selection = true;
	
	private TaskMonitor taskMonitor =null;
	
	public SelectClusterTask(Cluster cluster, boolean selection) {
		super();
		this.cluster = cluster;
		this.selection = selection;
	}

	public void selectCluster() {
		if (!cluster.isSelected()) {
			CyNetwork network = cluster.getParent().getView().getModel();
			AutoAnnotationManager autoAnnotationManager = AutoAnnotationManager.getInstance();
			autoAnnotationManager.flushPayloadEvents();
			// Wait for heatmap to finish updating
			boolean heatMapUpdating = true;
			while (heatMapUpdating) {
				heatMapUpdating = autoAnnotationManager.isHeatMapUpdating();
			}
			cluster.setSelected(true);
			// Select node(s) in the cluster
			if (cluster.isCollapsed()) {
				network.getRow(cluster.getGroupNode()).set(CyNetwork.SELECTED, true);
			} else {
				for (CyNode node : cluster.getNodes()) {
					network.getRow(node).set(CyNetwork.SELECTED, true);
				}
			}
			// Select the corresponding WordCloud through command line
			ArrayList<String> commands = new ArrayList<String>();
			String command = "wordcloud select cloudName=\"" + cluster.getCloudName() + "\"";
			commands.add(command);
			TaskIterator task = AutoAnnotationManager.getInstance().getCommandExecutor().createTaskIterator(commands, null);
			AutoAnnotationManager.getInstance().getSyncTaskManager().execute(task);
			// Select the annotations (ellipse and text label)
			cluster.getEllipse().setBorderColor(Color.YELLOW);
			cluster.getEllipse().setBorderWidth(3*cluster.getParent().getEllipseWidth());
			cluster.getTextAnnotation().setTextColor(Color.YELLOW);
		}
	}

	public void deselectCluster() {
		if (cluster.isSelected()) {
			CyNetwork network = cluster.getParent().getView().getModel();
			int ellipseBorderWidth = cluster.getParent().getEllipseWidth();
			cluster.setSelected(false);
			// Deselect node(s) in the cluster
			if (cluster.isCollapsed()) {
				network.getRow(cluster.getGroupNode()).set(CyNetwork.SELECTED, false);
			} else {
				for (CyNode node : cluster.getNodes()) {
					network.getRow(node).set(CyNetwork.SELECTED, false);
				}
			}
			// Deselect the annotations
			cluster.getEllipse().setBorderColor(Color.DARK_GRAY);
			cluster.getEllipse().setBorderWidth(ellipseBorderWidth);
			cluster.getTextAnnotation().setTextColor(Color.BLACK);
			cluster.getParent().updateCoordinates();
			if (cluster.coordinatesChanged()) {
				cluster.erase();
				
				// Redraw deselected clusters
				VisualizeClusterAnnotationTaskFactory visualizeCluster = new VisualizeClusterAnnotationTaskFactory(cluster);
				AutoAnnotationManager.getInstance().getDialogTaskManager().execute(visualizeCluster.createTaskIterator());
				
				cluster.setCoordinatesChanged(false);
			}
		}
	}



	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		this.taskMonitor = taskMonitor;
		
		if(selection)
			selectCluster();
		else	
			deselectCluster();
	}
	
	

}
