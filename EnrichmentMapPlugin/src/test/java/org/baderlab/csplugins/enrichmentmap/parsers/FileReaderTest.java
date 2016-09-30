package org.baderlab.csplugins.enrichmentmap.parsers;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import java.util.Map;

import org.baderlab.csplugins.enrichmentmap.StreamUtil;
import org.baderlab.csplugins.enrichmentmap.model.DataSet;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMapParameters;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentResult;
import org.baderlab.csplugins.enrichmentmap.model.GSEAResult;
import org.baderlab.csplugins.enrichmentmap.model.GenericResult;
import org.cytoscape.work.TaskMonitor;
import org.junit.Test;

/**
 * Created by
 * User: risserlin
 * Date: Mar 26, 2010
 * Time: 8:42:21 AM
 */
public class FileReaderTest {

	private TaskMonitor taskMonitor = mock(TaskMonitor.class);
	private StreamUtil streamUtil = new StreamUtil();
	
    
	@Test
    public void testGMTFileReader() throws Exception{

        String testDataFileName = "src/test/resources/org/baderlab/csplugins/enrichmentmap/Genesetstestfile.gmt";
        
        //create a new instance of the parameters
        EnrichmentMapParameters params = new EnrichmentMapParameters();        
        //set gmt file name 
        params.getFiles().get(EnrichmentMap.DATASET1).setGMTFileName(testDataFileName);
        
        //Create a new Enrichment map
        EnrichmentMap map = new EnrichmentMap(params);
                
        //get the default dataset
        DataSet dataset = map.getDataset(EnrichmentMap.DATASET1);

      //set up task
        GMTFileReaderTask task = new GMTFileReaderTask(dataset);
        	task.run(taskMonitor);

        //test to make sure that the file loaded in 10 genesets with a total of 75 genes
        assertEquals(10, map.getAllGenesets().size());
        assertEquals(75, map.getGenes().size());

    }

	@Test
    public void testExpression1ReaderNormal() throws Exception{

        //load the test expression file
        String testDataFileName = "src/test/resources/org/baderlab/csplugins/enrichmentmap/Expressiontestfile.gct";

        //create a new instance of the parameters
        EnrichmentMapParameters params = new EnrichmentMapParameters();        
        //set expression file name 
        params.getFiles().get(EnrichmentMap.DATASET1).setExpressionFileName(testDataFileName);
        
        //Create a new Enrichment map
        EnrichmentMap map = new EnrichmentMap(params);
                
        //get the default dataset
        DataSet dataset = map.getDataset(EnrichmentMap.DATASET1);

        //in order to load expression data the genes have to be registered with the application
        Map<String, Integer> genes = map.getGenes();
        Map<Integer, String> hash2genes = map.getHashkey2gene();

        //make sure that the genes are empty
        assertEquals(0,genes.size());

        //add the gene to the master list of genes
        int value = map.getNumberOfGenes();
        genes.put("GLS", value);
        hash2genes.put(value,"GLS");
        map.setNumberOfGenes(value++);

        genes.put("PSMA1", value);
        hash2genes.put(value,"PSMA1");
        map.setNumberOfGenes(value++);

        //different case to the one in the expression file
        genes.put("ZP1", value);
        hash2genes.put(value,"ZP1");
        map.setNumberOfGenes(value++);

        genes.put("ZYX", value);
        hash2genes.put(value,"ZYX");
        map.setNumberOfGenes(value++);

        //make sure all four genes have been associated
        assertEquals(4,map.getGenes().size());

        //load expression file
        ExpressionFileReaderTask task = new ExpressionFileReaderTask(dataset);
        task.run(taskMonitor);

        //There was one more gene in the expression file that wasn't in the set of genes
        //make sure it was was added
        assertEquals(4,map.getGenes().size());
        
        assertEquals(4, map.getDataset(EnrichmentMap.DATASET1).getExpressionSets().getNumGenes());
        assertEquals(59, map.getDataset(EnrichmentMap.DATASET1).getExpressionSets().getNumConditions());
        assertEquals(0.008720342, map.getDataset(EnrichmentMap.DATASET1).getExpressionSets().getMinExpression(),0.0);
        assertEquals(5.131481026, map.getDataset(EnrichmentMap.DATASET1).getExpressionSets().getMaxExpression(),0.0);

    }
	
