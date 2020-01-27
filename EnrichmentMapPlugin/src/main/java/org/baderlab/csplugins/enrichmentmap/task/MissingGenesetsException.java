package org.baderlab.csplugins.enrichmentmap.task;

import java.util.Collection;
import java.util.Collections;

@SuppressWarnings("serial")
public class MissingGenesetsException extends RuntimeException {

	private final Collection<String> missingGenesetNames;
	
	public MissingGenesetsException(String missingGenesetName) {
		super("The Geneset " + missingGenesetName + " is not found in the GMT file.");
		this.missingGenesetNames = Collections.singleton(missingGenesetName);
	}
	
	public MissingGenesetsException(Collection<String> missingGenesetNames) {
		super("There were " + missingGenesetNames.size() + " genesets not found in the GMT file.");
		this.missingGenesetNames = missingGenesetNames;
	}
	
	public Collection<String> getMissingGenesetNames() {
		return Collections.unmodifiableCollection(missingGenesetNames);
	}
}
