package org.baderlab.csplugins.enrichmentmap.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import org.baderlab.csplugins.enrichmentmap.model.AssociatedApp;
import org.baderlab.csplugins.enrichmentmap.model.Columns;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;

public class NetworkUtil {

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
	
	public static AssociatedApp getAssociatedApp(CyNetwork network) {
		CyTable table = network.getTable(CyNetwork.class, CyNetwork.HIDDEN_ATTRS);
		
		if(!Columns.EM_ASSOCIATED_APP.hasColumn(table))
			return null;
			
		String app = Columns.EM_ASSOCIATED_APP.get(network.getRow(network, CyNetwork.HIDDEN_ATTRS));
		
		if (AssociatedApp.GENEMANIA.name().equalsIgnoreCase(app))
			return AssociatedApp.GENEMANIA;
		if (AssociatedApp.STRING.name().equalsIgnoreCase(app))
			return AssociatedApp.STRING;
		
		return null;
	}
	
	public static boolean isAssociatedNetwork(CyNetwork network) {
		CyTable table = network.getTable(CyNetwork.class, CyNetwork.HIDDEN_ATTRS);
		return Columns.EM_NETWORK_SUID.hasColumn(table) && Columns.EM_NETWORK_SUID.get(network.getRow(network, CyNetwork.HIDDEN_ATTRS)) != null;
	}
	
	/**
	 * Returns the gene name of a node that belongs to an associated network (i.e. network created by another app).
	 */
	public static String getGeneName(CyNetwork network, CyNode node) {
		if (network != null && node != null) {
			AssociatedApp app = getAssociatedApp(network);
			
			if (app != null)
				return app.getGeneNameColumn().get(network.getRow(node));
		}
		
		return null;
	}
	
	/**
	 * Returns the query term for a gene name from an associated network (i.e. network created by another app).
	 */
	public static String getQueryTerm(CyNetwork network, String gene) {
		String queryTerm = null;
		
		if (network != null && gene != null) {
			AssociatedApp app = getAssociatedApp(network);
			
			if (app != null) {
				String colName = app.getGeneNameColumn().getBaseName();
				CyTable table = network.getDefaultNodeTable();
				
				Collection<CyRow> matchingRows = table.getMatchingRows(colName, gene);
				
				if (matchingRows != null && !matchingRows.isEmpty()) {
					for (CyRow row : matchingRows) {
						queryTerm = app.getQueryTermColumn().get(row);
						
						if (queryTerm != null)
							break;
					}
				}
			}
		}
		
		return queryTerm;
	}
}
