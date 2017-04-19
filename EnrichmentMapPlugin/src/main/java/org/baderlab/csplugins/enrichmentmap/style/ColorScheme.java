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
	RD_BU("RdBu",      new Color(103, 169, 207), new Color(247, 247, 247), new Color(239, 138, 98)),
	BR_BG("BrBG",      new Color(90, 180, 172),  new Color(245, 245, 245), new Color(216, 179, 101)),
	PI_YG("PiYG",      new Color(161, 215, 106), new Color(247, 247, 247), new Color(233, 163, 201)),
	PU_OR("PuOr",      new Color(153, 142, 195), new Color(247, 247, 247), new Color(241, 163, 64)),
	RD_YL_BU("RdYlBu", new Color(145, 191, 219), new Color(255, 255, 191), new Color(252, 141, 89)),
	// Sequential - Multi-hue
	BU_PU("BuPu",      new Color(136, 86, 167),  new Color(158, 188, 218), new Color(224, 236, 244)),
	OR_RD("OrRd",      new Color(227, 74, 51),   new Color(253, 187, 132), new Color(254, 232, 200)),
	YL_GN("YlGn",      new Color(49, 163, 84),   new Color(173, 221, 142), new Color(247, 252, 185)),
	YL_GN_B("YlGnB",   new Color(44, 127, 184),  new Color(127, 205, 187), new Color(237, 248, 177)),
	YL_OR_BR("YlOrBr", new Color(217, 95, 14),   new Color(254, 196, 79),  new Color(255, 247, 188)),
	;

	private String label;
	private final Color up, zero, down;
	
	private static Map<String, ColorScheme>cMap;

	ColorScheme(final String label, final Color down, final Color zero, final Color up) {
		this.label = label;
		this.up = up;
		this.down = down;
		this.zero = zero;
		addGradient(this);
	}

	public String getLabel() {
		return label;
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
