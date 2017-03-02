package org.baderlab.csplugins.enrichmentmap.view.util;

import java.awt.Color;

public final class ColorUtil {

	private ColorUtil() {}
	
	public static Color getContrastingColor(final Color color) {
		int d = 0;
		// Counting the perceptive luminance - human eye favors green color...
		final double a = 1 - (0.299 * color.getRed() + 0.587 * color.getGreen() + 0.114 * color.getBlue()) / 255;

		if (a < 0.5)
			d = 0; // bright colors - black font
		else
			d = 255; // dark colors - white font

		return new Color(d, d, d);
	}
	
	public static String toHexString(final Color color) {
		final int rgb = color.getRGB();
		final String hex = String.format("#%06X", (0xFFFFFF & rgb));

		return hex;
	}
}
