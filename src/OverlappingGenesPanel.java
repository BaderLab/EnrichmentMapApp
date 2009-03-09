
import cytoscape.util.FileUtil;

import javax.swing.*;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Enumeration;
import java.util.HashSet;
import java.io.*;

import giny.model.Node;
import giny.model.Edge;


/**
 * Created by
 * User: risserlin
 * Date: Jan 30, 2009
 * Time: 9:15:32 AM
 */
public class OverlappingGenesPanel extends JPanel {


    private Object[] columnNames;
    private Object[] columnNames2;
    private String[] phenotypes;
    private String[] phenotypes2;

    private int numConditions;
    private int numConditions2;

    private HashMap currentExpressionSet;
    private HashMap currentExpressionSet2;

    private boolean node=true;

    private String Dataset1phenotype1;
    private String Dataset1phenotype2;
    private String Dataset2phenotype1;
    private String Dataset2phenotype2;

    private HeatMapParameters hmParams;

    private EnrichmentMapParameters params;

    /**
     * Creates a new instance of OverlappingGenesPanel
     */

    public OverlappingGenesPanel(boolean node){
       this.node = node;
       this.setLayout(new java.awt.BorderLayout());


    }

    public void resetVariables(EnrichmentMapParameters params){
        this.params = params;

        GeneExpressionMatrix expression = params.getExpression();

        numConditions = expression.getNumConditions();
        columnNames = expression.getColumnNames();
        phenotypes = expression.getPhenotypes();

        this.Dataset1phenotype1 = params.getDataset1Phenotype1();
        this.Dataset1phenotype2 = params.getDataset1Phenotype2();

        hmParams = params.getHmParams();

        //get the current expressionSet
        if(node)
           currentExpressionSet = getNodeExpressionSet(params, expression);
       else
           currentExpressionSet = getEdgeExpressionSet(params, expression);

        if(params.isData2()){

            GeneExpressionMatrix expression2 = params.getExpression2();

            numConditions2 = expression2.getNumConditions();
            columnNames2 = expression2.getColumnNames();
            phenotypes2 = expression2.getPhenotypes();

            this.Dataset2phenotype1 = params.getDataset2Phenotype1();
            this.Dataset2phenotype2 = params.getDataset2Phenotype2();

            if(node)
               currentExpressionSet2 = getNodeExpressionSet(params, expression2);
            else
               currentExpressionSet2 = getEdgeExpressionSet(params, expression2);
        }


    }

    public void updatePanel(EnrichmentMapParameters params){

        resetVariables(params);
        updatePanel();
    }

