import java.util.HashMap;

/**
 * Created by
 * User: risserlin
 * Date: Jan 8, 2009
 * Time: 12:04:28 PM
 */
public class EnrichmentMapParameters {

    //GMT and GSEA output files
    private String GMTFileName;
    private String GSEAUpFileName;
    private String GSEADownFileName;

    //p-value cutoff
    private double pvalue;

    //fdr q-value cutoff
    private double qvalue;

    private double jaccardCutOff;

    //Hashmap stores the unique set of genes used in the gmt file
    private HashMap genes;
    private int NumberOfGenes = 0;

    //Hashmap of the GSEA Results, It is is a hash of the GSEAResults objects
    private HashMap gseaResults;
    private HashMap genesets;

    private HashMap gseaResultsOfInterest;
    private HashMap genesetsOfInterest;

    public EnrichmentMapParameters() {
        this.gseaResults = new HashMap();
        this.genes = new HashMap();
        this.genesets = new HashMap();
        this.gseaResultsOfInterest = new HashMap();
        this.genesetsOfInterest = new HashMap();

    }

    public EnrichmentMapParameters(String GMTFileName, String GSEAUpFileName, String GSEADownFileName, double pvalue, double qvalue) {
        this.GMTFileName = GMTFileName;
        this.GSEAUpFileName = GSEAUpFileName;
        this.GSEADownFileName = GSEADownFileName;
        this.pvalue = pvalue;
        this.qvalue = qvalue;
        this.gseaResults = new HashMap();
        this.genes = new HashMap();
        this.genesets = new HashMap();
        this.gseaResultsOfInterest = new HashMap();
        this.genesetsOfInterest = new HashMap();
    }

    public HashMap getGseaResultsOfInterest() {
        return gseaResultsOfInterest;
    }

    public void setGseaResultsOfInterest(HashMap gseaResultsOfInterest) {

        this.gseaResultsOfInterest = gseaResultsOfInterest;

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

    public HashMap getGseaResults() {
        return gseaResults;
    }

    public void setGseaResults(HashMap gseaResults) {
        this.gseaResults = gseaResults;
    }

    public String getGMTFileName() {

        return GMTFileName;
    }

    public void setGMTFileName(String GMTFileName) {
        this.GMTFileName = GMTFileName;
    }

    public String getGSEAUpFileName() {
        return GSEAUpFileName;
    }

    public void setGSEAUpFileName(String GSEAUpFileName) {
        this.GSEAUpFileName = GSEAUpFileName;
    }

    public String getGSEADownFileName() {
        return GSEADownFileName;
    }

    public void setGSEADownFileName(String GSEADownFileName) {
        this.GSEADownFileName = GSEADownFileName;
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
}
