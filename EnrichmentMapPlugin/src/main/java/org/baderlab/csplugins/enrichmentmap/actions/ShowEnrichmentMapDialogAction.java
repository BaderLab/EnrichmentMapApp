package org.baderlab.csplugins.enrichmentmap.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import org.baderlab.csplugins.enrichmentmap.view.mastermap.MasterMapDialogParameters;
import org.baderlab.csplugins.enrichmentmap.view.util.CardDialog;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.util.swing.IconManager;

import com.google.inject.Inject;
import com.google.inject.Provider;

@SuppressWarnings("serial")
public class ShowEnrichmentMapDialogAction extends AbstractAction {

	@Inject private Provider<MasterMapDialogParameters> dialogParametersProvider;
	@Inject private IconManager iconManager;
	@Inject private CySwingApplication application;
	
	private CardDialog masterMapDialog;
	
	public ShowEnrichmentMapDialogAction() {
		super("New EnrichmentMap...");
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (masterMapDialog == null) {
			MasterMapDialogParameters params = dialogParametersProvider.get();
			masterMapDialog = new CardDialog(application.getJFrame(), iconManager, params);
		}
		
		masterMapDialog.open();
	}
}
