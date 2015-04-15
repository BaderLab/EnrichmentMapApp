package org.baderlab.csplugins.enrichmentmap.util;

import junit.framework.TestCase;

public class LinearNumberInterpolatorTest extends TestCase {

	private static void assertDouble(double expected, double actual) {
		final double epsilon = 0.001;
		assertTrue("expected: " + expected + " actual: " + actual, Math.abs(expected-actual) < epsilon);
	}
	
	
	public void testLinearNumberInterpolator() {
		LinearNumberInterpolator interpolator = new LinearNumberInterpolator(0.5, 1.0, 0.0, 5.0);
		
		assertDouble(0.0, interpolator.getRangeValue(0.5));
		assertDouble(1.0, interpolator.getRangeValue(0.6));
		assertDouble(2.0, interpolator.getRangeValue(0.7));
		assertDouble(3.0, interpolator.getRangeValue(0.8));
		assertDouble(4.0, interpolator.getRangeValue(0.9));
		assertDouble(5.0, interpolator.getRangeValue(1.0));
		
		// numbers out of range
		assertDouble(-1.0, interpolator.getRangeValue(0.4));
		assertDouble(6.0, interpolator.getRangeValue(1.1));
	}
	
	
	public void testLinearNumberInterpolatorWithCutoff() {
		LinearNumberInterpolator interpolator = new LinearNumberInterpolator(0.5, 1.0, 0.0, 5.0).withDomainCutoff(-99, 99);
		
		assertDouble(0.0, interpolator.getRangeValue(0.5));
		assertDouble(1.0, interpolator.getRangeValue(0.6));
		assertDouble(2.0, interpolator.getRangeValue(0.7));
		assertDouble(3.0, interpolator.getRangeValue(0.8));
		assertDouble(4.0, interpolator.getRangeValue(0.9));
		assertDouble(5.0, interpolator.getRangeValue(1.0));
		
		// numbers out of range
		assertDouble(-99, interpolator.getRangeValue(0.4));
		assertDouble(99, interpolator.getRangeValue(1.1));
	}
	
	
}
