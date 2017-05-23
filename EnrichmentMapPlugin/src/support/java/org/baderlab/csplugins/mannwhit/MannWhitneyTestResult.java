package org.baderlab.csplugins.mannwhit;

public class MannWhitneyTestResult {

	public final double twoSided;
	public final double greater;
	public final double less;
	
	public MannWhitneyTestResult(double twoSided, double greater, double less) {
		this.twoSided = twoSided;
		this.greater = greater;
		this.less = less;
	}
	
}
