package org.baderlab.csplugins.enrichmentmap;

import junit.framework.TestCase;

import org.apache.commons.math3.stat.inference.MannWhitneyUTest;
import org.apache.commons.math3.stat.ranking.NaNStrategy;
import org.apache.commons.math3.stat.ranking.TiesStrategy;

import cytoscape.data.readers.TextFileReader;

/**
 * Test cases for Mann Whitney U
 */
public class MannWhitneyRankSumTest extends TestCase {
	double[] x, y;
	double expected_pVal, pValue;

    public void setUp() throws Exception {
        //nothing to do here
    }
    
    public void testMannWhitneyPvalue() {
        String testDataFileName = "src/test/resources/org/baderlab/csplugins/enrichmentmap/MannWhitneyTest_pvalues.csv";
        String fullText;
        String[] lines;

        TextFileReader reader = new TextFileReader(testDataFileName);
        reader.read();
        fullText = reader.getText();

        lines = fullText.split("\n");
        String[] tokens, x_s, y_s;
        int i, j, k;
        
        for (i=1; i < lines.length ; i++ ) {
        	// Split
        	tokens = lines[i].split(",");
        	
        	// Parse x contents
        	x_s = tokens[0].split("\\s");
        	x = new double[x_s.length];
        	for (j = 0; j < x_s.length; j++)
        		x[j] = Double.parseDouble(x_s[j]);
        	
        	// Parse y contents
        	y_s = tokens[1].split("\\s");
        	y = new double[y_s.length];
        	for (k = 0; k < y_s.length; k++)
        		y[k] = Double.parseDouble(y_s[k]);
        	
        	// Procure expected pval
        	expected_pVal = Double.parseDouble(tokens[2]);
        	
			MannWhitneyUTest test = new MannWhitneyUTest(NaNStrategy.FAILED, TiesStrategy.AVERAGE);
			pValue = test.mannWhitneyUTest(x, y);
			
			assertEquals(expected_pVal, pValue);
        }

    }
}
