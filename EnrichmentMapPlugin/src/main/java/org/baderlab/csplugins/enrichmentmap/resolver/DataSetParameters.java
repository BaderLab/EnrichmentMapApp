package org.baderlab.csplugins.enrichmentmap.resolver;

import org.baderlab.csplugins.enrichmentmap.model.DataSetFiles;
import org.baderlab.csplugins.enrichmentmap.model.EMDataSet.Method;

import com.google.common.base.MoreObjects;

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
	
	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
			.add("name", name)
			.add("method", method)
			.add("gmt", files.getGMTFileName())
			.add("enrichments1", files.getEnrichmentFileName1())
			.add("enrichments2", files.getEnrichmentFileName2())
			.add("expressions", files.getExpressionFileName())
			.add("ranks", files.getRankedFile())
			.add("classes", files.getClassFile())
			.add("phenotype1", files.getPhenotype1())
			.add("phenotype2", files.getPhenotype2())
			.toString();
	}
	
}
