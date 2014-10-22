package org.baderlab.csplugins.enrichmentmap.autoannotate.model;


import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;


public class ClusterTableModel extends AbstractTableModel implements TableModelListener {

		private static final long serialVersionUID = -1277709187563893042L;
	
	    private String[] columnNames;
	    private Vector<Object> dataVector;
	    
	    private TreeMap<Integer,Cluster> clusters;

		Class<?>[] types = {Object.class, Integer.class};
	    	    
		public ClusterTableModel(String[] columnNames, Object[][] data, TreeMap<Integer,Cluster> clusters) {
			super();
			this.columnNames = columnNames;
			//copy the clusters into the object otherwise when we try to delete things we will always be comparing the same objects. 
			this.clusters = new TreeMap<Integer,Cluster>();
			this.clusters.putAll(clusters);
			
			//convert data to a Vector
			dataVector = new Vector<Object>(data.length);
			for (int r = 0; r < data.length; r++){			
				dataVector.add(convertToVector(data[r]));
			}
			
		}
		
		//For given row get the cluster id for that row
		public Integer getClusterId(int row){
			String clusterName = (String)this.getValueAt(row, 0);
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
		
		//If the cluster is found in the table but not found in the given clusters - then delete it from the table.
		public void removeClusterFromTable(Cluster cluster){
			this.removeRow(this.getRowIndexOfCluster(cluster));
		}
		
		//If a cluster is found in given clusters but not in the table - then add it to the table
		public void addClusterToTable(Cluster cluster){
			Object[] newRow = {cluster, cluster.getSize()};
			this.addRow(newRow);
			this.clusters.put(cluster.getClusterNumber(), cluster);
		}
		
		//If the cluster is found in the given clusters and the table - check to see if the value is still the same.  If it isn't then set it to the value in given clusters
		public void updateClusterInTable(Cluster cluster){
			int clusterIndex = this.getRowIndexOfCluster(cluster);
			//if this given cluster is found the table - and the value is new, update the value
			if(clusterIndex != -1 && !this.getValueAt(clusterIndex, 1).equals(cluster.getSize())){
				this.setValueAt(cluster.getSize(), clusterIndex, 1);
			}
		}
		
		public void updateTable(TreeMap<Integer, Cluster> newclusters){
			// Update the table if the value has changed (WordCloud has been updated)
			//Compare the given set of clusters to the clusters that we currently have in the table
			
			//If a cluster is found in given clusters but not in the table - then add it to the table
			//If the cluster is found in the given clusters and the table - check to see if the value is still the same.  If it isn't then set it to the value in given clusters
			//If the cluster is found in the table but not found in the given clusters - then delete it from the table.
			int i = 0;
			for (Cluster cluster : newclusters.values()) {
				
				int clusterIndex = this.getRowIndexOfCluster(cluster);
				//if this given cluster is found the table - and the value is new, update the value
				if(clusterIndex != -1 && !this.getValueAt(clusterIndex, 1).equals(cluster.getSize())){
					this.setValueAt(cluster.getSize(), clusterIndex, 1);
				}
				//if the given cluster is not found in the table then add it
				else if(clusterIndex == -1){
					Object[] newRow = {cluster, cluster.getSize()};
					this.addRow(newRow);
					this.clusters.put(cluster.getClusterNumber(), cluster);
				}
			}
			
			//To remove the clusters need to search the current table against the given set of clusters.  Any clusters not found in the given clusters should be removed
			for (Cluster cluster : this.clusters.values()) {
				if(!newclusters.containsValue(cluster)){
					//remove the row from the table
					this.removeRow(this.getRowIndexOfCluster(cluster));

				}	
			}
			
			this.clusters = newclusters;
			//this.fireTableDataChanged();
			
		}
		
		public void addRow(Object[] newrow){
			int rowIndex = dataVector.size();
			dataVector.add(convertToVector(newrow));
			fireTableChanged(new TableModelEvent(this, rowIndex, rowIndex, -1, TableModelEvent.INSERT));
		}
		
		public void removeRow(int row){
		     dataVector.remove(row);
		     //fireTableRowsDeleted(row, row);
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
			return dataVector.size();
		}

		@Override
		public int getColumnCount() {
			return columnNames.length;
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			 return ((Vector<Object>) dataVector.get(rowIndex)).get(columnIndex);
		}

		 @Override 
         public String getColumnName(int index) { 
             return columnNames[index]; 
         } 
		
		@Override
		public void tableChanged(TableModelEvent e) {
			// TODO Auto-generated method stub
			
		}
		
		/**
    * Converts the data array to a <code>Vector</code>.
   * 
   * @param data the data array (<code>null</code> permitted).
   * 
   * @return A vector (or <code>null</code> if the data array 
   *         is <code>null</code>).
    */
	private Vector<Object> convertToVector(Object[] data) {
	   if (data == null)
		   return null;
	   Vector<Object> vector = new Vector<Object>(data.length);
	   for (int i = 0; i < data.length; i++) 
		   vector.add(data[i]);
	   return vector;          
   }
		

}
