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

/**
 * @author revilo
 * @date   Jul 9, 2009
 * @time   6:01:53 PM
 *
 */
public class PostAnalysisParameters extends EnrichmentMapParameters {
    
    // Post Analysis Type:
    private boolean isSignatureHub;

    // Disease Signature Constants
    /**
     * Enum for Signature-Hub cut-off metric:
     * HYPERGEOM  = 0
     * ABS_NUMBER = 1
     * JACCARD    = 2
     * OVERLAP    = 3
     */
    final public static int HYPERGEOM = 0, ABS_NUMBER = 1, JACCARD = 2, OVERLAP = 3; 
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
    private double signature_Hypergeom_Cutoff;   

    private int    signature_CutoffMetric;
    
    // Disease Signature default Values:
    private int    default_signature_absNumber_Cutoff = 5;
    private double default_signature_Jaccard_Cutoff   = 0.125;  
    private double default_signature_Overlap_Cutoff   = 0.25;  
    private double default_signature_Hypergeom_Cutoff = 0.05;
    
    private int    default_signature_CutoffMetric      = HYPERGEOM;
    
    // Disease Signature Data Structures:
    private HashMap<String,GeneSet> signatureGenesets;
    private DefaultListModel signatureSetNames;
    private DefaultListModel selectedSignatureSetNames;
    
    // Disease Signature State variables:
    private double currentNodePlacementY_Offset;
    
    /**
     * default constructor
     * @param instance of EnrichmentMapParameters with parameters of the current EnrichmentMap
     */
    public PostAnalysisParameters(EnrichmentMapParameters emParams) {
        // EnrichmentMapParameters
    	super(emParams);

        // Post Analysis Type:
        this.isSignatureHub = true;
    	
    	// Disease Signature Parameters:
        this.signatureGMTFileName       = "";
        this.signature_absNumber_Cutoff = default_signature_absNumber_Cutoff;
        this.signature_Jaccard_Cutoff   = default_signature_Jaccard_Cutoff;
        this.signature_Overlap_Cutoff   = default_signature_Overlap_Cutoff;
        this.signature_Hypergeom_Cutoff = default_signature_Hypergeom_Cutoff;
        this.signature_CutoffMetric     = default_signature_CutoffMetric;
        
        // Disease Signature Data Structures:
        this.signatureGenesets         = new HashMap<String,GeneSet>();
        this.signatureSetNames         = new DefaultListModel();
        this.selectedSignatureSetNames = new DefaultListModel();
        
        this.setGenesetSimilarity( emParams.getGenesetSimilarity() );
        
        // Disease Signature State variables:
        this.currentNodePlacementY_Offset = 0.0;
    }

