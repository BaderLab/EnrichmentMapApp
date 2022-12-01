package org.baderlab.csplugins.enrichmentmap.task;

@SuppressWarnings("serial")
public class UnsortedRanksException extends RuntimeException {

	private final String ranksFileName;
	
	public UnsortedRanksException(String message, String ranksFileName) {
		super(message);
		this.ranksFileName = ranksFileName;
	}
	
	public String getRanksFileName() {
		return ranksFileName;
	}

}
