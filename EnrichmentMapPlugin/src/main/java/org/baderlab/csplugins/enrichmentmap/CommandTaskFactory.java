package org.baderlab.csplugins.enrichmentmap;

import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.json.JSONResult;

import com.google.inject.Provider;

public interface CommandTaskFactory extends TaskFactory {
	
	
	public String getName();
	
	public String getDescription();
	
	public String getLongDescription();
	
	public boolean supportsJson();
	
	
	public static CommandTaskFactory create(String name, String desc, String longDesc, Provider<? extends Task> taskProvider, Task... moreTasks) {
		Task task = taskProvider.get();
		boolean supportsJson = supportsJson(task);
		
		return new CommandTaskFactory() {
			@Override
			public TaskIterator createTaskIterator() {
				var ti = new TaskIterator(taskProvider.get());
				for(Task task : moreTasks) {
					ti.append(task);
				}
				return ti;
			}

			@Override public boolean isReady() { return true; }
			@Override public String getName() { return name; }
			@Override public String getDescription() { return desc; }
			@Override public String getLongDescription() { return longDesc == null ? null : desc + " " + longDesc; }
			@Override public boolean supportsJson() { return supportsJson; }
		};
	}
	
	private static boolean supportsJson(Task task) {
		if(task instanceof ObservableTask) {
			return ((ObservableTask)task).getResultClasses().contains(JSONResult.class);
		}
		return false;
	}
}
