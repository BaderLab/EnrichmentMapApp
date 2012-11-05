package org.baderlab.csplugins.enrichmentmap.model;

/**
 * Created by
 * User: risserlin
 * Date: Oct 14, 2009
 * Time: 10:57:32 AM
 * <p>
 * Parent class to generic and GSEA results
 * An enrichment must consist of minimally a name, description, pvalue
 */
public class EnrichmentResult {

	//name of geneset this enrichment is associated with 
	String name = "";
	
	//the description of the geneset
	String desc = "";
	
	//p-value associated with the enrichment
	double pvalue ;
	
	//source of the enrichment map 
	//* if the enrichment was done using the Baderlab gmt files the source for each
	// * geneset is encoded in the geneset name.  Track that source for displaying on the 
	// * network
	String source = "none";
	
	
	//Method to print out into the session file for future loads.
	//tab delimited string with all the variables of the enrichment.
	public String toString(){
		return name + "\t" + pvalue + "\n";
	}
	
	public void setSource(){
        //if we can tokenize the name by "%" then set the source to the second item in the name
        //if you can split the name using '|', take the second token to be the gene set type
        String[] name_tokens = name.split("%");
        if(name_tokens.length > 1)
            this.source = name_tokens[1];
    }
	
	//Getters and Setters

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    public double getPvalue() {
        return pvalue;
    }

    public void setPvalue(double pvalue) {
        this.pvalue = pvalue;
    }
    public String getDescription() {
        return desc;
    }

    public void setDescription(String description) {
        this.desc = description;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }
}
