package org.baderlab.csplugins.enrichmentmap.autoannotate.task;

import java.util.HashMap;

import org.baderlab.csplugins.enrichmentmap.autoannotate.model.Cluster;
import org.baderlab.csplugins.enrichmentmap.autoannotate.view.AnnotationDisplayPanel;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.OpenBrowser;
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
	private OpenBrowser browser;
	private CyNetworkManager networkManager;
	private CyNetworkViewManager networkViewManager;
	private AnnotationManager annotationManager;
	private long networkID;
	private String nameColumnName;
	private String clusterColumnName;
	private CyServiceRegistrar registrar;
	private DialogTaskManager dialogTaskManager;
	private AnnotationDisplayPanel displayPanel;
	
	public AutoAnnotatorTaskFactory(CySwingApplication application, CyApplicationManager applicationManager, OpenBrowser browser, 
			CyNetworkViewManager networkViewManager, CyNetworkManager networkManager,
			AnnotationManager annotationManager, AnnotationDisplayPanel displayPanel, long networkID, String clusterColumnName,
			String nameColumnName, CyServiceRegistrar registrar, DialogTaskManager dialogTaskManager) {
		this.application = application;
		this.applicationManager = applicationManager;
		this.browser = browser;
		this.networkManager = networkManager;
		this.networkViewManager = networkViewManager;
		this.annotationManager = annotationManager;
		this.displayPanel = displayPanel;
		this.networkID = networkID;
		this.nameColumnName = nameColumnName;
		this.clusterColumnName = clusterColumnName;
		this.registrar = registrar;
		this.dialogTaskManager = dialogTaskManager;
	}
	
	public TaskIterator createTaskIterator() {
		return new TaskIterator(new AutoAnnotatorTask(application, applicationManager, browser, networkViewManager, networkManager, annotationManager, displayPanel, networkID, clusterColumnName, nameColumnName, registrar, dialogTaskManager));
	}

	public boolean isReady() {
		return true;
	}
	
}
