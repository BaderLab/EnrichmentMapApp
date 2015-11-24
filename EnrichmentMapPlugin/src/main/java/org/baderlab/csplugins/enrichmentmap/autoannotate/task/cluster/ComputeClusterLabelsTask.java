package org.baderlab.csplugins.enrichmentmap.autoannotate.task.cluster;

import org.baderlab.csplugins.enrichmentmap.autoannotate.AutoAnnotationManager;
import org.baderlab.csplugins.enrichmentmap.autoannotate.AutoAnnotationParameters;
import org.baderlab.csplugins.enrichmentmap.autoannotate.model.AnnotationSet;
import org.baderlab.csplugins.enrichmentmap.autoannotate.model.Cluster;
import org.cytoscape.model.CyTable;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;

public class ComputeClusterLabelsTask extends AbstractTask {
	
	private AnnotationSet annotationSet;
	private AutoAnnotationParameters params;

	private TaskMonitor taskMonitor;
	
	public ComputeClusterLabelsTask(AnnotationSet annotationSet,
			AutoAnnotationParameters params) {
		super();
		this.annotationSet = annotationSet;
		this.params = params;
	}

	
	@Override
	public void run(TaskMonitor arg0) throws Exception {
		Long clusterTableSUID = params.getNetwork().getDefaultNetworkTable().getRow(params.getNetwork().getSUID()).get(params.getName(), Long.class);
		CyTable clusterSetTable = AutoAnnotationManager.getInstance().getTableManager().getTable(clusterTableSUID);

		TaskIterator currentTasks = new TaskIterator();
		
		// Generate the labels for the clusters
		for (Cluster cluster : annotationSet.getClusterMap().values()) {
		    currentTasks.append(new UpdateClusterLabelTask(cluster, clusterSetTable));
		}
		AutoAnnotationManager.getInstance().getDialogTaskManager().execute(currentTasks);
	}
	
	

}
