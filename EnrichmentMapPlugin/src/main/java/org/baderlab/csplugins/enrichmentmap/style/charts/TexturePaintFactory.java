package org.baderlab.csplugins.enrichmentmap.style.charts;

import java.awt.TexturePaint;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

public class TexturePaintFactory {
	
	private BufferedImage img;
	
	public TexturePaintFactory(final BufferedImage img) {
		this.img = img;
	}

	public TexturePaint getPaint(Rectangle2D bound) {
		return new TexturePaint(img, bound);
	}

	public BufferedImage getImage() {
		return img;
	}
}
