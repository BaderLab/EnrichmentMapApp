package org.baderlab.csplugins.enrichmentmap.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;

public class NetworkUtil {

	private NetworkUtil() { }
	
	
	private interface NetworkGetter<T> {
		T get(CyNetwork network, Long suid);
	}
	
	public static CyNode getNodeWithValue(CyNetwork net, CyTable table, String colname, String value) {
		return getObjectWithValue(net, table, colname, value,
			new NetworkGetter<CyNode>() {
				public CyNode get(CyNetwork network, Long suid) {
					return network.getNode(suid);
				}
			}
		);
	}

	public static CyEdge getEdgeWithValue(CyNetwork net, CyTable table, String colname, String value) {
		return getObjectWithValue(net, table, colname, value,
			new NetworkGetter<CyEdge>() {
				public CyEdge get(CyNetwork network, Long suid) {
					return network.getEdge(suid);
				}
			}
		);
	}
	
	
	private static <T> T getObjectWithValue(CyNetwork net, CyTable table, String colname, String value, NetworkGetter<T> getter) {
		Collection<CyRow> matchingRows = table.getMatchingRows(colname, value);
		T nodeOrEdge = null;
		//only get the matching row if there is only one match
		if(matchingRows.size() == 1) {
			for(CyRow row : matchingRows) {
				Long id = row.get(CyNetwork.SUID, Long.class);
				if (id == null)
					continue;
				nodeOrEdge = getter.get(net, id);
				if (nodeOrEdge == null)
					continue;
			}
		}
		//There are multiple matches but check to see if they all belong to the same node.
		else {
			//Get the set of suid for the matching set
			Set<Long> ids = new HashSet<>();
			for(CyRow row : matchingRows) {
				Long id = row.get(CyNetwork.SUID, Long.class);
				if(id != null)
					ids.add(id);
			}
			if(!ids.isEmpty() && ids.size() == 1)
				nodeOrEdge = getter.get(net, ids.iterator().next());
			else
				System.out.println("There are multiple node/edge matches for name:" + value + " " + ids);
		}
		return nodeOrEdge;
    }
	
	
	public static List<Long> keys(Collection<? extends CyIdentifiable> nodesOrEdges) {
		List<Long> keys = new ArrayList<>(nodesOrEdges.size());
		for(CyIdentifiable obj : nodesOrEdges) {
			keys.add(obj.getSUID());
		}
		return keys;
	}
	
}
