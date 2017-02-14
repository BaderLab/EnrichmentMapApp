package org.baderlab.csplugins.enrichmentmap.view.expression;

public class ExpressionViewerParams {
	
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
	
	
	public ExpressionViewerParams(Transform transform) {
		this.transform = transform;
	}
	
	public Transform getTransform() {
		return transform;
	}

}
