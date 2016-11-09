package org.baderlab.csplugins.enrichmentmap.view.util;

import javax.swing.JPanel;

public interface CardDialogPage {
	
	String getID();
	
	String getPageTitle();
	
	default String getPageComboText() {
		return getPageTitle();
	}
	
	JPanel createBodyPanel(CardDialogCallback callback);
	
	void finish();

}
