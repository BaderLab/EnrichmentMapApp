package org.baderlab.csplugins.enrichmentmap.autoannotate.action;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

import javax.swing.JOptionPane;
import javax.swing.JTable;


import org.baderlab.csplugins.enrichmentmap.EnrichmentMapManager;
import org.baderlab.csplugins.enrichmentmap.autoannotate.AutoAnnotationManager;
import org.baderlab.csplugins.enrichmentmap.autoannotate.AutoAnnotationParameters;
import org.baderlab.csplugins.enrichmentmap.autoannotate.AutoAnnotationUtils;
import org.baderlab.csplugins.enrichmentmap.autoannotate.model.AnnotationSet;
import org.baderlab.csplugins.enrichmentmap.autoannotate.model.Cluster;
import org.baderlab.csplugins.enrichmentmap.autoannotate.model.ClusterTableModel;
import org.baderlab.csplugins.enrichmentmap.autoannotate.task.Observer;
import org.baderlab.csplugins.enrichmentmap.autoannotate.task.VisualizeClusterAnnotationTaskFactory;
import org.baderlab.csplugins.enrichmentmap.autoannotate.task.cluster.DeleteClusterTask;
import org.baderlab.csplugins.enrichmentmap.autoannotate.task.cluster.DrawClusterLabelTask;
import org.baderlab.csplugins.enrichmentmap.autoannotate.task.cluster.UpdateClusterLabelTask;
import org.baderlab.csplugins.enrichmentmap.heatmap.HeatMapParameters;
import org.cytoscape.application.swing.CytoPanel;
import org.cytoscape.command.CommandExecutorTaskFactory;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.work.SynchronousTaskManager;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.swing.DialogTaskManager;

public class AutoAnnotationActions {

	
	
