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

import java.util.Arrays;

import org.apache.commons.math3.util.Precision;
import org.baderlab.csplugins.enrichmentmap.view.util.FuncUtil;


/**
 * Class representing the expression of one gene/protein
 */
public class GeneExpression {

	private String name;
	private String description;
	private float[] expression;

	public GeneExpression(String name, String description) {
		this.name = name;
		this.description = description;
	}
	
	public GeneExpression(String name, String description, float[] expressions) {
		this(name, description);
		this.expression = expressions;
	}
	
	public GeneExpression(String name, String description, float dummyVal) {
		this(name, description);
		this.expression = new float[] { dummyVal };
	}

	/**
	 * Create an array of the expression values.
	 */
	public void setExpression(String[] expres) {
		// ignore the first two cells --> only if there are at least 3 cells
		int size = expres.length;

		if (size > 2) {
			expression = new float[size - 2];
			for (int i = 2; i < size; i++) {
				expression[i - 2] = parseAndRound(expres[i]);
			}
		} else {
			expression = new float[1];
			try {
				expression[0] = parseAndRound(expres[1]);
			} catch (NumberFormatException e) {
				// if the column doesn't contain doubles then just assume that the expression file is empty
				expression[0] = 0.0f;
			}
		}
	}
	
	private float parseAndRound(String exp) {
		float f = Float.parseFloat(exp);
		float r = Precision.round(f, 4);
		return r;
	}


	/**
	 * Row normalize the current gene expression set. Row normalization involved
	 * subtracting the mena of the row from each expression value in the row and
	 * subsequently dividing it by the standard deviation of the expression row.
	 *
	 * @return an array of the row normalized values of the gene expression set.
	 */
	public float[] rowNormalize() {
		float[] normalize = new float[expression.length];

		float mean = mean();
		float std  = std(mean);

		if(std == 0.0) {
			for (int i = 0; i < expression.length; i++)
				normalize[i] = 0.0f;
		} else {
			for (int i = 0; i < expression.length; i++)
				normalize[i] = (expression[i] - mean) / std;
		}

		return normalize;
	}
	
	
	public static float max(float[] expression) {
		return FuncUtil.reduceExpression(expression, Math::max);
	}
	
	public static float min(float[] expression) {
		return FuncUtil.reduceExpression(expression, Math::min);
	}
	
	public static float median(float[] expression) {
		if(expression == null || expression.length == 0)
			return 0;
		
		int ci = 0;
		float[] copy = new float[expression.length];
		for(float e : expression) {
			if(Float.isFinite(e)) {
				copy[ci++] = e;
			}
		}
		if(ci == 0)
			return Float.NaN;
		if(ci != expression.length)
			copy = Arrays.copyOf(copy, ci);
		
		Arrays.sort(copy);
		if(copy.length % 2 == 0)
		    return (copy[copy.length/2] + copy[copy.length/2 - 1]) / 2;
		else
		    return copy[copy.length/2];
	}
	
	public static float closestToZero(float[] expression) {
		float closest = max(expression);
		if(closest <= 0)
			return 0;
		for(float value : expression) {
			if(value > 0 && value < closest) {
				closest = value;
			}
		}
		return closest;
	}
	
	
	private float mean() {
		return FuncUtil.reduceExpression(expression, (x,y) -> x + y) / expression.length;
	}

	private float std(final float mean) {
		float sum = FuncUtil.reduceExpression(expression, (s, exp) -> s + (float)Math.pow(exp - mean, 2));
		return (float) Math.sqrt(sum) / expression.length;
	}

	public float[] rowLogTransform() {
		float[] logtransformed = new float[expression.length];
		for (int i = 0; i < expression.length; i++)
			logtransformed[i] = (float) Math.log1p(expression[i]);
		return logtransformed;
	}


	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public float[] getExpression() {
		return expression;
	}

	public void setExpression(float[] expression) {
		this.expression = expression;
	}
	
}
