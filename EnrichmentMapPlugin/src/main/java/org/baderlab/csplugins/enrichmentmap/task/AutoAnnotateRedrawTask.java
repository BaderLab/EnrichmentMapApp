package org.baderlab.csplugins.enrichmentmap.task;

import org.baderlab.csplugins.enrichmentmap.view.creation.DependencyChecker;
import org.cytoscape.command.CommandExecutorTaskFactory;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

import com.google.inject.Inject;
import com.google.inject.Provider;

public class AutoAnnotateRedrawTask extends AbstractTask {
	
	@Inject private CommandExecutorTaskFactory commandTaskFactory;
	@Inject private Provider<DependencyChecker> dependencyCheckerProvider;

	@Override
	public void run(TaskMonitor tm) {
		var commandAvailable = dependencyCheckerProvider.get().isAutoAnnotateRedrawCommandAvailable();
		if(commandAvailable) {
			var tasks = commandTaskFactory.createTaskIterator(null, "autoannotate redraw eventType=emChartChanged");
			insertTasksAfterCurrentTask(tasks);
		}
	}
}
