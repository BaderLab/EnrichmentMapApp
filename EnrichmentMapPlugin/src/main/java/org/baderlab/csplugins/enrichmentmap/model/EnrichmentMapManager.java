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

import org.baderlab.csplugins.enrichmentmap.view.heatmap.HeatMapParams;
import org.baderlab.csplugins.enrichmentmap.view.postanalysis.PostAnalysisPanelMediator;
import org.cytoscape.view.model.CyNetworkView;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;


@Singleton
public class EnrichmentMapManager {
	
	private Map<Long, EnrichmentMap> enrichmentMaps = new LinkedHashMap<>();
	private Map<Long, HeatMapParams> heatMapParams = new HashMap<>();
	private Map<Long, HeatMapParams> heatMapParamsEdges = new HashMap<>();
	
	@Inject private Provider<PostAnalysisPanelMediator> postAnalysisMediatorProvider;

	/**
	 * Registers a newly created Network.
	 */
	public void registerEnrichmentMap(EnrichmentMap map) {
		enrichmentMaps.put(map.getNetworkID(), map);
	}

	public Map<Long, EnrichmentMap> getAllEnrichmentMaps() {
		return new LinkedHashMap<>(enrichmentMaps);
	}

	public EnrichmentMap getEnrichmentMap(Long networkId) {
		return enrichmentMaps.get(networkId);
	}
	
	public EnrichmentMap removeEnrichmentMap(Long networkId) {
		EnrichmentMap map = enrichmentMaps.remove(networkId);
		if(map != null) {
			postAnalysisMediatorProvider.get().removeEnrichmentMap(map);
		}
		return map;
	}
	
	public boolean isEnrichmentMap(Long networkId) {
		return enrichmentMaps.containsKey(networkId);
	}
	
	public boolean isEnrichmentMap(CyNetworkView networkView) {
		return isEnrichmentMap(networkView.getModel().getSUID());
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
		heatMapParams.clear();
	}
	
}
