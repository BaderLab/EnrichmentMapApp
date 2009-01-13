
import java.util.HashSet;

/**
 * Created by
 * User: risserlin
 * Date: Jan 9, 2009
 * Time: 10:49:55 AM
 */
public class GenesetSimilarity {

    private String geneset1_Name;
    private String geneset2_Name;

    private double jaccard_coeffecient;

    private HashSet<Integer> overlapping_genes;

    public GenesetSimilarity(String geneset1_Name, String geneset2_Name, double jaccard_coeffecient, HashSet<Integer> overlapping_genes) {
        this.geneset1_Name = geneset1_Name;
        this.geneset2_Name = geneset2_Name;
        this.jaccard_coeffecient = jaccard_coeffecient;
        this.overlapping_genes = overlapping_genes;
    }

    public String getGeneset1_Name() {
        return geneset1_Name;
    }

    public void setGeneset1_Name(String geneset1_Name) {
        this.geneset1_Name = geneset1_Name;
    }

    public String getGeneset2_Name() {
        return geneset2_Name;
    }

    public void setGeneset2_Name(String geneset2_Name) {
        this.geneset2_Name = geneset2_Name;
    }

    public double getJaccard_coeffecient() {
        return jaccard_coeffecient;
    }

    public void setJaccard_coeffecient(double jaccard_coeffecient) {
        this.jaccard_coeffecient = jaccard_coeffecient;
    }

    public HashSet<Integer> getOverlapping_genes() {
        return overlapping_genes;
    }

    public void setOverlapping_genes(HashSet<Integer> overlapping_genes) {
        this.overlapping_genes = overlapping_genes;
    }

    public int getSizeOfOverlap(){
        return overlapping_genes.size();
    }
}
