package org.baderlab.csplugins.enrichmentmap.actions;

import cytoscape.CyNetwork;
import cytoscape.Cytoscape;
import cytoscape.data.writers.CytoscapeSessionWriter;
import cytoscape.data.writers.XGMMLWriter;
import cytoscape.view.CyNetworkView;
import cytoscape.util.export.PDFExporter;
import cytoscape.task.ui.JTaskConfig;
import cytoscape.task.util.TaskManager;
import giny.view.NodeView;

import javax.swing.*;
import javax.xml.bind.JAXBException;

import org.baderlab.csplugins.enrichmentmap.EnrichmentMapParameters;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.task.BuildBulkEnrichmentMapTask;
import org.baderlab.csplugins.enrichmentmap.task.BuildEnrichmentMapTask;
import org.baderlab.csplugins.enrichmentmap.view.BulkEMCreationPanel;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.URISyntaxException;
import java.util.Iterator;

/**
 * Created by IntelliJ IDEA.
 * User: User
 * Date: 1/28/11
 * Time: 10:07 AM
 * To change this template use File | Settings | File Templates.
 */
public class BuildBulkEnrichmentMapActionListener implements ActionListener{


    private JTaskConfig config;

    private BulkEMCreationPanel inputPanel;

    /**
     * @param panel - Enrichment map input panel
     */
    public BuildBulkEnrichmentMapActionListener (BulkEMCreationPanel panel) {
        this.inputPanel = panel;

    }

    /**
     * Creates a new task, checks the info in the parameters for the minimum amount of information
     *
     * @param event
     */
    public void actionPerformed(ActionEvent event) {

        config = new JTaskConfig();
        config.displayCancelButton(true);
        config.displayCloseButton(true);
        config.displayStatus(true);

       //make sure that the minimum information is set in the current set of parameters
       EnrichmentMapParameters params = inputPanel.getParams();

        //set the bulk em flag
        params.setBulkEM(true);
        
        BuildBulkEnrichmentMapTask new_map = new BuildBulkEnrichmentMapTask(params);
        boolean success = TaskManager.executeTask(new_map,config);

    }

}
