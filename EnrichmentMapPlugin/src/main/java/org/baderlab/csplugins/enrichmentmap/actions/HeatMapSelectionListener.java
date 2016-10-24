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
import java.util.concurrent.ForkJoinPool;

import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMapManager;
import org.baderlab.csplugins.enrichmentmap.model.LegacySupport;
import org.baderlab.csplugins.enrichmentmap.task.heatmap.UpdateHeatMapTask;
import org.baderlab.csplugins.enrichmentmap.view.heatmap.HeatMapParameters;
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
public class HeatMapSelectionListener implements RowsSetListener {
	
	@Inject private CyApplicationManager applicationManager;
	@Inject private SynchronousTaskManager<?> syncTaskManager;
	@Inject private CySwingApplication swingApplication;
	
	@Inject private EnrichmentMapManager manager;
	@Inject private UpdateHeatMapTask.Factory updateHeatMapTaskFactory;
	

	/**
	 * intialize the parameters needed for this instance of the action
	 */
	private EnrichmentMap getAndInitializeEnrichmentMap(CyNetwork network) {
		Long suid = network.getSUID();
		EnrichmentMap map = manager.getEnrichmentMap(suid);
		
		if(LegacySupport.isLegacyEnrichmentMap(map)) {
			HeatMapParameters hmParams = manager.getHeatMapParameters(suid);
			if(hmParams == null) {
				hmParams = new HeatMapParameters(map);
				manager.setHeatMapParameters(suid, hmParams);
			}
			return map;
		}
		return null;
	}

	/**
	 * Handle network action. This method handles edge or node selection or unselections.
	 */
	public void handleEvent(RowsSetEvent e) {
		// TODO: improve performance of calculating the Union of genesets (Nodes) and intersection of overlaps (Edges)
		// Meanwhile we have a flag to skip the updating of the Heatmap, which can be toggled by a check-mark in the EM-Menu
		boolean override_revalidate_heatmap = manager.isOverrideHeatmapRevalidation();
		CyNetwork network = applicationManager.getCurrentNetwork();

		// only handle event if it is a selected node
		if (network != null && e != null && (e.getSource() == network.getDefaultEdgeTable() || e.getSource() == network.getDefaultNodeTable())) {
			final EnrichmentMap map = getAndInitializeEnrichmentMap(network);
			if (map != null) {

				// There is no flag to indicate that this is only an edge/node selection
				// After select get the nodes and the edges that were selected.
				if (!override_revalidate_heatmap) {
					
					List<CyNode> selectedNodes = CyTableUtil.getNodesInState(network, CyNetwork.SELECTED, true);
					List<CyEdge> selectedEdges = CyTableUtil.getEdgesInState(network, CyNetwork.SELECTED, true);
					
					CytoPanel cytoPanelSouth = swingApplication.getCytoPanel(CytoPanelName.SOUTH);
					// Once we have amalgamated all the nodes and edges, launch a task to update the heatmap.
					// Start the task in a separate thread to avoid Cytoscape deadlock bug (redmine issue #3370)
					ForkJoinPool.commonPool().execute(() -> {
						UpdateHeatMapTask updateHeatmap = updateHeatMapTaskFactory.create(map, selectedNodes, selectedEdges, cytoPanelSouth);
						syncTaskManager.execute(new TaskIterator(updateHeatmap));
					});
				}
			}
		} // end of if e.getSource check
	}

}
