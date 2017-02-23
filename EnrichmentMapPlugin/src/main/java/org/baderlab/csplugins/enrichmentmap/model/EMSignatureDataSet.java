package org.baderlab.csplugins.enrichmentmap.model;

public class EMSignatureDataSet extends AbstractDataSet {

	public EMSignatureDataSet(String name) {
		super(name);
	}
	
	@Override
	public int hashCode() {
		final int prime = 11;
		int result = 7;
		result = prime * result + ((getName() == null) ? 0 : getName().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		EMSignatureDataSet other = (EMSignatureDataSet) obj;
		if (getName() == null) {
			if (other.getName() != null)
				return false;
		} else if (!getName().equals(other.getName()))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "EMSignatureGeneSet [name=" + getName() + "]";
	}
}
