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

import cytoscape.Cytoscape;
import cytoscape.view.CytoscapeDesktop;
import cytoscape.view.cytopanels.CytoPanel;
import cytoscape.util.CytoscapeAction;

import javax.swing.*;

import org.baderlab.csplugins.enrichmentmap.EnrichmentMapManager;
import org.baderlab.csplugins.enrichmentmap.Enrichment_Map_Plugin;
import org.baderlab.csplugins.enrichmentmap.view.EnrichmentMapInputPanel;

import java.awt.event.ActionEvent;
import java.net.URL;

/**
 * Created by
 * User: risserlin
 * Date: Jan 28, 2009
 * Time: 12:30:13 PM
 *
 * Click on Build Enrichment Map Action handler
 */
public class LoadEnrichmentsPanelAction extends CytoscapeAction {

    //variable to track initialization of network event listener
    private boolean initialized = false;

    public LoadEnrichmentsPanelAction(){
         super("Load GSEA Files");
    }

    public void actionPerformed(ActionEvent event) {

          int index = 0;

          String os = System.getProperty("os.name");

          CytoscapeDesktop desktop = Cytoscape.getDesktop();
          CytoPanel cytoPanel = desktop.getCytoPanel(SwingConstants.WEST);

          if(!initialized){
                EnrichmentMapManager.getInstance();
                initialized = true;

                EnrichmentMapInputPanel inputwindow = new EnrichmentMapInputPanel();

                //set the input window in the instance so we can udate the instance window
                //on network focus
                EnrichmentMapManager.getInstance().setInputWindow(inputwindow);

               //create an icon for the enrichment map panels
                URL EMIconURL = Enrichment_Map_Plugin.class.getResource("resources/enrichmentmap_logo_notext_small.png");
                ImageIcon EMIcon = null;
                if (EMIconURL != null) {
                    EMIcon = new ImageIcon(EMIconURL);
                }

                if(EMIcon != null)
                    cytoPanel.add("Enrichment Map",EMIcon, inputwindow);
                else
                     cytoPanel.add("Enrichment Map", inputwindow);
                index =  cytoPanel.indexOfComponent(inputwindow);

                cytoPanel.setSelectedIndex(index);
          }
          else{

                //check to see that the input window hasn't been closed

                EnrichmentMapInputPanel inputwindow  = EnrichmentMapManager.getInstance().getInputWindow();
                if(inputwindow == null){
                  inputwindow = new EnrichmentMapInputPanel();
                  EnrichmentMapManager.getInstance().setInputWindow(inputwindow);

                    //create an icon for the enrichment map panels
                    URL EMIconURL = Enrichment_Map_Plugin.class.getResource("resources/enrichmentmap_logo_notext_small.png");
                    ImageIcon EMIcon = null;
                    if (EMIconURL != null) {
                        EMIcon = new ImageIcon(EMIconURL);
                    }

                    if(EMIcon != null)
                        cytoPanel.add("Enrichment Map",EMIcon, inputwindow);
                    else
                        cytoPanel.add("Enrichment Map", inputwindow);

                  index =  cytoPanel.indexOfComponent(inputwindow);
                }
                else{
                   index =  cytoPanel.indexOfComponent(inputwindow);
                }

                cytoPanel.setSelectedIndex(index);

          }

        }
}
