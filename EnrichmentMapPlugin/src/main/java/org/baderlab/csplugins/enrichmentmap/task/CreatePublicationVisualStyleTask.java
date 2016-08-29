package org.baderlab.csplugins.enrichmentmap.task;

import java.awt.Color;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

import com.google.inject.Inject;

public class CreatePublicationVisualStyleTask extends AbstractTask {

	private static final String SUFFIX = "_publication";
	
	@Inject private CyApplicationManager applicationManager;
	@Inject private VisualMappingManager visualMappingManager;
	@Inject private VisualStyleFactory visualStyleFactory;
	@Inject private CyEventHelper eventHelper;
	
	
	private VisualStyle attemptToGetExistingStyle(String vs_name) {
		for(VisualStyle vs : visualMappingManager.getAllVisualStyles()) {
			if(vs.getTitle() != null && vs.getTitle().equals(vs_name)) {
				return vs;
			}
		}
		return null;
	}

	@Override
	public void run(TaskMonitor taskMonitor) {
		taskMonitor.setTitle("EnrichmentMap");
		taskMonitor.setStatusMessage("Create Publication-Ready Visual Style");
		
		VisualStyle currentStyle = visualMappingManager.getCurrentVisualStyle();
		if(currentStyle == null)
			return;
		String currentTitle = currentStyle.getTitle();
		
		// If the current visual style is publication then attempt to switch back
		if(currentTitle.endsWith(SUFFIX)) {
			String title = currentTitle.substring(0, currentTitle.length() - SUFFIX.length());
			VisualStyle existingStyle = attemptToGetExistingStyle(title);
			if(existingStyle != null) {
				visualMappingManager.setCurrentVisualStyle(existingStyle);
			}
		}
		else {
			String title = currentTitle + SUFFIX;
			VisualStyle visualStyle = attemptToGetExistingStyle(title);
			if(visualStyle == null) {
				// create a copy of the current style
				visualStyle = visualStyleFactory.createVisualStyle(currentStyle);
				visualStyle.setTitle(title);
				// Remove node labels
				visualStyle.removeVisualMappingFunction(BasicVisualLexicon.NODE_LABEL);
				visualStyle.setDefaultValue(BasicVisualLexicon.NODE_LABEL, "");
				// Make background white
				visualStyle.removeVisualMappingFunction(BasicVisualLexicon.NETWORK_BACKGROUND_PAINT);
				visualStyle.setDefaultValue(BasicVisualLexicon.NETWORK_BACKGROUND_PAINT, Color.WHITE);
				
				visualMappingManager.addVisualStyle(visualStyle);
			}
			visualMappingManager.setCurrentVisualStyle(visualStyle);
		}
		
		eventHelper.flushPayloadEvents(); // view won't update properly without this
		CyNetworkView view = applicationManager.getCurrentNetworkView();
		view.updateView();
	}

}
