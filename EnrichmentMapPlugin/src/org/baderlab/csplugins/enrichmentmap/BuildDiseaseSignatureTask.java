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

import giny.view.NodeView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import cytoscape.CyEdge;
import cytoscape.CyNetwork;
import cytoscape.CyNode;
import cytoscape.Cytoscape;
import cytoscape.data.CyAttributes;
import cytoscape.task.Task;
import cytoscape.task.TaskMonitor;
import cytoscape.view.CyNetworkView;

import cern.jet.random.HyperGeometric;
import cern.jet.random.engine.*;
import cern.jet.stat.Gamma;

/**
 * @author revilo
 * @date   July 10, 2009
 * @time   3:58:24 PM
 *
 */
public class BuildDiseaseSignatureTask implements Task {
    private PostAnalysisParameters paParams;
//    private EnrichmentMapParameters emParams;
    
    // Keep track of progress for monitoring:
    private TaskMonitor taskMonitor = null;
    private boolean interrupted = false;
    
    private HashMap<String,GeneSet> EnrichmentGenesets;
    private HashMap<String,GeneSet> SignatureGenesets;
    private HashMap<String,GeneSet> SelectedSignatureGenesets;
    
    // Gene Populations:
    private HashSet<Integer> EnrichmentGenes;
    private HashSet<Integer> SignatureGenes;
        
    private HashMap<String,GenesetSimilarity> geneset_similarities;
    
    private static RandomEngine dRand = new DRand(); 
    
    /**
     * default constructor
     * @param paParams
     */
    public BuildDiseaseSignatureTask(PostAnalysisParameters paParams) {
        //create a new instance of the parameters and copy the version received from the input
        //window into this new instance.
        this.setPaParams(new PostAnalysisParameters(paParams));
        this.EnrichmentGenesets   = this.getPaParams().getGenesets();
        this.SignatureGenesets    = this.getPaParams().getSignatureGenesets();

        if (this.paParams.getGenesetSimilarity() == null)
            this.geneset_similarities = new HashMap<String, GenesetSimilarity>();
        else
            this.geneset_similarities = this.paParams.getGenesetSimilarity();
            
        this.SelectedSignatureGenesets = new HashMap<String, GeneSet>();
        for (int i = 0; i < paParams.getSelectedSignatureSetNames().getSize(); i++){
            this.SelectedSignatureGenesets.put(paParams.getSelectedSignatureSetNames().get(i).toString(),
                    this.SignatureGenesets.get(paParams.getSelectedSignatureSetNames().get(i)));
        }
        // EnrichmentGenes: pool of all genes in Enrichment Gene Sets
        EnrichmentGenes = new HashSet<Integer>();
        for (Iterator<String> i = EnrichmentGenesets.keySet().iterator(); i.hasNext(); ){
            String setName = i.next();
            EnrichmentGenes.addAll(EnrichmentGenesets.get(setName).getGenes());
        }
        // SignatureGenes: pool of all genes in Signature Gene Sets
        SignatureGenes = new HashSet<Integer>();
        for (Iterator<String> i = SignatureGenesets.keySet().iterator(); i.hasNext(); ){
            String setName = i.next();
            SignatureGenes.addAll(SignatureGenesets.get(setName).getGenes());
        }
        
    }