    public void updatePanel(){


        if(currentExpressionSet != null){
            Object[][] data;
            JTable jTable1;
            String[] mergedcolumnNames = null;

            JPanel mainPanel = new JPanel();
            mainPanel.setLayout(new BorderLayout());

            mainPanel.add(createLegendPanel(), java.awt.BorderLayout.WEST);

            //create data subset
            if(params.isData2()){
               data = createMergedTableData();
               mergedcolumnNames = new String[columnNames.length + columnNames2.length - 2];
               System.arraycopy(columnNames,0,mergedcolumnNames,0,columnNames.length);
               System.arraycopy(columnNames2,2, mergedcolumnNames,columnNames.length,columnNames2.length-2);
               jTable1 = new JTable(new OverlappingGenesTableModel(mergedcolumnNames,data));
            }
            else{
               data = createTableData();
               jTable1 = new JTable(new OverlappingGenesTableModel(columnNames,data));
            }
            //Set up renderer and editor for the Color column.
            jTable1.setDefaultRenderer(Color.class,new ColorRenderer());

            TableColumnModel tcModel = jTable1.getColumnModel();
            jTable1.setDragEnabled(false);
            jTable1.setCellSelectionEnabled(true);


            //set the table header renderer to the vertical renderer
            ColumnHeaderVerticalRenderer pheno1_renderer = new ColumnHeaderVerticalRenderer();
            pheno1_renderer.setBackground(EnrichmentMapVisualStyle.lightest_phenotype1);
            ColumnHeaderVerticalRenderer pheno2_renderer = new ColumnHeaderVerticalRenderer();
            pheno2_renderer.setBackground(EnrichmentMapVisualStyle.lightest_phenotype2);

            ColumnHeaderVerticalRenderer default_renderer = new ColumnHeaderVerticalRenderer();

            if(params.isData2()){
                //go through the first data set
                for (int i=0;i<columnNames.length;i++){
                    if (i==0 || columnNames[i].equals("Name"))
                        tcModel.getColumn(i).setPreferredWidth(50);
                    else if (i==1 || columnNames[i].equals("Description"))
                        tcModel.getColumn(i).setPreferredWidth(50);
                    else{
                        tcModel.getColumn(i).setPreferredWidth(10);
                        if(phenotypes != null){
                            if(phenotypes[i-2].equalsIgnoreCase(Dataset1phenotype1))
                                tcModel.getColumn(i).setHeaderRenderer(pheno1_renderer);
                            else if(phenotypes[i-2].equalsIgnoreCase(Dataset1phenotype2))
                                tcModel.getColumn(i).setHeaderRenderer(pheno2_renderer);
                        }
                        else
                            tcModel.getColumn(i).setHeaderRenderer(default_renderer);
                    }
                }
                //go through the second data set
                for(int i = columnNames.length; i< (columnNames.length +columnNames2.length-2); i++){
                        tcModel.getColumn(i).setPreferredWidth(10);
                        if(phenotypes2 != null){
                            if(phenotypes2[i-columnNames.length].equalsIgnoreCase(Dataset2phenotype1))
                                tcModel.getColumn(i).setHeaderRenderer(pheno1_renderer);
                            else if(phenotypes2[i-columnNames.length].equalsIgnoreCase(Dataset2phenotype2))
                                tcModel.getColumn(i).setHeaderRenderer(pheno2_renderer);
                        }
                        else
                            tcModel.getColumn(i).setHeaderRenderer(default_renderer);

                }
            }
            else{
                for (int i=0;i<columnNames.length;i++){
                    if (i==0 || columnNames[i].equals("Name"))
                        tcModel.getColumn(i).setPreferredWidth(50);
                    else if (i==1 || columnNames[i].equals("Description"))
                        tcModel.getColumn(i).setPreferredWidth(50);
                    else{
                        tcModel.getColumn(i).setPreferredWidth(10);
                        if(phenotypes != null){
                            if(phenotypes[i-2].equalsIgnoreCase(Dataset1phenotype1))
                                tcModel.getColumn(i).setHeaderRenderer(pheno1_renderer);
                            else if(phenotypes[i-2].equalsIgnoreCase(Dataset1phenotype2))
                                tcModel.getColumn(i).setHeaderRenderer(pheno2_renderer);
                        }
                        else
                             tcModel.getColumn(i).setHeaderRenderer(default_renderer);
                    }

                }
            }

            jTable1.setColumnModel(tcModel);
            if(columnNames.length>20)
                jTable1.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

            JScrollPane jScrollPane = new javax.swing.JScrollPane(jTable1);

            mainPanel.add(jScrollPane);
            mainPanel.revalidate();

            this.add(mainPanel, java.awt.BorderLayout.CENTER);
        }
        this.revalidate();


    }

