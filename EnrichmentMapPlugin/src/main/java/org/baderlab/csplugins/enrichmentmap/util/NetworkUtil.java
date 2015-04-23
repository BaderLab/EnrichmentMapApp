package org.baderlab.csplugins.enrichmentmap.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
	
	
	/**
	 * Returns a single node that matches the given value.
	 * Returns null if there are no nodes that match or if there are multiple nodes that match.
	 */
	public static CyNode getNodeWithValue(CyNetwork net, CyTable table, String colname, String value) {
		return getObjectWithValue(net, table, colname, value,
			new NetworkGetter<CyNode>() {
				public CyNode get(CyNetwork network, Long suid) {
					return network.getNode(suid);
				}
			}
		);
	}

	/**
	 * Returns a single edge that matches the given value.
	 * Returns null if there are no edges that match or if there are multiple edges that match.
	 */
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
		T nodeOrEdge = null;
		
		Collection<CyRow> matchingRows = table.getMatchingRows(colname, value);
		for(CyRow row : matchingRows) {
			Long id = row.get(CyNetwork.SUID, Long.class);
			if(id == null)
				continue;
			T currentNodeOrEdge = getter.get(net, id);
			if(nodeOrEdge == null) {
				nodeOrEdge = currentNodeOrEdge;
			}
			else if(currentNodeOrEdge != null) {
				// found 2 nodes or edges that match the criteria
				//System.out.println("There are at least 2 nodes or edges that match for name: " + value);
				return null;
			}
		}
		return nodeOrEdge;
	}
	
	/**
	 * Maps a collection of nodes or edges to a list of their SUIDs.
	 */
	public static List<Long> keys(Collection<? extends CyIdentifiable> nodesOrEdges) {
		List<Long> keys = new ArrayList<>(nodesOrEdges.size());
		for(CyIdentifiable obj : nodesOrEdges) {
			keys.add(obj.getSUID());
		}
		return keys;
	}
	
}
