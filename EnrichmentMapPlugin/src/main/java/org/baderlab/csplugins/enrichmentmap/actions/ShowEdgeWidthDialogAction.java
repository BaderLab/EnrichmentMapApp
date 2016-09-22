package org.baderlab.csplugins.enrichmentmap.actions;

import java.awt.event.ActionEvent;

import javax.swing.JOptionPane;

import org.baderlab.csplugins.enrichmentmap.style.WidthFunction;
import org.baderlab.csplugins.enrichmentmap.view.EdgeWidthDialog;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.application.swing.CySwingApplication;

import com.google.inject.Inject;
import com.google.inject.Provider;


@SuppressWarnings("serial")
public class ShowEdgeWidthDialogAction extends AbstractCyAction {
	
	@Inject private Provider<EdgeWidthDialog> dialogProvider;
	@Inject private CySwingApplication application;
	@Inject private CyApplicationManager applicationManager;

	public ShowEdgeWidthDialogAction() {
		super("Post Analysis Edge Width...");
	}
	
	public void actionPerformed(ActionEvent e) {
		if(WidthFunction.appliesTo(applicationManager.getCurrentNetwork())) {
			EdgeWidthDialog dialog = dialogProvider.get();
			dialog.pack();
			dialog.setLocationRelativeTo(application.getJFrame());
			dialog.setVisible(true);
		}
		else {
			JOptionPane.showMessageDialog(application.getJFrame(), 
				"Please run Post Analysis first.",
				"EnrichmentMap Edge Width", 
				JOptionPane.WARNING_MESSAGE);
		}
	}

}
