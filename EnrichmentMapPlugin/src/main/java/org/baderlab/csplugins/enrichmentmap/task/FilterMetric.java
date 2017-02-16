package org.baderlab.csplugins.enrichmentmap.task;

import java.util.Set;

import org.baderlab.csplugins.enrichmentmap.model.PostAnalysisFilterType;
import org.baderlab.csplugins.enrichmentmap.model.Ranking;
import org.baderlab.csplugins.mannwhit.MannWhitneyUTestSided;

/**
 * Filters used by post-analysis.
 */
public interface FilterMetric {
	
	boolean match(int mapGenesetSize, Set<Integer> intersection, Set<Integer> signatureSet);
	
	PostAnalysisFilterType getFilterType(); // Used for optimization, to avoid processing when the filter type is None
	
	
	abstract class BaseFilterMetric implements FilterMetric {
		protected final double filter;
		protected final PostAnalysisFilterType type;
		
		public BaseFilterMetric(PostAnalysisFilterType type, double filter) {
			this.filter = filter;
			this.type = type;
		}
		
		public PostAnalysisFilterType getFilterType() {
			return type;
		}
	}
	
	
	class None extends BaseFilterMetric {
		
		public None() {
			super(PostAnalysisFilterType.NO_FILTER, 0.0);
		}
		
		public boolean match(int mapGenesetSize, Set<Integer> intersection, Set<Integer> signatureSet) {
			return true;
		}
	}
	
	
	class Percent extends BaseFilterMetric {
		
		public Percent(double filter) {
			super(PostAnalysisFilterType.PERCENT, filter);
		}
		
		public boolean match(int mapGenesetSize, Set<Integer> intersection, Set<Integer> signatureSet) {
			double relative_per = (double) intersection.size() / (double) mapGenesetSize;
			return relative_per >= filter / 100.0;
		}
	}
	

	class Number extends BaseFilterMetric {
		
		public Number(double filter) {
			super(PostAnalysisFilterType.NUMBER, filter);
		}
		
		public boolean match(int mapGenesetSize, Set<Integer> intersection, Set<Integer> signatureSet) {
			return intersection.size() >= filter;
		}
	}

	
	class Specific extends BaseFilterMetric {
		
		public Specific(double filter) {
			super(PostAnalysisFilterType.SPECIFIC, filter);
		}
		
		public boolean match(int mapGenesetSize, Set<Integer> intersection, Set<Integer> signatureSet) {
			double relative_per = (double) intersection.size() / (double) signatureSet.size();
			return relative_per >= filter / 100.0;
		}
	}

	
	class Hypergeom extends BaseFilterMetric {

		private final int N;
		
		public Hypergeom(double filter, int N) {
			super(PostAnalysisFilterType.HYPERGEOM, filter);
			this.N = N;
		}

		public boolean match(int mapGenesetSize, Set<Integer> intersection, Set<Integer> signatureSet) {
			// Calculate Hypergeometric pValue for Overlap
			// N: number of total genes (size of population / total number of balls)
			int n = signatureSet.size(); //size of signature geneset (sample size / number of extracted balls)
			int m = mapGenesetSize; //size of enrichment geneset (success Items / number of white balls in population)
			int k = intersection.size(); //size of intersection (successes /number of extracted white balls)

			double hyperPval;
			try {
				if(k > 0)
					hyperPval = Hypergeometric.hyperGeomPvalueSum(N, n, m, k, 0);
				else // Correct p-value of empty intersections to 1 (i.e. not significant)
					hyperPval = 1.0;
			} catch(ArithmeticException e) {
				e.printStackTrace();
				return false;
			}

			return hyperPval <= filter;
		}
	}

	
	class MannWhit extends BaseFilterMetric {

		private final Ranking ranks;
		
		public MannWhit(double filter, Ranking ranks, PostAnalysisFilterType type) {
			super(type, filter);
			if(!type.isMannWhitney())
				throw new IllegalArgumentException("FilterType is not Mann Whitney: " + type);
			this.ranks = ranks;
		}

		public boolean match(int mapGenesetSize, Set<Integer> intersection, Set<Integer> signatureSet) {
			// Calculate Mann-Whitney U pValue for Overlap
			Integer[] overlap_gene_ids = intersection.toArray(new Integer[0]);
			if(overlap_gene_ids.length > 0) {
				double[] overlap_gene_scores = new double[overlap_gene_ids.length];

				// Get the scores for the overlap
				for(int p = 0; p < overlap_gene_ids.length; p++) {
					overlap_gene_scores[p] = ranks.getScore(overlap_gene_ids[p]);
				}

				double[] scores = ranks.getScores();
				MannWhitneyUTestSided mann_whit = new MannWhitneyUTestSided();
				double mannPval = mann_whit.mannWhitneyUTest(overlap_gene_scores, scores, type.mannWhitneyTestType());
				if(mannPval <= filter) {
					return true;
				}
			}
			return false;
		}
	}
	
}
