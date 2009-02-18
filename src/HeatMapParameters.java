
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * Created by
 * User: risserlin
 * Date: Feb 11, 2009
 * Time: 12:23:01 PM
 */
public class HeatMapParameters {
      private ColorGradientRange range;
      private ColorGradientTheme theme;

      private boolean rowNorm = false;
      private boolean logtransform = false;

      private double minExpression;
      private double maxExpression;
      private double meanExpression;
      private double stdExpression;

     private OverlappingGenesPanel edgeOverlapPanel;
     private OverlappingGenesPanel nodeOverlapPanel;

    public HeatMapParameters(OverlappingGenesPanel edgeOverlapPanel, OverlappingGenesPanel nodeOverlapPanel) {
        this.edgeOverlapPanel = edgeOverlapPanel;
        this.nodeOverlapPanel = nodeOverlapPanel;
    }

    public void initColorGradients(GeneExpressionMatrix expression){

        minExpression = expression.getMinExpression();
        maxExpression = expression.getMaxExpression();
        meanExpression = expression.getMeanExpression();
        stdExpression = expression.getSTDExpression(meanExpression);

        double max = Math.max(Math.abs(minExpression), maxExpression);

        double median = 0;

        //if the minimum expression is above zero make it a one colour heatmap
        if(minExpression >= 0){
            range = ColorGradientRange.getInstance(0,max/2, max/2,max, 0,max/2,max/2,max);
            theme = ColorGradientTheme.GREEN_ONECOLOR_GRADIENT_THEME;
        }
        else{
            range = ColorGradientRange.getInstance(-max,median, median,max, -max,median,median,max);
            theme = ColorGradientTheme.GREEN_MAGENTA_GRADIENT_THEME;
        }

    }

    public void ResetColorGradient(){
          double min;
          double max;
          double median;

          if(rowNorm){
              min = (minExpression - meanExpression)/stdExpression;
              max = (maxExpression - meanExpression)/stdExpression;
              max = Math.max(Math.abs(min),max);

          }
          else if(logtransform){
              min = Math.log1p(minExpression);
              max = Math.log1p(maxExpression) ;
              max = Math.max(Math.abs(min),max);

          }
          else
              max = Math.max(Math.abs(minExpression), maxExpression);

          median = max/2;
          if(minExpression >= 0){
              median = max/2;
              range = ColorGradientRange.getInstance(0,median, median,max, 0,median,median,max);
              theme = ColorGradientTheme.GREEN_ONECOLOR_GRADIENT_THEME;
           }
          else{
              median = 0;
              range = ColorGradientRange.getInstance(-max,0, 0,max, -max,0,0,max);
              theme = ColorGradientTheme.GREEN_MAGENTA_GRADIENT_THEME;
          }

      }


    public JPanel createHeatMapOptionsPanel(){

        JPanel heatmapOptions = new JPanel();
        heatmapOptions.setPreferredSize(new Dimension(200,75));
        heatmapOptions.setLayout(new GridLayout(3,1));

        JRadioButton asIs;
        JRadioButton rowNormalized;
        JRadioButton logTransform;
        ButtonGroup dataView;

         asIs = new JRadioButton("Data As Is");
         asIs.setActionCommand("asis");

         rowNormalized = new JRadioButton("Row Normalize Data");
         rowNormalized.setActionCommand("rownorm");

        logTransform = new JRadioButton("Log Transform Data");
        logTransform.setActionCommand("logtransform");

         if(rowNorm)
             rowNormalized.setSelected(true);
         else if(logtransform)
            logTransform.setSelected(true);
         else
             asIs.setSelected(true);

         dataView = new javax.swing.ButtonGroup();
         dataView.add(asIs);
         dataView.add(rowNormalized);
         dataView.add(logTransform);

        asIs.addActionListener(new selectDataViewActionListener(edgeOverlapPanel, nodeOverlapPanel,this));

        rowNormalized.addActionListener(new selectDataViewActionListener(edgeOverlapPanel, nodeOverlapPanel,this));

        logTransform.addActionListener(new selectDataViewActionListener(edgeOverlapPanel, nodeOverlapPanel,this));

        heatmapOptions.add(asIs);
        heatmapOptions.add(rowNormalized);
        heatmapOptions.add(logTransform);

        return heatmapOptions;

    }


    public ColorGradientRange getRange() {
        return range;
    }

    public void setRange(ColorGradientRange range) {
        this.range = range;
    }

    public ColorGradientTheme getTheme() {
        return theme;
    }

    public void setTheme(ColorGradientTheme theme) {
        this.theme = theme;
    }

    public boolean isRowNorm() {
        return rowNorm;
    }

    public void setRowNorm(boolean rowNorm) {
        this.rowNorm = rowNorm;
    }

    public boolean isLogtransform() {
        return logtransform;
    }

    public void setLogtransform(boolean logtransform) {
        this.logtransform = logtransform;
    }
}
