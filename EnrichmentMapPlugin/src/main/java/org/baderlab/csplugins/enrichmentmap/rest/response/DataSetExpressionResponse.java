package org.baderlab.csplugins.enrichmentmap.rest.response;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.model.GeneExpressionMatrix;

public class DataSetExpressionResponse {

	private List<String> dataSets;
	private int numConditions;
	private List<String> columnNames;
	private int expressionUniverse;
	private List<GeneExpressionResponse> expressions;
	
	public DataSetExpressionResponse(EnrichmentMap map, List<String> dataSets, GeneExpressionMatrix matrix, Optional<Set<String>> genes) {
		this.dataSets = dataSets;
		
		Predicate<String> geneFilter = genes.isPresent() ? genes.get()::contains : s -> true;
		
		numConditions = matrix.getNumConditions() - 2;
		String[] colsWithName = matrix.getColumnNames();
		columnNames = Arrays.asList(colsWithName).subList(2, colsWithName.length);
		expressionUniverse = matrix.getExpressionUniverse();
		
		expressions = new ArrayList<>();
		
		matrix.getExpressionMatrix().forEach((hash, geneExpression) -> {
			String geneName = map.getGeneFromHashKey(hash);
			if(geneFilter.test(geneName)) {
				float[] values = geneExpression.getExpression();
				expressions.add(new GeneExpressionResponse(geneName, values));
			}
		});
	}

	public List<String> getDataSets() {
		return dataSets;
	}
	
	public List<GeneExpressionResponse> getExpressions() {
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
