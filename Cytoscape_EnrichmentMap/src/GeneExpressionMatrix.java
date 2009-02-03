
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

/**
 * Created by
 * User: risserlin
 * Date: Jan 30, 2009
 * Time: 9:32:17 AM
 */
public class GeneExpressionMatrix {

    private String[] columnNames;

    private int numConditions;
    private int numGenes;

    private HashMap expressionMatrix;

    private double maxExpression = 0;
    private double minExpression = 0;

    public GeneExpressionMatrix(String[] columnNames) {
        numConditions = columnNames.length;
        this.columnNames = columnNames;
    }

    public String[] getColumnNames() {
        return columnNames;
    }

    public void setColumnNames(String[] columnNames) {
        this.columnNames = columnNames;
    }

    public int getNumConditions() {
        return numConditions;
    }

    public void setNumConditions(int numConditions) {
        this.numConditions = numConditions;
    }

    public int getNumGenes() {
        return numGenes;
    }

    public void setNumGenes(int numGenes) {
        this.numGenes = numGenes;
    }

    public HashMap getExpressionMatrix() {
        return expressionMatrix;
    }

    public void setExpressionMatrix(HashMap expressionMatrix) {
        this.expressionMatrix = expressionMatrix;
    }

    public HashMap getExpressionMatrix(HashSet<Integer> subset){
        HashMap expression_subset = new HashMap();

        //go through the expression matrix and get the subset of
        //genes of interest
        for(Iterator i = subset.iterator(); i.hasNext();){
            int k = (Integer)i.next();
            if(expressionMatrix.containsKey(k)){
                expression_subset.put(k,expressionMatrix.get(k));
            }
            else{
                System.out.println("how is this key not in the hashmap?");
            }

        }

        return expression_subset;

    }

    public double getMaxExpression() {
        return maxExpression;
    }

    public void setMaxExpression(double maxExpression) {
        this.maxExpression = maxExpression;
    }

    public double getMinExpression() {
        return minExpression;
    }

    public void setMinExpression(double minExpression) {
        this.minExpression = minExpression;
    }
}
