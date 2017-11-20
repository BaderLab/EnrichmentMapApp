package org.baderlab.csplugins.enrichmentmap.task.postanalysis;

import java.util.Set;

import org.baderlab.csplugins.enrichmentmap.model.PostAnalysisFilterType;
import org.baderlab.csplugins.enrichmentmap.model.Ranking;
import org.baderlab.csplugins.mannwhit.MannWhitneyUTestSided;

import com.google.common.collect.Sets;

/**
 * Filters used by post-analysis.
 */
public interface FilterMetric {
	
	PostAnalysisFilterType getFilterType(); // Used for optimization, to avoid processing when the filter type is None
	
	double getCutoff();
	
	boolean passes(Set<Integer> geneSet, Set<Integer> sigSet);
	
	double computeValue(Set<Integer> geneSet, Set<Integer> sigSet) throws ArithmeticException;
	
	
	
	abstract class BaseFilterMetric implements FilterMetric {
		protected final double cutoff;
		protected final PostAnalysisFilterType type;
		
		public BaseFilterMetric(PostAnalysisFilterType type, double filter) {
			this.cutoff = filter;
			this.type = type;
		}
		
		public BaseFilterMetric(PostAnalysisFilterType type) {
			this(type, type.defaultValue);
		}
		
		public PostAnalysisFilterType getFilterType() {
			return type;
		}
		
		public double getCutoff() {
			return cutoff;
		}
	}
	
	
	class NoFilter extends BaseFilterMetric {
		
		public NoFilter() {
			super(PostAnalysisFilterType.NO_FILTER, 0.0);
		}
		
		public boolean passes(Set<Integer> geneSet, Set<Integer> sigSet) {
			return true;
		}

		public double computeValue(Set<Integer> geneSet, Set<Integer> sigSet) {
			return 0;
		}
	}
	
	
	class Percent extends BaseFilterMetric {
		
		public Percent(double filter) {
			super(PostAnalysisFilterType.PERCENT, filter);
		}
		
		public boolean passes(Set<Integer> geneSet, Set<Integer> sigSet) {
			return computeValue(geneSet, sigSet) >= (cutoff / 100.0);
		}

		public double computeValue(Set<Integer> geneSet, Set<Integer> sigSet) {
			Set<Integer> intersection = Sets.intersection(geneSet, sigSet);
			return (double) intersection.size() / (double) geneSet.size();
		}
	}
	

	class Number extends BaseFilterMetric {
		
		public Number(double filter) {
			super(PostAnalysisFilterType.NUMBER, filter);
		}
		
		public boolean passes(Set<Integer> geneSet, Set<Integer> sigSet) {
			return computeValue(geneSet, sigSet) >= cutoff;
		}
		
		public double computeValue(Set<Integer> geneSet, Set<Integer> sigSet) {
			return Sets.intersection(geneSet, sigSet).size();
		}
	}

	
	class Specific extends BaseFilterMetric {
		
		public Specific(double filter) {
			super(PostAnalysisFilterType.SPECIFIC, filter);
		}
		
		public boolean passes(Set<Integer> geneSet, Set<Integer> sigSet) {
			return computeValue(geneSet, sigSet) >= (cutoff / 100.0);
		}

		public double computeValue(Set<Integer> geneSet, Set<Integer> sigSet) {
			Set<Integer> intersection = Sets.intersection(geneSet, sigSet);
			return (double) intersection.size() / (double) sigSet.size();
		}
	}

	
	class Hypergeom extends BaseFilterMetric {

		private final int N;
		
		public Hypergeom(double filter, int N) {
			super(PostAnalysisFilterType.HYPERGEOM, filter);
			this.N = N;
		}

		public boolean passes(Set<Integer> geneSet, Set<Integer> sigSet) {
			return computeValue(geneSet, sigSet) <= cutoff;
		}

		public double computeValue(Set<Integer> geneSet, Set<Integer> sigSet) throws ArithmeticException {
			Set<Integer> intersection = Sets.intersection(geneSet, sigSet);
			// Calculate Hypergeometric pValue for Overlap
			// N: number of total genes (size of population / total number of balls)
			int n = sigSet.size(); //size of signature geneset (sample size / number of extracted balls)
			int m = geneSet.size(); //size of enrichment geneset (success Items / number of white balls in population)
			int k = intersection.size(); //size of intersection (successes /number of extracted white balls)

			if(k > 0)
				return Hypergeometric.hyperGeomPvalueSum(N, n, m, k, 0);
			else // Correct p-value of empty intersections to 1 (i.e. not significant)
				return 1.0;
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

		@Override
		public boolean passes(Set<Integer> geneSet, Set<Integer> sigSet) {
			return computeValue(geneSet, sigSet) <= cutoff;
		}

		@Override
		public double computeValue(Set<Integer> geneSet, Set<Integer> sigSet) {
			Set<Integer> intersection = Sets.intersection(geneSet, sigSet);
			Integer[] overlap_gene_ids = intersection.toArray(new Integer[0]);
			if(overlap_gene_ids.length > 0) {
				double[] overlap_gene_scores = new double[overlap_gene_ids.length];

				// Get the scores for the overlap
				for(int p = 0; p < overlap_gene_ids.length; p++) {
					overlap_gene_scores[p] = ranks.getScore(overlap_gene_ids[p]);
				}

				double[] scores = ranks.getScores();
				MannWhitneyUTestSided mann_whit = new MannWhitneyUTestSided();
				return mann_whit.mannWhitneyUTest(overlap_gene_scores, scores, type.mannWhitneyTestType());
			}
			return 1.0;
		}
	}
	
}
