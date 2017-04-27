package org.baderlab.csplugins.enrichmentmap.view.mastermap;

import java.awt.Dimension;
import java.awt.Image;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import javax.swing.AbstractButton;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;

import org.baderlab.csplugins.enrichmentmap.view.AboutDialog;
import org.baderlab.csplugins.enrichmentmap.view.util.CardDialogPage;
import org.baderlab.csplugins.enrichmentmap.view.util.CardDialogParameters;

import com.google.inject.Inject;
import com.google.inject.Provider;

public class MasterMapDialogParameters implements CardDialogParameters {

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
		return new Dimension(750, 700);
	}
	
	@Override
	public Dimension getMinimumSize() {
		return new Dimension(650, 500);
	}
	
	@Override
	public Icon getIcon() {
		URL iconURL = AboutDialog.class.getResource("enrichmentmap_logo.png");
		ImageIcon original = new ImageIcon(iconURL);
		Image scaled = original.getImage().getScaledInstance(80, 49, Image.SCALE_SMOOTH);
		return new ImageIcon(scaled);
	}
	
	@Override
	public AbstractButton[] getAdditionalButtons() {
		JButton resetButton = new JButton("Reset");
		resetButton.setActionCommand(RESET_BUTTON_ACTION_COMMAND);
		return new JButton[] { resetButton };
	}
}
