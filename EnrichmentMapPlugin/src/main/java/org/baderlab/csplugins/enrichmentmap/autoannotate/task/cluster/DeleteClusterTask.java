package org.baderlab.csplugins.enrichmentmap.autoannotate.task.cluster;

import java.util.ArrayList;

import javax.swing.JOptionPane;
import javax.swing.JTable;

import org.baderlab.csplugins.enrichmentmap.autoannotate.AutoAnnotationManager;
import org.baderlab.csplugins.enrichmentmap.autoannotate.AutoAnnotationParameters;
import org.baderlab.csplugins.enrichmentmap.autoannotate.model.AnnotationSet;
import org.baderlab.csplugins.enrichmentmap.autoannotate.model.Cluster;
import org.baderlab.csplugins.enrichmentmap.autoannotate.model.ClusterTableModel;
import org.baderlab.csplugins.enrichmentmap.autoannotate.task.UpdateAnnotationsTaskFactory;
import org.baderlab.csplugins.enrichmentmap.autoannotate.view.AutoAnnotationPanel;
import org.cytoscape.application.swing.CytoPanel;
import org.cytoscape.command.CommandExecutorTaskFactory;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.SynchronousTaskManager;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;

public class DeleteClusterTask extends AbstractTask {
	
	private AutoAnnotationParameters params;
	private AnnotationSet annotationSet;
	private Cluster currentCluster;

	public DeleteClusterTask(AutoAnnotationParameters params) {
		super();
		this.params = params;
	}
	
	public DeleteClusterTask(AutoAnnotationParameters params, Cluster currentCluster) {
		super();
		this.params = params;
		this.currentCluster = currentCluster;
	}
	
	public DeleteClusterTask(AnnotationSet annotationSet, Cluster currentCluster) {
		super();
		this.annotationSet = annotationSet;
		this.currentCluster = currentCluster;
	}
	
	
	@Override
	public void run(TaskMonitor arg0) throws Exception {
		if(this.annotationSet == null)
			this.annotationSet = this.params.getSelectedAnnotationSet();
		//If there is no cluster specified this task has been called directly from the Delete Button Action Listener
		if(this.currentCluster == null)
			deleteAction();
		//if the task has been initialized with a cluster then we can just delete the cluster - used by the remove annotation command and merge command
		else
			destroyCluster(this.currentCluster);
	}	
	
	public void deleteAction() {
		
		JTable clusterTable = annotationSet.getClusterTable();
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
						
			// Delete clusters
			for (Cluster cluster : selectedClusters) {
				destroyCluster(cluster);
				//remove the cluster from the Table
				((ClusterTableModel)this.annotationSet.getClusterTable().getModel()).removeClusterFromTable(cluster);
			}
			
			//update the Annotations
			autoAnnotationManager.getDialogTaskManager().execute(new UpdateAnnotationsTaskFactory(annotationSet).createTaskIterator());
			
			// Focus on this panel
			CytoPanel westPanel = autoAnnotationManager.getWestPanel();
			westPanel.setSelectedIndex(westPanel.indexOfComponent(autoAnnotationManager.getAnnotationPanel()));
		}
	}
	
	public void destroyCluster(Cluster clusterToDestroy) {
		//delete the cloud from wordcloud
		destroyCloud(clusterToDestroy);
		// Erase the annotations
		clusterToDestroy.erase();
		// Remove the cluster from the annotation set
		//If removing the whole annotation set (using Remove annotation button)
		//it possible that the annotation set has already been 
		//removed.  Check to make sure it isn't null
		if(this.annotationSet != null)
			this.annotationSet.getClusterMap().remove(clusterToDestroy.getClusterNumber());
	}
	
	public void destroyCloud(Cluster clusterToDestroy) {
		// Get services needed for accessing WordCloud through command line
		AutoAnnotationManager autoAnnotationManager = AutoAnnotationManager.getInstance();
		CommandExecutorTaskFactory executor = autoAnnotationManager.getCommandExecutor();
		SynchronousTaskManager<?> syncTaskManager = autoAnnotationManager.getSyncTaskManager();
		
		// Delete the WordCloud through the command line
		ArrayList<String> commands = new ArrayList<String>();
		String command = "wordcloud delete cloudName=\"" + clusterToDestroy.getCloudName() + "\"";
		commands.add(command);
		TaskIterator task = executor.createTaskIterator(commands, null);
		syncTaskManager.execute(task);
	}

	

	
	
}