	@Test
    public void testExpression1ReaderCommentLines() throws Exception{

        //load the test expression file
        String testDataFileName = "src/test/resources/org/baderlab/csplugins/enrichmentmap/Expressiontestfile_comments.gct";

        //create a new instance of the parameters
        EnrichmentMapParameters params = new EnrichmentMapParameters();        
        //set gmt file name 
        params.getFiles().get(EnrichmentMap.DATASET1).setExpressionFileName(testDataFileName);
        
        //Create a new Enrichment map
        EnrichmentMap map = new EnrichmentMap(params);
                
        //get the default dataset
        DataSet dataset = map.getDataset(EnrichmentMap.DATASET1);

        //in order to load expression data the genes have to be registered with the application
        Map<String, Integer> genes = map.getGenes();
        Map<Integer, String> hash2genes = map.getHashkey2gene();

        //make sure that the genes are empty
        assertEquals(0,genes.size());

        //add the gene to the master list of genes
        int value = map.getNumberOfGenes();
        genes.put("GLS", value);
        hash2genes.put(value,"GLS");
        map.setNumberOfGenes(value++);

        genes.put("PSMA1", value);
        hash2genes.put(value,"PSMA1");
        map.setNumberOfGenes(value++);

        //different case to the one in the expression file
        genes.put("ZP1", value);
        hash2genes.put(value,"ZP1");
        map.setNumberOfGenes(value++);

        genes.put("ZYX", value);
        hash2genes.put(value,"ZYX");
        map.setNumberOfGenes(value++);

        //make sure all four genes have been associated
        assertEquals(4,map.getGenes().size());

        //load expression file
        ExpressionFileReaderTask task = new ExpressionFileReaderTask(dataset);
        task.run(taskMonitor);


        //There was one more gene in the expression file that wasn't in the set of genes
        //make sure it was was added
        assertEquals(4,map.getGenes().size());
        assertEquals(5.131481026, map.getDataset(EnrichmentMap.DATASET1).getExpressionSets().getMaxExpression(),0.0);

        assertEquals(4, map.getDataset(EnrichmentMap.DATASET1).getExpressionSets().getNumGenes());
        assertEquals(59, map.getDataset(EnrichmentMap.DATASET1).getExpressionSets().getNumConditions());
        assertEquals(0.008720342, map.getDataset(EnrichmentMap.DATASET1).getExpressionSets().getMinExpression(),0.0);


    }

	@Test
    public void testExpression1ReaderRnk() throws Exception{

        //load the test expression file
        String testDataFileName = "src/test/resources/org/baderlab/csplugins/enrichmentmap/ExpressionTestFile.rnk";

      //create a new instance of the parameters
        EnrichmentMapParameters params = new EnrichmentMapParameters();        
        //set expression file name 
        params.getFiles().get(EnrichmentMap.DATASET1).setExpressionFileName(testDataFileName);
        
        //Create a new Enrichment map
        EnrichmentMap map = new EnrichmentMap(params);
                
        //get the default dataset
        DataSet dataset = map.getDataset(EnrichmentMap.DATASET1);

        //in order to load expression data the genes have to be registered with the application
        Map<String, Integer> genes = map.getGenes();
        Map<Integer, String> hash2genes = map.getHashkey2gene();

        //make sure that the genes are empty
        assertEquals(0,genes.size());

        //add the gene to the master list of genes
        int value = map.getNumberOfGenes();
        genes.put("GLS", value);
        hash2genes.put(value,"GLS");
        map.setNumberOfGenes(value++);

        genes.put("PSMA1", value);
        hash2genes.put(value,"PSMA1");
        map.setNumberOfGenes(value++);

        //different case to the one in the expression file
        genes.put("ZP1", value);
        hash2genes.put(value,"ZP1");
        map.setNumberOfGenes(value++);

        genes.put("ZYX", value);
        hash2genes.put(value,"ZYX");
        map.setNumberOfGenes(value++);

        //make sure all four genes have been associated
        assertEquals(4,map.getGenes().size());

        //load expression file
        ExpressionFileReaderTask task = new ExpressionFileReaderTask(dataset);
        task.run(taskMonitor);


        //There was one more gene in the expression file that wasn't in the set of genes
        //make sure it was was added
        assertEquals(4,map.getGenes().size());

        assertEquals(4, map.getDataset(EnrichmentMap.DATASET1).getExpressionSets().getNumGenes());
        assertEquals(3, map.getDataset(EnrichmentMap.DATASET1).getExpressionSets().getNumConditions());
        assertEquals(0.47536945, map.getDataset(EnrichmentMap.DATASET1).getExpressionSets().getMinExpression(),0.0);
        assertEquals(0.5418719, map.getDataset(EnrichmentMap.DATASET1).getExpressionSets().getMaxExpression(),0.0);

    }

