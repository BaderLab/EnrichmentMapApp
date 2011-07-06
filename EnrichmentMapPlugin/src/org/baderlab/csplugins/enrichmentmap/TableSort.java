package org.baderlab.csplugins.enrichmentmap;

/*
 * @(#)TableSorter.java 1.5 97/12/17
 *
 * Copyright (c) 1997 Sun Microsystems, Inc. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of Sun
 * Microsystems, Inc. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Sun.
 *
 * SUN MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF THE
 * SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE, OR NON-INFRINGEMENT. SUN SHALL NOT BE LIABLE FOR ANY DAMAGES
 * SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING
 * THIS SOFTWARE OR ITS DERIVATIVES.
 *
 */

/**
 * A sorter for TableModels. The sorter has a model (conforming to TableModel) 
 * and itself implements TableModel. TableSorter does not store or copy 
 * the data in the TableModel, instead it maintains an array of 
 * integers which it keeps the same size as the number of rows in its 
 * model. When the model changes it notifies the sorter that something 
 * has changed eg. "rowsAdded" so that its internal array of integers 
 * can be reallocated. As requests are made of the sorter (like 
 * getValueAt(row, col) it redirects them to its model via the mapping 
 * array. That way the TableSorter appears to hold another copy of the table 
 * with the rows in a different order. The sorting algorthm used is stable 
 * which means that it does not move around rows when its comparison 
 * function returns 0 to denote that they are equivalent. 
 *
 * @version 1.5 12/17/97
 * @author Philip Milne
 */

import java.util.*;

import javax.swing.table.TableModel;
import javax.swing.event.TableModelEvent;

// Imports for picking up mouse events from the JTable. 

import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.InputEvent;
import javax.swing.JTable;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;

public class TableSort extends Mapper
{
    int             indexes[];
    Vector          sortingColumns = new Vector();
    boolean         ascending = true;
    int compares;

    public TableSort()
    {
        indexes = new int[0]; // For consistency.        
    }

    public TableSort(TableModel model)
    {
        setModel(model);
    }

    public void setModel(TableModel model) {
        super.setModel(model); 
        reallocateIndexes(); 
    }

    public int compareRowsByColumn(int row1, int row2, int column)
    {
        Class type = model.getColumnClass(column);
        TableModel data = model;
        
        // Check for nulls

        Object o1 = data.getValueAt(row1, column);
        Object o2 = data.getValueAt(row2, column); 
       

        // If both values are null return 0
        if (o1 == null && o2 == null) {
            return 0; 
        }
        else if (o1 == null) { // Define null less than everything. 
            return -1; 
        } 
        else if (o2 == null) { 
            return 1; 
        }

        if (type.getSuperclass() == java.lang.Number.class)
            {
        	Number n1 = (Number)model.getExpValueAt(row1, column);
            double d1 = n1.doubleValue();
            Number n2 = (Number)model.getExpValueAt(row2, column);
            double d2 = n2.doubleValue();
               
        	
                if (d1 < d2)
                    return -1;
                else if (d1 > d2)
                    return 1;
                else
                    return 0;
            }
       else if (type == java.awt.Color.class)
        {
    	Object c1= model.getExpValueAt(row1, column);
       	Object c2= model.getExpValueAt(row2, column);
       	Double d1=(Double)c1;
       	Double d2=(Double)c2;
       
       	if (d1 < d2)
            return -1;
        else if (d1 > d2)
            return 1;
        else
            return 0;
	   
        }
        
        else if (type == String.class)
            {
                String s1 = (String)data.getValueAt(row1, column);
                String s2    = (String)data.getValueAt(row2, column);
                int result = s1.compareTo(s2);

                if (result < 0)
                    return -1;
                else if (result > 0)
                    return 1;
                else return 0;
            }
        else if (type == Boolean.class)
            {
                Boolean bool1 = (Boolean)data.getValueAt(row1, column);
                boolean b1 = bool1.booleanValue();
                Boolean bool2 = (Boolean)data.getValueAt(row2, column);
                boolean b2 = bool2.booleanValue();

                if (b1 == b2)
                    return 0;
                else if (b1) // Define false < true
                    return 1;
                else
                    return -1;
            }
        else
            {
                Object v1 = model.getValueAt(row1, column);
                String s1 = v1.toString();
                Object v2 = model.getValueAt(row2, column);
                String s2 = v2.toString();
                int result = s1.compareTo(s2);

                if (result < 0)
                    return -1;
                else if (result > 0)
                    return 1;
                else return 0;
            }
    }

