package org.baderlab.csplugins.enrichmentmap.autoannotate;

import java.util.ArrayList;

import org.baderlab.csplugins.enrichmentmap.autoannotate.view.AnnotationDisplayPanel;
import org.baderlab.csplugins.enrichmentmap.autoannotate.view.AutoAnnotatorInputPanel;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.events.NetworkViewAboutToBeDestroyedEvent;
import org.cytoscape.view.model.events.NetworkViewAboutToBeDestroyedListener;
import org.cytoscape.view.model.events.NetworkViewAddedEvent;
import org.cytoscape.view.model.events.NetworkViewAddedListener;
import org.cytoscape.view.model.events.UpdateNetworkPresentationEvent;
import org.cytoscape.view.model.events.UpdateNetworkPresentationListener;

/**
 * Created by
 * User: arkadyark
 * Date: July 9, 2014
 * Time: 9:46 AM
 */

public class AutoAnnotationManager implements
		NetworkViewAboutToBeDestroyedListener, NetworkViewAddedListener,
		UpdateNetworkPresentationListener {

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
	public void handleEvent(UpdateNetworkPresentationEvent e) {
//		displayPanel.updateAnnotations();
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

}
