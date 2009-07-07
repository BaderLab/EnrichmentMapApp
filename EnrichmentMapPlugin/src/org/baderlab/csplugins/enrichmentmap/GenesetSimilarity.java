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

import java.util.HashSet;
import java.util.Iterator;

/**
 * Created by
 * User: risserlin
 * Date: Jan 9, 2009
 * Time: 10:49:55 AM
 */
public class GenesetSimilarity {

    private String geneset1_Name;
    private String geneset2_Name;

    private double jaccard_coeffecient;

    private HashSet<Integer> overlapping_genes;

    public GenesetSimilarity(String geneset1_Name, String geneset2_Name, double jaccard_coeffecient, HashSet<Integer> overlapping_genes) {
        this.geneset1_Name = geneset1_Name;
        this.geneset2_Name = geneset2_Name;
        this.jaccard_coeffecient = jaccard_coeffecient;
        this.overlapping_genes = overlapping_genes;
    }

    //create a new object from an array of strings extracted from a file
    public GenesetSimilarity(String[] tokens){
        //make sure there is sufficient number of items in the row.
        if(tokens.length<4)
                return;

        //the first token is the hash key, don't need it in the object
        this.geneset1_Name = tokens[1];
        this.geneset2_Name = tokens[2];
        this.jaccard_coeffecient = Double.parseDouble(tokens[3]);

        this.overlapping_genes = new HashSet<Integer>();

        for(int i = 4; i<tokens.length;i++){
               this.overlapping_genes.add(Integer.parseInt(tokens[i]));
        }
    }

    public String getGeneset1_Name() {
        return geneset1_Name;
    }

    public void setGeneset1_Name(String geneset1_Name) {
        this.geneset1_Name = geneset1_Name;
    }

    public String getGeneset2_Name() {
        return geneset2_Name;
    }

    public void setGeneset2_Name(String geneset2_Name) {
        this.geneset2_Name = geneset2_Name;
    }

    public double getJaccard_coeffecient() {
        return jaccard_coeffecient;
    }

    public void setJaccard_coeffecient(double jaccard_coeffecient) {
        this.jaccard_coeffecient = jaccard_coeffecient;
    }

    public HashSet<Integer> getOverlapping_genes() {
        return overlapping_genes;
    }

    public void setOverlapping_genes(HashSet<Integer> overlapping_genes) {
        this.overlapping_genes = overlapping_genes;
    }

    public int getSizeOfOverlap(){
        return overlapping_genes.size();
    }

    public String toString(){
        String similarity = "";

        similarity += geneset1_Name + "\t" + geneset2_Name + "\t" + jaccard_coeffecient + "\t";

        for(Iterator i = overlapping_genes.iterator();i.hasNext();)
            similarity += i.next().toString() + "\t";

        return similarity;
    }
}
