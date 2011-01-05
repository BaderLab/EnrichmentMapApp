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

import cytoscape.visual.mappings.ContinuousMapping;
import cytoscape.visual.mappings.ObjectMapping;
import cytoscape.visual.mappings.LinearNumberToColorInterpolator;
import cytoscape.visual.mappings.BoundaryRangeValues;
import cytoscape.visual.mappings.continuous.ContinuousMappingPoint;

import java.awt.Color;
import java.util.List;
import java.util.ArrayList;

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
        if (measurement.equals(Double.NaN)) return theme.getNoDataColor();

        // sanity check
        final cytoscape.visual.mappings.ContinuousMapping continuousMapping = getContinuousMapping(theme,range);
        if (continuousMapping == null) return theme.getNoDataColor();
        final java.util.Map<String, Double> attrBundle = new java.util.HashMap<String, Double>();
        attrBundle.put(gene, measurement);

        cytoscape.visual.mappings.continuous.ContinuousRangeCalculator  calculator = new cytoscape.visual.mappings.continuous.ContinuousRangeCalculator(continuousMapping.getAllPoints(),
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
