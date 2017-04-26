package org.baderlab.csplugins.enrichmentmap.view.util;

import javax.swing.AbstractButton;
import javax.swing.JPanel;

public interface CardDialogPage {
	
	String getID();
	
	String getPageTitle();
	
	default String getPageComboText() {
		return getPageTitle();
	}
	
	JPanel createBodyPanel(CardDialogCallback callback);
	
	void finish();

	/**
	 * Called when one of the buttons returned by 
	 * {@link CardDialogParameters#getAdditionalButtons()} is clicked.
	 * 
	 * @see AbstractButton#setActionCommand(String)
	 */
	default void extraButtonClicked(String actionCommand) { }
}
