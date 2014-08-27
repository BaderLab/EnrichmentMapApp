package org.baderlab.csplugins.enrichmentmap.autoannotate.task;

import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;

/**
 * Created by
 * User: arkadyark
 * Date: June 17, 2014
 * Time: 11:43 AM
 */

public class AutoAnnotationTaskFactory implements TaskFactory {

	private CyNetworkView selectedView;
	private String clusterColumnName;
	private String nameColumnName;
	private String algorithm;
	private boolean layout;
	private boolean groups;
	private String annotationSetName;
	
	public AutoAnnotationTaskFactory(CyNetworkView selectedView,
			String clusterColumnName, String nameColumnName,  String algorithm,
			boolean layout, boolean groups, String annotationSetName) {
		this.selectedView = selectedView;
		this.clusterColumnName = clusterColumnName;
		this.nameColumnName = nameColumnName;
		this.algorithm = algorithm;
		this.layout = layout;
		this.groups = groups;
		this.annotationSetName = annotationSetName;
	}
	
	public TaskIterator createTaskIterator() {
		return new TaskIterator(new AutoAnnotationTask(selectedView, clusterColumnName, 
				nameColumnName, algorithm, layout, groups, annotationSetName));
	}
	public boolean isReady() {
		return true;
	}
	
}
