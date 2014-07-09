package org.baderlab.csplugins.enrichmentmap.autoannotate.task;

import java.util.HashMap;

import org.baderlab.csplugins.enrichmentmap.autoannotate.model.Cluster;
import org.baderlab.csplugins.enrichmentmap.autoannotate.view.AnnotationDisplayPanel;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.OpenBrowser;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.presentation.annotations.AnnotationManager;
import org.cytoscape.work.SynchronousTaskManager;
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
	private CyNetworkView selectedView;
	private String clusterColumnName;
	private String nameColumnName;
	private CyServiceRegistrar registrar;
	private AnnotationDisplayPanel displayPanel;
	
	public AutoAnnotatorTaskFactory(CySwingApplication application, CyApplicationManager applicationManager, 
			CyNetworkViewManager networkViewManager, CyNetworkManager networkManager,
			AnnotationManager annotationManager, AnnotationDisplayPanel displayPanel, CyNetworkView selectedView, String clusterColumnName,
			String nameColumnName, CyServiceRegistrar registrar, DialogTaskManager dialogTaskManager) {
		this.application = application;
		this.applicationManager = applicationManager;
		this.networkManager = networkManager;
		this.networkViewManager = networkViewManager;
		this.annotationManager = annotationManager;
		this.displayPanel = displayPanel;
		this.selectedView = selectedView;
		this.clusterColumnName = clusterColumnName;
		this.nameColumnName = nameColumnName;
		this.registrar = registrar;
	}
	
	public TaskIterator createTaskIterator() {
		return new TaskIterator(new AutoAnnotatorTask(application, applicationManager, networkViewManager, networkManager, annotationManager, displayPanel, selectedView, clusterColumnName, nameColumnName, registrar));
	}

	public boolean isReady() {
		return true;
	}
	
}
