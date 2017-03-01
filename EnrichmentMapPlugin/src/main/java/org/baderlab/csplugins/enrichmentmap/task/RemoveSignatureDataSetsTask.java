package org.baderlab.csplugins.enrichmentmap.task;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.baderlab.csplugins.enrichmentmap.model.EMSignatureDataSet;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskMonitor;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

public class RemoveSignatureDataSetsTask extends AbstractTask implements ObservableTask {

	public interface Factory {
		RemoveSignatureDataSetsTask create(
				@Assisted Collection<EMSignatureDataSet> dataSets,
				@Assisted EnrichmentMap map
		);
	}

	private Collection<EMSignatureDataSet> dataSets;
	private EnrichmentMap map;
	
	@Inject private CyNetworkManager networkManager;
	
	@Inject
	public RemoveSignatureDataSetsTask(@Assisted Collection<EMSignatureDataSet> dataSets, @Assisted EnrichmentMap map) {
		this.dataSets = dataSets;
		this.map = map;
	}
	
	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		CyNetwork network = networkManager.getNetwork(map.getNetworkID());
		
		if (network == null)
			throw new IllegalStateException("The Network with SUID " + map.getNetworkID() + " does not exist.");
		
		// TODO Undo option
		
		dataSets.forEach(ds -> {
			// TODO Delete associated columns?
			// Delete hub-nodes
			deleteNodes(ds.getNodeSuids().values(), network);
			// Remove Signature Data Set from Enrichment Map
			map.removeSignatureDataSet(ds);
		});
	}
	
	@Override
	public <R> R getResults(Class<? extends R> type) {
		return null;
	}

	private void deleteNodes(Collection<Long> suidList, CyNetwork net) {
		Set<CyNode> nodesToDelete = new HashSet<>();
		
		suidList.forEach(suid -> {
			CyNode node = net.getNode(suid);
			
			if (node != null)
				nodesToDelete.add(node);
		});
		
		if (!nodesToDelete.isEmpty())
			net.removeNodes(nodesToDelete);
	}
}
