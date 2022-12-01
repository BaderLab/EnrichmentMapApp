package org.baderlab.csplugins.enrichmentmap.util;

import java.util.Map;

import org.cytoscape.work.FinishStatus;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.SynchronousTaskManager;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.TaskObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleSyncTaskManager implements SynchronousTaskManager<Void> {

	private final TaskMonitor delegateTaskMonitor;
	
	
	public SimpleSyncTaskManager() {
		this.delegateTaskMonitor = null;
	}
	
	public SimpleSyncTaskManager(TaskMonitor delegateTaskMonitor) {
		this.delegateTaskMonitor = delegateTaskMonitor;
	}
	
	
	
	@Override
	public Void getConfiguration(TaskFactory factory, Object tunableContext) {
		throw new UnsupportedOperationException("There is no configuration available for a SyncrhonousTaskManager");	
	}

	@Override
	public void setExecutionContext(Map<String, Object> context) {
	}

	@Override
	public void execute(TaskIterator taskIterator) {
		execute(taskIterator, null);
	}

	@Override
	public void execute(TaskIterator taskIterator, TaskObserver observer) {
		TaskMonitor taskMonitor = (delegateTaskMonitor != null) ? delegateTaskMonitor : new LoggingTaskMonitor();
		
        Task task = null;
		try {
			while (taskIterator.hasNext()) {
				task = taskIterator.next();
				
				if(taskMonitor instanceof LoggingTaskMonitor)
					((LoggingTaskMonitor)taskMonitor).setTask(task);

				task.run(taskMonitor);

				if (task instanceof ObservableTask && observer != null) {
					observer.taskFinished((ObservableTask)task);
				} 
			}
            if (observer != null) 
            	observer.allFinished(FinishStatus.getSucceeded());

		} catch (Exception exception) {
			if(taskMonitor instanceof LoggingTaskMonitor)
				((LoggingTaskMonitor)taskMonitor).showException(exception);
			
            if (observer != null && task != null) 
            	observer.allFinished(FinishStatus.newFailed(task, exception));
		}
	}

	
	static class LoggingTaskMonitor implements TaskMonitor {
		
		private static final Logger logger = LoggerFactory.getLogger("org.cytoscape.application.userlog");
		private static final String LOG_PREFIX = "TaskMonitor";
		private Logger messageLogger = null;

		private Task task;

		public LoggingTaskMonitor() {
			this.messageLogger = LoggerFactory.getLogger(LOG_PREFIX);
		}

		public void setTask(final Task newTask) {
			this.task = newTask;
			this.messageLogger = LoggerFactory.getLogger(LOG_PREFIX+"."+newTask.getClass().getName());
		}

		public void setTitle(String title) {
			logger.info("Task (" + task.toString() + ") title: " + title);
		}

		public void setStatusMessage(String statusMessage) {
			// Kind of a hack. Ignore the status messages from ComputeSimilarityTaskParallel
			showMessage(TaskMonitor.Level.INFO, statusMessage);
		}

		public void showMessage(TaskMonitor.Level level, String message) {
			switch(level) {
			case INFO:
				logger.info("Task (" + task.toString() + ") status: " + message);
				messageLogger.info(message);
				break;
			case WARN:
				logger.warn("Task (" + task.toString() + ") status: " + message);
				messageLogger.warn(message);
				break;
			case ERROR:
				logger.error("Task (" + task.toString() + ") status: " + message);
				messageLogger.error(message);
				break;
			}
		}

		public void setProgress(double progress) {
			int prog = (int) Math.floor(progress * 100);
			logger.info("Task (" + task.toString() + ") progress: " + prog + "%");
		}

		public void showException(Exception exception) {
			logger.error("Exception executing task: "+exception.getMessage(), exception);
			messageLogger.error("Error executing task: "+exception.getMessage(), exception);
		}
	}
}
