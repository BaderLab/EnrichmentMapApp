package org.baderlab.csplugins.enrichmentmap.util;

import java.awt.Dimension;

import javax.swing.Icon;
import javax.swing.JPanel;

public interface NiceDialogController {
	
	String getTitle();
	
	String getSubTitle();
	
	JPanel createBodyPanel(NiceDialogCallback callback);
	
	void finish();
	

	default Icon getIcon() {
		return null;
	}
	
	default String getFinishButtonText() {
		return "Finish";
	}
	
	default Dimension getMinimumSize() {
		return new Dimension(400, 300);
	}
}
