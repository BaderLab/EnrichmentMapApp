package org.baderlab.csplugins.enrichmentmap.view.creation;

import java.awt.Dimension;
import java.util.Arrays;
import java.util.List;

import javax.swing.AbstractButton;
import javax.swing.JButton;

import org.baderlab.csplugins.enrichmentmap.view.util.CardDialogPage;
import org.baderlab.csplugins.enrichmentmap.view.util.CardDialogParameters;

import com.google.inject.Inject;
import com.google.inject.Provider;

public class CreationDialogParameters implements CardDialogParameters {

	static final String RESET_BUTTON_ACTION_COMMAND = "reset";
	
	@Inject private Provider<MasterDetailDialogPage> masterDetailDialogPage;
	
	
	@Override
	public List<CardDialogPage> getPages() {
		return Arrays.asList(
			masterDetailDialogPage.get()
		);
	}
	
	@Override
	public String getTitle() {
		return "Create Enrichment Map";
	}

	@Override
	public String getFinishButtonText() {
		return "Build";
	}
	
	@Override
	public String getPageChooserLabelText() {
		return "Analysis Type:";
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
	public AbstractButton[] getAdditionalButtons() {
		JButton resetButton = new JButton("Reset");
		resetButton.setActionCommand(RESET_BUTTON_ACTION_COMMAND);
		return new JButton[] { resetButton };
	}
}
