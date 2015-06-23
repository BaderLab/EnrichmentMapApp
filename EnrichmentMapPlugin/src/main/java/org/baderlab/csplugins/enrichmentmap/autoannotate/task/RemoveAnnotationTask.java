package org.baderlab.csplugins.enrichmentmap.autoannotate.task;

import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.JTable;

import org.baderlab.csplugins.enrichmentmap.EnrichmentMapManager;
import org.baderlab.csplugins.enrichmentmap.autoannotate.AutoAnnotationManager;
import org.baderlab.csplugins.enrichmentmap.autoannotate.AutoAnnotationParameters;
import org.baderlab.csplugins.enrichmentmap.autoannotate.model.AnnotationSet;
import org.baderlab.csplugins.enrichmentmap.autoannotate.model.Cluster;
import org.baderlab.csplugins.enrichmentmap.autoannotate.task.cluster.DeleteClusterTask;
import org.baderlab.csplugins.enrichmentmap.heatmap.HeatMapParameters;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;

public class RemoveAnnotationTask extends AbstractTask {
	
	private AutoAnnotationParameters params;
	
		
	public RemoveAnnotationTask(AutoAnnotationParameters params) {
		super();
		this.params = params;
	}

	public void removeAction() {
		
		AnnotationSet annotationSet = params.getSelectedAnnotationSet();
		CyNetwork selectedNetwork = annotationSet.getView().getModel();

		EnrichmentMapManager emManager = EnrichmentMapManager.getInstance();
	
		JTable clusterTable = annotationSet.getClusterTable();
		clusterTable.getParent().getParent().getParent().remove(clusterTable.getParent().getParent());
		
		// Prevent heatmap dialog from interrupting this task
		//first check if emManager exists.  If you use the app for networks that aren't EMs then no
		//need to check heatmap params
		if(emManager != null && emManager.getMap(selectedNetwork.getSUID()) != null){
			HeatMapParameters heatMapParameters = emManager.getMap(selectedNetwork.getSUID()).getParams().getHmParams();
			if (heatMapParameters != null) {
				heatMapParameters.setSort(HeatMapParameters.Sort.NONE);
			}
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
			clusterDeletionTasks.append(new DeleteClusterTask(this.params,cluster));				
		}
		AutoAnnotationManager.getInstance().getDialogTaskManager().execute(clusterDeletionTasks);
		params.removeAnnotationSet(annotationSet);			
			
	}
	
	@Override
	public void run(TaskMonitor arg0) throws Exception {
		removeAction();
		
	}

}
