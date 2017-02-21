package org.baderlab.csplugins.enrichmentmap.model;

import java.text.Collator;

public abstract class AbstractDataSet implements Comparable<AbstractDataSet> {

	private final String name;
	
	private final transient Collator collator = Collator.getInstance();
	
	protected AbstractDataSet(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	@Override
	public int compareTo(AbstractDataSet other) {
		return collator.compare(getName(), other.getName());
	}
}
