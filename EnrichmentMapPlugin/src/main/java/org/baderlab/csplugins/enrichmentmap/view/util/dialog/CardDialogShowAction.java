package org.baderlab.csplugins.enrichmentmap.view.util.dialog;

import java.awt.event.ActionEvent;

import javax.swing.JFrame;

import org.baderlab.csplugins.enrichmentmap.view.util.SwingUtil;
import org.cytoscape.application.swing.AbstractCyAction;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Provider;
import com.google.inject.assistedinject.Assisted;

@SuppressWarnings("serial")
public class CardDialogShowAction extends AbstractCyAction {

	@Inject private Provider<JFrame> jframeProvider;
	@Inject private Injector injector;
	
	private final Class<? extends CardDialogParameters> dialogParamsClass;
	
	private CardDialog dialog;
	
	
	public static interface Factory {
		CardDialogShowAction create(Class<? extends CardDialogParameters> dialogParamsClass, String title);
	}
	
	@Inject
	public CardDialogShowAction(@Assisted Class<? extends CardDialogParameters> dialogParamsClass, @Assisted String title) {
		super(title);
		this.dialogParamsClass = dialogParamsClass;
	}

	public void showDialog() {
		if (dialog == null) {
			CardDialogParameters dialogParams = injector.getInstance(dialogParamsClass);
			dialog = new CardDialog(jframeProvider.get(), dialogParams);
		}
		dialog.open();
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		showDialog();
	}
	
	
	public void dispose() {
		if(dialog != null) {
			SwingUtil.invokeOnEDTAndWait(() -> {
				dialog.dispose();
			});
		}
	}

}
