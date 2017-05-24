package org.baderlab.csplugins.enrichmentmap.task.postanalysis;

import cern.jet.stat.Gamma;

public class Hypergeometric {

	private Hypergeometric() {
	}

	/**
	 * Calculate the p-Value of the Hypergeometric Distribution
	 * <p>
	 * 
	 * from: http://en.wikipedia.org/wiki/Hypergeometric_distribution
	 * <p>
	 * 
	 * P(X=k) = {m over k} * { (N-m) over (n-k) } / {N over n}
	 * 
	 * @param N size of the population (Universe of genes)
	 * @param n size of the sample (signature geneset)
	 * @param m successes in population (enrichment geneset)
	 * @param k successes in sample (intersection of both genesets)
	 * 
	 * @return the p-Value of the Hypergeometric Distribution for P(X=k)
	 */
	public static double hyperGeomPvalue(final int N, final int n, final int m, final int k)
			throws ArithmeticException {
		//calculating in logarithmic scale as we are dealing with large numbers. 
		double log_p = binomialLog(m, k) + binomialLog(N - m, n - k) - binomialLog(N, n);
		return Math.exp(log_p);
	}

	/**
	 * Calculate sum over distinct p-Values of the Hypergeometric Distribution
	 * <p>
	 * 
	 * for P(X &ge; k) : Probability to get k or more successes in the sample
	 * with a size of n<br>
	 * P(X &gt; k) : Probability to get more that k successes in the sample with
	 * a size of n<br>
	 * P(X &le; k) : Probability to get k or less successes in the sample with a
	 * size of n<br>
	 * P(X &lt; k) : Probability to get less than k successes in the sample with
	 * a size of n
	 * <p>
	 * 
	 * @param N size of the population (Universe of genes)
	 * @param n size of the sample (signature geneset)
	 * @param m successes in population (enrichment geneset)
	 * @param k successes in sample (intersection of both genesets)
	 * @param mode = 0 : P(X &ge; k) (default)<br>
	 *            mode = 1 : P(X &gt; k) (behavior of R with "lower.tail=FALSE")
	 *            <br>
	 *            mode = 2 : P(X &le; k) (behavior of R with "lower.tail=TRUE")
	 *            <br>
	 *            mode = 3 : P(X &lt; k)<br>
	 * 
	 * @return the p-Value of the Hypergeometric Distribution for P(X>=k)
	 */
	public static double hyperGeomPvalueSum(int N, int n, int m, int k, int mode) throws ArithmeticException {
		// the number of successes in the sample (k) cannot be larger than the sample (n) or the number of total successes (m)
		double sum = 0.0;
		int kMax;
		switch(mode) {
			case 0:
				kMax = Math.min(n, m);
				for(int k_prime = k; k_prime <= kMax; k_prime++) {
					sum += hyperGeomPvalue(N, n, m, k_prime);
				}
				break;

			case 1:
				kMax = Math.min(n, m);
				for(int k_prime = k + 1; k_prime <= kMax; k_prime++) {
					sum += hyperGeomPvalue(N, n, m, k_prime);
				}
				break;

			case 2:
				for(int k_prime = k; k_prime >= 0; k_prime--) {
					sum += hyperGeomPvalue(N, n, m, k_prime);
				}
				break;

			case 3:
				for(int k_prime = k - 1; k_prime >= 0; k_prime--) {
					sum += hyperGeomPvalue(N, n, m, k_prime);
				}
				break;
		}

		return sum;
	}

	/**
	 * Equivalent to hyperGeomPvalueSum(N, n, m, k, 0)
	 */
	public static double hyperGeomPvalueSum(int N, int n, int m, int k) throws ArithmeticException {
		return hyperGeomPvalueSum(N, n, m, k, 0);
	}

	/**
	 * Calculate the log of Binomial coefficient "n over k" aka "n choose k"
	 * 
	 * adapted from
	 * http://code.google.com/p/beast-mcmc/source/browse/trunk/src/dr/math/Binomial.java?spec=svn1660&r=1660
	 * 
	 * @version Id: Binomial.java,v 1.11 2005/05/24 20:26:00 rambaut Exp
	 *          Licensed under "LGPL 2.1 or later"
	 * 
	 *          original by:
	 * @author Andrew Rambaut
	 * @author Alexei Drummond
	 * @author Korbinian Strimmer
	 * 
	 *         adapted for using cern.jet.stat:
	 * @author Oliver Stueker
	 * 
	 * @param n
	 * @param k
	 * @return the binomial coefficient "n over k"
	 */
	public static double binomialLog(final int n, final int k) throws ArithmeticException {
		return Gamma.logGamma(n + 1.0) - Gamma.logGamma(k + 1.0) - Gamma.logGamma(n - k + 1.0);
	}
}
