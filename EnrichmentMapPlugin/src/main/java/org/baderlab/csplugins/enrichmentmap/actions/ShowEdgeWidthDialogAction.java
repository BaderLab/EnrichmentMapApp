package org.baderlab.csplugins.enrichmentmap.actions;

import java.awt.event.ActionEvent;
import java.util.Map;

import javax.swing.JOptionPane;

import org.baderlab.csplugins.enrichmentmap.WidthFunction;
import org.baderlab.csplugins.enrichmentmap.view.EdgeWidthDialog;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.work.TaskManager;


@SuppressWarnings("serial")
public class ShowEdgeWidthDialogAction extends AbstractCyAction {
	
	private final CySwingApplication application;
	private final CyApplicationManager applicationManager;
	private final VisualMappingFunctionFactory vmfFactoryContinuous;
	private final TaskManager<?,?> taskManager;
	
    public ShowEdgeWidthDialogAction(Map<String,String> configProps, CyApplicationManager applicationManager, 
    		VisualMappingFunctionFactory vmfFactoryContinuous, TaskManager<?,?> taskManager,
			CyNetworkViewManager networkViewManager, CySwingApplication application) {
    	super(configProps, applicationManager, networkViewManager); 
    	putValue(NAME, "Post Analysis Edge Width...");
		this.application = application;
		this.applicationManager = applicationManager;
		this.vmfFactoryContinuous = vmfFactoryContinuous;
		this.taskManager = taskManager;
	}
	
	public void actionPerformed(ActionEvent _) {
		
		if(WidthFunction.appliesTo(applicationManager.getCurrentNetwork())) {
			EdgeWidthDialog dialog = new EdgeWidthDialog(application, applicationManager, vmfFactoryContinuous, taskManager);
			dialog.pack();
			dialog.setLocationRelativeTo(application.getJFrame());
			dialog.setVisible(true);
		}
		else {
			JOptionPane.showMessageDialog(application.getJFrame(), 
				"Please run Post Analysis first.",
				"EnrichmentMap Edge Width", 
				JOptionPane.WARNING_MESSAGE);
		}
		
		
	}

}
