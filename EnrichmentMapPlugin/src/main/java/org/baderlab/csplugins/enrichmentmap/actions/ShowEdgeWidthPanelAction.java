package org.baderlab.csplugins.enrichmentmap.actions;

import java.awt.event.ActionEvent;
import java.util.Map;

import javax.swing.JOptionPane;

import org.baderlab.csplugins.enrichmentmap.EnrichmentMapManager;
import org.baderlab.csplugins.enrichmentmap.PostAnalysisVisualStyle;
import org.baderlab.csplugins.enrichmentmap.view.EdgeWidthDialog;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.equations.EquationCompiler;
import org.cytoscape.view.model.CyNetworkViewManager;


@SuppressWarnings("serial")
public class ShowEdgeWidthPanelAction extends AbstractCyAction {
	
	private final CySwingApplication application;
	private final CyApplicationManager applicationManager;
	private final EnrichmentMapManager enrichmentMapManager;
	private final EquationCompiler equationCompiler;
	
    public ShowEdgeWidthPanelAction(Map<String,String> configProps, CyApplicationManager applicationManager, 
			CyNetworkViewManager networkViewManager, CySwingApplication application, EnrichmentMapManager enrichmentMapManager, EquationCompiler equationCompiler) {
    	super(configProps, applicationManager, networkViewManager); 
    	putValue(NAME, "Edge Width...");
		this.application = application;
		this.applicationManager = applicationManager;
		this.enrichmentMapManager = enrichmentMapManager;
		this.equationCompiler = equationCompiler;
	}
	
	public void actionPerformed(ActionEvent _) {
		
		if(PostAnalysisVisualStyle.appliesTo(applicationManager.getCurrentNetwork())) {
			EdgeWidthDialog dialog = new EdgeWidthDialog(application, applicationManager, enrichmentMapManager, equationCompiler);
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
