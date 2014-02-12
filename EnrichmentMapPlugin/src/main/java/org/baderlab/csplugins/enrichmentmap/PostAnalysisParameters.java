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

import java.util.HashMap;

import javax.swing.DefaultListModel;

import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.model.GeneSet;
import org.baderlab.csplugins.enrichmentmap.model.SetOfGeneSets;

/**
 * @author revilo
 * <p>
 * Date   Jul 9, 2009<br>
 * Time   6:01:53 PM<br>
 *
 */
public class PostAnalysisParameters extends EnrichmentMapParameters {
    
    // Post Analysis Type:
    private boolean isSignatureDiscovery;
    private boolean isKnownSignature;

    // Disease Signature Constants
    /**
     * Enum for Signature-Hub cut-off metric:
     * HYPERGEOM   = 0
     * ABS_NUMBER  = 1
     * JACCARD     = 2
     * OVERLAP     = 3
     * DIR_OVERLAP = 4
     */
    final public static int ABS_NUMBER = 0, JACCARD = 1, OVERLAP = 2, DIR_OVERLAP = 3; 

    //Gene Set Filtering Constants
    /**
     * Enum for Signature geneset filtering
     * Hypergeometric Test = 0
     * Percent overlap (percent of Enrichment Map geneset)  = 1
     * Number in overlap = 2
     * Percent overlap (percent of Signature geneset) = 3
     */
    final public static int HYPERGEOM = 0, MANN_WHIT = 1, PERCENT = 2, NUMBER = 3, SPECIFIC = 4;

    /**
     * String for Filtering options
     * HYPERGEOM (0): "Passed the Hypergeometric test at the desired cut-off"
     * MANN_WHIT (1): "Passed the Mann-Whitney-U rank sum test at the desired cut-off"
     * PERCENT (2) : "Contains at least X percent (% of EM geneset)"
     * NUMBER (3) : "Contains at least X genes"
     * PERCENT (4): "Contains at least X percent (% of Signature geneset)"
     */
      final public static String[] filterItems = {"Hypergeometric Test", "Mann-Whitney", "Overlap X percent of EM gs", "Overlap has at least X genes", "Overlap X percent of Signature gs"};
    /**
     * Strings for Signature-Hub cut-off metric:
     * ABS_NUMBER   (0) : "Number of common genes"
     * JACCARD      (1) : "Jaccard Coefficient"
     * OVERLAP      (2) : "Overlap Coefficient"
     * DIR_OVERLAP  (3) : "Directed Overlap"
     */
    final public static String[] sigCutoffItems = {"Number of Common Genes",
                                            "Jaccard Coefficient", 
                                            "Overlap Coefficient",
                                            "Directed Overlap",
                                            };
    
    final public static String SIGNATURE_INTERACTION_TYPE = "sig";
    
    private String signatureHub_nodeShape   = "TRIANGLE";
    private String signatureHub_nodeColor   = "255,255,0"; // yellow
    private String signatureHub_borderColor = "255,255,0"; // yellow
    private String signatureHub_edgeColor   = "255,0,200"; // pink
    
    
    // Disease Signature Parameters:
    private String signatureGMTFileName;
    
    private int    signature_absNumber_Cutoff;
    private double signature_Jaccard_Cutoff;
    private double signature_Overlap_Cutoff;
    private double signature_DirOverlap_Cutoff;
    private double signature_Hypergeom_Cutoff;   
    private double signature_Mann_Whit_Cutoff;

    private int    signature_CutoffMetric;
    
    // Disease Signature default Values:
    private int    default_signature_absNumber_Cutoff = 5;
    private double default_signature_Jaccard_Cutoff   = 0.125;  
    private double default_signature_Overlap_Cutoff   = 0.25;  
    private double default_signature_DirOverlap_Cutoff= 0.25;  
    private double default_signature_Hypergeom_Cutoff = 0.25;
    
    private int    default_signature_CutoffMetric = ABS_NUMBER;
    
    // Disease Signature Data Structures:
    private SetOfGeneSets signatureGenesets;
    private DefaultListModel signatureSetNames;
    private DefaultListModel selectedSignatureSetNames;
    
    // Disease Signature State variables:
    private double currentNodePlacementY_Offset;

    //Disease Signature filter
    private boolean filter = false;
    private int filterValue;
    private int default_filter_value = 50;
    private int default_signature_filterMetric = HYPERGEOM;
    private int signature_filterMetric;
    
