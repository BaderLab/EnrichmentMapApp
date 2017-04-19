package org.baderlab.csplugins.enrichmentmap.style;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public enum ColorScheme {
	
	// 3-color ColorBrewer schemes (color-blind safe and print friendly):
	// Diverging
	RD_BU("RdBu",      "ColorBrewer 3-class RdBu (Diverging, Colorblind Safe)",   new Color(103, 169, 207), new Color(247, 247, 247), new Color(239, 138, 98)),
	BR_BG("BrBG",      "ColorBrewer 3-class BrBG (Diverging, Colorblind Safe)",   new Color(90, 180, 172),  new Color(245, 245, 245), new Color(216, 179, 101)),
	PI_YG("PiYG",      "ColorBrewer 3-class PiYG (Diverging, Colorblind Safe)",   new Color(161, 215, 106), new Color(247, 247, 247), new Color(233, 163, 201)),
	PU_OR("PuOr",      "ColorBrewer 3-class PuOr (Diverging, Colorblind Safe)",   new Color(153, 142, 195), new Color(247, 247, 247), new Color(241, 163, 64)),
	RD_YL_BU("RdYlBu", "ColorBrewer 3-class RdYlBu (Diverging, Colorblind Safe)", new Color(145, 191, 219), new Color(255, 255, 191), new Color(252, 141, 89)),
	// Sequential - Multi-hue
	BU_PU("BuPu",      "ColorBrewer 3-class BuPu (Sequential, Colorblind Safe)",   new Color(136, 86, 167),  new Color(158, 188, 218), new Color(224, 236, 244)),
	OR_RD("OrRd",      "ColorBrewer 3-class OrRd (Sequential, Colorblind Safe)",   new Color(227, 74, 51),   new Color(253, 187, 132), new Color(254, 232, 200)),
	YL_GN("YlGn",      "ColorBrewer 3-class YlGn (Sequential, Colorblind Safe)",   new Color(49, 163, 84),   new Color(173, 221, 142), new Color(247, 252, 185)),
	YL_GN_B("YlGnB",   "ColorBrewer 3-class YlGnBu (Sequential, Colorblind Safe)", new Color(44, 127, 184),  new Color(127, 205, 187), new Color(237, 248, 177)),
	YL_OR_BR("YlOrBr", "ColorBrewer 3-class YlOrRd (Sequential, Colorblind Safe)", new Color(217, 95, 14),   new Color(254, 196, 79),  new Color(255, 247, 188)),
	;

	private final String name;
	private final String description;
	private final Color up, zero, down;
	
	private static Map<String, ColorScheme>cMap;

	ColorScheme(final String name, final String description, final Color down, final Color zero, final Color up) {
		this.name = name;
		this.description = description;
		this.up = up;
		this.down = down;
		this.zero = zero;
		addGradient(this);
	}

	public String getName() {
		return name;
	}
	
	public String getDescription() {
		return description;
	}
	
	public List<Color> getColors() {
		final List<Color> retColors = new ArrayList<>();
		retColors.add(up);
		if (zero != null) retColors.add(zero);
		retColors.add(down);

		return retColors;
	}

	public static boolean contains(final String name) {
		return name != null && cMap.containsKey(normalize(name));
	}
	
	public static ColorScheme getGradient(final String name) {
		return cMap.get(normalize(name));
	}
	
	public static List<Color> getColors(String name) {
		name = normalize(name);
		
		if (name != null && cMap.containsKey(name))
			return cMap.get(name).getColors();
		
		return Collections.emptyList();
	}
	
	private void addGradient(final ColorScheme cg) {
		if (cMap == null) cMap = new HashMap<>();
		cMap.put(normalize(cg.name()), cg);
	}
	
	private static String normalize(final String name) {
		return name != null ? name.toUpperCase().replaceAll("[-_]", "") : null;
	}
}
