package org.baderlab.csplugins.enrichmentmap.view.mastermap;

import java.awt.event.ActionEvent;

import org.baderlab.csplugins.enrichmentmap.view.util.CardDialog;
import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.util.swing.IconManager;

import com.google.inject.Inject;
import com.google.inject.Provider;

@SuppressWarnings("serial")
public class MasterMapDialogAction extends AbstractCyAction {

	@Inject private Provider<MasterMapDialogParameters> dialogParametersProvider;
	@Inject private IconManager iconManager;
	@Inject private CySwingApplication application;
	
	private CardDialog masterMapDialog;
	
	public MasterMapDialogAction() {
		super("Create MasterMap...");
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(masterMapDialog == null) {
			MasterMapDialogParameters params = dialogParametersProvider.get();
			masterMapDialog = new CardDialog(application.getJFrame(), iconManager, params);
		}
		masterMapDialog.open();
	}

}
