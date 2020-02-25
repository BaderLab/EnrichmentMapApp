package org.baderlab.csplugins.enrichmentmap.model;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nullable;

import org.baderlab.csplugins.enrichmentmap.model.EMDataSet.Method;

import com.google.common.base.MoreObjects;

public class DataSetParameters {

	private final String name;
	private final Method method;
	
	// one of the following must be provided
	// This is a hack, there should really be different subclasses for different parameter
	// types, but for the sake of compatibility with the session serialization this is easier.
	private final @Nullable DataSetFiles files;
	private final @Nullable transient TableParameters tableParameters;
	private final @Nullable transient GenemaniaParameters genemaniaParameters;
	
	
	public DataSetParameters(String name, Method method, DataSetFiles files) {
		this.name = Objects.requireNonNull(name);
		this.method = Objects.requireNonNull(method);
		this.files = Objects.requireNonNull(files);
		this.tableParameters = null;
		this.genemaniaParameters = null;
	}
	
	public DataSetParameters(String name, Method method, TableParameters tableParameters) {
		this.name = Objects.requireNonNull(name);
		this.method = Objects.requireNonNull(method);
		this.files = new DataSetFiles();
		this.tableParameters = Objects.requireNonNull(tableParameters);
		this.genemaniaParameters = null;
	}
	
	public DataSetParameters(String name, GenemaniaParameters genemaniaParameters) {
		this.name = Objects.requireNonNull(name);
		this.method = Method.Generic;
		this.files = new DataSetFiles();
		this.tableParameters = null;
		this.genemaniaParameters = Objects.requireNonNull(genemaniaParameters);
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
	
	public Optional<GenemaniaParameters> getGenemaniaParams() {
		return Optional.ofNullable(genemaniaParameters);
	}
	
	public Method getMethod() {
		return method;
	}
	
	public List<Path> getFilePaths() {
		List<Path> paths = new ArrayList<>();
		if(files.getGMTFileName() != null)
			paths.add(Paths.get(files.getGMTFileName()));
		if(files.getEnrichmentFileName1() != null)
			paths.add(Paths.get(files.getEnrichmentFileName1()));
		if(files.getEnrichmentFileName2() != null)
			paths.add(Paths.get(files.getEnrichmentFileName2()));
		if(files.getExpressionFileName() != null)
			paths.add(Paths.get(files.getExpressionFileName()));
		if(files.getRankedFile() != null)
			paths.add(Paths.get(files.getRankedFile()));
		if(files.getClassFile() != null)
			paths.add(Paths.get(files.getClassFile()));
		return paths;
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
