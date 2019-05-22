package org.baderlab.csplugins.enrichmentmap.model;

import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nullable;

import org.baderlab.csplugins.enrichmentmap.model.EMDataSet.Method;

import com.google.common.base.MoreObjects;

public class DataSetParameters {

	private final String name;
	private final Method method;
	
	// one or the other of the following must be provided
	private final @Nullable DataSetFiles files;
	private final @Nullable transient TableParameters tableParameters;
	
	
	public DataSetParameters(String name, Method method, DataSetFiles files) {
		this.name = Objects.requireNonNull(name);
		this.method = Objects.requireNonNull(method);
		this.files = Objects.requireNonNull(files);
		this.tableParameters = null;
	}
	
	public DataSetParameters(String name, Method method, TableParameters tableParameters) {
		this.name = Objects.requireNonNull(name);
		this.method = Objects.requireNonNull(method);
		this.files = new DataSetFiles();
		this.tableParameters = Objects.requireNonNull(tableParameters);
	}

	public String getName() {
		return name;
	}

	public DataSetFiles getFiles() {
		return files;
	}
	
	public Optional<TableParameters> getTableParams() {
		return Optional.ofNullable(tableParameters);
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
