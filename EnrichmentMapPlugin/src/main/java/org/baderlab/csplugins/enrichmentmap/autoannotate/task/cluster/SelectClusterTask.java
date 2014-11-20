package org.baderlab.csplugins.enrichmentmap.autoannotate.task.cluster;

import java.awt.Color;
import java.util.ArrayList;

import org.baderlab.csplugins.enrichmentmap.autoannotate.AutoAnnotationManager;
import org.baderlab.csplugins.enrichmentmap.autoannotate.model.Cluster;
import org.baderlab.csplugins.enrichmentmap.autoannotate.task.VisualizeClusterAnnotationTaskFactory;
import org.cytoscape.command.CommandExecutorTaskFactory;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.SynchronousTaskManager;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;

public class SelectClusterTask extends AbstractTask{
	
	//There are 4 different types of selection
	// 0. - select cluster annotation and nodes belonging to cluster - called when user clicks on cluster in cluster table.
	// 1. - deselect cluster annotation and nodes belonging to cluster - called when user clicks on cluster in cluster table.
	// 2. - select cluster annotation and but do not select nodes belonging to cluster - called when user selects nodes in the network
	// 3. - deselect cluster annotation and but do not select nodes belonging to cluster - called when user selects nodes in the network
	final public static int SELECTCLUSTER_WITHNODES = 0,  DESELECTCLUSTER_WITHNODES = 1, SELECTCLUSTER_NONODES  = 2, DESELECTCLUSTER_NONODES = 3,SELECTCLUSTER_NONODES_NOCLOUD  = 4; 
	
	private Cluster cluster;
	//if selection is true then select cluster.  If selection is false then de-select cluster
	private int selection = SELECTCLUSTER_WITHNODES ;
	
	private TaskMonitor taskMonitor =null;
	
	public SelectClusterTask(Cluster cluster, int selection) {
		super();
		this.cluster = cluster;
		this.selection = selection;
	}

	private void selectCluster(Cluster cluster){
		
		// Select the annotations (ellipse and text label)
		if(cluster.getEllipse() != null){
			cluster.getEllipse().setBorderColor(Color.YELLOW);
			cluster.getEllipse().setBorderWidth(3*cluster.getParent().getEllipseWidth());
		}
		if(cluster.getTextAnnotation() != null)
			cluster.getTextAnnotation().setTextColor(Color.YELLOW);
		
	}
	
	private void deselectCluster(Cluster cluster){
		// Deselect the annotations
		if(cluster.getEllipse() != null){
			cluster.getEllipse().setBorderColor(Color.DARK_GRAY);
			cluster.getEllipse().setBorderWidth(cluster.getParent().getEllipseWidth());
		}
		if(cluster.getTextAnnotation() != null)
			cluster.getTextAnnotation().setTextColor(Color.BLACK);
		cluster.getParent().updateCoordinates();
		if (cluster.coordinatesChanged()) {
			cluster.erase();
						
			// Redraw deselected clusters
			VisualizeClusterAnnotationTaskFactory visualizeCluster = new VisualizeClusterAnnotationTaskFactory(cluster);
			AutoAnnotationManager.getInstance().getDialogTaskManager().execute(visualizeCluster.createTaskIterator());
						
		}
	}
	
	private void updateCloud(String command){
		// Select the corresponding WordCloud through command line
		ArrayList<String> commands = new ArrayList<String>();
		commands.add(command);
		TaskIterator task = AutoAnnotationManager.getInstance().getCommandExecutor().createTaskIterator(commands, null);
		AutoAnnotationManager.getInstance().getSyncTaskManager().execute(task);
	}
	
	public void selectCluster_withnodes() {
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
			
			updateCloud( "wordcloud select cloudName=\"" + cluster.getCloudName() + "\"");
			selectCluster(cluster);			
			
		}
	}

	public void deselectCluster_withnodes() {
		if (cluster.isSelected()) {
			CyNetwork network = cluster.getParent().getView().getModel();
			cluster.setSelected(false);
			// Deselect node(s) in the cluster
			if (cluster.isCollapsed()) {
				network.getRow(cluster.getGroupNode()).set(CyNetwork.SELECTED, false);
			} else {
				for (CyNode node : cluster.getNodes()) {
					network.getRow(node).set(CyNetwork.SELECTED, false);
				}
			}
			deselectCluster(cluster);
		}
	}

	public void selectCluster_nonodes() {
		if (!cluster.isSelected()) {

			AutoAnnotationManager autoAnnotationManager = AutoAnnotationManager.getInstance();
			autoAnnotationManager.flushPayloadEvents();
			// Wait for heatmap to finish updating
			boolean heatMapUpdating = true;
			while (heatMapUpdating) {
				heatMapUpdating = autoAnnotationManager.isHeatMapUpdating();
			}
			cluster.setSelected(true);
			System.out.println( "wordcloud select cloudName=\"" + cluster.getCloudName() + "\" updateNodeSelection=false");
			updateCloud( "wordcloud select cloudName=\"" + cluster.getCloudName() + "\" updateNodeSelection=false");
			selectCluster(cluster);			
			
		}
	}
	
	public void selectCluster_nonodes_nocloud() {
		if (!cluster.isSelected()) {

			AutoAnnotationManager autoAnnotationManager = AutoAnnotationManager.getInstance();
			autoAnnotationManager.flushPayloadEvents();
			// Wait for heatmap to finish updating
			boolean heatMapUpdating = true;
			while (heatMapUpdating) {
				heatMapUpdating = autoAnnotationManager.isHeatMapUpdating();
			}
			cluster.setSelected(true);
			
			selectCluster(cluster);			
			
		}
	}
	
	public void deselectCluster_nonodes() {
		if (cluster.isSelected()) {
			cluster.setSelected(false);			
			deselectCluster(cluster);
		}
	}

	


	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		this.taskMonitor = taskMonitor;
		
		if(this.selection == SELECTCLUSTER_WITHNODES)
			selectCluster_withnodes();
		else if(this.selection == DESELECTCLUSTER_WITHNODES)	
			deselectCluster_withnodes();
		else if(this.selection == DESELECTCLUSTER_NONODES)	
			deselectCluster_nonodes();
		else if(this.selection == SELECTCLUSTER_NONODES)	
			selectCluster_nonodes();
		else if(this.selection == SELECTCLUSTER_NONODES_NOCLOUD)	
			selectCluster_nonodes_nocloud();
		
	}
	
	

}
