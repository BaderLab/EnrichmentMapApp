package org.baderlab.csplugins.enrichmentmap.view.creation.string;

import org.baderlab.csplugins.enrichmentmap.view.util.dialog.CardDialogShowAction;

import com.google.inject.Singleton;

@SuppressWarnings("serial")
@Singleton
public class StringDialogShowAction extends CardDialogShowAction {

	public StringDialogShowAction() {
		super(StringDialogParameters.class, "Create from STRING...");
	}

}
