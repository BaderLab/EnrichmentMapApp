/**
 **                       EnrichmentMap Cytoscape Plugin
 **
 ** Copyright (c) 2008-2009 Bader Lab, Donnelly Centre for Cellular and Biomolecular 
 ** Research, University of Toronto
 **
 ** Contact: http://www.baderlab.org
 **
 ** Code written by: Ruth Isserlin
 ** Authors: Daniele Merico, Ruth Isserlin, Oliver Stueker, Gary D. Bader
 **
 ** This library is free software; you can redistribute it and/or modify it
 ** under the terms of the GNU Lesser General Public License as published
 ** by the Free Software Foundation; either version 2.1 of the License, or
 ** (at your option) any later version.
 **
 ** This library is distributed in the hope that it will be useful, but
 ** WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 ** MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 ** documentation provided hereunder is on an "as is" basis, and
 ** University of Toronto
 ** has no obligations to provide maintenance, support, updates, 
 ** enhancements or modifications.  In no event shall the
 ** University of Toronto
 ** be liable to any party for direct, indirect, special,
 ** incidental or consequential damages, including lost profits, arising
 ** out of the use of this software and its documentation, even if
 ** University of Toronto
 ** has been advised of the possibility of such damage.  
 ** See the GNU Lesser General Public License for more details.
 **
 ** You should have received a copy of the GNU Lesser General Public License
 ** along with this library; if not, write to the Free Software Foundation,
 ** Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 **
 **/

// $Id$
// $LastChangedDate$
// $LastChangedRevision$
// $LastChangedBy$
// $HeadURL$

package org.baderlab.csplugins.enrichmentmap.view.util;

import static org.baderlab.csplugins.enrichmentmap.style.MasterMapVisualStyle.Columns.NODE_GS_TYPE;
import static org.baderlab.csplugins.enrichmentmap.style.MasterMapVisualStyle.Columns.NODE_GS_TYPE_ENRICHMENT;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.baderlab.csplugins.enrichmentmap.model.EMCreationParameters;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;

public class SliderBarActionListener implements ChangeListener {

	private final SliderBarPanel panel;

	private final ArrayList<HiddenNode> hiddenNodes;
	private final ArrayList<CyEdge> hiddenEdges;

	private final EnrichmentMap map;
	private final CyNetworkView networkView;

	public SliderBarActionListener(SliderBarPanel panel, EnrichmentMap map, CyNetworkView networkView) {
		if (panel == null)
			throw new IllegalArgumentException("'panel' must not be null");
		if (map == null)
			throw new IllegalArgumentException("'map' must not be null");
		if (networkView == null)
			throw new IllegalArgumentException("'networkView' must not be null");
		if (!networkView.getModel().getSUID().equals(map.getNetworkID()))
			throw new IllegalArgumentException("'networkView' is not from the passed EnrichmentMap's network");
		
		this.panel = panel;
		this.map = map;
		this.networkView = networkView;
		hiddenNodes = new ArrayList<>();
		hiddenEdges = new ArrayList<>();
	}

	/**
	 * Go through the current map and hide or unhide any nodes or edges associated with the threshold change.
	 */
	@Override
	public void stateChanged(ChangeEvent e) {
		JSlider source = (JSlider) e.getSource();
		
		if (source.getValueIsAdjusting())
			return;
		
		panel.setValue(source.getValue());
		
		// Check to see if the event is associated with only edges
		if (panel.isEdgesOnly()) {
			hideEdgesOnly(e);
			return;
		}
		
		Double maxCutoff = source.getValue() / panel.getPrecision();
		Double minCutoff = source.getMinimum() / panel.getPrecision();
		System.out.println(minCutoff + " << >> " + maxCutoff);

		CyNetwork network = networkView.getModel();
		CyTable defNodeTable = network.getDefaultNodeTable();

		List<CyNode> nodeList = network.getNodeList();
		EMCreationParameters params = map.getParams();
		
		// Get the prefix of the current network
		final String prefix = params.getAttributePrefix();
		
		// TODO Just testing with one column for now:
		final String colName = params.getPValueColumnNames().iterator().next();
		
		//go through all the existing nodes to see if we need to hide any new nodes.
		for (CyNode node : nodeList) {
			View<CyNode> nodeView = networkView.getNodeView(node);
			CyRow row = network.getRow(node);

			// skip Node if it's not an Enrichment-Geneset (but e.g. a Signature-Hub)
			if (defNodeTable.getColumn(prefix + NODE_GS_TYPE) != null
					&& !NODE_GS_TYPE_ENRICHMENT.equalsIgnoreCase(row.get(prefix + NODE_GS_TYPE, String.class)))
				continue;

			Double value = row.get(colName, Double.class);

			//possible that there isn't a p-value for this geneset
			if (value == null)
				value = 0.99;

			if (value > maxCutoff || value < minCutoff) {
				List<CyEdge> edges = network.getAdjacentEdgeList(node, CyEdge.Type.ANY);
				
				for (CyEdge currentEdge : edges) {
					hiddenEdges.add(currentEdge);
					//hide the edges in the network as well. -->trying to fix issue with layouts.
					View<CyEdge> currentEdgeView = networkView.getEdgeView(currentEdge);
					currentEdgeView.setLockedValue(BasicVisualLexicon.EDGE_VISIBLE, false);
				}
				
				hiddenNodes.add(new HiddenNode(node,
						nodeView.getVisualProperty(BasicVisualLexicon.NODE_X_LOCATION),
						nodeView.getVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION)));
				nodeView.setLockedValue(BasicVisualLexicon.NODE_VISIBLE, false);
			}
		}
		System.out.println("\n#" + hiddenNodes.size());

