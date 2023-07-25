package org.baderlab.csplugins.enrichmentmap.util;

public final class MathUtil {

	private static final double EPSILON = 1e-30d;

	private MathUtil() {
		// restrict instantiation
	}
	
	
	public static boolean isNumber(String s) {
	    if(s == null) {
	        return false;
	    }
	    try {
	        @SuppressWarnings("unused")
			double d = Double.parseDouble(s);
	    } catch (NumberFormatException e) {
	        return false;
	    }
	    return true;
	}
	
	
	/**
	 * Computes an inverse linear interpolation, returning an interpolation
	 * fraction. Returns 0.5 if the min and max values are the same.
	 * @param x the interpolated value
	 * @param min the minimum value (corresponds to f==0)
	 * @param min the maximum value (corresponds to f==1)
	 * @return the inferred interpolation fraction
	 */
    public static double invLinearInterp(final double x, final double min, final double max) {
        final double denom = max - min;
        return (denom < EPSILON && denom > -EPSILON ? 0 : (x - min) / denom);
    }
    
    /**
	 * Computes a linear interpolation between two values.
	 * @param f the interpolation fraction (typically between 0 and 1)
	 * @param min the minimum value (corresponds to f==0)
	 * @param max the maximum value (corresponds to f==1)
	 * @return the interpolated value
	 */
	public static double linearInterp(final double f, final double min, final double max) {
		return min + f * (max - min);
	}
	
	/**
	 * Computes a logarithmic interpolation between two values. Uses a
	 * zero-symmetric logarithm calculation (see <code>symLog</code>).
	 * @param f the interpolation fraction (typically between 0 and 1)
	 * @param min the minimum value (corresponds to f==0)
	 * @param max the maximum value (corresponds to f==1)
	 * @param b the base of the logarithm
	 * @return the interpolated value
	 */
	public static double logInterp(double f, double min, double max, double b) {
		min = symLog(min, b);
		max = symLog(max, b);
		f = min + f * (max - min);
		
		return f < 0 ? -Math.pow(b, -f) : Math.pow(b, f);
	}
	
    /**
	 * Computes an inverse logarithmic interpolation, returning an
	 * interpolation fraction. Uses a zero-symmetric logarithm.
	 * Returns 0.5 if the min and max values are the same.
	 * @param x the interpolated value
	 * @param min the minimum value (corresponds to f==0)
	 * @param min the maximum value (corresponds to f==1)
	 * @param b the base of the logarithm
	 * @return the inferred interpolation fraction
	 */
	public static double invLogInterp(double x, double min, double max, double b) {
        min = symLog(min, b);
        double denom = symLog(max, b) - min;
        return (denom < EPSILON && denom > -EPSILON ? 0 : (symLog(x, b) - min) / denom);
    }
    
	/**
	 * Computes a zero-symmetric logarithm. Computes the logarithm of the
	 * absolute value of the input, and determines the sign of the output
	 * according to the sign of the input value.
	 * @param x the number for which to compute the logarithm
	 * @param b the base of the logarithm
	 * @return the symmetric log value.
	 */
	public static double symLog(double x, double b) {
		return x == 0 ? 0 : x > 0 ? log(x, b) : -log(-x, b);
	}
	
	public static double log(double x, double b) {
		return Math.log(x) / Math.log(b);
	}
}
