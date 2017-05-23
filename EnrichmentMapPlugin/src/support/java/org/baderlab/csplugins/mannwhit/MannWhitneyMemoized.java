package org.baderlab.csplugins.mannwhit;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * A wrapper for MannWhitneyUTestSided that avoids recomputing results for arguments 
 * that have already been computed.
 * @author mkucera
 */
public class MannWhitneyMemoized {
	
	private MannWhitneyUTestSided delegate = new MannWhitneyUTestSided();
	private Map<CacheKey, MannWhitneyTestResult> cache = new ConcurrentHashMap<>();
	
	
	public MannWhitneyTestResult mannWhitneyUTestBatch(final double[] x, final double[] y) {
		return cache.computeIfAbsent(new CacheKey(x,y), k -> delegate.mannWhitneyUTestBatch(k.x, k.y));
	}
	
	/**
	 * The main reason for using a separate object for the key is that double[]
	 * needs to use Arrays.hashCode() and Arrays.equal() to work properly.
	 */
	private static class CacheKey {
		final double[] x;
		final double[] y;
		
		public CacheKey(double[] x, double[] y) {
			this.x = x;
			this.y = y;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + Arrays.hashCode(x);
			result = prime * result + Arrays.hashCode(y);
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			CacheKey other = (CacheKey) obj;
			if (!Arrays.equals(x, other.x))
				return false;
			if (!Arrays.equals(y, other.y))
				return false;
			return true;
		}
	}
	
}
