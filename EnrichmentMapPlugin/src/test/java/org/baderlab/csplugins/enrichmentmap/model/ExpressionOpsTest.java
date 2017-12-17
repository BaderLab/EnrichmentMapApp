package org.baderlab.csplugins.enrichmentmap.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.baderlab.csplugins.enrichmentmap.view.heatmap.HeatMapParams.Transform;
import org.baderlab.csplugins.enrichmentmap.view.heatmap.table.DataSetColorRange;
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
	public void testGeneExpressionOpsNaN() {
		float[] expressions = {Float.NaN, 0.0f, 1.0f, 2.0f, 3.0f, -1.0f, -2.0f};
		assertEquals(3.0f,  GeneExpression.max(expressions), 0.0f);
		assertEquals(-2.0f, GeneExpression.min(expressions), 0.0f);
		assertEquals(1.0f,  GeneExpression.closestToZero(expressions), 0.0f);
		assertEquals(0.5f,  GeneExpression.median(expressions), 0.0f);
		
		expressions = new float[] {Float.NaN, 0.0f, Float.NaN, 1.0f, 2.0f, 3.0f, -1.0f, -2.0f};
		assertEquals(0.5f,  GeneExpression.median(expressions), 0.0f);
	}
	
	@Test
	public void testGeneExpressionOpsOnlyNaN() {
		float[] expressions = {Float.NaN, Float.NaN, Float.NaN, Float.NaN};
		assertTrue(Float.isNaN(GeneExpression.max(expressions)));
		assertTrue(Float.isNaN(GeneExpression.min(expressions)));
		assertTrue(Float.isNaN(GeneExpression.closestToZero(expressions)));
		assertTrue(Float.isNaN(GeneExpression.median(expressions)));
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
		
		assertEquals(3.0f,  matrix.getMinMax(Transform.AS_IS)[1], 0.0f);
		assertEquals(-3.0f, matrix.getMinMax(Transform.AS_IS)[0], 0.0f);
		assertEquals(1.0f,  matrix.getClosestToZero(), 0.0f);
	}
	
	@Test
	public void testExpressionMatrixOpsNaN() {
		Map<Integer,GeneExpression> map = new HashMap<>();
		map.put(1, new GeneExpression("ge1", "", new float[] {0.0f, Float.NaN, 1.0f, 2.0f, 3.0f}));
		map.put(2, new GeneExpression("ge2", "", new float[] {0.0f, -1.0f, -2.0f, -3.0f}));
		
		GeneExpressionMatrix matrix = new GeneExpressionMatrix();
		matrix.setExpressionMatrix(map);
		
		assertEquals(3.0f,  matrix.getMinMax(Transform.AS_IS)[1], 0.0f);
		assertEquals(-3.0f, matrix.getMinMax(Transform.AS_IS)[0], 0.0f);
		assertEquals(1.0f,  matrix.getClosestToZero(), 0.0f);
	}
	
	@Test
	public void testExpressionMatrixOpsZeros() {
		Map<Integer,GeneExpression> map = new HashMap<>();
		map.put(1, new GeneExpression("ge1", "", new float[] {}));
		map.put(2, new GeneExpression("ge2", "", new float[] {}));
		
		GeneExpressionMatrix matrix = new GeneExpressionMatrix();
		matrix.setExpressionMatrix(map);
		
		assertEquals(0f, matrix.getMinMax(Transform.AS_IS)[1], 0.0f);
		assertEquals(0f, matrix.getMinMax(Transform.AS_IS)[0], 0.0f);
		assertEquals(0f, matrix.getClosestToZero(), 0.0f);
	}

	@Test
	public void testDataSetColorRangeNaN() {
		Map<Integer,GeneExpression> map = new HashMap<>();
		map.put(1, new GeneExpression("ge1", "", new float[] {Float.NaN, Float.NaN, 1.0f, Float.NaN, Float.NaN}));
		map.put(2, new GeneExpression("ge2", "", new float[] {Float.NaN, Float.NaN, Float.NaN, Float.NaN}));
		GeneExpressionMatrix matrix = new GeneExpressionMatrix();
		matrix.setExpressionMatrix(map);
		
		assertTrue(DataSetColorRange.create(matrix, Transform.AS_IS).isPresent());
		assertTrue(DataSetColorRange.create(matrix, Transform.LOG_TRANSFORM).isPresent());
		assertTrue(DataSetColorRange.create(matrix, Transform.ROW_NORMALIZE).isPresent());
		
		map = new HashMap<>();
		map.put(1, new GeneExpression("ge1", "", new float[] {Float.NaN, Float.NaN, Float.NaN, Float.NaN, Float.NaN}));
		map.put(2, new GeneExpression("ge2", "", new float[] {Float.NaN, Float.NaN, Float.NaN, Float.NaN}));
		matrix = new GeneExpressionMatrix();
		matrix.setExpressionMatrix(map);
		
		assertFalse(DataSetColorRange.create(matrix, Transform.AS_IS).isPresent());
		assertFalse(DataSetColorRange.create(matrix, Transform.LOG_TRANSFORM).isPresent());
		assertFalse(DataSetColorRange.create(matrix, Transform.ROW_NORMALIZE).isPresent());
	}
}
