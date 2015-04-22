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

import org.baderlab.csplugins.enrichmentmap.model.SetOfGeneSets;

/**
 * @author revilo
 * <p>
 * Date   Jul 9, 2009<br>
 * Time   6:01:53 PM<br>
 *
 */
public class PostAnalysisParameters {
    
    // Post Analysis Type:
    private boolean isSignatureDiscovery = false;
    private boolean isKnownSignature = true;

    // Disease Signature Constants
    /**
     * Enum for Signature-Hub cut-off metric:
     */
    public enum CutoffMetric { 
    	ABS_NUMBER("Number of Common Genes"), 
    	JACCARD("Jaccard Coefficient"), 
    	OVERLAP("Overlap Coefficient"), 
    	DIR_OVERLAP("Directed Overlap");
    	
    	public final String display;
    	CutoffMetric(String display) { this.display = display; }
    }

    //Gene Set Filtering Constants
    /**
     * Enum for Signature geneset filtering
     * Hypergeometric Test = 0
     * Percent overlap (percent of Enrichment Map geneset)  = 1
     * Number in overlap = 2
     * Percent overlap (percent of Signature geneset) = 3
     */
    public enum FilterMetric { 
    	HYPERGEOM("Hypergeometric Test"), 
    	MANN_WHIT("Mann-Whitney"), 
    	PERCENT("Overlap X percent of EM gs"), 
    	NUMBER("Overlap has at least X genes"), 
    	SPECIFIC("Overlap X percent of Signature gs");
    	
    	public final String display;
    	FilterMetric(String display) { this.display = display; }
    }
    
    final public static String SIGNATURE_INTERACTION_TYPE = "sig";
    
    
    // Disease Signature Parameters:
    private String signatureGMTFileName;
    
    private int    signature_absNumber_Cutoff;
    private double signature_Jaccard_Cutoff;
    private double signature_Overlap_Cutoff;
    private double signature_DirOverlap_Cutoff;
    private double signature_Hypergeom_Cutoff;   
    private double signature_Mann_Whit_Cutoff;

    private CutoffMetric signature_CutoffMetric;
    
    // Disease Signature default Values:
    private int    default_signature_absNumber_Cutoff = 5;
    private double default_signature_Jaccard_Cutoff   = 0.125;  
    private double default_signature_Overlap_Cutoff   = 0.25;  
    private double default_signature_DirOverlap_Cutoff= 0.25;  
    private double default_signature_Hypergeom_Cutoff = 0.05;
    
    private CutoffMetric default_signature_CutoffMetric = CutoffMetric.ABS_NUMBER;
    
    // Disease Signature Data Structures:
    private SetOfGeneSets signatureGenesets;
    
    // There should not be setters for these, call the get method then use addElement() or clear()
    private DefaultListModel<String> signatureSetNames;
    private DefaultListModel<String> selectedSignatureSetNames;
    
    // Disease Signature State variables:
    private double currentNodePlacementY_Offset;

    //Disease Signature filter
    private boolean filter = false;
    private int filterValue;
    private int default_filter_value = 50;
    private FilterMetric default_signature_filterMetric = FilterMetric.HYPERGEOM;
    private FilterMetric signature_filterMetric;
    
    //Disease Signature rank test
    private FilterMetric default_signature_rankTest = FilterMetric.MANN_WHIT;
    private FilterMetric signature_rankTest = default_signature_rankTest;
    private double default_signature_Mann_Whit_Cutoff = 0.05;
    
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
        this.signature_absNumber_Cutoff = default_signature_absNumber_Cutoff;
        this.signature_Jaccard_Cutoff   = default_signature_Jaccard_Cutoff;
        this.signature_Overlap_Cutoff   = default_signature_Overlap_Cutoff;
        this.signature_DirOverlap_Cutoff= default_signature_DirOverlap_Cutoff;
        this.signature_Hypergeom_Cutoff = default_signature_Hypergeom_Cutoff;
        this.signature_CutoffMetric     = default_signature_CutoffMetric;
        this.signature_Mann_Whit_Cutoff = default_signature_Mann_Whit_Cutoff;
        
