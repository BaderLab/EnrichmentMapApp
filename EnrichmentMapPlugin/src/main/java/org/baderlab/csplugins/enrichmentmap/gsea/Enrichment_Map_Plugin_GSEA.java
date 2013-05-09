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


