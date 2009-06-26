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
