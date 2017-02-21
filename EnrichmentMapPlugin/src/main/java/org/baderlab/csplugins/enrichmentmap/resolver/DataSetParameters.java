package org.baderlab.csplugins.enrichmentmap.resolver;

import org.baderlab.csplugins.enrichmentmap.model.EMDataSet.Method;
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
