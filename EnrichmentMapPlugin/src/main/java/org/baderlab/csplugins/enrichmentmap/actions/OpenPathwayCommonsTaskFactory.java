package org.baderlab.csplugins.enrichmentmap.actions;

import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMapManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.task.NodeViewTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.work.TaskIterator;

import com.google.inject.Inject;

public class OpenPathwayCommonsTaskFactory implements NodeViewTaskFactory {

	@Inject private OpenPathwayCommonsTask.Factory taskFactory;
	@Inject private EnrichmentMapManager emManager;

	@Override
	public TaskIterator createTaskIterator(View<CyNode> nodeView, CyNetworkView networkView) {
		return new TaskIterator(taskFactory.create(nodeView.getModel(), networkView.getModel()));
	}

	@Override
	public boolean isReady(View<CyNode> nodeView, CyNetworkView networkView) {
		// TODO disable if network has no class data
		return emManager.isEnrichmentMap(networkView);
	}

}