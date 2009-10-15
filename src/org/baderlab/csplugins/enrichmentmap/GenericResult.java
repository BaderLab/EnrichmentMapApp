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
 * Date: Jan 28, 2009
 * Time: 3:25:51 PM
 * <p>
 * Class representing a generic enrichment result.
 */
public class GenericResult extends EnrichmentResult{

    //minimum requirement of a generic enrichment results
    private String name = "";
    private String description = "";
    private double pvalue = 1.0;
    private int gsSize = 0;

    //optional parameters
    private double fdrqvalue = 1.0;

    //make the generic file synonamous with gsea result file
    //the phenotype is deduced from the sign of the ES score so create a variable to store
    //the phenotype.
    private double NES = 1.0;

    /**
     * Class constructor
     *
     * @param tokens - string tokenized line from a generic result file
     */
    public GenericResult(String[] tokens){
            //ignore the first token as it is from the hash
            this.name = tokens[1];
            this.description = tokens[2];
            this.pvalue = Double.parseDouble(tokens[3]);
            this.gsSize = Integer.parseInt(tokens[4]);
            this.fdrqvalue = Double.parseDouble(tokens[5]);
            this.NES = Double.parseDouble(tokens[6]);

    }

    /**
     * Class constructor - minimal requirements
     *
     * @param name - gene set name (enrichment result)
     * @param description - gene set description
     * @param pvalue - enrichment p-value
     * @param gssize - gene set size
     */
    public GenericResult(String name, String description, double pvalue, int gssize) {
        this.name = name;
        this.description = description;
        this.pvalue = pvalue;
        this.gsSize = gssize;
    }

    /**
     * Class constructor - minimal requirement with addition of fdr qvalue
     *
     * @param name - gene set name (enrichment result)
     * @param description - gene set description
     * @param pvalue - enrichment p-value
     * @param gssize - gene set size
     * @param fdrqvalue - enrichment fdr q-value
     */
    public GenericResult(String name, String description, double pvalue, int gssize, double fdrqvalue) {
        this.name = name;
        this.description = description;
        this.pvalue = pvalue;
        this.gsSize = gssize;
        this.fdrqvalue = fdrqvalue;
    }

    /**
     * Class constructor - minimal requirements with addition of fdr qvalue and phenotype
     *
     * @param name - gene set name (enrichment result)
     * @param description - gene set description
     * @param pvalue - enrichment p-value
     * @param gssize - gene set size
     * @param fdrqvalue - enrichment fdr q-value
     * @param phenotype - which phenotype or class is this enrichment results associated with
     */
    public GenericResult(String name, String description, double pvalue, int gssize, double fdrqvalue, double phenotype) {
         this.name = name;
        this.description = description;
        this.gsSize = gssize;
        this.pvalue = pvalue;
        this.fdrqvalue = fdrqvalue;
        this.NES = phenotype;
    }

    /**
     * Is this a gene set of interest, does its enrichment score pass the p-value threshold
     * (and optionallly does it also pass the fdr 1-value threshold)
     *
     * @param pvalue - pvalue of current gene set enrichment
     * @param fdrqvalue  - fdr q-value of current gene set enrichment
     * @param useFDR - is there a fdr q-value threshold set
     * @return whether these p-value and fdr q-value for the specified gene set enrichment fall into our gene sets of interest
     * category (i.e. do these values pass the specified thresholds)
     */
    public boolean geneSetOfInterest(double pvalue, double fdrqvalue, boolean useFDR){
        if(useFDR){
            if((this.pvalue <= pvalue) && (this.fdrqvalue <= fdrqvalue)){
                return true;
            }else{
                return false;
            }
        }
        else{
            if(this.pvalue <= pvalue){
                return true;
            }else{
                return false;
            }
        }
    }

    //Getters and Setters

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public int getGsSize() {
        return gsSize;
    }

    public void setGsSize(int gs_size) {
        this.gsSize = gs_size;
    }

    public double getNES() {
        return NES;
    }

    public void setNES(double NES) {
        this.NES = NES;
    }

    public String toString(){

        return name + "\t" + description + "\t" + pvalue + "\t" + gsSize + "\t" + fdrqvalue + "\t" + NES +"\n";
    }
}
