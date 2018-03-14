package org.baderlab.csplugins.enrichmentmap.model;

public enum Compress {
	NONE,
	CLASS_MEDIAN,
	CLASS_MIN,
	CLASS_MAX,
	DATASET_MEDIAN,
	DATASET_MIN,
	DATASET_MAX;
	
	public boolean isNone() {
		return this == NONE;
	}

	public boolean isClass() {
		return this == CLASS_MEDIAN || this == CLASS_MIN || this == CLASS_MAX;
	}

	public boolean isDataSet() {
		return this == DATASET_MEDIAN || this == DATASET_MIN || this == DATASET_MAX;
	}

	public boolean sameStructure(Compress other) {
		return (this.isNone() && other.isNone()) || (this.isClass() && other.isClass())
				|| (this.isDataSet() && other.isDataSet());
	}
}
