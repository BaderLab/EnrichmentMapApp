package org.baderlab.csplugins.enrichmentmap.parsers;

import org.cytoscape.work.FinishStatus;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskObserver;

public class GreatFilterTaskObserver implements TaskObserver {
	boolean taskComplete = false;
	boolean allFinished = false;
	
	public boolean isComplete() { return taskComplete; }
	public boolean isAllFinished() { return allFinished;}
	
	
	//This method is called by the taskmanager when the iterator is finished
	public void allFinished(FinishStatus arg0) {
		allFinished = true;
		
	}
	
	//This method is called by the observable task when it is complete
	//You can call this method if there is only one observable task
	//but if there is no observable task in the iterator it will never be called (which was happeneing when
	//the user selected to not cluster the large set of data.
	public void taskFinished(ObservableTask arg0) {
		taskComplete = true;
		
	}
}