    //Disease Signature rank test
    private int default_signature_rankTest = MANN_WHIT;
    private int signature_rankTest = default_signature_rankTest;
    private double default_signature_Mann_Whit_Cutoff = 0.05;
    
    // Rank file
    private String signature_rankFile;
    
    // Disease Signature data-set
    private String signature_dataSet;
    
    // Enrichment map universe
    private int universeSize;

    /**
     * default constructor
     * 
     * @param emParams instance of EnrichmentMapParameters with parameters of the current EnrichmentMap
     */
    public PostAnalysisParameters(EnrichmentMap map) {
        // EnrichmentMapParameters
    	super.copy(map.getParams());

        // Post Analysis Type:
        this.isSignatureDiscovery = true;
    	
    	// Disease Signature Parameters:
        this.signatureGMTFileName       = "";
        this.signature_absNumber_Cutoff = default_signature_absNumber_Cutoff;
        this.signature_Jaccard_Cutoff   = default_signature_Jaccard_Cutoff;
        this.signature_Overlap_Cutoff   = default_signature_Overlap_Cutoff;
        this.signature_DirOverlap_Cutoff= default_signature_DirOverlap_Cutoff;
        this.signature_Hypergeom_Cutoff = default_signature_Hypergeom_Cutoff;
        this.signature_CutoffMetric     = default_signature_CutoffMetric;
        this.signature_Mann_Whit_Cutoff = default_signature_Mann_Whit_Cutoff;
        
        // Disease Signature Data Structures:
        this.signatureGenesets         = new SetOfGeneSets();
        this.signatureSetNames         = new DefaultListModel();
        this.selectedSignatureSetNames = new DefaultListModel();
        
//        this.setGenesetSimilarity( emParams.getGenesetSimilarity() );
        
        // Disease Signature State variables:
        this.currentNodePlacementY_Offset = 0.0;

        //set the default filter
        this.filterValue = default_filter_value;

        this.signature_filterMetric = default_signature_filterMetric;
        
        // register this instance in emParams
        map.setPaParams(this);
    }

    /**
     * constructor to create a clean instance of PostAnalysisParameters
     * (used when using the Reset Button)
     */
    public PostAnalysisParameters() {
        // EnrichmentMapParameters
        super();

        // Post Analysis Type:
        this.isSignatureDiscovery = true;
        this.isKnownSignature = false;
        
        // Disease Signature Parameters:
        this.signatureGMTFileName       = "";
        this.signature_absNumber_Cutoff = default_signature_absNumber_Cutoff;
        this.signature_Jaccard_Cutoff   = default_signature_Jaccard_Cutoff;
        this.signature_Overlap_Cutoff   = default_signature_Overlap_Cutoff;
        this.signature_DirOverlap_Cutoff= default_signature_DirOverlap_Cutoff;
        this.signature_Hypergeom_Cutoff = default_signature_Hypergeom_Cutoff;
        this.signature_CutoffMetric     = default_signature_CutoffMetric;
        this.signature_Mann_Whit_Cutoff = default_signature_Mann_Whit_Cutoff;
        
        // Disease Signature Data Structures:
        this.signatureGenesets         = new SetOfGeneSets();
        this.signatureSetNames         = new DefaultListModel();
        this.selectedSignatureSetNames = new DefaultListModel();
        
        // Disease Signature State variables:
        this.currentNodePlacementY_Offset = 0.0;

        //set the default filter
        this.filterValue = default_filter_value;

        this.signature_filterMetric = default_signature_filterMetric;
    }
    

    /**
     * copies all attributes from another instance of PostAnalysisParameters
     * 
     * @param source  the original instance of PostAnalysisParameters
     */
    public void copyFrom(PostAnalysisParameters source) {
        super.copy(source);
        
        // Post Analysis Type:
        this.isSignatureDiscovery = source.isSignatureDiscovery();
        
        // Enrichment map data
        this.universeSize = source.getUniverseSize();

        // Disease Signature Parameters:
        this.signatureGMTFileName       = source.getSignatureGMTFileName();
        this.signature_absNumber_Cutoff = source.getSignature_absNumber_Cutoff();
        this.signature_Jaccard_Cutoff   = source.getSignature_Jaccard_Cutoff();
        this.signature_Overlap_Cutoff   = source.getSignature_Overlap_Cutoff();
        this.signature_DirOverlap_Cutoff= source.getSignature_DirOverlap_Cutoff();
        this.signature_Hypergeom_Cutoff = source.getSignature_Hypergeom_Cutoff();
        this.signature_Mann_Whit_Cutoff = source.getSignature_Mann_Whit_Cutoff();
        this.signature_CutoffMetric     = source.getSignature_CutoffMetric();
        this.signature_rankTest = source.getSignature_rankTest();

        // Disease Signature Data Structures:
        this.signatureGenesets         = source.getSignatureGenesets();
        this.signatureSetNames         = source.getSignatureSetNames();
        this.selectedSignatureSetNames = source.getSelectedSignatureSetNames();

        // Disease Signature State variables:
        this.currentNodePlacementY_Offset = source.getCurrentNodePlacementY_Offset();

        this.filterValue = source.getFilterValue();
    }
    
