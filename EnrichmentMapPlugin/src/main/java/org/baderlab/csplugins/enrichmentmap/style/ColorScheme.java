package org.baderlab.csplugins.enrichmentmap.style;

import java.awt.Color;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public enum ColorScheme {
	
	// 3-color ColorBrewer schemes (color-blind safe and print friendly):
	// Diverging
	RD_BU_9("RdBu-9", "ColorBrewer 9-class RdBu (Diverging, Colorblind Safe)",
			new Color[] {
					new Color(178, 24, 43), new Color(178, 24, 43), new Color(214, 96, 77), new Color(244, 165, 130),
					new Color(253, 219, 199), new Color(247, 247, 247), new Color(209, 229, 240),
					new Color(146, 197, 222), new Color(67, 147, 195), new Color(33, 102, 172), new Color(33, 102, 172),
					
			},
			new Double[] {
					4.0, 1.4, 1.0, 0.5, 0.49999, 0.0, -0.49999, -0.5, -1.0, -1.4, -4.0
			}
	),
	
	RD_BU_3("RdBu-3", "ColorBrewer 3-class RdBu (Diverging, Colorblind Safe)",
			new Color(103, 169, 207), new Color(247, 247, 247), new Color(239, 138, 98)),
	
	BR_BG_3("BrBG-3", "ColorBrewer 3-class BrBG (Diverging, Colorblind Safe)",
			new Color(90, 180, 172),  new Color(245, 245, 245), new Color(216, 179, 101)),
	
	PI_YG_3("PiYG-3", "ColorBrewer 3-class PiYG (Diverging, Colorblind Safe)",
			new Color(161, 215, 106), new Color(247, 247, 247), new Color(233, 163, 201)),
	
	PU_OR_3("PuOr-3", "ColorBrewer 3-class PuOr (Diverging, Colorblind Safe)",
			new Color(153, 142, 195), new Color(247, 247, 247), new Color(241, 163, 64)),
	
	RD_YL_BU_3("RdYlBu-3", "ColorBrewer 3-class RdYlBu (Diverging, Colorblind Safe)",
			new Color(145, 191, 219), new Color(255, 255, 191), new Color(252, 141, 89)),
	;

	private final String name;
	private final String description;
	private final Color[] colors;
	private final Double[] points;
	
	ColorScheme(String name, String description, Color down, Color zero, Color up) {
		this.name = name;
		this.description = description;
		this.colors = new Color[] { up, zero, down };
		this.points = null;
	}
	
	ColorScheme(String name, String description, Color[] colors, Double[] points) {
		this.name = name;
		this.description = description;
		this.colors = colors;
		this.points = points;
	}

	public String getName() {
		return name;
	}
	
	public String getDescription() {
		return description;
	}
	
	public List<Color> getColors() {
		return Arrays.asList(colors);
	}
	
	public Color getPosColor() {
		return colors[0];
	}
	
	public Color getNegColor() {
		return colors[colors.length-1];
	}
	
	public Color getZeroColor() {
		return colors[colors.length/2];
	}
	
	/**
	 * Can be used to create more complex gradients, with fixed range points,
	 * such as the ones created by Continuous Mappings.
	 */
	public List<Double> getPoints() {
		return points != null ? Arrays.asList(points) : Collections.emptyList();
	}
}
