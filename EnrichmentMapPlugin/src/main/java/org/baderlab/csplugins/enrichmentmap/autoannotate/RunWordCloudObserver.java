package org.baderlab.csplugins.enrichmentmap.autoannotate;

import org.cytoscape.work.FinishStatus;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskObserver;

public class RunWordCloudObserver implements TaskObserver {
    boolean taskComplete = false;
    
    
    public boolean isComplete() {
    	return taskComplete;
    }

	@Override
	public void taskFinished(ObservableTask task) {
		taskComplete = true;
	}

	@Override
	public void allFinished(FinishStatus arg0) {
		// TODO Auto-generated method stub
	}

}
