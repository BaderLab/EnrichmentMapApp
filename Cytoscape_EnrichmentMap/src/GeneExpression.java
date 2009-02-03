
/**
 * Created by
 * User: risserlin
 * Date: Jan 29, 2009
 * Time: 3:49:44 PM
 */
public class GeneExpression {

    private String name;
    private String description;

    private Double[] expression;

    private String[] row;

    public GeneExpression(String name, String description) {
        this.name = name;
        this.description = description;
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

    public Double[] getExpression() {
        return expression;
    }

    public void setExpression(Double[] expression) {
        this.expression = expression;
    }

    //Given a string representing a line in the file create
    //an array of expression values
    public void setExpression(String[] expres){

        row = expres;
        //ignore the first two cells
        int size = expres.length;

        expression = new Double[size-2];

        for(int i = 2; i< size;i++){
            expression[i-2] = Double.parseDouble(expres[i]);
        }

    }

    //given an expression row and the current maximum is there an element
    //that is higher and should be the new max
    public double newMax(double currentMax){
        double newMax = -1;

        for(int i =0;i<expression.length;i++){
            if(expression[i] > currentMax)
                newMax = expression[i];
        }
        return newMax;
    }

    public double newMin(double currentMin){
        double newMin = -1;

        for(int i =0;i<expression.length;i++){
            if(expression[i] < currentMin)
                newMin = expression[i];
        }
        return newMin;
    }

    public String[] getRow() {
        return row;
    }
}