    private Object[][] createTableData(){

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



    private Object[][] createMergedTableData(){

        int totalConditions = (numConditions + numConditions2-2);

        Object[][] data = new Object[Math.max(currentExpressionSet.size(), currentExpressionSet2.size())][totalConditions];

        //Got through the hashmap and put all the values is

        int k = 0;
        for(Iterator i = currentExpressionSet.keySet().iterator();i.hasNext();){

            Object currentKey = i.next();

            //Current expression row
            GeneExpression halfRow1 = (GeneExpression)currentExpressionSet.get(currentKey);

            //get the corresponding row from the second dataset
            GeneExpression halfRow2 = (GeneExpression)currentExpressionSet2.get(currentKey);

            Double[] expression_values1 = null;
            Double[] expression_values2 = null;
            if(hmParams.isRowNorm()){
                if(halfRow1 != null)
                    expression_values1 = halfRow1.rowNormalize();
                if(halfRow2 != null)
                    expression_values2 = halfRow2.rowNormalize();
            }
            else if(hmParams.isLogtransform()){
                if(halfRow1 != null)
                    expression_values1 = halfRow1.rowLogTransform();
                if(halfRow2 != null)
                    expression_values2 = halfRow2.rowLogTransform();
            }
            else{
                if(halfRow1 != null)
                    expression_values1   = halfRow1.getExpression();
                if(halfRow2 != null)
                    expression_values2   = halfRow2.getExpression();
            }

            if(halfRow1 != null){
                data[k][0] = halfRow1.getName();
                data[k][1] = halfRow1.getDescription();
            }
            else if(halfRow2 != null){
                data[k][0] = halfRow2.getName();
                data[k][1] = halfRow2.getDescription();
            }

            //if either of the expression_values is null set the array to have no data
            if(expression_values1 == null){
                expression_values1 = new Double[columnNames.length-2];
                for(int m = 0; m < expression_values1.length;m++)
                    expression_values1[m] = null;
            }
            if(expression_values2 == null){
                expression_values2 = new Double[columnNames2.length-2];
                for(int m = 0; m < expression_values2.length;m++)
                    expression_values2[m] = null;
            }


            for(int j = 0; j < halfRow1.getExpression().length;j++){

                data[k][j+2] = ColorGradientMapper.getColorGradient(hmParams.getTheme(),hmParams.getRange(),halfRow1.getName(),expression_values1[j]);

            }
            for(int j = halfRow1.getExpression().length; j < (halfRow1.getExpression().length + halfRow2.getExpression().length);j++){

                data[k][j+2] = ColorGradientMapper.getColorGradient(hmParams.getTheme(),hmParams.getRange(),halfRow2.getName(),expression_values2[j-halfRow1.getExpression().length]);

            }

            k++;
        }
        return data;
    }

    private JPanel createLegendPanel(){

        JPanel westPanel = new JPanel();
        westPanel.setLayout(new BorderLayout());
        JPanel topPanel = new JPanel();
        JPanel buttonPanel = new JPanel();
        topPanel.setLayout(new GridLayout(2,1));


        ColorGradientWidget new_legend = ColorGradientWidget.getInstance("expression legend",150,60,5,5,hmParams.getTheme(),hmParams.getRange(),true,ColorGradientWidget.LEGEND_POSITION.TOP);

        JButton SaveExpressionSet = new JButton("Save Expression Set");

        SaveExpressionSet.addActionListener(new java.awt.event.ActionListener() {
               public void actionPerformed(java.awt.event.ActionEvent evt) {
                      saveExpressionSetActionPerformed(evt);
               }
         });
        buttonPanel.add(SaveExpressionSet);

        topPanel.add(new_legend);
        topPanel.add(buttonPanel);

        westPanel.add(topPanel, BorderLayout.NORTH);

        westPanel.add(hmParams.createHeatMapOptionsPanel(),BorderLayout.SOUTH);

        westPanel.revalidate();
        return westPanel;
    }

   private void saveExpressionSetActionPerformed(ActionEvent evt){
        java.io.File file = FileUtil.getFile("Export Heatmap as txt File", FileUtil.SAVE);
        if (file != null && file.toString() != null) {
            String fileName = file.toString();
            if (!fileName.endsWith(".txt")) {
                fileName += ".txt";
                file = new File(fileName);
            }

            int response = JOptionPane.OK_OPTION;
            if(file.exists())
                    response = JOptionPane.showConfirmDialog(this, "The file already exists.  Would you like to overwrite it?");
            if(response == JOptionPane.NO_OPTION || response == JOptionPane.CANCEL_OPTION ){

            }
            else if(response == JOptionPane.YES_OPTION || response == JOptionPane.OK_OPTION){
                    try{
                        BufferedWriter output = new BufferedWriter(new FileWriter(file));
                        for(int j = 0; j < columnNames.length;j++)
                            if(j == (columnNames.length-1))
                                output.write(columnNames[j] + "\n");
                            else
                                output.write(columnNames[j] + "\t");

                        for(Iterator i = currentExpressionSet.keySet().iterator(); i.hasNext();){
                            GeneExpression row = (GeneExpression)currentExpressionSet.get(i.next());
                            output.write(row.toString());
                        }
                        output.flush();
                        output.close();
                        JOptionPane.showMessageDialog(this, "File " + fileName + " saved.");
                    }catch(IOException e){
                        JOptionPane.showMessageDialog(this, "unable to write to file " + fileName);
                }
            }
        }
    }

    private HashMap getNodeExpressionSet(EnrichmentMapParameters params, GeneExpressionMatrix expressionSet){

        Object[] nodes = params.getSelectedNodes().toArray();

        //go through the nodes only if there are some
        if(nodes.length > 0){
            HashSet union = null;

            for(int i = 0; i< nodes.length;i++){

                Node current_node = (Node)nodes[i];
                String nodename = current_node.getIdentifier();

                GeneSet current_geneset = (GeneSet)params.getGenesetsOfInterest().get(nodename);

                HashSet current_set = current_geneset.getGenes();

                if( union == null){
                    union = new HashSet(current_set);
                 }else{
                    union.addAll(current_set);
                }
            }
            return expressionSet.getExpressionMatrix(union);
        }

        return null;


    }

    private HashMap getEdgeExpressionSet(EnrichmentMapParameters params, GeneExpressionMatrix expressionSet){

        Object[] edges = params.getSelectedEdges().toArray();

        if(edges.length>0){
            HashSet intersect = null;
            HashSet union = null;

            for(int i = 0; i< edges.length;i++){

                Edge current_edge = (Edge) edges[i];
                String edgename = current_edge.getIdentifier();


                GenesetSimilarity similarity = params.getGenesetSimilarity().get(edgename);
                HashSet current_set = similarity.getOverlapping_genes();

                if(intersect == null && union == null){
                    intersect = new HashSet(current_set);
                    union = new HashSet(current_set);
                }else{
                    intersect.retainAll(current_set);
                    union.addAll(current_set);
                }
            }
            return expressionSet.getExpressionMatrix(intersect);
        }
        return null;
    }



    public void clearPanel(){

        this.removeAll();
        this.revalidate();
    }

}


