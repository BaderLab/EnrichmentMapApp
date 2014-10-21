package org.baderlab.csplugins.enrichmentmap.autoannotate.task;

import java.util.List;

import javax.swing.JOptionPane;

import org.baderlab.csplugins.enrichmentmap.EnrichmentMapUtils;
import org.baderlab.csplugins.enrichmentmap.autoannotate.AutoAnnotationManager;
import org.baderlab.csplugins.enrichmentmap.autoannotate.AutoAnnotationParameters;
import org.baderlab.csplugins.enrichmentmap.autoannotate.model.AnnotationSet;
import org.baderlab.csplugins.enrichmentmap.autoannotate.model.Cluster;
import org.baderlab.csplugins.enrichmentmap.autoannotate.view.AutoAnnotationPanel;
import org.baderlab.csplugins.enrichmentmap.task.BuildEnrichmentMapDummyTask;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyTable;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;

/**
 * Created by
 * User: arkadyark
 * Date: June 17, 2014
 * Time: 11:43 AM
 */

public class AutoAnnotationTask extends AbstractTask {

	private AutoAnnotationParameters params;
	
	private AutoAnnotationPanel autoAnnotationPanel;
		
	public AutoAnnotationTask(AutoAnnotationParameters params) {
		this.params = params;		
	}
	
