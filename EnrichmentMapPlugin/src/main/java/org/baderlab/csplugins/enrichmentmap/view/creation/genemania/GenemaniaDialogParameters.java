package org.baderlab.csplugins.enrichmentmap.view.creation.genemania;

import java.awt.Dimension;
import java.util.Arrays;
import java.util.List;

import javax.swing.AbstractButton;
import javax.swing.JButton;

import org.baderlab.csplugins.enrichmentmap.view.util.dialog.CardDialogPage;
import org.baderlab.csplugins.enrichmentmap.view.util.dialog.CardDialogParameters;

import com.google.inject.Inject;
import com.google.inject.Provider;

public class GenemaniaDialogParameters implements CardDialogParameters {

	static final String RESET_BUTTON_ACTION = "reset";
	
	@Inject private Provider<GenemaniaDialogPage> genemaniaDialogPage;
	
	@Override
	public String getTitle() {
		return "Create Enrichment Map from Genemania Network";
	}

	@Override
	public List<CardDialogPage> getPages() {
		return Arrays.asList(genemaniaDialogPage.get());
	}

	@Override
	public Dimension getPreferredSize() {
		return new Dimension(820, 700);
	}
	
	@Override
	public Dimension getMinimumSize() {
		return new Dimension(650, 550);
	}
	
	@Override
	public AbstractButton[] getExtraButtons() {
		JButton resetButton = new JButton("Reset");
		resetButton.setActionCommand(RESET_BUTTON_ACTION);
		
		return new JButton[] { resetButton };
	}
}
