/**
 **                       EnrichmentMap Cytoscape Plugin
 **
 ** Copyright (c) 2008-2009 Bader Lab, Donnelly Centre for Cellular and Biomolecular 
 ** Research, University of Toronto
 **
 ** Contact: http://www.baderlab.org
 **
 ** Code written by: Ruth Isserlin
 ** Authors: Daniele Merico, Ruth Isserlin, Oliver Stueker, Gary D. Bader
 **
 ** This library is free software; you can redistribute it and/or modify it
 ** under the terms of the GNU Lesser General Public License as published
 ** by the Free Software Foundation; either version 2.1 of the License, or
 ** (at your option) any later version.
 **
 ** This library is distributed in the hope that it will be useful, but
 ** WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 ** MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 ** documentation provided hereunder is on an "as is" basis, and
 ** University of Toronto
 ** has no obligations to provide maintenance, support, updates, 
 ** enhancements or modifications.  In no event shall the
 ** University of Toronto
 ** be liable to any party for direct, indirect, special,
 ** incidental or consequential damages, including lost profits, arising
 ** out of the use of this software and its documentation, even if
 ** University of Toronto
 ** has been advised of the possibility of such damage.  
 ** See the GNU Lesser General Public License for more details.
 **
 ** You should have received a copy of the GNU Lesser General Public License
 ** along with this library; if not, write to the Free Software Foundation,
 ** Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 **
 **/

// $Id$
// $LastChangedDate$
// $LastChangedRevision$
// $LastChangedBy$
// $HeadURL$

package org.baderlab.csplugins.enrichmentmap.view.legend;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;

import org.baderlab.csplugins.enrichmentmap.view.util.SwingUtil;

@SuppressWarnings("serial")
public class ColorLegendPanel extends JPanel {

	private static final int WIDTH = 150;
	private static final int HEIGHT = 36;

	private final Color minColor;
	private final Color maxColor;

	private final boolean legacy;

	public ColorLegendPanel(Color minColor, Color maxColor, String phenotype1, String phenotype2) {
		this(minColor, maxColor, phenotype1, phenotype2, true);
	}
	
	public ColorLegendPanel(Color minColor, Color maxColor, String phenotype1, String phenotype2, boolean legacy) {
		this.legacy = legacy;
		this.minColor = minColor;
		this.maxColor = maxColor;

		setPreferredSize(new Dimension(WIDTH, HEIGHT));
		setOpaque(false);

		JPanel gradientRectangle = new GradientRectanglePanel();
		JPanel gradientParentPanel = new JPanel(new BorderLayout());
		gradientParentPanel.add(gradientRectangle, BorderLayout.CENTER);
		gradientParentPanel.setBorder(BorderFactory.createLineBorder(UIManager.getColor("Separator.foreground")));
		
		JLabel minLabel = new JLabel(phenotype1);
		JLabel maxLabel = new JLabel(phenotype2);
		SwingUtil.makeSmall(minLabel, maxLabel);
		JPanel labelPanel = new JPanel(new BorderLayout());
		labelPanel.add(minLabel, BorderLayout.WEST);
		labelPanel.add(maxLabel, BorderLayout.EAST);
		labelPanel.setOpaque(false);

		setLayout(new BorderLayout());
		add(gradientParentPanel, BorderLayout.CENTER);
		add(labelPanel, BorderLayout.SOUTH);
	}

	private class GradientRectanglePanel extends JPanel {
		@Override
		public void paint(Graphics g) {
			if (getWidth() <= 0 || getHeight() <= 0)
				return;

			Graphics2D g2d = (Graphics2D) g;
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

			if (legacy)
				paintLegacyOneDatasetGradient(g2d);
			else
				paintNormalGradient(g2d);
		}

		private void paintNormalGradient(Graphics2D g2d) {
			final int w = getWidth();
			final int h = getHeight();
			
			Point2D.Float p1 = new Point2D.Float(0f, 0f); // Gradient line start
			Point2D.Float p2 = new Point2D.Float(w, 0f); // Gradient line end
			
			GradientPaint g1 = new GradientPaint(p1, minColor, p2, maxColor, false); // Acyclic gradient
			Rectangle2D.Float rect1 = new Rectangle2D.Float(p1.x, p1.y, w, h);
			
			g2d.setPaint(g1);
			g2d.fill(rect1);
		}
		
		private void paintLegacyOneDatasetGradient(Graphics2D g2d) {
			final int w = getWidth();
			final int h = getHeight();
			float ww = w / 5f;
			
			// first box
			Point2D.Float p1 = new Point2D.Float(0f, 0f); // Gradient line start
			Point2D.Float p2 = new Point2D.Float(ww, 0f); // Gradient line end
			
			// empty white box
			Point2D.Float p3 = new Point2D.Float(ww * 4, 0f); // Gradient line start
			Point2D.Float p4 = new Point2D.Float(ww * 5, 0f); // Gradient line start
			
			GradientPaint g1 = new GradientPaint(p1, minColor, p2, Color.WHITE, false); // Acyclic gradient
			GradientPaint g2 = new GradientPaint(p3, Color.WHITE, p4, maxColor, false); // Acyclic gradient
			
			Rectangle2D.Float rect1 = new Rectangle2D.Float(p1.x, p1.y, ww, h);
			Rectangle2D.Float rect2 = new Rectangle2D.Float(p2.x, p2.y, ww*3, h);
			Rectangle2D.Float rect3 = new Rectangle2D.Float(p3.x, p3.y, ww, h);
			
			g2d.setPaint(g1);
			g2d.fill(rect1);
			g2d.setPaint(Color.WHITE);
			g2d.fill(rect2);
			g2d.setPaint(g2);
			g2d.fill(rect3);
			
		}
	}

}