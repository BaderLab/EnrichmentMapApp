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
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.baderlab.csplugins.enrichmentmap.view.heatmap.HeatMapParams.Transform;

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


	public float getClosestToZero() {
		float closest = getMinMax(Transform.AS_IS)[1];
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
	
	
	public float[] getMinMax(Transform transform) {
		Iterator<GeneExpression> iter = expressionMatrix.values().iterator();
		if(!iter.hasNext())
			return null;
		
		float[] normValues = getExpressions(iter.next(), transform);
		float min = GeneExpression.min(normValues);
		float max = GeneExpression.max(normValues);
		
		while(iter.hasNext()) {
			normValues = getExpressions(iter.next(), transform);
			float newMin = GeneExpression.min(normValues);
			float newMax = GeneExpression.max(normValues);
			
			if(!Float.isFinite(min))
				min = newMin;
			else if(Float.isFinite(newMin)) 
				min = Math.min(min, newMin);
			
			if(!Float.isFinite(max))
				max = newMax;
			else if(Float.isFinite(newMax)) 
				max = Math.max(max, newMax);
		}
		
		return new float[] { min, max };
	}
	
	
	private static float[] getExpressions(GeneExpression expression, Transform transform) {
		switch(transform) {
			default:
			case AS_IS: return expression.getExpression();
			case LOG_TRANSFORM: return expression.rowLogTransform();
			case ROW_NORMALIZE: return expression.rowNormalize();
		}
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
		return new HashMap<>(expressionMatrix);
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
