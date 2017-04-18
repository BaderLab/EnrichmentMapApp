package org.baderlab.csplugins.enrichmentmap.style;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ColorScheme {
	
	public static final ColorScheme CONTRASTING = new ColorScheme("CONTRASTING", "Contrasting");
	public static final ColorScheme MODULATED = new ColorScheme("MODULATED", "Modulated");
	public static final ColorScheme RAINBOW = new ColorScheme("RAINBOW", "Rainbow");
	
	public static ColorScheme DEFAULT = CONTRASTING;
	
	private final String key;
	private final String label;
	private ColorGradient gradient;
	
	public ColorScheme(final String key, final String label) {
		this.key = key;
		this.label = label;
	}
	
	public ColorScheme(final ColorGradient gradient) {
		this.key = gradient.name();
		this.label = gradient.getLabel();
		this.gradient = gradient;
	}
	
	public String getKey() {
		return key;
	}
	
	public String getLabel() {
		return label;
	}
	
	public List<Color> getColors(final int nColors) {
		List<Color> colors = null;
		
		if (nColors > 0) {
			if (gradient != null) {
				colors = gradient.getColors();
				
				if (colors.size() > nColors && nColors == 2) {
					List<Color> newColors = new ArrayList<Color>();
					newColors.add(colors.get(0));
					newColors.add(colors.get(2));
				}
			} else if (nColors > 0) {
				if (RAINBOW.getKey().equalsIgnoreCase(key))
					colors = generateRainbowColors(nColors);
				if (MODULATED.getKey().equalsIgnoreCase(key))
					colors = generateModulatedRainbowColors(nColors);
				if (CONTRASTING.getKey().equalsIgnoreCase(key))
					colors = generateContrastingColors(nColors);
			}
		}
		
		if (colors == null)
			colors = Collections.emptyList();

		return colors;
	}
	
	public static ColorScheme parse(final String input) {
		if (RAINBOW.getKey().equalsIgnoreCase(input))     return RAINBOW;
		if (MODULATED.getKey().equalsIgnoreCase(input))   return MODULATED;
		if (CONTRASTING.getKey().equalsIgnoreCase(input)) return CONTRASTING;
		
		if (ColorGradient.contains(input))
			return new ColorScheme(ColorGradient.getGradient(input));
		
		return CONTRASTING;
	}
	
	// Rainbow colors just divide the Hue wheel into n pieces and return them
	public static List<Color> generateRainbowColors(int nColors) {
		List<Color> values = new ArrayList<Color>();
		
		for (float i = 0.0f; i < (float) nColors; i += 1.0f) {
			values.add(new Color(Color.HSBtoRGB(i / (float) nColors, 1.0f, 1.0f)));
		}
		
		return values;
	}

	// Rainbow colors just divide the Hue wheel into n pieces and return them,
	// but in this case, we're going to change the saturation and intensity
	public static List<Color> generateModulatedRainbowColors(int nColors) {
		List<Color> values = new ArrayList<Color>();
		
		for (float i = 0.0f; i < (float) nColors; i += 1.0f) {
			float sat = (Math.abs(((Number) Math.cos((8 * i) / (2 * Math.PI))).floatValue()) * 0.7f) + 0.3f;
			float br = (Math.abs(((Number) Math.sin(((i) / (2 * Math.PI)) + (Math.PI / 2))).floatValue()) * 0.7f) + 0.3f;
	
			values.add(new Color(Color.HSBtoRGB(i / (float) nColors, sat, br)));
		}
		
		return values;
	}

	// This is like rainbow, but we alternate sides of the color wheel
	public static List<Color> generateContrastingColors(int nColors) {
		List<Color> values = new ArrayList<Color>();
		
		// We need to special-case the situation where we only have two colors
		if (nColors == 2) {
			values.add(new Color(Color.HSBtoRGB(0.0f, 1.0f, 1.0f)));
			values.add(new Color(Color.HSBtoRGB(0.5f, 1.0f, 1.0f)));
			return values;
		}
	
		float divs = (float) nColors;
		for (float i = 0.0f; i < divs; i += 1.0f) {
			Color rgbColor = new Color(Color.HSBtoRGB(i / divs, 1.0f, 1.0f));
			values.add(rgbColor);
			i += 1.0f;
			if (i >= divs)
				break;
			float hue = (i / divs) + 0.5f; // This moves to the opposite side of the color wheel
			
			if (hue >= 1.0f)
				hue = hue - 1.0f;
			
			rgbColor = new Color(Color.HSBtoRGB(hue, 1.0f, 1.0f));
			values.add(rgbColor);
		}
		
		return values;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((key == null) ? 0 : key.toUpperCase().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ColorScheme other = (ColorScheme) obj;
		if (key == null) {
			if (other.key != null)
				return false;
		} else if (!key.equalsIgnoreCase(other.key))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return label;
	}
}
