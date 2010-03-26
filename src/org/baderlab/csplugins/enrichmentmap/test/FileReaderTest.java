package org.baderlab.csplugins.enrichmentmap.test;

import junit.framework.TestCase;
import org.baderlab.csplugins.enrichmentmap.EnrichmentMapParameters;
import org.baderlab.csplugins.enrichmentmap.GMTFileReaderTask;
import org.baderlab.csplugins.enrichmentmap.ExpressionFileReaderTask;

import java.util.HashMap;

/**
 * Created by
 * User: risserlin
 * Date: Mar 26, 2010
 * Time: 8:42:21 AM
 */
public class FileReaderTest extends TestCase {

    public void setUp() throws Exception {

    }

    public void testGMTFileReader(){

        String testDataFileName = "src/org/baderlab/csplugins/enrichmentmap/test/resources/Genesetstestfile.gmt";

        EnrichmentMapParameters params = new EnrichmentMapParameters();

        params.setGMTFileName(testDataFileName);

        //set up task
        GMTFileReaderTask task = new GMTFileReaderTask(params);

        //read in file
        task.run();

        //test to make sure that the file loaded in 9 genesets with a total of 72 genes
        assertEquals(9, params.getGenesets().size());
        assertEquals(73, params.getGenes().size());

    }
    public void TestExpression1Reader(){

        //load the test expression file
        String testDataFileName = "src/org/baderlab/csplugins/enrichmentmap/test/resources/Expressiontestfile.gct";

        EnrichmentMapParameters params = new EnrichmentMapParameters();

        params.setExpressionFileName1(testDataFileName);

        //in order to load expression data the genes have to be registered with the application
        HashMap<String, Integer> genes = params.getGenes();
        HashMap<Integer, String> hash2genes = params.getHashkey2gene();

        //make sure that the genes are empty
        assertEquals(0,genes.size());

        //add the gene to the master list of genes
        int value = params.getNumberOfGenes();
        genes.put("GLS", value);
        hash2genes.put(value,"GLS");
        params.setNumberOfGenes(value+1);

        genes.put("PSMA1", value);
        hash2genes.put(value,"PSMA1");
        params.setNumberOfGenes(value+1);

        //different case to the one in the expression file
        genes.put("zp1", value);
        hash2genes.put(value,"zp1");
        params.setNumberOfGenes(value+1);

        genes.put("ZYX", value);
        hash2genes.put(value,"ZYX");
        params.setNumberOfGenes(value+1);

        //make sure all four genes have been associated
        assertEquals(4,params.getGenes().size());

        //load expression file
        ExpressionFileReaderTask task = new ExpressionFileReaderTask(params,1);

        task.run();

        //There was one more gene in the expression file that wasn't in the set of genes
        //make sure it was was added
        assertEquals(5,params.getGenes().size());

        assertEquals(5, params.getExpression().getNumGenes());
        assertEquals(57, params.getExpression().getNumConditions());
        assertEquals(0.008720342, params.getExpression().getMinExpression());
        assertEquals(5.131481026, params.getExpression().getMaxExpression());
             
    }
}
