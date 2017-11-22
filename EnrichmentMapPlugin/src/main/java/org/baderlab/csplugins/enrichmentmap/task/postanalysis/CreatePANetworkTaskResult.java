package org.baderlab.csplugins.enrichmentmap.task.postanalysis;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.CyNetworkView;

/**
 * Contains information about the results of running
 * {@link CreateDiseaseSignatureTask}.
 * 
 * @author mkucera
 */
public class CreatePANetworkTaskResult {

	private final Set<CyEdge> existingEdgesFailingCutoff;
	private final Set<CyEdge> newEdges;
	private final Set<CyNode> newNodes;
	private final CyNetwork network;
	private final CyNetworkView networkView;
	private final int passedCutoffCount;
	private final boolean cancelled;

	private CreatePANetworkTaskResult(Builder builder) {
		// Not making copies of the Sets, this is why Builder.build() should only be called once.
		this.existingEdgesFailingCutoff = builder.existingEdgesFailingCutoff;
		this.newEdges = builder.newEdges;
		this.newNodes = builder.newNodes;
		this.network = builder.network;
		this.networkView = builder.networkView;
		this.passedCutoffCount = builder.passedCutoffCount;
		this.cancelled = builder.cancelled;
	}

	public Set<CyEdge> getExistingEdgesFailingCutoff() {
		return Collections.unmodifiableSet(existingEdgesFailingCutoff);
	}

	public Set<CyEdge> getNewEdges() {
		return Collections.unmodifiableSet(newEdges);
	}

	public Set<CyNode> getNewNodes() {
		return Collections.unmodifiableSet(newNodes);
	}

	public CyNetwork getNetwork() {
		return network;
	}

	public CyNetworkView getNetworkView() {
		return networkView;
	}

	public int getPassedCutoffCount() {
		return passedCutoffCount;
	}

	public boolean isCancelled() {
		return cancelled;
	}

	/**
	 * Note: This builder is not thread safe and the build() method should only
	 * be called once per builder instance.
	 */
	public static class Builder {
		private Set<CyEdge> existingEdgesFailingCutoff = new HashSet<>();
		private Set<CyEdge> newEdges = new HashSet<>();
		private Set<CyNode> newNodes = new HashSet<>();
		private CyNetwork network;
		private CyNetworkView networkView;
		private int passedCutoffCount = 0;
		private boolean cancelled = false;

		private CreatePANetworkTaskResult result = null;

		public void addExistingEdgeFailsCutoff(CyEdge edge) {
			if(edge != null)
				existingEdgesFailingCutoff.add(edge);
		}

		public void addNewEdge(CyEdge edge) {
			if(edge != null)
				newEdges.add(edge);
		}

		public void addNewNode(CyNode node) {
			if(node != null)
				newNodes.add(node);
		}

		public void incrementPassedCutoffCount() {
			passedCutoffCount++;
		}

		public void setNetwork(CyNetwork network) {
			this.network = network;
		}

		public void setNetworkView(CyNetworkView networkView) {
			this.networkView = networkView;
		}

		public void setCancelled(boolean cancelled) {
			this.cancelled = cancelled;
		}

		public CreatePANetworkTaskResult build() {
			if(result == null) {
				result = new CreatePANetworkTaskResult(this);
			}
			return result;
		}
	}

}
