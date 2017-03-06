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

package org.baderlab.csplugins.enrichmentmap.view.parameters;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import javax.swing.JPanel;
import javax.swing.UIManager;

import org.cytoscape.util.swing.LookAndFeelUtil;

/**
 * Created by
 * User: risserlin
 * Date: Feb 5, 2009
 * Time: 3:55:52 PM
 * <p>
 * enrichment map legend panel
 */

@SuppressWarnings("serial")
public class ColorLegendPanel extends JPanel {

	private static final int WIDTH = 150;
	private static final int HEIGHT = 36;
	
    private final Color minColor;
    private final Color maxColor;
    private final String phenotype1;
    private final String phenotype2;

	public ColorLegendPanel(Color minColor, Color maxColor, String phenotype1, String phenotype2) {
        this.minColor = minColor;
        this.maxColor = maxColor;
        this.phenotype1 = phenotype1;
        this.phenotype2 = phenotype2;
        
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setOpaque(false);
    }

	@Override
	public void paint(Graphics g) {
		final int w = getWidth();
		final int h = getHeight();
		
		if (w <= 0 || h <= 0)
			return;
		
		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		
		float ww = w / 5.f;
		float hPad = ww / 2.f; // To center the legend horizontally
        
        Point2D.Float p1 = new Point2D.Float(hPad, 0.f);  //Gradient line start
        Point2D.Float p2 = new Point2D.Float(hPad + ww, 0.f);  //Gradient line end

        //empty white box
        Point2D.Float p3 = new Point2D.Float(hPad + ww, 0.f);  //Gradient line start

        Point2D.Float p5 = new Point2D.Float(hPad + 2 * ww, 0.f);  //Gradient line start

        Point2D.Float p7 = new Point2D.Float(hPad + 3 * ww, 0.f);  //Gradient line start
        Point2D.Float p8 = new Point2D.Float(hPad + 4 * ww, 0.f);  //Gradient line end
        
        float w1 = 30;
        float w2 = 30;
        float hh = h / 2;
        
        // Need to create two gradients, one one for the max and one for the min
        GradientPaint g1 = new GradientPaint(p1, minColor, p2, Color.WHITE, false); //Acyclic gradient
        GradientPaint g2 = new GradientPaint(p7, Color.WHITE, p8, maxColor, false); //Acyclic gradient
        Rectangle2D.Float rect1 = new Rectangle2D.Float(p1.x , p1.y, w1, hh);
        Rectangle2D.Float rect2 = new Rectangle2D.Float(p3.x , p3.y, w2, hh);
        Rectangle2D.Float rect3 = new Rectangle2D.Float(p5.x , p5.y, w2, hh);
        Rectangle2D.Float rect4 = new Rectangle2D.Float(p7.x , p7.y, w1, hh);

        g2d.setFont(getLabelFont());
        float tyOffset = hh + h / 3.f; // Text y offset
        
		if (minColor != Color.WHITE) {
			g2d.setPaint(g1);
			g2d.fill(rect1);
			g2d.setPaint(Color.WHITE);
			g2d.draw(rect1);

			// make a white block
			g2d.setPaint(Color.WHITE);
			g2d.fill(rect2);
			g2d.draw(rect2);

			g2d.setPaint(getLabelForeground());
			g2d.drawString(phenotype1, p1.x, p1.y + tyOffset);
		} else {
			g2d.setPaint(getLabelForeground());
			g2d.drawString(phenotype1, p5.x, p5.y + tyOffset);
		}

        // Make a white block
        g2d.setPaint(Color.WHITE);
        g2d.fill(rect3);
        g2d.draw(rect3);

		g2d.setPaint(g2);
		g2d.fill(rect4);
		g2d.setPaint(Color.WHITE);
		g2d.draw(rect4);

		g2d.setPaint(getLabelForeground());
		g2d.drawString(phenotype2, p7.x, p7.y + tyOffset);
	}

	private static Font getLabelFont() {
		return UIManager.getFont("Label.font").deriveFont(LookAndFeelUtil.getSmallFontSize());
	}

	private static Color getLabelForeground() {
		return UIManager.getColor("Label.foreground");
	}
}