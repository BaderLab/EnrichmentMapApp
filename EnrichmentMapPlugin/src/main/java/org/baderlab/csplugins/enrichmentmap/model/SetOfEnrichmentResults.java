package org.baderlab.csplugins.enrichmentmap.model;

import java.util.HashMap;
import java.util.Map;


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
	private Map<String, EnrichmentResult> enrichments;

	//phenotype
	//each enrichment is associated with two phenotypes
	private String[] phenotypes;
	private String phenotype1 = DataSetFiles.default_pheno1;
	private String phenotype2 = DataSetFiles.default_pheno2;
	
	public SetOfEnrichmentResults() {
		this.enrichments = new HashMap<>();
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
		if(props.containsKey(ds + "%" + this.getClass().getSimpleName() + "%phenotype1"))
			this.phenotype1 = props.get(ds + "%" + this.getClass().getSimpleName() + "%phenotype1");
		if(props.containsKey(ds + "%" + this.getClass().getSimpleName() + "%phenotype2"))
			this.phenotype2 = props.get(ds + "%" + this.getClass().getSimpleName() + "%phenotype2");
		
	}

	public Map<String, EnrichmentResult> getEnrichments() {
		return enrichments;
	}

	public void setEnrichments(Map<String, EnrichmentResult> enrichments) {
		this.enrichments = enrichments;
	}

	public String[] getPhenotypes() {
		return phenotypes;
	}
	
	public void setPhenotypes(String[] phenotypes) {
		this.phenotypes = phenotypes;
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

}
