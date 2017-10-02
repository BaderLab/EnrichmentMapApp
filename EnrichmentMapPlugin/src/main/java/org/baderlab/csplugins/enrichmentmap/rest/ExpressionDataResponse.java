package org.baderlab.csplugins.enrichmentmap.rest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.baderlab.csplugins.enrichmentmap.model.EMDataSet;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.model.GeneExpressionMatrix;

public class ExpressionDataResponse {

	private List<DataSetExpressionResponse> dataSetExpressionList;
	
	public ExpressionDataResponse(EnrichmentMap map) {
		dataSetExpressionList = new ArrayList<>(map.getDataSetCount());
		
		Collection<String> keys = map.getExpressionMatrixKeys();
		for(String key : keys) {
			DataSetExpressionResponse dataSetResponse = createDataSetExpressionResponse(map, key);
			dataSetExpressionList.add(dataSetResponse);
		}
	}

	private static DataSetExpressionResponse createDataSetExpressionResponse(EnrichmentMap map, String key) {
		GeneExpressionMatrix matrix = map.getExpressionMatrix(key);
		
		List<String> dataSetNames = new ArrayList<>();
		for(EMDataSet dataSet : map.getDataSetList()) {
			if(dataSet.getExpressionKey().equals(key)) {
				dataSetNames.add(dataSet.getName());
			}
		}
		
		return new DataSetExpressionResponse(map, dataSetNames, matrix);
	}
	
	public List<DataSetExpressionResponse> getDataSetExpressionList() {
		return dataSetExpressionList;
	}

}
