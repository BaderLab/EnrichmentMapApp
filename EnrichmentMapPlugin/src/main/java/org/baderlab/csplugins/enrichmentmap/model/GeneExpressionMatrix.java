/**
 **                       EnrichmentMap Cytoscape Plugin
 **
 ** Copyright (c) 2008-2009 Bader Lab, Donnelly Centre for Cellular and Biomolecular 
 ** Research, University of Toronto
 **
 ** Contact: http://www.baderlab.org
 **
 ** Code written by: Ruth Isserlin
 ** Authors: Daniele Merico, Ruth Isserlin, Oliver Stueker, Gary D. Bader
 **
 ** This library is free software; you can redistribute it and/or modify it
 ** under the terms of the GNU Lesser General Public License as published
 ** by the Free Software Foundation; either version 2.1 of the License, or
 ** (at your option) any later version.
 **
 ** This library is distributed in the hope that it will be useful, but
 ** WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 ** MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 ** documentation provided hereunder is on an "as is" basis, and
 ** University of Toronto
 ** has no obligations to provide maintenance, support, updates, 
 ** enhancements or modifications.  In no event shall the
 ** University of Toronto
 ** be liable to any party for direct, indirect, special,
 ** incidental or consequential damages, including lost profits, arising
 ** out of the use of this software and its documentation, even if
 ** University of Toronto
 ** has been advised of the possibility of such damage.  
 ** See the GNU Lesser General Public License for more details.
 **
 ** You should have received a copy of the GNU Lesser General Public License
 ** along with this library; if not, write to the Free Software Foundation,
 ** Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 **
 **/

// $Id$
// $LastChangedDate$
// $LastChangedRevision$
// $LastChangedBy$
// $HeadURL$

package org.baderlab.csplugins.enrichmentmap.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.baderlab.csplugins.enrichmentmap.view.util.FuncUtil;

/**
 * Class representing a set of genes/proteins expresion profile
 */
public class GeneExpressionMatrix {

	//name of columns - specified by first or second row in the expression matrix
	private String[] columnNames;
	//number of conditions - number of columns
	private int numConditions;
	private int expressionUniverse;

	//Store two instances of the expression matrix, one with the raw expression values
	//and one with the row normalized values.  The row normalizes values are stored as opposed
	//to being computing on the fly to decrease the time needed to update a heatmap.
	private Map<Integer, GeneExpression> expressionMatrix = new HashMap<>();


	public static float getMaxExpression(Map<Integer,GeneExpression> matrix) {
		return FuncUtil.reduceExpressionMatrix(matrix, GeneExpression::max, Math::max);
	}
	
	public float getMaxExpression() {
		return getMaxExpression(expressionMatrix);
	}

	public static float getMinExpression(Map<Integer,GeneExpression> matrix) {
		return FuncUtil.reduceExpressionMatrix(matrix, GeneExpression::min, Math::min);
	}
	
	public float getMinExpression() {
		return getMinExpression(expressionMatrix);
	}
	
	public float getClosestToZero() {
		float closest = getMaxExpression();
		if(closest <= 0)
			return 0;
		for(GeneExpression expression : expressionMatrix.values()) {
			for(float value : expression.getExpression()) {
				if(value > 0 && value < closest) {
					closest = value;
				}
			}
		}
		return closest;
	}
	

	/**
	 * Compute the row Normalized version of the current expression matrix. Row
	 * Normalization involves computing the mean and standard deviation for each
	 * row in the matrix. Each value in that specific row has the mean
	 * subtracted and is divided by the standard deviation. Row normalization is
	 * computed lazily and cached with the expression matrix. 
	 * (Log normalization is computed on the fly)
	 */
	public synchronized Map<Integer, GeneExpression> rowNormalizeMatrix() {
		Map<Integer, GeneExpression> expressionMatrix_rowNormalized = new HashMap<>();
		for (Integer key : expressionMatrix.keySet()) {
			GeneExpression expression = (GeneExpression) expressionMatrix.get(key);
			GeneExpression norm_row = new GeneExpression(expression.getName(), expression.getDescription());
			float[] row_normalized = expression.rowNormalize();
			norm_row.setExpression(row_normalized);
			expressionMatrix_rowNormalized.put(key, norm_row);
		}
		return expressionMatrix_rowNormalized;
	}

	//Getters and Setters

	public String[] getColumnNames() {
		return columnNames;
	}

	public void setColumnNames(String[] columnNames) {
		if(columnNames.length == 2) {
			String[] new_names = new String[3];
			new_names[0] = columnNames[0];
			new_names[1] = "Description";
			new_names[2] = columnNames[1];
			this.columnNames = new_names;
		} else
			this.columnNames = columnNames;
	}

	public void setExpressionUniverse(int size) {
		this.expressionUniverse = size;
	}

	public int getExpressionUniverse() {
		return expressionUniverse;
	}

	public int getNumConditions() {
		return numConditions;
	}

	public void setNumConditions(int numConditions) {
		this.numConditions = numConditions;
	}

	public int getNumGenes() {
		return expressionMatrix.size();
	}

	public Map<Integer, GeneExpression> getExpressionMatrix() {
		return expressionMatrix;
	}

	public void setExpressionMatrix(Map<Integer, GeneExpression> expressionMatrix) {
		this.expressionMatrix = expressionMatrix;
	}

	/**
	 * Restores parameters saved in the session file. Note, most of this object
	 * is restored by the ExpressionFileReaderTask.
	 */
	public void restoreProps(String ds, Map<String, String> props) {
		String simpleName = this.getClass().getSimpleName();
		String val = props.get(ds + "%" + simpleName + "%expressionUniverse");
		if(val != null) {
			try {
				expressionUniverse = Integer.parseInt(val);
			} catch(NumberFormatException e) {
			}
		}
	}

	public Set<Integer> getGeneIds() {
		return expressionMatrix.keySet();
	}

	
}
