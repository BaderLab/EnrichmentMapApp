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

// $Id: LoadEnrichmentsPanelAction.java 286 2009-07-07 15:27:16Z risserlin $
// $LastChangedDate: 2009-07-07 11:27:16 -0400 (Tue, 07 Jul 2009) $
// $LastChangedRevision: 286 $
// $LastChangedBy: risserlin $
// $HeadURL: svn+ssh://risserlin@server1.baderlab.med.utoronto.ca/svn/EnrichmentMap/trunk/EnrichmentMapPlugin/src/org/baderlab/csplugins/enrichmentmap/LoadEnrichmentsPanelAction.java $

package org.baderlab.csplugins.enrichmentmap.autoannotate.action;

import org.baderlab.csplugins.enrichmentmap.EnrichmentMapManager;
import org.baderlab.csplugins.enrichmentmap.autoannotate.AutoAnnotationManager;
import org.baderlab.csplugins.enrichmentmap.autoannotate.view.AutoAnnotatorPanel;
import org.baderlab.csplugins.enrichmentmap.view.ParametersPanel;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.application.swing.CytoPanel;
import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.application.swing.CytoPanelState;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.OpenBrowser;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.presentation.annotations.AnnotationManager;
import org.cytoscape.work.swing.DialogTaskManager;

import java.awt.event.ActionEvent;
import java.util.Map;
import java.util.Properties;

/**
 * Created by
 * User: arkadyark
 * Date: June 17, 2014
 * Time: 8:46 AM
 *
 * Click on Annotate Clusters Action handler
 */
public class AutoAnnotatorPanelAction extends AbstractCyAction {

	private static final long serialVersionUID = 3764130543697594367L;


	//variable to track initialization of network event listener
    private boolean initialized = false;

    
    private final CytoPanel cytoPanelWest;
    
    private CyServiceRegistrar registrar;
	private CyApplicationManager applicationManager;
	private CySwingApplication application;
	private CyNetworkViewManager networkViewManager;
	private AnnotationManager annotationManager;
	private OpenBrowser openBrowser;
	private CyNetworkManager networkManager;
	private DialogTaskManager dialogTaskManager;
	private CyEventHelper eventHelper;
	
	private AutoAnnotationManager manager;


	private CyTableManager tableManager;

	private AutoAnnotatorPanel inputPanel;


	private EnrichmentMapManager emManager;


    
    public AutoAnnotatorPanelAction(Map<String,String> configProps, CyApplicationManager applicationManager, 
    			CyNetworkManager cyNetworkManagerRef, CyNetworkViewManager networkViewManager, 
    			CySwingApplication application, OpenBrowser openBrowserRef, AnnotationManager annotationManager, 
    			CyServiceRegistrar registrar, DialogTaskManager dialogTaskManager, CyEventHelper eventHelper,
    			AutoAnnotationManager autoAnnotationManager, CyTableManager tableManager, EnrichmentMapManager emManager){
    	
        super( configProps,  applicationManager,  networkViewManager);
     
 		putValue(NAME, "Annotate Clusters");		
 		
 		this.cytoPanelWest = application.getCytoPanel(CytoPanelName.WEST);
 		this.applicationManager = applicationManager;
 		this.networkManager = cyNetworkManagerRef;
 		this.networkViewManager = networkViewManager;
 		this.application = application;
 		this.openBrowser = openBrowserRef;
 		this.annotationManager = annotationManager;
 		this.registrar = registrar;
 		this.dialogTaskManager = dialogTaskManager;
 		this.eventHelper = eventHelper;
 		this.manager = autoAnnotationManager;
 		this.tableManager = tableManager;
 		this.emManager = emManager;
    }

	public void actionPerformed(ActionEvent event) {		
		if(!initialized) {
    		inputPanel = new AutoAnnotatorPanel(applicationManager, networkViewManager, 
    				application, openBrowser, networkManager, annotationManager, manager, registrar,
    				dialogTaskManager, eventHelper, tableManager, emManager);
    		inputPanel.updateSelectedView(applicationManager.getCurrentNetworkView());
    		manager.setInputPanel(inputPanel);
    		registrar.registerService(inputPanel,CytoPanelComponent.class, new Properties());
    		initialized = true;
    	}

          // If the state of the cytoPanelWest is HIDE, show it
          if (cytoPanelWest.getState() == CytoPanelState.HIDE) {
        	  cytoPanelWest.setState(CytoPanelState.DOCK);
          }
          
          // Select my panels
          int inputIndex = cytoPanelWest.indexOfComponent(inputPanel);
          if (inputIndex != -1) cytoPanelWest.setSelectedIndex(inputIndex);
        }
}
