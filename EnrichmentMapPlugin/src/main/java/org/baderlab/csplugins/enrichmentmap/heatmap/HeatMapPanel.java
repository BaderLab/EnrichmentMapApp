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

package org.baderlab.csplugins.enrichmentmap.heatmap;


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.TitledBorder;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;

import org.baderlab.csplugins.enrichmentmap.AfterInjection;
import org.baderlab.csplugins.enrichmentmap.heatmap.HeatMapParameters.Sort;
import org.baderlab.csplugins.enrichmentmap.heatmap.task.HeatMapHierarchicalClusterTaskFactory;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMapManager;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMapParameters;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentResult;
import org.baderlab.csplugins.enrichmentmap.model.GSEAResult;
import org.baderlab.csplugins.enrichmentmap.model.GeneExpression;
import org.baderlab.csplugins.enrichmentmap.model.GeneExpressionMatrix;
import org.baderlab.csplugins.enrichmentmap.model.Rank;
import org.baderlab.csplugins.enrichmentmap.model.Ranking;
import org.baderlab.csplugins.enrichmentmap.model.SignificantGene;
import org.baderlab.csplugins.enrichmentmap.parsers.DetermineEnrichmentResultFileReader;
import org.baderlab.csplugins.enrichmentmap.style.EnrichmentMapVisualStyle;
import org.baderlab.csplugins.enrichmentmap.task.ResultTaskObserver;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.io.util.StreamUtil;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.util.swing.FileChooserFilter;
import org.cytoscape.util.swing.FileUtil;
import org.cytoscape.util.swing.OpenBrowser;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.swing.DialogTaskManager;
import org.mskcc.colorgradient.ColorGradientRange;
import org.mskcc.colorgradient.ColorGradientTheme;
import org.mskcc.colorgradient.ColorGradientWidget;

import com.google.inject.Inject;



/**
 * Created by
 * User: risserlin
 * Date: Jan 30, 2009
 * Time: 9:15:32 AM
 * <p>
 * Creates a Heat map Panel - (heat map can consists of either one or two expression files depending on what
 * was supplied by the user)
 */
// Note: Not a singleton because there are two panels.
public class HeatMapPanel extends JPanel implements CytoPanelComponent {

	@Inject private EnrichmentMapManager emManager;
	
	@Inject private CySwingApplication application;
	@Inject private CyApplicationManager applicationManager;
	@Inject private FileUtil fileUtil;
	@Inject private OpenBrowser openBrowser;
	@Inject private DialogTaskManager dialogTaskMonitor;
	@Inject private StreamUtil streamUtil;

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

	//private JRadioButton colorOn;
	private JCheckBox showValues;

	private int numConditions = 0;
	private int numConditions2 = 0;

	private String[] hRow1;
	private String[] hRow2;
	private String[] rowGeneName;
	private int[] rowLength;
	private ColorGradientTheme[] rowTheme;
	private ColorGradientRange[] rowRange;

	private int[] halfRow1Length;
	private int[] halfRow2Length;
	private ColorGradientTheme[] themeHalfRow1;
	private ColorGradientTheme[] themeHalfRow2;
	private ColorGradientRange[] rangeHalfRow1;
	private ColorGradientRange[] rangeHalfRow2;
	//private boolean[] isHalfRow1;
	//private boolean[] isHalfRow2;
	private final Insets insets = new Insets(0, 0, 0, 0);

	//current subset of expression data from dataset 1 expression set
	private HashMap<Integer, GeneExpression> currentExpressionSet;
	//current subset of expression data from dataset 2 expression set
	private HashMap<Integer, GeneExpression> currentExpressionSet2;

	private Ranking ranks;

	private boolean node = true;

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
	private boolean OnlyLeadingEdge = false;
	private double leadingEdgeScoreAtMax1 = 0;
	private double leadingEdgeScoreAtMax2 = 0;
	private int leadingEdgeRankAtMax1 = 0;
	private int leadingEdgeRankAtMax2 = 0;

	//objects needed access to in order to print to pdf
	//private JScrollPane jScrollPane;
	private JTable jTable1;
	private JTableHeader tableHdr;
	private JPanel northPanel;
	private JComboBox rankOptionComboBox;

	//Up and down sort button
	final static int Ascending = 0, Descending = 1; // image States
	private ImageIcon[] iconArrow = createExpandAndCollapseIcon();

	
	public HeatMapPanel setNode(boolean node) {
		this.node = node;
		return this;
	}

	@AfterInjection
	public void createContents() {
		this.setLayout(new java.awt.BorderLayout());
		
		//initialize the linkout props
		initialize_linkouts();

		//initialize pop up menu
		rightClickPopupMenu = new JPopupMenu();
	}
	
