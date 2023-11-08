package org.baderlab.csplugins.enrichmentmap.parsers;

@SuppressWarnings("serial")
public class RanksUnsortedException extends RuntimeException {

	private final String ranksFileName;
	
	public RanksUnsortedException(String ranksFileName, String message) {
		super(message);
		this.ranksFileName = ranksFileName;
	}
	
	public String getRanksFileName() {
		return ranksFileName;
	}

}
