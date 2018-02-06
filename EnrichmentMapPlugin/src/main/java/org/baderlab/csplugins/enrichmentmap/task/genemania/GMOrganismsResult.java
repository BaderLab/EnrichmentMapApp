package org.baderlab.csplugins.enrichmentmap.task.genemania;

import java.io.Serializable;
import java.util.List;

public class GMOrganismsResult implements Serializable {
	
	private static final long serialVersionUID = 8454506417358350512L;
	
	private List<GMOrganism> organisms;
	
	public List<GMOrganism> getOrganisms() {
		return organisms;
	}
	
	public void setOrganisms(List<GMOrganism> organisms) {
		this.organisms = organisms;
	}
}