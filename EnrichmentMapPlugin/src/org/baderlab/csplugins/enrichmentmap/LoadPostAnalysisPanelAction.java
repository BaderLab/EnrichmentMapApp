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

import cytoscape.Cytoscape;
import cytoscape.view.CytoscapeDesktop;
import cytoscape.view.cytopanels.CytoPanel;
import cytoscape.util.CytoscapeAction;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * Created by
 * User: revilo
 * Date: July 9, 2009
 * Based on: LoadGSEAPanel by risserlin 
 */
public class LoadPostAnalysisPanelAction extends CytoscapeAction {

    //variable to track initialization of network event listener
    private boolean initialized = false;
    private int index = 0;
    public LoadPostAnalysisPanelAction(){
        super("Load Post Analysis Panel");
    }

    public void actionPerformed(ActionEvent event) {

        String os = System.getProperty("os.name");

        CytoscapeDesktop desktop = Cytoscape.getDesktop();
        CytoPanel cytoPanel = desktop.getCytoPanel(SwingConstants.WEST);

        if(!initialized){
            EnrichmentMapManager.getInstance();
            initialized = true;

            PostAnalysisInputPanel analysisWindow = new PostAnalysisInputPanel();

            //set the input window in the instance so we can update the instance window
            //on network focus
            EnrichmentMapManager.getInstance().setAnalysisWindow(analysisWindow);

            cytoPanel.add("Post Analysis", analysisWindow);
            index =  cytoPanel.indexOfComponent(analysisWindow);

            cytoPanel.setSelectedIndex(index);
        }
        else{

            //check to see that the input window hasn't been closed

            PostAnalysisInputPanel analysisWindow  = EnrichmentMapManager.getInstance().getAnalysisWindow();
            if(analysisWindow == null){
                analysisWindow = new PostAnalysisInputPanel();
                EnrichmentMapManager.getInstance().setAnalysisWindow(analysisWindow);  
                cytoPanel.add("Post Analysis", analysisWindow);
                index =  cytoPanel.indexOfComponent(analysisWindow);
            }
            else{
                index =  cytoPanel.indexOfComponent(analysisWindow);
            }

            cytoPanel.setSelectedIndex(index);

        }

    }
}
