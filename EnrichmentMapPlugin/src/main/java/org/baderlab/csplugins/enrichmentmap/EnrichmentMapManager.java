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

package org.baderlab.csplugins.enrichmentmap;


import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.view.EnrichmentMapInputPanel;
import org.baderlab.csplugins.enrichmentmap.view.HeatMapPanel;
import org.baderlab.csplugins.enrichmentmap.view.ParametersPanel;
import org.baderlab.csplugins.enrichmentmap.view.PostAnalysisInputPanel;
import org.cytoscape.application.events.SetCurrentNetworkEvent;
import org.cytoscape.application.events.SetCurrentNetworkListener;
import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.events.NetworkAboutToBeDestroyedEvent;
import org.cytoscape.model.events.NetworkAboutToBeDestroyedListener;
import org.cytoscape.service.util.CyServiceRegistrar;

import java.util.HashMap;
import java.util.Properties;


/**
 * Created by
 * User: risserlin
 * Date: Mar 3, 2009
 * Time: 1:48:31 PM
 * <p>
 * Main class managing all instances of enrichment map as well as singular instances
 * of heatmap panel, parameters panel and input panel.  (implemented as singular class)
 */
public class EnrichmentMapManager implements SetCurrentNetworkListener, NetworkAboutToBeDestroyedListener {

    private static EnrichmentMapManager manager = null;

    private HashMap<Long,EnrichmentMap> cyNetworkList;
    private HashMap<Long,PostAnalysisParameters> cyNetworkListPostAnalysis;

    //create only one instance of the  parameter and expression panels.
    private ParametersPanel parameterPanel;
    private HeatMapPanel nodesOverlapPanel;
    private HeatMapPanel edgesOverlapPanel;

    private EnrichmentMapInputPanel inputWindow;
    private PostAnalysisInputPanel analysisWindow;
    private CyServiceRegistrar registrar;

    /**
     * Method to get instance of EnrichmentMapManager.
     *
     * @return EnrichmentMapManager
     */
    public static EnrichmentMapManager getInstance() {
        if(manager == null)
            manager = new EnrichmentMapManager();
        return manager;
    }


    /**
     * initialize important variable
     */
    public void initialize(ParametersPanel parameterPanel,
    		HeatMapPanel nodesOverlapPanel, HeatMapPanel edgesOverlapPanel, CyServiceRegistrar registrar){
    		this.registrar = registrar;
    		
    		//initialize the parameters panel
    		this.parameterPanel = parameterPanel;       
        registrar.registerService(this.parameterPanel,CytoPanelComponent.class,new Properties());
        

        this.nodesOverlapPanel = nodesOverlapPanel;
        this.edgesOverlapPanel = edgesOverlapPanel;
        registrar.registerService(this.nodesOverlapPanel,CytoPanelComponent.class,new Properties());
        registrar.registerService(this.edgesOverlapPanel,CytoPanelComponent.class,new Properties());
          
    }
    
    /**
     * Constructor (private).
     *
     */
    private EnrichmentMapManager() {
       this.cyNetworkList = new HashMap<Long,EnrichmentMap>();
        this.cyNetworkListPostAnalysis = new HashMap<Long,PostAnalysisParameters>();
                
    }

    /**
     * Registers a newly created Network.
     *
     * @param cyNetwork Object.
     */
    public void registerNetwork(CyNetwork cyNetwork, EnrichmentMap map) {

        if(!cyNetworkList.containsKey(cyNetwork.getSUID()))
            cyNetworkList.put(cyNetwork.getSUID(),map);

    }

    public void registerNetwork(CyNetwork cyNetwork, PostAnalysisParameters paParams) {

        if(!cyNetworkListPostAnalysis.containsKey(cyNetwork.getSUID()))
            cyNetworkListPostAnalysis.put(cyNetwork.getSUID(),paParams);

    }


    /**
     * @return reference to the EM Legend Panel (EAST)
     */
    public ParametersPanel getParameterPanel() {
        return parameterPanel;
    }

    /**
     * @return reference to the EM Geneset Expression Viewer Panel (SOUTH)
     */
    public HeatMapPanel getNodesOverlapPanel() {
        return nodesOverlapPanel;
    }

    /**
     * @return reference to the EM Overlap Expression Viewer Panel (SOUTH)
     */
    public HeatMapPanel getEdgesOverlapPanel() {
        return edgesOverlapPanel;
    }

    /**
     * @return hashmap with instances of EnrichmentMap referenced by network Identifiers. (eg.: CyNetwork.getIdentifier() or CyNetworkView.getIdentifier() )
     */
    public HashMap<Long, EnrichmentMap> getCyNetworkList() {
        return cyNetworkList;
    }

    /**
     * Returns the instance of EnrichmentMapParameters for the CyNetwork with the Identifier "name",<br>
     * or null if the CyNetwork is not an EnrichmentMap. 
     * 
     * @param name the Identifier of a network <br>(eg.: CyNetwork.getIdentifier() or CyNetworkView.getIdentifier() )
     * @return EnrichmentMap
     */
    public EnrichmentMap getMap(Long id){
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
    public PostAnalysisInputPanel getAnalysisWindow() {
        return analysisWindow;
    }

    /**
     * @param reference to the Post Analysis Input Panel (WEST) 
     */
    public void setAnalysisWindow(PostAnalysisInputPanel analysisWindow) {
        this.analysisWindow = analysisWindow;
    }
    
    /**
     * Returns true if the network with the identifier networkID an EnrichmentMap.<br>
     * (and therefore an instance EnrichmentMapParameters is present) 
     * 
     * @param networkID
     * @return true or false
     */
    public boolean isEnrichmentMap(Long networkID){
        if (cyNetworkList.containsKey(networkID))
            return true;
        else
            return false;
    }

    /**
     * Network Focus Event.
     *
     * @param event PropertyChangeEvent
     */

	public void handleEvent(SetCurrentNetworkEvent event) {
        // get network id
        long networkId= event.getNetwork().getSUID();

        

        if (networkId > 0) {
            // update view
                if (cyNetworkList.containsKey(networkId)) {
                    //clear the panels before re-initializing them
                    nodesOverlapPanel.clearPanel();
                    edgesOverlapPanel.clearPanel();

                     EnrichmentMap currentNetwork= cyNetworkList.get(networkId);
                    //update the parameters panel
                    parameterPanel.updatePanel(currentNetwork);

                    //update the input window to contain the parameters of the selected network
                    //only if there is a input window
                    if(inputWindow!=null)
                        inputWindow.updateContents(currentNetwork.getParams());

                    if(analysisWindow!=null)
                        analysisWindow.updateContents(currentNetwork.getParams());

                    nodesOverlapPanel.updatePanel(currentNetwork);
                    edgesOverlapPanel.updatePanel(currentNetwork);

                    nodesOverlapPanel.revalidate();
                    edgesOverlapPanel.revalidate();
               
            }

        }
		
	}

    /**
     * Network about to be destroyed Event.
     *
     * @param event PropertyChangeEvent
     */
	public void handleEvent(NetworkAboutToBeDestroyedEvent event) {
		Long networkId = event.getNetwork().getSUID();
		
		// get the index (if it exists) of this network in our list
        // if it exists, remove it
        if (cyNetworkList.containsKey(networkId)) 
            cyNetworkList.remove(networkId);
        if (cyNetworkListPostAnalysis.containsKey(networkId)) 
        		cyNetworkListPostAnalysis.remove(networkId);
        
		
	}
    
}
