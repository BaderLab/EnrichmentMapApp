package org.baderlab.csplugins.enrichmentmap.task;

import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

@Singleton
public class CreatePublicationVisualStyleTaskFactory implements TaskFactory {

	@Inject private Provider<CreatePublicationVisualStyleTask> taskProvider;

	@Override
	public TaskIterator createTaskIterator() {
		return new TaskIterator(taskProvider.get());
	}

	@Override
	public boolean isReady() {
		return true;
	}
}
