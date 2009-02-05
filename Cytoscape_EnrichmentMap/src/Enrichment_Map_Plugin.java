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

        //set-up menu options in plugins menu
        JMenu menu = Cytoscape.getDesktop().getCyMenus().getOperationsMenu();
        JMenuItem item;

        //Enrichment map submenu
        JMenu submenu = new JMenu("Enrichment Maps");

        //GSEA results panel panel
        item = new JMenuItem("Load GSEA Results");
        item.addActionListener(new LoadGSEAPanelAction());
        submenu.add(item);


        //Generic Results panel
        item = new JMenuItem("Load Generic Results");
        item.addActionListener(new LoadGenericPanelAction());
        submenu.add(item);

       menu.add(submenu);
    }

}

