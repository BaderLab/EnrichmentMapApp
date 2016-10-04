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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.baderlab.csplugins.enrichmentmap.ApplicationModule.Edges;
import org.baderlab.csplugins.enrichmentmap.ApplicationModule.Nodes;
import org.baderlab.csplugins.enrichmentmap.heatmap.HeatMapPanel;
import org.baderlab.csplugins.enrichmentmap.heatmap.HeatMapParameters;
import org.baderlab.csplugins.enrichmentmap.view.EnrichmentMapInputPanel;
import org.baderlab.csplugins.enrichmentmap.view.ParametersPanel;
import org.baderlab.csplugins.enrichmentmap.view.PostAnalysisPanel;
import org.cytoscape.application.events.SetCurrentNetworkEvent;
import org.cytoscape.application.events.SetCurrentNetworkListener;
import org.cytoscape.application.events.SetCurrentNetworkViewEvent;
import org.cytoscape.application.events.SetCurrentNetworkViewListener;
import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.events.NetworkAboutToBeDestroyedEvent;
import org.cytoscape.model.events.NetworkAboutToBeDestroyedListener;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.CyNetworkView;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

/**
 * Main class managing all instances of enrichment map as well as singular
 * instances of heatmap panel, parameters panel and input panel. (implemented as singular class)
 */
@Singleton
public class EnrichmentMapManager implements SetCurrentNetworkListener, NetworkAboutToBeDestroyedListener, SetCurrentNetworkViewListener {
		
	@Inject private CyServiceRegistrar registrar;
	
	@Inject private Provider<ParametersPanel> parameterPanelProvider;
	@Inject private Provider<EnrichmentMapInputPanel> inputPanelProvider;
	@Inject private Provider<PostAnalysisPanel> postAnalysisPanelProvider;
	@Inject private @Nodes Provider<HeatMapPanel> nodesOverlapPanelProvider;
	@Inject private @Edges Provider<HeatMapPanel> edgesOverlapPanelProvider;

	private Map<Long, EnrichmentMap> enrichmentMaps = new HashMap<>();
	private Map<Long, HeatMapParameters> heatMapParameterMap = new HashMap<>();
	
	
//	private EnrichmentMapInputPanel inputWindow;
//	private PostAnalysisPanel analysisWindow;

	private boolean overrideHeatmapRevalidation = false;
	

	public void showPanels() {
		HeatMapPanel nodeOverlapPanel = nodesOverlapPanelProvider.get();
		HeatMapPanel edgeOverlapPanel = edgesOverlapPanelProvider.get();
		ParametersPanel parametersPanel = parameterPanelProvider.get();
		
		if(nodeOverlapPanel != null && edgeOverlapPanel != null && parametersPanel != null) {
			registrar.registerService(nodeOverlapPanel, CytoPanelComponent.class, new Properties());
			registrar.registerService(edgeOverlapPanel, CytoPanelComponent.class, new Properties());
			registrar.registerService(parametersPanel,  CytoPanelComponent.class, new Properties());
		}
	}

	/**
	 * Registers a newly created Network.
	 */
	public void registerEnrichmentMap(CyNetwork cyNetwork, EnrichmentMap map) {
		enrichmentMaps.put(cyNetwork.getSUID(), map);
	}

	public Map<Long, EnrichmentMap> getAllEnrichmentMaps() {
		return Collections.unmodifiableMap(enrichmentMaps);
	}

	public EnrichmentMap getEnrichmentMap(Long id) {
		return enrichmentMaps.get(id);
	}

	public void setHeatMapParameters(Long suid, HeatMapParameters parameters) {
		heatMapParameterMap.put(suid, parameters);
	}
	
	public HeatMapParameters getHeatMapParameters(Long suid) {
		return heatMapParameterMap.get(suid);
	}
	

//	public ParametersPanel getParameterPanel() {
//		return parameterPanelProvider.get();
//	}
//
//	public HeatMapPanel getNodesOverlapPanel() {
//		return nodesOverlapPanelProvider.get();
//	}
//
//	public HeatMapPanel getEdgesOverlapPanel() {
//		return edgesOverlapPanelProvider.get();
//	}
//	
//	public EnrichmentMapInputPanel getInputWindow() {
//		return inputPanelProvider.get();
//	}
//
//	public PostAnalysisPanel getAnalysisWindow() {
//		return postAnalysisPanelProvider.get();
//	}

	/**
	 * Returns true if the network with the identifier networkID an
	 * EnrichmentMap.<br> (and therefore an instance EnrichmentMapParameters is present)
	 */
	public boolean isEnrichmentMap(Long networkID) {
		return enrichmentMaps.containsKey(networkID);
	}

	/**
	 * Network Focus Event.
	 */
	public void handleEvent(SetCurrentNetworkEvent event) {
		long networkId = event.getNetwork().getSUID();
		if(networkId > 0) {
			
			HeatMapPanel nodesOverlapPanel = nodesOverlapPanelProvider.get();
			HeatMapPanel edgesOverlapPanel = edgesOverlapPanelProvider.get();
			ParametersPanel parameterPanel = parameterPanelProvider.get();
			EnrichmentMapInputPanel inputWindow = inputPanelProvider.get();
			PostAnalysisPanel analysisWindow = postAnalysisPanelProvider.get();
			
			// update view
			if(enrichmentMaps.containsKey(networkId)) {
				// clear the panels before re-initializing them
				nodesOverlapPanel.clearPanel();
				edgesOverlapPanel.clearPanel();

				EnrichmentMap currentNetwork = enrichmentMaps.get(networkId);
				// update the parameters panel
				parameterPanel.updatePanel(currentNetwork);

				// update the input window to contain the parameters of the
				// selected network
				// only if there is a input window
				if(inputWindow != null)
					inputWindow.updateContents(currentNetwork.getParams());

				if(analysisWindow != null)
					analysisWindow.showPanelFor(currentNetwork);

				nodesOverlapPanel.updatePanel(currentNetwork);
				edgesOverlapPanel.updatePanel(currentNetwork);

				nodesOverlapPanel.revalidate();
				edgesOverlapPanel.revalidate();
			} else {
				if(analysisWindow != null)
					analysisWindow.showPanelFor(null);
			}
		}
	}

	public void handleEvent(SetCurrentNetworkViewEvent e) {
		// make sure to clear the panel if there is no network view
		PostAnalysisPanel analysisWindow = postAnalysisPanelProvider.get();
		
		if(analysisWindow != null) {
			CyNetworkView view = e.getNetworkView();
			if(view == null) {
				analysisWindow.showPanelFor(null);
			} else {
				EnrichmentMap currentNetwork = enrichmentMaps.get(view.getModel().getSUID());
				analysisWindow.showPanelFor(currentNetwork); // may be null
			}
		}
	}

	
	/**
	 * Network about to be destroyed Event.
	 */
	public void handleEvent(NetworkAboutToBeDestroyedEvent event) {
		PostAnalysisPanel analysisWindow = postAnalysisPanelProvider.get();
		
		Long networkId = event.getNetwork().getSUID();
		EnrichmentMap removed = enrichmentMaps.remove(networkId);
		if(analysisWindow != null && removed != null)
			analysisWindow.removeEnrichmentMap(removed);
	}

	
	// MKTODO what is the difference between this and EnrichmentMapParameters.isDisableHeatmapAutofocus()?
	public boolean isOverrideHeatmapRevalidation() {
		return overrideHeatmapRevalidation;
	}

	public void setOverrideHeatmapRevalidation(boolean overrideHeatmapRevalidation) {
		this.overrideHeatmapRevalidation = overrideHeatmapRevalidation;
	}
}
