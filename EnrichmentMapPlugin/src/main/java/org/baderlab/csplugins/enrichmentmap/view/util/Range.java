package org.baderlab.csplugins.enrichmentmap.view.util;

import java.util.List;

public class Range {

	public final double min;
	public final double max;
	
	public Range(double min, double max) {
		assert min <= max;
		this.min = min;
		this.max = max;
	}

	public List<Double> toList() {
		return List.of(min, max);
	}
	
	@Override
	public String toString() {
		return "Range[min=" + min + ", max=" + max + "]";
	}
	
}