    public void buildDiseaseSignature() {

        /* **************************************************
         * Calculate Similarity between Signature Gene Sets *
         * and Enrichment Genesets.                         *
         ****************************************************/
        int maxValue = SelectedSignatureGenesets.size() * EnrichmentGenesets.size();
        int currentProgress = 0;
        double currentNodeY_offset = paParams.getCurrentNodePlacementY_Offset();
        double currentNodeY_increment = 150.0;
        
        try {
            CyNetwork current_network = Cytoscape.getCurrentNetwork();
            CyNetworkView currentNetworkView = Cytoscape.getCurrentNetworkView();
            CyAttributes cyNodeAttrs = Cytoscape.getNodeAttributes();
            CyAttributes cyEdgeAttrs = Cytoscape.getEdgeAttributes();

            String prefix = paParams.getAttributePrefix();
            if (prefix == null) {
                prefix = "EM1_";
                paParams.setAttributePrefix(prefix);
            }
            
            // make a HashMap of all Nodes in the Network
            HashMap<String,CyNode> nodesMap = new HashMap<String, CyNode>();
            List<CyNode> nodesList = current_network.nodesList();
            Iterator<CyNode> nodesIterator = nodesList.iterator();
            
            while (nodesIterator.hasNext()){
                CyNode aNode = (CyNode)nodesIterator.next();
                nodesMap.put(aNode.getIdentifier(), aNode);
            }
            
            // Common gene universe: Intersection of EnrichmentGenes and SignatureGenes
            HashSet<Integer> geneUniverse = new HashSet<Integer>();
            geneUniverse.addAll(EnrichmentGenes);
            geneUniverse.retainAll(SignatureGenes);
            int universeSize = geneUniverse.size();
            
            //iterate over selected Signature genesets
            for (Iterator<String> i = SelectedSignatureGenesets.keySet().iterator(); i.hasNext(); ){
                String hub_name = i.next().toString();
                
                // iterate over Enrichment Genesets
                for (Iterator<String> j = EnrichmentGenesets.keySet().iterator(); j.hasNext();) {
                    String geneset_name = j.next().toString();

                    // Calculate Percentage.  This must be a value between 0..100.
                    int percentComplete = (int) (((double) currentProgress / maxValue) * 100);
                    //  Estimate Time Remaining
                    long timeRemaining = maxValue - currentProgress;
                    if (taskMonitor != null) {
                       taskMonitor.setPercentCompleted(percentComplete);
                       taskMonitor.setStatus("Computing Geneset similirity " + currentProgress + " of " + maxValue);
                       taskMonitor.setEstimatedTimeRemaining(timeRemaining);
                    }
                    currentProgress++;
                    
                    if (interrupted) {
                        throw new InterruptedException();
                    }
                    
                    // Calculate overlap:
                    
                    //Check to see if this comparison has been done
                    //The key for the set of geneset similarities is the
                    //combination of the two names.  Check for either variation name1_name2
                    //or name2_name1
                    String similarity_key1 = hub_name     + " (" + PostAnalysisParameters.SIGNATURE_INTERACTION_TYPE + ") " + geneset_name;
                    String similarity_key2 = geneset_name + " (" + PostAnalysisParameters.SIGNATURE_INTERACTION_TYPE + ") " + hub_name;

                    //first check to see if the terms are the same
                    if(hub_name.equalsIgnoreCase(geneset_name)) {
                       //don't compare two identical genesets
                    }
                    else if (! nodesMap.containsKey(geneset_name)) {
                        // skip if the Geneset is not in the Network
                    }
                    else if(geneset_similarities.containsKey(similarity_key1) || geneset_similarities.containsKey(similarity_key2)){
                        //skip this geneset comparison.  It has already been done.
                    }
                    else{
                        //get the two genesets
                        GeneSet geneset1 = SelectedSignatureGenesets.get(hub_name);
                        GeneSet geneset2 = EnrichmentGenesets.get(geneset_name);

                        HashSet<Integer> genes1 = geneset1.getGenes();
                        HashSet<Integer> genes2 = geneset2.getGenes();

                        // restrict to a common gene universe
                        genes1.retainAll(geneUniverse);
                        genes2.retainAll(geneUniverse);

                       //Get the intersection
                        Set<Integer> intersection = new HashSet<Integer>(genes1);
                        intersection.retainAll(genes2);

                        //Get the union of the two sets
                        Set<Integer> union = new HashSet<Integer>(genes1);
                        union.addAll(genes2);

                        double coeffecient;

                        // if  either Jaccard or Overlap similarity are requested:
                        if(paParams.getSignature_CutoffMetric() == PostAnalysisParameters.JACCARD){
                            //compute Jaccard similarity
                            coeffecient = (double)intersection.size() / (double)union.size();
                        }
                        else if(paParams.getSignature_CutoffMetric() == PostAnalysisParameters.OVERLAP){
                            //compute Overlap similarity
                            coeffecient = (double)intersection.size() / Math.min((double)genes1.size(), (double)genes2.size());
                        } else {
                            // use setting from Enrichment Analysis
                            if (paParams.isJaccard() ) {
                                //compute Jaccard similarity
                                coeffecient = (double)intersection.size() / (double)union.size();
                            } else {
                                //compute Overlap similarity
                                coeffecient = (double)intersection.size() / Math.min((double)genes1.size(), (double)genes2.size());
                            }
                        }
                        
                        //create Geneset similarity object
                        GenesetSimilarity comparison = new GenesetSimilarity(hub_name,geneset_name, coeffecient, (HashSet<Integer>)intersection);
                        
                        // Calculate Hypergeometric pValue for Overlap
                        int N = universeSize;           //number of total genes      (size of population / total number of balls)
                        int n = genes1.size();          //size of signature geneset  (sample size / number of extracted balls)
                        int m = genes2.size();          //size of enrichment geneset (success Items / number of white balls in population)
                        int k = intersection.size();    //size of intersection       (successes /number of extracted white balls)
                        double hyperPval;
                        
                        if (k > 0) 
                            hyperPval = hyperGeomPvalue(N, n, m, k);
                        else // Correct p-value of empty intersections to 1 (i.e. not significant)
                            hyperPval = 1.0;
                        
                        //DEBUG:
//                        System.out.printf("N=%4d;\tn=%3d;\ts=%4d;\tk=%2d;\t=>\tp-Value=%10.8f\t%s%n", N, n, m, k, hyperPval, geneset_name);
//                        if (k > 0) 
//                            System.out.printf("%-50.50s,%4d,%3d,%4d,%2d,%10.8f,%10.8f,%10.8f%n", geneset_name.replace("[;,\"'()]", "_"), N, n, m, k, hyperGeomPvalue_colt(N, n, m, k),hyperGeomPvalue(N, n, m, k), hyperGeomPvalue_BiNGO(N, n, m, k));
                        //DEBUG END
                        
                        comparison.setHypergeom_pvalue(hyperPval);
                        comparison.setInteractionType(PostAnalysisParameters.SIGNATURE_INTERACTION_TYPE);
                            
                        geneset_similarities.put(similarity_key1,comparison);
                    }
                } // End: iterate over Enrichment Genesets
                
                /* ***************************
                 * Create Signature Hub Node *
                 *****************************/
                CyNode hub_node = Cytoscape.getCyNode(hub_name, true);
                current_network.addNode(hub_node);
                
                // add currentNodeY_offset to initial Y position of the Node
                // and increase currentNodeY_offset for the next Node
                NodeView hubNodeView = currentNetworkView.getNodeView(hub_node);
                double hubNodeY = hubNodeView.getYPosition();
                hubNodeView.setYPosition(hubNodeY + currentNodeY_offset);
                currentNodeY_offset += currentNodeY_increment;
                
                String formatted_label =  VisualizeEnrichmentMapTask.formatLabel(hub_node.getIdentifier());
                cyNodeAttrs.setAttribute(hub_node.getIdentifier(), prefix + EnrichmentMapVisualStyle.FORMATTED_NAME, formatted_label);

            /*TODO: Add gene list to Node                
                //create an attribute that stores the genes that are associated with this node as an attribute list
                //only create the list if the hashkey 2 genes is not null Otherwise it take too much time to populate the list
                if(paParams.getHashkey2gene() != null){
                    List<String> gene_list = new ArrayList<String>();
                    HashSet genes_hash = SelectedSignatureGenesets.get(hub_name).getGenes();
                    for(Iterator j=genes_hash.iterator(); j.hasNext();){
                        Integer current = (Integer)j.next();
                        String gene = paParams.getGeneFromHashKey(current);
                        if(gene_list != null)
                            gene_list.add(gene);
                    }
    
                    cyNodeAttrs.setListAttribute(hub_node.getIdentifier(), prefix+EnrichmentMapVisualStyle.GENES, gene_list);
                }
             */
                
                // set Visual Style bypass
                cyNodeAttrs.setAttribute(hub_node.getIdentifier(), "node.shape", paParams.getSignatureHub_nodeShape());
                cyNodeAttrs.setAttribute(hub_node.getIdentifier(), "node.fillColor", paParams.getSignatureHub_nodeColor());
                cyNodeAttrs.setAttribute(hub_node.getIdentifier(), "node.borderColor", paParams.getSignatureHub_borderColor());
                
            }// End: iterate over Signature Genesets
            
            paParams.setCurrentNodePlacementY_Offset(currentNodeY_offset);
            
            /* ****************************
             * Create Signature Hub Edges *
             ******************************/

            for (Iterator<String> i = geneset_similarities.keySet().iterator(); i.hasNext() ;) {
                if (interrupted) {
                    throw new InterruptedException();
                }
                String edge_name = i.next().toString();
                
                if (! geneset_similarities.get(edge_name).getInteractionType().equals(PostAnalysisParameters.SIGNATURE_INTERACTION_TYPE))
                    // skip if it's not a signature edge
                    continue;

                // check if combination passes Cut-Off:
                boolean passed_cutoff = false;
                if ( (paParams.getSignature_CutoffMetric() == PostAnalysisParameters.ABS_NUMBER) && 
                     (geneset_similarities.get(edge_name).getSizeOfOverlap() >= paParams.getSignature_absNumber_Cutoff() ) )
                    passed_cutoff = true;
                else if ( (paParams.getSignature_CutoffMetric() == PostAnalysisParameters.JACCARD) && 
                          (geneset_similarities.get(edge_name).getJaccard_coeffecient() >= paParams.getSignature_Jaccard_Cutoff() ) )
                    passed_cutoff = true;
                else if ( (paParams.getSignature_CutoffMetric() == PostAnalysisParameters.OVERLAP) && 
                        (geneset_similarities.get(edge_name).getJaccard_coeffecient() >= paParams.getSignature_Overlap_Cutoff() ) )
                    passed_cutoff = true;
                else if ( (paParams.getSignature_CutoffMetric() == PostAnalysisParameters.HYPERGEOM) && 
                          (geneset_similarities.get(edge_name).getHypergeom_pvalue() != -1.0) &&
                          (geneset_similarities.get(edge_name).getHypergeom_pvalue() <= paParams.getSignature_Hypergeom_Cutoff() ) )
                    passed_cutoff = true;

                //DEBUG: remove debug code:
//                if (paParams.getSignature_CutoffMetric() == PostAnalysisParameters.HYPERGEOM)
//                    System.out.println("HyperCutoff: "+paParams.getSignature_Hypergeom_Cutoff()+" (" + passed_cutoff + ")");
                
                if (passed_cutoff) {
                    CyNode hub_node = Cytoscape.getCyNode( geneset_similarities.get(edge_name).getGeneset1_Name() );
                    CyNode gene_set = Cytoscape.getCyNode( geneset_similarities.get(edge_name).getGeneset2_Name() );
   
                    CyEdge edge = Cytoscape.getCyEdge(hub_node, gene_set, "interaction", PostAnalysisParameters.SIGNATURE_INTERACTION_TYPE, true);
                    current_network.addEdge(edge);
                    
                /* TODO: Add gene list to Edges
                    //create an attribute that stores the genes that are associated with this edge as an attribute list
                    //only create the list if the hashkey 2 genes is not null Otherwise it take too much time to populate the list
                    if(paParams.getHashkey2gene() != null){
                        List<String> gene_list = new ArrayList<String>();
                        HashSet genes_hash = geneset_similarities.get(edge_name).getOverlapping_genes();
                        for(Iterator k=genes_hash.iterator(); k.hasNext();){
                            Integer current = (Integer)k.next();
                            String gene = paParams.getGeneFromHashKey(current);
                            if(gene_list != null)
                                gene_list.add(gene);
                        }
                        cyEdgeAttrs.setListAttribute(edge.getIdentifier(), prefix+EnrichmentMapVisualStyle.OVERLAP_GENES, gene_list);
                    }
                 */                    

                    cyEdgeAttrs.setAttribute(edge.getIdentifier(), prefix + EnrichmentMapVisualStyle.OVERLAP_SIZE       , geneset_similarities.get(edge_name).getSizeOfOverlap());
                    cyEdgeAttrs.setAttribute(edge.getIdentifier(), prefix + EnrichmentMapVisualStyle.JACCARD_COEFFECIENT, geneset_similarities.get(edge_name).getJaccard_coeffecient());
                    cyEdgeAttrs.setAttribute(edge.getIdentifier(), prefix + EnrichmentMapVisualStyle.HYPERGEOM_PVALUE   , geneset_similarities.get(edge_name).getHypergeom_pvalue());
                    cyEdgeAttrs.setAttribute(edge.getIdentifier(), "edge.color", paParams.getSignatureHub_edgeColor());
                    //change "edge.lineWidth" based on Hypergeometric Value 
                    if (geneset_similarities.get(edge_name).getHypergeom_pvalue() <= (paParams.getSignature_Hypergeom_Cutoff()/100) )
                        cyEdgeAttrs.setAttribute(edge.getIdentifier(), "edge.lineWidth", "7.5");
                    else 
                    if (geneset_similarities.get(edge_name).getHypergeom_pvalue() <= (paParams.getSignature_Hypergeom_Cutoff()/10) )
                        cyEdgeAttrs.setAttribute(edge.getIdentifier(), "edge.lineWidth", "5.0");
                    else
                        cyEdgeAttrs.setAttribute(edge.getIdentifier(), "edge.lineWidth", "1.0");
                    

                } //if (geneset_similarities.get(edge_name).getSizeOfOverlap() > 0)
            } //for
            //TODO: remove obsolete Code
            /* 
             * PYTHON IMPLEMENTATION          
             * for geneset in dis_sig[hub_name].keys():
             *     # generate new edges between hub-node and Gene sets
             *     if (geneset in nodes.keys() ) :
             *         edge = Cytoscape.getCyEdge(nodes[hub_name], nodes[geneset], "interaction", "-", True)
             *         graph.addEdge(edge)
             *         edges[edge.getIdentifier()] = edge
             *         x = graph.setEdgeAttributeValue(edge, "EM1_Overlap_size", dis_sig[hub_name][geneset][0])
             *         x = graph.setEdgeAttributeValue(edge, "EM1_Overlap_Prc",  dis_sig[hub_name][geneset][1])
             *         x = graph.setEdgeAttributeValue(edge, "EM1_Overlap_Hpv",  dis_sig[hub_name][geneset][2])
             *         if dis_sig[hub_name][geneset][2] < 0.0001 :
             *             x = graph.setEdgeAttributeValue(edge, "EM1_jaccard_coeffecient",  1.0 )
             *             # x = graph.setEdgeAttributeValue(edge, "edge.lineWidth", 5.0)
             *         elif dis_sig[hub_name][geneset][2] < 0.05 :
             *             x = graph.setEdgeAttributeValue(edge, "EM1_jaccard_coeffecient",  0.51 )
             *             # x = graph.setEdgeAttributeValue(edge, "edge.lineWidth", 1.0)
             *         else :
             *             x = graph.setEdgeAttributeValue(edge, "EM1_jaccard_coeffecient",  0.0 )
             *             # x = graph.setEdgeAttributeValue(edge, "edge.lineWidth", 0.0)
             *             
             *         # set Visual Style bypass
             *         x = graph.setEdgeAttributeValue(edge, "edge.color", hub_edge_color)
             *         
             */
 
            Cytoscape.getCurrentNetworkView().redrawGraph(false, true);
        } catch (InterruptedException e) {
            taskMonitor.setException(e, "Generation of Signature Hubs cancelled");
        }
    }
    
    
    /**
     * Calculate the p-Value of the Hypergeometric Distribution<p>
     * 
     *  from:  http://en.wikipedia.org/wiki/Hypergeometric_distribution<p>
     * 
     * P(X=k) = {m over k} * { (N-m) over (n-k) } / {N over n}
     * 
     * @param N size of the population  (Universe of genes)
     * @param n size of the sample      (signature geneset) 
     * @param m successes in population (enrichment geneset)
     * @param k successes in sample     (intersection of both genesets)
     * 
     * @return the p-Value of the Hypergeometric Distribution
     */
    public static double hyperGeomPvalue(int N, int n, int m, int k) {
        //calculating in logarithmic scale as we are dealing with large numbers. 
        double log_p = binomialLog(m, k) + binomialLog(N-m, n-k) - binomialLog(N, n) ;
        
        return Math.exp(log_p);
    }
    