    /** 
     * Checks all values of the PostAnalysisInputPanel 
     * 
     * @return String with error messages (one error per line) or empty String if everything is okay.
     * @see org.baderlab.csplugins.enrichmentmap.EnrichmentMapParameters#checkMinimalRequirements()
     */
    public String checkMinimalRequirements() {
        String errors = "";
        errors += checkGMTfiles();
        if(this.selectedSignatureSetNames.isEmpty())
            errors += "No Signature Genesets selected \n";
        if (this.signature_CutoffMetric == PostAnalysisParameters.ABS_NUMBER) {
            if (! (this.signature_absNumber_Cutoff > 0))
                errors += "Number of Genes Cutoff must be a positive, non-zero integer \n";
        } else if (this.signature_CutoffMetric == PostAnalysisParameters.OVERLAP) {
            if (! (this.signature_Overlap_Cutoff >= 0.0 && this.signature_Overlap_Cutoff <= 1.0))
                errors += "Overlap Cutoff must be a decimal Number between 0.0 and 1.0 \n";
        } else if (this.signature_CutoffMetric == PostAnalysisParameters.JACCARD) {
            if (! (this.signature_Jaccard_Cutoff >= 0.0 && this.signature_Jaccard_Cutoff <= 1.0))
                errors += "Jaccard Cutoff must be a decimal Number between 0.0 and 1.0 \n";
        } else if (this.signature_CutoffMetric == PostAnalysisParameters.DIR_OVERLAP) {
            if (! (this.signature_DirOverlap_Cutoff >= 0.0 && this.signature_DirOverlap_Cutoff <= 1.0))
                errors += "Directed Overlap Cutoff must be a decimal Number between 0.0 and 1.0 \n";
        } else
            errors += "Invalid Cutoff metric \n";
        
        return errors;
    }