		//go through all the hidden nodes to see if we need to restore any of them
		ArrayList<HiddenNode> unhiddenNodes = new ArrayList<>();
		ArrayList<CyEdge> unhiddenEdges = new ArrayList<>();

		for (HiddenNode hiddenNode : hiddenNodes) {
			CyNode node = hiddenNode.getNode();
			CyRow row = network.getRow(node);
			Double value = row.get(colName, Double.class);

			//possible that there isn't a p-value for this geneset
			if (value == null)
				value = 0.99;

			if (value <= maxCutoff && value >= minCutoff) {
				//network.restoreNode(currentNode);
				View<CyNode> currentNodeView = networkView.getNodeView(node);
				currentNodeView.setLockedValue(BasicVisualLexicon.NODE_VISIBLE, true);
				currentNodeView.setVisualProperty(BasicVisualLexicon.NODE_X_LOCATION, hiddenNode.getX());
				currentNodeView.setVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION, hiddenNode.getY());
				networkView.updateView();
				unhiddenNodes.add(hiddenNode);
			}
		}

		//For the unhidden edges we need to restore its edges with nodes that exist in the network
		//restore edges where both nodes are in the network.
		for (CyEdge edge : hiddenEdges) {
			View<CyNode> srcNodeView = networkView.getNodeView(edge.getSource());
			View<CyNode> tgtNodeView = networkView.getNodeView(edge.getTarget());
			
			if ((srcNodeView.getVisualProperty(BasicVisualLexicon.NODE_VISIBLE))
					&& (tgtNodeView.getVisualProperty(BasicVisualLexicon.NODE_VISIBLE))) {
				View<CyEdge> edgeView = networkView.getEdgeView(edge);
				edgeView.setLockedValue(BasicVisualLexicon.EDGE_VISIBLE, true);
				unhiddenEdges.add(edge);
			}
		}

		//remove the unhidden nodes from the list of hiddenNodes.
		for (Iterator<HiddenNode> k = unhiddenNodes.iterator(); k.hasNext();)
			hiddenNodes.remove(k.next());

		//remove the unhidden edges from the list of hiddenEdges.
		for (Iterator<CyEdge> k = unhiddenEdges.iterator(); k.hasNext();)
			hiddenEdges.remove(k.next());
		
		networkView.updateView();
	}

	public void hideEdgesOnly(ChangeEvent e) {
		JSlider source = (JSlider) e.getSource();
		Double minCutoff = source.getValue() / panel.getPrecision();
		Double maxCutoff = source.getMaximum() / panel.getPrecision();

		CyNetwork network = networkView.getModel();
		CyTable attributes = network.getDefaultEdgeTable();
		List<CyEdge> edgeList = network.getEdgeList();
		
		EMCreationParameters params = map.getParams();
		// Get the prefix of the current network
		final String prefix = map.getParams().getAttributePrefix();
		// TODO Just testing it with one column
		if (params.getSimilarityCutoffColumnNames().isEmpty()) return;
		String colName = params.getSimilarityCutoffColumnNames().iterator().next();
		
		// Go through all the existing edges to see if we need to hide any new edges.
		for (CyEdge edge : edgeList) {
			CyRow row = network.getRow(edge);
			
			// skip Node if it's not an Enrichment-Geneset (but e.g. a Signature-Hub)
			if (attributes.getColumn(prefix + NODE_GS_TYPE) != null
					&& !NODE_GS_TYPE_ENRICHMENT.equalsIgnoreCase(row.get(prefix + NODE_GS_TYPE, String.class)))
				continue;

			Double similarityCutoff = row.get(colName, Double.class);

			// Possible that there isn't a p-value for this geneset
			if (similarityCutoff == null)
				similarityCutoff = 0.1;

			if (similarityCutoff > maxCutoff || similarityCutoff < minCutoff) {
				hiddenEdges.add(edge);
				View<CyEdge> currentView = networkView.getEdgeView(edge);
				currentView.setLockedValue(BasicVisualLexicon.EDGE_VISIBLE, false);
			}
		}

		// Go through all the hidden edges to see if we need to restore any of them
		ArrayList<CyEdge> unhiddenEdges = new ArrayList<>();

		for (CyEdge edge : hiddenEdges) {
			CyRow row = network.getRow(edge);
			Double similarityCutoff = row.get(colName, Double.class);

			// Possible that there isn't a value for this geneset
			if (similarityCutoff == null)
				similarityCutoff = 0.1;

			if (similarityCutoff <= maxCutoff && similarityCutoff >= minCutoff) {
				View<CyEdge> currentView = networkView.getEdgeView(edge);
				currentView.setLockedValue(BasicVisualLexicon.EDGE_VISIBLE, true);
				unhiddenEdges.add(edge);
			}
		}

		// Remove the unhidden edges from the list of hiddenEdges.
		for (Iterator<CyEdge> k = unhiddenEdges.iterator(); k.hasNext();)
			hiddenEdges.remove(k.next());

		networkView.updateView();
	}

	private class HiddenNode {
		
		private final CyNode node;
		private final double x;
		private final double y;

		HiddenNode(CyNode node, double x, double y) {
			this.node = node;
			this.x = x;
			this.y = y;
		}

		CyNode getNode() {
			return node;
		}

		double getX() {
			return x;
		}

		double getY() {
			return y;
		}
	}
}
