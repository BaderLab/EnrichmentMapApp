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
    private String GSEADataset1FileName1;
    private String GSEADataset1FileName2;

    private String GSEADataset2FileName1;
    private String GSEADataset2FileName2;

    private boolean twoDatasets = false;

    private boolean jaccard;

    //p-value cutoff
    private double pvalue;

    //fdr q-value cutoff
    private double qvalue;

    private double jaccardCutOff;

    //Hashmap stores the unique set of genes used in the gmt file
    private HashMap genes;
    private HashSet datasetGenes;
    private int NumberOfGenes = 0;

    //Hashmap of the GSEA Results, It is is a hash of the GSEAResults objects
    private HashMap gseaResults1;
    private HashMap gseaResults2;
    private HashMap genesets;
    private HashMap filteredGenesets;

    //The GSEA results that pass the thresholds.
    //If there are two datasets these list can be different.
    private HashMap gseaResults1OfInterest;
    private HashMap gseaResults2OfInterest;


    private HashMap genesetsOfInterest;

    public EnrichmentMapParameters() {
        this.gseaResults1 = new HashMap();
        this.gseaResults2 = new HashMap();
        this.genes = new HashMap();
        this.datasetGenes = new HashSet();
        this.genesets = new HashMap();
        this.filteredGenesets = new HashMap();
        this.gseaResults1OfInterest = new HashMap();
        this.gseaResults2OfInterest = new HashMap();
        this.genesetsOfInterest = new HashMap();
        jaccard = true;

    }

    public EnrichmentMapParameters(String GMTFileName,  double pvalue, double qvalue) {
        this.GMTFileName = GMTFileName;
        this.pvalue = pvalue;
        this.qvalue = qvalue;
        this.gseaResults1 = new HashMap();
        this.gseaResults2 = new HashMap();
        this.genes = new HashMap();
        this.datasetGenes = new HashSet();
        this.genesets = new HashMap();
        this.filteredGenesets = new HashMap();
        this.gseaResults1OfInterest = new HashMap();
        this.gseaResults2OfInterest = new HashMap();
        this.genesetsOfInterest = new HashMap();
    }

    public boolean isJaccard() {
        return jaccard;
    }

    public void setJaccard(boolean jaccard) {
        this.jaccard = jaccard;
    }

    public HashMap getGseaResults1() {
        return gseaResults1;
    }

    public void setGseaResults1(HashMap gseaResults1) {
        this.gseaResults1 = gseaResults1;
    }

    public HashMap getGseaResults2() {
        return gseaResults2;
    }

    public void setGseaResults2(HashMap gseaResults2) {
        this.gseaResults2 = gseaResults2;
    }

    public HashMap getGseaResults1OfInterest() {
        return gseaResults1OfInterest;
    }

    public void setGseaResults1OfInterest(HashMap gseaResults1OfInterest) {
        this.gseaResults1OfInterest = gseaResults1OfInterest;
    }

    public HashMap getGseaResults2OfInterest() {
        return gseaResults2OfInterest;
    }

    public void setGseaResults2OfInterest(HashMap gseaResults2OfInterest) {
        this.gseaResults2OfInterest = gseaResults2OfInterest;
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

    public String getGSEADataset1FileName1() {
        return GSEADataset1FileName1;
    }

    public void setGSEADataset1FileName1(String GSEADataset1FileName1) {
        this.GSEADataset1FileName1 = GSEADataset1FileName1;
    }

    public String getGSEADataset1FileName2() {
        return GSEADataset1FileName2;
    }

    public void setGSEADataset1FileName2(String GSEADataset1FileName2) {
        this.GSEADataset1FileName2 = GSEADataset1FileName2;
    }

    public String getGSEADataset2FileName1() {
        return GSEADataset2FileName1;
    }

    public void setGSEADataset2FileName1(String GSEADataset2FileName1) {
        this.GSEADataset2FileName1 = GSEADataset2FileName1;
    }

    public String getGSEADataset2FileName2() {
        return GSEADataset2FileName2;
    }

    public void setGSEADataset2FileName2(String GSEADataset2FileName2) {
        this.GSEADataset2FileName2 = GSEADataset2FileName2;
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
        gseaResults1.clear();
        gseaResults2.clear();
        genes.clear();
        datasetGenes.clear();        
        filteredGenesets.clear();
        gseaResults1OfInterest.clear();
        gseaResults2OfInterest.clear();

    }
}
