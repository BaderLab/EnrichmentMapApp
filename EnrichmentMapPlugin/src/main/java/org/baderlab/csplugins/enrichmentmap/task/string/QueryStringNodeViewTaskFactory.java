package org.baderlab.csplugins.enrichmentmap.task.string;

import java.util.Collections;
import java.util.List;

import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMapManager;
import org.baderlab.csplugins.enrichmentmap.style.EMStyleBuilder.Columns;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.task.NodeViewTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.work.TaskIterator;

import com.google.inject.Inject;

public class QueryStringNodeViewTaskFactory implements NodeViewTaskFactory {

	@Inject private StringAppMediator stringAppMediator;
	@Inject private EnrichmentMapManager emManager;
	
	
	@Override
	public TaskIterator createTaskIterator(View<CyNode> nodeView, CyNetworkView networkView) {
		CyNode node = nodeView.getModel();
		CyNetwork network = networkView.getModel();
		EnrichmentMap map = emManager.getEnrichmentMap(network.getSUID());
		
		CyRow row = network.getRow(node);
		List<String> genes = Columns.NODE_GENES.get(row, map.getParams().getAttributePrefix());
		
		return stringAppMediator.createTaskIterator(map, genes, Collections.emptySet());
	}

	@Override
	public boolean isReady(View<CyNode> nodeView, CyNetworkView networkView) {
		return emManager.isEnrichmentMap(networkView);
	}
}
