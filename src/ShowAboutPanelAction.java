import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import cytoscape.Cytoscape;
import cytoscape.util.*;


/**
 * 
 */

/**
 * @author revilo
 * @date   Jun 12, 2009
 * @time   5:53:54 PM
 *
 */
public class ShowAboutPanelAction extends CytoscapeAction {

	public ShowAboutPanelAction() {
		super("Show About Box");
	}
	
	public void actionPerformed(ActionEvent event) {

		// open new dialog
		AboutPanel aboutPanel = new AboutPanel(Cytoscape.getDesktop(), true);
		aboutPanel.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		aboutPanel.pack();
		aboutPanel.setLocationRelativeTo(Cytoscape.getDesktop());
		aboutPanel.setVisible(true);

//		JOptionPane.showMessageDialog(Cytoscape.getDesktop(), TestPlugin.buildId, "About TestPlugin", JOptionPane.INFORMATION_MESSAGE );
	}
}
