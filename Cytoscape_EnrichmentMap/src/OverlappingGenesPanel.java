
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
import java.io.*;


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
        private Cursor hand;

        private int numConditions;

        private HashMap currentGeneExpressionSet;
        private String Dataset1phenotype1;
        private String Dataset1phenotype2;
        private String Dataset2phenotype1;
        private String Dataset2phenotype2;

        private EnrichmentMapParameters params;
        private HeatMapParameters hmParams;

    /**
     * Creates a new instance of OverlappingGenesPanel
     */

    public OverlappingGenesPanel(EnrichmentMapParameters params){
        this.params = params;

        GeneExpressionMatrix expression = params.getExpression();

        numConditions = expression.getNumConditions();
        columnNames = expression.getColumnNames();
        phenotypes = expression.getPhenotypes();

        this.Dataset1phenotype1 = params.getDataset1Phenotype1();
        this.Dataset1phenotype2 = params.getDataset1Phenotype2();
        this.Dataset2phenotype1 = params.getDataset2Phenotype1();
        this.Dataset2phenotype2 = params.getDataset2Phenotype2();
       this.setLayout(new java.awt.BorderLayout());


    }

    public void updatePanel(){

        if(currentGeneExpressionSet != null){
            JPanel mainPanel = new JPanel();
            mainPanel.setLayout(new BorderLayout());

            mainPanel.add(createLegendPanel(), java.awt.BorderLayout.WEST);

            //create data subset
            Object[][] data = createTableData();
            JTable jTable1 = new JTable(new OverlappingGenesTableModel(columnNames,data));

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

        Object[][] data = new Object[currentGeneExpressionSet.size()][numConditions];
        //Got through the hashmap and put all the values is

        int k = 0;
        for(Iterator i = currentGeneExpressionSet.keySet().iterator();i.hasNext();){
            //Current expression row
            GeneExpression row = (GeneExpression)currentGeneExpressionSet.get(i.next());
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

                        for(Iterator i = currentGeneExpressionSet.keySet().iterator(); i.hasNext();){
                            GeneExpression row = (GeneExpression)currentGeneExpressionSet.get(i.next());
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

    public HashMap getCurrentGeneExpressionSet() {
        return currentGeneExpressionSet;
    }

    public void setCurrentGeneExpressionSet(HashMap currentGeneExpressionSet) {
        this.currentGeneExpressionSet = currentGeneExpressionSet;
    }

    public HeatMapParameters getHmParams() {
        return hmParams;
    }

    public void setHmParams(HeatMapParameters hmParams) {
        this.hmParams = hmParams;
    }

    public void clearPanel(){

        this.removeAll();
        this.revalidate();
    }

}


