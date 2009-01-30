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
    private String GCTFileName;
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

    private HashMap expression;

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
        this.expression = new HashMap();
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

    public String getGCTFileName() {
        return GCTFileName;
    }

    public void setGCTFileName(String GCTFileName) {
        this.GCTFileName = GCTFileName;
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

    public boolean isFDR() {
        return FDR;
    }

    public void setFDR(boolean FDR) {
        this.FDR = FDR;
    }

    public HashMap getExpression() {
        return expression;
    }

    public void setExpression(HashMap expression) {
        this.expression = expression;
    }
}