	@Test
    public void testExpression1ReaderEDBRnk() throws Exception{

        //load the test expression file
        String testDataFileName = "src/test/resources/org/baderlab/csplugins/enrichmentmap/ExpressionTestFile_edbrnk.rnk";

      //create a new instance of the parameters
        EnrichmentMapParameters params = new EnrichmentMapParameters();        
        //set gmt file name 
        params.getFiles().get(EnrichmentMap.DATASET1).setExpressionFileName(testDataFileName);
        
        //Create a new Enrichment map
        EnrichmentMap map = new EnrichmentMap(params);
                
        //get the default dataset
        DataSet dataset = map.getDataset(EnrichmentMap.DATASET1);

        //in order to load expression data the genes have to be registered with the application
        Map<String, Integer> genes = map.getGenes();
        Map<Integer, String> hash2genes = map.getHashkey2gene();

        //make sure that the genes are empty
        assertEquals(0,genes.size());

        //add the gene to the master list of genes
        int value = map.getNumberOfGenes();
        genes.put("GLS", value);
        hash2genes.put(value,"GLS");
        map.setNumberOfGenes(value++);

        genes.put("PSMA1", value);
        hash2genes.put(value,"PSMA1");
        map.setNumberOfGenes(value++);

        //different case to the one in the expression file
        genes.put("ZP1", value);
        hash2genes.put(value,"ZP1");
        map.setNumberOfGenes(value++);

        genes.put("ZYX", value);
        hash2genes.put(value,"ZYX");
        map.setNumberOfGenes(value++);

        //make sure all four genes have been associated
        assertEquals(4,map.getGenes().size());

        //load expression file
        ExpressionFileReaderTask task = new ExpressionFileReaderTask(dataset);
        task.run(taskMonitor);


        //There was one more gene in the expression file that wasn't in the set of genes
        //make sure it was was added
        assertEquals(4,map.getGenes().size());

        assertEquals(4, map.getDataset(EnrichmentMap.DATASET1).getExpressionSets().getNumGenes());
        assertEquals(3, map.getDataset(EnrichmentMap.DATASET1).getExpressionSets().getNumConditions());
        assertEquals(0.47536945, map.getDataset(EnrichmentMap.DATASET1).getExpressionSets().getMinExpression(),0.0);
        assertEquals(0.5418719, map.getDataset(EnrichmentMap.DATASET1).getExpressionSets().getMaxExpression(),0.0);

    }
    
	@Test
    public void testGenericFileReader_5columns() throws Exception{
        //load the test expression file
        String testDataFileName = "src/test/resources/org/baderlab/csplugins/enrichmentmap/generic_enr_5col.txt";
        
      //create a new instance of the parameters
        EnrichmentMapParameters params = new EnrichmentMapParameters();        
        //set enrichment results file name
        params.getFiles().get(EnrichmentMap.DATASET1).setEnrichmentFileName1(testDataFileName);
        
        //Create a new Enrichment map
        EnrichmentMap map = new EnrichmentMap(params);
                
        //get the default dataset
        DataSet dataset = map.getDataset(EnrichmentMap.DATASET1);

        
        // check if empty
        assertEquals(0, map.getDataset(EnrichmentMap.DATASET1).getEnrichments().getEnrichments().size());
        
        // read
        ParseGenericEnrichmentResults task = new ParseGenericEnrichmentResults(dataset);
        task.run(taskMonitor);


        Map<String, EnrichmentResult> results = map.getDataset(EnrichmentMap.DATASET1).getEnrichments().getEnrichments();
        // check we have 4 results
        assertEquals(4, results.size() );
        
        // check pValues
        assertEquals(0.01,     ((GenericResult)results.get("GO:0000346")).getPvalue(),0.0);
        assertEquals(0.05,     ((GenericResult)results.get("GO:0030904")).getPvalue(),0.0);
        assertEquals(0.05,     ((GenericResult)results.get("GO:0008623")).getPvalue(),0.0);
        assertEquals(5.60E-42, ((GenericResult)results.get("GO:0046540")).getPvalue(),0.0);

        // check getFdrqvalues
        assertEquals(0.02, ((GenericResult)results.get("GO:0000346")).getFdrqvalue(),0.0);
        assertEquals(0.10, ((GenericResult)results.get("GO:0030904")).getFdrqvalue(),0.0);
        assertEquals(0.12, ((GenericResult)results.get("GO:0008623")).getFdrqvalue(),0.0);
        assertEquals(0.03, ((GenericResult)results.get("GO:0046540")).getFdrqvalue(),0.0);

        // check phenotypes
        assertEquals( 1.0, ((GenericResult)results.get("GO:0000346")).getNES(),0.0);
        assertEquals( 1.0, ((GenericResult)results.get("GO:0030904")).getNES(),0.0);
        assertEquals(-1.0, ((GenericResult)results.get("GO:0008623")).getNES(),0.0);
        assertEquals(-1.0, ((GenericResult)results.get("GO:0046540")).getNES(),0.0);
        
        return;
    }
    
