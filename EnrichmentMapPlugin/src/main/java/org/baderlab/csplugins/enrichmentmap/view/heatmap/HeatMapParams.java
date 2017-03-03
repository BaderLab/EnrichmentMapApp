package org.baderlab.csplugins.enrichmentmap.view.heatmap;

import org.baderlab.csplugins.brainlib.DistanceMetric;
import org.baderlab.csplugins.enrichmentmap.task.cluster.CosineDistance;
import org.baderlab.csplugins.enrichmentmap.task.cluster.EuclideanDistance;
import org.baderlab.csplugins.enrichmentmap.task.cluster.PearsonCorrelation;

public class HeatMapParams {
	
	public static enum Transform {
		AS_IS, 
		ROW_NORMALIZE, 
		LOG_TRANSFORM
	}
	
	public static enum Operator {
		UNION,
		INTERSECTION
	}
	
	public static enum Distance {
		COSINE,
		EUCLIDEAN,
		PEARSON;
		
		public DistanceMetric getMetric() {
			switch(this) {
				default:
				case COSINE:    return new CosineDistance();
				case EUCLIDEAN: return new EuclideanDistance();
				case PEARSON:   return new PearsonCorrelation();
			}
		}
	}
	
	private final Transform transform;
	private final Operator operator;
	private final Distance distanceMetric;
	private final boolean showValues;
	private final int sortIndex; // combo index 
	
	
	public HeatMapParams(Transform transform, Operator operator, int sortIndex, Distance distanceMetric, boolean showValues) {
		this.transform = transform;
		this.operator = operator;
		this.distanceMetric = distanceMetric;
		this.showValues = showValues;
		this.sortIndex = sortIndex;
	}
	
	
	public static HeatMapParams defaults() {
		return new Builder().build();
	}
	
	
	public static class Builder {
		private Transform transform = Transform.AS_IS;
		private Operator operator = Operator.UNION;
		private Distance distanceMetric = Distance.EUCLIDEAN;
		private boolean showValues = false;
		private int sortIndex = 0;
		
		public Builder() { }
		
		public Builder(HeatMapParams params) {
			this.transform = params.transform;
			this.operator = params.operator;
			this.distanceMetric = params.distanceMetric;
			this.showValues = params.showValues;
			this.sortIndex = params.sortIndex;
		}
		
		public Builder setTransform(Transform transform) {
			this.transform = transform;
			return this;
		}
		public Builder setOperator(Operator operator) {
			this.operator = operator;
			return this;
		}
		public Builder setDistanceMetric(Distance distanceMetric) {
			this.distanceMetric = distanceMetric;
			return this;
		}
		public Builder setShowValues(boolean showValues) {
			this.showValues = showValues;
			return this;
		}
		public Builder setSortIndex(int sortIndex) {
			this.sortIndex = sortIndex;
			return this;
		}
		
		public HeatMapParams build() {
			return new HeatMapParams(transform, operator, sortIndex, distanceMetric, showValues);
		}
	}


	public Transform getTransform() {
		return transform;
	}

	public int getSortIndex() {
		return sortIndex;
	}
	
	public Operator getOperator() {
		return operator;
	}

	public Distance getDistanceMetric() {
		return distanceMetric;
	}

	public boolean isShowValues() {
		return showValues;
	}


	@Override
	public String toString() {
		return "HeatMapParams [transform=" + transform + ", operator=" + operator + ", distanceMetric=" + distanceMetric
				+ ", showValues=" + showValues + ", sortIndex=" + sortIndex + "]";
	}

	
}