    /**
     * Checks if GMTFileName and SignatureGMTFileName are provided and if the files can be read.
     * 
     * @return String with error messages (one error per line) or empty String if everything is okay.
     */
    public String checkGMTfiles() {
        String errors = "";
        if(this.getGMTFileName().equalsIgnoreCase("") || ! checkFile(this.getGMTFileName()))
            errors += "GMT file can not be found \n";
        if(this.getSignatureGMTFileName() .equalsIgnoreCase("") || ! checkFile(this.getSignatureGMTFileName()))
            errors += "Signature GMT file can not be found \n";
        return errors;
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
     * @param signature_absNumber_Cutoff the signature_absNumber_Cutoff to set
     */
    public void setSignature_absNumber_Cutoff(int signature_absNumber_Cutoff) {
        this.signature_absNumber_Cutoff = signature_absNumber_Cutoff;
    }

    /**
     * @return the signature_absNumber_Cutoff
     */
    public int getSignature_absNumber_Cutoff() {
        return signature_absNumber_Cutoff;
    }

    /**
     * @param signature_Jaccard_Cutoff the signature_Jaccard_Cutoff to set
     */
    public void setSignature_Jaccard_Cutoff(double signature_Jaccard_Cutoff) {
        this.signature_Jaccard_Cutoff = signature_Jaccard_Cutoff;
    }

    /**
     * @return the signature_Jaccard_Cutoff
     */
    public double getSignature_Jaccard_Cutoff() {
        return signature_Jaccard_Cutoff;
    }

    /**
     * @param signature_Overlap_Cutoff the signature_Overlap_Cutoff to set
     */
    public void setSignature_Overlap_Cutoff(double signature_Overlap_Cutoff) {
        this.signature_Overlap_Cutoff = signature_Overlap_Cutoff;
    }

    /**
     * @return the signature_Overlap_Cutoff
     */
    public double getSignature_Overlap_Cutoff() {
        return signature_Overlap_Cutoff;
    }

    /**
     * @param signature_DirOverlap_Cutoff the signature_DirOverlap_Cutoff to set
     */
    public void setSignature_DirOverlap_Cutoff(double signature_DirOverlap_Cutoff) {
        this.signature_DirOverlap_Cutoff = signature_DirOverlap_Cutoff;
    }

    /**
     * @return the signature_DirOverlap_Cutoff
     */
    public double getSignature_DirOverlap_Cutoff() {
        return signature_DirOverlap_Cutoff;
    }

    /**
     * @param signature_Hypergeom_Cutoff the signature_Hypergeom_Cutoff to set
     */
    public void setSignature_Hypergeom_Cutoff(double signature_Hypergeom_Cutoff) {
        this.signature_Hypergeom_Cutoff = signature_Hypergeom_Cutoff;
    }

    /**
     * @return the signature_Hypergeom_Cutoff
     */
    public double getSignature_Hypergeom_Cutoff() {
        return signature_Hypergeom_Cutoff;
    }

    /**
     * @param signature_CutoffMetric the signature_CutoffMetric to set
     */
    public void setSignature_CutoffMetric(int signature_CutoffMetric) {
        this.signature_CutoffMetric = signature_CutoffMetric;
    }

    /**
     * @return the signature_CutoffMetric
     */
    public int getSignature_CutoffMetric() {
        return signature_CutoffMetric;
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
     * @param defaultSignatureAbsNumberCutoff the default_signature_absNumber_Cutoff to set
     */
    public void setDefault_signature_absNumber_Cutoff(
            int defaultSignatureAbsNumberCutoff) {
        default_signature_absNumber_Cutoff = defaultSignatureAbsNumberCutoff;
    }

    /**
     * @return the default_signature_absNumber_Cutoff
     */
    public int getDefault_signature_absNumber_Cutoff() {
        return default_signature_absNumber_Cutoff;
    }

    /**
     * @param defaultSignatureJaccardCutoff the default_signature_Jaccard_Cutoff to set
     */
    public void setDefault_signature_Jaccard_Cutoff(
            double defaultSignatureJaccardCutoff) {
        default_signature_Jaccard_Cutoff = defaultSignatureJaccardCutoff;
    }

    /**
     * @return the default_signature_Jaccard_Cutoff
     */
    public double getDefault_signature_Jaccard_Cutoff() {
        return default_signature_Jaccard_Cutoff;
    }

    /**
     * @param defaultSignatureOverlapCutoff the default_signature_Overlap_Cutoff to set
     */
    public void setDefault_signature_Overlap_Cutoff(
            double defaultSignatureOverlapCutoff) {
        default_signature_Overlap_Cutoff = defaultSignatureOverlapCutoff;
    }

    /**
     * @return the default_signature_Overlap_Cutoff
     */
    public double getDefault_signature_Overlap_Cutoff() {
        return default_signature_Overlap_Cutoff;
    }

    /**
     * @param defaultSignatureHypergeomCutoff the default_signature_Hypergeom_Cutoff to set
     */
    public void setDefault_signature_Hypergeom_Cutoff(
            double defaultSignatureHypergeomCutoff) {
        default_signature_Hypergeom_Cutoff = defaultSignatureHypergeomCutoff;
    }

    /**
     * @return the default_signature_Hypergeom_Cutoff
     */
    public double getDefault_signature_Hypergeom_Cutoff() {
        return default_signature_Hypergeom_Cutoff;
    }

    /**
     * @param default_signature_DirOverlap_Cutoff the default_signature_DirOverlap_Cutoff to set
     */
    public void setDefault_signature_DirOverlap_Cutoff(
            double default_signature_DirOverlap_Cutoff) {
        this.default_signature_DirOverlap_Cutoff = default_signature_DirOverlap_Cutoff;
    }

    /**
     * @return the default_signature_DirOverlap_Cutoff
     */
    public double getDefault_signature_DirOverlap_Cutoff() {
        return default_signature_DirOverlap_Cutoff;
    }

    /**
     * @param defaultSignatureCutoffMetric the default_signature_CutoffMetric to set
     */
    public void setDefault_signature_CutoffMetric(int defaultSignatureCutoffMetric) {
        default_signature_CutoffMetric = defaultSignatureCutoffMetric;
    }

    /**
     * @return the default_signature_CutoffMetric
     */
    public int getDefault_signature_CutoffMetric() {
        return default_signature_CutoffMetric;
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
     * @param signatureSetNames the signatureSetNames to set
     */
    public void setSignatureSetNames(DefaultListModel signatureSetNames) {
        this.signatureSetNames = signatureSetNames;
    }

    /**
     * @return the signatureSetNames
     */
    public DefaultListModel getSignatureSetNames() {
        return signatureSetNames;
    }

    /**
     * @param selectedSignatureSetNames the selectedSignatureSetNames to set
     */
    public void setSelectedSignatureSetNames(DefaultListModel selectedSignatureSetNames) {
        this.selectedSignatureSetNames = selectedSignatureSetNames;
    }

    /**
     * @return the selectedSignatureSetNames
     */
    public DefaultListModel getSelectedSignatureSetNames() {
        return selectedSignatureSetNames;
    }

    /**
     * @param signatureHub_nodeShape the signatureHub_nodeShape to set
     */
    public void setSignatureHub_nodeShape(String signatureHub_nodeShape) {
        this.signatureHub_nodeShape = signatureHub_nodeShape;
    }

    /**
     * @return the signatureHub_nodeShape
     */
    public String getSignatureHub_nodeShape() {
        return signatureHub_nodeShape;
    }

    /**
     * @param signatureHub_nodeColor the signatureHub_nodeColor to set
     */
    public void setSignatureHub_nodeColor(String signatureHub_nodeColor) {
        this.signatureHub_nodeColor = signatureHub_nodeColor;
    }

    /**
     * @return the signatureHub_nodeColor
     */
    public String getSignatureHub_nodeColor() {
        return signatureHub_nodeColor;
    }

    /**
     * @param signatureHub_borderColor the signatureHub_borderColor to set
     */
    public void setSignatureHub_borderColor(String signatureHub_borderColor) {
        this.signatureHub_borderColor = signatureHub_borderColor;
    }

    /**
     * @return the signatureHub_borderColor
     */
    public String getSignatureHub_borderColor() {
        return signatureHub_borderColor;
    }

    /**
     * @param signatureHub_edgeColor the signatureHub_edgeColor to set
     */
    public void setSignatureHub_edgeColor(String signatureHub_edgeColor) {
        this.signatureHub_edgeColor = signatureHub_edgeColor;
    }

    /**
     * @return the signatureHub_edgeColor
     */
    public String getSignatureHub_edgeColor() {
        return signatureHub_edgeColor;
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

    public boolean isFilter() {
        return filter;
    }

    public void setFilter(boolean filter) {
        this.filter = filter;
    }

    public int getFilterValue() {
        return filterValue;
    }

    public void setFilterValue(int filterValue) {
        this.filterValue = filterValue;
    }

    public int getSignature_filterMetric() {
        return signature_filterMetric;
    }

    public void setSignature_filterMetric(int signature_filterMetric) {
        this.signature_filterMetric = signature_filterMetric;
    }

    public int getDefault_signature_filterMetric() {
        return default_signature_filterMetric;
    }

	/**
	 * @return the default_signature_Mann_Whit_Cutoff
	 */
	public double getDefault_signature_Mann_Whit_Cutoff() {
		return default_signature_Mann_Whit_Cutoff;
	}

	/**
	 * @param default_signature_Mann_Whit_Cutoff the default_signature_Mann_Whit_Cutoff to set
	 */
	public void setDefault_signature_Mann_Whit_Cutoff(
			double default_signature_Mann_Whit_Cutoff) {
		this.default_signature_Mann_Whit_Cutoff = default_signature_Mann_Whit_Cutoff;
	}

	/**
	 * @return the signature_Mann_Whit_Cutoff
	 */
	public double getSignature_Mann_Whit_Cutoff() {
		return signature_Mann_Whit_Cutoff;
	}

	/**
	 * @param signature_Mann_Whit_Cutoff the signature_Mann_Whit_Cutoff to set
	 */
	public void setSignature_Mann_Whit_Cutoff(double signature_Mann_Whit_Cutoff) {
		this.signature_Mann_Whit_Cutoff = signature_Mann_Whit_Cutoff;
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
	 * @return the signature_rankTest
	 */
	public int getSignature_rankTest() {
		return signature_rankTest;
	}

	/**
	 * @param signature_rankTest the signature_rankTest to set
	 */
	public void setSignature_rankTest(int signature_rankTest) {
		this.signature_rankTest = signature_rankTest;
	}

	/**
	 * @return the default_signature_rankTest
	 */
	public int getDefault_signature_rankTest() {
		return default_signature_rankTest;
	}

	/**
	 * @param default_signature_rankTest the default_signature_rankTest to set
	 */
	public void setDefault_signature_rankTest(int default_signature_rankTest) {
		this.default_signature_rankTest = default_signature_rankTest;
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
}
