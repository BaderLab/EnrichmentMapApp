package org.baderlab.csplugins.enrichmentmap.task;

public class BuildDiseaseSignatureTaskResultFlags {

	public final boolean warnUserExistingEdges;
	public final boolean warnUserBypassStyle;
	
	
	public BuildDiseaseSignatureTaskResultFlags(boolean warnUserExistingEdges, boolean warnUserBypassStyle) {
		this.warnUserExistingEdges = warnUserExistingEdges;
		this.warnUserBypassStyle = warnUserBypassStyle;
	}
	
}
