package org.baderlab.csplugins.enrichmentmap.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import org.baderlab.csplugins.enrichmentmap.view.creation.CreationDialogParameters;
import org.baderlab.csplugins.enrichmentmap.view.util.CardDialog;
import org.cytoscape.application.swing.CySwingApplication;

import com.google.inject.Inject;
import com.google.inject.Provider;

@SuppressWarnings("serial")
public class ShowEnrichmentMapDialogAction extends AbstractAction {

	@Inject private Provider<CreationDialogParameters> dialogParametersProvider;
	@Inject private CySwingApplication application;
	
	private CardDialog masterMapDialog;
	
	public ShowEnrichmentMapDialogAction() {
		super("New EnrichmentMap...");
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (masterMapDialog == null) {
			CreationDialogParameters params = dialogParametersProvider.get();
			masterMapDialog = new CardDialog(application.getJFrame(), params);
		}
		
		masterMapDialog.open();
	}
}
