/**
 **                       EnrichmentMap Cytoscape Plugin
 **
 ** Copyright (c) 2008-2009 Bader Lab, Donnelly Centre for Cellular and Biomolecular 
 ** Research, University of Toronto
 **
 ** Contact: http://www.baderlab.org
 **
 ** Code written by: Ruth Isserlin
 ** Authors: Daniele Merico, Ruth Isserlin, Oliver Stueker, Gary D. Bader
 **
 ** This library is free software; you can redistribute it and/or modify it
 ** under the terms of the GNU Lesser General Public License as published
 ** by the Free Software Foundation; either version 2.1 of the License, or
 ** (at your option) any later version.
 **
 ** This library is distributed in the hope that it will be useful, but
 ** WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 ** MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 ** documentation provided hereunder is on an "as is" basis, and
 ** University of Toronto
 ** has no obligations to provide maintenance, support, updates, 
 ** enhancements or modifications.  In no event shall the
 ** University of Toronto
 ** be liable to any party for direct, indirect, special,
 ** incidental or consequential damages, including lost profits, arising
 ** out of the use of this software and its documentation, even if
 ** University of Toronto
 ** has been advised of the possibility of such damage.  
 ** See the GNU Lesser General Public License for more details.
 **
 ** You should have received a copy of the GNU Lesser General Public License
 ** along with this library; if not, write to the Free Software Foundation,
 ** Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 **
 **/

// $Id: HeatMapPanel.java 383 2009-10-08 20:06:35Z risserlin $
// $LastChangedDate: 2009-10-08 16:06:35 -0400 (Thu, 08 Oct 2009) $
// $LastChangedRevision: 383 $
// $LastChangedBy: risserlin $
// $HeadURL: svn+ssh://risserlin@server1.baderlab.med.utoronto.ca/svn/EnrichmentMap/trunk/EnrichmentMapPlugin/src/org/baderlab/csplugins/enrichmentmap/HeatMapPanel.java $

package org.baderlab.csplugins.enrichmentmap;


import cytoscape.util.FileUtil;
import cytoscape.CytoscapeInit;
import cytoscape.Cytoscape;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.TableColumnModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.*;
import java.util.List;
import java.io.*;

import giny.model.Node;
import giny.model.Edge;
import org.mskcc.colorgradient.*;
import org.baderlab.csplugins.brainlib.DistanceMatrix;
import org.baderlab.csplugins.brainlib.AvgLinkHierarchicalClustering;

/**
 * Created by
 * User: risserlin
 * Date: Jan 30, 2009
 * Time: 9:15:32 AM
 * <p>
 * Creates a Heat map Panel - (heat map can consists of either one or two expression files depending on what
 * was supplied by the user)
 */
public class HeatMapPanel extends JPanel {

    //Column names for expression set for data set 1
    private Object[] columnNames;
      //Column names for expression set for data set 1
    private Object[] columnNames2;

    //Phenotypes for expression set for data set 1 - index number indicates which column the phenotype is specific to
    private String[] phenotypes;
    //Phenotypes for expression set for data set 1 - index number indicates which column the phenotype is specific to
    private String[] phenotypes2;

    //expression data
    //private Object[][] data;
    private Object[][] expValue;

    private int numConditions = 0;
    private int numConditions2 = 0;

    private String[] hRow1;
    private String[] hRow2;
    private String[] rowGeneName;
    private int[] rowLength;
    private ColorGradientTheme [] rowTheme;
    private ColorGradientRange [] rowRange;

    private int[] halfRow1Length;
    private int[] halfRow2Length;
    private ColorGradientTheme [] themeHalfRow1;
    private ColorGradientTheme [] themeHalfRow2;
    private ColorGradientRange [] rangeHalfRow1;
    private ColorGradientRange [] rangeHalfRow2;
    private boolean[] isHalfRow1;
    private boolean[] isHalfRow2;
    private  final Insets insets = new Insets(0,0,0,0);

    //current subset of expression data from dataset 1 expression set
    private HashMap currentExpressionSet;
     //current subset of expression data from dataset 2 expression set
    private HashMap currentExpressionSet2;

    private boolean node=true;

    //phenotypes specified by the user (if correspond to the class file definition the colour of the column can
    //be changed to indicate its phenotype.
    private String Dataset1phenotype1;
    private String Dataset1phenotype2;
    private String Dataset2phenotype1;
    private String Dataset2phenotype2;

    //heat map parameters for heat map
    private HeatMapParameters hmParams;
    //enrichment map parameters for heat map
    private EnrichmentMapParameters params;

    //boolean indicating which direction the column was last sorted by
    private boolean[] column1_ascending;
    private boolean[] column2_ascending;

    //create a pop up menu for linkouts
    private JPopupMenu rightClickPopupMenu;

    //for the linkout properties that are loaded from cytoscape properties.
    private Map<String, Map<String, String>> linkoutProps;

