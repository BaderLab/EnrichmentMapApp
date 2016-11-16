package org.baderlab.csplugins.enrichmentmap.view.mastermap;

import org.baderlab.csplugins.enrichmentmap.model.DataSet.Method;
import org.baderlab.csplugins.enrichmentmap.model.DataSetFiles;

public class DataSetParameters {

	private final String name;
	private final DataSetFiles files;
	private final Method method;
	
	public DataSetParameters(String name, Method method, DataSetFiles files) {
		this.name = name;
		this.method = method;
		this.files = files;
	}

	public String getName() {
		return name;
	}

	public DataSetFiles getFiles() {
		return files;
	}
	
	public Method getMethod() {
		return method;
	}
	
}
