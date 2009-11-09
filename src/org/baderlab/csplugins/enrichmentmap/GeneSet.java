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
import java.util.BitSet;

/**
 * Created by
 * User: risserlin
 * Date: Jan 8, 2009
 * Time: 11:32:40 AM
 * <p>
 * Class representing Geneset Object <br>
 * Each Geneset consists of: <br>
 * Name <br>
 * Description <br>
 * A list of genes in the geneset (represented using a HashSet) - as genes are read in they are
 * converted into an integer and stored in global unique hashmap in the enrichment map paramters.  any subsequent
 * use of the gene is stored as its integer hashkey.
 */

public class GeneSet {

    //Gene set name
    private String Name;
    //Gene set description
    private String Description;
    //genes associated with this gene set
    private HashSet<Integer> genes = null;


    /**
     * Class Constructor - creates gene set with a specified name and description with an empty
     * list of genes.
     *
     * @param name - gene set name
     * @param descrip - gene set description
     */
    public GeneSet(String name, String descrip) {
        this.Name = name;
        this.Description = descrip;

        genes = new HashSet<Integer>();

    }

    /**
     * Class constructor - parse the string tokenized line of a session file representation of a GMT
     * file into a gene set object.  (in the original gmt file the gene set is specified followed by
     * the list of genes, in a session file the genes are converted to their hash keys.)
     *
     * @param tokens - string tokenized line for an GMT file.
     */
    public GeneSet(String[] tokens){
        this(tokens[1],tokens[2]);

        if(tokens.length<3)
            return;

        for(int i = 3; i < tokens.length;i++)
            this.genes.add(Integer.parseInt(tokens[i]));

    }

    /* Given a Hashkey
    *
    */

    /**
     * Add the gene hashkey to the set of genes
     *
     * @param gene_hashkey - a new gene hashkey to add to current geneset
     * @return true if it was successfully added, false otherwise.
     */
    public boolean addGene(int gene_hashkey){
        if(genes != null){
            return genes.add(gene_hashkey);
        }
        else{
            return false;
        }
    }

    //Getters and Setters

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getDescription() {
        return Description;
    }

    public void setDescription(String description) {
        Description = description;
    }

    public HashSet<Integer> getGenes() {
        return genes;
    }

    public void setGenes(HashSet<Integer> genes) {
        this.genes = genes;
    }


    public String toString(){
        String geneset = "";

        geneset += Name + "\t" + Description + "\t";

        for(Iterator i = genes.iterator(); i.hasNext();)
            geneset += i.next().toString() + "\t";

        return geneset;
    }


}
