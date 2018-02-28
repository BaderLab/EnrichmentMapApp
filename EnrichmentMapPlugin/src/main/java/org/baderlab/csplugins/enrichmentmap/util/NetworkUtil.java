package org.baderlab.csplugins.enrichmentmap.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import org.baderlab.csplugins.enrichmentmap.style.GMStyleBuilder.Columns;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;

public class NetworkUtil {

	public static final String EM_NETWORK_SUID_COLUMN = "EM_Network.SUID";
	
	private NetworkUtil() { }
	
	public static String getName(final CyNetwork network) {
		String name = "";
		
		try {
			name = network.getRow(network).get(CyNetwork.NAME, String.class);
		} catch (Exception e) {
		}
		
		if (name == null || name.trim().isEmpty())
			name = "? (SUID: " + network.getSUID() + ")";
		
		return name;
	}
	
	public static String getTitle(final CyNetworkView view) {
		String title = view.getVisualProperty(BasicVisualLexicon.NETWORK_TITLE);
		
		if (title == null || title.trim().isEmpty())
			title = getName(view.getModel());
		
		return title;
	}
	
	/**
	 * Returns a single node that matches the given value.
	 * Returns null if there are no nodes that match or if there are multiple nodes that match.
	 */
	public static CyNode getNodeWithValue(CyNetwork network, CyTable table, String colname, String value) {
		return getObjectWithValue(table, colname, value, network::getNode);
	}

	/**
	 * Returns a single edge that matches the given value.
	 * Returns null if there are no edges that match or if there are multiple edges that match.
	 */
	public static CyEdge getEdgeWithValue(CyNetwork network, CyTable table, String colname, String value) {
		return getObjectWithValue(table, colname, value, network::getEdge);
	}
	
	private static <T> T getObjectWithValue(CyTable table, String colname, String value, Function<Long, T> getter) {
		T nodeOrEdge = null;
		
		Collection<CyRow> matchingRows = table.getMatchingRows(colname, value);
		for(CyRow row : matchingRows) {
			Long id = row.get(CyNetwork.SUID, Long.class);
			if(id == null)
				continue;
			T currentNodeOrEdge = getter.apply(id);
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
	
	public static boolean isGeneManiaNetwork(CyNetwork network) {
		CyTable table = network.getTable(CyNetwork.class, CyNetwork.HIDDEN_ATTRS);
		
		return table.getColumn(EM_NETWORK_SUID_COLUMN) != null
				&& network.getRow(network, CyNetwork.HIDDEN_ATTRS).get(EM_NETWORK_SUID_COLUMN, Long.class) != null;
	}
	
	public static String getGeneManiaOrganism(CyNetwork network) {
		if (isGeneManiaNetwork(network))
			return Columns.GM_ORGANISM.get(network.getRow(network));
		
		return null;
	}
}
