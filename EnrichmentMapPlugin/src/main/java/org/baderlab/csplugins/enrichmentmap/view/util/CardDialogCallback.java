package org.baderlab.csplugins.enrichmentmap.view.util;

import javax.swing.JDialog;

import org.cytoscape.work.Task;
import org.cytoscape.work.TaskObserver;

public interface CardDialogCallback {
	
	void setFinishButtonEnabled(boolean enabled);
	
	CardDialog getDialog();

	JDialog getDialogFrame();
	
	void close();
	
	Task getCloseTask();
	
	TaskObserver getCloseTaskObserver();
}
