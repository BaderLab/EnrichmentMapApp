package org.baderlab.csplugins.enrichmentmap.parsers;

@SuppressWarnings("serial")
public class ParseGSEAEnrichmentException extends RuntimeException {
	
	private final String nonParsableToken;

	public ParseGSEAEnrichmentException(Throwable cause, String nonParsableToken) {
		super(cause);
		this.nonParsableToken = nonParsableToken;
	}

	public String getNonParseableToken() {
		return nonParsableToken;
	}
}
