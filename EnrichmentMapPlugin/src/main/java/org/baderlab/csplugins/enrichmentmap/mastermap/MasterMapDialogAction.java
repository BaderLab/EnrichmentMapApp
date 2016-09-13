package org.baderlab.csplugins.enrichmentmap.mastermap;

import java.awt.event.ActionEvent;

import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.application.swing.CySwingApplication;

import com.google.inject.Inject;
import com.google.inject.Provider;

@SuppressWarnings("serial")
public class MasterMapDialogAction extends AbstractCyAction {

	@Inject private Provider<MasterMapDialogController> controllerProvider;
	@Inject private CySwingApplication application;
	
	private NiceDialog masterMapDialog;
	
	public MasterMapDialogAction() {
		super("Create MasterMap...");
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(masterMapDialog == null) {
			MasterMapDialogController controller = controllerProvider.get();
			masterMapDialog = new NiceDialog(application.getJFrame(), controller);
		}
		masterMapDialog.open();
	}

}
