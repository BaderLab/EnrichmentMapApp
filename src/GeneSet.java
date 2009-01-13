
import java.util.HashSet;

/**
 * Created by
 * User: risserlin
 * Date: Jan 8, 2009
 * Time: 11:32:40 AM
 */

/* Geneset
 * Each Geneset consists of:
 * Name
 * Description
 * A list of genes in the geneset (represented using a HashSet)
 */

public class GeneSet {

    private String Name;
    private String Description;

    private HashSet<Integer> genes = null;

    public GeneSet(String name, String descrip) {
        this.Name = name;
        this.Description = descrip;

        genes = new HashSet<Integer>();

    }
    /* Given a Hashkey
    * Add the gene hashkey to the set of genes
    */
    public boolean addGene(int gene_hashkey){
        if(genes != null){
            return genes.add(gene_hashkey);
        }
        else{
            return false;
        }
    }


    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getDescription() {
        return Description;
    }

    public void setDescription(String description) {
        Description = description;
    }

    public HashSet<Integer> getGenes() {
        return genes;
    }

    public void setGenes(HashSet<Integer> genes) {
        this.genes = genes;
    }


}
