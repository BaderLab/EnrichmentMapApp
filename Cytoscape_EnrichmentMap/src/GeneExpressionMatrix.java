
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
    private HashMap expressionMatrix_rowNormalized;

    private double maxExpression = 0;
    private double minExpression = 0;

    private String[] phenotypes;


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

    public HashMap getExpressionMatrix_rowNormalized() {
        return expressionMatrix_rowNormalized;
    }

    public void setExpressionMatrix_rowNormalized(HashMap expressionMatrix_rowNormalized) {
        this.expressionMatrix_rowNormalized = expressionMatrix_rowNormalized;
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

    public double getMeanExpression(HashMap currentMatrix){
        double sum = 0.0;
        int k = 0;
        //go through the expression matrix
        for(Iterator i = currentMatrix.keySet().iterator(); i.hasNext();){
            Double[] currentRow = ((GeneExpression)currentMatrix.get(i.next())).getExpression();
            for(int j = 0; j< currentRow.length;j++){
                sum = sum + currentRow[j];
                k++;
            }

        }

        return sum/k;

    }

      public double getMaxExpression(HashMap currentMatrix){
        double max = 0.0;
          if(currentMatrix != null){
            //go through the expression matrix
            for(Iterator i = currentMatrix.keySet().iterator(); i.hasNext();){
                Double[] currentRow = ((GeneExpression)currentMatrix.get(i.next())).getExpression();
                for(int j = 0; j< currentRow.length;j++){
                    if(max < currentRow[j])
                        max = currentRow[j];
                }

            }
          }
        return max;

    }

public double getMinExpression(HashMap currentMatrix){
        double min = 0.0;
        //go through the expression matrix
        if(currentMatrix != null){
            for(Iterator i = currentMatrix.keySet().iterator(); i.hasNext();){
                Double[] currentRow = ((GeneExpression)currentMatrix.get(i.next())).getExpression();
                for(int j = 0; j< currentRow.length;j++){
                    if(min > currentRow[j])
                        min = currentRow[j];
                }

            }
        }
        return min;

    }

    public double getSTDExpression(double mean, HashMap currentMatrix){
        double sum = 0.0;
        int k= 0;
        //go through the expression matrix
        for(Iterator i = currentMatrix.keySet().iterator(); i.hasNext();){
            Double[] currentRow = ((GeneExpression)currentMatrix.get(i.next())).getExpression();
            for(int j = 0; j< currentRow.length;j++){
                sum = sum + Math.pow((currentRow[j]-mean),2);
                k++;
            }
       }

        return Math.sqrt(sum)/k;
    }

    public void rowNormalizeMatrix(){

        if(expressionMatrix == null)
            return;

        //create new matrix
        expressionMatrix_rowNormalized = new HashMap();
       
         int k= 0;
        //go through the expression matrix
        for(Iterator i = expressionMatrix.keySet().iterator(); i.hasNext();){
            GeneExpression currentexpression = ((GeneExpression)expressionMatrix.get(i.next()));
            String Name = currentexpression.getName();
            String description = currentexpression.getDescription();
            GeneExpression norm_row = new GeneExpression(Name,description);
            Double[] currentexpression_row_normalized = currentexpression.rowNormalize();
            norm_row.setExpression( currentexpression_row_normalized);

            expressionMatrix_rowNormalized.put(Name,norm_row);
       }

    }

    public String[] getPhenotypes() {
        return phenotypes;
    }

    public void setPhenotypes(String[] phenotypes) {
        this.phenotypes = phenotypes;
    }

}
