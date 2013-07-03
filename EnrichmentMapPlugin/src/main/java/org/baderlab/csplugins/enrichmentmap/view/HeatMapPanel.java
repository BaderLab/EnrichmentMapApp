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

package org.baderlab.csplugins.enrichmentmap.view;


import cytoscape.util.FileUtil;
import cytoscape.CytoscapeInit;
import cytoscape.Cytoscape;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.table.*;
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
import org.baderlab.csplugins.enrichmentmap.EnrichmentMapParameters;
import org.baderlab.csplugins.enrichmentmap.EnrichmentMapVisualStyle;
import org.baderlab.csplugins.enrichmentmap.heatmap.*;
import org.baderlab.csplugins.enrichmentmap.model.*;
import org.baderlab.csplugins.enrichmentmap.parsers.EnrichmentResultFileReaderTask;

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

    /**
     * 
     */
    private static final long serialVersionUID = 1903063204304411983L;
    
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
    
    private JRadioButton colorOn;
    private JRadioButton colorOff;
    
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
    //private boolean[] isHalfRow1;
    //private boolean[] isHalfRow2;
    private  final Insets insets = new Insets(0,0,0,0);

    //current subset of expression data from dataset 1 expression set
    private HashMap<Integer, GeneExpression> currentExpressionSet;
     //current subset of expression data from dataset 2 expression set
    private HashMap<Integer, GeneExpression> currentExpressionSet2;

    private boolean node=true;

    private boolean shownPearsonErrorMsg = false;

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
    private EnrichmentMap map;

    //create a pop up menu for linkouts
    private JPopupMenu rightClickPopupMenu;

    //for the linkout properties that are loaded from cytoscape properties.
    private Map<String, Map<String, String>> linkoutProps;

    //for displaying leading edge
    private boolean displayLeadingEdge = false;
    private double leadingEdgeScoreAtMax1 = 0;
    private double leadingEdgeScoreAtMax2 = 0;
    private int leadingEdgeRankAtMax1 = 0;
    private int leadingEdgeRankAtMax2 = 0;

    //objects needed access to in order to print to pdf
    //private JScrollPane jScrollPane;
    private JTable jTable1;
    private JTableHeader tableHdr;
    private JPanel northPanel;

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
    public void resetVariables(EnrichmentMap map){
        this.map = map;
        this.params = map.getParams();

        if(params.isData() || params.isData2()){
            GeneExpressionMatrix expression = map.getDataset(EnrichmentMap.DATASET1).getExpressionSets();
            numConditions = expression.getNumConditions();
            columnNames = expression.getColumnNames();

            phenotypes = expression.getPhenotypes();

            this.Dataset1phenotype1 = params.getFiles().get(EnrichmentMap.DATASET1).getPhenotype1();
            this.Dataset1phenotype2 = params.getFiles().get(EnrichmentMap.DATASET1).getPhenotype2();

            hmParams = params.getHmParams();
            boolean[] ascending;
            if(expression.getRanks() != null){
            		ascending = new boolean[columnNames.length + expression.getRanks().size()];
            		//set the rank files to ascending
            		for(int k = ascending.length ;  k > (ascending.length - expression.getRanks().size()); k--)
            			ascending[k-1] = true;
            }
            else
            		ascending = new boolean[columnNames.length];
            
            hmParams.setAscending(ascending);            

            displayLeadingEdge = false;
            leadingEdgeScoreAtMax1 = 0.0;
            leadingEdgeScoreAtMax2 = 0.0;
            leadingEdgeRankAtMax1 = 0;
            leadingEdgeRankAtMax2 = 0;

            //get the current expressionSet
            if(node)
                setNodeExpressionSet(params);
            else
                setEdgeExpressionSet(params);

            if(params.isData2() && map.getDataset(EnrichmentMap.DATASET2).getExpressionSets() != null && !map.getDataset(EnrichmentMap.DATASET1).getExpressionSets().getFilename().equalsIgnoreCase(map.getDataset(EnrichmentMap.DATASET2).getExpressionSets().getFilename())){

                GeneExpressionMatrix expression2 = map.getDataset(EnrichmentMap.DATASET2).getExpressionSets();

                numConditions2 = expression2.getNumConditions();
                columnNames2 = expression2.getColumnNames();

                boolean[] ascending2 = new boolean[columnNames.length + (columnNames2.length-2) +expression2.getRanks().size()];
                for(int k = ascending2.length ;  k > ascending2.length - expression2.getRanks().size(); k--)
                    ascending2[k-1] = true;
                hmParams.setAscending(ascending2);//we don't have to repeat the name and description columns

            }
            
            //if there are two expression sets, regardless if they are the same get the phenotypes of the second file.
            if(params.isData2() && map.getDataset(EnrichmentMap.DATASET2).getExpressionSets() != null){

                phenotypes2 = map.getDataset(EnrichmentMap.DATASET2).getExpressionSets().getPhenotypes();

                this.Dataset2phenotype1 = params.getFiles().get(EnrichmentMap.DATASET2).getPhenotype1();
                this.Dataset2phenotype2 = params.getFiles().get(EnrichmentMap.DATASET2).getPhenotype2();

            }

        }
    }

    /**
     * Update the panel base on given enrichment parameters
     *
     * @param params - enrichment map parameters to update the heat map to.
     */
    public void updatePanel(EnrichmentMap map){

        resetVariables(map);
        updatePanel();
    }

    /**
     * Update the heat map panel
     */
    public void updatePanel(){
    		//EnrichmentMapParameters params = map.getParams();
        if((currentExpressionSet != null) || (currentExpressionSet2 != null)){


            JTable rowTable;
            String[] mergedcolumnNames = null;

            JPanel mainPanel = new JPanel();
            mainPanel.setLayout(new BorderLayout());
            TableSort sort;

            HeatMapTableModel OGT;
            Object[][] data;

            //create data subset
            if(params.isData2() && map.getDataset(EnrichmentMap.DATASET2).getExpressionSets() != null && !map.getDataset(EnrichmentMap.DATASET1).getExpressionSets().getFilename().equalsIgnoreCase(map.getDataset(EnrichmentMap.DATASET2).getExpressionSets().getFilename())){

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

            CellHighlightRenderer highlightCellRenderer = new CellHighlightRenderer();

            sort = new TableSort(OGT);
            jTable1 = new JTable(sort);

            //add a listener to the table
            jTable1.addMouseListener(new HeatMapTableActionListener(jTable1,OGT, rightClickPopupMenu,linkoutProps));

            // used for listening to columns when clicked
            TableHeader header = new TableHeader(sort, jTable1, hmParams);

            tableHdr= jTable1.getTableHeader();
            tableHdr.addMouseListener(header);

            //check to see if there is already a sort been defined for this table
            //if(hmParams.isSortbycolumn()){
            if(hmParams.getSort() == HeatMapParameters.Sort.COLUMN){
                boolean ascending;
                ascending = hmParams.isAscending(hmParams.getSortIndex());

                if(hmParams.getSortIndex()>=columnNames.length)
                    hmParams.setSortbycolumnName((String)columnNames2[hmParams.getSortIndex()-columnNames.length+2]);

                else
                    hmParams.setSortbycolumnName((String)columnNames[hmParams.getSortIndex()]);

                header.sortByColumn(hmParams.getSortIndex(), ascending);
            }

            //Set up renderer and editor for the Color column.
            //default column width.  If we are using coloring default should be 10.  If we are using values default should be 50
            int defaultColumnwidth = 10;
            if(this.hmParams.isColoroff()){
            		jTable1.setDefaultRenderer(ExpressionTableValue.class, new RawExpressionValueRenderer());
            		defaultColumnwidth = 10;
            }
            else
            		jTable1.setDefaultRenderer(ExpressionTableValue.class,new ColorRenderer());

            //renderer for leading edge
            jTable1.setDefaultRenderer(String.class, highlightCellRenderer);
            

            //even though the renderer takes into account what to do with significantGene type
            //it is very important to define the renderer for the type specifically as the JTable
            //makes the assumption that the type of the first object in the table is the same for the rest
            //of the column and if it is a Significant gene it will default a general object renderer to the
            //whole column
            jTable1.setDefaultRenderer(SignificantGene.class, highlightCellRenderer);
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

            if(params.isData2() && map.getDataset(EnrichmentMap.DATASET2).getExpressionSets() != null && !map.getDataset(EnrichmentMap.DATASET1).getExpressionSets().getFilename().equalsIgnoreCase(map.getDataset(EnrichmentMap.DATASET2).getExpressionSets().getFilename())){
                //go through the first data set
                for (int i=0;i<columnNames.length;i++){
                    if (i==0 || columnNames[i].equals("Name"))
                        tcModel.getColumn(i).setPreferredWidth(50);
                    else if (i==1 || columnNames[i].equals("Description"))
                        tcModel.getColumn(i).setPreferredWidth(50);
                    else{
                        tcModel.getColumn(i).setPreferredWidth(defaultColumnwidth);
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
                        tcModel.getColumn(i).setPreferredWidth(defaultColumnwidth);
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
                        tcModel.getColumn(i).setPreferredWidth(defaultColumnwidth);
                        if(phenotypes != null){
                            if(phenotypes[i-2].equalsIgnoreCase(Dataset1phenotype1))
                                tcModel.getColumn(i).setHeaderRenderer(pheno1_renderer);
                            else if(phenotypes[i-2].equalsIgnoreCase(Dataset1phenotype2))
                                tcModel.getColumn(i).setHeaderRenderer(pheno2_renderer);
                            //Needed these extra lines when only have one expression file with both datasets.
                            else if(phenotypes[i-2].equalsIgnoreCase(Dataset2phenotype1))
                                tcModel.getColumn(i).setHeaderRenderer(pheno1_renderer);
                            else if(phenotypes[i-2].equalsIgnoreCase(Dataset2phenotype2))
                                tcModel.getColumn(i).setHeaderRenderer(pheno2_renderer);
                            else
                                tcModel.getColumn(i).setHeaderRenderer(default_renderer);
                        }
                        else
                             tcModel.getColumn(i).setHeaderRenderer(default_renderer);
                    }

                }
            }

            JScrollPane jScrollPane;
            jTable1.setColumnModel(tcModel);
            jScrollPane = new javax.swing.JScrollPane(jTable1);
            rowTable =new RowNumberTable(jTable1);
            jScrollPane.setRowHeaderView(rowTable);

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
            			data[k][j+2] = new ExpressionTableValue((Double)expValue[k][j+2], ColorGradientMapper.getColorGradient(RowCRT[k],RowCRR[k],RowGene[k],(Double)expValue[k][j+2]));
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


        int kValue;

        if(params.isTwoDistinctExpressionSets())
            kValue = expValue.length;
            //kValue = currentExpressionSet.size() + currentExpressionSet2.size();
        else
            kValue=Math.max(currentExpressionSet.size(), currentExpressionSet2.size());

        int totalConditions;
        //if either of the sets is a rank file instead of expression set then there is only one column
        if(numConditions == 2 && numConditions2 ==2)
        		totalConditions = 2 +1;
        else if(numConditions==2)
        		totalConditions = 1 + numConditions2 ;
        else if(numConditions2==2)
        		totalConditions = 1 + numConditions ;
        else
        		totalConditions= (numConditions + numConditions2-2);

        data = new Object[kValue][totalConditions];
        for(int k=0;k<kValue;k++){


            data[k][0] = expValue[k][0];
            data[k][1] = expValue[k][1];

            for(int j=0;j<HRow1[k];j++){
        				data[k][j+2] = new ExpressionTableValue((Double)expValue[k][j+2], ColorGradientMapper.getColorGradient(Row1CRT[k],Row1CRR[k],Row1gene[k],(Double)expValue[k][j+2]));
           }

            for(int j=HRow1[k];j<HRow2[k];j++){
            		
        				data[k][j+2] = new ExpressionTableValue((Double)expValue[k][j+2], ColorGradientMapper.getColorGradient(Row2CRT[k],Row2CRR[k],Row2gene[k],(Double)expValue[k][j+2]));
        			
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

        Ranking ranks = getRanks(currentExpressionSet);

        //if the ranks are the GSEA ranks and the leading edge is activated then we need to highlight
        //genes in the leading edge
        int topRank = -1;
        boolean isNegative = false;

        //just having one node is not suffiecient reason for dispalying the the leading edge make sure we are sorted by rank and that there is a rank file.
        //The issue is that the expression subset is only updated on node selection and that is where we determine if it is
        //a selection qualified for leadingedge annotation but the user can change the sorting option without updating the
        //selection.
        if (this.displayLeadingEdge &&  map.getDataset(EnrichmentMap.DATASET1).getExpressionSets().haveRanks() && (hmParams.getSort() == HeatMapParameters.Sort.RANK|| params.getDefaultSortMethod().equalsIgnoreCase(hmParams.getSort().toString()))){
            topRank = getTopRank();
            if(hmParams.getRankFileIndex().equalsIgnoreCase("Dataset 1 Ranking")|| hmParams.getRankFileIndex().equalsIgnoreCase(Ranking.GSEARanking))
                isNegative = isNegativeGS(1);
            else if(hmParams.getRankFileIndex().equalsIgnoreCase("Dataset 2 Ranking")|| hmParams.getRankFileIndex().equalsIgnoreCase(Ranking.GSEARanking))
                isNegative = isNegativeGS(2);
        }
        
        int n = 0;
        HashMap<Integer,Rank> current_ranks = ranks.getRanking();
        for(Iterator<Integer> i = currentExpressionSet.keySet().iterator();i.hasNext();){
            Integer key = i.next();

            //check to see the key is in the rank file.
            //For new rank files it is possible that some of the genes/proteins won't be ranked
            if(current_ranks.containsKey(key)){
                ranks_subset[n] = ((Rank)current_ranks.get(key)).getRank();
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

        //sort the ranks according to the ascending flag
        boolean ascending = hmParams.isAscending(hmParams.getSortIndex());

        //Doctor the sorting to always have the leading edge at the top
        //if it is supposed to be ascending and the gene set isNegative then reverse
        //the sorting
        //Displayleadingedge is specific for calculating the ranking for GSEA when one gene set is selected.
        //to make sure the doctoring of the sorting only happens with the leading edge stuff make sure the sorting
        //is by Rank.
        if(ascending && isNegative && this.displayLeadingEdge && hmParams.getSort() == HeatMapParameters.Sort.RANK){
            hmParams.changeAscendingValue(hmParams.getSortIndex());
            ascending = false;
        }
        else if(!ascending  && !isNegative && this.displayLeadingEdge && hmParams.getSort() == HeatMapParameters.Sort.RANK){
            hmParams.changeAscendingValue(hmParams.getSortIndex());
            ascending = true;
        }

        if(ascending)
        //sort ranks
            Arrays.sort(ranks_subset);
        else
            Arrays.sort(ranks_subset,Collections.reverseOrder());

        int k = 0;
        int previous = -1;
        boolean significant_gene = false;

        for(int m = 0 ; m < ranks_subset.length;m++){
             //if the current gene doesn't have a rank then don't show it
            if(ranks_subset[m] == -1)
                continue;

            if(ranks_subset[m] == previous)
               continue;

            previous = ranks_subset[m];

            significant_gene = false;

            if(ranks_subset[m] <= topRank && !isNegative && topRank != 0 && topRank != -1)
                significant_gene = true;
            else if(ranks_subset[m] >= topRank && isNegative && topRank != 0 && topRank != -1)
                significant_gene = true;

            ArrayList<Integer> keys = rank2keys.get(ranks_subset[m]);

            for(Iterator<Integer> p = keys.iterator();p.hasNext();){
                Integer key = (Integer)p.next();

                //Current expression row
                GeneExpression row 		= (GeneExpression)currentExpressionSet.get(key);
                Double[] expression_values = getExpression(row,1);

                // stores the gene names in column 0
                try{ // stores file name if the file contains integer (inserted to aid sorting)
                    if(significant_gene)
                        //cast the gene name as Significant gene type significant gene list
                        //significantGenes.add(row.getName());
                        expValue[k][0]= new SignificantGene(Integer.parseInt(row.getName()));
                    else
                        expValue[k][0]=Integer.parseInt(row.getName());
                }
                catch (NumberFormatException v){   // if not integer stores as string (inserted to aid sorting)
                    if(significant_gene)
                        //cast the gene name as Significant gene type significant gene list
                        //significantGenes.add(row.getName());
                        expValue[k][0]= new SignificantGene(row.getName());
                    else
                        expValue[k][0]=row.getName();


                }


                expValue[k][1]=row.getDescription();

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

        int totalConditions;
        //if either of the sets is a rank file instead of expression set then there is only one column
        if(numConditions == 2 && numConditions2 ==2)
        		totalConditions = 2 +1;
        else if(numConditions==2)
        		totalConditions = 1 + numConditions2 ;
        else if(numConditions2==2)
        		totalConditions = 1 + numConditions ;
        else
        		totalConditions= (numConditions + numConditions2-2);

        //when constructing a heatmap and the expression matrices do not contain the exact same set of
        //gene we have to use an expression set consisting of a merged set from the two expression sets.
        HashMap<Integer, GeneExpression> expressionUsing;
        if (currentExpressionSet.size() == 0)
            expressionUsing = currentExpressionSet2;
        else if(currentExpressionSet2.size() == 0)
            expressionUsing = currentExpressionSet;
        else{
            HashMap<Integer, GeneExpression> mergedExpression = new HashMap<Integer, GeneExpression>();
            
            mergedExpression.putAll(currentExpressionSet);
            	
            //go through the second and only add the expression that isn't in the first set
            for(Iterator<Integer> i = currentExpressionSet2.keySet().iterator();i.hasNext();){
                Integer key = i.next();
                GeneExpression exp2 = currentExpressionSet2.get(key);
                if(!mergedExpression.containsKey(key)){
            			//merge            			
            			//add to mergedExpression Set
            			mergedExpression.put(key,  exp2);
            }
            }
            expressionUsing = mergedExpression;
        }


        int kValue;

        if(params.isTwoDistinctExpressionSets())
            kValue = expressionUsing.size();
        else
            kValue=Math.max(currentExpressionSet.size(), currentExpressionSet2.size());
        expValue = new Object[kValue][totalConditions];
        hRow1= new String[kValue];
        hRow2= new String[kValue];
        halfRow1Length= new int[kValue];
        halfRow2Length= new int[kValue];
        rangeHalfRow1= new ColorGradientRange[kValue];
        rangeHalfRow2= new ColorGradientRange[kValue];
        themeHalfRow1= new ColorGradientTheme[kValue];
        themeHalfRow2= new ColorGradientTheme[kValue];

        Integer[] ranks_subset = new Integer[kValue];

        HashMap<Integer, ArrayList<Integer>> rank2keys = new HashMap<Integer,ArrayList<Integer>>();

        Ranking ranks = getRanks(expressionUsing);

        //if the ranks are the GSEA ranks and the leading edge is activated then we need to highlight
        //genes in the leading edge
        int topRank = getTopRank();
        boolean isNegative = false;
        if(hmParams.getRankFileIndex().equalsIgnoreCase("Dataset 1 Ranking") || hmParams.getRankFileIndex().equalsIgnoreCase(Ranking.GSEARanking))
            isNegative = isNegativeGS(1);
        else if(hmParams.getRankFileIndex().equalsIgnoreCase("Dataset 2 Ranking") || hmParams.getRankFileIndex().equalsIgnoreCase(Ranking.GSEARanking))
            isNegative = isNegativeGS(2);


        int n = 0;
        int maxRank = 0;
        int missingRanksCount = 0;
        	HashMap <Integer,Rank> current_ranks = ranks.getRanking();
        for(Iterator<Integer> i = expressionUsing.keySet().iterator();i.hasNext();){
            Integer key = i.next();
            //check to see the key is in the rank file.
            //For new rank files it is possible that some of the genes/proteins won't be ranked
            if(current_ranks.containsKey(key)){
                ranks_subset[n] = ((Rank)current_ranks.get(key)).getRank();

                if(ranks_subset[n] > maxRank)
                    maxRank = ranks_subset[n];
                //check to see if the rank is already in the list.
            }
            else{
                ranks_subset[n] = -1;
                missingRanksCount++;
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


        //A rank of -1 can indicate a missing rank which is expected when you have two distinct datasets
        //We want to make sure the -1 are at the bottom of the list with two distinct data sets
        if(params.isTwoDistinctExpressionSets() && missingRanksCount > 0 ){
            for(int s = 0 ; s < ranks_subset.length;s++){
                //if the current gene doesn't have a rank then don't show it
                if(ranks_subset[s] == -1)
                    ranks_subset[s] = maxRank + 100;

                //update the ranks2keys subset
                rank2keys.put(maxRank + 100, rank2keys.get(-1));
            }
        }

        //depending on the set value of ascending dictates which direction the ranks are
        //outputed.  if ascending then output the ranks as is, if not ascending (descending) inverse
        //the order of the ranks.
        boolean ascending = hmParams.isAscending(hmParams.getSortIndex());

       if(ascending)
            //sort ranks
             Arrays.sort(ranks_subset);
       else
             Arrays.sort(ranks_subset,Collections.reverseOrder());


        int k = 0;
        int previous = -1;
         boolean significant_gene = false;


        for(int m = 0 ; m < ranks_subset.length;m++){
            //if the current gene doesn't have a rank then don't show it
            if(ranks_subset[m] == -1)
                continue;

            if(ranks_subset[m] == previous)
                continue;

            previous = ranks_subset[m];

            significant_gene = false;

            if(ranks_subset[m] <= topRank && !isNegative && topRank != 0 && topRank != -1)
                significant_gene = true;
            else if(ranks_subset[m] >= topRank && isNegative && topRank != 0 && topRank != -1)
                significant_gene = true;

            ArrayList<Integer> keys = rank2keys.get(ranks_subset[m]);

            for(Iterator<Integer> p = keys.iterator();p.hasNext();){
                Integer currentKey = p.next();

                //Current expression row
                GeneExpression halfRow1 = (GeneExpression)currentExpressionSet.get(currentKey);

                //get the corresponding row from the second dataset
                GeneExpression halfRow2 = (GeneExpression)currentExpressionSet2.get(currentKey);

                Double[] expression_values1 = getExpression(halfRow1,1);
                Double[] expression_values2 = getExpression(halfRow2,2);

                //Get the name and the description of the row
                if(halfRow1 != null){
                     // stores the gene names in column 0
                    try{ // stores file name if the file contains integer (inserted to aid sorting)
                        if(significant_gene)
                            //cast the gene name as Significant gene type significant gene list
                            //significantGenes.add(row.getName());
                            expValue[k][0]= new SignificantGene(Integer.parseInt(halfRow1.getName()));

                        else
                            expValue[k][0]=Integer.parseInt(halfRow1.getName());
                    }
                    catch (NumberFormatException v){   // if not integer stores as string (inserted to aid sorting)
                        if(significant_gene)
                            //cast the gene name as Significant gene type significant gene list
                            //significantGenes.add(row.getName());
                            expValue[k][0]= new SignificantGene(halfRow1.getName());
                        else
                            expValue[k][0]=halfRow1.getName();
                    }
                    expValue[k][1]= halfRow1.getDescription();
                }
                else if(halfRow2 != null){
                        // stores the gene names in column 0
                    try{ // stores file name if the file contains integer (inserted to aid sorting)
                        if(significant_gene)
                            //cast the gene name as Significant gene type significant gene list
                            //significantGenes.add(row.getName());
                            expValue[k][0]= new SignificantGene(Integer.parseInt(halfRow2.getName()));

                        else
                            expValue[k][0]=Integer.parseInt(halfRow2.getName());
                    }
                    catch (NumberFormatException v){   // if not integer stores as string (inserted to aid sorting)
                        if(significant_gene)
                            //cast the gene name as Significant gene type significant gene list
                            //significantGenes.add(row.getName());
                            expValue[k][0]= new SignificantGene(halfRow2.getName());
                        else
                            expValue[k][0]=halfRow2.getName();
                    }
                    expValue[k][1]= halfRow2.getDescription();
                }

               /* if(halfRow1 != null){
                    halfRow1Length[k]=halfRow1.getExpression().length;
                    themeHalfRow1[k]=hmParams.getTheme();
                    rangeHalfRow1[k]=hmParams.getRange();
                    hRow1[k]=halfRow1.getName();
                    for(int j = 0; j < halfRow1.getExpression().length;j++){
                        expValue[k][j+2]=expression_values1[j];
                  }
                }

                if(halfRow2 != null){
                    if(halfRow1 == null)
                        halfRow2Length[k]=(halfRow2.getExpression().length);
                    else
                        halfRow2Length[k]=(halfRow1.getExpression().length + halfRow2.getExpression().length);
                    themeHalfRow2[k]=hmParams.getTheme();
                    rangeHalfRow2[k]=hmParams.getRange();
                    hRow2[k]=halfRow1.getName();
                    for(int j = halfRow1.getExpression().length; j < (halfRow1.getExpression().length + halfRow2.getExpression().length);j++){
                              expValue[k][j+2]=expression_values2[j-halfRow1.getExpression().length];                }
                } */


                halfRow1Length[k]=expression_values1.length;
                themeHalfRow1[k]=hmParams.getTheme();
                rangeHalfRow1[k]=hmParams.getRange();

                if(halfRow1 != null)
                    hRow1[k]=halfRow1.getName();
                else
                    hRow1[k]=halfRow2.getName();

                for(int j = 0; j < expression_values1.length;j++){
                        expValue[k][j+2]=expression_values1[j];
                }



               halfRow2Length[k]=(expression_values1.length + expression_values2.length);
               themeHalfRow2[k]=hmParams.getTheme();
               rangeHalfRow2[k]=hmParams.getRange();
               if(halfRow1 != null)
                    hRow2[k]=halfRow1.getName();
                else
                    hRow2[k]=halfRow2.getName();
               for(int j = expression_values1.length; j < (expression_values1.length + expression_values2.length);j++){
                    expValue[k][j+2]=expression_values2[j-expression_values1.length];                }

                k++;
            }

        }
        this.setThemeHalfRow1(themeHalfRow1);
        this.setRangeHalfRow1(rangeHalfRow1);
        this.setHalfRow1Length(halfRow1Length);
        this.sethRow1(hRow1);


        this.setThemeHalfRow2(themeHalfRow2);
        this.setRangeHalfRow2(rangeHalfRow2);
        this.setHalfRow2Length(halfRow2Length);
        this.sethRow2(hRow2);

       this.setExpValue(expValue);
        return expValue;
    }

    /**
     *  Given the gene expression row
     * @return The expression set, transformed according the user specified transformation.
     */
    private Double[] getExpression(GeneExpression row, int dataset){

        Double[] expression_values1 = null;

        if(hmParams.getTransformation() == HeatMapParameters.Transformation.ROWNORM){
            if(row != null)
                expression_values1 = row.rowNormalize();
        }
        else if(hmParams.getTransformation() == HeatMapParameters.Transformation.LOGTRANSFORM){
            if(row != null)
                expression_values1 = row.rowLogTransform();
        }
        else{
            if(row != null)
                expression_values1   = row.getExpression();
       }

        //if either of the expression_values is null set the array to have no data
      if(expression_values1 == null && dataset == 1){
        expression_values1 = new Double[columnNames.length-2];
        for(int q = 0; q < expression_values1.length;q++)
            expression_values1[q] = Double.NaN/*null*/;
        }
      else if(expression_values1 == null && dataset == 2){
        expression_values1 = new Double[columnNames2.length-2];
        for(int q = 0; q < expression_values1.length;q++)
            expression_values1[q] = Double.NaN/*null*/;
        }

      return expression_values1;
    }
    // created new North panel to accommodate the expression legend, normalization options,sorting options, saving option
   private JPanel coloroffPanel(){
       JPanel coloroffPanel= new JPanel() ;
       coloroffPanel.setMaximumSize(new Dimension(50,100));
       coloroffPanel.setMinimumSize(new Dimension(50,100));
       
       TitledBorder expBorder = BorderFactory.createTitledBorder("Expression Values");
       expBorder.setTitleJustification(TitledBorder.LEFT);
       coloroffPanel.setBorder(expBorder);
       
       //add a check box to turn off the color in the heat map.      
       colorOn = new JRadioButton("Hide values");
       colorOn.setActionCommand("on");
       
       colorOff = new JRadioButton("Show values");
       colorOff.setActionCommand("off");
       
       if(this.hmParams.isColoroff()){
   	   		colorOn.setSelected(false);
   	   		colorOff.setSelected(true);
       }
       else{
    	   		colorOn.setSelected(true);
      	   	colorOff.setSelected(false);
       }
       
       
       ButtonGroup colorOnOff = new javax.swing.ButtonGroup();
       colorOnOff.add(colorOn);
       colorOnOff.add(colorOff);
       

       colorOn.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                           selectColorOnOffActionPerformed(evt);
                    }
              });

       colorOff.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                    		selectColorOnOffActionPerformed(evt);
                    }
              });
    

       //create a panel for the two buttons;
       coloroffPanel.setLayout(new BorderLayout());
       coloroffPanel.add(colorOn, BorderLayout.NORTH);
       coloroffPanel.add(colorOff, BorderLayout.SOUTH);
       
       
       return coloroffPanel;
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

        northPanel = new JPanel();// new north panel
        JPanel buttonPanel = new JPanel();// brought button panel from westPanel
        northPanel.setLayout(new GridBagLayout());

        JButton SaveExpressionSet = new JButton("Save Expression Set");
        SaveExpressionSet.addActionListener(new java.awt.event.ActionListener() {
               public void actionPerformed(java.awt.event.ActionEvent evt) {
                      saveExpressionSetActionPerformed(evt);
               }
         });

        JButton exportExpressionSet = new JButton("Export Expression Set (PDF)");
        exportExpressionSet.addActionListener(new java.awt.event.ActionListener() {
               public void actionPerformed(java.awt.event.ActionEvent evt) {
                      exportExpressionSetActionPerformed(evt);
               }
         });

        buttonPanel.add(SaveExpressionSet);
        buttonPanel.add(exportExpressionSet);

        addComponent(northPanel, expressionLegendPanel(), 0, 0, 1, 1,
                GridBagConstraints.WEST, GridBagConstraints.NONE);

        addComponent(northPanel,coloroffPanel(), 1, 0, 1, 1,
                GridBagConstraints.WEST, GridBagConstraints.NONE);


        addComponent(northPanel,hmParams.createDataTransformationOptionsPanel(map), 2, 0, 1, 1,
                GridBagConstraints.CENTER, GridBagConstraints.NONE);

        addComponent(northPanel,hmParams.createSortOptionsPanel(map), 3, 0, 2, 1,
                    GridBagConstraints.CENTER, GridBagConstraints.NONE);

        addComponent(northPanel,buttonPanel, 5, 0, 1, 1,
                GridBagConstraints.CENTER, GridBagConstraints.NONE);

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
                        String[] currentColumns;
                        if(params.isData2() &&  map.getDataset(EnrichmentMap.DATASET2).getExpressionSets() != null
                        		&& !map.getDataset(EnrichmentMap.DATASET1).getExpressionSets().getFilename().equalsIgnoreCase(map.getDataset(EnrichmentMap.DATASET2).getExpressionSets().getFilename())){
                            currentColumns = new String[columnNames.length + columnNames2.length - 2];

                            System.arraycopy(columnNames,0,currentColumns,0,columnNames.length);
                            System.arraycopy(columnNames2,2, currentColumns,columnNames.length,columnNames2.length-2);
                        }
                        else
                            currentColumns = (String[])columnNames;

                        for(int j = 0; j < currentColumns.length;j++)
                            if(j == (currentColumns.length-1))
                                output.write(currentColumns[j] + "\n");
                            else
                                output.write(currentColumns[j] + "\t");

                        //get the sorted expression set
                        Object[][] sortedExpression;
                        if(params.isData2() &&  map.getDataset(EnrichmentMap.DATASET2).getExpressionSets() != null && !map.getDataset(EnrichmentMap.DATASET1).getExpressionSets().getFilename().equalsIgnoreCase(map.getDataset(EnrichmentMap.DATASET2).getExpressionSets().getFilename()))
                            sortedExpression = createSortedMergedTableData();
                        else
                            sortedExpression = createSortedTableData();

                        for(int k = 0; k < sortedExpression.length; k++){
                            for(int l = 0; l< sortedExpression[k].length; l++)
                                output.write(sortedExpression[k][l].toString() + "\t");
                            output.write("\n");
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

    private void exportExpressionSetActionPerformed(ActionEvent evt){
            java.io.File file = FileUtil.getFile("Export Heatmap as pdf File", FileUtil.SAVE);
            if (file != null && file.toString() != null) {
                String fileName = file.toString();
                if (!fileName.endsWith(".pdf")) {
                    fileName += ".pdf";
                    file = new File(fileName);
                }

                int response = JOptionPane.OK_OPTION;
                if(file.exists())
                        response = JOptionPane.showConfirmDialog(this, "The file already exists.  Would you like to overwrite it?");
                if(response == JOptionPane.NO_OPTION || response == JOptionPane.CANCEL_OPTION ){

                }
                else if(response == JOptionPane.YES_OPTION || response == JOptionPane.OK_OPTION){
                        try{
                            FileOutputStream output = new FileOutputStream(file);
                            HeatMapExporter exporter = new HeatMapExporter();
                            exporter.export(this.getNorthPanel(),this.getjTable1(),this.getTableHeader(), output) ;
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
        //all unique genesets - if there are two identical genesets in the two sets then 
        //one of them will get over written in the hash.
        //when using two distinct genesets we need to pull the gene info from each set separately.
        	HashMap<String,GeneSet> genesets = map.getAllGenesetsOfInterest();
        	HashMap<String, GeneSet> genesets_set1 = (map.getDatasets().containsKey(EnrichmentMap.DATASET1)) ? map.getDataset(EnrichmentMap.DATASET1).getSetofgenesets().getGenesets() : null;            
        	HashMap<String, GeneSet> genesets_set2 = (map.getDatasets().containsKey(EnrichmentMap.DATASET2)) ? map.getDataset(EnrichmentMap.DATASET2).getSetofgenesets().getGenesets() : null;
         
        //go through the nodes only if there are some
        if(nodes.length > 0){

            HashSet<Integer> union = new HashSet<Integer>();

            for (Object node1 : nodes) {

                Node current_node = (Node) node1;
                String nodename = current_node.getIdentifier();
                GeneSet current_geneset = genesets.get(nodename);
                HashSet<Integer> additional_set = null;


                //if we can't find the geneset and we are dealing with a two-distinct expression sets, check for the gene set in the second set
                //TODO:Add multi species support
                if(params.isTwoDistinctExpressionSets()){
                    GeneSet current_geneset_set1 = genesets_set1.get(nodename);
                    GeneSet current_geneset_set2 = genesets_set2.get(nodename);
                    if(current_geneset.equals(current_geneset_set1) && current_geneset_set2 != null)
                        additional_set = current_geneset_set2.getGenes();
                    if(current_geneset.equals(current_geneset_set2) && current_geneset_set1 != null)
                        additional_set = current_geneset_set1.getGenes();

                }

                //if only one node is selected activate leading edge potential
                //and if at least one rankfile is present
                //TODO: we probably have to catch cases where we have only a rank file for one of the datasets
                if(nodes.length == 1 ){
                    displayLeadingEdge = true;
                    if(params.getMethod().equalsIgnoreCase(EnrichmentMapParameters.method_GSEA)){

                        HashMap<String, EnrichmentResult> results1 = map.getDataset(EnrichmentMap.DATASET1).getEnrichments().getEnrichments();
                        if(results1.containsKey(nodename)){
                            GSEAResult current_result = (GSEAResult) results1.get(nodename);
                            leadingEdgeScoreAtMax1 = current_result.getScoreAtMax();
                            //if the  score at max is set to the default then get the direction of the leading edge
                            //from the NES
                            if(leadingEdgeScoreAtMax1 == EnrichmentResultFileReaderTask.DefaultScoreAtMax)
                                leadingEdgeScoreAtMax1 = current_result.getNES();

                            leadingEdgeRankAtMax1 = current_result.getRankAtMax();
                        }
                        if(map.getParams().isTwoDatasets()){
                        		HashMap<String, EnrichmentResult> results2 = map.getDataset(EnrichmentMap.DATASET2).getEnrichments().getEnrichments();
                        		if(results2.containsKey(nodename)){
                        			GSEAResult current_result = (GSEAResult) results2.get(nodename);
                        			leadingEdgeScoreAtMax2 = current_result.getScoreAtMax();
                        			//if the  score at max is set to the default then get the direction of the leading edge
                        			//from the NES
                        			if(leadingEdgeScoreAtMax2 == EnrichmentResultFileReaderTask.DefaultScoreAtMax)
                        				leadingEdgeScoreAtMax2 = current_result.getNES();

                        			leadingEdgeRankAtMax2 = current_result.getRankAtMax();
                        		}
                        }
                    }
                }
                if (current_geneset == null)
                    continue;

                HashSet<Integer> current_set = current_geneset.getGenes();

                if (union == null) {
                    union = new HashSet<Integer>(current_set);

                } else {
                    union.addAll(current_set);

                }

                if(additional_set != null)
                    union.addAll(additional_set);
            }

            HashSet<Integer> genes = union;
            currentExpressionSet = map.getDataset(EnrichmentMap.DATASET1).getExpressionSets().getExpressionMatrix(genes);
            if(params.isData2() && map.getDataset(EnrichmentMap.DATASET2).getExpressionSets() != null)
                currentExpressionSet2 =map.getDataset(EnrichmentMap.DATASET2).getExpressionSets().getExpressionMatrix(genes);

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


                GenesetSimilarity similarity = map.getGenesetSimilarity().get(edgename);
                if(similarity == null)
                    continue;

                HashSet<Integer> current_set = similarity.getOverlapping_genes();

                //if(intersect == null && union == null){
                if(intersect == null){
                    intersect = new HashSet<Integer>(current_set);
                }else{
                    intersect.retainAll(current_set);
                }

                if(intersect.size() < 1)
                    break;

            }
            currentExpressionSet = map.getDataset(EnrichmentMap.DATASET1).getExpressionSets().getExpressionMatrix(intersect);
            if(params.isData2() && map.getDataset(EnrichmentMap.DATASET2).getExpressionSets() != null)
                currentExpressionSet2 = map.getDataset(EnrichmentMap.DATASET2).getExpressionSets().getExpressionMatrix(intersect);
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
    private Ranking getRanks(HashMap<Integer, GeneExpression> expressionSet){
        //Get the ranks for all the keys, if there is a ranking file
        Ranking ranks = null;

        //check to see if any of the ordering have been initialized
        if(hmParams.getSort() == HeatMapParameters.Sort.DEFAULT){
            //initialize the default value
            if(params.getDefaultSortMethod().equalsIgnoreCase(HeatMapParameters.sort_hierarchical_cluster))
                hmParams.setSort(HeatMapParameters.Sort.CLUSTER);
            if(params.getDefaultSortMethod().equalsIgnoreCase(HeatMapParameters.sort_rank)){
                hmParams.setSort(HeatMapParameters.Sort.RANK);
                HashSet<String> ranksnames = map.getAllRankNames();
                if(!ranksnames.isEmpty())
                    hmParams.setRankFileIndex(ranksnames.iterator().next());
                else{
                    hmParams.setSort(HeatMapParameters.Sort.NONE);
                }
            }
            if(params.getDefaultSortMethod().equalsIgnoreCase(HeatMapParameters.sort_none))
                hmParams.setSort(HeatMapParameters.Sort.NONE);
            if(params.getDefaultSortMethod().equalsIgnoreCase(HeatMapParameters.sort_column)){
                hmParams.setSort(HeatMapParameters.Sort.COLUMN);
                hmParams.setSortIndex(0);
            }
        }

        HashSet<String> all_ranks = map.getAllRankNames();
        if(hmParams.getSort() == HeatMapParameters.Sort.RANK){
            for(Iterator<String> j = all_ranks.iterator(); j.hasNext(); ){
                String ranks_name = j.next().toString();
                if(ranks_name.equalsIgnoreCase(hmParams.getRankFileIndex()))
                    ranks = map.getRanksByName(ranks_name);
            }

            if(ranks == null)
               throw new IllegalThreadStateException("invalid sort index for rank files.");

        }
        else if((hmParams.getSort() == HeatMapParameters.Sort.COLUMN) || (hmParams.getSort() == HeatMapParameters.Sort.NONE) ){
            ranks = new Ranking() ;

            for(Iterator<Integer> i = expressionSet.keySet().iterator();i.hasNext();){
                    Integer key = i.next();
                    Rank temp = new Rank(((GeneExpression)expressionSet.get(key)).getName(),0.0,0);
                    ranks.addRank(key, temp);
                    //ranks..put(key,temp);
            }

            if(hmParams.getSort() == HeatMapParameters.Sort.COLUMN)
                hmParams.setSortbycolumn_event_triggered(true);
        }
        else if(hmParams.getSort() == HeatMapParameters.Sort.CLUSTER){
            ranks = getRanksByClustering();
        }
        return ranks;
    }

    /**
     * Hierarchical clusters the current expression set using pearson correlation and generates ranks
     * based on the the clustering output.
     *
     * @return set of ranks based on the hierarchical clustering of the current expression set.
     */
    private Ranking getRanksByClustering(){

        Ranking ranks = null;

        //The number of conditions includes the name and description
        //compute the number of data columns we have.  If there is only one data
        //column we can not cluster data
        int numdatacolumns = 0;
        int numdatacolumns2 = 0;

        int set1_size = 0;
        int set2_size = 0;
        if(currentExpressionSet != null)
            set1_size = currentExpressionSet.keySet().size();
        if(currentExpressionSet2 != null)
            set2_size = currentExpressionSet2.keySet().size();


        boolean cluster = true;

        if(numConditions > 0){
            numdatacolumns = numConditions - 2;
        }

        if(numConditions2 > 0){
            numdatacolumns2 = numConditions2 - 2;
        }
        //only create a ranking if there are genes in the expression set and there
        //is more than one column of data
        if(((set1_size > 1) || (set2_size >1)) && ((numdatacolumns + numdatacolumns2) > 1)){

            //check to see how many genes there are, if there are more than 1000 issue warning that
            //clustering will take a long time and give the user the option to abandon the clustering
            int hieracical_clustering_threshold = Integer.parseInt(CytoscapeInit.getProperties().getProperty("EnrichmentMap.hieracical_clustering_threshold", "1000"));
            if((set1_size > hieracical_clustering_threshold)
            || (set2_size > hieracical_clustering_threshold)) {
                int answer = JOptionPane.showConfirmDialog(Cytoscape.getDesktop(),
			                                      " The combination of these gene sets contain "
                                                  + currentExpressionSet.keySet().size() + " And " + currentExpressionSet2.keySet().size()
			                                      + " genes and "
			                                      + "\nClustering a set this size may take several "
			                                      + "minutes.\n" + "Do you wish to proceed with the clustering?"
			                                      + "\n\n(Choosing 'No' will switch the heatmap-sorting to 'No sort'.)",
			                                      "Cluster large set of genes",
			                                      JOptionPane.YES_NO_OPTION);
                if(answer == JOptionPane.NO_OPTION) {
                    cluster = false;
                    hmParams.changeSortComboBoxToNoSort();
                }
            }


            if((cluster)/*&&(!params.isTwoDistinctExpressionSets())*/){

                try{
                    //hmParams.setSortbyHC(true);
                    hmParams.setSort(HeatMapParameters.Sort.CLUSTER);

                    //create an array-list of the expression subset.
                    List<Double[]> clustering_expressionset = new ArrayList<Double[]>() ;
                    ArrayList<Integer> labels = new ArrayList<Integer>();
                    int j = 0;

                    /* Need to take into account all the different combinations of data when we are dealing with 2
                    expression files that don't match (as created with two different species, but can also happen if two
                     different platforms are used)
                     */

                    //if the two data sets have the same number genes we can cluster them together
                    if( set1_size == set2_size && set1_size != 0){

                        //go through the expression-set hashmap and add the key to the labels and add the expression to the clustering set
                        for(Iterator<Integer> i = currentExpressionSet.keySet().iterator();i.hasNext();){
                            Integer key = i.next();

                            Double[] x = ((GeneExpression)currentExpressionSet.get(key)).getExpression();
                            Double[] z;
                            if(params.isData2() && map.getDataset(EnrichmentMap.DATASET2).getExpressionSets() != null && currentExpressionSet2.containsKey(key)
                            		&& !map.getDataset(EnrichmentMap.DATASET1).getExpressionSets().getFilename().equalsIgnoreCase(map.getDataset(EnrichmentMap.DATASET2).getExpressionSets().getFilename())){
                                Double[] y = ((GeneExpression)currentExpressionSet2.get(key)).getExpression();
                                z = new Double[x.length + y.length];
                                System.arraycopy(x,0,z,0,x.length);
                                System.arraycopy(y,0,z,x.length,y.length);

                            }
                            else{
                                z = x;
                            }

                            //add the expression-set
                            clustering_expressionset.add(j, z);

                            //add the key to the labels
                            labels.add(j,key);

                            j++;
                        }
                    }
                    //if they are both non zero we need to make sure to include all the genes
                    else if( set1_size> 0 && set2_size> 0){

                         Double[] dummyexpression1 = new Double[numdatacolumns];
                         Double[] dummyexpression2 = new Double[numdatacolumns2];

                         for(int k = 0;k<numdatacolumns;k++)
                             dummyexpression1[k] = /*Double.NaN*/0.0;
                        for(int k = 0;k<numdatacolumns2;k++)
                             dummyexpression2[k] = /*Double.NaN*/0.0;

                        //go through the expression-set hashmap and add the key to the labels and add the expression to the clustering set
                        for(Iterator<Integer> i = currentExpressionSet.keySet().iterator();i.hasNext();){
                            Integer key = i.next();

                            Double[] x = ((GeneExpression)currentExpressionSet.get(key)).getExpression();
                            Double[] z;
                            if(params.isData2() && map.getDataset(EnrichmentMap.DATASET2).getExpressionSets() != null && currentExpressionSet2.containsKey(key) && !map.getDataset(EnrichmentMap.DATASET1).getExpressionSets().getFilename().equalsIgnoreCase(map.getDataset(EnrichmentMap.DATASET2).getExpressionSets().getFilename())){
                                Double[] y = ((GeneExpression)currentExpressionSet2.get(key)).getExpression();
                                z = new Double[x.length + y.length];
                                System.arraycopy(x,0,z,0,x.length);
                                System.arraycopy(y,0,z,x.length,y.length);

                            }
                            else{

                                //add a dummy value for the missing data
                                z = new Double[x.length + dummyexpression2.length];
                                System.arraycopy(x,0,z,0,x.length);
                                System.arraycopy(dummyexpression2,0,z,x.length,dummyexpression2.length);
                            }

                            //add the expression-set
                            clustering_expressionset.add(j, z);

                            //add the key to the labels
                            labels.add(j,key);

                            j++;
                        }
                        //go through the expression-set hashmap and add the key to the labels and add the expression to the clustering set
                        for(Iterator<Integer> i = currentExpressionSet2.keySet().iterator();i.hasNext();){
                            Integer key = i.next();

                            Double[] y = ((GeneExpression)currentExpressionSet2.get(key)).getExpression();
                            Double[] z;
                            if(currentExpressionSet.containsKey(key)){
                                Double[] x = ((GeneExpression)currentExpressionSet.get(key)).getExpression();
                                z = new Double[x.length + y.length];
                                System.arraycopy(x,0,z,0,x.length);
                                System.arraycopy(y,0,z,x.length,y.length);

                            }
                            else{

                                //add a dummy value for the missing data
                                z = new Double[y.length + dummyexpression1.length];
                                System.arraycopy(dummyexpression1,0,z,0,dummyexpression1.length);
                                System.arraycopy(y,0,z,dummyexpression1.length,y.length);
                            }

                            //add the expression-set
                            clustering_expressionset.add(j, z);

                            //add the key to the labels
                            labels.add(j,key);

                            j++;
                        }

                    }
                    //if one of the sets is zero
                    else if((set1_size> 0) && (set2_size == 0)){

                        Double[] dummyexpression1 = new Double[numdatacolumns];
                         Double[] dummyexpression2 = new Double[numdatacolumns2];

                         for(int k = 0;k<numdatacolumns;k++)
                             dummyexpression1[k] = /*Double.NaN*/0.0;
                        for(int k = 0;k<numdatacolumns2;k++)
                             dummyexpression2[k] = /*Double.NaN*/0.0;

                        //go through the expression-set hashmap and add the key to the labels and add the expression to the clustering set
                        for(Iterator<Integer> i = currentExpressionSet.keySet().iterator();i.hasNext();){
                            Integer key = i.next();

                            Double[] x = ((GeneExpression)currentExpressionSet.get(key)).getExpression();
                            Double[] z;
                            if(params.isData2() && map.getDataset(EnrichmentMap.DATASET2).getExpressionSets() != null && currentExpressionSet2.containsKey(key) && !map.getDataset(EnrichmentMap.DATASET1).getExpressionSets().getFilename().equalsIgnoreCase(map.getDataset(EnrichmentMap.DATASET2).getExpressionSets().getFilename())){
                                Double[] y = ((GeneExpression)currentExpressionSet2.get(key)).getExpression();
                                z = new Double[x.length + y.length];
                                System.arraycopy(x,0,z,0,x.length);
                                System.arraycopy(y,0,z,x.length,y.length);

                            }
                            else{
                                //add a dummy value for the missing data
                                z = new Double[x.length + dummyexpression2.length];
                                System.arraycopy(x,0,z,0,x.length);
                                System.arraycopy(dummyexpression2,0,z,x.length,dummyexpression2.length);
                            }

                            //add the expression-set
                            clustering_expressionset.add(j, z);

                            //add the key to the labels
                            labels.add(j,key);

                            j++;
                        }
                    }
                    else if((set2_size> 0)&& (set1_size == 0)){

                        Double[] dummyexpression1 = new Double[numdatacolumns];
                         Double[] dummyexpression2 = new Double[numdatacolumns2];

                         for(int k = 0;k<numdatacolumns;k++)
                             dummyexpression1[k] = /*Double.NaN*/0.0;
                        for(int k = 0;k<numdatacolumns2;k++)
                             dummyexpression2[k] = /*Double.NaN*/0.0;

                        //go through the expression-set hashmap and add the key to the labels and add the expression to the clustering set
                        for(Iterator<Integer> i = currentExpressionSet2.keySet().iterator();i.hasNext();){
                            Integer key = i.next();

                            Double[] y = ((GeneExpression)currentExpressionSet2.get(key)).getExpression();
                            Double[] z;
                            if(currentExpressionSet.containsKey(key)){
                                Double[] x = ((GeneExpression)currentExpressionSet.get(key)).getExpression();
                                z = new Double[x.length + y.length];
                                System.arraycopy(x,0,z,0,x.length);
                                System.arraycopy(y,0,z,x.length,y.length);

                            }
                            else{
                                //add a dummy value for the missing data
                                z = new Double[y.length + dummyexpression1.length];
                                System.arraycopy(dummyexpression1,0,z,0,dummyexpression1.length);
                                System.arraycopy(y,0,z,dummyexpression1.length,y.length);
                            }

                            //add the expression-set
                            clustering_expressionset.add(j, z);

                            //add the key to the labels
                            labels.add(j,key);

                            j++;
                        }
                    }

                    //create a distance matrix the size of the expression set
                    DistanceMatrix distanceMatrix;
                    if(set1_size == set2_size)
                        distanceMatrix = new DistanceMatrix(currentExpressionSet.keySet().size());
                    else if(set1_size == 0)
                        distanceMatrix = new DistanceMatrix(currentExpressionSet2.keySet().size());
                    else if(set2_size == 0)
                        distanceMatrix = new DistanceMatrix(currentExpressionSet.keySet().size());
                    else
                        distanceMatrix = new DistanceMatrix(currentExpressionSet2.keySet().size() + currentExpressionSet.keySet().size());
                    //calculate the distance metric based on the user choice of distance metric
                    if(params.getDefaultDistanceMetric().equalsIgnoreCase(HeatMapParameters.pearson_correlation)){
                        //if the user choice is pearson still have to check to make sure
                        //there are no errors with pearson calculation.  If can't calculate pearson
                        //then it calculates the cosine.
                        try{
                            distanceMatrix.calcDistances(clustering_expressionset, new PearsonCorrelation());
                        }catch(RuntimeException e){
                            try{
                                if(!shownPearsonErrorMsg){
                                    JOptionPane.showMessageDialog(Cytoscape.getDesktop(),"Unable to compute Pearson Correlation for this expression Set.\n  Cosine distance used for this set instead.\n To switch distance metric used for all hierarchical clustering \nPlease change setting under Advance Preferences in the Results Panel.");
                                    shownPearsonErrorMsg = true;
                                }
                                distanceMatrix.calcDistances(clustering_expressionset, new CosineDistance());
                            }catch(RuntimeException ex){
                                distanceMatrix.calcDistances(clustering_expressionset, new EuclideanDistance());
                            }
                        }
                    }
                    else if (params.getDefaultDistanceMetric().equalsIgnoreCase(HeatMapParameters.cosine))
                             distanceMatrix.calcDistances(clustering_expressionset, new CosineDistance());
                    else if (params.getDefaultDistanceMetric().equalsIgnoreCase(HeatMapParameters.euclidean))
                             distanceMatrix.calcDistances(clustering_expressionset, new EuclideanDistance());


                    distanceMatrix.setLabels(labels);

                    //cluster
                    AvgLinkHierarchicalClustering cluster_result = new AvgLinkHierarchicalClustering(distanceMatrix);

                    //check to see if there more than 1000 genes, if there are use eisen ordering otherwise use bar-joseph
                    if((set1_size + set2_size) > 1000)
                        cluster_result.setOptimalLeafOrdering(false);
                    else
                        cluster_result.setOptimalLeafOrdering(true);
                    cluster_result.run();

                    int[] order = cluster_result.getLeafOrder();
                    ranks = new Ranking();
                    for(int i =0;i< order.length;i++){
                        //get the label
                        Integer label =  (Integer)labels.get(order[i]);

                        GeneExpression exp;
                        //check for the expression in expression set 1
                        if(currentExpressionSet.containsKey(label))
                            exp = (GeneExpression)currentExpressionSet.get(label);
                        else if(currentExpressionSet2.containsKey(label))
                            exp = (GeneExpression)currentExpressionSet2.get(label);
                        else
                            exp = null;

                        Rank temp = new Rank(exp.getName(),0.0,i);
                        ranks.addRank(label,temp);
                    }
                }catch(OutOfMemoryError e){
                    JOptionPane.showMessageDialog(Cytoscape.getDesktop(), "Unable to complete clustering of genes due to insufficient memory.","Out of memory",JOptionPane.INFORMATION_MESSAGE);
                    cluster = false;
                }
            }
            else if(cluster && params.isTwoDistinctExpressionSets()){
             cluster = false;
               hmParams.setSort(HeatMapParameters.Sort.NONE);
            }
        }


       if((currentExpressionSet.keySet().size() == 1) || ((numdatacolumns + numdatacolumns2) <= 1) || !(cluster)){
           //hmParams.setNoSort(true);
           hmParams.setSort(HeatMapParameters.Sort.NONE);
           ranks = new Ranking();
            for(Iterator<Integer> i = currentExpressionSet.keySet().iterator();i.hasNext();){
                Integer key = i.next();
                Rank temp = new Rank(((GeneExpression)currentExpressionSet.get(key)).getName(),0.0,0);
                ranks.addRank(key,temp);
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

    private int getTopRank(){
        int topRank = 0;
        //just having one node is not suffiecient reason for dispalying the the leading edge make sure we are sorted by rank and that there is a rank file.
        //The issue is that the expression subset is only updated on node selection and that is where we determine if it is
        //a selection qualified for leadingedge annotation but the user can change the sorting option without updating the
        //selection.
        if(displayLeadingEdge && (map.getDataset(EnrichmentMap.DATASET1).getExpressionSets().haveRanks() || map.getDataset(EnrichmentMap.DATASET2).getExpressionSets().haveRanks())  
        		&& (hmParams.getSort() == HeatMapParameters.Sort.RANK || params.getDefaultSortMethod().equalsIgnoreCase(hmParams.getSort().toString()))){
            //get the rank under (or over) which everything should be higlighted
            if(hmParams.getRankFileIndex().equalsIgnoreCase("Dataset 1 Ranking") || hmParams.getRankFileIndex().equalsIgnoreCase("GSEARanking")){
                topRank = leadingEdgeRankAtMax1;
                if(leadingEdgeScoreAtMax1 > 0)
                    //because of the GSEA ranks are off slightly buffer the cutoff depending on whether the geneset
                    //is up or down regulated in order to get all genes in leading edge
                    topRank = topRank + 3;
            }
            else if(hmParams.getRankFileIndex().equalsIgnoreCase("Dataset 2 Ranking") || hmParams.getRankFileIndex().equalsIgnoreCase("GSEARanking")){
                topRank = leadingEdgeRankAtMax2;
                if(leadingEdgeScoreAtMax2 > 0)
                    //because of the GSEA ranks are off slightly buffer the cutoff depending on whether the geneset
                    //is up or down regulated in order to get all genes in leading edge
                    topRank = topRank + 3;
            }
        }
        return topRank;
    }

    private boolean isNegativeGS(int dataset){
        boolean isNegative = false;

        if(dataset == 1){
                if(leadingEdgeScoreAtMax1 < 0)
                    isNegative = true;
                else
                    isNegative = false;
        }else if (dataset == 2){
                if(leadingEdgeScoreAtMax2 < 0)
                    isNegative = true;
                else
                    isNegative = false;
        }
        return isNegative;
    }

    /**
     * jaccard or overlap radio button action listener
     *
     * @param evt
     */
    private void selectColorOnOffActionPerformed(java.awt.event.ActionEvent evt) {
        if(evt.getActionCommand().equalsIgnoreCase("on")){
        		this.hmParams.setColoroff(false);
        		colorOn.setSelected(true);
        		colorOff.setSelected(false);
        	}
        if(evt.getActionCommand().equalsIgnoreCase("off")){
    			this.hmParams.setColoroff(true);
    			colorOn.setSelected(false);
    			colorOff.setSelected(true);
        }
        
        this.updatePanel();
        this.revalidate();
    }
    
    
    //Getters and Setters
    Object[][] getExpValue() {
        return expValue;
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


    private void setExpValue(Object[][] expValue){
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

    public JTableHeader getTableHeader(){
        return tableHdr;
    }

    public JTable getjTable1() {
        return jTable1;
    }

    public JPanel getNorthPanel(){
        return northPanel;
    }

}


