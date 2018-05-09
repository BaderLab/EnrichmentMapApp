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
package org.baderlab.csplugins.enrichmentmap.view.heatmap.table;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.Optional;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.table.TableCellRenderer;

import org.baderlab.csplugins.enrichmentmap.model.EMDataSet;
import org.baderlab.csplugins.enrichmentmap.view.util.SwingUtil;
import org.baderlab.csplugins.enrichmentmap.view.util.TextIcon;

/**
 * Flips column headers to vertical position
 */
public class ColumnHeaderVerticalRenderer implements TableCellRenderer {

	public static final int MIN_HEIGHT = 65;
	
	private Optional<Color> labelBackgroundColor;
	
	public ColumnHeaderVerticalRenderer(Color labelBackgroundColor) {
		this.labelBackgroundColor = Optional.ofNullable(labelBackgroundColor);
	}
	
	public ColumnHeaderVerticalRenderer() {
		this.labelBackgroundColor = Optional.empty();
	}
	
	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
		HeatMapTableModel model = (HeatMapTableModel) table.getModel();
		EMDataSet dataset = model.getDataSet(col);
		
		JLabel verticalLabel = createVerticalLabel(value.toString());
		
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(verticalLabel, BorderLayout.CENTER);
		Color barColor = dataset.getColor();
		
		if (barColor != null) {
			JPanel barPanel = new JPanel();
			barPanel.setPreferredSize(new Dimension(verticalLabel.getWidth(), 5));
			barPanel.setBackground(barColor);
			panel.add(barPanel, BorderLayout.NORTH);
		}
		
		panel.setBackground(labelBackgroundColor.isPresent() ? labelBackgroundColor.get()
				: UIManager.getColor("TableHeader.background"));
		panel.setToolTipText(value.toString() + " - " + dataset.getName());
		
		return panel;
	}
	
	private JLabel createVerticalLabel(String value) {
		JLabel label = new JLabel();
		label.setBorder(UIManager.getBorder("TableHeader.cellBorder"));
		label.setToolTipText(value);
		
		// Create vertical text label
		String s = SwingUtil.abbreviate(value, 14);
		Font font = UIManager.getFont("TableHeader.font");
		Color foreground = UIManager.getColor("TableHeader.foreground");
		
		label.setIcon(new VerticalTextIcon(s, label.getFontMetrics(font), foreground, false));
		label.setVerticalAlignment(JLabel.BOTTOM);
		label.setHorizontalAlignment(JLabel.CENTER);
		
		Dimension prefSize = label.getPreferredSize();
		if(prefSize.height < MIN_HEIGHT) {
			label.setPreferredSize(new Dimension(prefSize.width, MIN_HEIGHT));
		}
		
		return label;
	}
	
	private class VerticalTextIcon extends TextIcon {

		private final boolean clockwise;
		
		public VerticalTextIcon(String text, FontMetrics fm, Color color, boolean clockwise) {
			super(text, fm.getFont(), color, fm.getHeight() + 4, fm.stringWidth(text) + 4);
			this.clockwise = clockwise;
		}
		
		@Override
		protected void drawText(String text, Font font, Graphics g, Component c, int x, int y) {
			Graphics2D g2d = (Graphics2D) g.create();
			g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			
			if (clockwise) {
				g2d.rotate(Math.PI / 2);
			} else {
				g2d.rotate(-Math.PI / 2);
				g2d.translate(-c.getHeight(), c.getWidth());
			}
			
			g2d.drawString(text, 4, -4);
			g2d.dispose();
		}
	}
}