import cytoscape.CyNetwork;
import cytoscape.visual.mappings.ContinuousMapping;
import cytoscape.visual.mappings.ObjectMapping;
import cytoscape.visual.mappings.LinearNumberToColorInterpolator;
import cytoscape.visual.mappings.BoundaryRangeValues;
import cytoscape.visual.mappings.continuous.ContinuousRangeCalculator;
import cytoscape.visual.mappings.continuous.ContinuousLegend;
import cytoscape.visual.VisualPropertyType;
import cytoscape.view.CyNetworkView;

import javax.swing.*;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.Iterator;
import java.util.StringTokenizer;

/**
 * Created by
 * User: risserlin
 * Date: Jan 30, 2009
 * Time: 9:15:32 AM
 */
public class OverlappingGenesPanel extends JPanel {


        private Object[] columnNames;
        private Object[] columnNames2;
        private Cursor hand;

        private ColorGradientRange range;
        private ColorGradientTheme theme;
        private int numConditions;

    /**
     * Creates a new instance of OverlappingGenesPanel
     */



    public OverlappingGenesPanel(GeneExpressionMatrix expression){

        initColorGradients(expression);

        numConditions = expression.getNumConditions();
        columnNames = expression.getColumnNames();

       this.setLayout(new java.awt.BorderLayout());

    }

    private void initColorGradients(GeneExpressionMatrix expression){

        double minExpression = expression.getMinExpression();
        double maxExpression = expression.getMaxExpression();

        double max = Math.max(Math.abs(minExpression), maxExpression);

        double median = max/2;

        //if the minimum expression is above zero make it a one colour heatmap
        if(minExpression >= 0){
            range = ColorGradientRange.getInstance(0,median, median,max, 0,median,median,max);
            theme = ColorGradientTheme.RED_ONECOLOR_GRADIENT_THEME;
        }
        else{
            range = ColorGradientRange.getInstance(-max,median, median,max, -max,median,median,max);
            theme = ColorGradientTheme.YELLOW_BLUE_GRADIENT_THEME;
        }

    }

    public void updatePanel(HashMap currentGeneExpressionSet){

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());

        //add the legend in the south section
        //mainPanel.add(legend, java.awt.BorderLayout.SOUTH);

        mainPanel.add(createLegendPanel(), java.awt.BorderLayout.WEST);

        //create data subset
        Object[][] data = createTableData(currentGeneExpressionSet);
        JTable jTable1 = new JTable(new OverlappingGenesTableModel(columnNames,data));

        //Set up renderer and editor for the Color column.
        jTable1.setDefaultRenderer(Color.class,new ColorRenderer());

        TableColumnModel tcModel = jTable1.getColumnModel();


        jTable1.setDragEnabled(false);
        jTable1.setCellSelectionEnabled(true);
        for (int i=0;i<columnNames.length;i++){
             if (i==0 || columnNames[i].equals("Name"))
                   tcModel.getColumn(i).setPreferredWidth(50);
             else if (i==1 || columnNames[i].equals("Description"))
                    tcModel.getColumn(i).setPreferredWidth(50);
             else
                   tcModel.getColumn(i).setPreferredWidth(10);
         }

       jTable1.setColumnModel(tcModel);

       JScrollPane jScrollPane = new javax.swing.JScrollPane(jTable1);
       jScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

       mainPanel.add(jScrollPane);

       this.add(mainPanel, java.awt.BorderLayout.CENTER);
       this.revalidate();


    }

    private Object[][] createTableData(HashMap currentExpressionSet){

        Object[][] data = new Object[currentExpressionSet.size()][numConditions];
        //Got through the hashmap and put all the values is

        int k = 0;
        for(Iterator i = currentExpressionSet.keySet().iterator();i.hasNext();){
            //Current expression row
            GeneExpression row = (GeneExpression)currentExpressionSet.get(i.next());
            Double[] expression_values = row.getExpression();
            String gene = row.getName();
            data[k][0] = row.getName();
            data[k][1] = row.getDescription();

            for(int j = 0; j < row.getExpression().length;j++){

                data[k][j+2] = ColorGradientMapper.getColorGradient(theme,range,row.getName(),expression_values[j]);

            }
            k++;
        }
        return data;
    }


    public JPanel createLegendPanel(){

        JPanel westPanel = new JPanel();

        ColorGradientWidget new_legend = ColorGradientWidget.getInstance("expression legend",150,60,5,5,theme,range,true,ColorGradientWidget.LEGEND_POSITION.TOP);

        westPanel.add(new_legend);
        return westPanel;
    }

    public void clearPanel(){
        JPanel mainPanel = new JPanel();

        this.add(mainPanel);
        this.revalidate();
    }

}


