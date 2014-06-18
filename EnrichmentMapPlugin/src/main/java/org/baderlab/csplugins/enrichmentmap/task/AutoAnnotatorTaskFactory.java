package org.baderlab.csplugins.enrichmentmap.task;

import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.OpenBrowser;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.presentation.annotations.AnnotationManager;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;

/**
 * Created by
 * User: arkadyark
 * Date: June 17, 2014
 * Time: 11:43 AM
 */

public class AutoAnnotatorTaskFactory implements TaskFactory {

	private CySwingApplication application;
	private OpenBrowser browser;
	private CyNetworkManager networkManager;
	private CyNetworkViewManager networkViewManager;
	private AnnotationManager annotationManager;
	private long networkID;
	private String nameColumnName;
	private String clusterColumnName;
	private CyServiceRegistrar registrar;
	
	public AutoAnnotatorTaskFactory(CySwingApplication application, OpenBrowser browser, 
			CyNetworkViewManager networkViewManager, CyNetworkManager networkManager,
			AnnotationManager annotationManager, long networkID,
    		String clusterColumnName, String nameColumnName, CyServiceRegistrar registrar) {
		this.application = application;
		this.browser = browser;
		this.networkManager = networkManager;
		this.networkViewManager = networkViewManager;
		this.annotationManager = annotationManager;
		this.networkID = networkID;
		this.nameColumnName = nameColumnName;
		this.clusterColumnName = clusterColumnName;
		this.registrar = registrar; 
	}
	
	public TaskIterator createTaskIterator() {
		return new TaskIterator(new AutoAnnotatorTask(application, browser, networkViewManager, networkManager, annotationManager, networkID, clusterColumnName, nameColumnName, registrar));
	}

	public boolean isReady() {
		return true;
	}
	
}
