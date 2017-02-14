package org.baderlab.csplugins.enrichmentmap.view.heatmap;

public class HeatMapParams {
	
	public static enum Transform {
		AS_IS, 
		ROW_NORMALIZE, 
		LOG_TRANSFORM
	}
	
	public static enum Sort {
		RANKS, 
		COLUMN, 
		CLUSTER 
	}
	
	private final Transform transform;
	
	
	public HeatMapParams(Transform transform) {
		this.transform = transform;
	}
	
	public Transform getTransform() {
		return transform;
	}

}
