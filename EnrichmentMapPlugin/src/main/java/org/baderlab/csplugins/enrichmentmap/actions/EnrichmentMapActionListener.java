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

package org.baderlab.csplugins.enrichmentmap.actions;

import java.util.List;

import org.baderlab.csplugins.enrichmentmap.ApplicationModule.Edges;
import org.baderlab.csplugins.enrichmentmap.ApplicationModule.Nodes;
import org.baderlab.csplugins.enrichmentmap.heatmap.HeatMapPanel;
import org.baderlab.csplugins.enrichmentmap.heatmap.HeatMapParameters;
import org.baderlab.csplugins.enrichmentmap.heatmap.task.UpdateHeatMapTask;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMapManager;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.application.swing.CytoPanel;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTableUtil;
import org.cytoscape.model.events.RowsSetEvent;
import org.cytoscape.model.events.RowsSetListener;
import org.cytoscape.work.SynchronousTaskManager;
import org.cytoscape.work.TaskIterator;

import com.google.inject.Inject;

/**
 * Class listener for node and edge selections. For each enrichment map there is
 * a separate instance of this class specifying the enrichment map parameters,
 * selected nodes, selected edges and heatmap panels
 */
public class EnrichmentMapActionListener implements RowsSetListener {
	
	@Inject private EnrichmentMapManager manager;
	
	@Inject private CyApplicationManager applicationManager;
	@Inject private SynchronousTaskManager<?> syncTaskManager;
	@Inject private CySwingApplication swingApplication;
	
	@Inject private @Edges HeatMapPanel edgeOverlapPanel;
	@Inject private @Nodes HeatMapPanel nodeOverlapPanel;
	

	/**
	 * intialize the parameters needed for this instance of the action
	 */
	private EnrichmentMap getAndInitializeEnrichmentMap(CyNetwork network) {
		// get the static enrichment map manager.
		EnrichmentMap map = manager.getMap(network.getSUID());
		if (map != null) {
			if (map.getParams().isData() && map.getParams().getHmParams() == null) {
				// create a heatmap parameters instance for this action listener
				HeatMapParameters hmParams = new HeatMapParameters(edgeOverlapPanel, nodeOverlapPanel);
				// If there are two distinct datasets intialize the theme and range for the heatmap coloring separately.
				if (map.getParams().isData2() && map.getDataset(EnrichmentMap.DATASET2).getExpressionSets() != null && !map.getDataset(EnrichmentMap.DATASET1).getExpressionSets().getFilename().equalsIgnoreCase(map.getDataset(EnrichmentMap.DATASET2).getExpressionSets().getFilename()))
					hmParams.initColorGradients(map.getDataset(EnrichmentMap.DATASET1).getExpressionSets(), map.getDataset(EnrichmentMap.DATASET2).getExpressionSets());
				else
					hmParams.initColorGradients(map.getDataset(EnrichmentMap.DATASET1).getExpressionSets());
				// associate the newly created heatmap parameters with the current enrichment map paramters
				map.getParams().setHmParams(hmParams);
			}

		}
		return map;
	}

	/**
	 * Handle network action. This method handles edge or node selection or unselections.
	 */
	public void handleEvent(RowsSetEvent e) {
		// TODO: improve performance of calculating the Union of genesets (Nodes) and intersection of overlaps (Edges)
		// Meanwhile we have a flag to skip the updating of the Heatmap, which can be toggled by a check-mark in the EM-Menu
		boolean override_revalidate_heatmap = manager.isOverrideHeatmapRevalidation();

		CyNetwork network = this.applicationManager.getCurrentNetwork();

		// only handle event if it is a selected node
		if (network != null && e != null && (e.getSource() == network.getDefaultEdgeTable() || e.getSource() == network.getDefaultNodeTable())) {
			final EnrichmentMap map = getAndInitializeEnrichmentMap(network);
			if (map != null) {

				// There is no flag to indicate that this is only an edge/node selection
				// After select get the nodes and the edges that were selected.
				if (!override_revalidate_heatmap) {
					
					List<CyNode> selectedNodes = CyTableUtil.getNodesInState(network, CyNetwork.SELECTED, true);
					List<CyEdge> selectedEdges = CyTableUtil.getEdgesInState(network, CyNetwork.SELECTED, true);
					
					final List<CyNode> Nodes = map.getParams().getSelectedNodes();
					final List<CyEdge> Edges = map.getParams().getSelectedEdges();
					
					Nodes.clear();
					Nodes.addAll(selectedNodes);
					
					Edges.clear();
					Edges.addAll(selectedEdges);
					
					CytoPanel cytoPanelSouth = swingApplication.getCytoPanel(CytoPanelName.SOUTH);
					// Once we have amalgamated all the nodes and edges, launch a task to update the heatmap.
					// Start the task in a separate thread to avoid Cytoscape deadlock bug (redmine issue #3370)
					new Thread(() -> {
						UpdateHeatMapTask updateHeatmap = new UpdateHeatMapTask(map, Nodes, Edges, edgeOverlapPanel, nodeOverlapPanel, cytoPanelSouth, applicationManager);
						syncTaskManager.execute(new TaskIterator(updateHeatmap));
					}).start();
				}
			}
		} // end of if e.getSource check
	}

}
