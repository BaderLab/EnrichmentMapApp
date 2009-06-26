package org.baderlab.csplugins.enrichmentmap;

import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Created by
 * User: risserlin
 * Date: Feb 12, 2009
 * Time: 12:22:50 PM
 */
public class ColumnHeaderVerticalRenderer extends DefaultTableCellRenderer {


    @Override
    public Component getTableCellRendererComponent(JTable table,
                                                   Object value, boolean isSelected, boolean hasFocus,
                                                   int row, int column) {

        JLabel label = new JLabel();

        label.setBorder(UIManager.getBorder("TableHeader.cellBorder"));
        label.setBackground(this.getBackground());
        label.setForeground(UIManager.getColor("TableHeader.foreground"));
        label.setFont(UIManager.getFont("TableHeader.font"));

        Icon icon = VerticalCaption.getVerticalCaption(label, value.toString(), false);

        label.setIcon(icon);
        label.setVerticalAlignment(JLabel.BOTTOM);
        label.setHorizontalAlignment(JLabel.CENTER);

        return label;
    }
}

class VerticalCaption {

    static Icon getVerticalCaption(JComponent component, String caption, boolean clockwise) {
        Font f = component.getFont();
        FontMetrics fm = component.getFontMetrics(f);
        int captionHeight = fm.getHeight();
        int captionWidth = fm.stringWidth(caption);
        BufferedImage bi = new BufferedImage(captionHeight + 4,
                captionWidth + 4, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = (Graphics2D) bi.getGraphics();

        g.setColor(component.getBackground()); // transparent
        g.fillRect(0, 0, bi.getWidth(), bi.getHeight());

        g.setColor(component.getForeground());
        g.setFont(f);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        if (clockwise) {
            g.rotate(Math.PI / 2);
        } else {

            g.rotate(-Math.PI / 2);
            g.translate(-bi.getHeight(), bi.getWidth());
        }
        g.drawString(caption, 2, -6);


        Icon icon = new ImageIcon(bi);
        return icon;
    }
}