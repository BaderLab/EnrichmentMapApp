package org.baderlab.csplugins.enrichmentmap;

/**
 * Created by
 * User: risserlin
 * Date: Mar 24, 2010
 * Time: 9:58:07 AM
 */
 public class SignificantGene{
        private String gene;
        private Integer gene_int;
        private boolean isInt = false;

    public SignificantGene(String name){
            gene = name;
    }

    public SignificantGene(Integer name){
            isInt = true;
            gene_int = name;
    }

    public String toString(){
            if(isInt)
                return Integer.toString(gene_int);
            else
                return gene;
    }
}
