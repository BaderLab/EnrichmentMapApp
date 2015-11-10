package org.baderlab.csplugins.enrichmentmap.task;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskManager;

public class CreatePublicationVisualStyleTaskRunner implements ActionListener {

	private final CyApplicationManager applicationManager;
	private final VisualMappingManager visualMappingManager;
	private final VisualStyleFactory visualStyleFactory;
	private final CyEventHelper eventHelper;
	
	private final TaskManager<?,?> taskManager;
	
	public CreatePublicationVisualStyleTaskRunner(
			CyApplicationManager applicationManager,
			VisualMappingManager visualMappingManager,
			VisualStyleFactory visualStyleFactory,
			CyEventHelper eventHelper,
			TaskManager<?,?> taskManager) {
		
		this.applicationManager = applicationManager;
		this.visualMappingManager = visualMappingManager;
		this.visualStyleFactory = visualStyleFactory;
		this.eventHelper = eventHelper;
		this.taskManager = taskManager;
	}
	
	public void run() {
		taskManager.execute(new TaskIterator(new CreatePublicationVisualStyleTask(applicationManager, visualMappingManager, visualStyleFactory, eventHelper)));
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		run();
	}
}
