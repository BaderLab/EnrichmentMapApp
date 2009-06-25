/**
 * Created by
 * User: risserlin
 * Date: Jan 28, 2009
 * Time: 3:25:51 PM
 */
public class GenericResult {

    private String name = "";
    private String description = "";
    private double pvalue = 1.0;
    private int gsSize = 0;

    //optional parameters
    private double fdrqvalue = 1.0;

    //make the generic file synonamous with gsea result file
    //the phenotype is deduced from the sign of the ES score so create a variable to store
    //the phenotype.
    private double NES = 1.0;

    public GenericResult(String[] tokens){
            //ignore the first token as it is from the hash
            this.name = tokens[1];
            this.description = tokens[2];
            this.pvalue = Double.parseDouble(tokens[3]);
            this.gsSize = Integer.parseInt(tokens[4]);
            this.fdrqvalue = Double.parseDouble(tokens[5]);
            this.NES = Double.parseDouble(tokens[6]);

        }


    public GenericResult(String name, String description, double pvalue, int gs_size) {
        this.name = name;
        this.description = description;
        this.pvalue = pvalue;
        this.gsSize = gs_size;
    }

    public GenericResult(String name, String description, double pvalue, int gs_size, double fdrqvalue) {
        this.name = name;
        this.description = description;
        this.pvalue = pvalue;
        this.gsSize = gs_size;
        this.fdrqvalue = fdrqvalue;
    }

    public GenericResult(String name, String description, double pvalue, int gs_size, double fdrqvalue, double phenotype) {
         this.name = name;
        this.description = description;
        this.gsSize = gs_size;
        this.pvalue = pvalue;
        this.fdrqvalue = fdrqvalue;
        this.NES = phenotype;
    }

    public boolean geneSetOfInterest(double pvalue, double fdrqvalue, boolean useFDR){
        if(useFDR){
            if((this.pvalue <= pvalue) && (this.fdrqvalue <= fdrqvalue)){
                return true;
            }else{
                return false;
            }
        }
        else{
            if(this.pvalue <= pvalue){
                return true;
            }else{
                return false;
            }
        }
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getPvalue() {
        return pvalue;
    }

    public void setPvalue(double pvalue) {
        this.pvalue = pvalue;
    }

    public double getFdrqvalue() {
        return fdrqvalue;
    }

    public void setFdrqvalue(double fdrqvalue) {
        this.fdrqvalue = fdrqvalue;
    }

    public int getGsSize() {
        return gsSize;
    }

    public void setGsSize(int gs_size) {
        this.gsSize = gs_size;
    }

    public double getNES() {
        return NES;
    }

    public void setNES(double NES) {
        this.NES = NES;
    }

    public String toString(){

        return name + "\t" + description + "\t" + pvalue + "\t" + gsSize + "\t" + fdrqvalue + "\t" + NES +"\n";
    }
}
