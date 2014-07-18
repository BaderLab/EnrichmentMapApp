package org.baderlab.csplugins.enrichmentmap.autoannotate.task;

import org.baderlab.csplugins.enrichmentmap.autoannotate.AutoAnnotationManager;
import org.baderlab.csplugins.enrichmentmap.autoannotate.view.AutoAnnotatorDisplayPanel;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.presentation.annotations.AnnotationManager;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.swing.DialogTaskManager;

/**
 * Created by
 * User: arkadyark
 * Date: June 17, 2014
 * Time: 11:43 AM
 */

public class AutoAnnotatorTaskFactory implements TaskFactory {

	private CySwingApplication application;
	private CyApplicationManager applicationManager;
	private CyNetworkManager networkManager;
	private CyNetworkViewManager networkViewManager;
	private AnnotationManager annotationManager;
	private AutoAnnotationManager autoAnnotationManager;
	private CyNetworkView selectedView;
	private String clusterColumnName;
	private String nameColumnName;
	private int annotationSetNumber;
	private CyServiceRegistrar registrar;
	private CyTableManager tableManager;
	
	public AutoAnnotatorTaskFactory(CySwingApplication application, CyApplicationManager applicationManager, 
			CyNetworkViewManager networkViewManager, CyNetworkManager networkManager,
			AnnotationManager annotationManager, AutoAnnotationManager autoAnnotationManager, CyNetworkView selectedView, String clusterColumnName,
			String nameColumnName, int annotationSetNumber, CyServiceRegistrar registrar, DialogTaskManager dialogTaskManager, CyTableManager tableManager) {
		this.application = application;
		this.applicationManager = applicationManager;
		this.networkManager = networkManager;
		this.networkViewManager = networkViewManager;
		this.annotationManager = annotationManager;
		this.autoAnnotationManager = autoAnnotationManager;
		this.selectedView = selectedView;
		this.clusterColumnName = clusterColumnName;
		this.nameColumnName = nameColumnName;
		this.annotationSetNumber = annotationSetNumber;
		this.registrar = registrar;
		this.tableManager = tableManager;
	}
	
	public TaskIterator createTaskIterator() {
		return new TaskIterator(new AutoAnnotatorTask(application, applicationManager, networkViewManager, networkManager, annotationManager, autoAnnotationManager, selectedView, clusterColumnName, nameColumnName, annotationSetNumber, registrar, tableManager));
	}

	public boolean isReady() {
		return true;
	}
	
}
