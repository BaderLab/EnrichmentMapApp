import java.util.HashMap;
import java.util.Iterator;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by
 * User: risserlin
 * Date: Jan 8, 2009
 * Time: 12:04:28 PM
 */
public class EnrichmentMapParameters {

    //GMT and GSEA output files
    private String GMTFileName;
    private String GCTFileName1;
    private String GCTFileName2;
    private String enrichmentDataset1FileName1;
    private String enrichmentDataset1FileName2;

    private String enrichmentDataset2FileName1;
    private String enrichmentDataset2FileName2;

    private boolean twoDatasets = false;

    private boolean jaccard;

    //p-value cutoff
    private double pvalue;

    //fdr q-value cutoff
    private double qvalue;

    private double jaccardCutOff;

    //flag to indicate if the results are from GSEA or generic
    private boolean GSEA = true;
    //flag to indicate if the user has supplied a data file
    private boolean Data = false;
    //flag to indicate if the user has supplied a data file
    private boolean Data2 = false;
    //falg to indicate if there are FDR Q-values
    private boolean FDR = false;

    //Hashmap stores the unique set of genes used in the gmt file
    private HashMap genes;
    private HashSet datasetGenes;
    private int NumberOfGenes = 0;

    //Hashmap of the GSEA Results, It is is a hash of the GSEAResults objects
    private HashMap enrichmentResults1;
    private HashMap enrichmentResults2;
    private HashMap genesets;
    private HashMap filteredGenesets;

    //The GSEA results that pass the thresholds.
    //If there are two datasets these list can be different.
    private HashMap enrichmentResults1OfInterest;
    private HashMap enrichmentResults2OfInterest;

    private HashMap genesetsOfInterest;

    private GeneExpressionMatrix expression;
    private GeneExpressionMatrix expression2;

    private String phenotype1;
    private String phenotype2;

    private String classFile1;
    private String classFile2;

    private HashMap<String, GenesetSimilarity> genesetSimilarity;

    public EnrichmentMapParameters() {
        this.enrichmentResults1 = new HashMap();
        this.enrichmentResults2 = new HashMap();
        this.genes = new HashMap();
        this.datasetGenes = new HashSet();
        this.genesets = new HashMap();
        this.filteredGenesets = new HashMap();
        this.enrichmentResults1OfInterest = new HashMap();
        this.enrichmentResults2OfInterest = new HashMap();
        this.genesetsOfInterest = new HashMap();
        jaccard = true;

    }

    public EnrichmentMapParameters(String GMTFileName,  double pvalue, double qvalue) {
        this();
        this.GMTFileName = GMTFileName;
        this.pvalue = pvalue;
        this.qvalue = qvalue;

    }

    public boolean isJaccard() {
        return jaccard;
    }

    public void setJaccard(boolean jaccard) {
        this.jaccard = jaccard;
    }

    public HashMap getEnrichmentResults1() {
        return enrichmentResults1;
    }

    public void setEnrichmentResults1(HashMap enrichmentResults1) {
        this.enrichmentResults1 = enrichmentResults1;
    }

    public HashMap getEnrichmentResults2() {
        return enrichmentResults2;
    }

    public void setEnrichmentResults2(HashMap enrichmentResults2) {
        this.enrichmentResults2 = enrichmentResults2;
    }

    public HashMap getEnrichmentResults1OfInterest() {
        return enrichmentResults1OfInterest;
    }

    public void setEnrichmentResults1OfInterest(HashMap enrichmentResults1OfInterest) {
        this.enrichmentResults1OfInterest = enrichmentResults1OfInterest;
    }

    public HashMap getEnrichmentResults2OfInterest() {
        return enrichmentResults2OfInterest;
    }

    public void setEnrichmentResults2OfInterest(HashMap enrichmentResults2OfInterest) {
        this.enrichmentResults2OfInterest = enrichmentResults2OfInterest;
    }

    public HashMap getGenesetsOfInterest() {

        return genesetsOfInterest;
    }

    public void setGenesetsOfInterest(HashMap genesetsOfInterest) {
        this.genesetsOfInterest = genesetsOfInterest;



    }

    public HashMap getGenesets() {
        return genesets;
    }

    public void setGenesets(HashMap genesets) {
        this.genesets = genesets;
    }

    public HashMap getFilteredGenesets() {
        return filteredGenesets;
    }

    public void setFilteredGenesets(HashMap filteredGenesets) {
        this.filteredGenesets = filteredGenesets;
    }

    public String getGMTFileName() {

        return GMTFileName;
    }

    public void setGMTFileName(String GMTFileName) {
        this.GMTFileName = GMTFileName;
    }

    public String getGCTFileName1() {
        return GCTFileName1;
    }

    public void setGCTFileName1(String GCTFileName) {
        this.GCTFileName1 = GCTFileName;
    }


    public String getGCTFileName2() {
        return GCTFileName2;
    }

    public void setGCTFileName2(String GCTFileName) {
        this.GCTFileName2 = GCTFileName;
    }

    public String getEnrichmentDataset1FileName1() {
        return enrichmentDataset1FileName1;
    }

    public void setEnrichmentDataset1FileName1(String enrichmentDataset1FileName1) {
        this.enrichmentDataset1FileName1 = enrichmentDataset1FileName1;
    }

