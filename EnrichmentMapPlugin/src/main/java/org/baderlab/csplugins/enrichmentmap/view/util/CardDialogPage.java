package org.baderlab.csplugins.enrichmentmap.view.util;

import javax.swing.AbstractButton;
import javax.swing.JPanel;

public interface CardDialogPage<T> {
	
	String getID();
	
	String getPageComboText();
	
	JPanel createBodyPanel(CardDialogCallback callback);
	
	T finish();

	/**
	 * Lifecycle method called every time the page is made visible.
	 */
	default void opened() { };
	
	/**
	 * Called when one of the buttons returned by 
	 * {@link CardDialogParameters#getAdditionalButtons()} is clicked.
	 * 
	 * @see AbstractButton#setActionCommand(String)
	 */
	default void extraButtonClicked(String actionCommand) { }
}
