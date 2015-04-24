package org.baderlab.csplugins.enrichmentmap.task;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.view.model.CyNetworkView;


/**
 * Contains information about the results of running {@link BuildDiseaseSignatureTask}.
 * 
 * @author mkucera
 */
public class BuildDiseaseSignatureTaskResult {

	private final Set<CyEdge> existingEdges;
	private final CyNetwork network;
	private final CyNetworkView networkView;
	private final boolean warnUserBypassStyle;
	private final int createdEdgeCount;
	private final boolean cancelled;
	
	private BuildDiseaseSignatureTaskResult(CyNetwork network, CyNetworkView networkView, Set<CyEdge> existingEdges, 
			                                boolean warnUserBypassStyle, int createdEdgeCount, boolean cancelled) {
		this.existingEdges = new HashSet<>(existingEdges);
		this.warnUserBypassStyle = warnUserBypassStyle;
		this.network = network;
		this.networkView = networkView;
		this.createdEdgeCount = createdEdgeCount;
		this.cancelled = cancelled;
	}
	
	public Set<CyEdge> getExistingEdgesFailingCutoff() {
		return Collections.unmodifiableSet(existingEdges);
	}

	public boolean isWarnUserBypassStyle() {
		return warnUserBypassStyle;
	}

	public CyNetwork getNetwork() {
		return network;
	}
	
	public CyNetworkView getNetworkView() {
		return networkView;
	}
	
	public int getCreatedEdgeCount() {
		return createdEdgeCount;
	}
	
	public boolean isCancelled() {
		return cancelled;
	}
	
	
	/**
	 * Builder.
	 */
	public static class Builder {
		private Set<CyEdge> existingEdges = new HashSet<>();
		private boolean warnUserBypassStyle;
		private CyNetwork network;
		private CyNetworkView networkView;
		private int createdEdgeCount = 0;
		private boolean cancelled;
		
		public void addExistingEdgeFailsCutoff(CyEdge edge) {
			if(edge != null) {
				existingEdges.add(edge);
			}
		}
		
		public void incrementCreatedEdgeCount() {
			createdEdgeCount++;
		}
		
		public void setWarnUserBypassStyle(boolean warn) {
			this.warnUserBypassStyle = warn;
		}
		
		public void setNetwork(CyNetwork network) {
			this.network = network;
		}
		
		public void setNetworkView(CyNetworkView networkView) {
			this.networkView = networkView;
		}
		
		public BuildDiseaseSignatureTaskResult build() {
			return new BuildDiseaseSignatureTaskResult(network, networkView, existingEdges, 
					                                   warnUserBypassStyle, createdEdgeCount, cancelled);
		}

		public void setCancelled(boolean cancelled) {
			this.cancelled = cancelled;
		}
	}


	
	
}
