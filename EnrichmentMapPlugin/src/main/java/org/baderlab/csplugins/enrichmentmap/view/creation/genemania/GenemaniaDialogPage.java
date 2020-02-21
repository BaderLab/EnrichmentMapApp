package org.baderlab.csplugins.enrichmentmap.view.creation.genemania;

import javax.swing.JPanel;

import org.baderlab.csplugins.enrichmentmap.view.util.CardDialogCallback;
import org.baderlab.csplugins.enrichmentmap.view.util.CardDialogPage;

public class GenemaniaDialogPage implements CardDialogPage {

	@Override
	public String getID() {
		return getClass().getSimpleName();
	}

	@Override
	public String getPageComboText() {
		return "Create Enrichment Map";
	}

	@Override
	public JPanel createBodyPanel(CardDialogCallback callback) {
		return new JPanel();
	}

	@Override
	public void finish() {

	}

}
