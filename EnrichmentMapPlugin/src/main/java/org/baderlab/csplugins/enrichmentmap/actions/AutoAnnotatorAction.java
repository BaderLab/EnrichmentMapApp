package org.baderlab.csplugins.enrichmentmap.actions;

import java.awt.event.ActionEvent;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.baderlab.csplugins.enrichmentmap.autoannotate.AutoAnnotator;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.OpenBrowser;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.presentation.annotations.AnnotationManager;

/**
 * Created by:
 * @author arkadyark
 * <p>
 * Date   Jun 13, 2014<br>
 * Time   08:51 AM<br>
 */

public class AutoAnnotatorAction extends AbstractCyAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1103992843566934612L;
	private CySwingApplication application;
	private OpenBrowser browser;
	private CyNetworkManager networkManager;
	private CyNetworkViewManager networkViewManager;
	private AnnotationManager annotationManager;
	private CyServiceRegistrar registrar;
	
	public AutoAnnotatorAction(Map<String,String> configProps, CyApplicationManager applicationManager, 
    		CyNetworkViewManager networkViewManager,CySwingApplication application, OpenBrowser openBrowserRef,
    		CyNetworkManager networkManager, AnnotationManager annotationManager, CyServiceRegistrar registrar){
		super( configProps,  applicationManager,  networkViewManager);
		putValue(NAME, "Annotate Clusters");
		this.application = application;
		this.browser = openBrowserRef;
		this.networkManager = networkManager;
		this.networkViewManager = networkViewManager;
		this.annotationManager = annotationManager;
		this.registrar = registrar;
   }

	@Override
	public void actionPerformed(ActionEvent event) {
		AutoAnnotator autoannotate = new AutoAnnotator(application, browser, networkManager, networkViewManager, annotationManager, registrar);
	}
}