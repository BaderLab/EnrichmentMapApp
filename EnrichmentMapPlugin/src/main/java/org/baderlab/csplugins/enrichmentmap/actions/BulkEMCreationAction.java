package org.baderlab.csplugins.enrichmentmap.actions;

import cytoscape.Cytoscape;
import cytoscape.util.CytoscapeAction;
import cytoscape.view.CytoscapeDesktop;
import cytoscape.view.cytopanels.CytoPanel;

import javax.swing.*;

import org.baderlab.csplugins.enrichmentmap.view.BulkEMCreationPanel;

import java.awt.event.ActionEvent;

/**
 * Created by IntelliJ IDEA.
 * User: User
 * Date: 1/28/11
 * Time: 9:13 AM
 * To change this template use File | Settings | File Templates.
 */
public class BulkEMCreationAction extends CytoscapeAction {

    public BulkEMCreationAction() {
        super("Bulk Enrichment Map Creation");
    }

    public void actionPerformed(ActionEvent event) {
       CytoscapeDesktop desktop = Cytoscape.getDesktop();
       CytoPanel cytoPanel = desktop.getCytoPanel(SwingConstants.WEST);

        //get a directory name where all of the GSEA results are stored
       BulkEMCreationPanel bulkwindow = new BulkEMCreationPanel();

        cytoPanel.add("Bulk EM Creation", bulkwindow);
        int index =  cytoPanel.indexOfComponent(bulkwindow);

        cytoPanel.setSelectedIndex(index);

    }

}
