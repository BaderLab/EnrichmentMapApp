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



import org.baderlab.csplugins.enrichmentmap.EnrichmentMapParameters;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.task.BuildEnrichmentMapTask;
import org.baderlab.csplugins.enrichmentmap.task.BuildGMTEnrichmentMapTask;
import org.baderlab.csplugins.enrichmentmap.view.EnrichmentMapInputPanel;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.io.util.StreamUtil;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyTableFactory;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.session.CySessionManager;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.swing.DialogTaskManager;

/**
 * Created by
 * User: risserlin
 * Date: Jan 8, 2009
 * Time: 1:35:40 PM
 * <p>
 * Action associated with the Build map button on the input panel
 * Checks minimum information is supplied before launching main build Enrichment map task
 */
public class EnrichmentMapParseInputTask extends AbstractTask {
    
	//services required
	private StreamUtil streamUtil;
    private CyApplicationManager applicationManager;
    private CyNetworkManager networkManager;
    private CyNetworkViewManager networkViewManager;
    private CyNetworkViewFactory networkViewFactory;
    private CyNetworkFactory networkFactory;
    private CyTableFactory tableFactory;
    private CyTableManager tableManager;
    
    private VisualMappingManager visualMappingManager;
    private VisualStyleFactory visualStyleFactory;
    
    //we will need all three mappers
    private VisualMappingFunctionFactory vmfFactoryContinuous;
    private VisualMappingFunctionFactory vmfFactoryDiscrete;
    private VisualMappingFunctionFactory vmfFactoryPassthrough;
    
    //
    private DialogTaskManager dialog;
    private CySessionManager sessionManager;
	
    private EnrichmentMapInputPanel inputPanel;
    private TaskMonitor taskMonitor;

    /**
     * @param panel - Enrichment map input panel
     */
    public EnrichmentMapParseInputTask (EnrichmentMapInputPanel panel,
    		CyNetworkFactory networkFactory, CyApplicationManager applicationManager, 
    		CyNetworkManager networkManager, CyNetworkViewManager networkViewManager,
    		CyTableFactory tableFactory,CyTableManager tableManager,CyNetworkViewFactory networkViewFactory,
    		VisualMappingManager visualMappingManager,VisualStyleFactory visualStyleFactory,
    		VisualMappingFunctionFactory vmfFactoryContinuous, VisualMappingFunctionFactory vmfFactoryDiscrete,
    	     VisualMappingFunctionFactory vmfFactoryPassthrough,DialogTaskManager dialog, CySessionManager sessionManager,StreamUtil streamUtil) {
        this.inputPanel = panel;
        this.networkFactory = networkFactory;
        this.applicationManager = applicationManager;
        this.networkManager = networkManager;
        this.networkViewManager	= networkViewManager;
        this.tableFactory = tableFactory;
        this.tableManager = tableManager;
        this.networkViewFactory = networkViewFactory;
        this.streamUtil = streamUtil;
        
        this.visualMappingManager = visualMappingManager;
        this.visualStyleFactory = visualStyleFactory;
        
        this.vmfFactoryContinuous = vmfFactoryContinuous;
        this.vmfFactoryDiscrete = vmfFactoryDiscrete;
        this.vmfFactoryPassthrough = vmfFactoryPassthrough;
        
        this.dialog = dialog;
        
        this.sessionManager = sessionManager;
    }

    /**
     * Creates a new task, checks the info in the parameters for the minimum amount of information
     *
     * @param event
     */
    public void build() {
       
       //make sure that the minimum information is set in the current set of parameters
       EnrichmentMapParameters params = inputPanel.getParams();
       
       //add the two datasets from the panel to the parameters
       params.addFiles(EnrichmentMap.DATASET1, inputPanel.getDataset1files());
       if(!inputPanel.getDataset2files().isEmpty())
    	   		params.addFiles(EnrichmentMap.DATASET2, inputPanel.getDataset2files());
       String errors = params.checkMinimalRequirements();

       if(errors.equalsIgnoreCase("")){
            BuildEnrichmentMapTask new_map = new BuildEnrichmentMapTask(params,networkFactory, 
            		applicationManager,networkManager,networkViewManager,tableFactory,tableManager,networkViewFactory, 
            		visualMappingManager,visualStyleFactory,
            		vmfFactoryContinuous, vmfFactoryDiscrete,vmfFactoryPassthrough, sessionManager,streamUtil);
                	    		
            dialog.execute(new_map.createTaskIterator());
       }
       else if(errors.equalsIgnoreCase("GMTONLY")){
           BuildGMTEnrichmentMapTask new_map = new BuildGMTEnrichmentMapTask(params,networkFactory, 
           		applicationManager,networkManager,networkViewManager,tableFactory,tableManager,networkViewFactory, 
           		visualMappingManager,visualStyleFactory,
           		vmfFactoryContinuous, vmfFactoryDiscrete,vmfFactoryPassthrough,sessionManager,streamUtil);
           dialog.execute(new_map.createTaskIterator());

       }
       else{
    	   //TODO:add error message
           //JOptionPane.showMessageDialog(,errors,"Invalid Input",JOptionPane.WARNING_MESSAGE);
       }


   }

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		this.taskMonitor = taskMonitor;
		build();
		// TODO Auto-generated method stub
		
	}

	
}
