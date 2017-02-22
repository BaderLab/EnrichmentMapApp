package org.baderlab.csplugins.enrichmentmap.model;

public class EMSignatureDataSet extends AbstractDataSet {

	private final GeneSet geneSet;
	
	public EMSignatureDataSet(String name, GeneSet geneSet) {
		super(name);
		this.geneSet = geneSet;
	}
	
	public GeneSet getGeneSet() {
		return geneSet;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 3;
		result = prime * result + ((geneSet == null) ? 0 : geneSet.hashCode());
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
		if (geneSet == null) {
			if (other.geneSet != null)
				return false;
		} else if (!geneSet.equals(other.geneSet))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "EMSignatureGeneSet [name=" + getName() + "]";
	}
}
