package org.baderlab.csplugins.enrichmentmap.rest.response;

import java.util.Arrays;
import java.util.List;

import org.baderlab.csplugins.enrichmentmap.model.EMDataSet;

public class DataSetClassResponse {

	private final String dataSet;
	private final List<String> classes;
	private final String phenotype1;
	private final String phenotype2;
	
	public DataSetClassResponse(EMDataSet dataSet) {
		this.dataSet = dataSet.getName();
		this.classes = Arrays.asList(dataSet.getEnrichments().getPhenotypes());
		this.phenotype1 = dataSet.getEnrichments().getPhenotype1();
		this.phenotype2 = dataSet.getEnrichments().getPhenotype2();
	}

	public String getDataSet() {
		return dataSet;
	}

	public List<String> getClasses() {
		return classes;
	}

	public String getPhenotype1() {
		return phenotype1;
	}

	public String getPhenotype2() {
		return phenotype2;
	}
	
	
	
}
