package org.baderlab.csplugins.enrichmentmap.view.util;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.io.IOException;

public abstract class IconUtil {
	
	public static final String EM_ICON = "a";
	public static final String EM_ICON_LAYER_1 = "b";
	public static final String EM_ICON_LAYER_2 = "c";
	public static final String EM_ICON_LAYER_3 = "d";
	public static final String STRING_ICON = "e";
	public static final String STRING_ICON_LAYER_1 = "f";
	public static final String STRING_ICON_LAYER_2 = "g";
	public static final String STRING_ICON_LAYER_3 = "h";
	public static final String GENEMANIA_ICON = "i";
	public static final String PC_ICON_LAYER_1 = "j";
	public static final String PC_ICON_LAYER_2 = "k";
	
	public static final String[] LAYERED_EM_ICON = new String[] { EM_ICON_LAYER_1, EM_ICON_LAYER_2, EM_ICON_LAYER_3 };
	public static final Color[] EM_ICON_COLORS = new Color[] { Color.WHITE, new Color(31, 120, 180), new Color(52, 160, 44) };
	
	public static final String[] LAYERED_STRING_ICON = new String[] { STRING_ICON_LAYER_1, STRING_ICON_LAYER_2, STRING_ICON_LAYER_3 };
	public static final Color[] STRING_ICON_COLORS = new Color[] { new Color(163, 172, 216), Color.WHITE, Color.BLACK };
	
	public static final String[] LAYERED_PC_ICON = new String[] { PC_ICON_LAYER_1, PC_ICON_LAYER_2 };
	public static final Color[] PC_ICON_COLORS = new Color[] { new Color(42, 62, 80), new Color(22, 160, 133) };
	
	public static final Color GENEMANIA_ICON_COLOR = Color.BLACK;
	
	private static Font iconFont;

	static {
		try {
			iconFont = Font.createFont(Font.TRUETYPE_FONT, IconUtil.class.getResourceAsStream("/fonts/enrichmentmap.ttf"));
		} catch (FontFormatException e) {
			throw new RuntimeException();
		} catch (IOException e) {
			throw new RuntimeException();
		}
	}
	
	public static Font getIconFont(float size) {
		return iconFont.deriveFont(size);
	}

	private IconUtil() {
		// ...
	}
}
