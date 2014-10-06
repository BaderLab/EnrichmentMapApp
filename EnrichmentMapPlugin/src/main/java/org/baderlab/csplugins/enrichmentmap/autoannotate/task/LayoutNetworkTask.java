package org.baderlab.csplugins.enrichmentmap.autoannotate.task;

import java.util.HashSet;
import java.util.Set;

import org.baderlab.csplugins.enrichmentmap.autoannotate.AutoAnnotationManager;
import org.baderlab.csplugins.enrichmentmap.autoannotate.AutoAnnotationParameters;
import org.baderlab.csplugins.enrichmentmap.autoannotate.model.AnnotationSet;
import org.baderlab.csplugins.enrichmentmap.autoannotate.model.Cluster;
import org.baderlab.csplugins.enrichmentmap.autoannotate.view.AutoAnnotationPanel;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.view.layout.CyLayoutAlgorithm;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.SynchronousTaskManager;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.swing.DialogTaskManager;

public class LayoutNetworkTask extends AbstractTask {
	
	private TaskMonitor taskMonitor;
	
	private AnnotationSet annotationSet;
	private CyNetworkView view;
	private AutoAnnotationParameters params;
	
	private CyLayoutAlgorithmManager layoutManager;
	private SynchronousTaskManager<?> syncTaskManager;

	public LayoutNetworkTask(AnnotationSet annotationSet,AutoAnnotationParameters params) {
		super();
		this.annotationSet = annotationSet;
		this.params = params;
		this.view = params.getNetworkView();
		
		this.layoutManager = AutoAnnotationManager.getInstance().getLayoutManager();
		this.syncTaskManager = AutoAnnotationManager.getInstance().getSyncTaskManager();
		
	}

	private void layoutNodes() {
		CyLayoutAlgorithm attributeCircle = layoutManager.getLayout("attributes-layout");
		TaskIterator iterator = attributeCircle.createTaskIterator(view, attributeCircle.createLayoutContext(), CyLayoutAlgorithm.ALL_NODE_VIEWS, params.getClusterColumnName());
		syncTaskManager.execute(iterator);
		CyLayoutAlgorithm force_directed = layoutManager.getLayout("force-directed");
		for (Cluster cluster :annotationSet.getClusterMap().values()) {
			Set<View<CyNode>> nodeViewSet = new HashSet<View<CyNode>>();
			for (CyNode node : cluster.getNodes()) {
				nodeViewSet.add(view.getNodeView(node));
			}
			// Only apply layout to nodes of size greater than 4
			if (nodeViewSet.size() > 4) {
				iterator = force_directed.createTaskIterator(view, force_directed.createLayoutContext(), nodeViewSet, null);
				syncTaskManager.execute(iterator);
			}
		}
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		this.taskMonitor = taskMonitor;
		
		taskMonitor.setStatusMessage("Laying out nodes...");
		
		layoutNodes();
	}
	

}
