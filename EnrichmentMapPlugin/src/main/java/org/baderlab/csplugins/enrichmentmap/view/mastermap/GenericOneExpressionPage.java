package org.baderlab.csplugins.enrichmentmap.view.mastermap;

import javax.swing.JPanel;

import org.baderlab.csplugins.enrichmentmap.view.util.CardDialogCallback;
import org.baderlab.csplugins.enrichmentmap.view.util.CardDialogPage;

import com.google.inject.Singleton;

@Singleton
public class GenericOneExpressionPage implements CardDialogPage {

	@Override
	public String getID() {
		return "mastermap.GenericOneExpressionPage";
	}

	@Override
	public String getPageTitle() {
		return "Create Enrichment Map";
	}

	@Override
	public String getPageComboText() {
		return "Generic/gProfiler - One expression file";
	}
	
	@Override
	public JPanel createBodyPanel(CardDialogCallback callback) {
		return new JPanel();
	}

	@Override
	public void finish() {
		System.out.println("BOOYAH!");
	}

}
