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
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMapManager;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;

public class SliderBarActionListener implements ChangeListener {

	private CyApplicationManager applicationManager;
	private EnrichmentMapManager emManager;
	
	private SliderBarPanel panel;

	private ArrayList<HiddenNodes> hiddenNodes;
	private ArrayList<CyEdge> hiddenEdges;

	public SliderBarActionListener(SliderBarPanel panel, CyApplicationManager applicationManager,
			EnrichmentMapManager emManager) {
		this.panel = panel;
		this.applicationManager = applicationManager;
		this.emManager = emManager;
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
		
		//check to see if the event is associated with only edges
		if (panel.isEdgesOnly()) {
			hideEdgesOnly(e);
			return;
		}
		
		Double maxCutoff = source.getValue() / panel.getPrecision();
		Double minCutoff = source.getMinimum() / panel.getPrecision();

		CyNetwork network = this.applicationManager.getCurrentNetwork();
		CyNetworkView view = this.applicationManager.getCurrentNetworkView();
		CyTable defNodeTable = network.getDefaultNodeTable();

		List<CyNode> nodes = network.getNodeList();

		EnrichmentMap em = emManager.getEnrichmentMap(network.getSUID());
		EMCreationParameters params = em.getParams();
		//get the prefix of the current network
		String prefix = params.getAttributePrefix();

		/*
		 * There are two different ways to hide and restore nodes. if you hide
		 * the nodes from the view perspective the node is still in the
		 * underlying network but it is just not visible. So when the node is
		 * restored it is in the exact same state and location. The only problem
		 * with this way is if you try and re-layout your network it behaves as
		 * if all the nodes that are hidden are still there and the layout does
		 * not change. if you hide the node from the network perspective the
		 * node is deleted from the network and when the node is restored
		 * (granted that you have tracked references of the "hidden" nodes and
		 * edges) it restored to the top right of the panel and the user is
		 * required to relayout the nodes. (can also be done programmatically)
		 */

		/*
		 * for(CyNode i:nodes){ CyNode currentNode =
		 * network.getNode(i.getSUID()); View<CyNode> currentView = view
		 * .getNodeView(currentNode); Double pvalue_dataset1 =
		 * network.getRow(currentNode).get(prefix +
		 * attrib_dataset1,Double.class,);
		 * 
		 * if((pvalue_dataset1 > max_cutoff) || (pvalue_dataset1 < min_cutoff)){
		 * if(params.isTwoDatasets()){ Double pvalue_dataset2 =
		 * network.getRow(currentNode).get(prefix +
		 * attrib_dataset2,Double.class); if((pvalue_dataset2 > max_cutoff) ||
		 * (pvalue_dataset2 < min_cutoff)){ view.hideGraphObject(currentView); }
		 * else{ view.showGraphObject(currentView); //restore the edges as well
		 * List<CyEdge> edges = network.getAdjacentEdgeList(currentNode,
		 * CyEdge.Type.ANY); for(CyEdge m:edges){ View<CyEdge> currentEdgeView =
		 * view.getEdgeView(m); view.showGraphObject(currentEdgeView); }
		 * 
		 * }
		 * 
		 * } else{ view.hideGraphObject(currentView); } } else{
		 * view.showGraphObject(currentView); //restore the edges as well
		 * List<CyEdge> edges = network.getAdjacentEdgeList(currentNode,
		 * CyEdge.Type.ANY); for(CyEdge m:edges){ View<CyEdge> currentEdgeView =
		 * view.getEdgeView(edges[m]); view.showGraphObject(currentEdgeView); }
		 * } }
		 */

		String attrib1 = panel.getAttrib1();
		String attrib2 = panel.getAttrib2();
		
		//go through all the existing nodes to see if we need to hide any new nodes.
		for (CyNode i : nodes) {
			CyNode currentNode = network.getNode(i.getSUID());
			View<CyNode> currentView = view.getNodeView(currentNode);

			// skip Node if it's not an Enrichment-Geneset (but e.g. a Signature-Hub)
			if (defNodeTable.getColumn(prefix + NODE_GS_TYPE) != null
					&& !NODE_GS_TYPE_ENRICHMENT.equalsIgnoreCase(
							network.getRow(currentNode).get(prefix + NODE_GS_TYPE, String.class)))
				continue;

			Double pvalueDataset1 = network.getRow(currentNode).get(prefix + attrib1, Double.class);

			//possible that there isn't a p-value for this geneset
			if (pvalueDataset1 == null)
				pvalueDataset1 = 0.99;

			if ((pvalueDataset1 > maxCutoff) || (pvalueDataset1 < minCutoff)) {
				CyColumn col = network.getDefaultNodeTable().getColumn(prefix + attrib2);
				
				if (col != null) {
					Double pvalueDataset2 = network.getRow(currentNode).get(prefix + attrib2, Double.class);
					
					if (pvalueDataset2 == null)
						pvalueDataset2 = 0.99;

					if ((pvalueDataset2 > maxCutoff) || (pvalueDataset2 < minCutoff)) {
						List<CyEdge> edges = network.getAdjacentEdgeList(currentNode, CyEdge.Type.ANY);
						
						for (CyEdge m : edges) {
							CyEdge currentEdge = m;
							hiddenEdges.add(network.getEdge(currentEdge.getSUID()));
							//hide the edges in the network as well. -->trying to fix issue with layouts.
							View<CyEdge> currentEdgeView = view.getEdgeView(currentEdge);
							currentEdgeView.setLockedValue(BasicVisualLexicon.EDGE_VISIBLE, false);
						}

						hiddenNodes.add(new HiddenNodes(currentNode,
								currentView.getVisualProperty(BasicVisualLexicon.NODE_X_LOCATION),
								currentView.getVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION)));
						//network.hideNode(currentNode);
						currentView.setLockedValue(BasicVisualLexicon.NODE_VISIBLE, false);
					}
				} else {
					List<CyEdge> edges = network.getAdjacentEdgeList(currentNode, CyEdge.Type.ANY);
					
					for (CyEdge m : edges) {
						CyEdge currentEdge = m;
						hiddenEdges.add(network.getEdge(currentEdge.getSUID()));
						//hide the edges in the network as well. -->trying to fix issue with layouts.
						View<CyEdge> currentEdgeView = view.getEdgeView(currentEdge);
						currentEdgeView.setLockedValue(BasicVisualLexicon.EDGE_VISIBLE, false);
					}
					
					hiddenNodes.add(new HiddenNodes(currentNode,
							currentView.getVisualProperty(BasicVisualLexicon.NODE_X_LOCATION),
							currentView.getVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION)));
					//network.hideNode(currentNode);
					currentView.setLockedValue(BasicVisualLexicon.NODE_VISIBLE, false);
				}
			}
		}

		//go through all the hidden nodes to see if we need to restore any of them
		ArrayList<HiddenNodes> unhiddenNodes = new ArrayList<>();
		ArrayList<CyEdge> unhiddenEdges = new ArrayList<>();

		for (Iterator<HiddenNodes> j = hiddenNodes.iterator(); j.hasNext();) {
			HiddenNodes currentHN = (HiddenNodes) j.next();
			CyNode currentNode = currentHN.getNode();
			Double pvalue_dataset1 = network.getRow(currentNode).get(prefix + attrib1, Double.class);

			//possible that there isn't a p-value for this geneset
			if (pvalue_dataset1 == null)
				pvalue_dataset1 = 0.99;

			if ((pvalue_dataset1 <= maxCutoff) && (pvalue_dataset1 >= minCutoff)) {
				//network.restoreNode(currentNode);
				View<CyNode> currentNodeView = view.getNodeView(currentNode);
				currentNodeView.setLockedValue(BasicVisualLexicon.NODE_VISIBLE, true);
				currentNodeView.setVisualProperty(BasicVisualLexicon.NODE_X_LOCATION, currentHN.getX());
				currentNodeView.setVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION, currentHN.getY());
				view.updateView();
				unhiddenNodes.add(currentHN);
			}
			
			CyColumn col = network.getDefaultNodeTable().getColumn(prefix + attrib2);
			
			if (col != null) {
				Double pvalue_dataset2 = network.getRow(currentNode).get(prefix + attrib2, Double.class);

				if (pvalue_dataset2 == null)
					pvalue_dataset2 = 0.99;

				if ((pvalue_dataset2 <= maxCutoff) && (pvalue_dataset2 >= minCutoff)) {
					//network.restoreNode(currentNode);
					View<CyNode> currentNodeView = view.getNodeView(currentNode);
					currentNodeView.setLockedValue(BasicVisualLexicon.NODE_VISIBLE, true);
					currentNodeView.setVisualProperty(BasicVisualLexicon.NODE_X_LOCATION, currentHN.getX());
					currentNodeView.setVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION, currentHN.getY());
					unhiddenNodes.add(currentHN);
				}
			}
		}

		//For the unhidden edges we need to restore its edges with nodes that exist in the network
		//restore edges where both nodes are in the network.
		for (Iterator<CyEdge> k = hiddenEdges.iterator(); k.hasNext();) {
			CyEdge currentEdge = k.next();
			View<CyNode> nodeSourceView = view.getNodeView(currentEdge.getSource());
			View<CyNode> nodeTargetView = view.getNodeView(currentEdge.getTarget());
			
			if ((nodeSourceView.getVisualProperty(BasicVisualLexicon.NODE_VISIBLE))
					&& (nodeTargetView.getVisualProperty(BasicVisualLexicon.NODE_VISIBLE))) {
				View<CyEdge> currentView = view.getEdgeView(currentEdge);
				currentView.setLockedValue(BasicVisualLexicon.EDGE_VISIBLE, true);
				//network.restoreEdge(currentEdge);
				unhiddenEdges.add(currentEdge);
			}
		}

		//remove the unhidden nodes from the list of hiddenNodes.
		for (Iterator<HiddenNodes> k = unhiddenNodes.iterator(); k.hasNext();)
			hiddenNodes.remove(k.next());

		//remove the unhidden edges from the list of hiddenEdges.
		for(Iterator<CyEdge> k = unhiddenEdges.iterator(); k.hasNext();)
			hiddenEdges.remove(k.next());
		view.updateView();
	}

	public void hideEdgesOnly(ChangeEvent e) {
		JSlider source = (JSlider) e.getSource();
		Double minCutoff = source.getValue() / panel.getPrecision();
		Double maxCutoff = source.getMaximum() / panel.getPrecision();

		CyNetwork network = this.applicationManager.getCurrentNetwork();
		CyNetworkView view = this.applicationManager.getCurrentNetworkView();
		CyTable attributes = network.getDefaultEdgeTable();

		List<CyEdge> edges = network.getEdgeList();

		//get the prefix of the current network
		String prefix = emManager.getEnrichmentMap(network.getSUID()).getParams().getAttributePrefix();
		//go through all the existing nodes to see if we need to hide any new nodes.

		String attrib1 = panel.getAttrib1();
		
		for (CyEdge i : edges) {
			CyEdge currentEdge = network.getEdge(i.getSUID());

			// skip Node if it's not an Enrichment-Geneset (but e.g. a Signature-Hub)
			if (attributes.getColumn(prefix + NODE_GS_TYPE) != null
					&& !NODE_GS_TYPE_ENRICHMENT.equalsIgnoreCase(
							network.getRow(currentEdge).get(prefix + NODE_GS_TYPE, String.class)))
				continue;

			Double similarityCutoff = network.getRow(currentEdge).get(prefix + attrib1, Double.class);

			//possible that there isn't a p-value for this geneset
			if (similarityCutoff == null)
				similarityCutoff = 0.1;

			if ((similarityCutoff > maxCutoff) || (similarityCutoff < minCutoff)) {
				hiddenEdges.add(currentEdge);
				View<CyEdge> currentView = view.getEdgeView(currentEdge);
				currentView.setLockedValue(BasicVisualLexicon.EDGE_VISIBLE, false);
				//network.hideEdge(currentEdge);
			}
		}

		//go through all the hidden edges to see if we need to restore any of them
		ArrayList<CyEdge> unhiddenEdges = new ArrayList<>();

		for (Iterator<CyEdge> j = hiddenEdges.iterator(); j.hasNext();) {
			CyEdge currentEdge = (CyEdge) j.next();
			Double similarityCutoff = network.getRow(currentEdge).get(prefix + attrib1, Double.class);

			//possible that there isn't a p-value for this geneset
			if (similarityCutoff == null)
				similarityCutoff = 0.1;

			if ((similarityCutoff <= maxCutoff) && (similarityCutoff >= minCutoff)) {
				//network.restoreEdge(currentEdge);
				View<CyEdge> currentView = view.getEdgeView(currentEdge);
				currentView.setLockedValue(BasicVisualLexicon.EDGE_VISIBLE, true);
				unhiddenEdges.add(currentEdge);
			}
		}

		//remove the unhidden edges from the list of hiddenEdges.
		for (Iterator<CyEdge> k = unhiddenEdges.iterator(); k.hasNext();)
			hiddenEdges.remove(k.next());

		view.updateView();
	}

	private class HiddenNodes {
		CyNode node;
		double x;
		double y;

		public HiddenNodes(CyNode node, double x, double y) {
			this.node = node;
			this.x = x;
			this.y = y;
		}

		public CyNode getNode() {
			return node;
		}

		public double getX() {
			return x;
		}

		public double getY() {
			return y;
		}
	}
}
