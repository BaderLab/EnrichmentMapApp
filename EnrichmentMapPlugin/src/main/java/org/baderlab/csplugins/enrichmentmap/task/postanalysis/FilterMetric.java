package org.baderlab.csplugins.enrichmentmap.task.postanalysis;

import java.util.Arrays;
import java.util.Set;

import javax.annotation.Nullable;

import org.baderlab.csplugins.enrichmentmap.model.PostAnalysisFilterType;
import org.baderlab.csplugins.enrichmentmap.model.Ranking;
import org.baderlab.csplugins.enrichmentmap.model.SignatureGenesetSimilarity;
import org.baderlab.csplugins.mannwhit.MannWhitneyMemoized;
import org.baderlab.csplugins.mannwhit.MannWhitneyTestResult;
import org.baderlab.csplugins.mannwhit.MannWhitneyUTestSided;

import com.google.common.collect.Sets;

/**
 * Filters used by post-analysis.
 */
public interface FilterMetric {
	
	PostAnalysisFilterType getFilterType(); // Used for optimization, to avoid processing when the filter type is None
	
	double getCutoff();
	
	double computeValue(Set<Integer> geneSet, Set<Integer> sigSet, @Nullable SignatureGenesetSimilarity similarity) throws ArithmeticException;
	
	boolean passes(double value);
	
	double moreSimilar(double x, double y);
	
	String getDisplayString();
	
	
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
		
		public boolean passes(double value) {
			return true;
		}
		
		public double moreSimilar(double x, double y) {
			return x;
		}

		public double computeValue(Set<Integer> geneSet, Set<Integer> sigSet, @Nullable SignatureGenesetSimilarity similarity) {
			return 0;
		}
		