	/**
	 * Set the Heat map Panel to the variables in the given enrichment map
	 * parameters set
	 *
	 * @param params - enrichment map parameters to reset the heat map to.
	 */
	public void resetVariables(EnrichmentMap map) {
		this.map = map;
		this.params = map.getParams();
		this.ranks = null;

		if (params.isData() || params.isData2()) {
			GeneExpressionMatrix expression = map.getDataset(EnrichmentMap.DATASET1).getExpressionSets();
			numConditions = expression.getNumConditions();
			columnNames = expression.getColumnNames();

			phenotypes = expression.getPhenotypes();

			this.Dataset1phenotype1 = params.getFiles().get(EnrichmentMap.DATASET1).getPhenotype1();
			this.Dataset1phenotype2 = params.getFiles().get(EnrichmentMap.DATASET1).getPhenotype2();

			long suid = map.getParams().getNetworkID();
			hmParams = emManager.getHeatMapParameters(suid);
			boolean[] ascending;
			if (expression.getRanks() != null) {
				ascending = new boolean[columnNames.length + map.getAllRankNames().size()];
				//set the rank files to ascending
				for (int k = ascending.length; k > (ascending.length - map.getAllRankNames().size()); k--)
					ascending[k - 1] = true;
			} else
				ascending = new boolean[columnNames.length];

			if (hmParams != null)
				hmParams.setAscending(ascending);

			displayLeadingEdge = false;
			leadingEdgeScoreAtMax1 = 0.0;
			leadingEdgeScoreAtMax2 = 0.0;
			leadingEdgeRankAtMax1 = 0;
			leadingEdgeRankAtMax2 = 0;

			//get the current expressionSet
			if (node)
				initializeLeadingEdge(params);

			if (params.isData2() && map.getDataset(EnrichmentMap.DATASET2).getExpressionSets() != null && !map.getDataset(EnrichmentMap.DATASET1)
					.getExpressionSets().getFilename().equalsIgnoreCase(map.getDataset(EnrichmentMap.DATASET2).getExpressionSets().getFilename())) {

				GeneExpressionMatrix expression2 = map.getDataset(EnrichmentMap.DATASET2).getExpressionSets();

				numConditions2 = expression2.getNumConditions();
				columnNames2 = expression2.getColumnNames();

				//additional ranks
				int additional_ranks = (map.getAllRankNames() != null) ? map.getAllRankNames().size() : 0;

				boolean[] ascending2 = new boolean[columnNames.length + (columnNames2.length - 2) + additional_ranks];
				for (int k = ascending2.length; k > ascending2.length - map.getAllRankNames().size(); k--)
					ascending2[k - 1] = true;
				if (hmParams != null)
					hmParams.setAscending(ascending2);//we don't have to repeat the name and description columns

			}

			//if there are two expression sets, regardless if they are the same get the phenotypes of the second file.
			if (params.isData2() && map.getDataset(EnrichmentMap.DATASET2).getExpressionSets() != null) {

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
	public void updatePanel(EnrichmentMap map) {

		resetVariables(map);
		updatePanel();
	}

	/**
	 * Update the heat map panel
	 */
	public void updatePanel() {
		//EnrichmentMapParameters params = map.getParams();
		if ((currentExpressionSet != null) || (currentExpressionSet2 != null)) {

			JTable rowTable;
			String[] mergedcolumnNames = null;

			JPanel mainPanel = new JPanel();
			mainPanel.setLayout(new BorderLayout());
			TableSort sort;

			HeatMapTableModel OGT;
			Object[][] data;

			//create data subset
			if (params.isData2() && map.getDataset(EnrichmentMap.DATASET2).getExpressionSets() != null && !map.getDataset(EnrichmentMap.DATASET1)
					.getExpressionSets().getFilename().equalsIgnoreCase(map.getDataset(EnrichmentMap.DATASET2).getExpressionSets().getFilename())) {

				// used exp[][] value to store all the expression values needed to create data[][]
				expValue = createSortedMergedTableData();
				data = createSortedMergedTableData(getExpValue());

				mergedcolumnNames = new String[columnNames.length + columnNames2.length - 2];

				System.arraycopy(columnNames, 0, mergedcolumnNames, 0, columnNames.length);
				System.arraycopy(columnNames2, 2, mergedcolumnNames, columnNames.length, columnNames2.length - 2);

				//used OGT to minimize call of new JTable
				OGT = new HeatMapTableModel(mergedcolumnNames, data, expValue);

			} else {
				// used exp[][] value to store all the expression values needed to create data[][]
				expValue = createSortedTableData();
				data = createSortedTableData(getExpValue());

				OGT = new HeatMapTableModel(columnNames, data, expValue);

			}

			CellHighlightRenderer highlightCellRenderer = new CellHighlightRenderer();

			sort = new TableSort(OGT);
			jTable1 = new JTable(sort);

			//add a listener to the table
			jTable1.addMouseListener(new HeatMapTableActionListener(jTable1, OGT, rightClickPopupMenu, linkoutProps, openBrowser));

			// used for listening to columns when clicked
			TableHeader header = new TableHeader(sort, jTable1, hmParams, this);

			tableHdr = jTable1.getTableHeader();
			tableHdr.addMouseListener(header);

			//check to see if there is already a sort been defined for this table
			//if(hmParams.isSortbycolumn()){
			if (hmParams.getSort() == HeatMapParameters.Sort.COLUMN) {
				boolean ascending;
				ascending = hmParams.isAscending(hmParams.getSortIndex());

				if (hmParams.getSortIndex() >= columnNames.length)
					hmParams.setSortbycolumnName((String) columnNames2[hmParams.getSortIndex() - columnNames.length + 2]);

				else
					hmParams.setSortbycolumnName((String) columnNames[hmParams.getSortIndex()]);

				header.sortByColumn(hmParams.getSortIndex(), ascending);
			}

			//Set up renderer and editor for the Color column.
			//default column width.  If we are using coloring default should be 10.  If we are using values default should be 50
			int defaultColumnwidth = 10;
			if (this.hmParams.isShowValues()) {
				jTable1.setDefaultRenderer(ExpressionTableValue.class, new RawExpressionValueRenderer());
				defaultColumnwidth = 50;
			} else
				jTable1.setDefaultRenderer(ExpressionTableValue.class, new ColorRenderer());

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

			if (params.isData2() && map.getDataset(EnrichmentMap.DATASET2).getExpressionSets() != null && !map.getDataset(EnrichmentMap.DATASET1)
					.getExpressionSets().getFilename().equalsIgnoreCase(map.getDataset(EnrichmentMap.DATASET2).getExpressionSets().getFilename())) {
				//go through the first data set
				for (int i = 0; i < columnNames.length; i++) {
					if (i == 0 || columnNames[i].equals("Name"))
						tcModel.getColumn(i).setPreferredWidth(50);
					else if (i == 1 || columnNames[i].equals("Description"))
						tcModel.getColumn(i).setPreferredWidth(50);
					else {
						tcModel.getColumn(i).setPreferredWidth(defaultColumnwidth);
						if (phenotypes != null) {
							if (phenotypes[i - 2].equalsIgnoreCase(Dataset1phenotype1))
								tcModel.getColumn(i).setHeaderRenderer(pheno1_renderer);
							else if (phenotypes[i - 2].equalsIgnoreCase(Dataset1phenotype2))
								tcModel.getColumn(i).setHeaderRenderer(pheno2_renderer);
							else
								tcModel.getColumn(i).setHeaderRenderer(default_renderer);
						} else
							tcModel.getColumn(i).setHeaderRenderer(default_renderer);
					}
				}
				//go through the second data set
				for (int i = columnNames.length; i < (columnNames.length + columnNames2.length - 2); i++) {
					tcModel.getColumn(i).setPreferredWidth(defaultColumnwidth);
					if (phenotypes2 != null) {
						if (phenotypes2[i - columnNames.length].equalsIgnoreCase(Dataset2phenotype1))
							tcModel.getColumn(i).setHeaderRenderer(pheno1_renderer);
						else if (phenotypes2[i - columnNames.length].equalsIgnoreCase(Dataset2phenotype2))
							tcModel.getColumn(i).setHeaderRenderer(pheno2_renderer);
						else
							tcModel.getColumn(i).setHeaderRenderer(default_renderer);
					} else
						tcModel.getColumn(i).setHeaderRenderer(default_renderer);

				}
			} else {
				for (int i = 0; i < columnNames.length; i++) {
					if (i == 0 || columnNames[i].equals("Name"))
						tcModel.getColumn(i).setPreferredWidth(50);
					else if (i == 1 || columnNames[i].equals("Description"))
						tcModel.getColumn(i).setPreferredWidth(50);
					else {
						tcModel.getColumn(i).setPreferredWidth(defaultColumnwidth);
						/*
						 * If the class file is made from within GSEA but the
						 * expression file has more than one class defined in it
						 * there is no way to align the class file to the
						 * expression file. the phenotypes have two less
						 * positions because the phenotype file doesn't define
						 * the name and description columns In this case use the
						 * default renderer Ticket #225
						 */
						if (phenotypes != null && (phenotypes.length + 2) == columnNames.length) {
							if (phenotypes[i - 2].equalsIgnoreCase(Dataset1phenotype1))
								tcModel.getColumn(i).setHeaderRenderer(pheno1_renderer);
							else if (phenotypes[i - 2].equalsIgnoreCase(Dataset1phenotype2))
								tcModel.getColumn(i).setHeaderRenderer(pheno2_renderer);
							//Needed these extra lines when only have one expression file with both datasets.
							else if (phenotypes[i - 2].equalsIgnoreCase(Dataset2phenotype1))
								tcModel.getColumn(i).setHeaderRenderer(pheno1_renderer);
							else if (phenotypes[i - 2].equalsIgnoreCase(Dataset2phenotype2))
								tcModel.getColumn(i).setHeaderRenderer(pheno2_renderer);
							else
								tcModel.getColumn(i).setHeaderRenderer(default_renderer);
						} else
							tcModel.getColumn(i).setHeaderRenderer(default_renderer);
					}

				}
			}

			JScrollPane jScrollPane;
			jTable1.setColumnModel(tcModel);
			jScrollPane = new javax.swing.JScrollPane(jTable1);
			rowTable = new RowNumberTable(jTable1);
			jScrollPane.setRowHeaderView(rowTable);

			if (columnNames.length > 20)
				jTable1.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

			mainPanel.add(jScrollPane);
			mainPanel.revalidate();

			this.add(createNorthPanel(), java.awt.BorderLayout.NORTH);
			this.add(jScrollPane, java.awt.BorderLayout.CENTER);

		}
		this.revalidate();

	}

	private Object[][] createSortedTableData(Object[][] expValue2) {
		this.expValue = expValue2;
		Object[][] data;

		int[] HRow = this.getRowLength();
		ColorGradientTheme[] RowCRT = this.getRowTheme();
		ColorGradientRange[] RowCRR = this.getRowRange();
		String[] RowGene = this.getRowGeneName();

		int kValue = currentExpressionSet.size();

		data = new Object[currentExpressionSet.size()][numConditions];
		for (int k = 0; k < kValue; k++) {

			data[k][0] = expValue[k][0];
			data[k][1] = expValue[k][1];

			for (int j = 0; j < HRow[k]; j++) {
				if (numConditions == 2)
					data[k][j + 1] = new ExpressionTableValue((Double) expValue[k][j + 1],
							getColor(RowCRT[k], RowCRR[k], RowGene[k], (Double) expValue[k][j + 1]));
				else
					data[k][j + 2] = new ExpressionTableValue((Double) expValue[k][j + 2],
							getColor(RowCRT[k], RowCRR[k], RowGene[k], (Double) expValue[k][j + 2]));
			}

		}

		return data;
	}

	private Color getColor(ColorGradientTheme theme, ColorGradientRange range, String gene, Double measurement) {
		if (theme == null || range == null || measurement == null)
			return Color.GRAY;

		float rLow = theme.getMinColor().getRed() / 255, gLow = theme.getMinColor().getGreen() / 255, bLow = theme.getMinColor().getBlue() / 255;
		float rMid = theme.getCenterColor().getRed() / 255, gMid = theme.getCenterColor().getGreen() / 255, bMid = theme.getCenterColor().getBlue() / 255;
		float rHigh = theme.getMaxColor().getRed() / 255, gHigh = theme.getMaxColor().getGreen() / 255, bHigh = theme.getMaxColor().getBlue() / 255;

		double median;
		if (range.getMinValue() >= 0)
			median = (range.getMaxValue() / 2);
		else
			median = 0.0;

		if (measurement <= median) {
			float prop = (float) ((float) (measurement - range.getMinValue()) / (median - range.getMinValue()));
			float rVal = rLow + prop * (rMid - rLow);
			float gVal = gLow + prop * (gMid - gLow);
			float bVal = bLow + prop * (bMid - bLow);

			return new Color(rVal, gVal, bVal);
		} else {
			//Related to bug https://github.com/BaderLab/EnrichmentMapApp/issues/116
			//When there is differing max and mins for datasets then it will throw exception
			//for the dataset2 if the value is bigger than the max
			//This need to be fixed on the dataset but in the meantime if the value is bigger
			//than the max set it to the max
			if (measurement > range.getMaxValue())
				measurement = range.getMaxValue();

			float prop = (float) ((float) (measurement - median) / (range.getMaxValue() - median));
			float rVal = rMid + prop * (rHigh - rMid);
			float gVal = gMid + prop * (gHigh - gMid);
			float bVal = bMid + prop * (bHigh - bMid);

			return new Color(rVal, gVal, bVal);

		}
	}

	private Object[][] createSortedMergedTableData(Object[][] expValue) {
		this.expValue = expValue;
		Object[][] data;

		int[] HRow1 = this.getHalfRow1Length();
		int[] HRow2 = this.getHalfRow2Length();
		ColorGradientTheme[] Row1CRT = this.getThemeHalfRow1();
		ColorGradientTheme[] Row2CRT = this.getThemeHalfRow2();
		ColorGradientRange[] Row1CRR = this.getRangeHalfRow1();
		ColorGradientRange[] Row2CRR = this.getRangeHalfRow2();
		String[] Row1gene = this.gethRow1();
		String[] Row2gene = this.gethRow2();

		int kValue;

		if (params.isTwoDistinctExpressionSets())
			kValue = expValue.length;
		//kValue = currentExpressionSet.size() + currentExpressionSet2.size();
		else
			kValue = Math.max(currentExpressionSet.size(), currentExpressionSet2.size());

		int totalConditions;
		//if either of the sets is a rank file instead of expression set then there is only one column
		if (numConditions == 2 && numConditions2 == 2)
			totalConditions = 2 + 1;
		else if (numConditions == 2)
			totalConditions = 1 + numConditions2;
		else if (numConditions2 == 2)
			totalConditions = 1 + numConditions;
		else
			totalConditions = (numConditions + numConditions2 - 2);

		data = new Object[kValue][totalConditions];
		for (int k = 0; k < kValue; k++) {

			data[k][0] = expValue[k][0];
			data[k][1] = expValue[k][1];

			for (int j = 0; j < HRow1[k]; j++) {
				data[k][j + 2] = new ExpressionTableValue((Double) expValue[k][j + 2],
						getColor(Row1CRT[k], Row1CRR[k], Row1gene[k], (Double) expValue[k][j + 2]));

			}

			for (int j = HRow1[k]; j < HRow2[k]; j++) {
				data[k][j + 2] = new ExpressionTableValue((Double) expValue[k][j + 2],
						getColor(Row2CRT[k], Row2CRR[k], Row2gene[k], (Double) expValue[k][j + 2]));

			}

		}

		return data;
	}

	private Object[][] createSortedTableData() {

		expValue = new Object[currentExpressionSet.size()][numConditions];
		rowLength = new int[currentExpressionSet.size()];
		rowTheme = new ColorGradientTheme[currentExpressionSet.size()];
		rowGeneName = new String[currentExpressionSet.size()];
		rowRange = new ColorGradientRange[currentExpressionSet.size()];
		//Got through the hashmap and put all the values is

		Integer[] ranks_subset = new Integer[currentExpressionSet.size()];

		HashMap<Integer, ArrayList<Integer>> rank2keys = new HashMap<Integer, ArrayList<Integer>>();

		Ranking ranks = getRanks(currentExpressionSet);
		if (ranks == null)
			ranks = getEmptyRanks(currentExpressionSet);

		//if the ranks are the GSEA ranks and the leading edge is activated then we need to highlight
		//genes in the leading edge
		int topRank = -1;
		boolean isNegative = false;

		//just having one node is not suffiecient reason for dispalying the the leading edge make sure we are sorted by rank and that there is a rank file.
		//The issue is that the expression subset is only updated on node selection and that is where we determine if it is
		//a selection qualified for leadingedge annotation but the user can change the sorting option without updating the
		//selection.
		if (this.displayLeadingEdge && map.getDataset(EnrichmentMap.DATASET1).getExpressionSets().haveRanks()
				&& (hmParams.getSort() == HeatMapParameters.Sort.RANK || params.getDefaultSortMethod().equalsIgnoreCase(hmParams.getSort().toString()))) {
			topRank = getTopRank();
			if (hmParams.getRankFileIndex().equalsIgnoreCase("Dataset 1 Ranking")
					|| hmParams.getRankFileIndex().equalsIgnoreCase(Ranking.GSEARanking + "-" + EnrichmentMap.DATASET1))
				isNegative = isNegativeGS(1);
			else if (hmParams.getRankFileIndex().equalsIgnoreCase("Dataset 2 Ranking")
					|| hmParams.getRankFileIndex().equalsIgnoreCase(Ranking.GSEARanking + "-" + EnrichmentMap.DATASET2))
				isNegative = isNegativeGS(2);
		}

		int n = 0;
		for (Iterator<Integer> i = currentExpressionSet.keySet().iterator(); i.hasNext();) {
			Integer key = i.next();

			//check to see the key is in the rank file.
			//For new rank files it is possible that some of the genes/proteins won't be ranked
			if (ranks.contains(key)) {
				ranks_subset[n] = ranks.getRank(key).getRank();
				//check to see if the rank is already in the list.
			} else {
				ranks_subset[n] = -1;
			}
			if (!rank2keys.containsKey(ranks_subset[n])) {
				ArrayList<Integer> temp = new ArrayList<Integer>();
				temp.add(key);
				rank2keys.put(ranks_subset[n], temp);
			} else {
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
		if (ascending && isNegative && this.displayLeadingEdge && hmParams.getSort() == HeatMapParameters.Sort.RANK) {
			hmParams.changeAscendingValue(hmParams.getSortIndex());
			ascending = false;
		} else if (!ascending && !isNegative && this.displayLeadingEdge && hmParams.getSort() == HeatMapParameters.Sort.RANK) {
			hmParams.changeAscendingValue(hmParams.getSortIndex());
			ascending = true;
		}

		if (ascending)
			//sort ranks
			Arrays.sort(ranks_subset);
		else
			Arrays.sort(ranks_subset, Collections.reverseOrder());

		int k = 0;
		int previous = -1;
		boolean significant_gene = false;

		for (int m = 0; m < ranks_subset.length; m++) {
			//if the current gene doesn't have a rank then don't show it
			if (ranks_subset[m] == -1)
				continue;

			if (ranks_subset[m] == previous)
				continue;

			previous = ranks_subset[m];

			significant_gene = false;

			if (ranks_subset[m] <= topRank && !isNegative && topRank != 0 && topRank != -1)
				significant_gene = true;
			else if (ranks_subset[m] >= topRank && isNegative && topRank != 0 && topRank != -1)
				significant_gene = true;
			//if we are only displaying the leading edge (can only do for individual nodes)
			if (OnlyLeadingEdge == true && significant_gene == false && topRank > 0)
				continue;

			ArrayList<Integer> keys = rank2keys.get(ranks_subset[m]);

			for (Iterator<Integer> p = keys.iterator(); p.hasNext();) {
				Integer key = (Integer) p.next();

				//Current expression row
				GeneExpression row = (GeneExpression) currentExpressionSet.get(key);
				Double[] expression_values = getExpression(row, 1);

				// stores the gene names in column 0
				try { // stores file name if the file contains integer (inserted to aid sorting)
					if (significant_gene)
						//cast the gene name as Significant gene type significant gene list
						//significantGenes.add(row.getName());
						expValue[k][0] = new SignificantGene(Integer.parseInt(row.getName()));
					else
						expValue[k][0] = Integer.parseInt(row.getName());
				} catch (NumberFormatException v) { // if not integer stores as string (inserted to aid sorting)
					if (significant_gene)
						//cast the gene name as Significant gene type significant gene list
						//significantGenes.add(row.getName());
						expValue[k][0] = new SignificantGene(row.getName());
					else
						expValue[k][0] = row.getName();

				}

				expValue[k][1] = row.getDescription();

				rowLength[k] = row.getExpression().length;
				rowTheme[k] = hmParams.getTheme_ds1();
				rowRange[k] = hmParams.getRange_ds1();
				rowGeneName[k] = row.getName();

				for (int j = 0; j < row.getExpression().length; j++) {
					if (numConditions == 2)
						expValue[k][j + 1] = expression_values[j];
					else
						expValue[k][j + 2] = expression_values[j];

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

	private Object[][] createSortedMergedTableData() {

		int totalConditions;
		//if either of the sets is a rank file instead of expression set then there is only one column
		if (numConditions == 2 && numConditions2 == 2)
			totalConditions = 2 + 1;
		else if (numConditions == 2)
			totalConditions = 1 + numConditions2;
		else if (numConditions2 == 2)
			totalConditions = 1 + numConditions;
		else
			totalConditions = (numConditions + numConditions2 - 2);

		//when constructing a heatmap and the expression matrices do not contain the exact same set of
		//gene we have to use an expression set consisting of a merged set from the two expression sets.
		HashMap<Integer, GeneExpression> expressionUsing;
		if (currentExpressionSet.size() == 0)
			expressionUsing = currentExpressionSet2;
		else if (currentExpressionSet2.size() == 0)
			expressionUsing = currentExpressionSet;
		else {
			HashMap<Integer, GeneExpression> mergedExpression = new HashMap<Integer, GeneExpression>();

			mergedExpression.putAll(currentExpressionSet);

			//go through the second and only add the expression that isn't in the first set
			for (Iterator<Integer> i = currentExpressionSet2.keySet().iterator(); i.hasNext();) {
				Integer key = i.next();
				GeneExpression exp2 = currentExpressionSet2.get(key);
				if (!mergedExpression.containsKey(key)) {
					//merge            			
					//add to mergedExpression Set
					mergedExpression.put(key, exp2);
				}
			}
			expressionUsing = mergedExpression;
		}

		int kValue;

		if (params.isTwoDistinctExpressionSets())
			kValue = expressionUsing.size();
		else
			kValue = Math.max(currentExpressionSet.size(), currentExpressionSet2.size());
		expValue = new Object[kValue][totalConditions];
		hRow1 = new String[kValue];
		hRow2 = new String[kValue];
		halfRow1Length = new int[kValue];
		halfRow2Length = new int[kValue];
		rangeHalfRow1 = new ColorGradientRange[kValue];
		rangeHalfRow2 = new ColorGradientRange[kValue];
		themeHalfRow1 = new ColorGradientTheme[kValue];
		themeHalfRow2 = new ColorGradientTheme[kValue];

		Integer[] ranks_subset = new Integer[kValue];

		Map<Integer, ArrayList<Integer>> rank2keys = new HashMap<>();

		Ranking ranks = getRanks(expressionUsing);
		if (ranks == null)
			ranks = getEmptyRanks(expressionUsing);

		//if the ranks are the GSEA ranks and the leading edge is activated then we need to highlight
		//genes in the leading edge
		int topRank = getTopRank();
		boolean isNegative = false;
		if (hmParams.getRankFileIndex().equalsIgnoreCase("Dataset 1 Ranking")
				|| hmParams.getRankFileIndex().equalsIgnoreCase(Ranking.GSEARanking + "-" + EnrichmentMap.DATASET1))
			isNegative = isNegativeGS(1);
		else if (hmParams.getRankFileIndex().equalsIgnoreCase("Dataset 2 Ranking")
				|| hmParams.getRankFileIndex().equalsIgnoreCase(Ranking.GSEARanking + "-" + EnrichmentMap.DATASET2))
			isNegative = isNegativeGS(2);

		int n = 0;
		int maxRank = 0;
		int missingRanksCount = 0;
		for (Iterator<Integer> i = expressionUsing.keySet().iterator(); i.hasNext();) {
			Integer key = i.next();
			//check to see the key is in the rank file.
			//For new rank files it is possible that some of the genes/proteins won't be ranked
			if (ranks.contains(key)) {
				ranks_subset[n] = ranks.getRank(key).getRank();

				if (ranks_subset[n] > maxRank)
					maxRank = ranks_subset[n];
				//check to see if the rank is already in the list.
			} else {
				ranks_subset[n] = -1;
				missingRanksCount++;
			}
			if (!rank2keys.containsKey(ranks_subset[n])) {
				ArrayList<Integer> temp = new ArrayList<Integer>();
				temp.add(key);
				rank2keys.put(ranks_subset[n], temp);
			} else {
				rank2keys.get(ranks_subset[n]).add(key);
			}
			n++;
		}

		//A rank of -1 can indicate a missing rank which is expected when you have two distinct datasets
		//We want to make sure the -1 are at the bottom of the list with two distinct data sets
		if (params.isTwoDistinctExpressionSets() && missingRanksCount > 0) {
			int fakeRank = 0;
			if (isNegative)
				fakeRank = 1;
			else
				fakeRank = maxRank + 100;

			for (int s = 0; s < ranks_subset.length; s++) {

				if (ranks_subset[s] == -1)
					ranks_subset[s] = fakeRank;

				//update the ranks2keys subset
				rank2keys.put(fakeRank, rank2keys.get(-1));
			}
		}

		//depending on the set value of ascending dictates which direction the ranks are
		//outputed.  if ascending then output the ranks as is, if not ascending (descending) inverse
		//the order of the ranks.
		boolean ascending = hmParams.isAscending(hmParams.getSortIndex());

		//Doctor the sorting to always have the leading edge at the top
		//if it is supposed to be ascending and the gene set isNegative then reverse
		//the sorting
		//Displayleadingedge is specific for calculating the ranking for GSEA when one gene set is selected.
		//to make sure the doctoring of the sorting only happens with the leading edge stuff make sure the sorting
		//is by Rank.
		if (ascending && isNegative && this.displayLeadingEdge && hmParams.getSort() == HeatMapParameters.Sort.RANK) {
			hmParams.changeAscendingValue(hmParams.getSortIndex());
			ascending = false;
		} else if (!ascending && !isNegative && this.displayLeadingEdge && hmParams.getSort() == HeatMapParameters.Sort.RANK) {
			hmParams.changeAscendingValue(hmParams.getSortIndex());
			ascending = true;
		}

		if (ascending)
			//sort ranks
			Arrays.sort(ranks_subset);
		else
			Arrays.sort(ranks_subset, Collections.reverseOrder());

		int k = 0;
		int previous = -1;
		boolean significant_gene = false;

		for (int m = 0; m < ranks_subset.length; m++) {
			//if the current gene doesn't have a rank then don't show it
			if (ranks_subset[m] == -1)
				continue;

			if (ranks_subset[m] == previous)
				continue;

			previous = ranks_subset[m];

			significant_gene = false;

			if (ranks_subset[m] <= topRank && !isNegative && topRank != 0 && topRank != -1)
				significant_gene = true;
			else if (ranks_subset[m] >= topRank && isNegative && topRank != 0 && topRank != -1)
				significant_gene = true;

			//if we are only displaying the leading edge (can only do for individual nodes)
			if (OnlyLeadingEdge == true && significant_gene == false && topRank > 0)
				continue;

			ArrayList<Integer> keys = rank2keys.get(ranks_subset[m]);

			for (Iterator<Integer> p = keys.iterator(); p.hasNext();) {
				Integer currentKey = p.next();

				//Current expression row
				GeneExpression halfRow1 = (GeneExpression) currentExpressionSet.get(currentKey);

				//get the corresponding row from the second dataset
				GeneExpression halfRow2 = (GeneExpression) currentExpressionSet2.get(currentKey);

				Double[] expression_values1 = getExpression(halfRow1, 1);
				Double[] expression_values2 = getExpression(halfRow2, 2);

				//Get the name and the description of the row
				if (halfRow1 != null) {
					// stores the gene names in column 0
					try { // stores file name if the file contains integer (inserted to aid sorting)
						if (significant_gene)
							//cast the gene name as Significant gene type significant gene list
							//significantGenes.add(row.getName());
							expValue[k][0] = new SignificantGene(Integer.parseInt(halfRow1.getName()));

						else
							expValue[k][0] = Integer.parseInt(halfRow1.getName());
					} catch (NumberFormatException v) { // if not integer stores as string (inserted to aid sorting)
						if (significant_gene)
							//cast the gene name as Significant gene type significant gene list
							//significantGenes.add(row.getName());
							expValue[k][0] = new SignificantGene(halfRow1.getName());
						else
							expValue[k][0] = halfRow1.getName();
					}
					expValue[k][1] = halfRow1.getDescription();
				} else if (halfRow2 != null) {
					// stores the gene names in column 0
					try { // stores file name if the file contains integer (inserted to aid sorting)
						if (significant_gene)
							//cast the gene name as Significant gene type significant gene list
							//significantGenes.add(row.getName());
							expValue[k][0] = new SignificantGene(Integer.parseInt(halfRow2.getName()));

						else
							expValue[k][0] = Integer.parseInt(halfRow2.getName());
					} catch (NumberFormatException v) { // if not integer stores as string (inserted to aid sorting)
						if (significant_gene)
							//cast the gene name as Significant gene type significant gene list
							//significantGenes.add(row.getName());
							expValue[k][0] = new SignificantGene(halfRow2.getName());
						else
							expValue[k][0] = halfRow2.getName();
					}
					expValue[k][1] = halfRow2.getDescription();
				}

				/*
				 * if(halfRow1 != null){
				 * halfRow1Length[k]=halfRow1.getExpression().length;
				 * themeHalfRow1[k]=hmParams.getTheme();
				 * rangeHalfRow1[k]=hmParams.getRange();
				 * hRow1[k]=halfRow1.getName(); for(int j = 0; j <
				 * halfRow1.getExpression().length;j++){
				 * expValue[k][j+2]=expression_values1[j]; } }
				 * 
				 * if(halfRow2 != null){ if(halfRow1 == null)
				 * halfRow2Length[k]=(halfRow2.getExpression().length); else
				 * halfRow2Length[k]=(halfRow1.getExpression().length +
				 * halfRow2.getExpression().length);
				 * themeHalfRow2[k]=hmParams.getTheme();
				 * rangeHalfRow2[k]=hmParams.getRange();
				 * hRow2[k]=halfRow1.getName(); for(int j =
				 * halfRow1.getExpression().length; j <
				 * (halfRow1.getExpression().length +
				 * halfRow2.getExpression().length);j++){
				 * expValue[k][j+2]=expression_values2[j-halfRow1.getExpression(
				 * ).length]; } }
				 */

				halfRow1Length[k] = expression_values1.length;
				themeHalfRow1[k] = hmParams.getTheme_ds1();
				rangeHalfRow1[k] = hmParams.getRange_ds1();

				if (halfRow1 != null)
					hRow1[k] = halfRow1.getName();
				else
					hRow1[k] = halfRow2.getName();

				for (int j = 0; j < expression_values1.length; j++) {
					expValue[k][j + 2] = expression_values1[j];
				}

				halfRow2Length[k] = (expression_values1.length + expression_values2.length);
				themeHalfRow2[k] = hmParams.getTheme_ds2();
				rangeHalfRow2[k] = hmParams.getRange_ds2();
				if (halfRow1 != null)
					hRow2[k] = halfRow1.getName();
				else
					hRow2[k] = halfRow2.getName();
				for (int j = expression_values1.length; j < (expression_values1.length + expression_values2.length); j++) {
					expValue[k][j + 2] = expression_values2[j - expression_values1.length];
				}

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
	 * Given the gene expression row
	 * 
	 * @return The expression set, transformed according the user specified
	 *         transformation.
	 */
	private Double[] getExpression(GeneExpression row, int dataset) {

		Double[] expression_values1 = null;

		if (hmParams.getTransformation() == HeatMapParameters.Transformation.ROWNORM) {
			if (row != null)
				expression_values1 = row.rowNormalize();
		} else if (hmParams.getTransformation() == HeatMapParameters.Transformation.LOGTRANSFORM) {
			if (row != null)
				expression_values1 = row.rowLogTransform();
		} else {
			if (row != null)
				expression_values1 = row.getExpression();
		}

		//if either of the expression_values is null set the array to have no data
		if (expression_values1 == null && dataset == 1) {
			expression_values1 = new Double[columnNames.length - 2];
			for (int q = 0; q < expression_values1.length; q++)
				expression_values1[q] = Double.NaN/* null */;
		} else if (expression_values1 == null && dataset == 2) {
			expression_values1 = new Double[columnNames2.length - 2];
			for (int q = 0; q < expression_values1.length; q++)
				expression_values1[q] = Double.NaN/* null */;
		}

		return expression_values1;
	}

	// created new North panel to accommodate the expression legend, normalization options,sorting options, saving option
	private JPanel showValuesPanel() {

		JPanel showValuesPanel = new JPanel();
		showValuesPanel.setMaximumSize(new Dimension(50, 100));
		showValuesPanel.setMinimumSize(new Dimension(50, 100));

		showValues = new JCheckBox("Show values");

		if (this.hmParams.isShowValues()) {
			showValues.setSelected(true);
		}

		showValues.addItemListener(new java.awt.event.ItemListener() {
			public void itemStateChanged(ItemEvent evt) {
				showValuesStateChanged(evt);

			}
		});

		//create a panel for the two buttons;
		showValuesPanel.setLayout(new BorderLayout());
		showValuesPanel.add(showValues, BorderLayout.SOUTH);
		return showValuesPanel;
	}

	/**
	 * create legend panel
	 * 
	 * @return legend panel
	 */
	private JPanel expressionLegendPanel() {
		JPanel expLegendPanel = new JPanel(new BorderLayout());
		expLegendPanel.setPreferredSize(new Dimension(200, 75));

		TitledBorder expBorder = BorderFactory.createTitledBorder("Expression legend");
		expBorder.setTitleJustification(TitledBorder.LEFT);
		expLegendPanel.setBorder(expBorder);

		if (this.currentExpressionSet2 != null && !this.currentExpressionSet2.isEmpty()) {
			ColorGradientWidget new_legend_ds1 = ColorGradientWidget.getInstance("", 100, 30, 5, 5, hmParams.getTheme_ds1(), hmParams.getRange_ds1(), true,
					ColorGradientWidget.LEGEND_POSITION.LEFT);
			ColorGradientWidget new_legend_ds2 = ColorGradientWidget.getInstance("", 100, 30, 5, 5, hmParams.getTheme_ds2(), hmParams.getRange_ds2(), true,
					ColorGradientWidget.LEGEND_POSITION.LEFT);

			expLegendPanel.add(new_legend_ds1, BorderLayout.NORTH);
			expLegendPanel.add(new_legend_ds2, BorderLayout.SOUTH);
		} else {
			ColorGradientWidget new_legend = ColorGradientWidget.getInstance("", 200, 30, 5, 5, hmParams.getTheme_ds1(), hmParams.getRange_ds1(), true,
					ColorGradientWidget.LEGEND_POSITION.LEFT);

			expLegendPanel.add(new_legend, BorderLayout.CENTER);
		}

		expLegendPanel.revalidate();
		return expLegendPanel;
	}

	/**
	 * Creates north panel containing panels for legend, sort by combo box, data
	 * tranformation combo box, save expression set button
	 *
	 * @return panel
	 */
	private JPanel createNorthPanel() {

		northPanel = new JPanel();// new north panel
		JPanel buttonPanel = new JPanel();// brought button panel from westPanel
		buttonPanel.setLayout(new BorderLayout());
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

		buttonPanel.add(SaveExpressionSet, BorderLayout.NORTH);
		buttonPanel.add(exportExpressionSet, BorderLayout.SOUTH);

		addComponent(northPanel, expressionLegendPanel(), 0, 0, 2, 1, GridBagConstraints.CENTER, GridBagConstraints.NONE);

		//add the show data values to the transformation drop down panel.	
		JPanel datatransformPanel = createDataTransformationOptionsPanel();
		datatransformPanel.add(showValuesPanel(), BorderLayout.SOUTH);

		addComponent(northPanel, datatransformPanel, 2, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.NONE);

		addComponent(northPanel, createSortOptionsPanel(), 3, 0, 2, 1, GridBagConstraints.CENTER, GridBagConstraints.NONE);

		addComponent(northPanel, buttonPanel, 5, 0, 1, 1, GridBagConstraints.CENTER, GridBagConstraints.NONE);

		northPanel.revalidate();
		return northPanel;
	}

	/**
	 * method to create Data Transformations Options combo box
	 *
	 * @param params - enrichment map parameters of current map
	 * @return - panel with the Data Transformations Options combo box
	 */
	public JPanel createDataTransformationOptionsPanel() {
		JPanel heatmapOptions;
		JComboBox hmOptionComboBox;

		TitledBorder HMBorder = BorderFactory.createTitledBorder("Normalization");
		HMBorder.setTitleJustification(TitledBorder.LEFT);
		heatmapOptions = new JPanel();
		heatmapOptions.setLayout(new BorderLayout());
		hmOptionComboBox = new JComboBox();
		hmOptionComboBox.addItem(HeatMapParameters.asis);
		hmOptionComboBox.addItem(HeatMapParameters.rownorm);
		hmOptionComboBox.addItem(HeatMapParameters.logtrans);

		switch (hmParams.getTransformation()) {
		case ASIS:
			hmOptionComboBox.setSelectedItem(HeatMapParameters.asis);
			break;
		case ROWNORM:
			hmOptionComboBox.setSelectedItem(HeatMapParameters.rownorm);
			break;
		case LOGTRANSFORM:
			hmOptionComboBox.setSelectedItem(HeatMapParameters.logtrans);
			break;
		}

		hmOptionComboBox.addActionListener(new HeatMapActionListener(hmParams.getEdgeOverlapPanel(), hmParams.getNodeOverlapPanel(), hmOptionComboBox,
				this.hmParams, map, fileUtil, streamUtil, application));
		heatmapOptions.add(hmOptionComboBox, BorderLayout.NORTH);
		heatmapOptions.setBorder(HMBorder);
		return heatmapOptions;
	}

	/**
	 * method to create Sort by combo box
	 *
	 * @param params - enrichment map parameters of current map
	 * @return - panel with the sort by combo box
	 */
	public JPanel createSortOptionsPanel() {

		JPanel RankOptions;

		TitledBorder RankBorder = BorderFactory.createTitledBorder("Sorting");
		Set<String> ranks = map.getAllRankNames();
		RankBorder.setTitleJustification(TitledBorder.LEFT);
		RankOptions = new JPanel();
		rankOptionComboBox = new JComboBox();

		//create a panel for the combobox and button
		JPanel ComboButton = new JPanel();

		rankOptionComboBox.addItem(HeatMapParameters.sort_hierarchical_cluster);

		//create the rank options based on what we have in the set of ranks
		//Go through the ranks hashmap and insert each ranking as an option
		if (ranks != null) {
			//convert the ranks into a treeset so that they are ordered

			for (Iterator<String> j = ranks.iterator(); j.hasNext();) {
				String ranks_name = j.next().toString();
				rankOptionComboBox.addItem(ranks_name);
			}
		}

		rankOptionComboBox.addItem(HeatMapParameters.sort_none);

		switch (hmParams.getSort()) {
		case DEFAULT:
			rankOptionComboBox.setSelectedItem(map.getParams().getDefaultSortMethod());
			if (map.getParams().getDefaultSortMethod().equalsIgnoreCase(HeatMapParameters.sort_rank)) {
				hmParams.setSort(Sort.RANK);
				if (ranks != null) {
					hmParams.setRankFileIndex(ranks.iterator().next());
					hmParams.setSortIndex(hmParams.getAscending().length - ranks.size());
				} else {
					rankOptionComboBox.setSelectedItem(HeatMapParameters.sort_none);
					hmParams.setSort(Sort.NONE);
				}
			} else if (map.getParams().getDefaultSortMethod().equalsIgnoreCase(HeatMapParameters.sort_none))
				hmParams.setSort(Sort.NONE);
			else if (map.getParams().getDefaultSortMethod().equalsIgnoreCase(HeatMapParameters.sort_hierarchical_cluster))
				hmParams.setSort(Sort.CLUSTER);
			break;

		case CLUSTER:
			rankOptionComboBox.setSelectedItem(HeatMapParameters.sort_hierarchical_cluster);
			break;

		case NONE:
			rankOptionComboBox.setSelectedItem(HeatMapParameters.sort_none);
			break;

		case RANK:
			int k = 0;
			int columns = 0;
			//add columns to the colum set but make sure the expression files are not the same dile
			if (map.getParams().isData2() && map.getDataset(EnrichmentMap.DATASET2).getExpressionSets() != null && !map.getDataset(EnrichmentMap.DATASET1)
					.getExpressionSets().getFilename().equalsIgnoreCase(map.getDataset(EnrichmentMap.DATASET2).getExpressionSets().getFilename()))
				columns = map.getDataset(EnrichmentMap.DATASET1).getExpressionSets().getColumnNames().length
						+ map.getDataset(EnrichmentMap.DATASET2).getExpressionSets().getColumnNames().length - 2;
			else
				columns = map.getDataset(EnrichmentMap.DATASET1).getExpressionSets().getColumnNames().length;

			for (Iterator<String> j = ranks.iterator(); j.hasNext();) {
				String ranks_name = j.next().toString();
				if (ranks_name.equalsIgnoreCase(hmParams.getRankFileIndex())) {
					rankOptionComboBox.setSelectedItem(ranks_name);
					hmParams.setSortIndex(columns + k);
				}
				k++;
			}
			break;

		case COLUMN:
			rankOptionComboBox.addItem(HeatMapParameters.sort_column + ":" + hmParams.getSortbycolumnName());
			rankOptionComboBox.setSelectedItem(HeatMapParameters.sort_column + ":" + hmParams.getSortbycolumnName());
			break;

		}

		// set the selection in the rank combo box
		//if this is the initial creation then set the rank to the default
		//add the option to add another rank file
		rankOptionComboBox.addItem("Add Rankings ... ");

		ComboButton.add(rankOptionComboBox);

		//include the button only if we are sorting by column or ranks
		if (hmParams.getSort() == Sort.RANK || hmParams.getSort() == Sort.COLUMN) {
			JButton arrow;
			if (hmParams.isAscending(hmParams.getSortIndex()))
				arrow = createArrowButton(Ascending);
			else
				arrow = createArrowButton(Descending);
			ComboButton.add(arrow);
			arrow.addActionListener(new ChangeSortAction(arrow));
		}

		rankOptionComboBox.addActionListener(new HeatMapActionListener(hmParams.getEdgeOverlapPanel(), hmParams.getNodeOverlapPanel(), rankOptionComboBox,
				hmParams, map, fileUtil, streamUtil, application));

		ComboButton.revalidate();

		RankOptions.add(ComboButton);
		RankOptions.setBorder(RankBorder);
		RankOptions.revalidate();

		return RankOptions;
	}

	/**
	 * Returns a button with an arrow icon and a collapse/expand action
	 * listener.
	 *
	 * @return button Button which is used in the titled border component
	 */
	private JButton createArrowButton(int direction) {
		JButton button = new JButton(iconArrow[direction]);
		button.setBorder(BorderFactory.createEmptyBorder(0, 1, 5, 1));
		button.setVerticalTextPosition(AbstractButton.CENTER);
		button.setHorizontalTextPosition(AbstractButton.LEFT);
		button.setMargin(new Insets(0, 0, 3, 0));

		//We want to use the same font as those in the titled border font
		Font font = BorderFactory.createTitledBorder("Sample").getTitleFont();
		Color color = BorderFactory.createTitledBorder("Sample").getTitleColor();
		button.setFont(font);
		button.setForeground(color);
		button.setFocusable(false);
		button.setContentAreaFilled(false);

		return button;
	}

	private ImageIcon[] createExpandAndCollapseIcon() {
		ImageIcon[] iconArrow = new ImageIcon[2];
		URL iconURL;
		//                         Oliver at 26/06/2009:  relative path works for me,
		//                         maybe need to change to org/baderlab/csplugins/enrichmentmap/resources/arrow_collapsed.gif
		iconURL = this.getClass().getResource("arrow_up.gif");
		if (iconURL != null) {
			iconArrow[Ascending] = new ImageIcon(iconURL);
		}
		iconURL = this.getClass().getResource("arrow_down.gif");
		if (iconURL != null) {
			iconArrow[Descending] = new ImageIcon(iconURL);
		}
		return iconArrow;
	}

	/**
	 * Handles expanding and collapsing of extra content on the user's click of
	 * the titledBorder component.
	 */
	private class ChangeSortAction extends AbstractAction implements ActionListener, ItemListener {
		/**
		 * 
		 */
		private static final long serialVersionUID = -8951978251258210440L;
		private JButton arrow;

		private ChangeSortAction(JButton arrow) {
			this.arrow = arrow;

		}

		public void actionPerformed(ActionEvent e) {
			hmParams.flipAscending(hmParams.getSortIndex());

			if (hmParams.isAscending(hmParams.getSortIndex()))
				arrow.setIcon(iconArrow[Ascending]);
			else
				arrow.setIcon(iconArrow[Descending]);

			hmParams.getEdgeOverlapPanel().clearPanel();
			hmParams.getNodeOverlapPanel().clearPanel();

			hmParams.getEdgeOverlapPanel().updatePanel();
			hmParams.getNodeOverlapPanel().updatePanel();

		}

		public void itemStateChanged(ItemEvent e) {
			hmParams.flipAscending(hmParams.getSortIndex());
		}
	}

	private void addComponent(Container container, Component component, int gridx, int gridy, int gridwidth, int gridheight, int anchor, int fill) {
		GridBagConstraints gbc = new GridBagConstraints(gridx, gridy, gridwidth, gridheight, 1.0, 1.0, anchor, fill, insets, 0, 0);
		container.add(component, gbc);
	}

	private void saveExpressionSetActionPerformed(ActionEvent evt) {
		FileChooserFilter filter_txt = new FileChooserFilter("txt Files", "txt");

		//the set of filter (required by the file util method
		ArrayList<FileChooserFilter> all_filters = new ArrayList<FileChooserFilter>();
		all_filters.add(filter_txt);

		java.io.File file = fileUtil.getFile(application.getJFrame(), "Export Heatmap as txt File", FileUtil.SAVE, all_filters);
		if (file != null && file.toString() != null) {
			String fileName = file.toString();
			if (!fileName.endsWith(".txt")) {
				fileName += ".txt";
				file = new File(fileName);
			}

			int response = JOptionPane.OK_OPTION;
			if (file.exists())
				response = JOptionPane.showConfirmDialog(this, "The file already exists.  Would you like to overwrite it?");
			if (response == JOptionPane.NO_OPTION || response == JOptionPane.CANCEL_OPTION) {

			} else if (response == JOptionPane.YES_OPTION || response == JOptionPane.OK_OPTION) {
				try {

					//ask user if they want to export only the leading edge.
					//only ask if the leadingedge is displayed
					if (this.displayLeadingEdge == true && hmParams.getSort() == HeatMapParameters.Sort.RANK
							&& params.getMethod().equalsIgnoreCase(EnrichmentMapParameters.method_GSEA)) {
						int response2 = JOptionPane.showConfirmDialog(this, "Would you like to save the leading edge only?");
						if (response2 == JOptionPane.YES_OPTION || response2 == JOptionPane.OK_OPTION)
							this.OnlyLeadingEdge = true;
					}
					BufferedWriter output = new BufferedWriter(new FileWriter(file));
					String[] currentColumns;
					if (params.isData2() && map.getDataset(EnrichmentMap.DATASET2).getExpressionSets() != null && !map.getDataset(EnrichmentMap.DATASET1)
							.getExpressionSets().getFilename().equalsIgnoreCase(map.getDataset(EnrichmentMap.DATASET2).getExpressionSets().getFilename())) {
						currentColumns = new String[columnNames.length + columnNames2.length - 2];

						System.arraycopy(columnNames, 0, currentColumns, 0, columnNames.length);
						System.arraycopy(columnNames2, 2, currentColumns, columnNames.length, columnNames2.length - 2);
					} else
						currentColumns = (String[]) columnNames;

					for (int j = 0; j < currentColumns.length; j++)
						if (j == (currentColumns.length - 1))
							output.write(currentColumns[j] + "\n");
						else
							output.write(currentColumns[j] + "\t");

					//get the sorted expression set
					Object[][] sortedExpression;
					if (params.isData2() && map.getDataset(EnrichmentMap.DATASET2).getExpressionSets() != null && !map.getDataset(EnrichmentMap.DATASET1)
							.getExpressionSets().getFilename().equalsIgnoreCase(map.getDataset(EnrichmentMap.DATASET2).getExpressionSets().getFilename()))
						sortedExpression = createSortedMergedTableData();
					else
						sortedExpression = createSortedTableData();

					for (int k = 0; k < sortedExpression.length; k++) {
						for (int l = 0; l < sortedExpression[k].length; l++)
							if (sortedExpression[k][l] != null)
								output.write(sortedExpression[k][l].toString() + "\t");
							else
								output.write("" + "\t");
						output.write("\n");
					}

					output.flush();
					output.close();
					JOptionPane.showMessageDialog(this, "File " + fileName + " saved.");

					this.OnlyLeadingEdge = false;
				} catch (IOException e) {
					JOptionPane.showMessageDialog(this, "unable to write to file " + fileName);
				}
			}
		}
	}

	private void exportExpressionSetActionPerformed(ActionEvent evt) {
		//JOptionPane.showMessageDialog(this, "PDF export currently not available");
		
		List<FileChooserFilter> filter = Collections.singletonList(new FileChooserFilter("pdf Files", "pdf"));
		File file = fileUtil.getFile(application.getJFrame(), "Export Heatmap as PDF File", FileUtil.SAVE, filter);
		
		if (file != null && file.toString() != null) {
			String fileName = file.toString();
			if (!fileName.endsWith(".pdf")) {
				fileName += ".pdf";
				file = new File(fileName);
			}
			
			HeatMapExporterTask task = new HeatMapExporterTask(getjTable1(), getTableHeader(), file);
			dialogTaskMonitor.execute(new TaskIterator(task));
		}
	}

	/**
	 * Collates the current selected nodes genes to represent the expression of
	 * the genes that are in all the selected nodes. and sets the expression
	 * sets (both if there are two datasets)
	 *
	 * @param params - enrichment map parameters of the current map
	 *
	 */
	private void initializeLeadingEdge(EnrichmentMapParameters params) {

		Object[] nodes = params.getSelectedNodes().toArray();

		//if only one node is selected activate leading edge potential
		//and if at least one rankfile is present
		//TODO: we probably have to catch cases where we have only a rank file for one of the datasets
		if (nodes.length == 1) {
			//get the current Network
			CyNetwork network = applicationManager.getCurrentNetwork();
			for (Object node1 : nodes) {

				CyNode current_node = (CyNode) node1;

				String nodename = network.getRow(current_node).get(CyNetwork.NAME, String.class);

				displayLeadingEdge = true;
				if (params.getMethod().equalsIgnoreCase(EnrichmentMapParameters.method_GSEA)) {

					Map<String, EnrichmentResult> results1 = map.getDataset(EnrichmentMap.DATASET1).getEnrichments().getEnrichments();
					if (results1.containsKey(nodename)) {
						GSEAResult current_result = (GSEAResult) results1.get(nodename);
						leadingEdgeScoreAtMax1 = current_result.getScoreAtMax();
						//if the  score at max is set to the default then get the direction of the leading edge
						//from the NES
						if (leadingEdgeScoreAtMax1 == DetermineEnrichmentResultFileReader.DefaultScoreAtMax)
							leadingEdgeScoreAtMax1 = current_result.getNES();

						leadingEdgeRankAtMax1 = current_result.getRankAtMax();
					}
					if (map.getParams().isTwoDatasets()) {
						Map<String, EnrichmentResult> results2 = map.getDataset(EnrichmentMap.DATASET2).getEnrichments().getEnrichments();
						if (results2.containsKey(nodename)) {
							GSEAResult current_result = (GSEAResult) results2.get(nodename);
							leadingEdgeScoreAtMax2 = current_result.getScoreAtMax();
							//if the  score at max is set to the default then get the direction of the leading edge
							//from the NES
							if (leadingEdgeScoreAtMax2 == DetermineEnrichmentResultFileReader.DefaultScoreAtMax)
								leadingEdgeScoreAtMax2 = current_result.getNES();

							leadingEdgeRankAtMax2 = current_result.getRankAtMax();
						}
					}
				}
			}
		}

	}

	private Ranking getEmptyRanks(Map<Integer, GeneExpression> expressionSet) {
		Ranking ranks = new Ranking();

		for (Iterator<Integer> i = expressionSet.keySet().iterator(); i.hasNext();) {
			Integer key = i.next();
			Rank temp = new Rank(((GeneExpression) expressionSet.get(key)).getName(), 0.0, 0);
			ranks.addRank(key, temp);
			//ranks..put(key,temp);
		}

		return ranks;
	}

	/**
	 * Get the current specified rank file.
	 *
	 * @return current ranking specified by sort by combo box
	 */
	private Ranking getRanks(Map<Integer, GeneExpression> expressionSet) {
		//Get the ranks for all the keys, if there is a ranking file
		Ranking ranks = null;

		//check to see if any of the ordering have been initialized
		if (hmParams.getSort() == HeatMapParameters.Sort.DEFAULT) {
			//initialize the default value
			if (params.getDefaultSortMethod().equalsIgnoreCase(HeatMapParameters.sort_hierarchical_cluster))
				hmParams.setSort(HeatMapParameters.Sort.CLUSTER);
			if (params.getDefaultSortMethod().equalsIgnoreCase(HeatMapParameters.sort_rank)) {
				hmParams.setSort(HeatMapParameters.Sort.RANK);
				Set<String> ranksnames = map.getAllRankNames();
				if (!ranksnames.isEmpty())
					hmParams.setRankFileIndex(ranksnames.iterator().next());
				else {
					hmParams.setSort(HeatMapParameters.Sort.NONE);
				}
			}
			if (params.getDefaultSortMethod().equalsIgnoreCase(HeatMapParameters.sort_none))
				hmParams.setSort(HeatMapParameters.Sort.NONE);
			if (params.getDefaultSortMethod().equalsIgnoreCase(HeatMapParameters.sort_column)) {
				hmParams.setSort(HeatMapParameters.Sort.COLUMN);
				hmParams.setSortIndex(0);
			}
		}

		Set<String> all_ranks = map.getAllRankNames();
		if (hmParams.getSort() == HeatMapParameters.Sort.RANK) {
			for (Iterator<String> j = all_ranks.iterator(); j.hasNext();) {
				String ranks_name = j.next().toString();
				if (ranks_name.equalsIgnoreCase(hmParams.getRankFileIndex()))
					ranks = map.getRanksByName(ranks_name);
			}

			if (ranks == null)
				throw new IllegalThreadStateException("invalid sort index for rank files.");

		} else if ((hmParams.getSort() == HeatMapParameters.Sort.COLUMN) || (hmParams.getSort() == HeatMapParameters.Sort.NONE)) {
			ranks = new Ranking();

			for (Iterator<Integer> i = expressionSet.keySet().iterator(); i.hasNext();) {
				Integer key = i.next();
				Rank temp = new Rank(((GeneExpression) expressionSet.get(key)).getName(), 0.0, 0);
				ranks.addRank(key, temp);
				//ranks..put(key,temp);
			}

			if (hmParams.getSort() == HeatMapParameters.Sort.COLUMN)
				hmParams.setSortbycolumn_event_triggered(true);
		} else if (hmParams.getSort() == HeatMapParameters.Sort.CLUSTER) {
			HeatMapHierarchicalClusterTaskFactory clustertask = new HeatMapHierarchicalClusterTaskFactory(numConditions, numConditions2, this, map, hmParams);
			ResultTaskObserver observer = new ResultTaskObserver();

			this.dialogTaskMonitor.execute(clustertask.createTaskIterator(), observer);
			while (!observer.isAllFinished()) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			ranks = this.ranks;
		}

		//after trying to sort by hierarchical just check that sorting hasn't defaulted to no sort
		if (hmParams.getSort() == HeatMapParameters.Sort.NONE) {
			ranks = getEmptyRanks(expressionSet);
		}

		return ranks;
	}

	/**
	 * For each heat map there is a standard menu built from the linkouts which
	 * are stored in the Cytoscape Properties. when the heat map is first built
	 * initialze this linkout list
	 *
	 * This code was pulled from the CyAttributeBrowserTable.java in cytoscape
	 * coreplugins
	 *
	 */
	//TODO:initialize linkouts using cytoscape default properties
	private void initialize_linkouts() {
		// First, load existing property
		/*
		 * Properties props = CytoscapeInit.getProperties();
		 * 
		 * // Use reflection to get resource Class linkout = null;
		 * 
		 * try { linkout = Class.forName("linkout.LinkOut"); } catch
		 * (ClassNotFoundException e1) {
		 * JOptionPane.showMessageDialog(application.getJFrame(),
		 * "Could't create LinkOut class","Could't create LinkOut class"
		 * ,JOptionPane.WARNING_MESSAGE); return; }
		 * 
		 * final ClassLoader cl = linkout.getClassLoader();
		 * 
		 * try { props.load(cl.getResource("linkout.props").openStream()); }
		 * catch (IOException e1) { JOptionPane.showMessageDialog(this,
		 * "Could't read LinkOut class","Could't read LinkOut class"
		 * ,JOptionPane.WARNING_MESSAGE); }
		 * 
		 * linkoutProps = new HashMap<String, Map<String, String>>();
		 * 
		 * final String nodeLink = "nodelinkouturl";
		 * 
		 * String[] parts = null;
		 * 
		 * for (Map.Entry<Object, Object> entry : props.entrySet()) {
		 * Map<String, String> pair = null;
		 * 
		 * if (entry.getKey().toString().startsWith(nodeLink)) { parts =
		 * entry.getKey().toString().split("\\.");
		 * 
		 * if (parts.length == 3) { pair = linkoutProps.get(parts[1]);
		 * 
		 * if (pair == null) { pair = new HashMap<String, String>();
		 * linkoutProps.put(parts[1], pair); }
		 * 
		 * pair.put(parts[2], entry.getValue().toString()); } } }
		 */

	}

	private int getTopRank() {
		int topRank = 0;

		//just having one node is not suffiecient reason for dispalying the the leading edge make sure we are sorted by rank and that there is a rank file.
		//The issue is that the expression subset is only updated on node selection and that is where we determine if it is
		//a selection qualified for leadingedge annotation but the user can change the sorting option without updating the
		//selection.
		if (displayLeadingEdge
				&& (map.getDataset(EnrichmentMap.DATASET1).getExpressionSets().haveRanks()
						|| map.getDataset(EnrichmentMap.DATASET2).getExpressionSets().haveRanks())
				&& (hmParams.getSort() == HeatMapParameters.Sort.RANK || params.getDefaultSortMethod().equalsIgnoreCase(hmParams.getSort().toString()))) {
			//get the rank under (or over) which everything should be higlighted
			if (hmParams.getRankFileIndex().equalsIgnoreCase("Dataset 1 Ranking")
					|| hmParams.getRankFileIndex().equalsIgnoreCase(Ranking.GSEARanking + "-" + EnrichmentMap.DATASET1)) {
				topRank = leadingEdgeRankAtMax1 + 3;
				//the rank at max is counted starting as if the bottom of the list were at the top
				//if this is a negative gene set then subtract it from the total number of ranks
				//given out.  Even though the analysis might only use half the genes the rank at max
				//is based on the entire expression file being ranked.
				if (leadingEdgeScoreAtMax1 < 0) {
					if (hmParams.getRankFileIndex().equalsIgnoreCase("Dataset 1 Ranking"))
						topRank = map.getDataset(EnrichmentMap.DATASET1).getExpressionSets().getRanksByName("Dataset 1 Ranking").getMaxRank() - topRank;
					else if (hmParams.getRankFileIndex().equalsIgnoreCase(Ranking.GSEARanking + "-" + EnrichmentMap.DATASET1))
						topRank = map.getDataset(EnrichmentMap.DATASET1).getExpressionSets().getRanksByName(Ranking.GSEARanking).getMaxRank() - topRank;
				}
			} else if (hmParams.getRankFileIndex().equalsIgnoreCase("Dataset 2 Ranking")
					|| hmParams.getRankFileIndex().equalsIgnoreCase(Ranking.GSEARanking + "-" + EnrichmentMap.DATASET2)) {
				topRank = leadingEdgeRankAtMax2 + 3;
				if (leadingEdgeScoreAtMax2 < 0) {
					if (hmParams.getRankFileIndex().equalsIgnoreCase("Dataset 2 Ranking"))
						topRank = map.getDataset(EnrichmentMap.DATASET2).getExpressionSets().getRanksByName("Dataset 2 Ranking").getMaxRank() - topRank;
					else if (hmParams.getRankFileIndex().equalsIgnoreCase(Ranking.GSEARanking + "-" + EnrichmentMap.DATASET2))
						topRank = map.getDataset(EnrichmentMap.DATASET2).getExpressionSets().getRanksByName(Ranking.GSEARanking).getMaxRank() - topRank;
				}
			}
		}
		//because of the GSEA ranks are off slightly buffer the cutoff depending on whether the geneset
		//is up or down regulated in order to get all genes in leading edge
		return topRank;
	}

	private boolean isNegativeGS(int dataset) {
		boolean isNegative = false;

		if (dataset == 1) {
			if (leadingEdgeScoreAtMax1 < 0)
				isNegative = true;
			else
				isNegative = false;
		} else if (dataset == 2) {
			if (leadingEdgeScoreAtMax2 < 0)
				isNegative = true;
			else
				isNegative = false;
		}
		return isNegative;
	}

	/*
	 * Showvalue toggle action listener
	 */
	private void showValuesStateChanged(ItemEvent e) {
		//JCheckBox source = (JCheckBox)e.getItemSelectable();
		JCheckBox source = (JCheckBox) e.getSource();

		//if (source == showValues) {
		if (e.getStateChange() == ItemEvent.DESELECTED) {
			showValues.setSelected(false);
			this.hmParams.setShowValues(false);
		}
		if (e.getStateChange() == ItemEvent.SELECTED) {
			this.hmParams.setShowValues(true);
			showValues.setSelected(true);
		}
		this.updatePanel();
		this.revalidate();
		//}

	}

	//Getters and Setters
	Object[][] getExpValue() {
		return expValue;
	}

	public void setColumnSort() {
		rankOptionComboBox.addItem(HeatMapParameters.sort_column + ":" + hmParams.getSortbycolumnName());
		rankOptionComboBox.setSelectedItem(HeatMapParameters.sort_column + ":" + hmParams.getSortbycolumnName());
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

	private void setExpValue(Object[][] expValue) {
		this.expValue = expValue;
	}

	public void clearPanel() {
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
	public void setThemeHalfRow1(ColorGradientTheme[] themeHalfRow1) {
		this.themeHalfRow1 = themeHalfRow1;
	}

	/**
	 * @return the themeHalfRow1
	 */
	public ColorGradientTheme[] getThemeHalfRow1() {
		return themeHalfRow1;
	}

	/**
	 * @param themeHalfRow2 the themeHalfRow2 to set
	 */
	public void setThemeHalfRow2(ColorGradientTheme[] themeHalfRow2) {
		this.themeHalfRow2 = themeHalfRow2;
	}

	/**
	 * @return the themeHalfRow2
	 */
	public ColorGradientTheme[] getThemeHalfRow2() {
		return themeHalfRow2;
	}

	/**
	 * @param rangeHalfRow1 the rangeHalfRow1 to set
	 */
	public void setRangeHalfRow1(ColorGradientRange[] rangeHalfRow1) {
		this.rangeHalfRow1 = rangeHalfRow1;
	}

	/**
	 * @return the rangeHalfRow1
	 */
	public ColorGradientRange[] getRangeHalfRow1() {
		return rangeHalfRow1;
	}

	/**
	 * @param rangeHalfRow2 the rangeHalfRow2 to set
	 */
	public void setRangeHalfRow2(ColorGradientRange[] rangeHalfRow2) {
		this.rangeHalfRow2 = rangeHalfRow2;
	}

	/**
	 * @return the rangeHalfRow2
	 */
	public ColorGradientRange[] getRangeHalfRow2() {
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

	public JTableHeader getTableHeader() {
		return tableHdr;
	}

	public JTable getjTable1() {
		return jTable1;
	}

	public JPanel getNorthPanel() {
		return northPanel;
	}

	public Component getComponent() {
		return this;
	}

	public HashMap<Integer, GeneExpression> getCurrentExpressionSet() {
		return currentExpressionSet;
	}

	public void setCurrentExpressionSet(HashMap<Integer, GeneExpression> currentExpressionSet) {
		this.currentExpressionSet = currentExpressionSet;
	}

	public HashMap<Integer, GeneExpression> getCurrentExpressionSet2() {
		return currentExpressionSet2;
	}

	public void setCurrentExpressionSet2(HashMap<Integer, GeneExpression> currentExpressionSet2) {
		this.currentExpressionSet2 = currentExpressionSet2;
	}

	public Ranking getRanks() {
		return ranks;
	}

	public void setRanks(Ranking ranks) {
		this.ranks = ranks;
	}

	public CytoPanelName getCytoPanelName() {
		return CytoPanelName.SOUTH;
	}

	public Icon getIcon() {
		URL EMIconURL = this.getClass().getResource("enrichmentmap_logo_notext_small.png");
		ImageIcon EMIcon = null;
		if (EMIconURL != null) {
			EMIcon = new ImageIcon(EMIconURL);
		}
		return EMIcon;
	}

	public String getTitle() {
		if (node)
			return "Heat Map (nodes)";
		else
			return "Heat Map (edges)";
	}

}
