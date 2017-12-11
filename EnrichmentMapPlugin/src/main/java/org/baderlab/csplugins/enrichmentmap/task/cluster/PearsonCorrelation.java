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

// $Id: PearsonCorrelation.java 390 2009-10-14 20:36:53Z risserlin $
// $LastChangedDate: 2009-10-14 16:36:53 -0400 (Wed, 14 Oct 2009) $
// $LastChangedRevision: 390 $
// $LastChangedBy: risserlin $
// $HeadURL: svn+ssh://risserlin@server1.baderlab.med.utoronto.ca/svn/EnrichmentMap/trunk/EnrichmentMapPlugin/src/org/baderlab/csplugins/enrichmentmap/PearsonCorrelation.java $

package org.baderlab.csplugins.enrichmentmap.task.cluster;

import org.baderlab.csplugins.brainlib.DistanceMetric;

/**
 * Calculate the distance between two protein/gene expression sets using pearson
 * correlation
 */
public class PearsonCorrelation implements DistanceMetric {

	@Override
	public float calc(float[] x, float[] y) {
		double result = 0;
		double sum_x = 0;
		double sum_y = 0;
		double sum_xy = 0;
		double sum_sq_x = 0;
		double sum_sq_y = 0;

		//calculate the means of the data.
		for(int i = 0; i < x.length; i++) {
			//calculate all the values need for the pearson correlation
			//sum of protein/gene 1 expression values
			sum_x += x[i];
			//sum of protein/gene 2 expression values
			sum_y += y[i];

			//sum of protein/gene 1 * protein/gene 2 expression values
			sum_xy += x[i] * y[i];

			//sum of protein/gene 1 squared expression values
			sum_sq_x += x[i] * x[i];
			//sum of protein/gene 2 squared expression values
			sum_sq_y += y[i] * y[i];
		}
		// make all variables means instead of sums.
		sum_x = sum_x / x.length;
		sum_y = sum_y / y.length;
		sum_xy = sum_xy / x.length;
		sum_sq_x = sum_sq_x / x.length;
		sum_sq_y = sum_sq_y / y.length;

		double numerator = sum_xy - ((sum_x * sum_y));
		double denominator = (Math.sqrt(sum_sq_x - ((sum_x * sum_x))) * Math.sqrt((sum_sq_y - ((sum_y * sum_y)))));

		//check to see if the denominator is zero (can't divide by zero)
		if(denominator == 0) {
			throw new RuntimeException("standard deviation is zero");
		} else
			result = numerator / denominator;
		return (float)(1 - result);
	}

}
