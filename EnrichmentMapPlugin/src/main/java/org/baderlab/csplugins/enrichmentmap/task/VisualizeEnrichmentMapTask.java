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

package org.baderlab.csplugins.enrichmentmap.task;

import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.style.EnrichmentMapVisualStyle;
import org.baderlab.csplugins.enrichmentmap.view.ParametersPanel;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.view.layout.CyLayoutAlgorithm;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.assistedinject.Assisted;

/**
 * Create visual representation of enrichment map in cytoscape
 */
public class VisualizeEnrichmentMapTask extends AbstractTask {

	private final EnrichmentMap map;

	@Inject private CyNetworkManager networkManager;
	@Inject private CyNetworkViewManager networkViewManager;
	@Inject private CyNetworkViewFactory networkViewFactory;
	@Inject private VisualMappingManager visualMappingManager;
	@Inject private VisualStyleFactory visualStyleFactory;
	@Inject private CyLayoutAlgorithmManager layoutManager;

	@Inject private EnrichmentMapVisualStyle.Factory emVisualStyleFactory;
	@Inject private Provider<ParametersPanel> parametersPanelProvider;
	

	public interface Factory {
		VisualizeEnrichmentMapTask create(EnrichmentMap map);
	}
	
	@Inject
	public VisualizeEnrichmentMapTask(@Assisted EnrichmentMap map) {
		this.map = map;
	}

	
	/**
	 * Compute, and create cytoscape enrichment map
	 */
	public void visualizeMap(TaskMonitor taskMonitor) {
		CyNetwork network = networkManager.getNetwork(map.getNetworkID());
		String prefix = map.getParams().getAttributePrefix();

		//create the network view
		CyNetworkView view = networkViewFactory.createNetworkView(network);
		networkViewManager.addNetworkView(view);

		String vs_name = prefix + "Enrichment_map_style";

		EnrichmentMapVisualStyle em_vs = emVisualStyleFactory.create(map);
		
		VisualStyle vs = visualStyleFactory.createVisualStyle(vs_name);
		em_vs.applyVisualStyle(vs, prefix);

		visualMappingManager.addVisualStyle(vs);
		visualMappingManager.setCurrentVisualStyle(vs);

		vs.apply(view);
		view.updateView();

		//apply force directed layout
		CyLayoutAlgorithm layout = layoutManager.getLayout("force-directed");
		if(layout != null) {
			String layoutAttribute = null;
			insertTasksAfterCurrentTask(layout.createTaskIterator(view, layout.createLayoutContext(),
					CyLayoutAlgorithm.ALL_NODE_VIEWS, layoutAttribute));
		}
		
		//update Parameter panel
		ParametersPanel parametersPanel = parametersPanelProvider.get();
		parametersPanel.updatePanel(map);
	}

	public String getTitle() {
		return "Building Enrichment Map";
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		taskMonitor.setTitle("Creating Network View");
		visualizeMap(taskMonitor);
	}

}
