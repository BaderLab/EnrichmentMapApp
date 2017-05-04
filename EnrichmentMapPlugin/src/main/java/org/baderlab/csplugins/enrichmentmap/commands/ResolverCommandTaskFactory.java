package org.baderlab.csplugins.enrichmentmap.commands;

import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;

import com.google.inject.Inject;
import com.google.inject.Provider;

public class ResolverCommandTaskFactory implements TaskFactory {

	@Inject private Provider<ResolverCommandTask> taskProvider;

	public TaskIterator createTaskIterator() {
		return new TaskIterator(taskProvider.get());
	}

	public boolean isReady() {
		return true;
	}

}
