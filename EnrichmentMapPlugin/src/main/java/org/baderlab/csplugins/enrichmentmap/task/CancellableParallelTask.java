package org.baderlab.csplugins.enrichmentmap.task;

import java.text.MessageFormat;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.baderlab.csplugins.enrichmentmap.util.DiscreteTaskMonitor;
import org.baderlab.csplugins.enrichmentmap.util.NullTaskMonitor;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

public abstract class CancellableParallelTask<T> extends AbstractTask {

	@Override
	public void run(TaskMonitor tm) throws InterruptedException {
		tm = NullTaskMonitor.check(tm);
			
		int cpus = Runtime.getRuntime().availableProcessors();
		ExecutorService executor = Executors.newFixedThreadPool(cpus);

		T t = compute(tm, executor);

		// Support cancellation
		Timer timer = new Timer();
		timer.scheduleAtFixedRate(new TimerTask() {
			public void run() {
				if (cancelled) {
					executor.shutdownNow();
				}
			}
		}, 0, 1000);

		executor.shutdown();
		executor.awaitTermination(3, TimeUnit.HOURS);
		timer.cancel();

		if(!cancelled)
			done(t);
	}

	public abstract T compute(TaskMonitor tm, ExecutorService executor);

	public void done(T t) {

	}

	
	
	public static DiscreteTaskMonitor discreteTaskMonitor(TaskMonitor tm, int size) {
		DiscreteTaskMonitor taskMonitor = new DiscreteTaskMonitor(tm, size);
        taskMonitor.setTitle("Computing Geneset Similarities...");
        taskMonitor.setPercentMessageCallback(percent -> MessageFormat.format("Computing Geneset Similarity: {0,number,#%}", percent));
        return taskMonitor;
	}
}
