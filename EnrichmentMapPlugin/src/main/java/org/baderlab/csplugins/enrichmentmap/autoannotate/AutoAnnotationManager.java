package org.baderlab.csplugins.enrichmentmap.autoannotate;

import java.util.ArrayList;

import javax.swing.DefaultComboBoxModel;

import org.baderlab.csplugins.enrichmentmap.autoannotate.view.AnnotationDisplayPanel;
import org.baderlab.csplugins.enrichmentmap.autoannotate.view.AutoAnnotatorInputPanel;
import org.cytoscape.model.events.ColumnCreatedEvent;
import org.cytoscape.model.events.ColumnCreatedListener;
import org.cytoscape.model.events.ColumnDeletedEvent;
import org.cytoscape.model.events.ColumnDeletedListener;
import org.cytoscape.model.events.ColumnNameChangedEvent;
import org.cytoscape.model.events.ColumnNameChangedListener;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.events.NetworkViewAboutToBeDestroyedEvent;
import org.cytoscape.view.model.events.NetworkViewAboutToBeDestroyedListener;
import org.cytoscape.view.model.events.NetworkViewAddedEvent;
import org.cytoscape.view.model.events.NetworkViewAddedListener;
import org.cytoscape.view.model.events.UpdateNetworkPresentationEvent;
import org.cytoscape.view.model.events.UpdateNetworkPresentationListener;
import org.cytoscape.view.vizmap.events.VisualStyleChangedEvent;
import org.cytoscape.view.vizmap.events.VisualStyleChangedListener;

/**
 * Created by
 * User: arkadyark
 * Date: July 9, 2014
 * Time: 9:46 AM
 */

public class AutoAnnotationManager implements
		NetworkViewAboutToBeDestroyedListener, NetworkViewAddedListener,
		ColumnCreatedListener, ColumnDeletedListener,
		ColumnNameChangedListener {

	private static AutoAnnotationManager manager = null;
	
	private AutoAnnotatorInputPanel inputPanel;
	private AnnotationDisplayPanel displayPanel;
	private ArrayList<CyNetworkView> toAdd;
	
	public AutoAnnotationManager() {
		toAdd = new ArrayList<CyNetworkView>();
	}
	
    public static AutoAnnotationManager getInstance() {
        if(manager == null)
            manager = new AutoAnnotationManager();
        return manager;
    }
	
    public void setInputPanel(AutoAnnotatorInputPanel inputPanel) {
    	this.inputPanel = inputPanel;
    	if (!toAdd.isEmpty()) {
			for (CyNetworkView view : toAdd) {
				inputPanel.addNetworkView(view);
			}
			toAdd.clear();
    	}
    }
    
    public void setDisplayPanel(AnnotationDisplayPanel displayPanel) {
    	this.displayPanel = displayPanel;
    }

	@Override
	public void handleEvent(NetworkViewAddedEvent e) {
		// Adds to a queue for the case when the input panel hasn't yet been created
		toAdd.add(e.getNetworkView());
		if (inputPanel != null) {
			for (CyNetworkView view : toAdd) {
				inputPanel.addNetworkView(view);
				toAdd.remove(view);
			}
		}
	}
	
	@Override
	public void handleEvent(NetworkViewAboutToBeDestroyedEvent e) {
		inputPanel.removeNetworkView(e.getNetworkView());
	}

	@Override
	public void handleEvent(ColumnNameChangedEvent e) {
		if (inputPanel != null) {
			if (e.getSource() == inputPanel.selectedView.getModel().getDefaultNodeTable()) {
				inputPanel.clusterColumnDropdown.removeItem(e.getOldColumnName());
				inputPanel.nameColumnDropdown.removeItem(e.getOldColumnName());
				inputPanel.clusterColumnDropdown.addItem(e.getNewColumnName());
				inputPanel.nameColumnDropdown.addItem(e.getNewColumnName());		
			}
		}
	}

	@Override
	public void handleEvent(ColumnDeletedEvent e) {
		String columnName = e.getColumnName();
		if (((DefaultComboBoxModel) inputPanel.clusterColumnDropdown.getModel()).getIndexOf(columnName) != -1) {
			inputPanel.clusterColumnDropdown.removeItem(e.getColumnName());			
		}
		if (((DefaultComboBoxModel) inputPanel.nameColumnDropdown.getModel()).getIndexOf(columnName) != -1) {
			inputPanel.nameColumnDropdown.removeItem(e.getColumnName());			
		}
	}

	@Override
	public void handleEvent(ColumnCreatedEvent e) {
		String columnName = e.getColumnName();
		if (((DefaultComboBoxModel) inputPanel.clusterColumnDropdown.getModel()).getIndexOf(columnName) == -1) {
			inputPanel.clusterColumnDropdown.addItem(e.getColumnName());			
		}
		if (((DefaultComboBoxModel) inputPanel.nameColumnDropdown.getModel()).getIndexOf(columnName) == -1) {
			inputPanel.nameColumnDropdown.addItem(e.getColumnName());			
		}
	}

}
