package org.baderlab.csplugins.enrichmentmap.task;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;

public class CreatePublicationVisualStyleTaskFactory implements TaskFactory {

	private final CyApplicationManager applicationManager;
	private final VisualMappingManager visualMappingManager;
	private final VisualStyleFactory visualStyleFactory;
	private final CyEventHelper eventHelper;
	
	
	public CreatePublicationVisualStyleTaskFactory(
			CyApplicationManager applicationManager,
			VisualMappingManager visualMappingManager,
			VisualStyleFactory visualStyleFactory,
			CyEventHelper eventHelper) {
		
		this.applicationManager = applicationManager;
		this.visualMappingManager = visualMappingManager;
		this.visualStyleFactory = visualStyleFactory;
		this.eventHelper = eventHelper;
	}

	@Override
	public TaskIterator createTaskIterator() {
		return new TaskIterator(new CreatePublicationVisualStyleTask(applicationManager, visualMappingManager, visualStyleFactory, eventHelper));
	}

	@Override
	public boolean isReady() {
		return true;
	}
}
