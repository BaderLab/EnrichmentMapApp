/**
 * Created by
 * User: risserlin
 * Date: Jan 28, 2009
 * Time: 3:25:51 PM
 */
public class GenericResult {

    private String id = "";
    private String Name = "";
    private double pvalue = 1.0;
    private int gsSize = 0;

    //optional parameters
    private double fdrqvalue = 1.0;
    private String phenotype = "";

    public GenericResult(String id, String name, double pvalue, int gs_size) {
        this.id = id;
        Name = name;
        this.pvalue = pvalue;
        this.gsSize = gs_size;
    }

    public GenericResult(String[] tokens){
        //ignore the first token as it is from the hash
        this.id = tokens[1];
        Name = tokens[2];
        this.pvalue = Double.parseDouble(tokens[3]);
        this.gsSize = Integer.parseInt(tokens[4]);
        this.fdrqvalue = Double.parseDouble(tokens[5]);
        this.phenotype = tokens[6];

    }

    public GenericResult(String id, String name, double pvalue, int gs_size, double fdrqvalue) {
        this.id = id;
        Name = name;
        this.pvalue = pvalue;
        this.gsSize = gs_size;
        this.fdrqvalue = fdrqvalue;
    }

    public GenericResult(String id, String name, double pvalue, double fdrqvalue, String phenotype) {
        this.id = id;
        Name = name;
        this.pvalue = pvalue;
        this.fdrqvalue = fdrqvalue;
        this.phenotype = phenotype;
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


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
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

    public String getPhenotype() {
        return phenotype;
    }

    public void setPhenotype(String phenotype) {
        this.phenotype = phenotype;
    }

    public int getGsSize() {
        return gsSize;
    }

    public void setGsSize(int gs_size) {
        this.gsSize = gs_size;
    }

    public String toString(){

        return id + "\t" + Name + "\t" + pvalue + "\t" + gsSize + "\t" + fdrqvalue + "\t" +phenotype+"\n";
    }
}
