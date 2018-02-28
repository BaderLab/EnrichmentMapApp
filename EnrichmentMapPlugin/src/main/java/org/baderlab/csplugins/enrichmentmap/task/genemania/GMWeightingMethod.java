package org.baderlab.csplugins.enrichmentmap.task.genemania;

public enum GMWeightingMethod {
	
	AUTOMATIC_SELECT("automatic"),
	AUTOMATIC("query gene-based"),
	BP("GO biological process-based"),
	MF("GO molecular function-based"),
	CC("GO cellular component-based"),
	AVERAGE("equal (by network)"),
	AVERAGE_CATEGORY("equal (by data type)");

	private final String description;

	GMWeightingMethod(String description) {
		this.description = description;
	}

	@Override
	public String toString() {
		return description;
	}
}
