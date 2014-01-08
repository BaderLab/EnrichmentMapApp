package org.baderlab.csplugins.enrichmentmap.gsea;

import org.baderlab.csplugins.enrichmentmap.EnrichmentMapUtils;

import java.io.File;
import java.util.*;

/*
 * Main Enrichment Map Class for the gsea specific em plugin
 * Used by GSEA to launch and create EM directly from GSEA
 * gets name from plugin_gsea.props and commands are associated with different name (EnrichmentMap_gsea)
 * 
 * **************************************** VERY IMPORTANT
 * When changes are made to this class need to also make them to Enrichment_Map_Plugin_GSEA
 * Had to duplicate this class in order to handle case of launching EM from GSEA when the user already has EM installed
 * (and the version potentially is an older version without the command interface)
 */

public class Enrichment_Map_Plugin_GSEA {
    
	EnrichmentMapUtils utils;
	
	public Enrichment_Map_Plugin_GSEA(){
		
		//initialize plugin properties
		utils = new EnrichmentMapUtils("gsea");
	}   

}


