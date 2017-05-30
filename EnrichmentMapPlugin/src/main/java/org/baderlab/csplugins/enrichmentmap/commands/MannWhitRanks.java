package org.baderlab.csplugins.enrichmentmap.commands;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.ImmutableMap;

public class MannWhitRanks {

	public static final String DESCRIPTION = "When using Mann-Whitney allows to specify which rank file to use with each dataset."
			+ " Example usage \"DataSetName1:RankFile1,DataSetName2:RankFile2\"";
	
	
	private Map<String,String> dataSetToRankFile = new HashMap<>();
	
	public MannWhitRanks(Map<String,String> dataSetToRankFile) {
		this.dataSetToRankFile = ImmutableMap.copyOf(dataSetToRankFile);
	}
	
	public MannWhitRanks() {
		this.dataSetToRankFile = Collections.emptyMap();
	}
	
	public Map<String,String> getDataSetToRankFile() {
		return dataSetToRankFile;
	}
	
	public String getRankFile(String dataSet) {
		return dataSetToRankFile.get(dataSet);
	}

	@Override
	public String toString() {
		return "MannWhitRanking [dataSetToRankFile=" + dataSetToRankFile + "]";
	}
	
	public boolean isEmpty() {
		return dataSetToRankFile.isEmpty();
	}

}
