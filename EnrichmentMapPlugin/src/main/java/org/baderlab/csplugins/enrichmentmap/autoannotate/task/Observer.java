/**
 * Created by
 * User: arkadyark
 * Date: Jul 24, 2014
 * Time: 10:52:48 AM
 */
package org.baderlab.csplugins.enrichmentmap.autoannotate.task;

import org.cytoscape.work.FinishStatus;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskObserver;

/**
 * @author arkadyarkhangorodsky
 *
 */
public class Observer implements TaskObserver {
	// Class to see when a task has finished
	private boolean allTasksFinished = false;
	
	@Override
	public void allFinished(FinishStatus arg0) {
		allTasksFinished = true;
	}

	
	@Override
	public void taskFinished(ObservableTask arg0) {
		return;
	}

	public boolean isFinished() {
		return allTasksFinished;
	}
	
}
