package org.baderlab.csplugins.enrichmentmap;



import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import javax.swing.JTable;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;

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

/* method handles mouse click events on the column of table in heatmap panel
  *
  */
    public void mouseClicked(MouseEvent mouseEvent) {
		
	    TableColumnModel columnModel = table.getColumnModel();
	    
	    int viewColumn = columnModel.getColumnIndexAtX(mouseEvent.getX());
	    int column = table.convertColumnIndexToModel(viewColumn);
	    int evt=mouseEvent.getClickCount();
	    if (evt == 1 && column != -1) {
	    	this.isSort=!isSort;
            hmparams.setSortbycolumn(true);
            hmparams.setSortbyrank(false);
            hmparams.setSortIndex(column);
            hmparams.setSortbycolumn_event_triggered(true);
            hmparams.changeRankComboBoxToColumnSorted();

	    }
	  
	  }

    public void sortByColumn(int column, boolean ascending){
         sort.sortByColumn(column, ascending);
         //hmparams.changeRankComboBoxToColumnSorted();

    }

    public void setSort(boolean isSort) {
		this.isSort = isSort;
	}
	
	public boolean isSort() {
		return isSort;
	}
	private JTable getTable() {
		return table;
	}
	
	private void setTable(JTable table) {
		this.table = table;
	}
	
	
}
