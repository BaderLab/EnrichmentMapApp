package org.baderlab.csplugins.enrichmentmap.view.heatmap.table;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import javax.annotation.Nullable;

import org.apache.commons.lang3.tuple.Pair;
import org.baderlab.csplugins.enrichmentmap.model.EMDataSet;
import org.baderlab.csplugins.enrichmentmap.model.GeneExpression;
import org.baderlab.csplugins.enrichmentmap.model.GeneExpressionMatrix;
import org.baderlab.csplugins.enrichmentmap.view.heatmap.HeatMapParams.Transform;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

public class ExpressionCache {
	
	private final Cache<Pair<Integer,EMDataSet>, Optional<float[]>> cache;
	private final Transform transform;

	public ExpressionCache(Transform transform) {
		this.transform = transform;
		this.cache = CacheBuilder.newBuilder().maximumSize(20).build();
	}
	
	public Optional<float[]> getExpressions(EMDataSet dataset, int geneID) {
		try {
			return cache.get(Pair.of(geneID, dataset), 
				() -> Optional.ofNullable(getExpression(dataset, geneID, transform))
			);
		} catch (ExecutionException e) {
			return Optional.empty();
		}
	}
	
	public float getExpression(EMDataSet dataset, int geneID, int expressionIndex) {
		Optional<float[]> vals = getExpressions(dataset, geneID);
		if(vals.isPresent()) {
			return vals.get()[expressionIndex];
		} else {
			return Float.NaN;
		}
	}
	

	public static GeneExpression getGeneExpression(EMDataSet dataset, int geneID) {
		GeneExpressionMatrix matrix = dataset.getExpressionSets();
		Map<Integer,GeneExpression> expressions = matrix.getExpressionMatrix();
		GeneExpression row = expressions.get(geneID);
		return row;
	}
	
	private static @Nullable float[] getExpression(EMDataSet dataset, int geneID, Transform transform) {
		GeneExpression expression = getGeneExpression(dataset, geneID);
		if(expression != null) {
			switch(transform) {
				case ROW_NORMALIZE: return expression.rowNormalize();
				case LOG_TRANSFORM: return expression.rowLogTransform();
				case AS_IS:         return expression.getExpression();
			}
		}
		return null;
	}
	
}