    public int compare(int row1, int row2)
    {
        compares++;
        for(int level = 0; level < sortingColumns.size(); level++)
            {
                Integer column = (Integer)sortingColumns.elementAt(level);
                int result = compareRowsByColumn(row1, row2, column.intValue());
                if (result != 0)
                    return ascending ? result : -result;
            }
        return 0;
    }

    public void  reallocateIndexes()
    {
        int rowCount = model.getRowCount();  
        indexes		 = new int[rowCount];

        // Initialise with the identity mapping.
        for(int row = 0; row < rowCount; row++)
            indexes[row] = row;
    }

    public void tableChanged(TableModelEvent e)
    {
        //System.out.println("Sorter: tableChanged"); 
        reallocateIndexes();

        super.tableChanged(e);
    }

    public void checkModel()
    {
        if (indexes.length != model.getRowCount()) {
            System.err.println("Sorter not informed of a change in model.");
        }
    }

    public void  sort(Object sender)
    {
        checkModel();

        compares = 0;
        // n2sort();
        // qsort(0, indexes.length-1);
        shuttlesort((int[])indexes.clone(), indexes, 0, indexes.length);
        //System.out.println("Compares: "+compares);
    }

    public void n2sort() {
        for(int i = 0; i < getRowCount(); i++) {
            for(int j = i+1; j < getRowCount(); j++) {
                if (compare(indexes[i], indexes[j]) == -1) {
                    swap(i, j);
                }
            }
        }
    }

     public void shuttlesort(int from[], int to[], int low, int high) {
        if (high - low < 2) {
            return;
        }
        int middle = (low + high)/2;
        shuttlesort(to, from, low, middle);
        shuttlesort(to, from, middle, high);

        int p = low;
        int q = middle;

        if (high - low >= 4 && compare(from[middle-1], from[middle]) <= 0) {
            for (int i = low; i < high; i++) {
                to[i] = from[i];
            }
            return;
        }

        // A normal merge. 

        for(int i = low; i < high; i++) {
            if (q >= high || (p < middle && compare(from[p], from[q]) <= 0)) {
                to[i] = from[p++];
            }
            else {
                to[i] = from[q++];
            }
        }
    }

    public void swap(int i, int j) {
        int tmp = indexes[i];
        indexes[i] = indexes[j];
        indexes[j] = tmp;
    }

    public Object getValueAt(int aRow, int aColumn)
    {
        checkModel();
        return model.getValueAt(indexes[aRow], aColumn);
    }

    public void setValueAt(Object aValue, int aRow, int aColumn)
    {
        checkModel();
        model.setValueAt(aValue, indexes[aRow], aColumn);
    }

    public void sortByColumn(int column) {
        sortByColumn(column, true);
    }

    public void sortByColumn(int column, boolean ascending) {
        this.ascending = ascending;
        sortingColumns.removeAllElements();
        sortingColumns.addElement(new Integer(column));
        sort(this);
        super.tableChanged(new TableModelEvent(this)); 
    }
    
  public void addMouseListenerToHeaderInTable(JTable table) { 
        final TableSort sorter = this; 
        final JTable tableView = table; 
        tableView.setColumnSelectionAllowed(false); 
        MouseAdapter listMouseListener = new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                TableColumnModel columnModel = tableView.getColumnModel();
                int viewColumn = columnModel.getColumnIndexAtX(e.getX()); 
                int column = tableView.convertColumnIndexToModel(viewColumn); 
                if(e.getClickCount() == 1 && column != -1) {
                    //System.out.println("Sorting ..."); 
                    int shiftPressed = e.getModifiers()&InputEvent.SHIFT_MASK; 
                    boolean ascending = (shiftPressed == 0); 
                    sorter.sortByColumn(column, ascending); 
                }
             }
         };
        JTableHeader th = tableView.getTableHeader(); 
        th.addMouseListener(listMouseListener); 
    }



}
