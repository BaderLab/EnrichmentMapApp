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

package org.baderlab.csplugins.enrichmentmap.task.distance;

import org.baderlab.csplugins.brainlib.DistanceMetric;

/**
 * Created by User: risserlin Date: Nov 2, 2010 Time: 9:42:43 AM
 */
public class CosineDistance extends DistanceMetric {

	/**
	 * Calculate the cosine distance for two vectors
	 */
	public double calc(Object expr1, Object expr2) {

		Double[] vectorA = (Double[]) expr1;
		Double[] vectorB = (Double[]) expr2;

		double result = 0.0;

		//numerator - the dot product between A and B
		double numerator = 0;

		//denominator - the magnitude of A time the magnitude of B
		double denominator = 0;

		//make sure vectorA and vectorB are not null and the same leghth
		if(vectorA.length == vectorB.length && vectorA.length > 0) {
			double magnitudeA = 0;
			double magnitudeB = 0;
			for(int i = 0; i < vectorA.length; i++) {
				numerator = numerator + (vectorA[i] * vectorB[i]);
				magnitudeA = magnitudeA + (vectorA[i] * vectorA[i]);
				magnitudeB = magnitudeB + (vectorB[i] * vectorB[i]);
			}

			denominator = Math.sqrt(magnitudeA) * Math.sqrt(magnitudeB);
		} else
			throw new RuntimeException("vectors are not the same length. Can not compute cosine distance");

		if(!(denominator == 0))
			result = numerator / denominator;
		else
			throw new RuntimeException("Can not divided by zero.  Can not computer cosine distance");

		return 1 - result;
	}
}
