package org.baderlab.csplugins.enrichmentmap.rest;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.model.GeneExpressionMatrix;

public class DataSetExpressionResponse {

	private List<String> dataSets;
	private int numConditions;
	private List<String> columnNames;
	private int expressionUniverse;
	private Map<String,float[]> expressions;
	
	public DataSetExpressionResponse(EnrichmentMap map, List<String> dataSets, GeneExpressionMatrix matrix) {
		this.dataSets = dataSets;
		
		numConditions = matrix.getNumConditions() - 2;
		String[] colsWithName = matrix.getColumnNames();
		columnNames = Arrays.asList(colsWithName).subList(2, colsWithName.length);
		expressionUniverse = matrix.getExpressionUniverse();
		
		expressions = new HashMap<>();
		
		matrix.getExpressionMatrix().forEach((hash, geneExpression) -> {
			expressions.put(map.getGeneFromHashKey(hash), geneExpression.getExpression());
		});
	}

	public List<String> getDataSets() {
		return dataSets;
	}
	
	public Map<String,float[]> getExpressions() {
		return expressions;
	}
	
	public int getNumConditions() {
		return numConditions;
	}
	
	public List<String> getColumnNames() {
		return columnNames;
	}
	
	public int getExpressionUniverse() {
		return expressionUniverse;
	}
	
}
