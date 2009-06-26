package org.baderlab.csplugins.enrichmentmap;

import cytoscape.Cytoscape;
import cytoscape.view.CytoscapeDesktop;
import cytoscape.view.cytopanels.CytoPanel;
import cytoscape.util.CytoscapeAction;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * Created by
 * User: risserlin
 * Date: Jan 28, 2009
 * Time: 12:30:13 PM
 */
public class LoadGSEAPanelAction extends CytoscapeAction {

    //variable to track initialization of network event listener
    private boolean initialized = false;
    private int index = 0;
    public LoadGSEAPanelAction(){
         super("Load GSEA Files");
    }

      public void actionPerformed(ActionEvent event) {

          String os = System.getProperty("os.name");

          CytoscapeDesktop desktop = Cytoscape.getDesktop();
          CytoPanel cytoPanel = desktop.getCytoPanel(SwingConstants.WEST);

          if(!initialized){
                EnrichmentMapManager.getInstance();
                initialized = true;

             EnrichmentMapInputPanel inputwindow = new EnrichmentMapInputPanel();

              //set the input window in the instance so we can udate the instance window
              //on network focus
              EnrichmentMapManager.getInstance().setInputWindow(inputwindow);

              cytoPanel.add("Enrichment Map", inputwindow);
              index =  cytoPanel.indexOfComponent(inputwindow);

              cytoPanel.setSelectedIndex(index);
          }
          else{

              cytoPanel.setSelectedIndex(index);

          }

        }
}
