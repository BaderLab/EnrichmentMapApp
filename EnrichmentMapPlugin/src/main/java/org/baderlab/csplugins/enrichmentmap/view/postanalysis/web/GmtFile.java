package org.baderlab.csplugins.enrichmentmap.view.postanalysis.web;

public class GmtFile {

	private final String filePath;
	private final int size;
	
	public GmtFile(String filePath, int size) {
		this.filePath = filePath;
		this.size = size;
	}

	public String getFilePath() {
		return filePath;
	}

	public int getSize() {
		return size;
	}

}
