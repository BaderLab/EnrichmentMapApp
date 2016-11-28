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

package org.baderlab.csplugins.enrichmentmap.heatmap;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.Optional;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * Created by
 * User: risserlin
 * Date: Feb 12, 2009
 * Time: 12:22:50 PM
 * <p>
 * Flips column headers to vertical position
 */
@SuppressWarnings("serial")
public class ColumnHeaderVerticalRenderer extends DefaultTableCellRenderer {

	private Optional<Color> labelBackgroundColor;
	
	public ColumnHeaderVerticalRenderer(Color labelBackgroundColor) {
		this.labelBackgroundColor = Optional.ofNullable(labelBackgroundColor);
	}
	
	public ColumnHeaderVerticalRenderer() {
		this.labelBackgroundColor = Optional.empty();
	}
	

    @Override
    public Component getTableCellRendererComponent(JTable table,
                                                   Object value, boolean isSelected, boolean hasFocus,
                                                   int row, int column) {
        JLabel label = new JLabel();

        label.setBorder(UIManager.getBorder("TableHeader.cellBorder"));
        label.setBackground(this.getBackground());
        label.setForeground(UIManager.getColor("TableHeader.foreground"));
        label.setFont(UIManager.getFont("TableHeader.font"));

        Icon icon = getVerticalCaption(label, value.toString(), false);

        label.setIcon(icon);
        label.setVerticalAlignment(JLabel.BOTTOM);
        label.setHorizontalAlignment(JLabel.CENTER);

        return label;
    }

    
    private Icon getVerticalCaption(JComponent component, String caption, boolean clockwise) {
		Font f = component.getFont();
		FontMetrics fm = component.getFontMetrics(f);
		int captionHeight = fm.getHeight();
		int captionWidth = fm.stringWidth(caption);
		BufferedImage bi = new BufferedImage(captionHeight + 4, captionWidth + 4, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = (Graphics2D) bi.getGraphics();

        if(labelBackgroundColor.isPresent()) {
	        g.setColor(labelBackgroundColor.get()); 
	        g.fillRect(0, 0, bi.getWidth(), bi.getHeight());
        }
        
        g.setColor(component.getForeground());
        g.setFont(f);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

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