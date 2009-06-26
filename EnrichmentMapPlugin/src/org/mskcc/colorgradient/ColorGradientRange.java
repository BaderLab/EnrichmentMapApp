// $Id: ColorGradientRange.java,v 1.3 2008/06/16 14:18:55 grossb Exp $
//------------------------------------------------------------------------------
/** Copyright (c) 2008 Memorial Sloan-Kettering Cancer Center.
 **
 ** This library is free software; you can redistribute it and/or modify it
 ** under the terms of the GNU Lesser General Public License as published
 ** by the Free Software Foundation; either version 2.1 of the License, or
 ** any later version.
 **
 ** This library is distributed in the hope that it will be useful, but
 ** WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 ** MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 ** documentation provided hereunder is on an "as is" basis, and
 ** Memorial Sloan-Kettering Cancer Center
 ** has no obligations to provide maintenance, support,
 ** updates, enhancements or modifications.  In no event shall
 ** Memorial Sloan-Kettering Cancer Center
 ** be liable to any party for direct, indirect, special,
 ** incidental or consequential damages, including lost profits, arising
 ** out of the use of this software and its documentation, even if
 ** Memorial Sloan-Kettering Cancer Center
 ** has been advised of the possibility of such damage.  See
 ** the GNU Lesser General Public License for more details.
 **
 ** You should have received a copy of the GNU Lesser General Public License
 ** along with this library; if not, write to the Free Software Foundation,
 ** Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 **/

package org.mskcc.colorgradient;

/**
 * Class used to store min, center, max (& orig) values.
 *
 * @author Benjamin Gross
 */
public class ColorGradientRange {

	// final members
	final private double origMinValue;
	final private double origCenterLowValue;
	final private double origCenterHighValue;
	final private double origMaxValue;

	// mutable members
	private double minValue;
	private double centerLowValue;
	private double centerHighValue;
	private double maxValue;

	/**
	 * Method to get instance of ColorGradientRange.
	 *
	 * @param origMinValue double
	 * @param origCenterLowValue double
	 * @param origCenterHighValue double
	 * @param origMaxValue double
	 * @param minValue double 
	 * @param centerLowValue double
	 * @param centerHighValue double
	 * @param maxValue double
	 * @return ColorGradientRange
	 */
	public static ColorGradientRange getInstance(double origMinValue,
											double origCenterLowValue, double origCenterHighValue,
											double origMaxValue,
											double minValue,
											double centerLowValue, double centerHighValue,
											double maxValue) {
		return new ColorGradientRange(origMinValue, origCenterLowValue, origCenterHighValue, origMaxValue,
								 minValue, centerLowValue, centerHighValue, maxValue);
	}

	/**
	 * Constructor (private).
	 *
	 * @param origMinValue double
	 * @param origCenterLowValue double
	 * @param origCenterHighValue double
	 * @param origMaxValue double
	 * @param minValue double 
	 * @param centerLowValue double
	 * @param centerHighValue double
	 * @param maxValue double
	 */
	private ColorGradientRange(double origMinValue,
						  double origCenterLowValue, double origCenterHighValue,
						  double origMaxValue,
						  double minValue,
						  double centerLowValue, double centerHighValue,
						  double maxValue) {

		// init members
		this.origMinValue = origMinValue;
		this.origCenterLowValue = origCenterLowValue;
		this.origCenterHighValue = origCenterHighValue;
		this.origMaxValue = origMaxValue;
		this.minValue = minValue;
		this.centerLowValue = centerLowValue;
		this.centerHighValue = centerHighValue;
		this.maxValue = maxValue;
	}

	// mutators
	public void setMinValue(double minValue) { this.minValue = minValue; }
	public void setCenterLowValue(double centerLowValue) { this.centerLowValue = centerLowValue; }
	public void setCenterHighValue(double centerHighValue) { this.centerHighValue = centerHighValue; }
	public void setMaxValue(double maxValue) { this.maxValue = maxValue; }
	
	// accessors
	public double getOrigMinValue() { return origMinValue; }
	public double getMinValue() { return minValue; }
	public double getOrigCenterLowValue() { return origCenterLowValue; }
	public double getCenterLowValue() { return centerLowValue; }
	public double getOrigCenterHighValue() { return origCenterHighValue; }
	public double getCenterHighValue() { return centerHighValue; }
	public double getOrigMaxValue() { return origMaxValue; }
	public double getMaxValue() { return maxValue; }
}
