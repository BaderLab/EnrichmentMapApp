package org.baderlab.csplugins.enrichmentmap.view.util;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;

import javax.swing.AbstractButton;
import javax.swing.Icon;
import javax.swing.UIManager;

public class TextIcon implements Icon {
	
	private final Color TRANSPARENT_COLOR = new Color(255, 255, 255, 0);
	
	private final String text;
	private final Font font;
	private final Color color;
	private final int width;
	private final int height;
	
	public TextIcon(String text, Font font, Color color, int width, int height) {
		this.text = text;
		this.font = font;
		this.color = color != null ? color : UIManager.getColor("CyColor.complement(-2)");
		this.width = width;
		this.height = height;
	}
	
	@Override
	public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHints(new RenderingHints(RenderingHints.KEY_TEXT_ANTIALIASING,
				RenderingHints.VALUE_TEXT_ANTIALIAS_ON));
        
        int xx = c.getWidth();
        int yy = c.getHeight();
        g2d.setPaint(TRANSPARENT_COLOR);
        g2d.fillRect(0, 0, xx, yy);
        
        Color fg = color;
        
        if (c instanceof AbstractButton) {
	        	if (!c.isEnabled())
	        		fg = UIManager.getColor("Label.disabledForeground");
	        	else if (((AbstractButton) c).getModel().isPressed())
	        		fg = fg.darker();
        }
        
        g2d.setPaint(fg);
        g2d.setFont(font);
        drawText(g2d, c, x, y);
        
        g2d.dispose();
	}
	
	@Override
	public int getIconWidth() {
		return width;
	}

	@Override
	public int getIconHeight() {
		return height;
	}
	
	public String getText() {
		return text;
	}
	
	public Font getFont() {
		return font;
	}
	
	public Color getColor() {
		return color;
	}
	
	protected void drawText(Graphics g, Component c, int x, int y) {
		FontMetrics fm = g.getFontMetrics(font);
		Rectangle2D rect = fm.getStringBounds(text, g);

		int textHeight = (int) rect.getHeight();
		int textWidth = (int) rect.getWidth();

		// Center text horizontally and vertically
		int xx = x + (getIconWidth() - textWidth) / 2;
		int yy = y + (getIconHeight() - textHeight) / 2 + fm.getAscent();

		g.drawString(text, xx, yy);
	}
}

