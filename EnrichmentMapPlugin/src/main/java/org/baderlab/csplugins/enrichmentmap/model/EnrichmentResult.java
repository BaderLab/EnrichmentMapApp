package org.baderlab.csplugins.enrichmentmap.model;

import java.util.Optional;

/**
 * Parent class to generic and GSEA results An enrichment must consist of
 * minimally a name, description, pvalue
 */
public class EnrichmentResult {

	//name of geneset this enrichment is associated with 
	private final String name;

	//the description of the geneset
	private final String desc;

	//p-value associated with the enrichment
	private final double pvalue;
	
	//gene set size
	private int gsSize;

	// source of the enrichment map 
	// if the enrichment was done using the Baderlab gmt files the source for each
	// geneset is encoded in the geneset name.  Track that source for displaying on the network
	private final Optional<String> source;

	public EnrichmentResult(String name, String desc, double pvalue, int gsSize) {
		this.name = name;
		this.desc = desc;
		this.pvalue = pvalue;
		this.gsSize = gsSize;
		
		//if we can tokenize the name by "%" then set the source to the second item in the name
		//if you can split the name using '|', take the second token to be the gene set type
		String[] name_tokens = name.split("%");
		if(name_tokens.length > 1)
			this.source = Optional.of(name_tokens[1]);
		else
			this.source = Optional.empty();
	}

	/**
	 * This method is meant to be overridden in subclasses. The only reason its not
	 * abstract is because in JUnits its convenient to just create an instance of this class.
	 */
	public boolean isGeneSetOfInterest(EnrichmentResultFilterParams params) {
		return true;
	}
	
	//Method to print out into the session file for future loads.
	//tab delimited string with all the variables of the enrichment.
	public String toString() {
		return name + "\t" + pvalue + "\n";
	}

	//Getters and Setters

	public String getName() {
		return name;
	}


	public double getPvalue() {
		return pvalue;
	}

	public String getDescription() {
		return desc;
	}

	public int getGsSize() {
		return gsSize;
	}
	
	public void setGsSize(int size) {
		this.gsSize = size;
	}
	
	public Optional<String> getSource() {
		return source;
	}
}
