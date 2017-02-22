package org.baderlab.csplugins.enrichmentmap.view.heatmap;

public class HeatMapParams {
	
	public static enum Transform {
		AS_IS, 
		ROW_NORMALIZE, 
		LOG_TRANSFORM
	}
	
//	public static enum Sort {
//		RANKS, 
//		COLUMN, 
//		CLUSTER 
//	}
	
	public static enum Operator {
		UNION,
		INTERSECTION
	}
	
	public static enum DistanceMetric {
		COSINE,
		EUCLIDEAN,
		PEARSON
	}
	
	private final Transform transform;
//	private final Sort sort;
	private final Operator operator;
	private final DistanceMetric distanceMetric;
	private final boolean showValues;
	
	
	public HeatMapParams(Transform transform, /*Sort sort,*/ Operator operator, DistanceMetric distanceMetric, boolean showValues) {
		this.transform = transform;
//		this.sort = sort;
		this.operator = operator;
		this.distanceMetric = distanceMetric;
		this.showValues = showValues;
	}
	
	
	public static HeatMapParams defaults() {
		return new Builder().build();
	}
	
	
	public static class Builder {
		private Transform transform = Transform.AS_IS;
//		private Sort sort = Sort.RANKS;
		private Operator operator = Operator.UNION;
		private DistanceMetric distanceMetric = DistanceMetric.EUCLIDEAN;
		private boolean showValues = false;
		
		public Builder() { }
		
		public Builder(HeatMapParams params) {
			this.transform = params.transform;
//			this.sort = params.sort;
			this.operator = params.operator;
			this.distanceMetric = params.distanceMetric;
			this.showValues = params.showValues;
		}
		
		public void setTransform(Transform transform) {
			this.transform = transform;
		}
//		public void setSort(Sort sort) {
//			this.sort = sort;
//		}
		public void setOperator(Operator operator) {
			this.operator = operator;
		}
		public void setDistanceMetric(DistanceMetric distanceMetric) {
			this.distanceMetric = distanceMetric;
		}
		public void setShowValues(boolean showValues) {
			this.showValues = showValues;
		}
		
		public HeatMapParams build() {
			return new HeatMapParams(transform, /*sort,*/ operator, distanceMetric, showValues);
		}
	}


	public Transform getTransform() {
		return transform;
	}

//	public Sort getSort() {
//		return sort;
//	}

	public Operator getOperator() {
		return operator;
	}

	public DistanceMetric getDistanceMetric() {
		return distanceMetric;
	}

	public boolean isShowValues() {
		return showValues;
	}

}
