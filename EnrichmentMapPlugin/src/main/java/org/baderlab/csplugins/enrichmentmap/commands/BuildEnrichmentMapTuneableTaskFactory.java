package org.baderlab.csplugins.enrichmentmap.commands;

import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;

import com.google.inject.Inject;
import com.google.inject.Provider;

public class BuildEnrichmentMapTuneableTaskFactory implements TaskFactory {
	
	@Inject private Provider<BuildEnrichmentMapTuneableTask> taskProvider;

	public TaskIterator createTaskIterator() {
		return new TaskIterator(taskProvider.get());
	}

	public boolean isReady() {
		return true;
	}

}
