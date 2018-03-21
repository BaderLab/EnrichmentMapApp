package org.baderlab.csplugins.enrichmentmap.task;

import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.style.EMStyleOptions;
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

public class CreateEMViewTask extends AbstractTask {

	@Inject private CyNetworkManager networkManager;
	@Inject private CyNetworkViewManager networkViewManager;
	@Inject private CyNetworkViewFactory networkViewFactory;
	@Inject private CyLayoutAlgorithmManager layoutManager;
	@Inject private ApplyEMStyleTask.Factory applyStyleTaskFactory;
	
	private final EnrichmentMap map;

	public interface Factory {
		CreateEMViewTask create(EnrichmentMap map);
	}
	
	@Inject
	public CreateEMViewTask(@Assisted EnrichmentMap map) {
		this.map = map;
	}
	
	@Override
	public void run(TaskMonitor tm) {
		tm.setTitle("Creating EnrichmentMap View");
		visualizeMap();
		tm.setStatusMessage("");
	}

	private void visualizeMap() {
		CyNetwork network = networkManager.getNetwork(map.getNetworkID());
		CyNetworkView view = networkViewFactory.createNetworkView(network);
		networkViewManager.addNetworkView(view);
		
		//apply force directed layout
		CyLayoutAlgorithm layout = layoutManager.getLayout("force-directed");
		if (layout == null)
			layout = layoutManager.getDefaultLayout();
		
		Task styleTask = applyStyleTaskFactory.create(new EMStyleOptions(view, map), false);
		TaskIterator layoutTasks = layout.createTaskIterator(view, layout.createLayoutContext(),
				CyLayoutAlgorithm.ALL_NODE_VIEWS, null);
		
		TaskIterator tasks = new TaskIterator(styleTask);
		tasks.append(layoutTasks);
		insertTasksAfterCurrentTask(tasks);
	}
}
