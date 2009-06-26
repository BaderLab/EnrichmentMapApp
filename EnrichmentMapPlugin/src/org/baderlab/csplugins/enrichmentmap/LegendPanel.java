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
 */

public class LegendPanel extends JPanel {

    /**
     * the height of the panel
     */
    private final int DIM_HEIGHT = 75;
    /**
     * the width of the panel
     */
    private final int DIM_WIDTH = 350;

    /*--------------------------------------------------------------
    Fields.
    --------------------------------------------------------------*/
    private static Double min;
    private static Double max;
    private static Color mincolor;
    private static Color maxcolor;

    /*--------------------------------------------------------------
     Constructor.
    --------------------------------------------------------------*/

    public LegendPanel(Double min, Double max, Color mincolor, Color maxcolor) {
        super();
        this.min = min;
        this.max = max;
        this.mincolor = mincolor;
        this.maxcolor = maxcolor;
        setPreferredSize(new Dimension(DIM_WIDTH, DIM_HEIGHT));
        setOpaque(false);
        //setBackground(Color.WHITE);
        //create border.
        setBorder(BorderFactory.createEtchedBorder());
    }

    /*----------------------------------------------------------------
    PAINT.
    ----------------------------------------------------------------*/


    public void paint(Graphics g) {

        Graphics2D g2D = (Graphics2D) g;
        Point2D.Float p1 = new Point2D.Float(75.f, 30.f);  //Gradient line start
        Point2D.Float p2 = new Point2D.Float(175.f, 30.f);  //Gradient line end
        Point2D.Float p3 = new Point2D.Float(175.f, 30.f);  //Gradient line start
        Point2D.Float p4 = new Point2D.Float(275.f, 30.f);  //Gradient line end
        float width = 100;
        float height = 25;
        //Need to create two gradients, one one for the max and one for the min
        GradientPaint g1 = new GradientPaint(p1, mincolor, p2, Color.WHITE, false); //Acyclic gradient
        GradientPaint g2 = new GradientPaint(p3, Color.WHITE, p4, maxcolor, false); //Acyclic gradient
        Rectangle2D.Float rect1 = new Rectangle2D.Float(p1.x , p1.y, width, height);
        Rectangle2D.Float rect2 = new Rectangle2D.Float(p3.x , p3.y, width, height);

        if(mincolor != Color.WHITE){

            g2D.setPaint(g1);
            g2D.fill(rect1);
            g2D.setPaint(Color.WHITE);
            g2D.draw(rect1);

            g2D.setPaint(Color.BLACK);
            g2D.drawString(""+min, p1.x - 50, p1.y - 12);
        }
        else{
            g2D.setPaint(Color.BLACK);
            g2D.drawString(""+min, p3.x - 50, p3.y - 12);
        }

        g2D.setPaint(g2);
        g2D.fill(rect2);
        g2D.setPaint(Color.WHITE);
        g2D.draw(rect2);

        g2D.setPaint(Color.BLACK);
        g2D.drawString("< " + max, p4.x - 10, p4.y - 12);
    }
}
