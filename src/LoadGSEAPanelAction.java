
import cytoscape.Cytoscape;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

/**
 * Created by
 * User: risserlin
 * Date: Jan 28, 2009
 * Time: 12:30:13 PM
 */
public class LoadGSEAPanelAction implements ActionListener {

      public void actionPerformed(ActionEvent event) {

            String os = System.getProperty("os.name");

            // open new dialog
            //if the operating system is Mac, open a special window
           if(os.contains("Mac") || (os.contains("mac"))){
                GSEAInputFilesPanelMac amd
                = new GSEAInputFilesPanelMac(Cytoscape.getDesktop(),
                              true);

                amd.pack();
                amd.setLocationRelativeTo(Cytoscape.getDesktop());
                amd.setVisible(true);
            }
            else{
                GSEAInputFilesPanel amd
                = new GSEAInputFilesPanel(Cytoscape.getDesktop(),
                              true);

                amd.pack();
                amd.setLocationRelativeTo(Cytoscape.getDesktop());
                amd.setVisible(true);
            }


        }
}
