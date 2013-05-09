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

import org.baderlab.csplugins.enrichmentmap.EnrichmentMapManager;
import org.baderlab.csplugins.enrichmentmap.view.PostAnalysisInputPanel;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.application.swing.CytoPanel;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.application.swing.CytoPanelState;
import org.cytoscape.view.model.CyNetworkViewManager;

import java.awt.event.ActionEvent;
import java.util.Map;

/**
 * Created by
 * User: revilo
 * Date: July 9, 2009
 * Based on: LoadEnrichmentsPanel by risserlin
 */
public class LoadPostAnalysisPanelAction extends AbstractCyAction {

    //variable to track initialization of network event listener
    private boolean initialized = false;
    private int index = 0;
        
    private final CytoPanel cytoPanelWest;
    private PostAnalysisInputPanel postEMinputPanel;
    
    public LoadPostAnalysisPanelAction(Map<String,String> configProps, CyApplicationManager applicationManager, 
			CyNetworkViewManager networkViewManager,CySwingApplication application,  PostAnalysisInputPanel inputPanel){
    super( configProps,  applicationManager,  networkViewManager);
 
		putValue(NAME, "Load Post Analysis Panel");
				
		this.cytoPanelWest = application.getCytoPanel(CytoPanelName.WEST);
		this.postEMinputPanel = inputPanel;

    }

    public void actionPerformed(ActionEvent event) {

        String os = System.getProperty("os.name");     

        if(!initialized){
            EnrichmentMapManager.getInstance();
            initialized = true;
            
            //set the input window in the instance so we can update the instance window
            //on network focus
            EnrichmentMapManager.getInstance().setAnalysisWindow(this.postEMinputPanel);

         // If the state of the cytoPanelWest is HIDE, show it
            if (cytoPanelWest.getState() == CytoPanelState.HIDE) {
                    cytoPanelWest.setState(CytoPanelState.DOCK);
            }

            // Select my panel
            int index = cytoPanelWest.indexOfComponent(this.postEMinputPanel);
            if (index == -1) {
                    return;
            }
            cytoPanelWest.setSelectedIndex(index);
      
        }
        else{

            //check to see that the input window hasn't been closed

            PostAnalysisInputPanel analysisWindow  = EnrichmentMapManager.getInstance().getAnalysisWindow();
            if(analysisWindow == null){
                /*analysisWindow = new PostAnalysisInputPanel();
                EnrichmentMapManager.getInstance().setAnalysisWindow(analysisWindow);  
                
             // If the state of the cytoPanelWest is HIDE, show it
                if (cytoPanelWest.getState() == CytoPanelState.HIDE) {
                        cytoPanelWest.setState(CytoPanelState.DOCK);
                }

                // Select my panel
                int index = cytoPanelWest.indexOfComponent(this.postEMinputPanel);
                if (index == -1) {
                        return;
                }
                cytoPanelWest.setSelectedIndex(index);
*/
            }

        }
    }
}
