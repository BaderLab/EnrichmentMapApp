package org.baderlab.csplugins.enrichmentmap.model;

import java.util.Map;

import javax.annotation.Nullable;

public class EMSignatureDataSet extends AbstractDataSet {

	// These fields were added in 3.1, sessions saved with 3.0 will not be able to restore these fields.
	// These are just to remember some of the creation parameters to show in the Creation Paramters Dialog.
	private @Nullable String source;
	private @Nullable String gmtFile;
	private @Nullable PostAnalysisFilterType type;
	private @Nullable Map<String,String> dataSetRankTestMessage;
	
	
	public EMSignatureDataSet(EnrichmentMap map, String name) {
		super(map, name);
	}
	
	
	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public String getGmtFile() {
		return gmtFile;
	}

	public void setGmtFile(String gmtFile) {
		this.gmtFile = gmtFile;
	}

	public PostAnalysisFilterType getType() {
		return type;
	}

	public void setType(PostAnalysisFilterType type) {
		this.type = type;
	}

	public Map<String, String> getDataSetRankTestMessage() {
		return dataSetRankTestMessage;
	}

	public void setDataSetRankTestMessage(Map<String, String> dataSetRankTestMessage) {
		this.dataSetRankTestMessage = dataSetRankTestMessage;
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
