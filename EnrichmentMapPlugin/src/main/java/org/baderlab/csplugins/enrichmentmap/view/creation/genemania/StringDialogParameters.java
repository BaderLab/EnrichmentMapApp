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

public class StringDialogParameters implements CardDialogParameters {

	@Inject private Provider<StringDialogPage> stringDialogPage;
	
	@Override
	public String getTitle() {
		return "Create Enrichment Map from STRING Network";
	}

	@Override
	public List<CardDialogPage> getPages() {
		return Arrays.asList(stringDialogPage.get());
	}

	@Override
	public Dimension getPreferredSize() {
		return new Dimension(820, 320);
	}
	
	@Override
	public Dimension getMinimumSize() {
		return new Dimension(650, 320);
	}
	
	@Override
	public AbstractButton[] getExtraButtons() {
		JButton resetButton = new JButton("Reset");
		resetButton.setActionCommand("reset");
		return new JButton[] { resetButton };
	}
}
