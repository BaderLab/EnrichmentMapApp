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

package org.baderlab.csplugins.enrichmentmap.actions;

import javax.swing.*;

import org.baderlab.csplugins.enrichmentmap.EnrichmentMapManager;
import org.baderlab.csplugins.enrichmentmap.view.EnrichmentMapInputPanel;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.application.swing.CytoPanel;
import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.application.swing.CytoPanelState;
import org.cytoscape.io.util.StreamUtil;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.FileChooserFilter;
import org.cytoscape.util.swing.FileUtil;
import org.cytoscape.util.swing.OpenBrowser;
import org.cytoscape.view.model.CyNetworkViewManager;

import java.awt.event.ActionEvent;
import java.util.Map;
import java.util.Properties;

/**
 * Created by
 * User: risserlin
 * Date: Jan 28, 2009
 * Time: 12:30:13 PM
 *
 * Click on Build Enrichment Map Action handler
 */
public class LoadEnrichmentsPanelAction extends AbstractCyAction {

    //variable to track initialization of network event listener
    private boolean initialized = false;

    
    private final CytoPanel cytoPanelWest;
    private EnrichmentMapInputPanel EMinputPanel;
    private CyServiceRegistrar registrar;
    
    
    public LoadEnrichmentsPanelAction(Map<String,String> configProps, CyApplicationManager applicationManager, 
    			CyNetworkViewManager networkViewManager, CySwingApplication application,  
    			 EnrichmentMapInputPanel inputPanel, CyServiceRegistrar registrar){
        super( configProps,  applicationManager,  networkViewManager);
     
 		putValue(NAME, "Create Enrichment Map");		
 		
 		this.cytoPanelWest = application.getCytoPanel(CytoPanelName.WEST);
 		this.EMinputPanel = inputPanel;
 		this.registrar = registrar;
 		

    }

    public void actionPerformed(ActionEvent event) {
                
          if(!initialized){      
                initialized = true;

                //EnrichmentMapInputPanel inputwindow = new EnrichmentMapInputPanel(application,browser,streamUtilRef);
                registrar.registerService(this.EMinputPanel,CytoPanelComponent.class,new Properties());
          
                //set the input window in the instance so we can udate the instance window
                //on network focus
                EnrichmentMapManager.getInstance().setInputWindow(this.EMinputPanel);
          } 
                  
          // If the state of the cytoPanelWest is HIDE, show it
          if (cytoPanelWest.getState() == CytoPanelState.HIDE) {
        	  	cytoPanelWest.setState(CytoPanelState.DOCK);
          }

         // Select my panel
        int index = cytoPanelWest.indexOfComponent(this.EMinputPanel);
        if (index == -1) {
        	 	return;
        }
       cytoPanelWest.setSelectedIndex(index);

          
         

        }
    
}
