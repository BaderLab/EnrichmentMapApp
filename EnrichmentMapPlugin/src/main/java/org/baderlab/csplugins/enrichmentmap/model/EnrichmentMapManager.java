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

package org.baderlab.csplugins.enrichmentmap.model;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.baderlab.csplugins.enrichmentmap.model.event.AssociatedEnrichmentMapsChangedEvent;
import org.baderlab.csplugins.enrichmentmap.model.event.EnrichmentMapAddedEvent;
import org.baderlab.csplugins.enrichmentmap.model.io.ModelSerializer;
import org.baderlab.csplugins.enrichmentmap.view.heatmap.HeatMapParams;
import org.baderlab.csplugins.enrichmentmap.view.postanalysis.PADialogMediator;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.events.NetworkAboutToBeDestroyedEvent;
import org.cytoscape.model.events.NetworkAboutToBeDestroyedListener;
import org.cytoscape.model.subnetwork.CyRootNetwork;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.events.NetworkViewAddedEvent;
import org.cytoscape.view.model.events.NetworkViewAddedListener;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;


@Singleton
public class EnrichmentMapManager implements NetworkAboutToBeDestroyedListener, NetworkViewAddedListener {
	
	private Map<Long, EnrichmentMap> enrichmentMaps = new LinkedHashMap<>();
	private Map<Long, EnrichmentMap> associatedEnrichmentMaps = new LinkedHashMap<>();
	private Map<Long, HeatMapParams> heatMapParams = new HashMap<>();
	private Map<Long, HeatMapParams> heatMapParamsEdges = new HashMap<>();
	
	@Inject private Provider<PADialogMediator> postAnalysisMediatorProvider;
	
	@Inject private CyEventHelper eventHelper;
	@Inject private CyNetworkManager networkManager;
	@Inject private CyRootNetworkManager rootNetworkManager;
	

	/**
	 * If the user creates a new network under the same network collection, we can just reuse the model.
	 */
	@Override
	public void handleEvent(NetworkViewAddedEvent e) {
		if(isEnrichmentMap(e.getNetworkView())) 
			return;
		
		CyNetwork newNetwork = e.getNetworkView().getModel();
		CyRootNetwork newNetworkRoot = rootNetworkManager.getRootNetwork(newNetwork);
		
		for(Long networkSuid : enrichmentMaps.keySet()) {
			CyNetwork network = networkManager.getNetwork(networkSuid);
			CyRootNetwork root = rootNetworkManager.getRootNetwork(network);
			if(root == newNetworkRoot) {
				System.out.println("Copying EnrichmentMap model");
				// newly created network is under an enrichmentmap network
				EnrichmentMap map = getEnrichmentMap(network.getSUID());
				if(map != null) {
					EnrichmentMap newMap = ModelSerializer.deepCopy(map);
					newMap.setNetworkID(newNetwork.getSUID());
					registerEnrichmentMap(newMap);
				}
				return;
			}
		}
	}
	
	@Override
	public void handleEvent(NetworkAboutToBeDestroyedEvent e) {
		removeEnrichmentMap(e.getNetwork().getSUID());
	}
	
	
	/**
	 * Registers a newly created Network.
	 */
	public void registerEnrichmentMap(EnrichmentMap map) {
		if(enrichmentMaps.containsKey(map.getNetworkID()))
			return;
		
		enrichmentMaps.put(map.getNetworkID(), map);
		for (Long id : map.getAssociatedNetworkIDs())
			associatedEnrichmentMaps.put(id, map);
		
		// This event is reentrant (fires on the same thread that called registerEnrichmentMap). But that is safe 
		// because the only listeners are inside this App so we can control what code is actually being run here.
		eventHelper.fireEvent(new EnrichmentMapAddedEvent(this, map));
	}

	
	public Map<Long, EnrichmentMap> getAllEnrichmentMaps() {
		return new LinkedHashMap<>(enrichmentMaps);
	}

	
	public EnrichmentMap getEnrichmentMap(Long networkId) {
		EnrichmentMap map = enrichmentMaps.get(networkId);
		return map != null ? map : associatedEnrichmentMaps.get(networkId);
	}
	
	
	public EnrichmentMap removeEnrichmentMap(Long networkId) {
		EnrichmentMap map = enrichmentMaps.remove(networkId);
		if (map != null)
			postAnalysisMediatorProvider.get().removeEnrichmentMap(map);
		
		// Update our internal genemania map and fire an event if it changed
		if (associatedEnrichmentMaps.remove(networkId) != null) {
			eventHelper.fireEvent(new AssociatedEnrichmentMapsChangedEvent(this, map, getAssociatedEnrichmentMaps()));
		}
		return map;
	}
	
	public boolean isEnrichmentMap(Long networkId) {
		return enrichmentMaps.containsKey(networkId);
	}
	
	public boolean isEnrichmentMap(CyNetworkView networkView) {
		return isEnrichmentMap(networkView.getModel().getSUID());
	}
	
	public void addAssociatedAppAttributes(CyNetwork network, EnrichmentMap map, AssociatedApp app) {
		// Add EM Network SUID to associated network's table.
		CyTable table = network.getTable(CyNetwork.class, CyNetwork.HIDDEN_ATTRS);
		
		Columns.EM_NETWORK_SUID.createColumnIfAbsent(table);
		Columns.EM_NETWORK_SUID.set(table.getRow(network.getSUID()), map.getNetworkID());
		
		// Add App name to associated network's hidden table, to make it easier and more consistent later
		Columns.EM_ASSOCIATED_APP.createColumnIfAbsent(table);
		Columns.EM_ASSOCIATED_APP.set(table.getRow(network.getSUID()), app.name());
		
		// Update our internal map and fire an event if it changed
		if (associatedEnrichmentMaps.put(network.getSUID(), map) == null) {
			eventHelper.fireEvent(new AssociatedEnrichmentMapsChangedEvent(this, map, getAssociatedEnrichmentMaps()));
		}
	}
	
	public boolean isAssociatedEnrichmentMap(Long networkId) {
		return associatedEnrichmentMaps.containsKey(networkId);
	}
	
	public boolean isAssociatedEnrichmentMap(CyNetworkView networkView) {
		return networkView != null && isAssociatedEnrichmentMap(networkView.getModel().getSUID());
	}
	
	public Map<Long, EnrichmentMap> getAssociatedEnrichmentMaps() {
		return new HashMap<>(associatedEnrichmentMaps);
	}
	
	public void registerHeatMapParams(Long networkId, boolean edges, HeatMapParams params) {
		if(edges)
			heatMapParamsEdges.put(networkId, params);
		else
			heatMapParams.put(networkId, params);
	}
	
	public HeatMapParams getHeatMapParams(Long networkId, boolean edges) {
		if(edges)
			return heatMapParamsEdges.get(networkId);
		else
			return heatMapParams.get(networkId);
	}
	
	public void reset() {
		enrichmentMaps.clear();
		associatedEnrichmentMaps.clear();
		heatMapParams.clear();
	}

}
