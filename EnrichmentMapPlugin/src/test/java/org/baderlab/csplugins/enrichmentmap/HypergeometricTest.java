/*
 *                       EnrichmentMap Cytoscape Plugin
 *
 * Copyright (c) 2008-2009 Bader Lab, Donnelly Centre for Cellular and Biomolecular 
 * Research, University of Toronto
 *
 * Contact: http://www.baderlab.org
 *
 * Code written by: Ruth Isserlin
 * Authors: Daniele Merico, Ruth Isserlin, Oliver Stueker, Gary D. Bader
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 * documentation provided hereunder is on an "as is" basis, and
 * University of Toronto
 * has no obligations to provide maintenance, support, updates, 
 * enhancements or modifications.  In no event shall the
 * University of Toronto
 * be liable to any party for direct, indirect, special,
 * incidental or consequential damages, including lost profits, arising
 * out of the use of this software and its documentation, even if
 * University of Toronto
 * has been advised of the possibility of such damage.  
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 *
 */

// $Id$
// $LastChangedDate$
// $LastChangedRevision$
// $LastChangedBy$
// $HeadURL$

package org.baderlab.csplugins.enrichmentmap;

import java.io.Reader;

import junit.framework.TestCase;

import org.baderlab.csplugins.enrichmentmap.BuildDiseaseSignatureTask;

import cytoscape.data.readers.TextFileReader;

/**
 * @author revilo
 * <p>
 * Date   Jul 24, 2009<br>
 * Time   11:50:07 AM<br>
 *
 */
public class HypergeometricTest extends TestCase {
    int N, n, m, k;
    double pValue, expected_pVal;
    Reader testData = null;
    
    public void setUp() throws Exception {
        //nothing to do here
    }
    
    public void testHyperGeomPvalueSmall() {
        N = 50;
        n = 10;
        m = 5;
        k = 4;
        
        pValue = BuildDiseaseSignatureTask.hyperGeomPvalue(N, n, m, k);
        assertEquals(0.003964583, pValue, 0.0005);

        k = 5;
        pValue = BuildDiseaseSignatureTask.hyperGeomPvalue(N, n, m, k);
        assertEquals(0.0001189375, pValue, 0.0005);
        
    }
    
    public void testHyperGeomPvalueBig() {
        String testDataFileName = "src/test/resources/org/baderlab/csplugins/enrichmentmap/HypergeometricTest_pvalues.csv";
        String fullText;
        String[] lines;

        TextFileReader reader = new TextFileReader(testDataFileName);
        reader.read();
        fullText = reader.getText();

        lines = fullText.split("\n");
        
        for (int i=0; i < lines.length ; i++ ) {
            if (i==0) {
                // Skip headerline
            } else {
                String[] tokens = lines[i].split(",");
                N = Integer.parseInt(tokens[0]);
                n = Integer.parseInt(tokens[1]);
                m = Integer.parseInt(tokens[2]);
                k = Integer.parseInt(tokens[3]);
                expected_pVal = Double.parseDouble(tokens[4]);
                pValue = BuildDiseaseSignatureTask.hyperGeomPvalue_sum(N, n, m, k, 1);
                
                assertEquals(expected_pVal, pValue, 0.00000005);
            }
            
        }
        
    }
}
