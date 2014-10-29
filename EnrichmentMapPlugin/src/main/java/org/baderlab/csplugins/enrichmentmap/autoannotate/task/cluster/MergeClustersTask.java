package org.baderlab.csplugins.enrichmentmap.autoannotate.task.cluster;

import java.util.ArrayList;

import javax.swing.JOptionPane;
import javax.swing.JTable;

import org.baderlab.csplugins.enrichmentmap.autoannotate.AutoAnnotationManager;
import org.baderlab.csplugins.enrichmentmap.autoannotate.model.AnnotationSet;
import org.baderlab.csplugins.enrichmentmap.autoannotate.model.Cluster;
import org.baderlab.csplugins.enrichmentmap.autoannotate.model.ClusterTableModel;
import org.baderlab.csplugins.enrichmentmap.autoannotate.task.Observer;
import org.baderlab.csplugins.enrichmentmap.autoannotate.task.UpdateAnnotationTask;
import org.cytoscape.application.swing.CytoPanel;
import org.cytoscape.command.CommandExecutorTaskFactory;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.SynchronousTaskManager;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.swing.DialogTaskManager;

public class MergeClustersTask extends AbstractTask {

	private AnnotationSet annotationSet;
	
	
	
	public MergeClustersTask(AnnotationSet annotationSet) {
		super();
		this.annotationSet = annotationSet;
	}


	public void mergeAction() {
		
		JTable clusterTable = annotationSet.getClusterTable();
		CyNetworkView selectedView = annotationSet.getView();
		CyNetwork selectedNetwork = selectedView.getModel();
		AutoAnnotationManager autoAnnotationManager = AutoAnnotationManager.getInstance();

		int[] selectedRows = clusterTable.getSelectedRows();
		if (selectedRows.length < 2) {
			JOptionPane.showMessageDialog(null, "Please select at least two clusters", "Error Message",
					JOptionPane.ERROR_MESSAGE);
		} else {
			// Get the selected clusters from the selected rows
			ArrayList<Cluster> selectedClusters = new ArrayList<Cluster>();
			for (int rowIndex=0; rowIndex < clusterTable.getRowCount(); rowIndex++) {
				Cluster cluster = (Cluster) clusterTable.getModel().getValueAt(clusterTable.convertRowIndexToModel(rowIndex), 0);
				for (int selectedRow : selectedRows) {
					if (rowIndex == selectedRow) {
						selectedClusters.add(cluster);
						break;
					}
				}
			}
			// Get services needed for accessing WordCloud through command line
			CommandExecutorTaskFactory executor = autoAnnotationManager.getCommandExecutor();
			SynchronousTaskManager<?> syncTaskManager = autoAnnotationManager.getSyncTaskManager();
			DialogTaskManager dialogTaskManager = autoAnnotationManager.getDialogTaskManager();

			// Sets the values in the cluster table to all be the first cluster
			Cluster firstCluster = selectedClusters.get(0);
			int clusterNumber = firstCluster.getClusterNumber();
			String clusterColumnName = annotationSet.getClusterColumnName();
			for (Cluster clusterToSwallow : selectedClusters.subList(1, selectedClusters.size())) {
				// Update values in cluster column
				for (CyNode node : clusterToSwallow.getNodes()) {
					selectedNetwork.getRow(node).set(clusterColumnName, clusterNumber);
				}
				// Swallow nodes/coordinates from smaller cluster
				firstCluster.swallow(clusterToSwallow);
				// Destroy the smaller cluster
				AutoAnnotationManager.getInstance().getDialogTaskManager().execute(new TaskIterator(new DeleteClusterTask(this.annotationSet,clusterToSwallow)));				
				
			}
			
			// Create a new cloud for the merged cluster
			// Clear any previously selected nodes
			for (CyNode node : selectedNetwork.getNodeList()) {
				selectedNetwork.getRow(node).set(CyNetwork.SELECTED, false);
			}
			for (CyNode node : firstCluster.getNodesToCoordinates().keySet()) {
				selectedNetwork.getRow(node).set(CyNetwork.SELECTED, true);
			}
			String nameColumnName = annotationSet.getNameColumnName();
			ArrayList<String> commands = new ArrayList<String>();
			//delete the previous cloud
			//String command_delete = "wordcloud delete cloudName=\"" +  firstCluster.getCloudName() + "\"";
			//commands.add(command_delete);
			//recreate it with the new set of nodes.
			String command = "wordcloud create wordColumnName=\"" + nameColumnName + "\"" + 
					" nodeList=\"selected\" cloudName=\"" +  firstCluster.getCloudName() + "\""
					+ " cloudGroupTableName=\"" + annotationSet.getName() + "\"";
			commands.add(command);
			Observer observer = new Observer();
			TaskIterator taskIterator = executor.createTaskIterator(commands, null);
			dialogTaskManager.execute(taskIterator, observer);
			// Wait until WordCloud is finished
			while (!observer.isFinished()) {
				try {
					Thread.sleep(1);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			}
			for (CyNode node : firstCluster.getNodesToCoordinates().keySet()) {
				selectedNetwork.getRow(node).set(CyNetwork.SELECTED, false);
			}
			
			//update the clusterTable - remove the old entry and put the new one in.
			((ClusterTableModel)clusterTable.getModel()).removeClusterFromTable(firstCluster);
			((ClusterTableModel)clusterTable.getModel()).addClusterToTable(firstCluster);
			
			firstCluster.setCoordinatesChanged(true);
			
			this.insertTasksAfterCurrentTask(new UpdateAnnotationTask(annotationSet));
			
			// Deselect rows (no longer meaningful)
			clusterTable.clearSelection();
			// Focus on this panel
			CytoPanel westPanel = autoAnnotationManager.getWestPanel();
			westPanel.setSelectedIndex(westPanel.indexOfComponent(autoAnnotationManager.getAnnotationPanel()));
		}
	}


	@Override
	public void run(TaskMonitor arg0) throws Exception {
		mergeAction();
		
	}
	
}
