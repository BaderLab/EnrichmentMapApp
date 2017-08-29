package org.baderlab.csplugins.enrichmentmap.util;

import java.util.Map;
import java.util.WeakHashMap;

import javax.swing.Timer;

public class CoalesceTimerStore<T> {

	public static final int DEFAULT_DELAY = 120;
	
	// Note: I think this code can leak Timer objects. 
	// Probably need to explicitly manage a ReferenceQueue.
	
	private final Map<T,Timer> store = new WeakHashMap<>();
	private final int delay;
	
	public CoalesceTimerStore(int delay) {
		this.delay = delay;
	}
	
	public CoalesceTimerStore() {
		this(DEFAULT_DELAY);
	}
	
	/**
	 * The Java standard library doesn't have a map with both concurrency and weak keys.
	 * Also the default implementation of compute isn't atomic. Therefore we will synchronize explicitly.
	 */
	public synchronized void coalesce(T key, Runnable runnable) {
		store.compute(key, (k,t) -> {
			if(t == null) {
				//System.out.println("start timer");
				t = new Timer(0, evt -> {
					//System.out.println("fire timer");
					runnable.run();
				});
				t.setRepeats(false);
				t.setCoalesce(true);
			} else {
				//System.out.println("reset timer");
				t.stop();
			}
			
			t.setInitialDelay(delay);
			t.start();
			return t;
		});
	}
	
	public synchronized void remove(T key) {
		Timer timer = store.remove(key);
		if(timer != null)
			timer.stop();
	}
	
}
