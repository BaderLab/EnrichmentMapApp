package org.baderlab.csplugins.enrichmentmap.util;



/**
 * A simple linear interpolator from a domain to a range.
 * 
 * 
 * The following code is based on<br>
 * org.cytoscape.view.vizmap.internal.mappings.interpolators.LinearNumberInterpolator<br>
 * and<br>
 * org.cytoscape.view.vizmap.internal.mappings.interpolators.LinearNumberToNumberInterpolator<br>
 *
 */
public class LinearNumberInterpolator {

	private final double lowerDomain, lowerRange, upperDomain, upperRange;
	
	
	public LinearNumberInterpolator(double lowerDomain, double upperDomain, double lowerRange, double upperRange) {
		this.lowerDomain = lowerDomain;
		this.lowerRange = lowerRange;
		this.upperDomain = upperDomain;
		this.upperRange = upperRange;
	}
	
	
	public double getRangeValue(double domainValue) {
		if (lowerDomain == upperDomain)
			return lowerRange;

		double frac = (domainValue - lowerDomain) / (upperDomain - lowerDomain);
		return (frac * upperRange) + ((1.0 - frac) * lowerRange);
	}
	
	/**
	 * Returns a new LinearNumberInterpolator with the following property:
	 * If a domain value is below the lowerDomain parameter then return lowerValue,
	 * if a domain value is above the upperDomain parameter then return upperValue.
	 */
	public LinearNumberInterpolator withDomainCutoff(final double lowerValue, final double upperValue) {
		return new LinearNumberInterpolator(lowerDomain, upperDomain, lowerRange, upperRange) {
			@Override
			public double getRangeValue(double domainValue) {
				if(domainValue < lowerDomain)
					return lowerValue;
				if(domainValue > upperDomain)
					return upperValue;
				return super.getRangeValue(domainValue);
			}
		};
	}
	
}
