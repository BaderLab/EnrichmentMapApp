package org.baderlab.csplugins.enrichmentmap.view.heatmap;

import java.util.List;

import javax.swing.RowSorter.SortKey;

import org.baderlab.csplugins.brainlib.DistanceMetric;
import org.baderlab.csplugins.enrichmentmap.task.cluster.CosineDistance;
import org.baderlab.csplugins.enrichmentmap.task.cluster.EuclideanDistance;
import org.baderlab.csplugins.enrichmentmap.task.cluster.PearsonCorrelation;

public class HeatMapParams {
	
	public static enum Transform {
		AS_IS, 
		ROW_NORMALIZE, 
		LOG_TRANSFORM;
	}
	
	public static enum Compress {
		NONE,
		CLASS_MEDIAN,
		CLASS_MIN,
		CLASS_MAX,
		DATASET_MEDIAN,
		DATASET_MIN,
		DATASET_MAX;
		public boolean isNone() { return this == NONE; }
		public boolean isClass() { return this == CLASS_MEDIAN || this == CLASS_MIN || this == CLASS_MAX; }
		public boolean isDataSet() { return this == DATASET_MEDIAN || this == DATASET_MIN || this == DATASET_MAX; }
		public boolean sameStructure(Compress other) {
			return (this.isNone() && other.isNone()) 
				|| (this.isClass() && other.isClass()) 
				|| (this.isDataSet() && other.isDataSet());
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
	private final Compress compress;
	private final Operator operator;
	private final Distance distanceMetric;
	private final boolean showValues;
	private final String rankingOptionName; // combo index 
	private final List<? extends SortKey> sortKeys;
	
	
	private HeatMapParams(Builder builder) {
		this.transform = builder.transform;
		this.compress = builder.compress;
		this.operator = builder.operator;
		this.distanceMetric = builder.distanceMetric;
		this.showValues = builder.showValues;
		this.rankingOptionName = builder.rankingOptionName;
		this.sortKeys = builder.sortKeys;
	}
	
	
	public static class Builder {
		private Transform transform = Transform.AS_IS;
		private Compress compress = Compress.NONE;
		private Operator operator = Operator.UNION;
		private Distance distanceMetric = Distance.EUCLIDEAN;
		private boolean showValues = false;
		private String rankingOptionName;
		private List<? extends SortKey> sortKeys;
		
		public Builder() { }
		
		public Builder(HeatMapParams other) {
			this.transform = other.transform;
			this.compress = other.compress;
			this.operator = other.operator;
			this.distanceMetric = other.distanceMetric;
			this.showValues = other.showValues;
			this.rankingOptionName = other.rankingOptionName;
			this.sortKeys = other.sortKeys;
		}
		
		public Builder setTransform(Transform transform) {
			this.transform = transform;
			return this;
		}
		public Builder setCompress(Compress compress) {
			this.compress = compress;
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
		public Builder setSortKeys(List<? extends SortKey> sortKeys) {
			this.sortKeys = sortKeys;
			return this;
		}
		
		public HeatMapParams build() {
			return new HeatMapParams(this);
		}
	}


	public Transform getTransform() {
		return transform;
	}
	
	public Compress getCompress() {
		return compress;
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
	
	public List<? extends SortKey> getSortKeys() {
		return sortKeys;
	}
	
}
