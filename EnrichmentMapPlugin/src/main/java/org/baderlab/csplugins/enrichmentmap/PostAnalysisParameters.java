/*
 *                       EnrichmentMap Cytoscape Plugin
 *
 * Copyright (c) 2008-2009 Bader Lab, Donnelly Centre for Cellular and Biomolecular
 * Research, University of Toronto
 *
 * Contact: http://www.baderlab.org
 *
 * Code written by: Ruth Isserlin
 * Authors: Daniele Merico, Ruth Isserlin, Oliver Stueker, Gary D. Bader
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 * documentation provided hereunder is on an "as is" basis, and
 * University of Toronto
 * has no obligations to provide maintenance, support, updates,
 * enhancements or modifications.  In no event shall the
 * University of Toronto
 * be liable to any party for direct, indirect, special,
 * incidental or consequential damages, including lost profits, arising
 * out of the use of this software and its documentation, even if
 * University of Toronto
 * has been advised of the possibility of such damage.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 *
 */

// $Id$
// $LastChangedDate$
// $LastChangedRevision$
// $LastChangedBy$
// $HeadURL$

package org.baderlab.csplugins.enrichmentmap;



import javax.swing.DefaultListModel;

import org.baderlab.csplugins.enrichmentmap.FilterParameters.FilterType;
import org.baderlab.csplugins.enrichmentmap.model.SetOfGeneSets;

public class PostAnalysisParameters {
    
    // Post Analysis Type:
    private boolean isSignatureDiscovery = false;
    private boolean isKnownSignature = true;

    private FilterParameters filterParameters = new FilterParameters();
    private FilterParameters rankTestParameters = new FilterParameters();
    
    final public static String SIGNATURE_INTERACTION_TYPE = "sig";
    final public static String SIGNATURE_INTERACTION_TYPE_SET1 = "sig_set1";
    final public static String SIGNATURE_INTERACTION_TYPE_SET2 = "sig_set2";
    
    
    // Disease Signature Parameters:
    private String signatureGMTFileName;
    
    // Disease Signature Data Structures:
    private SetOfGeneSets signatureGenesets;
    
    // There should not be setters for these, call the get method then use addElement() or clear()
    private DefaultListModel<String> signatureSetNames;
    private DefaultListModel<String> selectedSignatureSetNames;
    
    // Disease Signature State variables:
    private double currentNodePlacementY_Offset;
    
    // Rank file
    private String signature_rankFile;
    
    // Disease Signature data-set
    private String signature_dataSet;
    
    // Enrichment map universe
    private int universeSize;

    //attribute prefix associated with this map
  	private String attributePrefix = null;
  	

	/**
     * constructor to create a clean instance of PostAnalysisParameters
     * (used when using the Reset Button)
     */
    public PostAnalysisParameters() {
        // Disease Signature Parameters:
        this.signatureGMTFileName       = "";
        
        // Disease Signature Data Structures:
        this.signatureGenesets         = new SetOfGeneSets();
        this.signatureSetNames         = new DefaultListModel<>();
        this.selectedSignatureSetNames = new DefaultListModel<>();
        
        // Disease Signature State variables:
        this.currentNodePlacementY_Offset = 0.0;
        
        // Default for hypergeom in filter is 0.25
        filterParameters.setValue(FilterType.HYPERGEOM, 0.25);
    }
    

    /**
     * copies all attributes from another instance of PostAnalysisParameters
     */
    public PostAnalysisParameters(PostAnalysisParameters source) {
    	this();
        // Post Analysis Type:
        this.isSignatureDiscovery = source.isSignatureDiscovery();
        this.isKnownSignature = source.isKnownSignature();
        
        // Enrichment map data
        this.universeSize = source.getUniverseSize();

        // Disease Signature Parameters:
        this.signatureGMTFileName       = source.getSignatureGMTFileName();
        this.signature_dataSet = source.getSignature_dataSet();

        // Disease Signature Data Structures:
        this.signatureGenesets         = source.getSignatureGenesets();
        this.signatureSetNames         = source.getSignatureSetNames();
        this.selectedSignatureSetNames = source.getSelectedSignatureSetNames();

        // Disease Signature State variables:
        this.currentNodePlacementY_Offset = source.getCurrentNodePlacementY_Offset();

        this.attributePrefix = source.getAttributePrefix();
        
        this.filterParameters = new FilterParameters(source.getFilterParameters());
        this.rankTestParameters = new FilterParameters(source.getRankTestParameters());
    }
    
