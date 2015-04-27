package org.baderlab.csplugins.enrichmentmap;

import static org.mockito.Mockito.*;

import java.util.HashSet;
import java.util.Set;

import org.cytoscape.work.AbstractTaskManager;
import org.cytoscape.work.FinishStatus;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.TaskObserver;
import org.cytoscape.work.TunableMutator;


/**
 * A TaskManager that just runs the tasks in order in the current Thread.
 * Only useful for testing.
 * 
 * @author mkucera
 */
public class SerialTestTaskManager extends AbstractTaskManager<Void, Void> {

	private final Set<Class<?>> tasksToIgnore = new HashSet<>();
	
	public SerialTestTaskManager() {
		super(mock(TunableMutator.class));
	}

	public void ignoreTask(Class<?> taskClass) {
		if(taskClass != null) {
			tasksToIgnore.add(taskClass);
		}
	}
	
	@Override
	public Void getConfiguration(TaskFactory factory, Object tunableContext) {
		return null;
	}

	@Override
	public void execute(TaskIterator iterator) {
		execute(iterator, null);
	}

	@Override
	public void execute(TaskIterator iterator, TaskObserver observer) {
		TaskMonitor monitor = mock(TaskMonitor.class);
		FinishStatus finishStatus = null;
		
		Task task = null;
		try {
			while(iterator.hasNext()) {
				task = iterator.next();
				
				if(tasksToIgnore.contains(task.getClass())) {
					//System.out.println("Task Ignored: " + task.getClass());
					continue;
				}
				
				task.run(monitor);
				//System.out.println("Task Ran: " + task.getClass());
				
				if(task instanceof ObservableTask && observer != null) {
					observer.taskFinished((ObservableTask)task);
				}
			}
			
			finishStatus = FinishStatus.getSucceeded();
		} catch(Exception e) {
			finishStatus = FinishStatus.newFailed(task, e);
			throw new AssertionError("Task failed", e);
		} finally {
			if(observer != null) {
				observer.allFinished(finishStatus);
			}
		}
	}

}

