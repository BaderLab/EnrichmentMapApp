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
           // open new dialog

            GenericInputFilesPanel amd
                = new GenericInputFilesPanel(Cytoscape.getDesktop(),
                              true);

            amd.pack();
            amd.setLocationRelativeTo(Cytoscape.getDesktop());
            amd.setVisible(true);
           
        }
}
