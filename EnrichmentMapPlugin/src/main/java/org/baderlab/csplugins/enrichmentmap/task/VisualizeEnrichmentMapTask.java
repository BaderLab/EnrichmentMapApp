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




import org.baderlab.csplugins.enrichmentmap.EnrichmentMapVisualStyle;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;

import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;


/**
 * Created by
 * User: risserlin
 * Date: Jan 8, 2009
 * Time: 4:11:11 PM
 * <p>
 * Create visual representation of enrichment map in cytoscape
 */
public class VisualizeEnrichmentMapTask extends AbstractTask {

    private EnrichmentMap map;
    private CyNetwork network;
    
    private CyNetworkManager networkManager;
    private CyNetworkViewManager networkViewManager;
    private CyNetworkViewFactory networkViewFactory;
    private VisualMappingManager visualMappingManager;
    private VisualStyleFactory visualStyleFactory;
    
    //we will need all three mappers
    private VisualMappingFunctionFactory vmfFactoryContinuous;
    private VisualMappingFunctionFactory vmfFactoryDiscrete;
    private VisualMappingFunctionFactory vmfFactoryPassthrough;
       

    //enrichment map name
    private String mapName;

    // Keep track of progress for monitoring:
    private TaskMonitor taskMonitor = null;

    /**
     * Class constructor - current task monitor
     *
     * @param params - enrichment map parameters for current map
     * @param taskMonitor - current task monitor
     */
    public VisualizeEnrichmentMapTask(EnrichmentMap map, 
    		CyNetworkManager networkManager, CyNetworkViewManager networkViewManager,
    		CyNetworkViewFactory networkViewFactory,
    		VisualMappingManager visualMappingManager,VisualStyleFactory visualStyleFactory,
    		VisualMappingFunctionFactory vmfFactoryContinuous, VisualMappingFunctionFactory vmfFactoryDiscrete,
     VisualMappingFunctionFactory vmfFactoryPassthrough) {
        this(map);       
        this.networkViewManager	= networkViewManager;
        this.networkViewFactory = networkViewFactory;
        this.visualMappingManager = visualMappingManager;
        this.visualStyleFactory = visualStyleFactory;
        
        this.vmfFactoryContinuous = vmfFactoryContinuous;
        this.vmfFactoryDiscrete = vmfFactoryDiscrete;
        this.vmfFactoryPassthrough = vmfFactoryPassthrough;        
        
        this.network = this.networkManager.getNetwork(map.getParams().getNetworkID());
    }

    /**
     * Class constructor
     *
     * @param params - enrichment map parameters for current map
     */
    public VisualizeEnrichmentMapTask(EnrichmentMap map) {
        this.map = map;
        mapName = "Enrichment Map";

    }

    /**
     * Compute, and create cytoscape enrichment map
     *
     * @return  true if successful
     */
    public boolean visualizeMap(){
            
    	String prefix = map.getParams().getAttributePrefix();
    	
    		//create the network view
            CyNetworkView view = networkViewFactory.createNetworkView( network );
            networkViewManager.addNetworkView(view);
            
            String vs_name = prefix + "Enrichment_map_style";
            // check to see if a visual style with this name already exists
            VisualStyle vs = visualStyleFactory.createVisualStyle(vs_name);

            if (vs != null) {
                // if not, create it and add it to the catalog
                // Create the visual style
                EnrichmentMapVisualStyle em_vs = new EnrichmentMapVisualStyle(map.getParams(),vmfFactoryContinuous,vmfFactoryDiscrete,vmfFactoryPassthrough);

                vs = em_vs.createVisualStyle(vs, prefix);
                
            }
            
            this.visualMappingManager.addVisualStyle(vs);
            
            vs.apply(view);
            view.updateView();
            
            //TODO:apply layouts
            //view.applyLayout(CyLayouts.getLayout("force-directed"));
           
            //TODO: add listeners and panels.
            //initialize parameter panel with info for this network
            /*ParametersPanel parametersPanel = EMmanager.getParameterPanel();
            parametersPanel.updatePanel(map);
            final CytoscapeDesktop desktop = Cytoscape.getDesktop();
            final CytoPanel cytoSidePanel = desktop.getCytoPanel(SwingConstants.EAST);
            cytoSidePanel.setSelectedIndex(cytoSidePanel.indexOfComponent(parametersPanel));

            //set focus to EnrichmentMapInputPanel (otherwise it is reverted to the Editor)
            EnrichmentMapInputPanel emInputPanel = EMmanager.getInputWindow();
            final CytoPanel cytoControlPanel = desktop.getCytoPanel(SwingConstants.WEST);
            cytoControlPanel.setSelectedIndex(cytoControlPanel.indexOfComponent(emInputPanel));

            //add the click on node/edge listener
            view.addGraphViewChangeListener(new EnrichmentMapActionListener(map));

            //make sure the network is registered so that Quickfind works
            Cytoscape.firePropertyChange(cytoscape.view.CytoscapeDesktop.NETWORK_VIEW_CREATED, network, view);
       		*/
        return true;
    }
   
    /**
     * Sets the Task Monitor.
     *
     * @param taskMonitor TaskMonitor Object.
     */
    public void setTaskMonitor(TaskMonitor taskMonitor) {
        if (this.taskMonitor != null) {
            throw new IllegalStateException("Task Monitor is already set.");
        }
        this.taskMonitor = taskMonitor;
    }

    /**
     * Gets the Task Title.
     *
     * @return human readable task title.
     */
    public String getTitle() {
        return new String("Building Enrichment Map");
    }

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		this.taskMonitor = taskMonitor;
		visualizeMap();		
		
	}

}
