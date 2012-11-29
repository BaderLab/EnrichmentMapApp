package org.baderlab.csplugins.enrichmentmap.model;

import java.util.HashMap;


/**
 * Each enrichment analysis creates a set of enrichments and is associated with a set of parameters
 * @author risserlin
 *
 */

public class SetOfEnrichmentResults {

	//name of results (ie. Dataset1 or name specified by user)
	private String name;
	
	//the set of the enrichments
	// Hash Key = name of enriched set
	// Hash Value = the entire enrichment results containing: name, description, pvalue, fdr...
	private HashMap<String, EnrichmentResult> enrichments;

	//method
	
	//filename (GSEA has two files for the enrichments)
	private String filename1;
	private String filename2;
	
	//phenotype
	//each enrichment is associated with two phenotypes
	private String phenotype1 = "UP";
	private String phenotype2 = "DOWN";
	
	public SetOfEnrichmentResults() {
		this.enrichments = new HashMap<String, EnrichmentResult>();
	}
	
	public SetOfEnrichmentResults(HashMap<String, EnrichmentResult> enrichments) {
		this.enrichments = enrichments;
	}
	
	/*
	 * Create a set of Enrichment results on re-load
	 * Given - the dataset these enrichment results are associated with and the loaded in EM property file
	 */
	public SetOfEnrichmentResults(String ds, HashMap<String,String> props){
		this();
		if(props.containsKey(ds + "%" + this.getClass().getSimpleName() + "%name"))
			this.name = props.get(ds + "%" + this.getClass().getSimpleName() + "%name");
		if(props.containsKey(ds + "%" + this.getClass().getSimpleName() + "%filename1"))
			this.filename1 = props.get(ds + "%" + this.getClass().getSimpleName() + "%filename1");
		if(props.containsKey(ds + "%" + this.getClass().getSimpleName() + "%filename2"))
			this.filename2 = props.get(ds + "%" + this.getClass().getSimpleName() + "%filename2");
		if(props.containsKey(ds + "%" + this.getClass().getSimpleName() + "%phenotype1"))
			this.phenotype1 = props.get(ds + "%" + this.getClass().getSimpleName() + "%phenotype1");
		if(props.containsKey(ds + "%" + this.getClass().getSimpleName() + "%phenotype2"))
			this.phenotype2 = props.get(ds + "%" + this.getClass().getSimpleName() + "%phenotype2");
		
	}

	public HashMap<String, EnrichmentResult> getEnrichments() {
		return enrichments;
	}

	public void setEnrichments(HashMap<String, EnrichmentResult> enrichments) {
		this.enrichments = enrichments;
	}

	public String getPhenotype1() {
		return phenotype1;
	}

	public void setPhenotype1(String phenotype1) {
		this.phenotype1 = phenotype1;
	}

	public String getPhenotype2() {
		return phenotype2;
	}

	public void setPhenotype2(String phenotype2) {
		this.phenotype2 = phenotype2;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getFilename1() {
		return filename1;
	}

	public void setFilename1(String filename) {
		this.filename1 = filename;
	}

	public String getFilename2() {
		return filename2;
	}

	public void setFilename2(String filename2) {
		this.filename2 = filename2;
	}

	public void copy(SetOfEnrichmentResults copy){
		this.name = copy.getName();
		this.enrichments = copy.getEnrichments();
		this.phenotype1 = copy.getPhenotype1();
		this.phenotype2 = copy.getPhenotype2();
		this.filename1 = copy.getFilename1();
		this.filename2 = copy.getFilename2();
	}
	
	public String toString(String ds){
		StringBuffer paramVariables = new StringBuffer();
		paramVariables.append(ds + "%" + this.getClass().getSimpleName() + "%name\t" + name + "\n");
		paramVariables.append(ds + "%" + this.getClass().getSimpleName() + "%filename1\t" + filename1 + "\n");
		paramVariables.append(ds + "%" + this.getClass().getSimpleName() + "%filename2\t" + filename2 + "\n");
		paramVariables.append(ds + "%" + this.getClass().getSimpleName() + "%phenotype1\t" + phenotype1  + "\n");
        paramVariables.append(ds + "%" + this.getClass().getSimpleName() + "%phenotype2\t" + phenotype2   + "\n");
        
        return paramVariables.toString();
        
	}
	
	
	
}