    /**
     * constructor to create a clean instance of PostAnalysisParameters
     * (used when using the Reset Button)
     */
    public PostAnalysisParameters() {
        // EnrichmentMapParameters
        super();

        // Post Analysis Type:
        this.isSignatureHub = true;
        
        // Disease Signature Parameters:
        this.signatureGMTFileName       = "";
        this.signature_absNumber_Cutoff = default_signature_absNumber_Cutoff;
        this.signature_Jaccard_Cutoff   = default_signature_Jaccard_Cutoff;
        this.signature_Overlap_Cutoff   = default_signature_Overlap_Cutoff;
        this.signature_Hypergeom_Cutoff = default_signature_Hypergeom_Cutoff;
        this.signature_CutoffMetric     = default_signature_CutoffMetric;
        
        // Disease Signature Data Structures:
        this.signatureGenesets         = new HashMap<String,GeneSet>();
        this.signatureSetNames         = new DefaultListModel();
        this.selectedSignatureSetNames = new DefaultListModel();
        
        // Disease Signature State variables:
        this.currentNodePlacementY_Offset = 0.0;
    }
    
    
    /**
     * copy constructor
     * @param instance if PostAnalysisParameters to copy
     */
    public PostAnalysisParameters(PostAnalysisParameters copy) {
        // EnrichmentMapParameters
        super(copy);

        // Post Analysis Type:
        this.isSignatureHub = copy.isSignatureHub();

        // Disease Signature Parameters:
        this.signatureGMTFileName       = copy.getSignatureGMTFileName();
        this.signature_absNumber_Cutoff = copy.getSignature_absNumber_Cutoff();
        this.signature_Jaccard_Cutoff   = copy.getSignature_Jaccard_Cutoff();
        this.signature_Overlap_Cutoff   = copy.getSignature_Overlap_Cutoff();
        this.signature_Hypergeom_Cutoff = copy.getSignature_Hypergeom_Cutoff();
        this.signature_CutoffMetric     = copy.getSignature_CutoffMetric();

        // Disease Signature Data Structures:
        this.signatureGenesets         = copy.getSignatureGenesets();
        this.signatureSetNames         = copy.getSignatureSetNames();
        this.selectedSignatureSetNames = copy.getSelectedSignatureSetNames();

        // needed because EnrichmentMapParameters copy constructor assumes that it is only called from within the InputPanel:
        this.setGenesets( copy.getGenesets() ); 
        this.setGenesetSimilarity( copy.getGenesetSimilarity() );
        
        // Disease Signature State variables:
        this.currentNodePlacementY_Offset = copy.getCurrentNodePlacementY_Offset();

    }

    
    public String checkMinimalRequirements() {
    	String errors = "";
    	// TODO: checkMinimalRequirements for PostAnalysis
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
     * @param isSignatureHub the isSignatureHub to set
     */
    public void setSignatureHub(boolean isSignatureHub) {
        this.isSignatureHub = isSignatureHub;
    }

    /**
     * @return the default_signature_absNumber_Cutoff
     */
    public int getDefault_signature_absNumber_Cutoff() {
        return default_signature_absNumber_Cutoff;
    }

    /**
     * @return the default_signature_Jaccard_Cutoff
     */
    public double getDefault_signature_Jaccard_Cutoff() {
        return default_signature_Jaccard_Cutoff;
    }

    /**
     * @return the default_signature_Overlap_Cutoff
     */
    public double getDefault_signature_Overlap_Cutoff() {
        return default_signature_Overlap_Cutoff;
    }

    /**
     * @return the default_signature_Hypergeom_Cutoff
     */
    public double getDefault_signature_Hypergeom_Cutoff() {
        return default_signature_Hypergeom_Cutoff;
    }

    /**
     * @return the default_signature_CutoffMetric
     */
    public int getDefault_signature_CutoffMetric() {
        return default_signature_CutoffMetric;
    }

    /**
     * @param defaultSignatureAbsNumberCutoff the default_signature_absNumber_Cutoff to set
     */
    public void setDefault_signature_absNumber_Cutoff(
            int defaultSignatureAbsNumberCutoff) {
        default_signature_absNumber_Cutoff = defaultSignatureAbsNumberCutoff;
    }

    /**
     * @param defaultSignatureJaccardCutoff the default_signature_Jaccard_Cutoff to set
     */
    public void setDefault_signature_Jaccard_Cutoff(
            double defaultSignatureJaccardCutoff) {
        default_signature_Jaccard_Cutoff = defaultSignatureJaccardCutoff;
    }

    /**
     * @param defaultSignatureOverlapCutoff the default_signature_Overlap_Cutoff to set
     */
    public void setDefault_signature_Overlap_Cutoff(
            double defaultSignatureOverlapCutoff) {
        default_signature_Overlap_Cutoff = defaultSignatureOverlapCutoff;
    }

    /**
     * @param defaultSignatureHypergeomCutoff the default_signature_Hypergeom_Cutoff to set
     */
    public void setDefault_signature_Hypergeom_Cutoff(
            double defaultSignatureHypergeomCutoff) {
        default_signature_Hypergeom_Cutoff = defaultSignatureHypergeomCutoff;
    }

    /**
     * @param defaultSignatureCutoffMetric the default_signature_CutoffMetric to set
     */
    public void setDefault_signature_CutoffMetric(int defaultSignatureCutoffMetric) {
        default_signature_CutoffMetric = defaultSignatureCutoffMetric;
    }

    /**
     * @return the isDiseaseSignature
     */
    public boolean isSignatureHub() {
        return isSignatureHub;
    }

    /**
     * @param signatureGenesets the signatureGenesets to set
     */
    public void setSignatureGenesets(HashMap<String,GeneSet> signatureGenesets) {
        this.signatureGenesets = signatureGenesets;
    }

    /**
     * @return the signatureGenesets
     */
    public HashMap<String, GeneSet> getSignatureGenesets() {
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


}
