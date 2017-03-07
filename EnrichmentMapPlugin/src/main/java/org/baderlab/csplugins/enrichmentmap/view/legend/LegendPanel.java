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

package org.baderlab.csplugins.enrichmentmap.view.legend;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;
import static org.baderlab.csplugins.enrichmentmap.view.util.SwingUtil.makeSmall;
import static org.cytoscape.util.swing.LookAndFeelUtil.equalizeSize;
import static org.cytoscape.util.swing.LookAndFeelUtil.isAquaLAF;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.io.File;
import java.net.URL;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

import org.baderlab.csplugins.enrichmentmap.model.EMCreationParameters;
import org.baderlab.csplugins.enrichmentmap.model.EMCreationParameters.SimilarityMetric;
import org.baderlab.csplugins.enrichmentmap.model.EMDataSet;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.model.LegacySupport;
import org.baderlab.csplugins.enrichmentmap.style.ColumnDescriptor;
import org.baderlab.csplugins.enrichmentmap.style.EMStyleBuilder;
import org.baderlab.csplugins.enrichmentmap.style.EMStyleBuilder.Columns;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyTable;
import org.cytoscape.util.swing.BasicCollapsiblePanel;
import org.cytoscape.util.swing.LookAndFeelUtil;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.RenderingEngine;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.presentation.property.values.NodeShape;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;

import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * Right hand information Panel containing files uploaded and legends
 */
@Singleton
@SuppressWarnings("serial")
public class LegendPanel extends JPanel {

	private static final int LEGEND_ICON_SIZE = 18;
	
	private final Border DEF_LEGEND_BORDER = BorderFactory.createLineBorder(UIManager.getColor("Separator.foreground"));
	private final Color DEF_LEGEND_BG = Color.WHITE;
	
	@Inject private CyApplicationManager applicationManager;
	@Inject private VisualMappingManager visualMappingManager;
	
	private BasicCollapsiblePanel nodeLegendPanel;
	private BasicCollapsiblePanel edgeLegendPanel;
	private BasicCollapsiblePanel propertiesPanel;
	
	private JPanel nodeShapePanel;
	private JLabel nodeShapeIcon1 = new JLabel();
	private JLabel nodeShapeIcon2 = new JLabel();
	private JLabel nodeShapeDesc1 = new JLabel("Gene Set");
	private JLabel nodeShapeDesc2 = new JLabel("Signature Set");
	
	private JTextPane infoPane;
	
	public LegendPanel() {
		setLayout(new BorderLayout());
		
		Border iconBorder = BorderFactory.createEmptyBorder(4, 4, 4, 4);
		nodeShapeIcon1.setBorder(iconBorder);
		nodeShapeIcon2.setBorder(iconBorder);
		
		makeSmall(nodeShapeDesc1, nodeShapeDesc2);
		equalizeSize(nodeShapeDesc1, nodeShapeDesc2);
	}
	
