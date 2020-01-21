package org.baderlab.csplugins.enrichmentmap.util;

import org.cytoscape.work.TaskMonitor;

public class NullTaskMonitor implements TaskMonitor {

	public static TaskMonitor check(TaskMonitor tm) {
		return tm == null ? new NullTaskMonitor() : tm;
	}
	
	@Override
	public void setTitle(String title) {
	}

	@Override
	public void setProgress(double progress) {
	}

	@Override
	public void setStatusMessage(String statusMessage) {
	}

	@Override
	public void showMessage(Level level, String message) {
	}

}
