import cytoscape.plugin.CytoscapePlugin;
import cytoscape.Cytoscape;
import cytoscape.task.ui.JTaskConfig;
import cytoscape.task.util.TaskManager;
import cytoscape.util.CytoscapeAction;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.awt.event.ActionEvent;
import java.awt.*;


public class Enrichment_Map_Plugin extends CytoscapePlugin {

    /*--------------------------------------------------------------
      CONSTRUCTOR.
      --------------------------------------------------------------*/
    public Enrichment_Map_Plugin(){

        //create a new action to respond to menu activation
        Enrichment_Map_PluginAction action = new Enrichment_Map_PluginAction();
        //set the preferred menu
        action.setPreferredMenu("Plugins");
        //and add it to the menus
        Cytoscape.getDesktop().getCyMenus().addAction(action);
		String tmp = System.getProperty("user.dir") ;
    }

     // Gives a description of this plugin.

	public String describe() {
        StringBuffer sb = new StringBuffer();
            sb.append("Load GSEA results and Geneset file (gmt)  ");
            sb.append("to generate an Enrichment map of the genesets where nodes are the genesets");
            sb.append(" and edges are drawn between nodes that have common edges.");
        return sb.toString();
	}

    // INTERNAL LISTENER-CLASS : This listener-class gets attached to the menu item.

	public class Enrichment_Map_PluginAction extends CytoscapeAction {


        // The constructor sets the text that should appear on the menu item.

    	public Enrichment_Map_PluginAction() {super("Enrichment Maps");}

        /**
         * This method opens the BiNGO settingspanel upon selection of the menu item
         * and opens the settingspanel for BiNGO.
         *
         * @param event event triggered when BiNGO menu item clicked.
         */

        public void actionPerformed(ActionEvent event) {

             //create new window to prompt users to load their files
            JFrame window = new JFrame("Enrichment Map Settings");

            // open new dialog
		    InputFilesPanel amd
		        = new InputFilesPanel(Cytoscape.getDesktop(),
						      true);

            amd.pack();
		    amd.setLocationRelativeTo(Cytoscape.getDesktop());
            amd.setVisible(true);

             //  Configure JTask
            JTaskConfig config = new JTaskConfig();

            //  Show Cancel/Close Buttons
            config.displayCancelButton(true);
            config.displayStatus(true);

            

		}
    }
}

