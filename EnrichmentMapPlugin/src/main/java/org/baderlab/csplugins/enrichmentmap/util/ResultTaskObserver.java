package org.baderlab.csplugins.enrichmentmap.util;

import org.cytoscape.work.FinishStatus;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskObserver;

public class ResultTaskObserver implements TaskObserver {

	private boolean taskComplete = false;
	private boolean allFinished = false;

	public boolean isComplete() {
		return taskComplete;
	}

	public boolean isAllFinished() {
		return allFinished;
	}

	public void allFinished(FinishStatus status) {
		allFinished = true;
	}

	public void taskFinished(ObservableTask task) {
		taskComplete = true;
	}

}
