package org.baderlab.csplugins.enrichmentmap;

import java.util.Arrays;
//import java.util.Comparator;
import java.util.Vector;

import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;


public class Comparator {

	/**
	 * @param args
	 */private static boolean ascending = false;
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		// OverlappingGenesTableModel model = new OverlappingGenesTableModel();
		   // JTable table = new JTable(model);
		    
		    // Add data here...
		    
		    // Disable autoCreateColumnsFromModel otherwise all the column customizations
		    // and adjustments will be lost when the model data is sorted
		 //   table.setAutoCreateColumnsFromModel(false);
		    
		    // Sort the values in the second column of the model
		    // in descending order
		    int mColIndex = 1;
		    
		//    sortColumn(model, mColIndex, ascending);
	}
		    // Regardless of sort order (ascending or descending), null values always appear last.
		    // colIndex specifies a column in model.
		    public static void sortColumn(OverlappingGenesTableModel model, int colIndex, boolean ascending) {
		  //      Vector data = model.getDataVector();
		        Object[] colData = new Object[model.getRowCount()];
		    
		        // Copy the column data in an array
		        for (int i=0; i<colData.length; i++) {
		           // colData[i] = ((Vector)data.get(i)).get(colIndex);
		        }
		    
		        // Sort the array of column data
		    //    Arrays.sort(colData, new ColumnSorter(ascending));
		    
		        // Copy the sorted values back into the table model
		        for (int i=0; i<colData.length; i++) {
		  //          ((Vector)data.get(i)).set(colIndex, colData[i]);
		        }
		        model.fireTableStructureChanged();
		    }
		   /* 
		    public class ColumnSorter implements Comparator {
		        boolean ascending;
		     public  ColumnSorter(boolean ascending) {
		            this.ascending = ascending;
		        }
		        public int compare(Object a, Object b) {
		            // Treat empty strains like nulls
		            if (a instanceof String && ((String)a).length() == 0) {
		                a = null;
		            }
		            if (b instanceof String && ((String)b).length() == 0) {
		                b = null;
		            }
		    
		            // Sort nulls so they appear last, regardless
		            // of sort order
		            if (a == null && b == null) {
		                return 0;
		            } else if (a == null) {
		                return 1;
		            } else if (b == null) {
		                return -1;
		            } else if (a instanceof Comparable) {
		                if (ascending) {
		                    return ((Comparable)a).compareTo(b);
		                } else {
		                    return ((Comparable)b).compareTo(a);
		                }
		            } else {
		                if (ascending) {
		                    return a.toString().compareTo(b.toString());
		                } else {
		                    return b.toString().compareTo(a.toString());
		                }
		            }
		        }
		    }
*/
	}

	

