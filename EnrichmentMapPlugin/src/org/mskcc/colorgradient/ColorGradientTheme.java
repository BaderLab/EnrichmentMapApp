// $Id: ColorGradientTheme.java,v 1.1 2008/06/06 15:30:13 grossb Exp $
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

// imports
import java.awt.Color;
import org.baderlab.csplugins.enrichmentmap.*;

/**
 * Encapsulates a Color Theme, e.g. a BLUE_RED_GRADIENT_THEME.
 *
 * @author Ethan Cerami.
 */
public class ColorGradientTheme {
    private String name;
    private Color minColor, centerColor, maxColor;
    private Color noDataColor;
	private Color minLabelColor, maxLabelColor;

    /**
     * Private Constructor.  Enforces Enumeration Pattern.
     *
     * @param name        Name of Color Theme.
     * @param minColor    Minimum Color;  lower bound of color gradient.
     * @param centerColor Center Color; center of color gradient.
     * @param maxColor    Maxiumum Color; upper bound of color gradient.
     * @param noDataColor No Data Color
	 * @param minLabelColor Color
	 * @param maxLabelColor Color
     */
    private ColorGradientTheme(String name, Color minColor, Color centerColor,
							   Color maxColor, Color noDataColor,
							   Color minLabelColor, Color maxLabelColor) {
        this.name = name;
        this.minColor = minColor;
        this.centerColor = centerColor;
        this.maxColor = maxColor;
        this.noDataColor = noDataColor;
		this.minLabelColor = minLabelColor;
		this.maxLabelColor = maxLabelColor;
    }

    /**
     * Gets String representation of Color Theme.
     * Returned value includes name, and all colors.
     *
     * @return String representation of Color Theme.
     */
    public String toString() {
        return "Color Theme:  " + this.name + " [Min Color:  " + minColor
                + ", Center Color:  " + centerColor + ", Max Color:  "
                + maxColor + ", No Data Color" + noDataColor + "]";
    }

    /**
     * Gets Name of Color Theme.
     *
     * @return Name of Color Theme.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Gets the color associated with the minimum value.
     * This represents the lower bounds of the color gradient.
     *
     * @return Color Object.
     */
    public Color getMinColor() {
        return this.minColor;
    }

    /**
     * Gets the color associated with the center value.
     * This represents the center of the color gradient.
     *
     * @return Color Object.
     */
    public Color getCenterColor() {
        return this.centerColor;
    }

    /**
     * Gets the color associated with the maximum value.
     * This represents the upper bounds of the color gradient.
     *
     * @return Color Object.
     */
    public Color getMaxColor() {
        return this.maxColor;
    }

    /**
     * Gets the Color Associated with Node Data.
     * For example, if a node has no expression data, it is colored with
     * the noDataColor Object.
     *
     * @return Color Object.
     */
    public Color getNoDataColor() {
        return this.noDataColor;
    }

    /**
     * Gets the Label color when gradient is at the low end.
     *
     * @return Color Object.
     */
    public Color getMinLabelColor() {
        return this.minLabelColor;
    }

    /**
     * Gets the Label color when gradient is at the high end.
     *
     * @return Color Object.
     */
    public Color getMaxLabelColor() {
        return this.maxLabelColor;
    }

    /**
     * Color Theme:  BLUE_RED_GRADIENT_THEME
     */
    public static final ColorGradientTheme BLUE_RED_GRADIENT_THEME
            = new ColorGradientTheme("Blue Red", Color.BLUE, Color.WHITE,
									 Color.RED, Color.LIGHT_GRAY,
									 Color.WHITE, Color.BLACK);

    /**
     * Color Theme:  GREEN_RED_GRADIENT_THEME
     */
    public static final ColorGradientTheme GREEN_RED_GRADIENT_THEME
            = new ColorGradientTheme("Green Red", Color.GREEN, Color.BLACK,
									 Color.RED, Color.LIGHT_GRAY,
									 Color.BLACK, Color.WHITE);

    /**
     * Color Theme:  YELLOW_BLUE_GRADIENT_THEME
     */
    public static final ColorGradientTheme YELLOW_BLUE_GRADIENT_THEME
            = new ColorGradientTheme("Yellow Blue", Color.YELLOW, Color.BLACK,
									 Color.BLUE, Color.LIGHT_GRAY,
									 Color.BLACK, Color.WHITE);
      /**
     * Color Theme:  GREEN_MAGENTA_GRADIENT_THEME
     */
    public static final ColorGradientTheme GREEN_MAGENTA_GRADIENT_THEME
            = new ColorGradientTheme("Green Magenta", Color.GREEN, Color.WHITE,
									 Color.MAGENTA, Color.LIGHT_GRAY,
									 Color.BLACK, Color.WHITE);
    /**
     * Color Theme:  RED_ONECOLOR_GRADIENT_THEME
     */
    public static final ColorGradientTheme RED_ONECOLOR_GRADIENT_THEME
            = new ColorGradientTheme("Red", Color.WHITE, new Color(255,120,120),
									 Color.RED, Color.LIGHT_GRAY,
									 Color.BLACK, Color.WHITE);

    /**
     * Color Theme:  RED_ONECOLOR_GRADIENT_THEME
     */
    public static final ColorGradientTheme GREEN_ONECOLOR_GRADIENT_THEME
            = new ColorGradientTheme("Green", Color.WHITE, new Color(120,255,120),
									 Color.GREEN, Color.LIGHT_GRAY,
									 Color.BLACK, Color.WHITE);

    /**
     * Color Theme: enrichmentmap node coloring
     */
      public static final ColorGradientTheme ENRICHMENTMAP_NODE_THEME
            = new ColorGradientTheme("Node colouring",
                                    EnrichmentMapVisualStyle.max_phenotype2,
                                    EnrichmentMapVisualStyle.overColor,
                                    EnrichmentMapVisualStyle.max_phenotype1,
                                    Color.gray, Color.black, Color.white);

}
