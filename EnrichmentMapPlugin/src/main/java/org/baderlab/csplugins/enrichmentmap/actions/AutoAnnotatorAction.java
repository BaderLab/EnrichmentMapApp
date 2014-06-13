package org.baderlab.csplugins.enrichmentmap.actions;

import java.awt.event.ActionEvent;
import java.util.Map;

import org.baderlab.csplugins.enrichmentmap.autoannotate.AutoAnnotator;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.util.swing.OpenBrowser;
import org.cytoscape.view.model.CyNetworkViewManager;

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
	
	public AutoAnnotatorAction(Map<String,String> configProps, CyApplicationManager applicationManager, 
    		CyNetworkViewManager networkViewManager,CySwingApplication application, OpenBrowser openBrowserRef){
		super( configProps,  applicationManager,  networkViewManager);
		putValue(NAME, "Annotate clusters");
		this.application = application;
		this.browser = openBrowserRef;
   }

	@Override
	public void actionPerformed(ActionEvent event) {
		AutoAnnotator autoannotate = new AutoAnnotator(application, browser);
		
	}
}