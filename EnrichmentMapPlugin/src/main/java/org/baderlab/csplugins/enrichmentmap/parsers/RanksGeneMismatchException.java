package org.baderlab.csplugins.enrichmentmap.parsers;

@SuppressWarnings("serial")
public class RanksGeneMismatchException extends RuntimeException {

	private final String ranksFileName;
	
	public RanksGeneMismatchException(String ranksFileName, String message) {
		super(message);
		this.ranksFileName = ranksFileName;
	}
	
	public String getRanksFileName() {
		return ranksFileName;
	}

}