    /**
     * Class constructor - creates new instance of a Heat map panel
     *
     * @param node - boolean indicating with this is a heat map for node unions or edge overlaps.
     * if true it is a node heatmap, else it is an edge heatmap
     */
    public HeatMapPanel(boolean node){
       this.node = node;
       this.setLayout(new java.awt.BorderLayout());

       //initialize the linkout props
        initialize_linkouts();

        //initialize pop up menu
        rightClickPopupMenu = new JPopupMenu();
    }

    /**
     * Set the Heat map Panel to the variables in the given enrichment map parameters set
     *
     * @param params - enrichment map parameters to reset the heat map to.
     */
    public void resetVariables(EnrichmentMapParameters params){
        this.params = params;

        GeneExpressionMatrix expression = params.getExpression();
        numConditions = expression.getNumConditions();
        columnNames = expression.getColumnNames();
        column1_ascending = new boolean[columnNames.length];
        phenotypes = expression.getPhenotypes();

        this.Dataset1phenotype1 = params.getDataset1Phenotype1();
        this.Dataset1phenotype2 = params.getDataset1Phenotype2();

        hmParams = params.getHmParams();

        //get the current expressionSet
        if(node)
            setNodeExpressionSet(params);
       else
            setEdgeExpressionSet(params);

        if(params.isData2()){

            GeneExpressionMatrix expression2 = params.getExpression2();

            numConditions2 = expression2.getNumConditions();
            columnNames2 = expression2.getColumnNames();
            column2_ascending = new boolean[columnNames2.length-2]; //we don't have to repeat the name and description columns
            phenotypes2 = expression2.getPhenotypes();

            this.Dataset2phenotype1 = params.getDataset2Phenotype1();
            this.Dataset2phenotype2 = params.getDataset2Phenotype2();

        }


    }

    /**
     * Update the panel base on given enrichment parameters
     *
     * @param params - enrichment map parameters to update the heat map to.
     */
    public void updatePanel(EnrichmentMapParameters params){

        resetVariables(params);
        updatePanel();
    }

