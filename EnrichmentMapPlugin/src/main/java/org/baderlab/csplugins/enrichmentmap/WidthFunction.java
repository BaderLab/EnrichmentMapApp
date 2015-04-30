package org.baderlab.csplugins.enrichmentmap;

import org.cytoscape.equations.AbstractFunction;
import org.cytoscape.equations.ArgDescriptor;
import org.cytoscape.equations.ArgType;

public class WidthFunction extends AbstractFunction {
	
	public static final String NAME = "EnrichmentMapWidth";
	
	public WidthFunction() {
		super(new ArgDescriptor[] { new ArgDescriptor(ArgType.INT, "number", "any numeric value") });
	}

	public String getName() { 
		return NAME; 
	}
 
	public String getFunctionSummary() { 
		return "Calculate edge width for EnrichmentMap networks."; 
	}
	

	public Class<Double> getReturnType() { 
		return Double.class; 
	}

	public Object evaluateFunction(final Object[] args) {
		// TEMP
		final double number;
		if (args[0] instanceof Double)
			number = (Double)args[0];
		else // Assume we are dealing with an integer.
			number = (Long)args[0];

		return Math.abs(number);
	}
}