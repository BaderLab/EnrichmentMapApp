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

package org.baderlab.csplugins.enrichmentmap.view.heatmap;

import javax.swing.JTable;
import javax.swing.table.TableColumn;

/**
 * Created by User: vinod vasavan Date: July, 2009
 * <p>
 * public class LineNumberTable
 */
@Deprecated
public class RowNumberTable extends JTable {

	private JTable mainTable;

	public RowNumberTable(JTable table1) {
		super();
		mainTable = table1;
		setAutoCreateColumnsFromModel(false);
		setModel(mainTable.getModel());
		setSelectionModel(mainTable.getSelectionModel());
		setAutoscrolls(false);

		addColumn(new TableColumn());
		getColumnModel().getColumn(0).setCellRenderer(mainTable.getTableHeader().getDefaultRenderer());
		getColumnModel().getColumn(0).setPreferredWidth(50);
		setPreferredScrollableViewportSize(getPreferredSize());
	}

	public boolean isCellEditable(int row, int column) {
		return false;
	}

	public Object getValueAt(int row, int column) {
		return new Integer(row + 1);
	}

	public int getRowHeight(int row) {
		return mainTable.getRowHeight();
	}

}
