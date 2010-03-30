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

package org.baderlab.csplugins.enrichmentmap;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

/**
 * Created by
 * User: risserlin
 * Date: Feb 5, 2009
 * Time: 3:55:52 PM
 * <p>
 * enrichment map legend panel
 */

public class LegendPanel extends JPanel {

    /**
     * the height of the panel
     */
    private final int DIM_HEIGHT = 35;
    /**
     * the width of the panel
     */
    private final int DIM_WIDTH = 150;

    /*--------------------------------------------------------------
    Fields.
    --------------------------------------------------------------*/
    private static Color mincolor;
    private static Color maxcolor;
    private String phenotype1;
    private String phenotype2;


    /**
     * Class constructor
     *
     * @param mincolor
     * @param maxcolor
     * @param phenotype1
     * @param phenotype2
     */
    public LegendPanel(Color mincolor, Color maxcolor,String phenotype1,String phenotype2) {
        super();
        this.mincolor = mincolor;
        this.maxcolor = maxcolor;
        this.phenotype1 = phenotype1;
        this.phenotype2 = phenotype2;
        setPreferredSize(new Dimension(DIM_WIDTH, DIM_HEIGHT));
        setOpaque(false);
        //setBackground(Color.WHITE);
        //create border.
        setBorder(BorderFactory.createEtchedBorder());
    }

    /*----------------------------------------------------------------
    PAINT.
    ----------------------------------------------------------------*/

    /**
     * Paint legend
     *
     * @param g
     */
  public void paint(Graphics g) {

        Graphics2D g2D = (Graphics2D) g;
        Point2D.Float p1 = new Point2D.Float(00.f, 0.f);  //Gradient line start
        Point2D.Float p2 = new Point2D.Float(30.f, 0.f);  //Gradient line end

        //empty white box
        Point2D.Float p3 = new Point2D.Float(30.f, 0.f);  //Gradient line start
        Point2D.Float p4 = new Point2D.Float(60.f, 0.f);  //Gradient line end

        Point2D.Float p5 = new Point2D.Float(60.f, 0.f);  //Gradient line start
        Point2D.Float p6 = new Point2D.Float(90.f, 0.f);  //Gradient line end

        Point2D.Float p7 = new Point2D.Float(90.f, 0.f);  //Gradient line start
        Point2D.Float p8 = new Point2D.Float(120.f, 0.f);  //Gradient line end
        float width1 = 30;
        float width2 = 30;
        float height = 15;
        //Need to create two gradients, one one for the max and one for the min
        GradientPaint g1 = new GradientPaint(p1, mincolor, p2, Color.WHITE, false); //Acyclic gradient
        GradientPaint g2 = new GradientPaint(p7, Color.WHITE, p8, maxcolor, false); //Acyclic gradient
        Rectangle2D.Float rect1 = new Rectangle2D.Float(p1.x , p1.y, width1, height);
        Rectangle2D.Float rect2 = new Rectangle2D.Float(p3.x , p3.y, width2, height);
        Rectangle2D.Float rect3 = new Rectangle2D.Float(p5.x , p5.y, width2, height);
        Rectangle2D.Float rect4 = new Rectangle2D.Float(p7.x , p7.y, width1, height);

        if(mincolor != Color.WHITE){

            g2D.setPaint(g1);
            g2D.fill(rect1);
            g2D.setPaint(Color.WHITE);
            g2D.draw(rect1);

            //make a white block
            g2D.setPaint(Color.WHITE);
            g2D.fill(rect2);
            g2D.draw(rect2);

            g2D.setPaint(Color.BLACK);
            //g2D.drawString(""+min, p1.x , p1.y - 5);
            g2D.drawString(phenotype1,p1.x, p1.y+height+10);


        }
        else{
            g2D.setPaint(Color.BLACK);
            //g2D.drawString(""+min, p5.x , p5.y - 5);
            g2D.drawString(phenotype1,p5.x, p5.y+height+10);
        }

        //make a white block
        g2D.setPaint(Color.WHITE);
        g2D.fill(rect3);
        g2D.draw(rect3);

        g2D.setPaint(g2);
        g2D.fill(rect4);
        g2D.setPaint(Color.WHITE);
        g2D.draw(rect4);

        g2D.setPaint(Color.BLACK);
        //g2D.drawString("< " + max, p7.x , p8.y - 5);
        g2D.drawString(phenotype2,p7.x, p7.y+ height +10);
    }
}