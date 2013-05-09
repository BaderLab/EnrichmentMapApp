package org.baderlab.csplugins.enrichmentmap.actions;


import javax.swing.*;

import org.baderlab.csplugins.enrichmentmap.view.BulkEMCreationPanel;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.application.swing.CytoPanel;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.application.swing.CytoPanelState;
import org.cytoscape.view.model.CyNetworkViewManager;

import java.awt.event.ActionEvent;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: User
 * Date: 1/28/11
 * Time: 9:13 AM
 * To change this template use File | Settings | File Templates.
 */
public class BulkEMCreationAction extends AbstractCyAction {

	private CytoPanel cytoPanelWest;
	private BulkEMCreationPanel bulkEmInput;
	
    public BulkEMCreationAction(Map<String,String> configProps, CyApplicationManager applicationManager, 
    		CyNetworkViewManager networkViewManager,CySwingApplication application, BulkEMCreationPanel bulkEmInput) {

        super( configProps,  applicationManager,  networkViewManager);
		putValue(NAME, "Bulk Enrichment Map Creation");
		
		this.cytoPanelWest = application.getCytoPanel(CytoPanelName.WEST);
		this.bulkEmInput = bulkEmInput;
    }

    public void actionPerformed(ActionEvent event) {
              
     // If the state of the cytoPanelWest is HIDE, show it
        if (cytoPanelWest.getState() == CytoPanelState.HIDE) {
                cytoPanelWest.setState(CytoPanelState.DOCK);
        }

        // Select my panel
        int index = cytoPanelWest.indexOfComponent(this.bulkEmInput);
        if (index == -1) {
                return;
        }
        cytoPanelWest.setSelectedIndex(index);

    }

}
