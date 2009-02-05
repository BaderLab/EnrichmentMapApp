import cytoscape.Cytoscape;
import cytoscape.util.CytoscapeAction;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by
 * User: risserlin
 * Date: Jan 28, 2009
 * Time: 12:32:11 PM
 */
public class LoadGenericPanelAction extends CytoscapeAction {

    public LoadGenericPanelAction() {
          super("Load Generic files");
    }

    public void actionPerformed(ActionEvent event) {
           // open new dialog

            GenericInputFilesPanel amd
                = new GenericInputFilesPanel(Cytoscape.getDesktop(),
                              false);

            amd.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            amd.pack();
            amd.setLocationRelativeTo(Cytoscape.getDesktop());
            amd.setVisible(true);
           
        }
}