    /**
     * Calculate the log of Binomial coefficient "n over k" aka "n choose k"
     * 
     * adapted from http://code.google.com/p/beast-mcmc/source/browse/trunk/src/dr/math/Binomial.java?spec=svn1660&r=1660
     * @version Id: Binomial.java,v 1.11 2005/05/24 20:26:00 rambaut Exp
     * Licensed under "LGPL 2.1 or later"
     * 
     * original by:
     * @author Andrew Rambaut
     * @author Alexei Drummond
     * @author Korbinian Strimmer
     * 
     * adapted for using cern.jet.stat:
     * @author Oliver Stueker
     * 
     * @param n
     * @param k
     * @return the binomial coefficient "n over k"
     */
    public static double binomialLog(int n, int k) {
        return (Gamma.logGamma(n + 1.0) - Gamma.logGamma(k + 1.0) - Gamma.logGamma(n - k + 1.0));
    }
    
    
    /**
     * Calculate the p-Value of the Hypergeometric Distribution<p>
     * 
     * Implementation from BiNGO ( http://www.psb.ugent.be/cbd/papers/BiNGO/ ):
     * calculates the Hypergeometric probability P(x or more |X,N,n) for given x, X, n, N.<p>
     *      
     * P(x or more |X,N,n) = 1 - sum{[C(n,i)*C(N-n, X-i)] / C(N,X)}
     * for i=0 ... x-1
     * 
     * 
     * @param bigN size Universe                   number of genes in whole genome.                       
     * @param bigX sample Size                     number of genes in cluster A.                          
     * @param n    number of white balls.          number of genes with GO category B in the whole genome.
     * @param x    number of white balls in Sample number of genes with GO category B in cluster A.       
     *
     * @return the p-Value of the Hypergeometric Distribution
     * 
     * @author BiNGO
     */
    public static double hyperGeomPvalue_BiNGO(int bigN, int bigX, int n, int x) {
        if (bigN >= 2) {
            double sum = 0;
            // mode of distribution, integer division (returns integer <= double
            // result)!
            int mode = (bigX + 1) * (n + 1) / (bigN + 2);
            if (x >= mode) {
                int i = x;
                while ((bigN - n >= bigX - i) && (i <= Math.min(bigX, n))) {
                    double pdfi = Math.exp(Gamma.logGamma(n + 1)
                            - Gamma.logGamma(i + 1) - Gamma.logGamma(n - i + 1)
                            + Gamma.logGamma(bigN - n + 1)
                            - Gamma.logGamma(bigX - i + 1)
                            - Gamma.logGamma(bigN - n - bigX + i + 1)
                            - Gamma.logGamma(bigN + 1)
                            + Gamma.logGamma(bigX + 1)
                            + Gamma.logGamma(bigN - bigX + 1));
                    sum = sum + pdfi;
                    i++;
                }
            } else {
                int i = x - 1;
                while ((bigN - n >= bigX - i) && (i >= 0)) {
                    double pdfi = Math.exp(Gamma.logGamma(n + 1)
                            - Gamma.logGamma(i + 1) - Gamma.logGamma(n - i + 1)
                            + Gamma.logGamma(bigN - n + 1)
                            - Gamma.logGamma(bigX - i + 1)
                            - Gamma.logGamma(bigN - n - bigX + i + 1)
                            - Gamma.logGamma(bigN + 1)
                            + Gamma.logGamma(bigX + 1)
                            + Gamma.logGamma(bigN - bigX + 1));
                    sum = sum + pdfi;
                    i--;
                }
                sum = 1 - sum;
            }
            return (new Double(sum));
        } else {
            return (new Double(1));
        }
    }
    
    
    
