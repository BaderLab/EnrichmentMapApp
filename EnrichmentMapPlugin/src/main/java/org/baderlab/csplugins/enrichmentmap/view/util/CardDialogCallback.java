package org.baderlab.csplugins.enrichmentmap.view.util;

import javax.swing.JDialog;

public interface CardDialogCallback {
	
	void setFinishButtonEnabled(boolean enabled);

	JDialog getDialogFrame();
	
	void close();
}
