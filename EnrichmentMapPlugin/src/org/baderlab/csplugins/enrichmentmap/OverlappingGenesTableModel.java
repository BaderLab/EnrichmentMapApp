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

import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

/**
 * Created by
 * User: risserlin
 * Date: Jan 30, 2009
 * Time: 10:56:40 AM
 */
public class OverlappingGenesTableModel  extends AbstractTableModel implements TableModelListener  {
				
				
                private Object[] columnNames;
                private Object[][] data;
                private Object[][] expValue;

                public OverlappingGenesTableModel() {
					// TODO Auto-generated constructor stub
				}
                public OverlappingGenesTableModel(Object[] columnNames,Object[][] data,Object[][] expValue){
                    super();
                    this.columnNames = columnNames;
                    this.data=data;
                    this.expValue=expValue;
                    }
                
                public int getColumnCount() {
                    return columnNames.length;

                }

                public int getRowCount() {
                    return data.length;
                }

                public String getColumnName(int col) {
                     return (String)columnNames[col];
                }

                public Object getValueAt(int row, int col) {
                	  return data[row][col];
                }

                /*
                 * JTable uses this method to determine the default renderer/
                 * editor for each cell.  If we didn't implement this method,
                 * then the last column would contain text ("true"/"false"),
                 * rather than a check box.
                 * 
                 * Used in TableSort to identify the class of the column
                 */
                public Class getColumnClass(int c) {
                    return getValueAt(0, c).getClass();
                }

                /*
                 * Don't need to implement this method unless your table's
                 * editable.
                 */
                public boolean isCellEditable(int row, int col) {
                    return false;

                }
                
               /*
                * Don't need to implement this method unless your table's
                * data can change.
                */
                public void setValueAt(Object value, int row, int col) {
                    data[row][col] = value;
                    fireTableCellUpdated(row, col);
                }

				public void setExpValueAt(Object value, int row, int col) {
					expValue [row][col]= value;
				}

				public Object getExpValueAt(int row, int col) {
				return expValue[row][col];
				}


				public void tableChanged(TableModelEvent e) {
					// TODO Auto-generated method stub
					fireTableChanged(e);
				}
				
    }

