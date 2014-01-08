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
import javax.swing.*;
import org.baderlab.csplugins.enrichmentmap.actions.LoadEnrichmentsPanelAction;
//import org.baderlab.csplugins.enrichmentmap.actions.LoadPostAnalysisPanelAction;
import org.baderlab.csplugins.enrichmentmap.actions.ShowAboutPanelAction;
import org.baderlab.csplugins.enrichmentmap.commands.EnrichmentMapCommandHandler;
import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.application.swing.CytoPanel;

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
public class Enrichment_Map_Plugin {
    	
	EnrichmentMapUtils utils;
    /**
     * Constructor
     */
    public Enrichment_Map_Plugin(){
    	    	
    		//initialize plugin properties
    		utils = new EnrichmentMapUtils("");
    	       
        	//Register CyCommand for enrichment maps.
        //EnrichmentMapCommandHandler handlre = new EnrichmentMapCommandHandler(utils.pluginName);
       
        // TODO: add LinkOut for MSigDb GSEA gene sets
  /*      Properties cyto_props = CytoscapeInit.getProperties();
        if ( ! cyto_props.containsKey("nodelinkouturl.MSigDb.GSEA Gene sets"))
            cyto_props.put("nodelinkouturl.MSigDb.GSEA Gene sets", "http://www.broad.mit.edu/gsea/msigdb/cards/%ID%.html");
        // remove old nodelinkouturl (for legacy issues)
        if (cyto_props.containsKey("nodelinkouturl.MSigDb"))
            cyto_props.remove("nodelinkouturl.MSigDb");
    */    
    }

    public void onCytoscapeExit(){

    }
  

}