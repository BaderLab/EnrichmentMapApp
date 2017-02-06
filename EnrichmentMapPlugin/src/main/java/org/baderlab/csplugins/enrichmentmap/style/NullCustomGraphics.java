package org.baderlab.csplugins.enrichmentmap.style;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.List;

import javax.swing.ImageIcon;

import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.customgraphics.CustomGraphicLayer;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics;

/**
 * Null object for Custom Graphics. This is used to reset custom graphics on node views.
 */
public class NullCustomGraphics<T extends CustomGraphicLayer> implements CyCustomGraphics<T> {
	
	public static Image DEF_IMAGE = emptyIcon(24, 24).getImage();
	
	private static final CyCustomGraphics<CustomGraphicLayer> NULL = new NullCustomGraphics<>();
	
	protected float fitRatio = 0.9f;
	protected int width = 50;
	protected int height = 50;

	public static CyCustomGraphics<CustomGraphicLayer> getNullObject() {
		return NULL;
	}

	// Human readable name of this null object.
	private static final String NAME = "[ EMPTY EM CHART ]";

	private NullCustomGraphics() {
	}

	@Override
	public String toString() {
		return "None";
	}

	@Override
	public Image getRenderedImage() {
		return DEF_IMAGE;
	}

	@Override
	public String toSerializableString() {
		return "";
	}

	@Override
	public Long getIdentifier() {
		return 0L;
	}

	@Override
	public void setIdentifier(Long id) {
		// Cannot be changed...
	}

	@Override
	public String getDisplayName() {
		return NAME;
	}

	@Override
	public void setDisplayName(String displayName) {
		// Cannot be changed...
	}

	@Override
	public List<T> getLayers(CyNetworkView networkView, View<? extends CyIdentifiable> grView) {
		return Collections.emptyList();
	}

	@Override
	public int getWidth() {
		return width;
	}

	@Override
	public int getHeight() {
		return height;
	}

	@Override
	public void setWidth(int width) {
		this.width = width;
	}

	@Override
	public void setHeight(int height) {
		this.height = height;
	}

	@Override
	public float getFitRatio() {
		return fitRatio;
	}

	@Override
	public void setFitRatio(float ratio) {
		this.fitRatio = ratio;
	}
	
	public static ImageIcon emptyIcon(final int width, final int height) {
		final BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		
		return new ImageIcon(bi);
	}
}
