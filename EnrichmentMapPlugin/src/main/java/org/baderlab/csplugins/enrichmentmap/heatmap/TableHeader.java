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

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JTable;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;


/**
 * Created by
 * User: vinod vasavan
 * Date: July 2009
 * <p>
 * Mouse click active table header
 */
public class TableHeader extends MouseAdapter{

	private JTable table;
	private TableSort sort;
	private boolean isSort=false;
    private HeatMapParameters hmparams;

    public TableHeader(TableSort sort,JTable table, HeatMapParameters hmparams){
        this.sort = sort;
        this.table = table;
        this.hmparams = hmparams;

    }
    public TableHeader(){

    }
	
    public static void mount(TableSort sort,JTable table){
		TableHeader header = new TableHeader();
		header.table = table;
		header.sort= sort;

        JTableHeader tableHdr= table.getTableHeader();
		tableHdr.addMouseListener(header);
	}

    /**
     *  method handles mouse click events on the column of table in heatmap panel
     */
    public void mouseClicked(MouseEvent mouseEvent) {
		
	    TableColumnModel columnModel = table.getColumnModel();
	    
	    int viewColumn = columnModel.getColumnIndexAtX(mouseEvent.getX());
	    int column = table.convertColumnIndexToModel(viewColumn);
	    int evt=mouseEvent.getClickCount();
	    if (evt == 1 && column != -1) {
            hmparams.setSort(HeatMapParameters.Sort.COLUMN);
            hmparams.setSortIndex(column);
            hmparams.setSortbycolumn_event_triggered(true);
            hmparams.changeSortComboBoxToColumnSorted();

	    }
	  
	  }

    public void sortByColumn(int column, boolean ascending){
         sort.sortByColumn(column, ascending);

    }

}
