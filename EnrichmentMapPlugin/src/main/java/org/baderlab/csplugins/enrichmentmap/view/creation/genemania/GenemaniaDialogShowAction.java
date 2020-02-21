package org.baderlab.csplugins.enrichmentmap.view.creation.genemania;

import org.baderlab.csplugins.enrichmentmap.view.util.CardDialogShowAction;

import com.google.inject.Singleton;

@SuppressWarnings("serial")
@Singleton
public class GenemaniaDialogShowAction extends CardDialogShowAction {

	public GenemaniaDialogShowAction() {
		super(GenemaniaDialogParameters.class, "Create from Genemania...");
	}

}
