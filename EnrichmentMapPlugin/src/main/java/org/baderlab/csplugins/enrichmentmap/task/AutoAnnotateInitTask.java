package org.baderlab.csplugins.enrichmentmap.task;

import org.baderlab.csplugins.enrichmentmap.view.creation.DependencyChecker;
import org.cytoscape.command.CommandExecutorTaskFactory;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;

public class AutoAnnotateInitTask extends AbstractTask {
	
	@Inject private CommandExecutorTaskFactory commandTaskFactory;
	@Inject private Provider<DependencyChecker> dependencyCheckerProvider;

	private String dataset;
	
	public static interface Factory {
		AutoAnnotateInitTask create(String dataset);
	}
	
	@AssistedInject
	public AutoAnnotateInitTask(@Assisted String dataset) {
		this.dataset = dataset;
	}
	
	@Override
	public void run(TaskMonitor tm) {
		var commandAvailable = dependencyCheckerProvider.get().isCommandAvailable("autoannotate", "eminit");
		if(commandAvailable) {
			var tasks = commandTaskFactory.createTaskIterator(null, "autoannotate eminit dataSet=\"" + dataset + "\"");
			insertTasksAfterCurrentTask(tasks);
		}
	}
}
