package org.baderlab.csplugins.enrichmentmap.mastermap.view;

import java.awt.event.ActionEvent;

import org.baderlab.csplugins.enrichmentmap.util.NiceDialog;
import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.util.swing.IconManager;

import com.google.inject.Inject;
import com.google.inject.Provider;

@SuppressWarnings("serial")
public class MasterMapDialogAction extends AbstractCyAction {

	@Inject private Provider<MasterMapDialogController> controllerProvider;
	@Inject private IconManager iconManager;
	@Inject private CySwingApplication application;
	
	private NiceDialog masterMapDialog;
	
	public MasterMapDialogAction() {
		super("Create MasterMap...");
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(masterMapDialog == null) {
			MasterMapDialogController controller = controllerProvider.get();
			masterMapDialog = new NiceDialog(application.getJFrame(), iconManager, controller);
		}
		masterMapDialog.open();
	}

}
