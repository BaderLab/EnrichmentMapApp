package org.baderlab.csplugins.enrichmentmap.model;

import java.text.Collator;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public abstract class AbstractDataSet implements Comparable<AbstractDataSet> {

	private final String name;
	private final Set<Long> nodeSuids = new HashSet<>();
	private final Set<Long> edgeSuids = new HashSet<>();
	
	/** EnrichmentMap only creates nodes for these genes. */
	private SetOfGeneSets geneSetsOfInterest = new SetOfGeneSets();
	
	private static final Collator collator = Collator.getInstance();
	private final Object lock = new Object();
	
	protected AbstractDataSet(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public Set<Long> getNodeSuids() {
		synchronized (lock) {
			return Collections.unmodifiableSet(nodeSuids);
		}
	}
	
	public void setNodeSuids(Set<Long> newValue) {
		synchronized (lock) {
			nodeSuids.clear();
			nodeSuids.addAll(newValue);
		}
	}
	
	public void addNodeSuid(Long suid) {
		synchronized (lock) {
			nodeSuids.add(suid);
		}
	}
	
	public void clearNodeSuids() {
		synchronized (lock) {
			nodeSuids.clear();
		}
	}
	
	public Set<Long> getEdgeSuids() {
		synchronized (lock) {
			return Collections.unmodifiableSet(edgeSuids);
		}
	}
	
	public void setEdgeSuids(Set<Long> newValue) {
		synchronized (lock) {
			edgeSuids.clear();
			edgeSuids.addAll(newValue);
		}
	}
	
	public void addEdgeSuid(Long suid) {
		synchronized (lock) {
			edgeSuids.add(suid);
		}
	}
	
	public void clearEdgeSuids() {
		synchronized (lock) {
			edgeSuids.clear();
		}
	}
	
	public SetOfGeneSets getGeneSetsOfInterest() {
		return geneSetsOfInterest;
	}

	public void setGeneSetsOfInterest(SetOfGeneSets geneSetsOfInterest) {
		this.geneSetsOfInterest = geneSetsOfInterest;
	}
		
	@Override
	public int compareTo(AbstractDataSet other) {
		return collator.compare(getName(), other.getName());
	}
}