    /**
     * Update the heat map panel
     */
    public void updatePanel(){
        if(currentExpressionSet != null){

            JTable jTable1;
            JTable rowTable;
            String[] mergedcolumnNames = null;

            JPanel mainPanel = new JPanel();
            mainPanel.setLayout(new BorderLayout());
            TableSort sort;

            HeatMapTableModel OGT;
            Object[][] data;

            //create data subset
            if(params.isData2()){

                // used exp[][] value to store all the expression values needed to create data[][]
                expValue	= createSortedMergedTableData();
                data		= createSortedMergedTableData(getExpValue());

               mergedcolumnNames = new String[columnNames.length + columnNames2.length - 2];

               System.arraycopy(columnNames,0,mergedcolumnNames,0,columnNames.length);
               System.arraycopy(columnNames2,2, mergedcolumnNames,columnNames.length,columnNames2.length-2);

               //used OGT to minimize call of new JTable
               OGT		=	new HeatMapTableModel(mergedcolumnNames,data,expValue);

            }
            else{
                // used exp[][] value to store all the expression values needed to create data[][]
                expValue	=	createSortedTableData();
                data 		=	createSortedTableData(getExpValue());

                OGT			=	new HeatMapTableModel(columnNames,data,expValue);

            }

            sort = new TableSort(OGT);
            jTable1 = new JTable(sort);

            //add a listener to the table
            jTable1.addMouseListener(new HeatMapTableActionListener(jTable1,OGT, rightClickPopupMenu,linkoutProps));

            // used for listening to columns when clicked
            TableHeader header = new TableHeader(sort, jTable1, hmParams);

            JTableHeader tableHdr= jTable1.getTableHeader();
            tableHdr.addMouseListener(header);

            //check to see if there is already a sort been defined for this table
            if(hmParams.isSortbycolumn()){
                boolean ascending;

                if(hmParams.getSortIndex()>=columnNames.length){
                    ascending = column2_ascending[hmParams.getSortIndex()-columnNames.length];
                    hmParams.setSortbycolumnName((String)columnNames2[hmParams.getSortIndex()-columnNames.length+2]);
                }
                else{
                    ascending = column1_ascending[hmParams.getSortIndex()];
                    hmParams.setSortbycolumnName((String)columnNames[hmParams.getSortIndex()]);
                }
                //only swap the direction of the sort if a column sort action was triggered
                if(hmParams.isSortbycolumn_event_triggered()){
                    //reset sort column trigger
                    hmParams.setSortbycolumn_event_triggered(false);

                    //change the ascending boolean flag for the column we are about to sort by
                    //if the index is larger than column name 1 then it is from the second dataset
                    if(hmParams.getSortIndex()>=columnNames.length){
                        column2_ascending[hmParams.getSortIndex()-columnNames.length] = !column2_ascending[hmParams.getSortIndex()-columnNames.length];
                        ascending = column2_ascending[hmParams.getSortIndex()-columnNames.length];
                    }
                    else{
                        column1_ascending[hmParams.getSortIndex()] = !column1_ascending[hmParams.getSortIndex()];
                        ascending = column1_ascending[hmParams.getSortIndex()];
                    }
                }
                header.sortByColumn(hmParams.getSortIndex(), ascending);
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
            default_renderer.setBackground(Color.white);

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
                            else
                                tcModel.getColumn(i).setHeaderRenderer(default_renderer);
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
                            else
                                tcModel.getColumn(i).setHeaderRenderer(default_renderer);
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
                            else
                                tcModel.getColumn(i).setHeaderRenderer(default_renderer);
                        }
                        else
                             tcModel.getColumn(i).setHeaderRenderer(default_renderer);
                    }

                }
            }


            jTable1.setColumnModel(tcModel);
            JScrollPane jScrollPane = new javax.swing.JScrollPane(jTable1);
            rowTable =new RowNumberTable(jTable1);
            jScrollPane.setRowHeaderView(rowTable );

            if(columnNames.length>20)
                jTable1.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

            mainPanel.add(jScrollPane);
            mainPanel.revalidate();

            this.add(createNorthPanel(), java.awt.BorderLayout.NORTH);
            this.add(jScrollPane, java.awt.BorderLayout.CENTER);
        }
        this.revalidate();


    }

    private Object[][] createSortedTableData(Object[][] expValue2) {
        this.expValue=expValue2;
        Object[][] data;

        int[] HRow=this.getRowLength();
        ColorGradientTheme[] RowCRT=this.getRowTheme();
        ColorGradientRange[] RowCRR=this.getRowRange();
        String[] RowGene=this.getRowGeneName();

        int kValue=currentExpressionSet.size();
        data = new Object[currentExpressionSet.size()][numConditions];
        for(int k=0;k<kValue;k++){


            data[k][0] =  expValue[k][0];
            data[k][1] = expValue[k][1];

            for(int j=0;j<HRow[k];j++){
                data[k][j+2] = ColorGradientMapper.getColorGradient(RowCRT[k],RowCRR[k],RowGene[k],(Double)expValue[k][j+2]);
             }

        }
        // TODO Auto-generated method stub
        return data;
    }

    private Object[][] createSortedMergedTableData(Object[][] expValue) {
        this.expValue=expValue;
        Object[][] data;

        int[] HRow1						=this.getHalfRow1Length();
        int[] HRow2						=this.getHalfRow2Length();
        ColorGradientTheme[] Row1CRT	=this.getThemeHalfRow1();
        ColorGradientTheme[] Row2CRT	=this.getThemeHalfRow2();
        ColorGradientRange[] Row1CRR	=this.getRangeHalfRow1();
        ColorGradientRange[] Row2CRR	=this.getRangeHalfRow2();
        String[] Row1gene				=this.gethRow1();
        String[] Row2gene				=this.gethRow2();


        int kValue=Math.max(currentExpressionSet.size(), currentExpressionSet2.size());
        int totalConditions = (numConditions + numConditions2-2);
        data = new Object[Math.max(currentExpressionSet.size(), currentExpressionSet2.size())][totalConditions];
        for(int k=0;k<kValue;k++){


            data[k][0] = expValue[k][0];
            data[k][1] = expValue[k][1];

            for(int j=0;j<HRow1[k];j++){
                data[k][j+2] = ColorGradientMapper.getColorGradient(Row1CRT[k],Row1CRR[k],Row1gene[k],(Double)expValue[k][j+2]);
             }

            for(int j=HRow1[k];j<HRow2[k];j++){
                data[k][j+2] = ColorGradientMapper.getColorGradient(Row2CRT[k],Row2CRR[k],Row2gene[k],(Double)expValue[k][j+2]);
             }

        }

        return data;
    }

    private Object[][] createSortedTableData(){

         expValue 		= new Object[currentExpressionSet.size()][numConditions];
         rowLength		= new int[currentExpressionSet.size()];
         rowTheme		= new ColorGradientTheme[currentExpressionSet.size()];
         rowGeneName	= new String[currentExpressionSet.size()];
         rowRange		= new ColorGradientRange[currentExpressionSet.size()];
         //Got through the hashmap and put all the values is

        Integer[] ranks_subset = new Integer[currentExpressionSet.size()];

        HashMap<Integer, ArrayList<Integer>> rank2keys = new HashMap<Integer,ArrayList<Integer>>();

        HashMap<Integer, Ranking> ranks = getRanks();

        int n = 0;
        for(Iterator i = currentExpressionSet.keySet().iterator();i.hasNext();){
            Integer key = (Integer)i.next();

            //check to see the key is in the rank file.
            //For new rank files it is possible that some of the genes/proteins won't be ranked
            if(ranks.containsKey(key)){
                ranks_subset[n] = ((Ranking)ranks.get(key)).getRank();
                //check to see if the rank is already in the list.
            }
            else{
                ranks_subset[n] = -1;
            }
            if(!rank2keys.containsKey(ranks_subset[n])){
                ArrayList<Integer> temp = new ArrayList<Integer>();
                temp.add(key);
                rank2keys.put(ranks_subset[n],temp);
            }
            else{
                rank2keys.get(ranks_subset[n]).add(key);
            }
            n++;
        }
        //sort ranks
        Arrays.sort(ranks_subset);

        int k = 0;
        int previous = -1;

        for(int m = 0 ; m < ranks_subset.length;m++){
             //if the current gene doesn't have a rank then don't show it
            if(ranks_subset[m] == -1)
                continue;

            if(ranks_subset[m] == previous)
               continue;

            previous = ranks_subset[m];

            ArrayList keys = rank2keys.get(ranks_subset[m]);

            for(Iterator p = keys.iterator();p.hasNext();){
                Integer key = (Integer)p.next();

                //Current expression row
                GeneExpression row 		= (GeneExpression)currentExpressionSet.get(key);
                Double[] expression_values;
                if(hmParams.isRowNorm())
                   expression_values 	= row.rowNormalize();
                else if(hmParams.isLogtransform())
                   expression_values 	= row.rowLogTransform();
                else
                   expression_values   	= row.getExpression();
                // stores the gene names in column 0
                try{ // stores file name if the file contains integer (inserted to aid sorting)
                    expValue[k][0]=Integer.parseInt(row.getName());
                }
                catch (NumberFormatException v){   // if not integer stores as string (inserted to aid sorting)
                    expValue[k][0]=row.getName();
                }

                expValue[k][1]=row.getDescription(); // stores the description

                rowLength[k]=row.getExpression().length;
                rowTheme[k]=hmParams.getTheme();
                rowRange[k]=hmParams.getRange();
                rowGeneName[k]=row.getName();

                for(int j = 0; j < row.getExpression().length;j++){
                    expValue[k][j+2]=expression_values[j];
                }
                k++;
             }
         }

        this.setRowTheme(rowTheme);
        this.setRowGeneName(rowGeneName);
        this.setRowRange(rowRange);
        this.setExpValue(expValue);
        return expValue;
    }

    private Object[][] createSortedMergedTableData(){

        int totalConditions = (numConditions + numConditions2-2);

        int kValue=Math.max(currentExpressionSet.size(), currentExpressionSet2.size());
        expValue = new Object[Math.max(currentExpressionSet.size(), currentExpressionSet2.size())][totalConditions];
        hRow1= new String[kValue];
        hRow2= new String[kValue];
        halfRow1Length= new int[kValue];
        halfRow2Length= new int[kValue];
        isHalfRow1 = new boolean[kValue];
        isHalfRow2 = new boolean[kValue];
        rangeHalfRow1= new ColorGradientRange[kValue];
        rangeHalfRow2= new ColorGradientRange[kValue];
        themeHalfRow1= new ColorGradientTheme[kValue];
        themeHalfRow2= new ColorGradientTheme[kValue];

        Integer[] ranks_subset = new Integer[currentExpressionSet.size()];

        HashMap<Integer, ArrayList<Integer>> rank2keys = new HashMap<Integer,ArrayList<Integer>>();

        HashMap<Integer, Ranking> ranks = getRanks();

        int n = 0;
        for(Iterator i = currentExpressionSet.keySet().iterator();i.hasNext();){
            Integer key = (Integer)i.next();
            //check to see the key is in the rank file.
            //For new rank files it is possible that some of the genes/proteins won't be ranked
            if(ranks.containsKey(key)){
                ranks_subset[n] = ((Ranking)ranks.get(key)).getRank();
                //check to see if the rank is already in the list.
            }
            else{
                ranks_subset[n] = -1;
            }
            if(!rank2keys.containsKey(ranks_subset[n])){
                ArrayList<Integer> temp = new ArrayList<Integer>();
                temp.add(key);
                rank2keys.put(ranks_subset[n],temp);
            }
            else{
                rank2keys.get(ranks_subset[n]).add(key);
            }
            n++;
         }
        //sort ranks
        Arrays.sort(ranks_subset);

        int k = 0;
        int previous = -1;

        for(int m = 0 ; m < ranks_subset.length;m++){
            //if the current gene doesn't have a rank then don't show it
            if(ranks_subset[m] == -1)
                continue;

            if(ranks_subset[m] == previous)
                continue;

            previous = ranks_subset[m];

            ArrayList keys = rank2keys.get(ranks_subset[m]);

            for(Iterator p = keys.iterator();p.hasNext();){
                Integer currentKey = (Integer)p.next();

                //Current expression row
                GeneExpression halfRow1 = (GeneExpression)currentExpressionSet.get(currentKey);

                //get the corresponding row from the second dataset
                GeneExpression halfRow2 = (GeneExpression)currentExpressionSet2.get(currentKey);

                Double[] expression_values1 = null;
                Double[] expression_values2 = null;

                if(hmParams.isRowNorm()){
                    if(halfRow1 != null)
                        isHalfRow1[k]=true;
                        expression_values1 = halfRow1.rowNormalize();
                    if(halfRow2 != null)
                        isHalfRow2[k]=true;
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
                    try{

                        expValue[k][0] = Integer.parseInt(halfRow1.getName());

                    }
                    catch (NumberFormatException e){

                        expValue[k][0]= halfRow1.getName();
                    }

                    //data[k][1] = halfRow1.getDescription();
                    expValue[k][1]= halfRow1.getDescription();
                }
                else if(halfRow2 != null){
                    try{
                        //data[k][0] = Integer.parseInt(halfRow2.getName());
                        expValue[k][0]= Integer.parseInt(halfRow2.getName());
                    }
                    catch (NumberFormatException e){
                        //data[k][0] = halfRow2.getName();
                        expValue[k][0]= halfRow2.getName();
                    }
                    //data[k][1] = halfRow2.getDescription();
                    expValue[k][1]= halfRow2.getDescription();
                }

               //if either of the expression_values is null set the array to have no data
                if(expression_values1 == null){
                    expression_values1 = new Double[columnNames.length-2];
                    for(int q = 0; m < expression_values1.length;q++)
                        expression_values1[q] = null;
                }
                if(expression_values2 == null){
                    expression_values2 = new Double[columnNames2.length-2];
                    for(int q = 0; m < expression_values2.length;q++)
                       expression_values2[q] = null;
                }

                halfRow1Length[k]=halfRow1.getExpression().length;
                themeHalfRow1[k]=hmParams.getTheme();
                rangeHalfRow1[k]=hmParams.getRange();
                hRow1[k]=halfRow1.getName();
                for(int j = 0; j < halfRow1.getExpression().length;j++){
                    expValue[k][j+2]=expression_values1[j];
                  }
                halfRow2Length[k]=(halfRow1.getExpression().length + halfRow2.getExpression().length);
                themeHalfRow2[k]=hmParams.getTheme();
                rangeHalfRow2[k]=hmParams.getRange();
                hRow2[k]=halfRow1.getName();
                for(int j = halfRow1.getExpression().length; j < (halfRow1.getExpression().length + halfRow2.getExpression().length);j++){
                              expValue[k][j+2]=expression_values2[j-halfRow1.getExpression().length];                }

                k++;
            }

        }
        this.setThemeHalfRow1(themeHalfRow1);
        this.setRangeHalfRow1(rangeHalfRow1);
        this.setHalfRow1Length(halfRow1Length);
        this.sethRow1(hRow1);
        this.setIsHalfRow1(isHalfRow1);


        this.setThemeHalfRow2(themeHalfRow2);
        this.setRangeHalfRow2(rangeHalfRow2);
        this.setHalfRow2Length(halfRow2Length);
        this.sethRow2(hRow2);
        this.setIsHalfRow2(isHalfRow2);

       this.setExpValue(expValue);
        return expValue;
    }


    // created new North panel to accommodate the expression legend, normalization options,sorting options, saving option
   private JPanel emptyPanel(){
       JPanel empty= new JPanel() ;
       empty.setMaximumSize(new Dimension(50,50));
       empty.setMinimumSize(new Dimension(50,50));
       return empty;
   }

    /**
     * create legend panel
     * @return legend panel
     */
    private JPanel expressionLegendPanel(){
        JPanel expLegendPanel = new JPanel();

         TitledBorder expBorder = BorderFactory.createTitledBorder("Expression legend");
         expBorder.setTitleJustification(TitledBorder.LEFT);
         expLegendPanel.setBorder(expBorder);

        ColorGradientWidget new_legend = ColorGradientWidget.getInstance("",200,30,5,5,hmParams.getTheme(),hmParams.getRange(),true,ColorGradientWidget.LEGEND_POSITION.LEFT);

        expLegendPanel.add(new_legend);
        expLegendPanel.revalidate();
        return expLegendPanel;
    }

    /**
     * Creates north panel containing panels for
     * legend, sort by combo box, data tranformation combo box, save expression set button
     *
     * @return panel
     */
    private JPanel createNorthPanel(){

        JPanel northPanel = new JPanel();// new north panel
        JPanel buttonPanel = new JPanel();// brought button panel from westPanel
        //northPanel.setLayout(new GridLayout(1,4));
        northPanel.setLayout(new GridBagLayout());

        JButton SaveExpressionSet = new JButton("Save Expression Set");
        SaveExpressionSet.addActionListener(new java.awt.event.ActionListener() {
               public void actionPerformed(java.awt.event.ActionEvent evt) {
                      saveExpressionSetActionPerformed(evt);
               }
         });

        buttonPanel.add(SaveExpressionSet);
        addComponent(northPanel,expressionLegendPanel(), 0, 0, 1, 1,
                GridBagConstraints.WEST, GridBagConstraints.NONE);

        addComponent(northPanel,emptyPanel(), 1, 0, 1, 1,
                GridBagConstraints.WEST, GridBagConstraints.NONE);


        addComponent(northPanel,hmParams.createDataTransformationOptionsPanel(params), 2, 0, 1, 1,
                GridBagConstraints.CENTER, GridBagConstraints.NONE);

        addComponent(northPanel,hmParams.createSortOptionsPanel(params), 3, 0, 1, 1,
                    GridBagConstraints.CENTER, GridBagConstraints.NONE);

        addComponent(northPanel,buttonPanel, 4, 0, 1, 1,
                GridBagConstraints.CENTER, GridBagConstraints.NONE);

        //northPanel.add(buttonPanel);
        northPanel.revalidate();
        return northPanel;
    }


    private  void addComponent(Container container, Component component,
                               int gridx, int gridy, int gridwidth, int gridheight, int anchor,
                               int fill) {
            GridBagConstraints gbc = new GridBagConstraints(gridx, gridy,
              gridwidth, gridheight, 1.0, 1.0, anchor, fill, insets, 0, 0);
            container.add(component, gbc);
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

     /**
     * Collates the current selected nodes genes to represent the expression of the genes that
     * are in all the selected nodes.  and sets the expression sets (both if there are two datasets)
     *
     * @param params - enrichment map parameters of the current map
     *
     */
    private void setNodeExpressionSet(EnrichmentMapParameters params){

        Object[] nodes = params.getSelectedNodes().toArray();
         HashMap<String,GeneSet> genesets = params.getGenesetsOfInterest();

        //go through the nodes only if there are some
        if(nodes.length > 0){
            HashSet<Integer> union = new HashSet<Integer>();

            for (Object node1 : nodes) {

                Node current_node = (Node) node1;
                String nodename = current_node.getIdentifier();
                GeneSet current_geneset = genesets.get(nodename);
                if (current_geneset == null)
                    continue;

                HashSet<Integer> current_set = current_geneset.getGenes();

                if (union == null) {
                    union = new HashSet<Integer>(current_set);

                } else {
                    union.addAll(current_set);

                }


            }

            HashSet<Integer> genes = union;
            currentExpressionSet = params.getExpression().getExpressionMatrix(genes);
            if(params.isData2())
                currentExpressionSet2 =params.getExpression2().getExpressionMatrix(genes);

        }
        else{
            currentExpressionSet = null;
            currentExpressionSet2 = null;
        }


    }

    /**
     * Collates the current selected edges genes to represent the expression of the genes that
     * are in all the selected edges.
     *
     * @param params - enrichment map parameters of the current map
     *
     */
    private void setEdgeExpressionSet(EnrichmentMapParameters params){

        Object[] edges = params.getSelectedEdges().toArray();

        if(edges.length>0){
            HashSet<Integer> intersect = null;
            //HashSet union = null;

            for(int i = 0; i< edges.length;i++){

                Edge current_edge = (Edge) edges[i];
                String edgename = current_edge.getIdentifier();


                GenesetSimilarity similarity = params.getGenesetSimilarity().get(edgename);
                if(similarity == null)
                    continue;

                HashSet<Integer> current_set = similarity.getOverlapping_genes();

                //if(intersect == null && union == null){
                if(intersect == null){
                    intersect = new HashSet<Integer>(current_set);
                    //union = new HashSet(current_set);
                }else{
                    intersect.retainAll(current_set);
                    //union.addAll(current_set);
                }

                if(intersect.size() < 1)
                    break;

            }
            currentExpressionSet = params.getExpression().getExpressionMatrix(intersect);
            if(params.isData2())
                currentExpressionSet2 = params.getExpression2().getExpressionMatrix(intersect);
        }
        else{
            currentExpressionSet = null;
            currentExpressionSet2 = null;
        }
    }

    /**
     * Get the current specified rank file.
     *
     * @return current ranking specified by sort by combo box
     */
    private HashMap<Integer,Ranking> getRanks(){
        //Get the ranks for all the keys, if there is a ranking file
        HashMap<Integer,Ranking> ranks = null;

        HashMap<String, HashMap<Integer, Ranking>> all_ranks = params.getRanks();
        if(hmParams.isSortbyrank()){
            for(Iterator j = all_ranks.keySet().iterator(); j.hasNext(); ){
                String ranks_name = j.next().toString();
                if(ranks_name.equalsIgnoreCase(hmParams.getRankFileIndex()))
                    ranks = all_ranks.get(ranks_name);
            }

            if(ranks == null)
               throw new IllegalThreadStateException("invalid sort index for rank files.");

        }

        else{
            ranks = getRanksByClustering();
        }
        return ranks;
    }

    /**
     * Hierarchical clusters the current expression set using pearson correlation and generates ranks
     * based on the the clutering output.
     *
     * @return set of ranks based on the hierarchical clustering of the current expression set.
     */
    private HashMap<Integer, Ranking> getRanksByClustering(){

        HashMap<Integer, Ranking> ranks = null;

        //The number of conditions includes the name and description
        //compute the number of data columns we have.  If there is only one data
        //column we can not cluster data
        int numdatacolumns = 0;
        int numdatacolumns2 = 0;

        if(numConditions > 0){
            numdatacolumns = numConditions - 2;
        }

        if(numConditions2 > 0){
            numdatacolumns2 = numConditions2 - 2;
        }
        //only create a ranking if there are genes in the expression set and there
        //is more than one column of data
        if((currentExpressionSet.keySet().size() > 1) && ((numdatacolumns + numdatacolumns2) > 1)){

            //create an arraylist of the expression subset.
            List clustering_expressionset = new ArrayList() ;
            ArrayList labels = new ArrayList();
            int j = 0;

            //go through the expressionset hashmap and add the key to the labels and add the expression to the clustering set
            for(Iterator i = currentExpressionSet.keySet().iterator();i.hasNext();){
                Integer key = (Integer)i.next();

                Double[] x = ((GeneExpression)currentExpressionSet.get(key)).getExpression();
                Double[] z;
                if(params.isData2()){
                    Double[] y = ((GeneExpression)currentExpressionSet2.get(key)).getExpression();
                    z = new Double[x.length + y.length];
                    System.arraycopy(x,0,z,0,x.length);
                    System.arraycopy(y,0,z,x.length,y.length);

                }
                else{
                    z = x;
                }

                //add the expresionset
                clustering_expressionset.add(j, z);

                //add the key to the labels
                labels.add(j,key);

                j++;
            }

            //create a distance matrix the size of the expression set
            DistanceMatrix distanceMatrix = new DistanceMatrix(currentExpressionSet.keySet().size());
            distanceMatrix.calcDistances(clustering_expressionset, new AlignExpressionDataDistance());

            distanceMatrix.setLabels(labels);

            //cluster
            AvgLinkHierarchicalClustering cluster = new AvgLinkHierarchicalClustering(distanceMatrix);
            cluster.setOptimalLeafOrdering(true);
            cluster.run();

            int[] order = cluster.getLeafOrder();
            ranks = new HashMap<Integer,Ranking>();
            for(int i =0;i< order.length;i++){
                 //get the label
                Integer label =  (Integer)labels.get(order[i]);
                Ranking temp = new Ranking(((GeneExpression)currentExpressionSet.get(label)).getName(),0.0,i);
                ranks.put(label,temp);
            }
        }
        else if((currentExpressionSet.keySet().size() == 1) || ((numdatacolumns + numdatacolumns2) <= 1)){
            ranks = new HashMap<Integer,Ranking>();
            for(Iterator i = currentExpressionSet.keySet().iterator();i.hasNext();){
                Integer key = (Integer)i.next();
                Ranking temp = new Ranking(((GeneExpression)currentExpressionSet.get(key)).getName(),0.0,0);
                ranks.put(key,temp);
            }
        }
        return ranks;
    }

    /**
     * For each heat map there is a standard menu built from the linkouts which are stored in the Cytoscape
     * Properties.  when the heat map is first built initialze this linkout list
     *
     * This code was pulled from the CyAttributeBrowserTable.java in cytoscape coreplugins
     *
     */
     private void initialize_linkouts(){
        // First, load existing property
        Properties props = CytoscapeInit.getProperties();

        // Use reflection to get resource
        Class linkout = null;

        try {
            linkout = Class.forName("linkout.LinkOut");
        } catch (ClassNotFoundException e1) {
            JOptionPane.showMessageDialog(Cytoscape.getDesktop(),"Could't create LinkOut class","Could't create LinkOut class",JOptionPane.WARNING_MESSAGE);
            return;
        }

        final ClassLoader cl = linkout.getClassLoader();

        try {
            props.load(cl.getResource("linkout.props").openStream());
        } catch (IOException e1) {
            JOptionPane.showMessageDialog(Cytoscape.getDesktop(),"Could't read LinkOut class","Could't read LinkOut class",JOptionPane.WARNING_MESSAGE);
        }

        linkoutProps = new HashMap<String, Map<String, String>>();

		final String nodeLink = "nodelinkouturl";

		String[] parts = null;

		for (Map.Entry<Object, Object> entry : props.entrySet()) {
			Map<String, String> pair = null;

			if (entry.getKey().toString().startsWith(nodeLink)) {
				parts = entry.getKey().toString().split("\\.");

				if (parts.length == 3) {
					pair = linkoutProps.get(parts[1]);

					if (pair == null) {
						pair = new HashMap<String, String>();
						linkoutProps.put(parts[1], pair);
					}

					pair.put(parts[2], entry.getValue().toString());
				}
			}
		}

    }



    //Getters and Setters

    Object[][] getExpValue() {
        return expValue;
    }
    private Object getExpValue(int row, int col) {
        return expValue[row][col];
    }
    private String[] gethRow1() {
        return hRow1;
    }

    private void sethRow1(String[] hRow1) {
        this.hRow1 = hRow1;
    }

    private String[] gethRow2() {
        return hRow2;
    }

    private void sethRow2(String[] hRow2) {
        this.hRow2 = hRow2;
    }

    private void setExpValue(int i,int j,Object expressionValues2) {
        this.expValue[i][j] = expressionValues2;
    }
    void setExpValue(Object[][] expValue){
        this.expValue=expValue;
    }

    public void clearPanel(){
        this.removeAll();
        this.revalidate();
    }

    /**
     * @param halfRow1Length the halfRow1Length to set
     */
    public void setHalfRow1Length(int[] halfRow1Length) {
        this.halfRow1Length = halfRow1Length;
    }

    /**
     * @return the halfRow1Length
     */
    public int[] getHalfRow1Length() {
        return halfRow1Length;
    }

    /**
     * @param hRow2Length the hRow2Length to set
     */
    public void setHalfRow2Length(int[] hRow2Length) {
        this.halfRow2Length = hRow2Length;
    }

    /**
     * @return the hRow2Length
     */
    public int[] getHalfRow2Length() {
        return halfRow2Length;
    }

    /**
     * @param themeHalfRow1 the themeHalfRow1 to set
     */
    public void setThemeHalfRow1(ColorGradientTheme [] themeHalfRow1) {
        this.themeHalfRow1 = themeHalfRow1;
    }

    /**
     * @return the themeHalfRow1
     */
    public ColorGradientTheme [] getThemeHalfRow1() {
        return themeHalfRow1;
    }

    /**
     * @param themeHalfRow2 the themeHalfRow2 to set
     */
    public void setThemeHalfRow2(ColorGradientTheme [] themeHalfRow2) {
        this.themeHalfRow2 = themeHalfRow2;
    }

    /**
     * @return the themeHalfRow2
     */
    public ColorGradientTheme [] getThemeHalfRow2() {
        return themeHalfRow2;
    }

    /**
     * @param rangeHalfRow1 the rangeHalfRow1 to set
     */
    public void setRangeHalfRow1(ColorGradientRange [] rangeHalfRow1) {
        this.rangeHalfRow1 = rangeHalfRow1;
    }

    /**
     * @return the rangeHalfRow1
     */
    public ColorGradientRange [] getRangeHalfRow1() {
        return rangeHalfRow1;
    }

    /**
     * @param rangeHalfRow2 the rangeHalfRow2 to set
     */
    public void setRangeHalfRow2(ColorGradientRange [] rangeHalfRow2) {
        this.rangeHalfRow2 = rangeHalfRow2;
    }

    /**
     * @return the rangeHalfRow2
     */
    public ColorGradientRange [] getRangeHalfRow2() {
        return rangeHalfRow2;
    }

    /**
     * @param isHalfRow1 the isHalfRow1 to set
     */
    public void setIsHalfRow1(boolean[] isHalfRow1) {
        this.isHalfRow1 = isHalfRow1;
    }

    /**
     * @return the isHalfRow1
     */
    public boolean[] getIsHalfRow1() {
        return isHalfRow1;
    }

    /**
     * @param isHalfRow2 the isHalfRow2 to set
     */
    public void setIsHalfRow2(boolean[] isHalfRow2) {
        this.isHalfRow2 = isHalfRow2;
    }

    /**
     * @return the isHalfRow2
     */
    public boolean[] getIsHalfRow2() {
        return isHalfRow2;
    }

    /**
     * @param rowGeneName the rowGeneName to set
     */
    public void setRowGeneName(String[] rowGeneName) {
        this.rowGeneName = rowGeneName;
    }

    /**
     * @return the rowGeneName
     */
    public String[] getRowGeneName() {
        return rowGeneName;
    }

    /**
     * @param rowLength the rowLength to set
     */
    public void setRowLength(int[] rowLength) {
        this.rowLength = rowLength;
    }

    /**
     * @return the rowLength
     */
    public int[] getRowLength() {
        return rowLength;
    }

    private ColorGradientRange[] getRowRange() {
        return rowRange;
    }

    private void setRowRange(ColorGradientRange[] rowRange) {
        this.rowRange = rowRange;
    }

    /**
     * @param rowTheme the rowTheme to set
     */
    public void setRowTheme(ColorGradientTheme[] rowTheme) {
        this.rowTheme = rowTheme;
    }

    /**
     * @return the rowTheme
     */
    public ColorGradientTheme[] getRowTheme() {
        return rowTheme;
    }



}


