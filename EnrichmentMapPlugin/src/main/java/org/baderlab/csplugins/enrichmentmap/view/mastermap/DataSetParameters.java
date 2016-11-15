package org.baderlab.csplugins.enrichmentmap.view.mastermap;

import org.baderlab.csplugins.enrichmentmap.model.DataSetFiles;

public class DataSetParameters {

	private final String name;
	private final DataSetFiles files;
	
	public DataSetParameters(String name, DataSetFiles files) {
		this.name = name;
		this.files = files;
	}

	public String getName() {
		return name;
	}

	public DataSetFiles getFiles() {
		return files;
	}
	
}
