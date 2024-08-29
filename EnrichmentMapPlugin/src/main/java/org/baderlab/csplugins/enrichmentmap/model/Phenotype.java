package org.baderlab.csplugins.enrichmentmap.model;

import java.util.Objects;

public class Phenotype {
	
	public static enum Type {
		POSITIVE, NEGATIVE, OTHER
	}
	
	private final EMDataSet dataset;
	private final String name;
	private final Type type;
	
	public Phenotype(EMDataSet dataset, String name, Type type) {
		this.dataset = Objects.requireNonNull(dataset);
		this.name = Objects.requireNonNull(name);
		this.type = Objects.requireNonNull(type);
	}
	
	public EMDataSet getDataSet() {
		return dataset;
	}

	public String getName() {
		return name;
	}

	public Type getType() {
		return type;
	}

	@Override
	public String toString() {
		return "Phenotype[dataset=" + dataset + ", name=" + name + ", type=" + type + "]";
	}
	
}
