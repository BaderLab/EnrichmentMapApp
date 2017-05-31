package org.baderlab.csplugins.enrichmentmap.task.postanalysis;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.baderlab.csplugins.enrichmentmap.model.EMSignatureDataSet;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

public class RemoveSignatureDataSetsTask extends AbstractTask {

	public interface Factory {
		RemoveSignatureDataSetsTask create(Collection<EMSignatureDataSet> signatureDataSets, EnrichmentMap map);
	}

	private final Collection<EMSignatureDataSet> signatureDataSets;
	private final EnrichmentMap map;
	
	@Inject private CyNetworkManager networkManager;
	
	@Inject
	public RemoveSignatureDataSetsTask(@Assisted Collection<EMSignatureDataSet> signatureDataSets, @Assisted EnrichmentMap map) {
		this.signatureDataSets = signatureDataSets;
		this.map = map;
	}
	
	@Override
	public void run(TaskMonitor taskMonitor) {
		CyNetwork network = networkManager.getNetwork(map.getNetworkID());
		
		if (network == null)
			throw new IllegalStateException("The Network with SUID " + map.getNetworkID() + " does not exist.");
		
		deleteSignatureNodesAndEdges(network);
		signatureDataSets.forEach(map::removeSignatureDataSet);
	}
	
	
	private void deleteSignatureNodesAndEdges(CyNetwork network) {
		Set<Long> edgeSuids = getEdgesToDelete();
		Set<Long> nodeSuids = getNodesToDelete();
		Collection<CyEdge> edges = getElements(edgeSuids, network::getEdge);
		Collection<CyNode> nodes = getElements(nodeSuids, network::getNode);
		network.removeEdges(edges);
		network.removeNodes(nodes);
	}
	
	private Set<Long> getNodesToDelete() {
		// Delete signature nodes that are in the given data sets but not in any other signature data sets
		Collection<EMSignatureDataSet> otherSignatureSets = new ArrayList<>(map.getSignatureSetList());
		otherSignatureSets.removeAll(signatureDataSets);
		Set<Long> nodesToKeep   = EnrichmentMap.getNodesUnion(otherSignatureSets);
		Set<Long> nodesToDelete = EnrichmentMap.getNodesUnion(signatureDataSets);
		nodesToDelete.removeAll(nodesToKeep);
		return nodesToDelete;
	}
	
	private Set<Long> getEdgesToDelete() {
		Set<Long> edgesToDelete = EnrichmentMap.getEdgesUnion(signatureDataSets);
		return edgesToDelete;
	}
	
	private static <T> Collection<T> getElements(Set<Long> suids, Function<Long,T> getter) {
		return suids.stream().map(getter).filter(n->n!=null).collect(Collectors.toList());
	}
	
}
