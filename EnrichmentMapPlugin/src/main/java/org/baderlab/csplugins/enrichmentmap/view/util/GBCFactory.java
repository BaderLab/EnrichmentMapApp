package org.baderlab.csplugins.enrichmentmap.view.util;

import java.awt.GridBagConstraints;
import java.awt.Insets;

public class GBCFactory {

	private GridBagConstraints gbc = new GridBagConstraints();
	
	private GBCFactory() {
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
	}
	
	public static GBCFactory grid(int x, int y) {
		GBCFactory factory = new GBCFactory();
		factory.gbc.gridx = x;
		factory.gbc.gridy = y;
		return factory;
	}
	
	public GridBagConstraints get() {
		return gbc;
	}
	
	public GBCFactory weightx(double weightx) {
		gbc.weightx = weightx;
		return this;
	}
	
	public GBCFactory weighty(double weighty) {
		gbc.weighty = weighty;
		return this;
	}
	
	public GBCFactory gridwidth(int gridwidth) {
		gbc.gridwidth = gridwidth;
		return this;
	}
	
	public GBCFactory gridheight(int gridheight) {
		gbc.gridheight = gridheight;
		return this;
	}
	
	public GBCFactory anchor(int anchor) {
		gbc.anchor = anchor;
		return this;
	}
	
	public GBCFactory fill(int fill) {
		gbc.fill = fill;
		return this;
	}
	
	public GBCFactory insets(int top, int left, int bottom, int right) {
		gbc.insets = new Insets(top, left, bottom, right);
		return this;
	}
	
	public GBCFactory insets(int v) {
		return insets(v,v,v,v);
	}
	
	
}
