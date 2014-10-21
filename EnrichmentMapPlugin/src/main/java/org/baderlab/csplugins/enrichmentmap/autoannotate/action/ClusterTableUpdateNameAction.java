package org.baderlab.csplugins.enrichmentmap.autoannotate.action;

import java.awt.event.ActionEvent;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.AbstractAction;

import org.baderlab.csplugins.enrichmentmap.autoannotate.AutoAnnotationManager;
import org.baderlab.csplugins.enrichmentmap.autoannotate.model.AnnotationSet;
import org.baderlab.csplugins.enrichmentmap.autoannotate.model.Cluster;
import org.baderlab.csplugins.enrichmentmap.autoannotate.task.DrawClusterLabelTask;
import org.cytoscape.work.TaskIterator;

public class ClusterTableUpdateNameAction extends AbstractAction {

	private static final long serialVersionUID = 3764130543697594367L;
	
	private AnnotationSet annotationSet;

	public ClusterTableUpdateNameAction(AnnotationSet annotationSet) {
		this.annotationSet = annotationSet;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// get the original and new value for the cell that has been edited.  
		 TableCellListener tcl = (TableCellListener)e.getSource();
		 String originalValue = (String)tcl.getOldValue();
		 String newValue = (String)tcl.getNewValue();
		 
	        System.out.println("Row   : " + tcl.getRow());
	        System.out.println("Column: " + tcl.getColumn());
	        System.out.println("Old   : " + tcl.getOldValue());
	        System.out.println("New   : " + tcl.getNewValue());
		 
		 //figure out which cluster has been changed
		 TreeMap<Integer, Cluster> clusters = annotationSet.getClusterMap();
		 for(Map.Entry<Integer, Cluster>entry: clusters.entrySet()){
			 Cluster currentCluster = entry.getValue();
			 //TODO what two clusters have the same label.  The below code will change the label on both of them. 
			 if(currentCluster.getLabel().equals(originalValue)){
				 //update the value associated with the label
				 currentCluster.setLabel(newValue);
				 currentCluster.eraseText();
				 DrawClusterLabelTask drawlabel = new DrawClusterLabelTask(currentCluster);
				 AutoAnnotationManager.getInstance().getDialogTaskManager().execute(new TaskIterator(drawlabel));
				 
			 }
		 }		
	}
	
	
}
