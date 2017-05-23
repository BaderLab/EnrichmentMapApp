package org.baderlab.csplugins.enrichmentmap.util;

import java.text.MessageFormat;
import java.util.concurrent.atomic.AtomicInteger;

import org.cytoscape.work.TaskMonitor;

public class DiscreteTaskMonitor implements TaskMonitor {

	private final TaskMonitor delegate;
	private final double low;
	private final double high;
	
	
	private final int totalWork;
	private AtomicInteger currentWork = new AtomicInteger(0);
	private String messageTemplate;
	
	
	public DiscreteTaskMonitor(TaskMonitor delegate, int totalWork, double low, double high) {
		this.delegate = delegate;
		this.totalWork = totalWork;
		this.low = low;
		this.high = high;
	}
	
	public DiscreteTaskMonitor(TaskMonitor delegate, int totalWork) {
		this(delegate, totalWork, 0.0, 1.0);
	}
	
	public void setStatusMessageTemplate(String template) {
		this.messageTemplate = template;
	}
	
	private static double map(double in, double inStart, double inEnd, double outStart, double outEnd) {
		double slope = (outEnd - outStart) / (inEnd - inStart);
		return outStart + slope * (in - inStart);
	}
	
	@Override
	public void setProgress(double progress) {
		double mappedProgress = map(progress, 0.0, 1.0, low, high);
		delegate.setProgress(mappedProgress);
		if(messageTemplate != null) {
			String message = MessageFormat.format(messageTemplate, getCurrentWork(), getTotalWork());
			delegate.setStatusMessage(message);
		}
	}
	
	public void addWork(int delta) {
		int work = currentWork.getAndAdd(delta);
		double mappedProgress = map(work, 0, totalWork, 0.0, 1.0);
		setProgress(mappedProgress);
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

	public int getTotalWork() {
		return totalWork;
	}
	
	public int getCurrentWork() {
		return currentWork.get();
	}
}
