
import javax.swing.*;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.Iterator;


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

        private int numConditions;

        private HashMap currentGeneExpressionSet;

        private HeatMapParameters hmParams;

    /**
     * Creates a new instance of OverlappingGenesPanel
     */

    public OverlappingGenesPanel(GeneExpressionMatrix expression, HeatMapParameters hmParams){

        this.hmParams = hmParams;

        hmParams.initColorGradients(expression);

        numConditions = expression.getNumConditions();
        columnNames = expression.getColumnNames();

       this.setLayout(new java.awt.BorderLayout());


    }

    public void updatePanel(HashMap currentGeneExpressionSet){

        this.currentGeneExpressionSet = currentGeneExpressionSet;

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());


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
       if(columnNames.length>10)
            jTable1.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

       JScrollPane jScrollPane = new javax.swing.JScrollPane(jTable1);
       //jScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

       mainPanel.add(jScrollPane);
       mainPanel.revalidate();



       this.add(mainPanel, java.awt.BorderLayout.CENTER);

       this.revalidate();


    }

      public void updatePanel(){

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
       jTable1.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

       JScrollPane jScrollPane = new javax.swing.JScrollPane(jTable1);
       jScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

       mainPanel.add(jScrollPane);
       mainPanel.revalidate();

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
            Double[] expression_values;
            if(hmParams.isRowNorm())
                expression_values = row.rowNormalize();
            else if(hmParams.isLogtransform())
                expression_values = row.rowLogTransform();
            else
                 expression_values   = row.getExpression();

            data[k][0] = row.getName();
            data[k][1] = row.getDescription();

            for(int j = 0; j < row.getExpression().length;j++){

                data[k][j+2] = ColorGradientMapper.getColorGradient(hmParams.getTheme(),hmParams.getRange(),row.getName(),expression_values[j]);

            }
            k++;
        }
        return data;
    }


    private JPanel createLegendPanel(){

        JPanel westPanel = new JPanel();
        westPanel.setLayout(new BorderLayout());

        ColorGradientWidget new_legend = ColorGradientWidget.getInstance("expression legend",150,60,5,5,hmParams.getTheme(),hmParams.getRange(),true,ColorGradientWidget.LEGEND_POSITION.TOP);

        westPanel.add(new_legend, BorderLayout.NORTH);

        westPanel.add(hmParams.createHeatMapOptionsPanel(),BorderLayout.SOUTH);

        westPanel.revalidate();
        return westPanel;
    }


    public void clearPanel(){

        this.removeAll();
        this.revalidate();
    }

}


