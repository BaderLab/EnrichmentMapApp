package org.baderlab.csplugins.enrichmentmap.task;

import static org.baderlab.csplugins.enrichmentmap.style.EMStyleBuilder.FILTERED_OUT_EDGE_TRANSPARENCY;
import static org.baderlab.csplugins.enrichmentmap.style.EMStyleBuilder.FILTERED_OUT_NODE_TRANSPARENCY;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.EDGE_LABEL_TRANSPARENCY;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.EDGE_TRANSPARENCY;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.EDGE_VISIBLE;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_BORDER_TRANSPARENCY;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_LABEL_TRANSPARENCY;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_TRANSPARENCY;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NODE_VISIBLE;

import java.util.List;
import java.util.Set;

import org.baderlab.csplugins.enrichmentmap.style.NullCustomGraphics;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.RenderingEngineManager;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

public class FilterNodesEdgesTask extends AbstractTask {

	public enum FilterMode {
		HIDE("Hide filtered out nodes and edges"),
		HIGHLIGHT("Highlight filtered nodes and edges");
		
		private final String label;

		private FilterMode(String label) {
			this.label = label;
		}
		
		@Override
		public String toString() {
			return label;
		}
	}
	
	@Inject private RenderingEngineManager renderingEngineManager;
	
	private final CyNetworkView networkView;
	private final Set<CyNode> nodes;
	private final Set<CyEdge> edges;
	private final FilterMode filterMode;

	public interface Factory {
		FilterNodesEdgesTask create(CyNetworkView networkView, Set<CyNode> nodes, Set<CyEdge> filteredEdges,
				FilterMode filterMode);
	}
	
	@Inject
	public FilterNodesEdgesTask(@Assisted CyNetworkView networkView, @Assisted Set<CyNode> nodes,
			@Assisted Set<CyEdge> edges, @Assisted FilterMode filterMode) {
		this.networkView = networkView;
		this.nodes = nodes;
		this.edges = edges;
		this.filterMode = filterMode;
	}
	
	@Override
	public void run(TaskMonitor taskMonitor) {
		taskMonitor.setTitle("Filtering Nodes and Edges");
		
		taskMonitor.setStatusMessage("Filtering nodes...");
		taskMonitor.setProgress(0.0);
		
		filterNodes(nodes, taskMonitor);
		
		if (cancelled)
			return;
		
		taskMonitor.setStatusMessage("Filtering edges...");
		taskMonitor.setProgress(0.2);
		
		filterEdges(nodes, edges, taskMonitor, 0.2);
		taskMonitor.setProgress(1.0);
	}

	private void filterNodes(Set<CyNode> nodes, TaskMonitor taskMonitor) {
		CyNetwork net = networkView.getModel();
		
		for (CyNode n : net.getNodeList()) {
			if (cancelled)
				return;
			
			final View<CyNode> nv = networkView.getNodeView(n);
			
			if (nv == null)
				continue; // Should never happen!
			
			boolean filteredIn = nodes.contains(n);
			
			VisualLexicon lexicon = renderingEngineManager.getDefaultVisualLexicon(); 
			VisualProperty<?> customGraphics1 = lexicon.lookup(CyNode.class, "NODE_CUSTOMGRAPHICS_1");
			
			// Don't forget to remove all previous locked values first!
			nv.clearValueLock(NODE_VISIBLE);
			nv.clearValueLock(NODE_TRANSPARENCY);
			nv.clearValueLock(NODE_BORDER_TRANSPARENCY);
			nv.clearValueLock(NODE_LABEL_TRANSPARENCY);
			
			if (customGraphics1 != null)
				nv.clearValueLock(customGraphics1);
			
			if (!filteredIn) {
				switch (filterMode) {
					case HIDE:
						net.getRow(n).set(CyNetwork.SELECTED, false);
						nv.setLockedValue(NODE_VISIBLE, false);
						break;
					case HIGHLIGHT:
						nv.setLockedValue(NODE_TRANSPARENCY, FILTERED_OUT_NODE_TRANSPARENCY);
						nv.setLockedValue(NODE_BORDER_TRANSPARENCY, FILTERED_OUT_NODE_TRANSPARENCY);
						nv.setLockedValue(NODE_LABEL_TRANSPARENCY, 0);
						if (customGraphics1 != null)
							nv.setLockedValue(customGraphics1, NullCustomGraphics.getNullObject());
						break;
				}
			}
		}
	}
	
	private void filterEdges(Set<CyNode> nodes, Set<CyEdge> edges, TaskMonitor taskMonitor, double initialProgress) {
		CyNetwork net = networkView.getModel();
		List<CyEdge> edgeList = net.getEdgeList();
		int total = edgeList.size();
		int count = 0;
		float progress = (float) initialProgress;
		
		for (CyEdge e : edgeList) {
			if (cancelled)
				return;
			
			final View<CyEdge> ev = networkView.getEdgeView(e);
			
			if (ev == null)
				continue; // Should never happen!
			
			boolean filteredIn = edges.contains(e) && nodes.contains(e.getSource()) && nodes.contains(e.getTarget());

			// Don't forget to remove all locked values first!
			ev.clearValueLock(EDGE_VISIBLE);
			ev.clearValueLock(EDGE_TRANSPARENCY);
			ev.clearValueLock(EDGE_LABEL_TRANSPARENCY);
			
			if (!filteredIn) {
				switch (filterMode) {
					case HIDE:
						net.getRow(e).set(CyNetwork.SELECTED, false);
						ev.setLockedValue(EDGE_VISIBLE, false);
						break;
					case HIGHLIGHT:
						ev.setLockedValue(EDGE_TRANSPARENCY, FILTERED_OUT_EDGE_TRANSPARENCY);
						ev.setLockedValue(EDGE_LABEL_TRANSPARENCY, FILTERED_OUT_EDGE_TRANSPARENCY);
						break;
				}
			}
			
			// Use only 2 decimals to avoid too many UI updates when setting very small numbers
			float newProgress = Math.round((initialProgress + count * (1 - initialProgress) / total) * 100) / 100.0f;
			
			if (newProgress != progress) {
				taskMonitor.setProgress(newProgress);
				progress = newProgress;
			}
			
			count++;
		}
	}
}
