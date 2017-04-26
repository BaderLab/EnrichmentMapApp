package org.baderlab.csplugins.enrichmentmap.view.util;

import java.awt.Dimension;
import java.util.List;

import javax.swing.AbstractButton;
import javax.swing.Icon;

public interface CardDialogParameters {
	
	String getTitle();
	
	List<CardDialogPage> getPages();

	default Icon getIcon() {
		return null;
	}
	
	default String getFinishButtonText() {
		return "Finish";
	}
	
	default String getPageChooserLabelText() {
		return null;
	}
	
	default Dimension getPreferredSize() {
		return new Dimension(400, 300);
	}
	
	/** 
	 * Return a list of extra buttons, that will go in the bottom left area of the button bar. 
	 * An ActionListener will be attached to each button that will call 
	 * CardDialogPage.extraButtonClicked(button.getActionCommand()) when the button is clicked.
	 * 
	 * @see AbstractButton#setActionCommand(String)
	 */
	default AbstractButton[] getAdditionalButtons() {
		return null;
	}
}
