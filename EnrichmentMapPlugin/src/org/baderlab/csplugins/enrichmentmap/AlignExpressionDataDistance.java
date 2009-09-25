package org.baderlab.csplugins.enrichmentmap;

import org.baderlab.csplugins.brainlib.DistanceMetric;

/**
 * Created by
 * User: risserlin
 * Date: Sep 24, 2009
 * Time: 3:29:24 PM
 */
public class AlignExpressionDataDistance  extends DistanceMetric {

    public double calc(Object expr1,Object expr2){

//        if ((!(expr1 instanceof double[])) || (!(expr2 instanceof double[]))) {
///            throw new RuntimeException("Non expression values passed to AlignExpressionDataDistance");
//        }

       Double[] x = (Double[])expr1;
       Double[] y = (Double[])expr2;

        double result = 0;
        double sum_x = 0;
        double sum_y = 0;
        double sum_xy = 0;
        double sum_sq_x = 0;
        double sum_sq_y = 0;
        double sum_coproduct = 0;

        //calculate the means of the data.
        for(int i = 0; i<x.length;i++){
              //calculate all the values need for the pearson correlation
              sum_x += x[i];
              sum_y += y[i];

              sum_xy += x[i] * y[i];

              sum_sq_x += x[i] * x[i];
              sum_sq_y += y[i] * y[i];
        }
        // make all variables means instead of sums.
        sum_x = sum_x / x.length;
        sum_y = sum_y / y.length;
        sum_xy = sum_xy / x.length;
        sum_sq_x = sum_sq_x / x.length;
        sum_sq_y = sum_sq_y / y.length;

        double numerator = sum_xy - ((sum_x * sum_y));
        double denominator = (Math.sqrt(sum_sq_x - ((sum_x * sum_x))) * Math.sqrt((sum_sq_y - ((sum_y * sum_y)))));
        result = numerator / denominator;
        return 1-result;
    }
}

