import cytoscape.Cytoscape;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by
 * User: risserlin
 * Date: Jan 28, 2009
 * Time: 12:32:11 PM
 */
public class LoadGenericPanelAction implements ActionListener {
      public void actionPerformed(ActionEvent event) {

            String os = System.getProperty("os.name");

            // open new dialog
            //if the operating system is Mac, open a special window
           if(os.contains("Mac") || (os.contains("mac"))){
                InputFilesPanelMac amd
                = new InputFilesPanelMac(Cytoscape.getDesktop(),
                              true);

                amd.pack();
                amd.setLocationRelativeTo(Cytoscape.getDesktop());
                amd.setVisible(true);
            }
            else{
                InputFilesPanel amd
                = new InputFilesPanel(Cytoscape.getDesktop(),
                              true);

                amd.pack();
                amd.setLocationRelativeTo(Cytoscape.getDesktop());
                amd.setVisible(true);
            }



        }
}
