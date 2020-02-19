package org.baderlab.csplugins.enrichmentmap.view.util;

import javax.swing.AbstractButton;
import javax.swing.JPanel;

public interface CardDialogPage {
	
	String getID();
	
	String getPageComboText();
	
	JPanel createBodyPanel(CardDialogCallback callback);
	
	void finish();

	/**
	 * Lifecycle method called every time the page is made visible.
	 */
	default void opened() { };
	
	/**
	 * Called when one of the buttons returned by 
	 * {@link CardDialogParameters#getExtraButtons()} is clicked.
	 * 
	 * @see AbstractButton#setActionCommand(String)
	 */
	default void extraButtonClicked(String actionCommand) { }
}
