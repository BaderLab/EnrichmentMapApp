package org.baderlab.csplugins.enrichmentmap.view.creation;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JFrame;

import org.baderlab.csplugins.enrichmentmap.view.util.CardDialog;
import org.baderlab.csplugins.enrichmentmap.view.util.CardDialogParameters;
import org.baderlab.csplugins.enrichmentmap.view.util.SwingUtil;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

@SuppressWarnings("serial")
@Singleton
public class CreationDialogShowAction extends AbstractAction {

	@Inject private Provider<CreationDialogParameters> dialogParametersProvider;
	@Inject private Provider<JFrame> jframeProvider;
	
	private CardDialog masterMapDialog;
	
	public CreationDialogShowAction() {
		super("New EnrichmentMap...");
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (masterMapDialog == null) {
			CardDialogParameters params = dialogParametersProvider.get();
			masterMapDialog = new CardDialog(jframeProvider.get(), params);
		}
		masterMapDialog.open();
	}
	
	
	public void dispose() {
		if(masterMapDialog != null) {
			SwingUtil.invokeOnEDTAndWait(() -> {
				masterMapDialog.dispose();
			});
		}
	}
}
