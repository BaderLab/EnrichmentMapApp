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
import java.util.Properties;

import javax.annotation.Nullable;

import org.baderlab.csplugins.enrichmentmap.ApplicationModule;
import org.baderlab.csplugins.enrichmentmap.ApplicationModule.Edges;
import org.baderlab.csplugins.enrichmentmap.ApplicationModule.Nodes;
import org.baderlab.csplugins.enrichmentmap.heatmap.HeatMapPanel;
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
import com.google.inject.Singleton;

/**
 * Main class managing all instances of enrichment map as well as singular
 * instances of heatmap panel, parameters panel and input panel. (implemented as
 * singular class)
 */
@Singleton
public class EnrichmentMapManager implements SetCurrentNetworkListener, NetworkAboutToBeDestroyedListener, SetCurrentNetworkViewListener {
		
	@Inject private CyServiceRegistrar registrar;
	@Inject private ParametersPanel parameterPanel;
	@Inject private @Nodes @Nullable HeatMapPanel nodesOverlapPanel;
	@Inject private @Edges @Nullable HeatMapPanel edgesOverlapPanel;

	private HashMap<Long, EnrichmentMap> cyNetworkList = new HashMap<>();
	private EnrichmentMapInputPanel inputWindow;
	private PostAnalysisPanel analysisWindow;

	private boolean overrideHeatmapRevalidation = false;
	

	public void registerServices() {
		if(nodesOverlapPanel != null)
			registrar.registerService(nodesOverlapPanel, CytoPanelComponent.class, new Properties());
		if(edgesOverlapPanel != null)
			registrar.registerService(edgesOverlapPanel, CytoPanelComponent.class, new Properties());
		if(parameterPanel != null)
			registrar.registerService(parameterPanel,    CytoPanelComponent.class, new Properties());
	}

	/**
	 * Registers a newly created Network.
	 */
	public void registerNetwork(CyNetwork cyNetwork, EnrichmentMap map) {
		if(!cyNetworkList.containsKey(cyNetwork.getSUID()))
			cyNetworkList.put(cyNetwork.getSUID(), map);
	}

	public ParametersPanel getParameterPanel() {
		return parameterPanel;
	}

	public HeatMapPanel getNodesOverlapPanel() {
		return nodesOverlapPanel;
	}

	public HeatMapPanel getEdgesOverlapPanel() {
		return edgesOverlapPanel;
	}

	public HashMap<Long, EnrichmentMap> getCyNetworkList() {
		return cyNetworkList;
	}

	/**
	 * Returns the instance of EnrichmentMapParameters for the CyNetwork with
	 * the Identifier "name",<br> or null if the CyNetwork is not an EnrichmentMap.
	 * 
	 * @param name the Identifier of a network <br> (eg.: CyNetwork.getIdentifier() or CyNetworkView.getIdentifier() )
	 * @return EnrichmentMap
	 */
	public EnrichmentMap getMap(Long id) {
		if(cyNetworkList.containsKey(id))
			return cyNetworkList.get(id);
		else
			return null;
	}

	/**
	 * @return reference to the Enrichment Map Input Panel (WEST)
	 */
	public EnrichmentMapInputPanel getInputWindow() {
		return inputWindow;
	}

	/**
	 * @param reference to the Enrichment Map Input Panel (WEST)
	 */
	public void setInputWindow(EnrichmentMapInputPanel inputWindow) {
		this.inputWindow = inputWindow;
	}

	/**
	 * @return reference to the Post Analysis Input Panel (WEST)
	 */
	public PostAnalysisPanel getAnalysisWindow() {
		return analysisWindow;
	}

	/**
	 * @param reference to the Post Analysis Input Panel (WEST)
	 */
	public void setAnalysisWindow(PostAnalysisPanel analysisWindow) {
		this.analysisWindow = analysisWindow;
	}

	/**
	 * Returns true if the network with the identifier networkID an
	 * EnrichmentMap.<br> (and therefore an instance EnrichmentMapParameters is present)
	 */
	public boolean isEnrichmentMap(Long networkID) {
		return cyNetworkList.containsKey(networkID);
	}

	/**
	 * Network Focus Event.
	 */
	public void handleEvent(SetCurrentNetworkEvent event) {
		// get network id
		long networkId = event.getNetwork().getSUID();

		if(networkId > 0) {
			// update view
			if(cyNetworkList.containsKey(networkId)) {
				// clear the panels before re-initializing them
				nodesOverlapPanel.clearPanel();
				edgesOverlapPanel.clearPanel();

				EnrichmentMap currentNetwork = cyNetworkList.get(networkId);
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
		if(analysisWindow != null) {
			CyNetworkView view = e.getNetworkView();
			if(view == null) {
				analysisWindow.showPanelFor(null);
			} else {
				EnrichmentMap currentNetwork = cyNetworkList.get(view.getModel().getSUID());
				analysisWindow.showPanelFor(currentNetwork); // may be null
			}
		}
	}

	
	/**
	 * Network about to be destroyed Event.
	 */
	public void handleEvent(NetworkAboutToBeDestroyedEvent event) {
		Long networkId = event.getNetwork().getSUID();
		EnrichmentMap removed = cyNetworkList.remove(networkId);
		if(analysisWindow != null && removed != null)
			analysisWindow.removeEnrichmentMap(removed);
	}

	public boolean isOverrideHeatmapRevalidation() {
		return overrideHeatmapRevalidation;
	}

	public void setOverrideHeatmapRevalidation(boolean overrideHeatmapRevalidation) {
		this.overrideHeatmapRevalidation = overrideHeatmapRevalidation;
	}
}
