package org.baderlab.csplugins.enrichmentmap.rest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.baderlab.csplugins.enrichmentmap.model.EMDataSet;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.model.GeneExpressionMatrix;

public class ExpressionDataResponse {

	private List<DataSetExpressionResponse> dataSetExpressionList;
	private List<DataSetClassResponse> dataSetClassList;
	
	public ExpressionDataResponse(EnrichmentMap map) {
		this(map, Optional.empty());
	}
	
	public ExpressionDataResponse(EnrichmentMap map, Optional<Set<String>> genes) {
		dataSetExpressionList = new ArrayList<>(map.getDataSetCount());
		
		Collection<String> keys = map.getExpressionMatrixKeys();
		for(String key : keys) {
			DataSetExpressionResponse dataSetResponse = createDataSetExpressionResponse(map, key, genes);
			if(!dataSetResponse.getExpressions().isEmpty()) {
				dataSetExpressionList.add(dataSetResponse);
			}
		}
		
		dataSetClassList = new ArrayList<>(map.getDataSetCount());
		
		for(EMDataSet dataSet : map.getDataSetList()) {
			if(dataSet.getEnrichments().getPhenotypes() != null) {
				dataSetClassList.add(new DataSetClassResponse(dataSet));
			}
		}
	}


	private static DataSetExpressionResponse createDataSetExpressionResponse(EnrichmentMap map, String key, Optional<Set<String>> genes) {
		GeneExpressionMatrix matrix = map.getExpressionMatrix(key);
		
		List<String> dataSetNames = new ArrayList<>();
		for(EMDataSet dataSet : map.getDataSetList()) {
			if(dataSet.getExpressionKey().equals(key)) {
				dataSetNames.add(dataSet.getName());
			}
		}
		
		return new DataSetExpressionResponse(map, dataSetNames, matrix, genes);
	}
	
	
	public List<DataSetExpressionResponse> getDataSetExpressionList() {
		return dataSetExpressionList;
	}
	

	public List<DataSetClassResponse> getDataSetClassList() {
		return dataSetClassList;
	}
	
}
