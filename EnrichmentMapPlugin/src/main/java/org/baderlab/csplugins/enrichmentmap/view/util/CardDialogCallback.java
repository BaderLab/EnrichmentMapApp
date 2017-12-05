package org.baderlab.csplugins.enrichmentmap.view.util;

import javax.swing.JDialog;

import org.cytoscape.work.Task;

public interface CardDialogCallback {
	
	void setFinishButtonEnabled(boolean enabled);
	
	CardDialog getDialog();

	JDialog getDialogFrame();
	
	void close();
	
	Task getCloseTask();
}
