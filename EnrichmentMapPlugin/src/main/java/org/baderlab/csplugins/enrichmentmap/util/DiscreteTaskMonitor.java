package org.baderlab.csplugins.enrichmentmap.util;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.cytoscape.work.TaskMonitor;

public class DiscreteTaskMonitor implements TaskMonitor {

	private final TaskMonitor delegate;
	private final double low;
	private final double high;
	
	
	private final int totalWork;
	private AtomicInteger currentWork = new AtomicInteger(0);
	
	private BiFunction<Integer,Integer,String> ofMessage;
	private Function<Double,String> percentMessage;
	
	
	public DiscreteTaskMonitor(TaskMonitor delegate, int totalWork, double low, double high) {
		this.delegate = NullTaskMonitor.check(delegate);
		this.totalWork = totalWork;
		this.low = low;
		this.high = high;
	}
	
	public DiscreteTaskMonitor(TaskMonitor delegate, int totalWork) {
		this(delegate, totalWork, 0.0, 1.0);
	}
	
	public void setOfMessageCallback(BiFunction<Integer,Integer,String> ofMessage) {
		this.ofMessage = ofMessage;
	}
	
	public void setPercentMessageCallback(Function<Double,String> percentMessage) {
		this.percentMessage = percentMessage;
	}
	
	private static double map(double in, double inStart, double inEnd, double outStart, double outEnd) {
		double slope = (outEnd - outStart) / (inEnd - inStart);
		return outStart + slope * (in - inStart);
	}
	
	@Override
	public void setProgress(double progress) {
		double mappedProgress = map(progress, 0.0, 1.0, low, high);
		delegate.setProgress(mappedProgress);
		if(ofMessage != null) {
			String message = ofMessage.apply(getCurrentWork(), getTotalWork());
			delegate.setStatusMessage(message);
		} else if(percentMessage != null) {
			String message = percentMessage.apply(getCurrentWorkPercent());
			delegate.setStatusMessage(message);
		}
	}
	
	private double getCurrentWorkPercent() {
		double current = getCurrentWork();
		double total = getTotalWork();
		return current / total;
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
