package org.baderlab.csplugins.enrichmentmap.util;

import org.cytoscape.work.TaskMonitor;

public class DiscreteTaskMonitor implements TaskMonitor {

	private final TaskMonitor delegate;
	
	private final int totalWork;
	private int currentWork = 0;
	
	
	public DiscreteTaskMonitor(TaskMonitor delegate, int totalWork) {
		this.delegate = delegate;
		this.totalWork = totalWork;
	}
	
	
	private static double map(double in, double inStart, double inEnd, double outStart, double outEnd) {
		double slope = (outEnd - outStart) / (inEnd - inStart);
		return outStart + slope * (in - inStart);
	}
	
	@Override
	public void setProgress(double progress) {
		double mappedProgress = map(progress, 0.0, 1.0, 0.0, 1.0);
		delegate.setProgress(mappedProgress);
	}
	
	public void setWork(int currentWork) {
		this.currentWork = currentWork;
		double mappedProgress = map(currentWork, 0, totalWork, 0.0, 1.0);
		setProgress(mappedProgress);
	}
	
	public void addWork(int workToAdd) {
		setWork(currentWork + workToAdd);
	}
	
	public void inc() {
		addWork(1);
	}
	
	@Override
	public void setTitle(String title) {
		delegate.setTitle(title);
	}


	@Override
	public void setStatusMessage(String statusMessage) {
		delegate.setStatusMessage(statusMessage);
	}

	@Override
	public void showMessage(Level level, String message) {
		delegate.showMessage(level, message);
	}

}
