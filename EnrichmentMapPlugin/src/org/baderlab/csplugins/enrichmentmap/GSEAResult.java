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
 */
public class GSEAResult extends EnrichmentResult{

    private String Name;
    private int gsSize;
    private double ES;
    private double NES;
    private double pvalue;
    private double fdrqvalue;
    private double fwerqvalue;

    public GSEAResult(String name, int size, double ES, double NES, double pvalue, double fdrqvalue, double fwerqvalue) {
        Name = name;
        this.gsSize = size;
        this.ES = ES;
        this.NES = NES;
        this.pvalue = pvalue;
        this.fdrqvalue = fdrqvalue;
        this.fwerqvalue = fwerqvalue;
    }

    public GSEAResult(String[] tokens){
       if(tokens.length != 8)
        return;

        this.Name = tokens[1];
        this.gsSize = Integer.parseInt(tokens[2]);
        this.ES = Double.parseDouble(tokens[3]);
        this.NES = Double.parseDouble(tokens[4]);
        this.pvalue = Double.parseDouble(tokens[5]);
        this.fdrqvalue = Double.parseDouble(tokens[6]);
        this.fwerqvalue = Double.parseDouble(tokens[7]);
    }

    public boolean geneSetOfInterest(double pvalue, double fdrqvalue){
        if((this.pvalue <= pvalue) && (this.fdrqvalue <= fdrqvalue)){
            return true;
       }else{
            return false;
        }
    }

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

    public String toString(){

        return Name + "\t" + gsSize + "\t" + ES + "\t" + NES +"\t"+pvalue + "\t" + fdrqvalue + "\t" + fwerqvalue;
    }
}
