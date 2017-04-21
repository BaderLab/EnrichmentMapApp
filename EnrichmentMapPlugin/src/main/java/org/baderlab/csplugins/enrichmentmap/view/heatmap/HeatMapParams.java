package org.baderlab.csplugins.enrichmentmap.view.heatmap;

import org.baderlab.csplugins.brainlib.DistanceMetric;
import org.baderlab.csplugins.enrichmentmap.task.cluster.CosineDistance;
import org.baderlab.csplugins.enrichmentmap.task.cluster.EuclideanDistance;
import org.baderlab.csplugins.enrichmentmap.task.cluster.PearsonCorrelation;

public class HeatMapParams {
	
	public static enum Transform {
		AS_IS, 
		ROW_NORMALIZE, 
		LOG_TRANSFORM,
		COMPRESS_MEDIAN,
		COMPRESS_MAX,
		COMPRESS_MIN;
		
		public boolean isCompress() {
			switch(this) {
			case COMPRESS_MAX: 
			case COMPRESS_MEDIAN:
			case COMPRESS_MIN: 
				return true;
			default: 
				return false;
			}
		}
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
	private final String rankingOptionName; // combo index 
	
	
	private HeatMapParams(Builder builder) {
		this.transform = builder.transform;
		this.operator = builder.operator;
		this.distanceMetric = builder.distanceMetric;
		this.showValues = builder.showValues;
		this.rankingOptionName = builder.rankingOptionName;
	}
	
	
	public static HeatMapParams.Builder defaults() {
		return new Builder();
	}
	
	
	public static class Builder {
		private Transform transform = Transform.AS_IS;
		private Operator operator = Operator.UNION;
		private Distance distanceMetric = Distance.EUCLIDEAN;
		private boolean showValues = false;
		private String rankingOptionName;
		
		public Builder() { }
		
		public Builder(HeatMapParams params) {
			this.transform = params.transform;
			this.operator = params.operator;
			this.distanceMetric = params.distanceMetric;
			this.showValues = params.showValues;
			this.rankingOptionName = params.rankingOptionName;
		}
		
		public static Builder from(Builder other) {
			Builder b = new Builder();
			b.transform = other.transform;
			b.operator = other.operator;
			b.distanceMetric = other.distanceMetric;
			b.showValues = other.showValues;
			b.rankingOptionName = other.rankingOptionName;
			return b;
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
		public Builder setRankingOptionName(String rankingOptionName) {
			this.rankingOptionName = rankingOptionName;
			return this;
		}
		
		public HeatMapParams build() {
			return new HeatMapParams(this);
		}
	}


	public Transform getTransform() {
		return transform;
	}

	public String getRankingOptionName() {
		return rankingOptionName;
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
				+ ", showValues=" + showValues + ", rankingOptionName=" + rankingOptionName + "]";
	}
	
}
