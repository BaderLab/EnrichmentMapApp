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

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.border.Border;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

/**
 * Created by User: risserlin Date: Feb 2, 2009 Time: 9:50:34 AM
 */
@SuppressWarnings("serial")
public class ColorRenderer extends JLabel implements TableCellRenderer {
	Border unselectedBorder = null;
	Border selectedBorder = null;
	boolean isBordered = true;
	HeatMapTableModel ogt = new HeatMapTableModel();

	public ColorRenderer() {
		setOpaque(true); //MUST do this for background to show up.
	}

	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		Color newColor = null;
		Double numberValue = 0.0;

		if((value != null) && (value instanceof ExpressionTableValue)) {
			ExpressionTableValue avalue = (ExpressionTableValue) value;
			numberValue = avalue.getExpression_value();
			newColor = avalue.getExpression_color();
		}

		TableModel tc = table.getModel();

		//Object disp=tc.getExpValueAt(row, column);
		setBackground(newColor);
		if(isBordered) {
			if(isSelected) {
				if(selectedBorder == null) {
					selectedBorder = BorderFactory.createMatteBorder(0, 0, 0, 0, table.getSelectionBackground());
				}
				setBorder(selectedBorder);
			} else {
				if(unselectedBorder == null) {
					unselectedBorder = BorderFactory.createMatteBorder(0, 0, 0, 0, table.getBackground());
				}
				setBorder(unselectedBorder);
			}
		}

		// setToolTipText("Exp Value: " + );
		setToolTipText("Exp value: " + numberValue);

		return this;
	}
}