	public void mergeAction(AnnotationSet annotationSet) {
		
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
				AutoAnnotationManager.getInstance().getDialogTaskManager().execute(new TaskIterator(new DeleteClusterTask(AutoAnnotationManager.getInstance().getAnnotationPanel(),clusterToSwallow)));				
				
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
			String command = "wordcloud create wordColumnName=\"" + nameColumnName + "\"" + 
					" nodesToUse=\"selected\" cloudName=\"" +  firstCluster.getCloudName() + "\""
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
			firstCluster.setCoordinatesChanged(true);
			updateAction(annotationSet);
			// Deselect rows (no longer meaningful)
			clusterTable.clearSelection();
			// Focus on this panel
			CytoPanel westPanel = autoAnnotationManager.getWestPanel();
			westPanel.setSelectedIndex(westPanel.indexOfComponent(autoAnnotationManager.getAnnotationPanel()));
		}
	}

	public void removeAction(AutoAnnotationParameters params) {
		
		AnnotationSet annotationSet = params.getSelectedAnnotationSet();
		CyNetwork selectedNetwork = annotationSet.getView().getModel();

		
		AutoAnnotationManager autoAnnotationManager = AutoAnnotationManager.getInstance();
		EnrichmentMapManager emManager = EnrichmentMapManager.getInstance();
	
		JTable clusterTable = annotationSet.getClusterTable();
		clusterTable.getParent().getParent().getParent().remove(clusterTable.getParent().getParent());
		
		// Prevent heatmap dialog from interrupting this task
		HeatMapParameters heatMapParameters = emManager.getMap(selectedNetwork.getSUID()).getParams().getHmParams();
		if (heatMapParameters != null) {
			heatMapParameters.setSort(HeatMapParameters.Sort.NONE);
		}
		// Delete all annotations
		Iterator<Cluster> clusterIterator = annotationSet.getClusterMap().values().iterator();
		// Iterate over a copy to prevent Concurrent Modification
		ArrayList<Cluster> clusterSetCopy = new ArrayList<Cluster>();
		while (clusterIterator.hasNext()){
			clusterSetCopy.add(clusterIterator.next());
		}
		// Delete each cluster (WordCloud)
		TaskIterator clusterDeletionTasks = new TaskIterator();
		for (Cluster cluster : clusterSetCopy) {
			clusterDeletionTasks.append(new DeleteClusterTask(AutoAnnotationManager.getInstance().getAnnotationPanel(),cluster));				
		}
		AutoAnnotationManager.getInstance().getDialogTaskManager().execute(clusterDeletionTasks);
		params.removeAnnotationSet(annotationSet);			
			
	}
	
	public void updateAction(AnnotationSet annotationSet) {
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
				cluster.setCoordinatesChanged(false);
			}
		}
		//run all the tasks
		AutoAnnotationManager.getInstance().getDialogTaskManager().execute(currentTasks);
		
		// Update the table if the value has changed (WordCloud has been updated)
		((ClusterTableModel)clusterTable.getModel()).updateTable(annotationSet.getClusterMap());
		
		clusterTable.clearSelection();
	}

	public void extractAction(AnnotationSet annotationSet) {
		CyNetworkView selectedView = annotationSet.getView();
		CyNetwork selectedNetwork = selectedView.getModel();
		AutoAnnotationManager autoAnnotationManager = AutoAnnotationManager.getInstance();
		 JTable clusterTable = annotationSet.getClusterTable();
		 
		// Get selected nodes
		ArrayList<CyNode> selectedNodes = new ArrayList<CyNode>();
		for (CyNode node : selectedNetwork.getNodeList()) {
			if (selectedNetwork.getRow(node).get(CyNetwork.SELECTED, Boolean.class)) {
				selectedNodes.add(node);
			}
		}
		// Clear node selections (for WordCloud)
		for (CyNode node : selectedNodes) {
			selectedNetwork.getRow(node).set(CyNetwork.SELECTED, false);
		}
		if (selectedNodes.size() < 1) {
			JOptionPane.showMessageDialog(null, "Please select at least one node", "Error Message",
					JOptionPane.ERROR_MESSAGE);
		} else {
			// Get services needed for accessing WordCloud through command line
			CommandExecutorTaskFactory executor = autoAnnotationManager.getCommandExecutor();
			DialogTaskManager dialogTaskManager = autoAnnotationManager.getDialogTaskManager();
			Class<?> columnType = selectedNetwork.getDefaultNodeTable().getColumn(annotationSet.getClusterColumnName()).getType();
			String clusterColumnName = annotationSet.getClusterColumnName();
			String nameColumnName = annotationSet.getNameColumnName();
			int newClusterNumber = annotationSet.getNextClusterNumber();
			Cluster newCluster = null;
			
			if (columnType == Integer.class) {
				// Discrete clusters, remove nodes from other clusters
				HashSet<Cluster> clustersChanged = new HashSet<Cluster>();
				for (Cluster cluster : annotationSet.getClusterMap().values()) {
					for (CyNode node : selectedNodes) {
						if (cluster.getNodes().contains(node)) {
							cluster.removeNode(node);
							cluster.setCoordinatesChanged(true);
							clustersChanged.add(cluster);
						}
					}
				}
				for (CyRow row : selectedNetwork.getDefaultNodeTable().getAllRows()) {
					row.set(clusterColumnName, newClusterNumber);
				}
				for (Cluster modifiedCluster : clustersChanged) {
					// Select nodes to make the cloud
					for (CyNode node : modifiedCluster.getNodesToCoordinates().keySet()) {
						selectedNetwork.getRow(node).set(CyNetwork.SELECTED, true);
					}
					// Create a new cloud for the extracted cluster
					ArrayList<String> commands = new ArrayList<String>();
					String deleteCommand = "wordcloud delete cloudName=\"" +  modifiedCluster.getCloudName() + "\"";
					String createCommand = "wordcloud create wordColumnName=\"" + nameColumnName + "\"" + 
							" nodesToUse=\"selected\" cloudName=\"" +  modifiedCluster.getCloudName() + "\""
							+ " cloudGroupTableName=\"" + annotationSet.getName() + "\"";
					commands.add(deleteCommand);
					commands.add(createCommand);
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
					// Clear selection
					for (CyNode node : modifiedCluster.getNodesToCoordinates().keySet()) {
						selectedNetwork.getRow(node).set(CyNetwork.SELECTED, false);
					}
					// Ensures that only one cluster changes at a time in the table
					autoAnnotationManager.flushPayloadEvents();
				}
			}
			
			newCluster = new Cluster(newClusterNumber, annotationSet);
			annotationSet.addCluster(newCluster);
			
			for (CyNode node : selectedNodes) {
				// Get coordinates from the nodeView
				View<CyNode> nodeView = selectedView.getNodeView(node);
				double x = nodeView.getVisualProperty(BasicVisualLexicon.NODE_X_LOCATION);
				double y = nodeView.getVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION);
				double[] coordinates = {x, y};
				double nodeRadius = nodeView.getVisualProperty(BasicVisualLexicon.NODE_WIDTH);
				newCluster.addNodeCoordinates(node, coordinates);
				newCluster.addNodeRadius(node, nodeRadius);
				selectedNetwork.getRow(node).set(CyNetwork.SELECTED, true);
			}	

			for (CyNode node : newCluster.getNodesToCoordinates().keySet()) {
				selectedNetwork.getRow(node).set(CyNetwork.SELECTED, true);
			}
			
			// Create a new cloud for the extracted cluster
			ArrayList<String> commands = new ArrayList<String>();
			String command = "wordcloud create wordColumnName=\"" + nameColumnName + "\"" + 
					" nodesToUse=\"selected\" cloudName=\"" +  newCluster.getCloudName() + "\""
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
			for (CyNode node : selectedNodes) {
				selectedNetwork.getRow(node).set(CyNetwork.SELECTED, false);
			}

			CyTableManager tableManager = autoAnnotationManager.getTableManager();
			
			String annotationSetName = annotationSet.getName();
			Long clusterTableSUID = selectedNetwork.getDefaultNetworkTable().getRow(selectedNetwork.getSUID()).get(annotationSetName, Long.class);
	    	CyTable clusterSetTable = tableManager.getTable(clusterTableSUID);
	    	
	    	// Generate the labels for the clusters
	    	AutoAnnotationManager.getInstance().getDialogTaskManager().execute(new TaskIterator(new UpdateClusterLabelTask(newCluster,clusterSetTable)));

			
			// Redraw selected clusters
			VisualizeClusterAnnotationTaskFactory visualizeCluster = new VisualizeClusterAnnotationTaskFactory(newCluster);
			AutoAnnotationManager.getInstance().getDialogTaskManager().execute(visualizeCluster.createTaskIterator());
			
			updateAction(annotationSet);
			
			// Deselect rows (no longer meaningful)
			clusterTable.clearSelection();
			// Focus on this panel
			CytoPanel westPanel = autoAnnotationManager.getWestPanel();
			westPanel.setSelectedIndex(westPanel.indexOfComponent(autoAnnotationManager.getAnnotationPanel()));
		}
	}
}
