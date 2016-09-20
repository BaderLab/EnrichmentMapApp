package org.baderlab.csplugins.enrichmentmap.task;

import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
//A dummy task so that the correct title is set on the task dialog.

public class TitleTask extends AbstractTask {
	
	private final String title;
	
	public TitleTask(String title) {
		this.title = title;
	}
	
	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		//add sleep so that the correct title gets put in the dialog
		Thread.sleep(1500);
		taskMonitor.setTitle(title);
	}
}