    //test GSEA enrichment results reader
	@Test
    public void testGSEAEnrichmentsReader() throws Exception{

        //load the test enrichment files - GSEA creates two enrichment results files.
        String testDataFileName = "src/test/resources/org/baderlab/csplugins/enrichmentmap/GSEA_enrichments1.xls";
        String testDataFileName2 ="src/test/resources/org/baderlab/csplugins/enrichmentmap/GSEA_enrichments2.xls";

      //create a new instance of the parameters
        EnrichmentMapParameters params = new EnrichmentMapParameters();        
        //set enrichment file name 
        params.getFiles().get(EnrichmentMap.DATASET1).setEnrichmentFileName1(testDataFileName);
        params.getFiles().get(EnrichmentMap.DATASET1).setEnrichmentFileName2(testDataFileName2);
        //Create a new Enrichment map
        EnrichmentMap map = new EnrichmentMap(params);
                
        //get the default dataset
        DataSet dataset = map.getDataset(EnrichmentMap.DATASET1);
        
        ParseGSEAEnrichmentResults task = new ParseGSEAEnrichmentResults(dataset);
        task.run(taskMonitor);
        
        //Get the enrichment
        Map<String, EnrichmentResult> enrichments = map.getDataset(EnrichmentMap.DATASET1).getEnrichments().getEnrichments();
        
        assertEquals(40,enrichments.size());
        
        //Check the contents of some of the genesets
        // example from file 1 (ANTIGEN PROCESSING AND PRESENTATION%KEGG%HSA04612)
        //check p-values
        assertEquals(0.0,((GSEAResult)enrichments.get("ANTIGEN PROCESSING AND PRESENTATION%KEGG%HSA04612")).getPvalue(),0.0);
        //check fdr value
        assertEquals(0.086938426,((GSEAResult)enrichments.get("ANTIGEN PROCESSING AND PRESENTATION%KEGG%HSA04612")).getFdrqvalue(),0.0);
        //check ES value
        assertEquals(0.6854155,((GSEAResult)enrichments.get("ANTIGEN PROCESSING AND PRESENTATION%KEGG%HSA04612")).getES(),0.0);       
        //check NES
        assertEquals(2.1194055,((GSEAResult)enrichments.get("ANTIGEN PROCESSING AND PRESENTATION%KEGG%HSA04612")).getNES(),0.0);
        //check ranks at max
        assertEquals(836,((GSEAResult)enrichments.get("ANTIGEN PROCESSING AND PRESENTATION%KEGG%HSA04612")).getRankAtMax());        
        //check size
        assertEquals(27,((GSEAResult)enrichments.get("ANTIGEN PROCESSING AND PRESENTATION%KEGG%HSA04612")).getGsSize());
        
        // example from file 2 (EMBRYONIC HEART TUBE MORPHOGENESIS%GO%GO:0003143)
        //check p-values
        assertEquals(0.040152963,((GSEAResult)enrichments.get("EMBRYONIC HEART TUBE MORPHOGENESIS%GO%GO:0003143")).getPvalue(),0.0);
        //check fdr value
        assertEquals(1.0,((GSEAResult)enrichments.get("EMBRYONIC HEART TUBE MORPHOGENESIS%GO%GO:0003143")).getFdrqvalue(),0.0);
        //check ES value
        assertEquals(-0.49066687,((GSEAResult)enrichments.get("EMBRYONIC HEART TUBE MORPHOGENESIS%GO%GO:0003143")).getES(),0.0);       
        //check NES
        assertEquals(-1.477554,((GSEAResult)enrichments.get("EMBRYONIC HEART TUBE MORPHOGENESIS%GO%GO:0003143")).getNES(),0.0);
        //check ranks at max
        assertEquals(1597,((GSEAResult)enrichments.get("EMBRYONIC HEART TUBE MORPHOGENESIS%GO%GO:0003143")).getRankAtMax());        
        //check size
        assertEquals(17,((GSEAResult)enrichments.get("EMBRYONIC HEART TUBE MORPHOGENESIS%GO%GO:0003143")).getGsSize());
        
        
    }

