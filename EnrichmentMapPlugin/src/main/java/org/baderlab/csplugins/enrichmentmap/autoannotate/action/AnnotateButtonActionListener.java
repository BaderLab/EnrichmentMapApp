package org.baderlab.csplugins.enrichmentmap.autoannotate.action;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.baderlab.csplugins.enrichmentmap.autoannotate.AutoAnnotationManager;
import org.baderlab.csplugins.enrichmentmap.autoannotate.AutoAnnotationParameters;

import org.baderlab.csplugins.enrichmentmap.autoannotate.task.AutoAnnotationTask;
import org.baderlab.csplugins.enrichmentmap.autoannotate.task.Observer;
import org.baderlab.csplugins.enrichmentmap.autoannotate.view.AutoAnnotationPanel;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.TaskIterator;

public class AnnotateButtonActionListener implements ActionListener{
	
	private AutoAnnotationPanel autoAnnotationPanel;
	
	public AnnotateButtonActionListener(AutoAnnotationPanel autoAnnotationPanel) {
		super();		
		this.autoAnnotationPanel = autoAnnotationPanel;
	}

		//Action associated with the Annotate button in the main input panel.  
	public void actionPerformed(ActionEvent arg0) {
		AutoAnnotationManager autoAnnotationManager = AutoAnnotationManager.getInstance();
		
		//get the current View
		CyNetworkView selectedView = this.autoAnnotationPanel.getCurrentView();
		CyNetwork selectedNetwork = selectedView.getModel();
					
		// Get the params for this network
		AutoAnnotationParameters params;
		if (autoAnnotationManager.getNetworkViewToAutoAnnotationParameters().containsKey(selectedView)) {
			// Not the first annotation set for this network view, lookup
			params = autoAnnotationManager.getNetworkViewToAutoAnnotationParameters().get(selectedView);
		} else {
			// First annotation set for the view, make/register the new network view parameters
			params = new AutoAnnotationParameters(selectedNetwork, selectedView);
			autoAnnotationManager.getNetworkViewToAutoAnnotationParameters().put(selectedView, params);
		}
				
		autoAnnotationManager.getDialogTaskManager().execute(new TaskIterator(new AutoAnnotationTask(params)));
		
		
	}

}
