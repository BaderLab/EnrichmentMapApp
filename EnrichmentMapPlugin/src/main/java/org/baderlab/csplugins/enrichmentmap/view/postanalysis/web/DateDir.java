package org.baderlab.csplugins.enrichmentmap.view.postanalysis.web;

public class DateDir {

	private String folder;
	private long timestamp;
	
	public DateDir(String folder, long timestamp) {
		this.folder = folder;
		this.timestamp = timestamp;
	}

	public String getFolder() {
		return folder;
	}

	public long getTimestamp() {
		return timestamp;
	}


}
