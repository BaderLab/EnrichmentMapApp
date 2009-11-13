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

// $Id$
// $LastChangedDate$
// $LastChangedRevision$
// $LastChangedBy$
// $HeadURL$

package org.baderlab.csplugins.enrichmentmap;

import javax.swing.*;
import javax.swing.border.TitledBorder;

import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeMap;
import java.net.URL;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;

import org.mskcc.colorgradient.* ;

import cytoscape.Cytoscape;


/**
 * Created by
 * User: risserlin
 * Date: Feb 11, 2009
 * Time: 12:23:01 PM
 * <p>
 * Parameters specific to the heat map functioning
 */
public class HeatMapParameters {

    private org.mskcc.colorgradient.ColorGradientRange range;
    private org.mskcc.colorgradient.ColorGradientTheme theme;

    //data transformation options (row normalized, as if or log transformed)
    public static enum Transformation{
        ROWNORM, ASIS, LOGTRANSFORM
    }

    //there are 5 sorting type, either by rank file or by a specific column, clustering, no order or default
    public  static enum Sort{
        RANK, COLUMN, CLUSTER, NONE, DEFAULT
    }

    Sort sort;
    Transformation transformation;

    //name of column currently sorted by
    private String sortbycolumnName;

    //tag to indicate if this was a column click or if this was a normalization click.
    private boolean sortbycolumn_event_triggered = false;

    //store the index of the column that we are sorting by
    private int sortIndex = 0;

    //store the name of the rank file sorted by
    private String rankFileIndex;

    //store the state (ascending or descending) of each column in the table as well as each of the rank files.
    private boolean[] ascending;

    //Up and down sort button
    final static int Ascending = 0, Descending= 1; // image States
    private ImageIcon[] iconArrow = createExpandAndCollapseIcon();

    //minimum and maximum expression values used to create colour mapper
    private double minExpression;
    private double maxExpression;
    private double minExpression_rownorm;
    private double maxExpression_rownorm;

    private JPanel heatmapOptions;
    private JPanel RankOptions;
    private JComboBox hmOptionComboBox;
    private JComboBox rankOptionComboBox;

    //pointer to panels containing the heatmaps.
    private HeatMapPanel edgeOverlapPanel;
    private HeatMapPanel nodeOverlapPanel;

    public static String sort_hierarchical_cluster = "Hierarchical Cluster";
    public static String sort_rank = "Ranks";
    public static String sort_column = "Columns";
    public static String sort_none = "No Sort";

    /**
     * Class constructor -
     *
     * @param edgeOverlapPanel - heatmap for edge genes overlaps
     * @param nodeOverlapPanel - heatmap for node genes unions
     */
    public HeatMapParameters(HeatMapPanel edgeOverlapPanel, HeatMapPanel nodeOverlapPanel) {
        this.edgeOverlapPanel = edgeOverlapPanel;
        this.nodeOverlapPanel = nodeOverlapPanel;
        sort = Sort.DEFAULT;
        transformation = Transformation.ASIS;
    }

