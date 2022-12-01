package org.baderlab.csplugins.enrichmentmap.util;

import org.cytoscape.work.TaskMonitor;

public class DelegatingTaskMonitor implements TaskMonitor {

	
	private final TaskMonitor delegate;
	
	public DelegatingTaskMonitor(TaskMonitor delegate) {
		this.delegate = delegate;
	}
	
	@Override
	public void setTitle(String title) {
		delegate.setTitle(title);
	}

	@Override
	public void setProgress(double progress) {
		delegate.setProgress(progress);
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