	//test GSEA enrichment results reader
	@Test
    public void testGSEAEDBEnrichmentsReader() throws Exception{

        //load the test enrichment files - GSEA creates two enrichment results files.
        String testDataFileName = "src/test/resources/org/baderlab/csplugins/enrichmentmap/task/LoadDataset/GSEA_example_results/edb/results.edb";
       
      //create a new instance of the parameters
        EnrichmentMapParameters params = new EnrichmentMapParameters();        
        //set enrichment file name 
        params.getFiles().get(EnrichmentMap.DATASET1).setEnrichmentFileName1(testDataFileName);
 
        //Create a new Enrichment map
        EnrichmentMap map = new EnrichmentMap(params);
                
        //get the default dataset
        DataSet dataset = map.getDataset(EnrichmentMap.DATASET1);
        
        ParseEDBEnrichmentResults task = new ParseEDBEnrichmentResults(dataset);
        task.run(taskMonitor);
        
        //Get the enrichment
        Map<String, EnrichmentResult> enrichments = map.getDataset(EnrichmentMap.DATASET1).getEnrichments().getEnrichments();
        
        assertEquals(14,enrichments.size());
        
        //Check the contents of some of the genesets
        // example from file 1 (ANTIGEN PROCESSING AND PRESENTATION%KEGG%HSA04612)
        //check p-values
        assertEquals(0.2271,((GSEAResult)enrichments.get("PROTEASOME ACTIVATOR COMPLEX%GO%GO:0008537")).getPvalue(),0.0);
        //check fdr value
        assertEquals(0.2447,((GSEAResult)enrichments.get("PROTEASOME ACTIVATOR COMPLEX%GO%GO:0008537")).getFdrqvalue(),0.0);
        //check ES value
        assertEquals(0.7852,((GSEAResult)enrichments.get("PROTEASOME ACTIVATOR COMPLEX%GO%GO:0008537")).getES(),0.0);       
        //check NES
        assertEquals(1.1793,((GSEAResult)enrichments.get("PROTEASOME ACTIVATOR COMPLEX%GO%GO:0008537")).getNES(),0.0);
        //check ranks at max
        assertEquals(6,((GSEAResult)enrichments.get("PROTEASOME ACTIVATOR COMPLEX%GO%GO:0008537")).getRankAtMax());        
        //check size
        assertEquals(2,((GSEAResult)enrichments.get("PROTEASOME ACTIVATOR COMPLEX%GO%GO:0008537")).getGsSize());
        
        // example from file 2 (EMBRYONIC HEART TUBE MORPHOGENESIS%GO%GO:0003143)
        //check p-values
        assertEquals(0.4545,((GSEAResult)enrichments.get("PROTEASOME COMPLEX%GO%GO:0000502")).getPvalue(),0.0);
        //check fdr value
        assertEquals(0.8650,((GSEAResult)enrichments.get("PROTEASOME COMPLEX%GO%GO:0000502")).getFdrqvalue(),0.0);
        //check ES value
        assertEquals(-0.4707,((GSEAResult)enrichments.get("PROTEASOME COMPLEX%GO%GO:0000502")).getES(),0.0);       
        //check NES
        assertEquals(-0.9696,((GSEAResult)enrichments.get("PROTEASOME COMPLEX%GO%GO:0000502")).getNES(),0.0);
 
        //check ranks at max
        //The Rank at max in the edb file is different from the excel files.  In the excel file that we have been
        //  using up until now they convert the rank as if you are counting from the bottom of the list but in the 
        //edb file they count from the top of the ranked list (going from positive to negative ES scores)
        assertEquals(15,((GSEAResult)enrichments.get("PROTEASOME COMPLEX%GO%GO:0000502")).getRankAtMax());        
        //check size
        assertEquals(39,((GSEAResult)enrichments.get("PROTEASOME COMPLEX%GO%GO:0000502")).getGsSize());
        
        
    }
    
   
    //test Bingo enrichment results
	@Test
    public void testBingoEnrichmentsReader() throws Exception{

        //load the test enrichment files - Bingo
        String testDataFileName = "src/test/resources/org/baderlab/csplugins/enrichmentmap/BingoResults.bgo";

      //create a new instance of the parameters
        EnrichmentMapParameters params = new EnrichmentMapParameters();        
        //set enrichment file name 
        params.getFiles().get(EnrichmentMap.DATASET1).setEnrichmentFileName1(testDataFileName);

        //Create a new Enrichment map
        EnrichmentMap map = new EnrichmentMap(params);
                
        //get the default dataset
        DataSet dataset = map.getDataset(EnrichmentMap.DATASET1);
        

        ParseBingoEnrichmentResults task = new ParseBingoEnrichmentResults(dataset);
        task.run(taskMonitor);
        
        //Get the enrichment
        Map<String, EnrichmentResult> enrichments = map.getDataset(EnrichmentMap.DATASET1).getEnrichments().getEnrichments();
        
        assertEquals(74,enrichments.size());
        
        //check p-values
        assertEquals(0.0010354,((GenericResult)enrichments.get("NUCLEOLAR PART")).getPvalue(),0.0);
        //check fdr value
        assertEquals(0.047796,((GenericResult)enrichments.get("NUCLEOLAR PART")).getFdrqvalue(),0.0);
        //check geneset siz
        assertEquals(5,((GenericResult)enrichments.get("NUCLEOLAR PART")).getGsSize());       
        
        //check p-values
        assertEquals(0.0000000000016209,((GenericResult)enrichments.get("NUCLEOLUS")).getPvalue(),0.0);
        //check fdr value
        assertEquals(0.0000000042203,((GenericResult)enrichments.get("NUCLEOLUS")).getFdrqvalue(),0.0);
        //check geneset siz
        assertEquals(43,((GenericResult)enrichments.get("NUCLEOLUS")).getGsSize());       
        
     
    }
    
