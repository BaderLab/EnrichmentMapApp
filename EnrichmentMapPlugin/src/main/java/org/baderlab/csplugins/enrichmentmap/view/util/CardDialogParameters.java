package org.baderlab.csplugins.enrichmentmap.view.util;

import java.awt.Dimension;
import java.util.List;

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
	
	default Dimension getMinimumSize() {
		return new Dimension(400, 300);
	}
}
