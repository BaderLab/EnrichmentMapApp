package org.baderlab.csplugins.enrichmentmap.view.creation;

import org.baderlab.csplugins.enrichmentmap.view.util.dialog.CardDialogShowAction;

import com.google.inject.Singleton;

@SuppressWarnings("serial")
@Singleton
public class CreationDialogShowAction extends CardDialogShowAction {

	public CreationDialogShowAction() {
		super(CreationDialogParameters.class, "New EnrichmentMap...");
	}
	
}