		public String getDisplayString() {
			return "no filter";
		}
		
	}
	
	
	class Percent extends BaseFilterMetric {
		
		public Percent(double filter) {
			super(PostAnalysisFilterType.PERCENT, filter);
		}
		
		public boolean passes(double value) {
			return value >= (cutoff / 100.0);
		}
		
		public double moreSimilar(double x, double y) {
			return Math.max(x, y);
		}

		public double computeValue(Set<Integer> geneSet, Set<Integer> sigSet, @Nullable SignatureGenesetSimilarity similarity) {
			Set<Integer> intersection = Sets.intersection(geneSet, sigSet);
			return (double) intersection.size() / (double) geneSet.size();
		}
		
		public String getDisplayString() {
			return "(intersection size)/(gene set size) >= " + (cutoff / 100.0);
		}
	}
	

	class Number extends BaseFilterMetric {
		
		public Number(double filter) {
			super(PostAnalysisFilterType.NUMBER, filter);
		}
		
		public boolean passes(double value) {
			return value >= cutoff;
		}
		
		public double moreSimilar(double x, double y) {
			return Math.max(x, y);
		}
		
		public double computeValue(Set<Integer> geneSet, Set<Integer> sigSet, @Nullable SignatureGenesetSimilarity similarity) {
			return Sets.intersection(geneSet, sigSet).size();
		}
		
		public String getDisplayString() {
			return "intersection size >= " + cutoff;
		}
	}

	
	class Specific extends BaseFilterMetric {
		
		public Specific(double filter) {
			super(PostAnalysisFilterType.SPECIFIC, filter);
		}
		
		public boolean passes(double value) {
			return value >= (cutoff / 100.0);
		}
		
		public double moreSimilar(double x, double y) {
			return Math.max(x, y);
		}

		public double computeValue(Set<Integer> geneSet, Set<Integer> sigSet, @Nullable SignatureGenesetSimilarity similarity) {
			Set<Integer> intersection = Sets.intersection(geneSet, sigSet);
			return (double) intersection.size() / (double) sigSet.size();
		}
		
		public String getDisplayString() {
			return "(intersection size)/(signature gene set size) >= " + (cutoff / 100.0);
		}
	}

	
	class Hypergeom extends BaseFilterMetric {

		private final int u;
		
		public Hypergeom(double filter, int u) {
			super(PostAnalysisFilterType.HYPERGEOM, filter);
			this.u = u;
		}

		public boolean passes(double value) {
			return value <= cutoff;
		}

		public double moreSimilar(double x, double y) {
			return Math.min(x, y);
		}
		
		public double computeValue(Set<Integer> geneSet, Set<Integer> sigSet, @Nullable SignatureGenesetSimilarity similarity) throws ArithmeticException {
			Set<Integer> intersection = Sets.intersection(geneSet, sigSet);
			// Calculate Hypergeometric pValue for Overlap
			// u: number of total genes (size of population / total number of balls)
			int n = sigSet.size(); //size of signature geneset (sample size / number of extracted balls)
			int m = geneSet.size(); //size of enrichment geneset (success Items / number of white balls in population)
			int k = intersection.size(); //size of intersection (successes /number of extracted white balls)

			double hyperPval;
			if(k > 0)
				hyperPval = Hypergeometric.hyperGeomPvalueSum(u, n, m, k, 0);
			else // Correct p-value of empty intersections to 1 (i.e. not significant)
				hyperPval = 1.0;
			
			if(similarity != null) {
				similarity.setHypergeomPValue(hyperPval);
				similarity.setHypergeomU(u);
				similarity.setHypergeomN(n);
				similarity.setHypergeomM(m);
				similarity.setHypergeomK(k);
			}
			
			return hyperPval;
		}
		
		public String getDisplayString() {
			return "universe size = " + u;
		}
	}

	
	class MannWhit extends BaseFilterMetric {

		private final String rankingName;
		private final @Nullable Ranking ranks;
		private final MannWhitneyMemoized mannWhitneyCache = new MannWhitneyMemoized();

		
		public MannWhit(PostAnalysisFilterType type, double filter, String rankingName, Ranking ranks) {
			super(type, filter);
			if(!type.isMannWhitney())
				throw new IllegalArgumentException("FilterType is not Mann Whitney: " + type);
			this.ranks = ranks;
			this.rankingName = rankingName;
		}

		public boolean passes(double value) {
			return value <= cutoff;
		}

		public double moreSimilar(double x, double y) {
			return Math.min(x, y);
		}
		
		public double computeValue(Set<Integer> geneSet, Set<Integer> sigSet, @Nullable SignatureGenesetSimilarity similarity) {
			Set<Integer> intersection = Sets.intersection(geneSet, sigSet);
			int size = intersection.size();
			if(ranks == null || ranks.isEmpty() || size == 0) {
				if(similarity != null) {
					similarity.setMannWhitPValueTwoSided(1.0); // avoid NoDataException
					similarity.setMannWhitPValueGreater(1.0);
					similarity.setMannWhitPValueLess(1.0);
					similarity.setMannWhitMissingRanks(true);
				}
				return 1.0;
			}
			
			Integer[] overlapGeneIds = intersection.toArray(new Integer[size]);
			double[] overlapGeneScores = new double[overlapGeneIds.length];
			
			int j = 0;
			for (Integer geneId : overlapGeneIds) {
				Double score = ranks.getScore(geneId);
				if (score != null) {
					overlapGeneScores[j++] = score; // unbox
				}
			}
	
			overlapGeneScores = Arrays.copyOf(overlapGeneScores, j);
			double[] scores = ranks.getScores();
			
			if(similarity == null) {
				MannWhitneyUTestSided mann_whit = new MannWhitneyUTestSided();
				return mann_whit.mannWhitneyUTest(overlapGeneScores, scores, type.mannWhitneyTestType());
			} 
			else {
				MannWhitneyTestResult result = mannWhitneyCache.mannWhitneyUTestBatch(overlapGeneScores, scores);
				similarity.setMannWhitPValueTwoSided(result.twoSided);
				similarity.setMannWhitPValueGreater(result.greater);
				similarity.setMannWhitPValueLess(result.less);
				
				switch(type) {
					default:
					case MANN_WHIT_TWO_SIDED: return result.twoSided;
					case MANN_WHIT_GREATER: return result.greater;
					case MANN_WHIT_LESS: return result.less;
				}
			}
		}
		
		public String getDisplayString() {
			return "ranks: " + rankingName;
		}
	}
		
	
}
