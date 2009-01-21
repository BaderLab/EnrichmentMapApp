/**
 * Created by
 * User: risserlin
 * Date: Jan 8, 2009
 * Time: 3:01:22 PM
 */
public class GSEAResult {

    private String Name;
    private int size;
    private double ES;
    private double NES;
    private double pvalue;
    private double fdrqvalue;
    private double fwerqvalue;

    public GSEAResult(String name, int size, double ES, double NES, double pvalue, double fdrqvalue, double fwerqvalue) {
        Name = name;
        this.size = size;
        this.ES = ES;
        this.NES = NES;
        this.pvalue = pvalue;
        this.fdrqvalue = fdrqvalue;
        this.fwerqvalue = fwerqvalue;
    }

    public boolean geneSetOfInterest(double pvalue, double fdrqvalue){
        if((this.pvalue <= pvalue) && (this.fdrqvalue <= fdrqvalue)){
            return true;
       }else{
            return false;
        }
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public double getES() {
        return ES;
    }

    public void setES(double ES) {
        this.ES = ES;
    }

    public double getNES() {
        return NES;
    }

    public void setNES(double NES) {
        this.NES = NES;
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

    public double getFwerqvalue() {
        return fwerqvalue;
    }

    public void setFwerqvalue(double fwerqvalue) {
        this.fwerqvalue = fwerqvalue;
    }
}