    //test David enrichment results reader
	@Test
    public void testDavidEnrichmentsReader() throws Exception{

        //load the test enrichment files - Bingo
        String testDataFileName = "src/test/resources/org/baderlab/csplugins/enrichmentmap/DavidResults.txt";

      //create a new instance of the parameters
        EnrichmentMapParameters params = new EnrichmentMapParameters();        
        //set enrichment file name 
        params.getFiles().get(EnrichmentMap.DATASET1).setEnrichmentFileName1(testDataFileName);

        //Create a new Enrichment map
        EnrichmentMap map = new EnrichmentMap(params);
                
        //get the default dataset
        DataSet dataset = map.getDataset(EnrichmentMap.DATASET1);
        

        ParseDavidEnrichmentResults task = new ParseDavidEnrichmentResults(dataset);
        task.run(taskMonitor);
        
        //Get the enrichment
        Map<String, EnrichmentResult> enrichments = map.getDataset(EnrichmentMap.DATASET1).getEnrichments().getEnrichments();
        
        assertEquals(215,enrichments.size());
        
        //check p-values
        assertEquals(0.00000005210169741980237,((GenericResult)enrichments.get("GO:0031974~MEMBRANE-ENCLOSED LUMEN")).getPvalue(),0.0);
        //check fdr value
        assertEquals(0.000016724505445320226,((GenericResult)enrichments.get("GO:0031974~MEMBRANE-ENCLOSED LUMEN")).getFdrqvalue(),0.0);
        //check geneset siz
        assertEquals(95,((GenericResult)enrichments.get("GO:0031974~MEMBRANE-ENCLOSED LUMEN")).getGsSize());
        
      //check p-values
        assertEquals(0.0009179741851709047,((GenericResult)enrichments.get(((String)"domain:Leucine-zipper").toUpperCase())).getPvalue(),0.0);
        //check fdr value
        assertEquals(0.46717397126592464,((GenericResult)enrichments.get(((String)"domain:Leucine-zipper").toUpperCase())).getFdrqvalue(),0.0);
        //check geneset siz
        assertEquals(11,((GenericResult)enrichments.get(((String)"domain:Leucine-zipper").toUpperCase())).getGsSize());
        

    }
}