    public String getEnrichmentDataset1FileName2() {
        return enrichmentDataset1FileName2;
    }

    public void setEnrichmentDataset1FileName2(String enrichmentDataset1FileName2) {
        this.enrichmentDataset1FileName2 = enrichmentDataset1FileName2;
    }

    public String getEnrichmentDataset2FileName1() {
        return enrichmentDataset2FileName1;
    }

    public void setEnrichmentDataset2FileName1(String enrichmentDataset2FileName1) {
        this.enrichmentDataset2FileName1 = enrichmentDataset2FileName1;
    }

    public String getEnrichmentDataset2FileName2() {
        return enrichmentDataset2FileName2;
    }

    public void setEnrichmentDataset2FileName2(String enrichmentDataset2FileName2) {
        this.enrichmentDataset2FileName2 = enrichmentDataset2FileName2;
    }

    public double getPvalue() {
        return pvalue;
    }

    public void setPvalue(double pvalue) {
        this.pvalue = pvalue;
    }

    public double getQvalue() {
        return qvalue;
    }

    public void setQvalue(double qvalue) {
        this.qvalue = qvalue;
    }

    public HashMap getGenes() {
        return genes;
    }

    public void setGenes(HashMap genes) {
        this.genes = genes;
    }

    public HashSet getDatasetGenes() {
        return datasetGenes;
    }

    public void setDatasetGenes(HashSet datasetGenes) {
        this.datasetGenes = datasetGenes;
    }

    public int getNumberOfGenes() {
        return NumberOfGenes;
    }

    public void setNumberOfGenes(int numberOfGenes) {
        NumberOfGenes = numberOfGenes;
    }

    public double getJaccardCutOff() {
        return jaccardCutOff;
    }

    public void setJaccardCutOff(double jaccardCutOff) {
        this.jaccardCutOff = jaccardCutOff;
    }

    public boolean isTwoDatasets() {
        return twoDatasets;
    }

    public void setTwoDatasets(boolean twoDatasets) {
        this.twoDatasets = twoDatasets;
    }

    public void noFilter(){
        this.filteredGenesets = genesets;
    }

    public void filterGenesets(){
        //iterate through each geneset and filter each one
         for(Iterator j = genesets.keySet().iterator(); j.hasNext(); ){

             String geneset2_name = j.next().toString();
             GeneSet current_set = (GeneSet) genesets.get(geneset2_name);

             //compare the HashSet of dataset genes to the HashSet of the current Geneset
             //only keep the genes from the geneset that are in the dataset genes
             HashSet<Integer> geneset_genes = current_set.getGenes();

             //Get the intersection between current geneset and dataset genes
             Set<Integer> intersection = new HashSet<Integer>(geneset_genes);
             intersection.retainAll(datasetGenes);

             //Add new geneset to the filtered set of genesets
             HashSet<Integer> new_geneset = new HashSet<Integer>(intersection);
             GeneSet new_set = new GeneSet(geneset2_name,current_set.getDescription());
             new_set.setGenes(new_geneset);

             this.filteredGenesets.put(geneset2_name,new_set);

         }
        //once we have filtered the genesets clear the original genesets object
        genesets.clear();
    }

    public void dispose(){
        genesets.clear();
        genesetsOfInterest.clear();
        enrichmentResults1.clear();
        enrichmentResults2.clear();
        genes.clear();
        datasetGenes.clear();        
        filteredGenesets.clear();
        enrichmentResults1OfInterest.clear();
        enrichmentResults2OfInterest.clear();

    }

    public boolean isGSEA() {
        return GSEA;
    }

    public void setGSEA(boolean GSEA) {
        this.GSEA = GSEA;
    }

    public boolean isData() {
        return Data;
    }

    public void setData(boolean data) {
        Data = data;
    }

    public boolean isData2() {
        return Data2;
    }

    public void setData2(boolean data2) {
        Data2 = data2;
    }

    public boolean isFDR() {
        return FDR;
    }

    public void setFDR(boolean FDR) {
        this.FDR = FDR;
    }

    public GeneExpressionMatrix getExpression() {
        return expression;
    }

    public void setExpression(GeneExpressionMatrix expression) {
        this.expression = expression;
    }

    public GeneExpressionMatrix getExpression2() {
        return expression2;
    }

    public void setExpression2(GeneExpressionMatrix expression2) {
        this.expression2 = expression2;
    }

    public HashMap<String, GenesetSimilarity> getGenesetSimilarity() {
        return genesetSimilarity;
    }

    public void setGenesetSimilarity(HashMap<String, GenesetSimilarity> genesetSimilarity) {
        this.genesetSimilarity = genesetSimilarity;
    }

    public String getPhenotype1() {
        return phenotype1;
    }

    public void setPhenotype1(String phenotype1) {
        this.phenotype1 = phenotype1;
    }

    public String getPhenotype2() {
        return phenotype2;
    }

    public void setPhenotype2(String phenotype2) {
        this.phenotype2 = phenotype2;
    }

    public String getClassFile1() {
        return classFile1;
    }

    public void setClassFile1(String classFile1) {
        this.classFile1 = classFile1;
    }

    public String getClassFile2() {
        return classFile2;
    }

    public void setClassFile2(String classFile2) {
        this.classFile2 = classFile2;
    }
}