    /** 
     * Checks all values of the PostAnalysisInputPanel 
     * 
     * @return String with error messages (one error per line) or empty String if everything is okay.
     * @see org.baderlab.csplugins.enrichmentmap.EnrichmentMapParameters#checkMinimalRequirements()
     */
    public void checkMinimalRequirements(StringBuilder errors) {
        errors.append(checkGMTfiles());
        if(selectedSignatureSetNames.isEmpty())
        	errors.append("No Signature Genesets selected \n");
    }

    
    public FilterParameters getFilterParameters() {
    	return filterParameters;
    }
    
    public FilterParameters getRankTestParameters() {
    	return rankTestParameters;
    }
    
    /**
     * Checks if SignatureGMTFileName is provided and if the file can be read.
     * 
     * @return String with error messages (one error per line) or empty String if everything is okay.
     */
    public String checkGMTfiles() {
        String signatureGMTFileName = getSignatureGMTFileName();
		if(signatureGMTFileName == null || signatureGMTFileName.isEmpty() || !EnrichmentMapParameters.checkFile(signatureGMTFileName))
            return "Signature GMT file can not be found \n";
        return "";
    }

    
    // ***************************************
    // getters and setters
    // ***************************************

    /**
     * @param signatureGMTFileName the signatureGMTFileName to set
     */
    public void setSignatureGMTFileName(String signatureGMTFileName) {
        this.signatureGMTFileName = signatureGMTFileName;
    }

    /**
     * @return the signatureGMTFileName
     */
    public String getSignatureGMTFileName() {
        return signatureGMTFileName;
    }

    /**
     * Set post-analysis type (Signature Discovery)
     * @param boolean isSignatureDiscovery
     * @return null 
     */
    public void setSignatureHub(boolean isSignatureDiscovery) {
        this.isSignatureDiscovery = isSignatureDiscovery;
    	if (this.isSignatureDiscovery) {
            this.isKnownSignature = false;
    	} else{
    		this.isKnownSignature = true;
    	}
    }


    /**
     * True iff Signature Discovery panel has been requested
     * @param null
     * @return boolean isSignatureDiscovery
     */
    public boolean isSignatureDiscovery() {
        return isSignatureDiscovery;
    }

    /**
     * @param signatureGenesets the signatureGenesets to set
     */
    public void setSignatureGenesets(SetOfGeneSets signatureGenesets) {
        this.signatureGenesets = signatureGenesets;
    }

    /**
     * @return the signatureGenesets
     */
    public SetOfGeneSets getSignatureGenesets() {
        return signatureGenesets;
    }

    /**
     * @return the signatureSetNames
     */
    public DefaultListModel<String> getSignatureSetNames() {
        return signatureSetNames;
    }

    /**
     * @return the selectedSignatureSetNames
     */
    public DefaultListModel<String> getSelectedSignatureSetNames() {
        return selectedSignatureSetNames;
    }

    /**
     * @param currentNodePlacementY_Offset the currentNodePlacementY_Offset to set
     */
    public void setCurrentNodePlacementY_Offset(double currentNodePlacementY_Offset) {
        this.currentNodePlacementY_Offset = currentNodePlacementY_Offset;
    }

    /**
     * @return the currentNodePlacementY_Offset
     */
    public double getCurrentNodePlacementY_Offset() {
        return currentNodePlacementY_Offset;
    }


	/**
	 * Get signature rank file
	 * @param null
	 * @return String signature_rankFile
	 */
	public String getSignature_rankFile() {
		return signature_rankFile;
	}

	/**
	 * Set signature rank file
	 * @param String signature_rankFile 
	 * @return null
	 */
	public void setSignature_rankFile(String signature_rankFile) {
		this.signature_rankFile = signature_rankFile;
	}

	/**
	 * Get signature data set
	 * @param null
	 * @return String signature_dataSet
	 */
	public String getSignature_dataSet() {
		return signature_dataSet;
	}

	/**
	 * Set signature data set
	 * @param String signature_dataSet
	 * @return null 
	 */
	public void setSignature_dataSet(String signature_dataSet) {
		this.signature_dataSet = signature_dataSet;
	}

	/**
	 * True iff KnownSignature Panel has been requested
	 * @param null
	 * @return boolean isKnownSignature
	 */
	public boolean isKnownSignature() {
		return isKnownSignature;
	}

	/**
	 * Set post-analysis type (KnownSignature)
	 * @param boolean isKnownSignature 
	 * @return null
	 */
	public void setKnownSignature(boolean isKnownSignature) {
		this.isKnownSignature = isKnownSignature;
	}

	/**
	 * @return the universeSize
	 */
	public int getUniverseSize() {
		return universeSize;
	}

	/**
	 * @param universeSize the universeSize to set
	 */
	public void setUniverseSize(int universeSize) {
		this.universeSize = universeSize;
	}
	
	
	public String getAttributePrefix() {
		return attributePrefix;
	}


	public void setAttributePrefix(String attributePrefix) {
		this.attributePrefix = attributePrefix;
	}
}