    /**
     * Calculate the p-Value of the Hypergeometric Distribution<p>
     * 
     * Implementation from:  http://acs.lbl.gov/~hoschek/colt/api/cern/jet/random/HyperGeometric.html<p>
     * 
     * @param N size of the population  (Universe of genes)
     * @param n size of the sample      (signature geneset) 
     * @param m successes in population (enrichment geneset)
     * @param k successes in sample     (intersection of both genesets)
     * 
     * @return the p-Value of the Hypergeometric Distribution
     */
    public static double hyperGeomPvalue_colt(int N, int n, int m, int k) {
        //calculating in logarithmic scale as we are dealing with large numbers. 
        
        int colt_N = N;                 //number of total genes      (size of population / total number of balls)  
        int colt_s = n;                 //size of signature geneset  (sample size / number of extracted balls)  
        int colt_n = m;                 //size of enrichment geneset (success Items / number of white balls in population)  
        int colt_k = k;                 //size of intersection       (successes /number of extracted white balls)  
              
        HyperGeometric hyperDist = new HyperGeometric(colt_N, colt_s, colt_n, dRand); // generate Hypergeometric Distribution  
        return hyperDist.pdf(colt_k); // calculate p-value for k successes  

    }

    /* ***************************************
     * getters and setters                   *
     *****************************************/
    /**
     * @param paParams the paParams to set
     */
    public void setPaParams(PostAnalysisParameters paParams) {
        this.paParams = paParams;
    }

    /**
     * @return the paParams
     */
    public PostAnalysisParameters getPaParams() {
        return paParams;
    }

    
    
    // ***************************************
    // from here: Auto-generated method stubs!
    // ***************************************
    /**
     * @see cytoscape.task.Task#run()
     */
    public void run() {
    	buildDiseaseSignature();
    }

    /**
     * @see cytoscape.task.Task#getTitle()
     */
    public String getTitle() {
        // TODO Auto-generated method stub
        return new String("Generating Signature Hubs");
    }

    /**
     * @see cytoscape.task.Task#halt()
     */
    public void halt() {
        this.interrupted = true;

    }

    /**
     * @see cytoscape.task.Task#setTaskMonitor(cytoscape.task.TaskMonitor)
     */
    public void setTaskMonitor(TaskMonitor taskMonitor)
            throws IllegalThreadStateException {

        if (this.taskMonitor != null) {
            throw new IllegalStateException("Task Monitor is already set.");
        }
        this.taskMonitor = taskMonitor;
    }

}