    /**
     * Initialize the the color gradients based on the expression matrix
     * associated with this set of heatmap panels (ie. both node and edge heatmap panels)
     *
     * @param expression - expression matrix used for this heatmap set
     */
    public void initColorGradients(GeneExpressionMatrix expression){

        minExpression = expression.getMinExpression();
        maxExpression = expression.getMaxExpression();
        minExpression_rownorm = expression.getMinExpression(expression.getExpressionMatrix_rowNormalized());
        maxExpression_rownorm = expression.getMaxExpression(expression.getExpressionMatrix_rowNormalized());

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

    /**
     * Reset color gradients based on a change in the data transformation.
     */
    public void ResetColorGradient(){
          double min;
          double max;
          double median;

          switch(transformation){
              case ROWNORM:
                min = minExpression_rownorm;
                max = maxExpression_rownorm;

                //if both row normalization values are zero, can't perform row normalization
                //issue warning
                //This happens when there is only one data column in the dataset (or if it is rank file)
                if((min == 0) && (max == 0)){
                   JOptionPane.showMessageDialog(Cytoscape.getDesktop(),"Row normalization does not work with only one data column per dataset.","Row normalization error",JOptionPane.WARNING_MESSAGE);
                }
                max = Math.max(Math.abs(min),max);
                break;

               case LOGTRANSFORM:
                    min = Math.log1p(minExpression);
                    max = Math.log1p(maxExpression) ;
                    max = Math.max(Math.abs(min),max);
                   break;

               case ASIS:
                default:
                    min = minExpression;
                    max = Math.max(Math.abs(minExpression), maxExpression);
                    break;
          }

          median = max/2;
          if(min >= 0){
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

    /**
     * method to create Sort by combo box
     *
     * @param params - enrichment map parameters of current map
     * @return - panel with the sort by combo box
     */
    public JPanel createSortOptionsPanel(EnrichmentMapParameters params){
    	 TitledBorder RankBorder = BorderFactory.createTitledBorder("Sorting");
        HashMap<String, HashMap<Integer, Ranking>> ranks = params.getRanks();
         RankBorder.setTitleJustification(TitledBorder.LEFT);
    	RankOptions 		= new JPanel();
    	rankOptionComboBox	= new JComboBox();

        //create a panel for the combobox and button
        JPanel ComboButton = new JPanel();

        rankOptionComboBox.addItem(sort_hierarchical_cluster);

        //create the rank options based on what we have in the set of ranks
        //Go through the ranks hashmap and insert each ranking as an option
        if(ranks != null){
            //convert the ranks into a treeset so that they are ordered
            TreeMap ranks_ordered = new TreeMap();
            ranks_ordered.putAll(ranks);
            for(Iterator j = ranks_ordered.keySet().iterator(); j.hasNext(); ){
                String ranks_name = j.next().toString();
                rankOptionComboBox.addItem(ranks_name);
            }
        }

        rankOptionComboBox.addItem(sort_none);

        switch(sort){
            case DEFAULT:
                rankOptionComboBox.setSelectedItem(params.getDefaultSortMethod());
                if(params.getDefaultSortMethod().equalsIgnoreCase(sort_rank)){
                    sort = Sort.RANK;
                    if(ranks != null)
                        rankFileIndex = ranks.keySet().iterator().next();
                    else{
                        rankOptionComboBox.setSelectedItem(sort_none);
                        sort = Sort.NONE;
                    }
                }
                else if(params.getDefaultSortMethod().equalsIgnoreCase(sort_none))
                    sort = Sort.NONE;
                else if(params.getDefaultSortMethod().equalsIgnoreCase(sort_hierarchical_cluster))
                    sort = Sort.CLUSTER;
                break;

            case CLUSTER:
               rankOptionComboBox.setSelectedItem(sort_hierarchical_cluster);
                break;

            case NONE:
                rankOptionComboBox.setSelectedItem(sort_none);
                break;

            case RANK:
                for(Iterator j = ranks.keySet().iterator(); j.hasNext(); ){
                    String ranks_name = j.next().toString();
                    if(ranks_name.equalsIgnoreCase(rankFileIndex))
                        rankOptionComboBox.setSelectedItem(ranks_name);
                }
                break;

            case COLUMN:
                 rankOptionComboBox.addItem(sort_column + ":" + sortbycolumnName);
                rankOptionComboBox.setSelectedItem(sort_column + ":" + sortbycolumnName);
                break;

        }

        // set the selection in the rank combo box
        //if this is the initial creation then set the rank to the default
        //add the option to add another rank file
        rankOptionComboBox.addItem("Add Rankings ... ");

        ComboButton.add(rankOptionComboBox);

         //include the button only if we are sorting by column or ranks
        if(sort == Sort.RANK || sort == Sort.COLUMN){
            JButton arrow;
            if(isAscending(sortIndex))
                 arrow = createArrowButton(Ascending);
            else
                arrow = createArrowButton(Descending);
            ComboButton.add(arrow);
            arrow.addActionListener(new ChangeSortAction(arrow));
        }

        rankOptionComboBox.addActionListener(new HeatMapActionListener(edgeOverlapPanel, nodeOverlapPanel,rankOptionComboBox,this, params));

        ComboButton.revalidate();

        RankOptions.add(ComboButton);
        RankOptions.setBorder(RankBorder);
        RankOptions.revalidate();

        return RankOptions;
    }


    /**
     * Returns a button with an arrow icon and a collapse/expand action listener.
     *
     * @return button Button which is used in the titled border component
     */
    private JButton createArrowButton (int direction) {
        JButton button = new JButton(iconArrow[direction]);
        button.setBorder(BorderFactory.createEmptyBorder(0,1,5,1));
        button.setVerticalTextPosition(AbstractButton.CENTER);
        button.setHorizontalTextPosition(AbstractButton.LEFT);
        button.setMargin(new Insets(0,0,3,0));

        //We want to use the same font as those in the titled border font
        Font font = BorderFactory.createTitledBorder("Sample").getTitleFont();
        Color color = BorderFactory.createTitledBorder("Sample").getTitleColor();
        button.setFont(font);
        button.setForeground(color);
        button.setFocusable(false);
        button.setContentAreaFilled(false);

        return button;
    }

    private ImageIcon[] createExpandAndCollapseIcon () {
        ImageIcon[] iconArrow = new ImageIcon[2];
        URL iconURL;
        //                         Oliver at 26/06/2009:  relative path works for me,
        //                         maybe need to change to org/baderlab/csplugins/enrichmentmap/resources/arrow_collapsed.gif
        iconURL = Enrichment_Map_Plugin.class.getResource("resources/arrow_up.gif");
        if (iconURL != null) {
            iconArrow[Ascending] = new ImageIcon(iconURL);
        }
        iconURL = Enrichment_Map_Plugin.class.getResource("resources/arrow_down.gif");
        if (iconURL != null) {
            iconArrow[Descending] = new ImageIcon(iconURL);
        }
        return iconArrow;
    }

    /**
     * Handles expanding and collapsing of extra content on the user's click of the titledBorder component.
     */
    private class ChangeSortAction extends AbstractAction implements ActionListener, ItemListener {
        private JButton arrow;

        private ChangeSortAction(JButton arrow){
             this.arrow = arrow;

        }

        public void actionPerformed(ActionEvent e) {
             flipAscending(getSortIndex());

             if(isAscending(getSortIndex()))
                arrow.setIcon(iconArrow[Ascending]);
             else
                arrow.setIcon(iconArrow[Descending]);

            edgeOverlapPanel.updatePanel();
            nodeOverlapPanel.updatePanel();

        }
        public void itemStateChanged(ItemEvent e) {
             flipAscending(getSortIndex());
        }
    }

   /**
     * method to create Data Transformations Options combo box
     *
     * @param params - enrichment map parameters of current map
     * @return - panel with the Data Transformations Options combo box
     */
    public JPanel createDataTransformationOptionsPanel(EnrichmentMapParameters params){
    	 TitledBorder HMBorder = BorderFactory.createTitledBorder("Normalization");
         HMBorder.setTitleJustification(TitledBorder.LEFT);
    	heatmapOptions   = new JPanel();
    	hmOptionComboBox = new JComboBox();
        hmOptionComboBox.addItem("Data As Is");
        hmOptionComboBox.addItem("Row Normalize Data");
        hmOptionComboBox.addItem("Log Transform Data");

       switch(transformation){
           case ASIS: hmOptionComboBox.setSelectedItem("Data As Is");
               break;
           case ROWNORM: hmOptionComboBox.setSelectedItem("Row Normalize Data");
               break;
           case LOGTRANSFORM: hmOptionComboBox.setSelectedItem("Log Transform Data");
               break;
       }

        hmOptionComboBox.addActionListener(new HeatMapActionListener(edgeOverlapPanel, nodeOverlapPanel,hmOptionComboBox,this, params));
        heatmapOptions.add(hmOptionComboBox);
        heatmapOptions.setBorder(HMBorder);
        return heatmapOptions;
    }

    /**
     * If data is sorted by a column update the sort by combo box to contain the column name sorted by
     */
    public void changeSortComboBoxToColumnSorted(){
          rankOptionComboBox.addItem(sort_column + ":" + sortbycolumnName);
          rankOptionComboBox.setSelectedItem(sort_column + ":" + sortbycolumnName);

      }

    /**
     * Set the Sort Option ComboBox back to "No Sort".
     * e.g. if too many genes are selected and the user chooses to abort the hieracical clustering.
     */
    public void changeSortComboBoxToNoSort(){
        rankOptionComboBox.setSelectedItem(HeatMapParameters.sort_none);
    }

    //Getters and Setters.
     public HeatMapPanel getEdgeOverlapPanel(){
    	return edgeOverlapPanel;
    }

    public HeatMapPanel getNodeOverlapPanel(){
    	return nodeOverlapPanel;
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

    public Transformation getTransformation() {
        return transformation;
    }

    public void setTransformation(Transformation transformation) {
        this.transformation = transformation;
    }

    public Sort getSort() {
        return sort;
    }

    public void setSort(Sort sort) {
        this.sort = sort;
    }

    public int getSortIndex() {
        return sortIndex;
    }

    public void setSortIndex(int sortIndex) {
        this.sortIndex = sortIndex;
    }

    public boolean isSortbycolumn_event_triggered() {
        return sortbycolumn_event_triggered;
    }

    public void setSortbycolumn_event_triggered(boolean sortbycolumn_event_triggered) {
        this.sortbycolumn_event_triggered = sortbycolumn_event_triggered;
    }

    public String getSortbycolumnName() {
        return sortbycolumnName;
    }

    public void setSortbycolumnName(String sortbycolumnName) {
        this.sortbycolumnName = sortbycolumnName;
    }

    public String getRankFileIndex() {
        return rankFileIndex;
    }

    public void setRankFileIndex(String rankFileIndex) {
        this.rankFileIndex = rankFileIndex;
    }

    public boolean[] getAscending() {
        return ascending;
    }

    public void setAscending(boolean[] ascending) {
        this.ascending = ascending;
    }

    public boolean isAscending(int index){
        if(index == -1)
            return true;
        return ascending[index];
    }

    public void flipAscending(int index){
        ascending[index] = !ascending[index];
    }

}
