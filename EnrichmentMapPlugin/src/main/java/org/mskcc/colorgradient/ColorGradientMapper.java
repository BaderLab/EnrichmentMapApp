// $Id$
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

import java.awt.Color;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cytoscape.visual.mappings.BoundaryRangeValues;
import cytoscape.visual.mappings.ContinuousMapping;
import cytoscape.visual.mappings.Interpolator;
import cytoscape.visual.mappings.LinearNumberToColorInterpolator;
import cytoscape.visual.mappings.ObjectMapping;
import cytoscape.visual.mappings.continuous.ContinuousRangeCalculator;

/**
 * Maps color gradient onto network views.
 *
 * @author Ethan Cerami, Benjamin Gross
 */
public class ColorGradientMapper {

    /**
     * Method to get instance of ColorGradientMapper.
     *
     * @return ColorGradientMapper
     */
    public static ColorGradientMapper getInstance() {
        return new ColorGradientMapper();
    }


    /**
     * Gets color gradient (as hex string) for given gene/sample/data type
     * @param gene String
     * @param measurement Double
     * @return Color
     */
    public static Color getColorGradient(ColorGradientTheme theme, ColorGradientRange range, String gene, Double measurement) {

        // sparse matrix files will contain NaN value when no data is available
        if (measurement == null || measurement.equals(Double.NaN)) return theme.getNoDataColor();

        // sanity check
        final ContinuousMapping continuousMapping = getContinuousMapping(theme,range);
        if (continuousMapping == null) return theme.getNoDataColor();
        final Map<String, Double> attrBundle = new HashMap<String, Double>();
        attrBundle.put(gene, measurement);

        ContinuousRangeCalculator  calculator = createContinuousRangeCalculatorHack(getAllPointsHack(continuousMapping),
                                                             continuousMapping.getInterpolator(), attrBundle);

        return (Color)calculator.calculateRangeValue(gene);

    }



    public static ContinuousMapping getContinuousMapping(ColorGradientTheme colorGradientTheme,
                                                         ColorGradientRange colorGradientRange) {

        // sanity check
        if (colorGradientTheme == null || colorGradientRange == null) return null;

        ContinuousMapping continuousMapping = new ContinuousMapping(Color.WHITE, ObjectMapping.NODE_MAPPING);
        
        continuousMapping.setInterpolator(new LinearNumberToColorInterpolator());

        final Color minColor = colorGradientTheme.getMinColor();
        final Color medColor = colorGradientTheme.getCenterColor();
        final Color maxColor = colorGradientTheme.getMaxColor();

        final BoundaryRangeValues bv0 = new BoundaryRangeValues(minColor, minColor, minColor);
        final BoundaryRangeValues bv1a = new BoundaryRangeValues(medColor, medColor, medColor);
        final BoundaryRangeValues bv1b = new BoundaryRangeValues(medColor, medColor, medColor);
        final BoundaryRangeValues bv2 = new BoundaryRangeValues(maxColor, maxColor, maxColor);

        // add points to continuous mapper
        continuousMapping.addPoint(colorGradientRange.getMinValue(), bv0);
        continuousMapping.addPoint(colorGradientRange.getCenterLowValue(), bv1a);
        continuousMapping.addPoint(colorGradientRange.getCenterHighValue(), bv1b);
        continuousMapping.addPoint(colorGradientRange.getMaxValue(), bv2);

        // outta here
        return continuousMapping;
    }

    	/*
     * This hack is a workaround for the API change in Cytoscape 2.8.0.
     * The issue involves a change in the signature of the constructor for
     * ContinuousRangeCalculator from ArrayList to List.  This workaround uses
     * late binding to construct a new instance so the bytecode verifier
     * doesn't get confused when the constructor signature isn't what it
     * expects.
     */
    private static ContinuousRangeCalculator createContinuousRangeCalculatorHack(ArrayList<?> points, Interpolator interpolator, Map<String, Double> attributes) {
		Constructor<ContinuousRangeCalculator> constructor = null;
		for (Class<?> type : new Class[] { ArrayList.class, List.class }) {
	    	try {
				constructor = ContinuousRangeCalculator.class.getConstructor(type, Interpolator.class, Map.class);
			} catch (SecurityException e) {
				throw new RuntimeException(e);
			} catch (NoSuchMethodException e) {
				continue;
			}
		}
		if (constructor == null) {
			throw new RuntimeException();
		}
		try {
			return constructor.newInstance(points, interpolator, attributes);
		} catch (IllegalArgumentException e) {
			throw new RuntimeException(e);
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}

	/*
     * This hack is a workaround for the API change in Cytoscape 2.8.0.
     * The issue involves a change in the return type of
     * ContinuousMapping.getAllPoints() from ArrayList to
     * List<ContinuousMappingPoint>.  This workaround uses late binding to
     * invoke getAllPoints() so the bytecode verifier doesn't get confused
     * when the return type isn't what it expects.
     */
    private static ArrayList<?> getAllPointsHack(ContinuousMapping mapping) {
    	try {
			Method method = ContinuousMapping.class.getMethod("getAllPoints");
			return (ArrayList<?>) method.invoke(mapping);
		} catch (SecurityException e) {
			throw new RuntimeException(e);
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		} catch (IllegalArgumentException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}


    /**
     * Gets color gradient (as hex string) for given gene/sample/data type.
     *
     * @param gene String
     * @param measurement Double
     * @return String
     */
    public static String getColorGradientAsString(ColorGradientTheme theme, ColorGradientRange range, String gene, Double measurement) {

        final Color color = getColorGradient(theme,range, gene, measurement);
        final int red  = color.getRed();
        String redColor = Integer.toHexString(red);
        if (red <= 9) redColor = "0" + redColor;
        final int green = color.getGreen();
        String greenColor = Integer.toHexString(green);
        if (green <= 9) greenColor = "0" + greenColor;
        final int blue = color.getBlue();
        String blueColor = Integer.toHexString(blue);
        if (blue <= 9) blueColor = "0" + blueColor;

        // outta here
        return (redColor + greenColor + blueColor);
    }

    /**
     * Constructor (private).
     */
    private ColorGradientMapper() {
    }
}
