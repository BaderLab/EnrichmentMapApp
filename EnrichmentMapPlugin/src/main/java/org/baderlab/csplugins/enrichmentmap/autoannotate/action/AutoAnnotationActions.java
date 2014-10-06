package org.baderlab.csplugins.enrichmentmap.autoannotate.action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import org.baderlab.csplugins.enrichmentmap.EnrichmentMapManager;
import org.baderlab.csplugins.enrichmentmap.EnrichmentMapUtils;
import org.baderlab.csplugins.enrichmentmap.autoannotate.AutoAnnotationManager;
import org.baderlab.csplugins.enrichmentmap.autoannotate.AutoAnnotationParameters;
import org.baderlab.csplugins.enrichmentmap.autoannotate.AutoAnnotationUtils;
import org.baderlab.csplugins.enrichmentmap.autoannotate.model.AnnotationSet;
import org.baderlab.csplugins.enrichmentmap.autoannotate.model.Cluster;
import org.baderlab.csplugins.enrichmentmap.autoannotate.task.AnnotateClustersTask;
import org.baderlab.csplugins.enrichmentmap.autoannotate.task.AutoAnnotationTaskFactory;
import org.baderlab.csplugins.enrichmentmap.autoannotate.task.CreateClustersTask;
import org.baderlab.csplugins.enrichmentmap.autoannotate.task.RunClustermakerTask;
import org.baderlab.csplugins.enrichmentmap.autoannotate.task.CreateGroupsTask;
import org.baderlab.csplugins.enrichmentmap.autoannotate.task.DrawClusterLabelTask;
import org.baderlab.csplugins.enrichmentmap.autoannotate.task.LayoutNetworkTask;
import org.baderlab.csplugins.enrichmentmap.autoannotate.task.Observer;
import org.baderlab.csplugins.enrichmentmap.autoannotate.task.UpdateClusterLabelTask;
import org.baderlab.csplugins.enrichmentmap.autoannotate.task.VisualizeClusterAnnotationTaskFactory;
import org.baderlab.csplugins.enrichmentmap.autoannotate.view.AutoAnnotationPanel;
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

	public static void deleteAction(AnnotationSet annotationSet, JTable clusterTable) {
		
		int[] selectedRows = clusterTable.getSelectedRows();
		if (selectedRows.length < 1) {
			JOptionPane.showMessageDialog(null, "Please select at least one cluster", "Error Message",
					JOptionPane.ERROR_MESSAGE);
		} else {
			AutoAnnotationManager autoAnnotationManager = AutoAnnotationManager.getInstance();
			
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
			// Delete clusters
			for (Cluster cluster : selectedClusters) {
				AutoAnnotationUtils.destroyCluster(cluster, executor, syncTaskManager);
			}
			updateAction(annotationSet);
			// Focus on this panel
			CytoPanel westPanel = autoAnnotationManager.getWestPanel();
			westPanel.setSelectedIndex(westPanel.indexOfComponent(autoAnnotationManager.getAnnotationPanel()));
		}
	}
	
	public static void mergeAction(AnnotationSet annotationSet, JTable clusterTable) {
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
				AutoAnnotationUtils.destroyCluster(clusterToSwallow, executor, syncTaskManager);
			}
			// Destroy the cloud for the first cluster
			AutoAnnotationUtils.destroyCloud(firstCluster, executor, syncTaskManager);
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

	public static void removeAction(JComboBox<AnnotationSet> clusterSetDropdown, 
			HashMap<AnnotationSet, JTable> clustersToTables,
			AutoAnnotationParameters params) {
		
		AnnotationSet annotationSet = params.getSelectedAnnotationSet();
		CyNetwork selectedNetwork = annotationSet.getView().getModel();

		int confirmation = JOptionPane.showConfirmDialog(null, "Are you sure you want to remove this annotation set? This cannot be undone.",
				"Remove confirmation", JOptionPane.YES_NO_OPTION);
		
		if (confirmation == JOptionPane.YES_OPTION) {
			AutoAnnotationManager autoAnnotationManager = AutoAnnotationManager.getInstance();
			EnrichmentMapManager emManager = EnrichmentMapManager.getInstance();
	
			JTable clusterTable = clustersToTables.get(annotationSet);
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
			for (Cluster cluster : clusterSetCopy) {
				AutoAnnotationUtils.destroyCluster(cluster, autoAnnotationManager.getCommandExecutor(), autoAnnotationManager.getSyncTaskManager());
			}
			params.removeAnnotationSet(annotationSet);
			clustersToTables.remove(annotationSet);
			// Remove cluster set from dropdown
			clusterSetDropdown.removeItem(annotationSet);
		}
	}
	
	public static void updateAction(AnnotationSet annotationSet) {
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
		DefaultTableModel model = (DefaultTableModel) clusterTable.getModel();
		int i = 0;
		int numRows = model.getRowCount();
		for (Cluster cluster : annotationSet.getClusterMap().values()) {
			if (i >= numRows) {
				Object[] newRow = {cluster, cluster.getSize()};
				model.addRow(newRow);
			} else {
				if (!(model.getValueAt(i, 0).equals(cluster))) {
					model.setValueAt(cluster, i, 0);
					model.setValueAt(cluster.getSize(), i, 1);
				} else if (!(model.getValueAt(i,  1)).equals(cluster.getSize())) {
					// Cluster hasn't changed but size has
					model.setValueAt(cluster.getSize(), i, 1);
				}
				i++;
			}
		}
		// Remove rows left over at the end (deletion)
		while (numRows > i) {
			model.removeRow(i);
			numRows = model.getRowCount();
		}
		clusterTable.clearSelection();
	}

	public static void extractAction(AnnotationSet annotationSet, JTable clusterTable) {
		CyNetworkView selectedView = annotationSet.getView();
		CyNetwork selectedNetwork = selectedView.getModel();
		AutoAnnotationManager autoAnnotationManager = AutoAnnotationManager.getInstance();

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
