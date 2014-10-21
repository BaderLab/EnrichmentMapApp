package org.baderlab.csplugins.enrichmentmap.autoannotate.model;


import java.util.Map;
import java.util.TreeMap;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;


public class ClusterTableModel extends AbstractTableModel implements TableModelListener {

		private static final long serialVersionUID = -1277709187563893042L;
	
	    private String[] columnNames;
	    private Object[][] data;
	    
	    private TreeMap<Integer,Cluster> clusters;

		Class<?>[] types = {Object.class, Integer.class};
	    	    
		public ClusterTableModel(String[] columnNames, Object[][] data, TreeMap<Integer,Cluster> clusters) {
			super();
			this.columnNames = columnNames;
			this.data = data;
			this.clusters = clusters;
		}
		
		//For given row get the cluster id for that row
		public Integer getClusterId(int row){
			String clusterName = (String)data[row][0];
			 for(Map.Entry<Integer, Cluster>entry: clusters.entrySet()){
				 if(clusterName.equals(entry.getValue().getLabel()))
					 return entry.getKey();
			 }
			 return -1;			 
		}
		
		public Integer getRowIndexOfCluster(Cluster cluster){
			for (int rowIndex = 0; rowIndex < this.getRowCount(); rowIndex++) {
				if (cluster.equals(this.getValueAt(rowIndex, 0))) {
					return rowIndex;
				}
			}
			return -1;
		}
		
		@Override
		public Class<?> getColumnClass(int columnIndex) {
			return this.types[columnIndex];
		}
		
		@Override
		public boolean isCellEditable(int row, int column) {
			return column == 0;
		}

		@Override
		public int getRowCount() {
			return data.length;
		}

		@Override
		public int getColumnCount() {
			return columnNames.length;
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			return data[rowIndex][columnIndex];
		}

		 @Override 
         public String getColumnName(int index) { 
             return columnNames[index]; 
         } 
		
		@Override
		public void tableChanged(TableModelEvent e) {
			// TODO Auto-generated method stub
			
		}
		
		
		

}
