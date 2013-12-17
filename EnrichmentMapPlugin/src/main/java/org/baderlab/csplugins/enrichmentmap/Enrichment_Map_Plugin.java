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
import cytoscape.plugin.CytoscapePlugin;
import cytoscape.Cytoscape;
import cytoscape.CytoscapeInit;
import javax.swing.*;
import org.baderlab.csplugins.enrichmentmap.actions.LoadEnrichmentsPanelAction;
import org.baderlab.csplugins.enrichmentmap.actions.LoadPostAnalysisPanelAction;
import org.baderlab.csplugins.enrichmentmap.actions.ShowAboutPanelAction;
import org.baderlab.csplugins.enrichmentmap.commands.EnrichmentMapCommandHandler;
import java.io.File;
import java.util.*;


/*
 * Main Enrichment Map Class
 * 
 * **************************************** VERY IMPORTANT
 * When changes are made to this class need to also make them to Enrichment_Map_Plugin_GSEA
 * Had to duplicate this class in order to handle case of launching EM from GSEA when the user already has EM installed
 * (and the version potentially is an older version without the command interface)
 */
public class Enrichment_Map_Plugin extends CytoscapePlugin {
    
	EnrichmentMapUtils utils;
    /**
     * Constructor
     */
    public Enrichment_Map_Plugin(){

    		//initialize plugin properties
    		utils = new EnrichmentMapUtils("");
    	
        //set-up menu options in plugins menu
        JMenu menu = Cytoscape.getDesktop().getCyMenus().getOperationsMenu();
        JMenuItem item;

        //Enrichment map submenu
        JMenu submenu = new JMenu(utils.pluginName);

        //Enrichment map input  panel
        item = new JMenuItem("Load Enrichment Results");
        item.addActionListener(new LoadEnrichmentsPanelAction());
        submenu.add(item);

        //Post Analysis panel
        item = new JMenuItem("Post Analysis");
        item.addActionListener(new LoadPostAnalysisPanelAction());
        submenu.add(item);

        //TODO: Ruth's automatic Enrichment Map Annotaion.
//        item = new JMenuItem("Compute Potential Annotation");
//        item.addActionListener(new ComputeAnnotationAction());
//        submenu.add(item);

        	//Register CyCommand for enrichment maps.
        EnrichmentMapCommandHandler handlre = new EnrichmentMapCommandHandler(utils.pluginName);
       

        //About Box
        item = new JMenuItem("About");
        item.addActionListener(new ShowAboutPanelAction());
        submenu.add(item);

        menu.add(submenu);

        // add LinkOut for MSigDb GSEA gene sets
        Properties cyto_props = CytoscapeInit.getProperties();
        if ( ! cyto_props.containsKey("nodelinkouturl.MSigDb.GSEA Gene sets"))
            cyto_props.put("nodelinkouturl.MSigDb.GSEA Gene sets", "http://www.broad.mit.edu/gsea/msigdb/cards/%ID%.html");
        // remove old nodelinkouturl (for legacy issues)
        if (cyto_props.containsKey("nodelinkouturl.MSigDb"))
            cyto_props.remove("nodelinkouturl.MSigDb");
        
    }

    public void onCytoscapeExit(){

    }

    /**
     * SaveSessionStateFiles collects all the data stored in the Enrichment maps
     * and creates property files for each network listing the variables needed to rebuild the map.
     * All data(Hashmaps) collections needed for the Enrichment map are stored in separate files specified by the name
     * of the network with specific file endings to indicate what type of data is stored in the files (i.e. ENR for enrichment,
     * genes for genes...).
     *
     * @param pFileList - pointer to the set of files to be added to the session
     */
    public void saveSessionStateFiles(List<File> pFileList){
       utils.saveSessionStateFiles(pFileList);
    }

    /**
     * Restore Enrichment maps
     *
     * @param pStateFileList - list of files associated with thie session
     */
    public void restoreSessionState(List<File> pStateFileList) {
    		utils.restoreSessionState(pStateFileList);
    }

    

}