	public void run(TaskMonitor taskMonitor) throws Exception {
		//get the current View
		this.autoAnnotationPanel = AutoAnnotationManager.getInstance().getAnnotationPanel();
				
		CyNetworkView selectedView = this.autoAnnotationPanel.getCurrentView();
					
		if (selectedView == null) {
			JOptionPane.showMessageDialog(null, "Load an Enrichment Map", "Error Message", JOptionPane.ERROR_MESSAGE);
		} else {									
			String clusterColumnName = null;
			String algorithm = null;
			if (this.autoAnnotationPanel.isClusterMaker()) {
				// using clusterMaker algorithms
				algorithm = this.autoAnnotationPanel.getAlgorithm();
				clusterColumnName = this.autoAnnotationPanel.getClusterColumnName(params);
			} else {
				// using a user specified column
				clusterColumnName = this.autoAnnotationPanel.getClusterColumnName(params);
			}
			String annotationSetName = params.nextAnnotationSetName(algorithm, clusterColumnName);
						
			//update parameters for this annotation set
			if(algorithm != null)
				this.params.setAlgorithm(algorithm);
			this.params.setName(annotationSetName);
			this.params.setClusterColumnName(clusterColumnName);
			this.params.setGroups(this.autoAnnotationPanel.isGroupsSelected());
			this.params.setAnnotateColumnName(this.autoAnnotationPanel.getAnnotationColumnName());
						
			//Annotate the network in 5 step.  Each step needs to be completed before proceeds so can't have them all 
			//in one task iterator
			//AutoAnnotationPanel annotationPanel = AutoAnnotationManager.getInstance().getAnnotationPanel();
			this.autoAnnotationPanel.setAnnotating(true);
						
			//Turn off automatic heatmap on selection
			EnrichmentMapUtils.setOverrideHeatmapRevalidation(true);
						
			AnnotationSet annotationSet = new AnnotationSet(this.params.getName(),this.params.getNetworkView(),this.params.getClusterColumnName(), this.params.getAnnotateColumnName());

			//Step 0 - add dummy task to bring up the dialog box
			TaskIterator currentTasks = new TaskIterator();
			
			new BuildEnrichmentMapDummyTask("Annotating Enrichment Map").run(taskMonitor);
			taskMonitor.setTitle("Annotating Enrichment Map");
			
			//AutoAnnotationManager.getInstance().getDialogTaskManager().execute(currentTasks);
			Observer observer;
			//step 1a - cluster
			if(this.params.getAlgorithm() != null){
				taskMonitor.setTitle("Clustering Nodes...");
				observer = new Observer();
				AutoAnnotationManager.getInstance().getDialogTaskManager().execute(new RunClustermakerTaskFactory(this.params).createTaskIterator(),observer);
				
				//currentTasks.append(new RunClustermakerTask(this.params));
				waitTilTaskIsDone(observer);
			}
				
			//step 1b - make clusters for annotating
			taskMonitor.setTitle("Creating Clusters...");
			//currentTasks.append(new CreateClustersTask(annotationSet, this.params));
			observer = new Observer();						
			AutoAnnotationManager.getInstance().getDialogTaskManager().execute(new TaskIterator(new CreateClustersTask(annotationSet, this.params)),observer);
			waitTilTaskIsDone(observer);
			
			//step2 - annotate the network create word clouds
			taskMonitor.setTitle("Calculating annotations...");
			//AnnotateClustersTaskFactory annotateClusters = new AnnotateClustersTaskFactory(annotationSet, this.params);
			//currentTasks.append(annotateClusters.createTaskIterator());
			observer = new Observer();			
			AutoAnnotationManager.getInstance().getDialogTaskManager().execute(new AnnotateClustersTaskFactory(annotationSet, this.params).createTaskIterator(),observer );			
			waitTilTaskIsDone(observer);
			
			//step3 - layout network
			if (this.autoAnnotationPanel.isLayoutNodeSelected() && this.params.getNetwork().getDefaultNodeTable().getColumn(params.getClusterColumnName()).getType() != List.class) {
				// Can't group layout with fuzzy clusters			
				taskMonitor.setTitle("Laying Out Network...");
				LayoutNetworkTask layouttask = new LayoutNetworkTask(annotationSet,this.params);
				currentTasks.append(layouttask);
				//observer = new Observer();
				//AutoAnnotationManager.getInstance().getDialogTaskManager().execute(new TaskIterator(new LayoutNetworkTask(annotationSet,this.params)),observer );				
				//waitTilTaskIsDone(observer);
			}
				
			//step3 - annotate the network
			//taskMonitor.setTitle("Annotating clusters...");
			//AnnotateClustersTaskFactory annotateClusters = new AnnotateClustersTaskFactory(annotationSet, this.params);
			//currentTasks.append(annotateClusters.createTaskIterator());
			//observer = new Observer();			
			//AutoAnnotationManager.getInstance().getDialogTaskManager().execute(new AnnotateClustersTaskFactory(annotationSet, this.params).createTaskIterator(),observer );			
			//waitTilTaskIsDone(observer);
				
			// Generate the labels for the clusters
			ComputeClusterLabelsTask computeLabels = new ComputeClusterLabelsTask(annotationSet,this.params);
			currentTasks.append(computeLabels);
			//observer = new Observer();
			//AutoAnnotationManager.getInstance().getDialogTaskManager().execute(new TaskIterator(new ComputeClusterLabelsTask(annotationSet,this.params)),observer );
			//waitTilTaskIsDone(observer);
				
			//Step 4 - create groups
			//Add groups if groups was selected
			if (annotationSet.usingGroups()) {				
				taskMonitor.setTitle("Creating Groups...");
				currentTasks.append(new CreateGroupsTask(annotationSet,this.params));
				//observer = new Observer();
				//AutoAnnotationManager.getInstance().getDialogTaskManager().execute(new TaskIterator(new CreateGroupsTask(annotationSet,this.params)),observer );
				//waitTilTaskIsDone(observer);
			}
			//Step 5 - clean up
			// Add these clusters to the table on the annotationPanel
			taskMonitor.setTitle("Cleaning Up...");
			currentTasks.append(new UpdateAnnotationPanelTask(annotationSet, this.params));
			//observer = new Observer();
			//AutoAnnotationManager.getInstance().getDialogTaskManager().execute(new TaskIterator(new UpdateAnnotationPanelTask(annotationSet, this.params)),observer );
			//waitTilTaskIsDone(observer);
			AutoAnnotationManager.getInstance().getDialogTaskManager().execute(currentTasks);
						
		}	
		
	}
	
	private void waitTilTaskIsDone(Observer observer){
		while (!observer.isFinished()) {
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}			
		}
	}
	
}
