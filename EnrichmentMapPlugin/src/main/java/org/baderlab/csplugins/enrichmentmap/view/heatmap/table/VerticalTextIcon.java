package org.baderlab.csplugins.enrichmentmap.view.heatmap.table;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.Icon;
import javax.swing.UIManager;

public class VerticalTextIcon implements Icon {
	
	private final int width;
	private final int height;
	private final boolean clockwise;
	private final Color color;
	private final Font font;
	private final String text;
	
	public VerticalTextIcon(FontMetrics fm, Color color, boolean clockwise, String text) {
		this.width  = fm.getHeight() + 4;
		this.height = fm.stringWidth(text) + 4;
		this.clockwise = clockwise;
		this.color = color;
		this.font = fm.getFont();
		this.text = text;
	}

	@Override
	public int getIconWidth() {
		return width;
	}

	@Override
	public int getIconHeight() {
		return height;
	}

	@Override
	public void paintIcon(Component c, Graphics g, int x, int y) {
		Graphics2D g2d = (Graphics2D) g.create();
		g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		
		if(color == null)
			g2d.setColor(c != null ? c.getForeground() : UIManager.getColor("Label.foreground"));
		else
			g2d.setColor(color);
		
        g2d.setFont(font);
        
		if (clockwise) {
			g2d.rotate(Math.PI / 2);
		} else {
			g2d.rotate(-Math.PI / 2);
			g2d.translate(-c.getHeight(), c.getWidth());
		}
		
		g2d.drawString(text, 4, -4);
		g2d.dispose();
	}
}
