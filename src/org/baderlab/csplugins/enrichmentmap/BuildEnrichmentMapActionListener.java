package org.baderlab.csplugins.enrichmentmap;
import cytoscape.task.ui.JTaskConfig;
import cytoscape.task.util.TaskManager;
import cytoscape.task.TaskMonitor;
import cytoscape.task.Task;


import javax.swing.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.HashMap;

/**
 * Created by
 * User: risserlin
 * Date: Jan 8, 2009
 * Time: 1:35:40 PM
 */
public class BuildEnrichmentMapActionListener implements ActionListener {

    private JTaskConfig config;

    private EnrichmentMapInputPanel inputPanel;

    public BuildEnrichmentMapActionListener (EnrichmentMapInputPanel panel) {
        this.inputPanel = panel;

    }

   public void actionPerformed(ActionEvent event) {

        config = new JTaskConfig();
        config.displayCancelButton(true);
        config.displayCloseButton(true);
        config.displayStatus(true);

       //if(params.isGSEA()){
            BuildGSEAEnrichmentMapTask new_map = new BuildGSEAEnrichmentMapTask(inputPanel.getParams());
            boolean success = TaskManager.executeTask(new_map,config);
       //}
       //else{
           //BuildGenericEnrichmentMapTask new_map = new BuildGenericEnrichmentMapTask(inputPanel, params);
           //boolean success = TaskManager.executeTask(new_map, config);
       //}

   }


}
