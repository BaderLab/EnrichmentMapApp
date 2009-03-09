
import cytoscape.Cytoscape;
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

    public LoadGSEAPanelAction(){
         super("Load GSEA Files");
    }

      public void actionPerformed(ActionEvent event) {

            String os = System.getProperty("os.name");

          if(!initialized){
                EnrichmentMapManager.getInstance();
                initialized = true;
          }

            // open new dialog
            //if the operating system is Mac, open a special window
           if(os.contains("Mac") || (os.contains("mac"))){
                GSEAInputFilesPanelMac amd
                = new GSEAInputFilesPanelMac(Cytoscape.getDesktop(),
                              false);

                amd.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                amd.pack();
                amd.setLocationRelativeTo(Cytoscape.getDesktop());
                amd.setVisible(true);

            }
            else{
                GSEAInputFilesPanel amd
                = new GSEAInputFilesPanel(Cytoscape.getDesktop(),
                              false);

                amd.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                amd.pack();
                amd.setLocationRelativeTo(Cytoscape.getDesktop());
                amd.setVisible(true);
            }



        }
}