        // Disease Signature Data Structures:
        this.signatureGenesets         = new SetOfGeneSets();
        this.signatureSetNames         = new DefaultListModel<>();
        this.selectedSignatureSetNames = new DefaultListModel<>();
        
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
        // Post Analysis Type:
        this.isSignatureDiscovery = source.isSignatureDiscovery();
        this.isKnownSignature = source.isKnownSignature();
        
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
        this.signature_dataSet = source.getSignature_dataSet();

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
        StringBuilder errors = new StringBuilder();
        
        errors.append(checkGMTfiles());
        
        if(selectedSignatureSetNames.isEmpty())
        	errors.append("No Signature Genesets selected \n");
        
        switch(signature_CutoffMetric) {
        	case ABS_NUMBER:
	        	if(signature_absNumber_Cutoff <= 0)
	        		errors.append("Number of Genes Cutoff must be a positive, non-zero integer \n");
	        	break;
        	case OVERLAP:
        		if(signature_Overlap_Cutoff < 0.0 || signature_Overlap_Cutoff > 1.0)
                	errors.append("Overlap Cutoff must be a decimal Number between 0.0 and 1.0 \n");
        		break;
        	case JACCARD:
        		if(signature_Jaccard_Cutoff < 0.0 || signature_Jaccard_Cutoff > 1.0)
                	errors.append("Jaccard Cutoff must be a decimal Number between 0.0 and 1.0 \n");
        		break;
        	case DIR_OVERLAP:
        		if(signature_DirOverlap_Cutoff < 0.0 || signature_DirOverlap_Cutoff > 1.0)
                	errors.append("Directed Overlap Cutoff must be a decimal Number between 0.0 and 1.0 \n");
        		break;
        	default:
        		errors.append("Invalid Cutoff metric \n");
        }
        
        return errors.toString();
    }

    
    /**
     * Checks if SignatureGMTFileName is provided and if the file can be read.
     * 
     * @return String with error messages (one error per line) or empty String if everything is okay.
     */
    public String checkGMTfiles() {
        String signatureGMTFileName = getSignatureGMTFileName();
		if(signatureGMTFileName.isEmpty() || !EnrichmentMapParameters.checkFile(signatureGMTFileName))
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
    public void setSignature_CutoffMetric(CutoffMetric signature_CutoffMetric) {
        this.signature_CutoffMetric = signature_CutoffMetric;
    }

    /**
     * @return the signature_CutoffMetric
     */
    public CutoffMetric getSignature_CutoffMetric() {
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
    public void setDefault_signature_CutoffMetric(CutoffMetric defaultSignatureCutoffMetric) {
        default_signature_CutoffMetric = defaultSignatureCutoffMetric;
    }

    /**
     * @return the default_signature_CutoffMetric
     */
    public CutoffMetric getDefault_signature_CutoffMetric() {
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

    public FilterMetric getSignature_filterMetric() {
        return signature_filterMetric;
    }

    public void setSignature_filterMetric(FilterMetric signature_filterMetric) {
        this.signature_filterMetric = signature_filterMetric;
    }

    public FilterMetric getDefault_signature_filterMetric() {
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
	public FilterMetric getSignature_rankTest() {
		return signature_rankTest;
	}

	/**
	 * @param signature_rankTest the signature_rankTest to set
	 */
	public void setSignature_rankTest(FilterMetric signature_rankTest) {
		this.signature_rankTest = signature_rankTest;
	}

	/**
	 * @return the default_signature_rankTest
	 */
	public FilterMetric getDefault_signature_rankTest() {
		return default_signature_rankTest;
	}

	/**
	 * @param default_signature_rankTest the default_signature_rankTest to set
	 */
	public void setDefault_signature_rankTest(FilterMetric default_signature_rankTest) {
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
	
	
	public String getAttributePrefix() {
		return attributePrefix;
	}


	public void setAttributePrefix(String attributePrefix) {
		this.attributePrefix = attributePrefix;
	}
}
