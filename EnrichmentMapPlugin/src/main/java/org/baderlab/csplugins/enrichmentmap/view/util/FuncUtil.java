package org.baderlab.csplugins.enrichmentmap.view.util;

import java.util.Iterator;
import java.util.Map;

import org.baderlab.csplugins.enrichmentmap.model.GeneExpression;

public class FuncUtil {

	@FunctionalInterface
	public interface ExprFloatFunc {
		float apply(float[] fs);
	}
	
	@FunctionalInterface
	public interface FloatFloatFunc {
		float apply(float x, float y);
	}

	
	public static float reduceExpression(float[] expression, FuncUtil.FloatFloatFunc op) {
		if(expression == null || expression.length == 0)
			return 0;
		float x = expression[0];
		for(int i = 1; i < expression.length; i++) {
			x = op.apply(x, expression[i]);
		}
		return x;
	}
	
	
	public static float reduceExpressionMatrix(Map<Integer,GeneExpression> matrix, FuncUtil.ExprFloatFunc arrayop, FuncUtil.FloatFloatFunc binop) {
		Iterator<GeneExpression> iter = matrix.values().iterator();
		float x = arrayop.apply(iter.next().getExpression());
		while(iter.hasNext()) {
			x = binop.apply(x, arrayop.apply(iter.next().getExpression()));
		}
		return x;
	}
	
}
