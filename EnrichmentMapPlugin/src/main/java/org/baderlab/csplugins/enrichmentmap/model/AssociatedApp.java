package org.baderlab.csplugins.enrichmentmap.model;

public enum AssociatedApp {
	GENEMANIA("GeneMANIA"),
	STRING("STRING");
	
	private final String name;

	private AssociatedApp(String name) {
		this.name = name;
	}
	
	@Override
	public String toString() {
		return name;
	}
}
