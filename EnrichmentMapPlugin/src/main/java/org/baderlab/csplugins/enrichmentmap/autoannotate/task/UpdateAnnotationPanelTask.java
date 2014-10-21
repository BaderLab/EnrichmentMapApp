package org.baderlab.csplugins.enrichmentmap.autoannotate.task;

import org.baderlab.csplugins.enrichmentmap.EnrichmentMapUtils;
import org.baderlab.csplugins.enrichmentmap.autoannotate.AutoAnnotationManager;
import org.baderlab.csplugins.enrichmentmap.autoannotate.AutoAnnotationParameters;
import org.baderlab.csplugins.enrichmentmap.autoannotate.model.AnnotationSet;
import org.baderlab.csplugins.enrichmentmap.autoannotate.view.AutoAnnotationPanel;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

public class UpdateAnnotationPanelTask extends AbstractTask {
	private AnnotationSet annotationSet;
	private AutoAnnotationParameters params;

	private TaskMonitor taskMonitor;
	
	public UpdateAnnotationPanelTask(AnnotationSet annotationSet,
			AutoAnnotationParameters params) {
		super();
		this.annotationSet = annotationSet;
		this.params = params;
	}



	@Override
	public void run(TaskMonitor arg0) throws Exception {
		
		AutoAnnotationPanel autoAnnotationPanel = AutoAnnotationManager.getInstance().getAnnotationPanel();
		
		autoAnnotationPanel.addClusters(annotationSet,params);
		autoAnnotationPanel.updateSelectedView(params.getNetworkView());
		AutoAnnotationManager.getInstance().getWestPanel().setSelectedIndex(AutoAnnotationManager.getInstance().getWestPanel().indexOfComponent(autoAnnotationPanel));
		EnrichmentMapUtils.setOverrideHeatmapRevalidation(false);
				
		// Let the panel know annotating is finished
		autoAnnotationPanel.setAnnotating(false);
		//update the panel parameters to the newly created set.
		autoAnnotationPanel.updateParameters();
		
		//deselect all the nodes in the network
		for (View<CyNode> nodeView : params.getNetworkView().getNodeViews()) {
			if (nodeView.getVisualProperty(BasicVisualLexicon.NODE_VISIBLE)) {
				params.getNetwork().getRow(nodeView.getModel()).set(CyNetwork.SELECTED, false);
			}
		}
		
	}

}
