package org.baderlab.csplugins.enrichmentmap.task;

import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.view.layout.CyLayoutAlgorithm;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

public class VisualizeMasterMapTask extends AbstractTask {

	@Inject private CyNetworkManager networkManager;
	@Inject private CyNetworkViewManager networkViewManager;
	@Inject private CyNetworkViewFactory networkViewFactory;
	@Inject private VisualMappingManager visualMappingManager;
	@Inject private VisualStyleFactory visualStyleFactory;
	@Inject private CyLayoutAlgorithmManager layoutManager;
	
	private final EnrichmentMap map;
	
	
	public interface Factory {
		VisualizeMasterMapTask create(EnrichmentMap map);
	}
	
	@Inject
	public VisualizeMasterMapTask(@Assisted EnrichmentMap map) {
		this.map = map;
	}
	
	
	@Override
	public void run(TaskMonitor taskMonitor) {
		taskMonitor.setTitle("Create Network View");
		visualizeMap();
		taskMonitor.setStatusMessage("");
	}

	private void visualizeMap() {
		CyNetwork network = networkManager.getNetwork(map.getNetworkID());
		String prefix = map.getParams().getAttributePrefix();
		
		CyNetworkView view = networkViewFactory.createNetworkView(network);
		networkViewManager.addNetworkView(view);
		
		//apply force directed layout
		CyLayoutAlgorithm layout = layoutManager.getLayout("force-directed");
		if(layout == null)
			layout = layoutManager.getDefaultLayout();
		if(layout != null)
			insertTasksAfterCurrentTask(layout.createTaskIterator(view, layout.createLayoutContext(), CyLayoutAlgorithm.ALL_NODE_VIEWS, null));
	}
}
