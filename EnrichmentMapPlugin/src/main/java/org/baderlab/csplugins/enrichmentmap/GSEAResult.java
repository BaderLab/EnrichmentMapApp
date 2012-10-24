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
/**
 * Created by
 * User: risserlin
 * Date: Jan 8, 2009
 * Time: 3:01:22 PM
 * <p>
 * Class representing a specialized enrichment result generated from Gene set enrichment Analysis(GSEa)
 * GSEA enrichment result contain additional information (as compared to a generic result) including
 * Enrichment score(ES), normalized Enrichment Score (NES), Family-wise error rate (FWER)
 */
public class GSEAResult extends EnrichmentResult{

    //gene set name
    private String Name;
    //gene set size
    private int gsSize;
    //enrichment score
    private double ES;
    //normalized enrichment score
    private double NES;
    //p-value
    private double pvalue;
    //false discovery rate q-value
    private double fdrqvalue;
    //family wise error rate (fwer) q-value
    private double fwerqvalue;
    //the rank (off by two) of the gene that is at the apex of ES score calculation
    private int rankAtMax;
    //translate the rank at max to the corresponding score at the max
    private double scoreAtMax;

    private String Source = "none";

    /**
     * Class Constructor
     *
     * @param name - gene set name
     * @param size - gene set size
     * @param ES - enrichment score
     * @param NES - normalized enrichment score
     * @param pvalue
     * @param fdrqvalue
     * @param fwerqvalue
     */
    public GSEAResult(String name, int size, double ES, double NES, double pvalue, double fdrqvalue, double fwerqvalue, int rankAtMax, double scoreAtMax) {
        Name = name;
        this.gsSize = size;
        this.ES = ES;
        this.NES = NES;
        this.pvalue = pvalue;
        this.fdrqvalue = fdrqvalue;
        this.fwerqvalue = fwerqvalue;
        this.rankAtMax = rankAtMax;
        this.scoreAtMax = scoreAtMax;

        setSource();
    }

    /**
     * Class constructor - build GSEA result from tokenized line from a GSEA results file
     *
     * @param tokens - tokenized line from a GSEA results file
     */
    public GSEAResult(String[] tokens){

        //old session files will be missing rankatmax and scoreatmax
        if(tokens.length != 8)
            if(tokens.length != 10)
                return;

        this.Name = tokens[1];
        this.gsSize = Integer.parseInt(tokens[2]);
        this.ES = Double.parseDouble(tokens[3]);
        this.NES = Double.parseDouble(tokens[4]);
        this.pvalue = Double.parseDouble(tokens[5]);
        this.fdrqvalue = Double.parseDouble(tokens[6]);
        this.fwerqvalue = Double.parseDouble(tokens[7]);

        if(tokens.length == 10){
            this.rankAtMax = Integer.parseInt(tokens[8]);
            this.scoreAtMax = Double.parseDouble(tokens[9]);
        }
        else{
            this.rankAtMax = -1;
            this.scoreAtMax = -1;
        }
        setSource();
    }

    /**
     * Is this a gene set of interest, does its enrichment score pass the p-value threshold and fdr threshold
     *
     * @param pvalue - pvalue of current gene set enrichment
     * @param fdrqvalue  - fdr q-value of current gene set enrichment
     * @return whether these p-value and fdr q-value for the specified gene set enrichment fall into our gene sets of interest
     * category (i.e. do these values pass the specified thresholds)
     */
    public boolean geneSetOfInterest(double pvalue, double fdrqvalue){
        if((this.pvalue <= pvalue) && (this.fdrqvalue <= fdrqvalue)){
            return true;
       }else{
            return false;
        }
    }

    //Getters and Settters

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public int getGsSize() {
        return gsSize;
    }

    public void setGsSize(int gsSize) {
        this.gsSize = gsSize;
    }

    public double getES() {
        return ES;
    }

    public void setES(double ES) {
        this.ES = ES;
    }

    public double getNES() {
        return NES;
    }

    public void setNES(double NES) {
        this.NES = NES;
    }

    public double getPvalue() {
        return pvalue;
    }

    public void setPvalue(double pvalue) {
        this.pvalue = pvalue;
    }

    public double getFdrqvalue() {
        return fdrqvalue;
    }

    public void setFdrqvalue(double fdrqvalue) {
        this.fdrqvalue = fdrqvalue;
    }

    public double getFwerqvalue() {
        return fwerqvalue;
    }

    public void setFwerqvalue(double fwerqvalue) {
        this.fwerqvalue = fwerqvalue;
    }

    public int getRankAtMax() {
        return rankAtMax;
    }

    public void setRankAtMax(int rankAtMax) {
        this.rankAtMax = rankAtMax;
    }

    public double getScoreAtMax() {
        return scoreAtMax;
    }

    public void setScoreAtMax(double scoreAtMax) {
        this.scoreAtMax = scoreAtMax;
    }

    public String toString(){

        return Name + "\t" + gsSize + "\t" + ES + "\t" + NES +"\t"+pvalue + "\t" + fdrqvalue + "\t" + fwerqvalue + "\t"
                + rankAtMax + "\t" + scoreAtMax ;
    }

    public String getSource() {
        return Source;
    }

    public void setSource(String source) {
        Source = source;
    }

    private void setSource(){
        //if we can tokenize the name by "%" then set the source to the second item in the name
        //if you can split the name using '|', take the second token to be the gene set type
        String[] name_tokens = Name.split("%");
        if(name_tokens.length > 1)
            this.Source = name_tokens[1];
    }
}
