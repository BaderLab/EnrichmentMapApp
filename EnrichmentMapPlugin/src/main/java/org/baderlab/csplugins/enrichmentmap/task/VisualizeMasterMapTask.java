package org.baderlab.csplugins.enrichmentmap.task;

import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.style.EMStyleOptions;
import org.baderlab.csplugins.enrichmentmap.style.MasterMapVisualStyleTask;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.view.layout.CyLayoutAlgorithm;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

public class VisualizeMasterMapTask extends AbstractTask {

	@Inject private CyNetworkManager networkManager;
	@Inject private CyNetworkViewManager networkViewManager;
	@Inject private CyNetworkViewFactory networkViewFactory;
	@Inject private CyLayoutAlgorithmManager layoutManager;
	@Inject private MasterMapVisualStyleTask.Factory masterMapVisualStyleTaskFactory;
	
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
		CyNetworkView view = networkViewFactory.createNetworkView(network);
		networkViewManager.addNetworkView(view);
		
		//apply force directed layout
		CyLayoutAlgorithm layout = layoutManager.getLayout("force-directed");
		
		if (layout == null)
			layout = layoutManager.getDefaultLayout();
		
		Task styleTask = masterMapVisualStyleTaskFactory.create(new EMStyleOptions(view, map), null);
		TaskIterator layoutTasks = layout.createTaskIterator(view, layout.createLayoutContext(),
				CyLayoutAlgorithm.ALL_NODE_VIEWS, null);
		
		TaskIterator moreTasks = new TaskIterator();
		moreTasks.append(styleTask);
		moreTasks.append(layoutTasks);
		insertTasksAfterCurrentTask(moreTasks);
	}
	
}