	/**
	 * Update parameters panel based on given enrichment map parameters
	 */
	void update(EnrichmentMap map, CyNetworkView view) {
		EMCreationParameters params = map != null ? map.getParams() : null;

		removeAll();

		if (params == null) {
			JLabel infoLabel = new JLabel("No EnrichmentMap View selected");
			infoLabel.setEnabled(false);
			infoLabel.setForeground(UIManager.getColor("Label.disabledForeground"));
			infoLabel.setHorizontalAlignment(JLabel.CENTER);
			infoLabel.setVerticalAlignment(JLabel.CENTER);
			infoLabel.setBorder(new EmptyBorder(120, 40, 120, 40));

			add(infoLabel, BorderLayout.CENTER);
		} else {
			updateNodeShapeLegend(map, view);
			updateInfoPanel(map);

			JPanel panel = new JPanel();
			final GroupLayout layout = new GroupLayout(panel);
			panel.setLayout(layout);
			layout.setAutoCreateContainerGaps(true);
			layout.setAutoCreateGaps(true);

			layout.setHorizontalGroup(layout.createParallelGroup(Alignment.CENTER, true)
					.addComponent(getNodeLegendPanel(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
					.addComponent(getEdgeLegendPanel(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
					.addComponent(getPropertiesPanel(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE));
			layout.setVerticalGroup(layout.createSequentialGroup()
					.addComponent(getNodeLegendPanel(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addComponent(getEdgeLegendPanel(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addComponent(getPropertiesPanel(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
			);
			
			JScrollPane scrollPane = new JScrollPane(panel);
			add(scrollPane, BorderLayout.CENTER);
		}

		
		revalidate();
	}

	private void updateNodeShapeLegend(EnrichmentMap map, CyNetworkView view) {
		JPanel p = getNodeShapePanel();
		VisualStyle style = view != null ? visualMappingManager.getVisualStyle(view) : null;
		
		nodeShapeIcon1.setVisible(style != null);
		nodeShapeDesc1.setVisible(style != null);
		nodeShapeIcon2.setVisible(style != null && map.hasSignatureDataSets());
		nodeShapeDesc2.setVisible(style != null && map.hasSignatureDataSets());
		
		if (style != null) {
			NodeShape shape = EMStyleBuilder.getGeneSetNodeShape(style);
			nodeShapeIcon1.setIcon(getIcon(BasicVisualLexicon.NODE_SHAPE, shape));
			
			if (map.hasSignatureDataSets()) {
				shape = EMStyleBuilder.getSignatureNodeShape(style);
				nodeShapeIcon2.setIcon(getIcon(BasicVisualLexicon.NODE_SHAPE, shape));
			}
		}
		
		p.revalidate();
	}
	 
	private void updateInfoPanel(EnrichmentMap map) {
		getInfoPane().setText(getInfoText(map));
	}
	 
	private BasicCollapsiblePanel getNodeLegendPanel() {
		if (nodeLegendPanel == null) {
			nodeLegendPanel = new BasicCollapsiblePanel("Nodes (Gene Sets)");
			nodeLegendPanel.setCollapsed(false);
			
			final GroupLayout layout = new GroupLayout(nodeLegendPanel.getContentPane());
			nodeLegendPanel.getContentPane().setLayout(layout);
			layout.setAutoCreateContainerGaps(false);
			layout.setAutoCreateGaps(true);
			
			layout.setHorizontalGroup(layout.createParallelGroup(Alignment.CENTER, true)
					.addComponent(getNodeShapePanel(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
			);
			layout.setVerticalGroup(layout.createSequentialGroup()
					.addComponent(getNodeShapePanel(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
			);
			
			if (isAquaLAF())
				nodeLegendPanel.setOpaque(false);
		}
		
		return nodeLegendPanel;
	}
	
	private BasicCollapsiblePanel getEdgeLegendPanel() {
		if (edgeLegendPanel == null) {
			edgeLegendPanel = new BasicCollapsiblePanel("Edges (Similarity Between Gene Sets)");
			edgeLegendPanel.setCollapsed(false);
			
			final GroupLayout layout = new GroupLayout(edgeLegendPanel.getContentPane());
			edgeLegendPanel.getContentPane().setLayout(layout);
			layout.setAutoCreateContainerGaps(false);
			layout.setAutoCreateGaps(true);
			
			layout.setHorizontalGroup(layout.createParallelGroup(Alignment.CENTER, true)
			);
			layout.setVerticalGroup(layout.createSequentialGroup()
			);
			
			if (isAquaLAF())
				edgeLegendPanel.setOpaque(false);
		}
		
		return edgeLegendPanel;
	}
	
	private BasicCollapsiblePanel getPropertiesPanel() {
		if (propertiesPanel == null) {
			JScrollPane scrollPane = new JScrollPane(getInfoPane(), JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
					JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			Dimension d = new Dimension(300, 100);
			scrollPane.setPreferredSize(d);
			makeSmall(scrollPane);
			
			propertiesPanel = new BasicCollapsiblePanel("Properties");
			propertiesPanel.setCollapsed(true);
			propertiesPanel.getContentPane().setLayout(new BorderLayout());
			propertiesPanel.getContentPane().add(scrollPane, BorderLayout.CENTER);
			
			if (isAquaLAF())
				propertiesPanel.setOpaque(false);
		}
		
		return propertiesPanel;
	}
	
	private JPanel getNodeShapePanel() {
		if (nodeShapePanel == null) {
			nodeShapePanel = new JPanel();
			nodeShapePanel.setBorder(DEF_LEGEND_BORDER);
			nodeShapePanel.setBackground(DEF_LEGEND_BG);
			
			final GroupLayout layout = new GroupLayout(nodeShapePanel);
			nodeShapePanel.setLayout(layout);
			layout.setAutoCreateContainerGaps(true);
			layout.setAutoCreateGaps(true);

			layout.setHorizontalGroup(layout.createSequentialGroup()
					.addComponent(nodeShapeIcon1, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addComponent(nodeShapeDesc1, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addComponent(nodeShapeIcon2, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addComponent(nodeShapeDesc2, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
			);
			layout.setVerticalGroup(layout.createParallelGroup(Alignment.CENTER, false)
					.addComponent(nodeShapeIcon1)
					.addComponent(nodeShapeDesc1)
					.addComponent(nodeShapeIcon2)
					.addComponent(nodeShapeDesc2)
			);
		}
		
		return nodeShapePanel;
	}
	
	private JTextPane getInfoPane() {
		if (infoPane == null) {
			infoPane = new JTextPane();
			infoPane.setEditable(false);
			infoPane.setContentType("text/html");
			makeSmall(infoPane);
		}
		
		return infoPane;
	}

	/**
	 * Get the files and parameters corresponding to the current enrichment map
	 */
	private String getInfoText(EnrichmentMap map) {
		EMCreationParameters params = map.getParams();
		
		final String INDENT = "&nbsp;&nbsp;&nbsp;&nbsp;";
		
		String s = "<html><font size='-2' face='sans-serif'>";

		s = s + "<b>P-value Cut-off:</b> " + params.getPvalue() + "<br>";
		s = s + "<b>FDR Q-value Cut-off:</b> " + params.getQvalue() + "<br>";

		if (params.getSimilarityMetric() == SimilarityMetric.JACCARD) {
			s = s + "<b>Jaccard Cut-off:</b> " + params.getSimilarityCutoff() + "<br>";
			s = s + "<b>Test used:</b> Jaccard Index<br>";
		} else if (params.getSimilarityMetric() == SimilarityMetric.OVERLAP) {
			s = s + "<b>Overlap Cut-off:</b> " + params.getSimilarityCutoff() + "<br>";
			s = s + "<b>Test Used:</b> Overlap Index<br>";
		} else if (params.getSimilarityMetric() == SimilarityMetric.COMBINED) {
			s = s + "<b>Jaccard Overlap Combined Cut-off:</b> " + params.getSimilarityCutoff() + "<br>";
			s = s + "<b>Test Used:</b> Jaccard Overlap Combined Index (k constant = " + params.getCombinedConstant() + ")<br>";
		}
		
		for (EMDataSet ds : map.getDataSetList()) {
			s = s + "<b>Data Sets</h4><b>";
			s = s + "<b>" + ds.getName() + "</b><br>";
			s = s + "<b>Gene Sets File: </b><br>"
					+ INDENT + shortenPathname(ds.getDataSetFiles().getGMTFileName()) + "<br>";
		
			String enrichmentFileName1 = ds.getDataSetFiles().getEnrichmentFileName1();
			String enrichmentFileName2 = ds.getDataSetFiles().getEnrichmentFileName2();
		
			if (enrichmentFileName1 != null || enrichmentFileName2 != null) {
				s = s + "<b>Data Files: </b><br>";
				
				if (enrichmentFileName1 != null)
					s = s + INDENT + shortenPathname(enrichmentFileName1) + "<br>";
				
				if (enrichmentFileName2 != null)
					s = s + INDENT + shortenPathname(enrichmentFileName2) + "<br>";
			}
			
//			if (LegacySupport.isLegacyTwoDatasets(map)) {
//				enrichmentFileName1 = map.getDataSet(LegacySupport.DATASET2).getDataSetFiles().getEnrichmentFileName1();
//				enrichmentFileName2 = map.getDataSet(LegacySupport.DATASET2).getDataSetFiles().getEnrichmentFileName2();
//				
//				if (enrichmentFileName1 != null || enrichmentFileName2 != null) {
//					s = s + "<b>Dataset 2 Data Files: </b><br>";
//					
//					if (enrichmentFileName1 != null)
//						s = s + INDENT + shortenPathname(enrichmentFileName1) + "<br>";
//					
//					if (enrichmentFileName2 != null)
//						s = s + INDENT + shortenPathname(enrichmentFileName2) + "<br>";
//				}
//			}
			
			s = s + "<b>Data file:</b>" + shortenPathname(ds.getDataSetFiles().getExpressionFileName()) + "<br>";
			// TODO:fix second dataset viewing.
			/*
			 * if(params.isData2() && params.getEM().getExpression(LegacySupport.DATASET2) != null)
			 * runInfoText = runInfoText + "<b>Data file 2:</b>" + shortenPathname(params.getExpressionFileName2()) + "<br>";
			 */
			
			if (ds != null && ds.getDataSetFiles().getGseaHtmlReportFile() != null)
				s = s + "<b>GSEA Report 1:</b>" + shortenPathname(ds.getDataSetFiles().getGseaHtmlReportFile()) + "<br>";
			
//			if (map.getDataSet(LegacySupport.DATASET2) != null
//					&& map.getDataSet(LegacySupport.DATASET2).getDataSetFiles().getGseaHtmlReportFile() != null) {
//				s = s + "<b>GSEA Report 2:</b>"
//						+ shortenPathname(map.getDataSet(LegacySupport.DATASET2).getDataSetFiles().getGseaHtmlReportFile()) + "<br>";
//			}
		}

		s = s + "</font></html>";
		
		return s;
	}

	/**
	 * Create the legend - contains the enrichment score colour mapper and diagram where the colours are
	 */
	private JPanel createLegendPanel(EMCreationParameters params, EnrichmentMap map) {
		JPanel panel = new JPanel();
		
		if (LookAndFeelUtil.isAquaLAF())
			panel.setOpaque(false);
		
		final GroupLayout layout = new GroupLayout(panel);
		panel.setLayout(layout);
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(!LookAndFeelUtil.isAquaLAF());
		
		ParallelGroup hGroup = layout.createParallelGroup(Alignment.CENTER, false);
		SequentialGroup vGroup = layout.createSequentialGroup();
		layout.setHorizontalGroup(layout.createSequentialGroup()
				.addGap(0, 0, Short.MAX_VALUE)
				.addGroup(hGroup)
				.addGap(0, 0, Short.MAX_VALUE)
		);
		layout.setVerticalGroup(vGroup);
		
		// represent the node color as an png/gif instead of using java to generate the representation
		URL nodeIconURL = this.getClass().getResource("node_color_small.png");
		
		if (nodeIconURL != null) {
			ImageIcon nodeIcon;
			nodeIcon = new ImageIcon(nodeIconURL);
			JLabel nodeColorLabel = new JLabel(nodeIcon);
			
			hGroup.addComponent(nodeColorLabel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE);
			vGroup.addComponent(nodeColorLabel).addPreferredGap(ComponentPlacement.RELATED);
		}
		
		// TODO These colors are no longer used
		/* See http://colorbrewer2.org/#type=diverging&scheme=RdBu&n=9 */
		final Color MAX_PHENOTYPE_1 = new Color(178, 24, 43);
		final Color LIGHTER_PHENOTYPE_1 = new Color(214, 96, 77);
		final Color LIGHTEST_PHENOTYPE_1 = new Color(244, 165, 130);
		final Color OVER_COLOR = new Color(247, 247, 247);
		final Color MAX_PHENOTYPE_2 = new Color(33, 102, 172);
		final Color LIGHTER_PHENOTYPE_2 = new Color(67, 147, 195);
		final Color LIGHTEST_PHENOTYPE_2 = new Color(146, 197, 222);
		
		if (map.getDataSet(LegacySupport.DATASET1) != null) {
			ColorLegendPanel nodeLegendPanel = new ColorLegendPanel(
					MAX_PHENOTYPE_1,
					MAX_PHENOTYPE_2,
					map.getDataSet(LegacySupport.DATASET1).getEnrichments().getPhenotype1(),
					map.getDataSet(LegacySupport.DATASET1).getEnrichments().getPhenotype2());
			nodeLegendPanel.setToolTipText("Phenotype * (1-P_value)");
		
			hGroup.addComponent(nodeLegendPanel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE);
			vGroup.addComponent(nodeLegendPanel).addPreferredGap(ComponentPlacement.UNRELATED);
		}

		// If there are two datasets then we need to define the node border legend as well.
		if (LegacySupport.isLegacyTwoDatasets(map)) {
			// represent the node border color as an png/gif instead of using java to generate the representation
			URL nodeborderIconURL = this.getClass().getResource("node_border_color_small.png");

			if (nodeborderIconURL != null) {
				ImageIcon nodeBorderIcon = new ImageIcon(nodeborderIconURL);
				JLabel nodeBorderColorLabel = new JLabel(nodeBorderIcon);
				
				hGroup.addComponent(nodeBorderColorLabel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE);
				vGroup.addComponent(nodeBorderColorLabel).addPreferredGap(ComponentPlacement.RELATED);
			}

			ColorLegendPanel nodeLegendPanel2 = new ColorLegendPanel(
					MAX_PHENOTYPE_1,
					MAX_PHENOTYPE_2,
					map.getDataSet(LegacySupport.DATASET2).getEnrichments().getPhenotype1(),
					map.getDataSet(LegacySupport.DATASET2).getEnrichments().getPhenotype2());
			nodeLegendPanel2.setToolTipText("Phenotype * (1-P_value)");
			
			hGroup.addComponent(nodeLegendPanel2, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE);
			vGroup.addComponent(nodeLegendPanel2).addPreferredGap(ComponentPlacement.UNRELATED);
		}

		return panel;
	}
	
	/**
	 * Shorten path name to only contain the parent directory
	 */
	private String shortenPathname(String pathname) {
		if (pathname != null) {
			String[] tokens = pathname.split("\\" + File.separator);

			int numTokens = tokens.length;
			final String newPathname;
			
			if (numTokens >= 2)
				newPathname = "..." + File.separator + tokens[numTokens - 2] + File.separator + tokens[numTokens - 1];
			else
				newPathname = pathname;

			return newPathname;
		}
		
		return "";
	}

	private String resolveGseaReportFilePath(EnrichmentMap map, int dataset) {
		String reportFile = null;
		ColumnDescriptor<String> colDescr = null;
		
		if (dataset == 1) {
			if (map.getDataSet(LegacySupport.DATASET1) != null) {
				reportFile = map.getDataSet(LegacySupport.DATASET1).getDataSetFiles().getGseaHtmlReportFile();
				colDescr = Columns.NET_REPORT1_DIR;
			}
		} else {
			if (map.getDataSet(LegacySupport.DATASET2) != null) {
				reportFile = map.getDataSet(LegacySupport.DATASET2).getDataSetFiles().getGseaHtmlReportFile();
				colDescr = Columns.NET_REPORT2_DIR;
			}
		}

		// Try the path that is stored in the params:
		if (reportFile != null && new File(reportFile).canRead()) {
			return reportFile;
		} else if (colDescr != null) {
			// if not, try from Network attributes:
			CyNetwork network = applicationManager.getCurrentNetwork();
			CyTable networkTable = network.getDefaultNetworkTable();
			String tryPath = colDescr.get(networkTable.getRow(network.getSUID()));

			String tryReportFile = tryPath + File.separator + "index.html";
			
			if (new File(tryReportFile).canRead()) {
				return tryReportFile;
			} else { // we found nothing
				if (reportFile == null || reportFile.equalsIgnoreCase("null"))
					return null;
				else
					return reportFile;
			}
		} else {
			return null;
		}
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Icon getIcon(VisualProperty<?> vp, Object value) {
		if (value == null)
			return null;
		
		RenderingEngine<CyNetwork> engine = applicationManager.getCurrentRenderingEngine();
		Icon icon = engine.createIcon((VisualProperty)vp, value, LEGEND_ICON_SIZE, LEGEND_ICON_SIZE);
		
		return icon;
	}
}
