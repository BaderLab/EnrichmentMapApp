package org.baderlab.csplugins.enrichmentmap.model;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class ExpressionOpsTest {

	@Test
	public void testGeneExpressionOps() {
		float[] expressions = {0.0f, 1.0f, 2.0f, 3.0f, -1.0f, -2.0f};
		assertEquals(3.0f,  GeneExpression.max(expressions), 0.0f);
		assertEquals(-2.0f, GeneExpression.min(expressions), 0.0f);
		assertEquals(1.0f,  GeneExpression.closestToZero(expressions), 0.0f);
		assertEquals(0.5f,  GeneExpression.median(expressions), 0.0f);
	}
	
	@Test
	public void testGeneExpressionOpsZeros() {
		float[] expressions = {};
		assertEquals(0f, GeneExpression.max(expressions), 0.0f);
		assertEquals(0f, GeneExpression.min(expressions), 0.0f);
		assertEquals(0f, GeneExpression.closestToZero(expressions), 0.0f);
		assertEquals(0f, GeneExpression.median(expressions), 0.0f);
		
		expressions = null;
		assertEquals(0f, GeneExpression.max(expressions), 0.0f);
		assertEquals(0f, GeneExpression.min(expressions), 0.0f);
		assertEquals(0f, GeneExpression.closestToZero(expressions), 0.0f);
		assertEquals(0f, GeneExpression.median(expressions), 0.0f);
	}
	
	@Test
	public void testGeneExpressionOpsNegatives() {
		float[] expressions = {-1f,-2f,-3f};
		assertEquals(-1f, GeneExpression.max(expressions), 0.0f);
		assertEquals(-3f, GeneExpression.min(expressions), 0.0f);
		assertEquals(0f,  GeneExpression.closestToZero(expressions), 0.0f);
		assertEquals(-2f, GeneExpression.median(expressions), 0.0f);
	}
	
	@Test
	public void testExpressionMatrixOps() {
		Map<Integer,GeneExpression> map = new HashMap<>();
		map.put(1, new GeneExpression("ge1", "", new float[] {0.0f, 1.0f, 2.0f, 3.0f}));
		map.put(2, new GeneExpression("ge2", "", new float[] {0.0f, -1.0f, -2.0f, -3.0f}));
		
		GeneExpressionMatrix matrix = new GeneExpressionMatrix();
		matrix.setExpressionMatrix(map);
		
		assertEquals(3.0f,  matrix.getMaxExpression(), 0.0f);
		assertEquals(-3.0f, matrix.getMinExpression(), 0.0f);
		assertEquals(1.0f,  matrix.getClosestToZero(), 0.0f);
	}
	
	@Test
	public void testExpressionMatrixOpsZeros() {
		Map<Integer,GeneExpression> map = new HashMap<>();
		map.put(1, new GeneExpression("ge1", "", new float[] {}));
		map.put(2, new GeneExpression("ge2", "", new float[] {}));
		
		GeneExpressionMatrix matrix = new GeneExpressionMatrix();
		matrix.setExpressionMatrix(map);
		
		assertEquals(0f, matrix.getMaxExpression(), 0.0f);
		assertEquals(0f, matrix.getMinExpression(), 0.0f);
		assertEquals(0f, matrix.getClosestToZero(), 0.0f);
	}

}
