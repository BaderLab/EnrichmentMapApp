package org.baderlab.csplugins.enrichmentmap.autoannotate.task;

import org.baderlab.csplugins.enrichmentmap.autoannotate.AutoAnnotationManager;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.swing.DialogTaskManager;

/**
 * Created by
 * User: arkadyark
 * Date: June 17, 2014
 * Time: 11:43 AM
 */

public class AutoAnnotationTaskFactory implements TaskFactory {

	private CySwingApplication application;
	private AutoAnnotationManager autoAnnotationManager;
	private CyNetworkView selectedView;
	private String clusterColumnName;
	private String nameColumnName;
	private String algorithm;
	private String annotationSetName;
	private DialogTaskManager dialogTaskManager;
	private CyServiceRegistrar registrar;
	private CyTableManager tableManager;
	
	public AutoAnnotationTaskFactory(CySwingApplication application, AutoAnnotationManager autoAnnotationManager, 
			CyNetworkView selectedView, String clusterColumnName, String nameColumnName,  String algorithm, String annotationSetName, 
			CyServiceRegistrar registrar, DialogTaskManager dialogTaskManager, CyTableManager tableManager) {
		this.application = application;
		this.autoAnnotationManager = autoAnnotationManager;
		this.selectedView = selectedView;
		this.clusterColumnName = clusterColumnName;
		this.nameColumnName = nameColumnName;
		this.algorithm = algorithm;
		this.annotationSetName = annotationSetName;
		this.dialogTaskManager = dialogTaskManager;
		this.registrar = registrar;
		this.tableManager = tableManager;
	}
	
	public TaskIterator createTaskIterator() {
		return new TaskIterator(new AutoAnnotationTask(application, autoAnnotationManager, selectedView, clusterColumnName, 
				nameColumnName, algorithm, annotationSetName, dialogTaskManager, registrar, tableManager));
	}
	public boolean isReady() {
		return true;
	}
	
}
