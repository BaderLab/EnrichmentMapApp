package org.baderlab.csplugins.enrichmentmap.view.control.io;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.baderlab.csplugins.enrichmentmap.style.ChartOptions;

public class ViewParams {

	public enum CutoffParam {
		P_VALUE, Q_VALUE;
	}
	
	private long networkViewID;
	private CutoffParam nodeCutoffParam;
	private Double pValue;
	private Double qValue;
	private Double similarityCoefficient;
	private Set<String> filteredOutDataSets;
	private ChartOptions chartOptions;
	private boolean publicationReady;
	
	public ViewParams() {
	}
	
	public ViewParams(
			long networkViewID,
			CutoffParam nodeCutoffParam,
			Double pValue,
			Double qValue,
			Double similarityCoefficient,
			Collection<String> filteredDataSets,
			ChartOptions chartOptions,
			boolean publicationReady
	) {
		this.networkViewID = networkViewID;
		this.nodeCutoffParam = nodeCutoffParam;
		this.pValue = pValue;
		this.qValue = qValue;
		this.similarityCoefficient = similarityCoefficient;
		this.filteredOutDataSets = filteredDataSets != null ? new HashSet<>(filteredDataSets) : null;
		this.chartOptions = chartOptions;
		this.publicationReady = publicationReady;
	}

	public long getNetworkViewID() {
		return networkViewID;
	}
	
	public void setNetworkViewID(long networkViewID) {
		this.networkViewID = networkViewID;
	}
	
	public CutoffParam getNodeCutoffParam() {
		return nodeCutoffParam;
	}
	
	public void setNodeCutoffParam(CutoffParam nodeCutoffParam) {
		this.nodeCutoffParam = nodeCutoffParam;
	}
	
	public Double getPValue() {
		return pValue;
	}
	
	public void setPValue(Double pValue) {
		this.pValue = pValue;
	}
	
	public Double getQValue() {
		return qValue;
	}

	public void setQValue(Double qValue) {
		this.qValue = qValue;
	}

	public Double getSimilarityCoefficient() {
		return similarityCoefficient;
	}

	public void setSimilarityCoefficient(Double similarityCoefficient) {
		this.similarityCoefficient = similarityCoefficient;
	}

	public Set<String> getFilteredOutDataSets() {
		return filteredOutDataSets;
	}
	
	public void setFilteredOutDataSets(Set<String> filteredDataSets) {
		this.filteredOutDataSets = filteredDataSets;
	}
	
	public ChartOptions getChartOptions() {
		return chartOptions;
	}
	
	public void setChartOptions(ChartOptions chartOptions) {
		this.chartOptions = chartOptions;
	}
	
	public boolean isPublicationReady() {
		return publicationReady;
	}
	
	public void setPublicationReady(boolean publicationReady) {
		this.publicationReady = publicationReady;
	}

	@Override
	public int hashCode() {
		final int prime = 23;
		int result = 3;
		result = prime * result + (int) (networkViewID ^ (networkViewID >>> 32));
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
		ViewParams other = (ViewParams) obj;
		if (networkViewID != other.networkViewID)
			return false;
		return true;
	}
}
