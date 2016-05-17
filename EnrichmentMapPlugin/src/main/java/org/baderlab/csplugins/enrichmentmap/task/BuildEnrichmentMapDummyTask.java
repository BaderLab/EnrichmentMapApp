package org.baderlab.csplugins.enrichmentmap.task;

import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
//A dummy task so that the correct title is set on the task dialog.

public class BuildEnrichmentMapDummyTask extends AbstractTask {
	
	private TaskMonitor taskMonitor;
	
	private String title;
	
	public BuildEnrichmentMapDummyTask(String title) {
		super();
		this.title = title;
	}

	private void build(){
		
	}
	
	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		//add sleep so that the correct title gets put in the dialog
		Thread.sleep(1500);
		this.taskMonitor = taskMonitor;
		this.taskMonitor.setTitle(this.title);
		
		build();
		
	}

	/**
	 * Gets the Task Title.
	 *
	 * @return human readable task title.
	 */
	public String getTitle() {
		return new String("Building an Enrichment Map");
	}
}
