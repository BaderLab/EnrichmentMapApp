package org.baderlab.csplugins.enrichmentmap.view.control.io;

import org.baderlab.csplugins.enrichmentmap.style.ChartOptions;

public class ViewParams {

	private long networkViewID;
	private ChartOptions chartOptions;
	private boolean publicationReady;
	
	public ViewParams() {
	}
	
	public ViewParams(long networkViewID, ChartOptions chartOptions) {
		this.networkViewID = networkViewID;
		this.chartOptions = chartOptions;
	}

	public long getNetworkViewID() {
		return networkViewID;
	}
	
	public void setNetworkViewID(long networkViewID) {
		this.networkViewID = networkViewID;
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
