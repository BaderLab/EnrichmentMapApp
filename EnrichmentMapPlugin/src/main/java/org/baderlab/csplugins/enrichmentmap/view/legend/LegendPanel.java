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
import java.awt.Font;
import java.awt.Paint;
import java.text.Collator;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

import org.baderlab.csplugins.enrichmentmap.model.EMDataSet;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.style.AbstractColumnDescriptor;
import org.baderlab.csplugins.enrichmentmap.style.ChartData;
import org.baderlab.csplugins.enrichmentmap.style.ChartOptions;
import org.baderlab.csplugins.enrichmentmap.style.ChartType;
import org.baderlab.csplugins.enrichmentmap.style.EMStyleBuilder.Colors;
import org.baderlab.csplugins.enrichmentmap.style.EMStyleBuilder.Columns;
import org.baderlab.csplugins.enrichmentmap.style.EMStyleOptions;
import org.baderlab.csplugins.enrichmentmap.view.util.ChartUtil;
import org.baderlab.csplugins.enrichmentmap.view.util.SwingUtil;
import org.cytoscape.util.swing.BasicCollapsiblePanel;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.presentation.property.values.CyColumnIdentifier;
import org.cytoscape.view.presentation.property.values.CyColumnIdentifierFactory;
import org.cytoscape.view.vizmap.VisualMappingFunction;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.mappings.DiscreteMapping;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;

import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * Right hand information Panel containing files uploaded and legends
 */
@Singleton
@SuppressWarnings("serial")
public class LegendPanel extends JPanel {

	private final Border DEF_LEGEND_BORDER = BorderFactory.createLineBorder(UIManager.getColor("Separator.foreground"));
	private final Color DEF_LEGEND_BG = Color.WHITE;
	
	@Inject private LegendContent.Factory legendContentFactory;
	@Inject private VisualMappingManager visualMappingManager;
	@Inject private CyColumnIdentifierFactory columnIdFactory;
	
	private BasicCollapsiblePanel nodeLegendPanel;
	private BasicCollapsiblePanel edgeLegendPanel;
	
	private JPanel nodeColorPanel;
	private JPanel nodeChartColorPanel;
	
	private JPanel nodeShapePanel;
	private JLabel nodeShapeIcon1 = new JLabel();
	private JLabel nodeShapeIcon2 = new JLabel();
	private JLabel nodeShapeDesc1 = new JLabel("Gene Set");
	private JLabel nodeShapeDesc2 = new JLabel("Signature Set");
	
	private JPanel nodeChartPanel;
	private final JPanel chartLegendPanel = new JPanel(new BorderLayout());
	
	private JPanel edgeColorPanel;
	private LegendContent content;


	public LegendPanel() {
		setLayout(new BorderLayout());
		
		chartLegendPanel.setOpaque(false);
		
		Border iconBorder = BorderFactory.createEmptyBorder(4, 4, 4, 4);
		nodeShapeIcon1.setBorder(iconBorder);
		nodeShapeIcon2.setBorder(iconBorder);
		
		makeSmall(nodeShapeDesc1, nodeShapeDesc2);
		equalizeSize(nodeShapeDesc1, nodeShapeDesc2);
	}
	
	public LegendContent getLegendContent() {
		return content;
	}
	
	/**
	 * Update parameters panel based on given enrichment map parameters
	 * @param chartType 
	 */
	void update(EMStyleOptions options, Collection<EMDataSet> filteredDataSets) {
		removeAll();
		content = null;
		
		if (options == null) {
			JLabel infoLabel = new JLabel("No EnrichmentMap View selected");
			infoLabel.setEnabled(false);
			infoLabel.setForeground(UIManager.getColor("Label.disabledForeground"));
			infoLabel.setHorizontalAlignment(JLabel.CENTER);
			infoLabel.setVerticalAlignment(JLabel.CENTER);
			infoLabel.setBorder(new EmptyBorder(120, 40, 120, 40));

			add(infoLabel, BorderLayout.CENTER);
		} else {
			nodeLegendPanel = null;
			nodeColorPanel = null;
			nodeChartColorPanel = null;
			nodeShapePanel = null;
			nodeChartPanel = null;
			edgeLegendPanel = null;
			edgeColorPanel = null;

			content = legendContentFactory.create(options, filteredDataSets);
			
			updateNodeColorPanel();
			updateNodeShapePanel();
			updateNodeChartPanel();
			updateNodeChartColorPanel();
			updateEdgeColorPanel();
			
			JPanel panel = new JPanel();
			final GroupLayout layout = new GroupLayout(panel);
			panel.setLayout(layout);
			layout.setAutoCreateContainerGaps(true);
			layout.setAutoCreateGaps(true);

			layout.setHorizontalGroup(layout.createParallelGroup(Alignment.CENTER, true)
				.addComponent(getNodeLegendPanel(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(getEdgeLegendPanel(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
			);
			layout.setVerticalGroup(layout.createSequentialGroup()
				.addComponent(getNodeLegendPanel(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				.addComponent(getEdgeLegendPanel(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
			);
			
			JScrollPane scrollPane = new JScrollPane(panel);
			add(scrollPane, BorderLayout.CENTER);
		}

		revalidate();
	}

	private void updateNodeColorPanel() {
		JPanel p = getNodeColorPanel();
		p.removeAll();
		
		ColorLegendPanel clp = content.getNodeColorPanel();
		if(clp != null) {
			GroupLayout layout = (GroupLayout) p.getLayout();
	
			layout.setHorizontalGroup(layout.createSequentialGroup()
				.addGap(0, 0, Short.MAX_VALUE)
				.addComponent(clp, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				.addGap(0, 0, Short.MAX_VALUE)
			);
			layout.setVerticalGroup(layout.createParallelGroup(Alignment.CENTER, false)
				.addComponent(clp)
			);
			
			p.setVisible(true);
		} else {
			p.setVisible(false);
		}
	}
	
	private void updateNodeChartColorPanel() {
		JPanel p = getNodeChartColorPanel();
		p.removeAll();
		
		EMStyleOptions options = content.getStyleOptions();
		Collection<EMDataSet> dataSets = content.getFilteredDataSets();
		
		ChartOptions chartOptions = options.getChartOptions();
		ChartData data = chartOptions.getData();
		
		if(data != ChartData.NONE) {
			AbstractColumnDescriptor columnDescriptor = data.getColumnDescriptor();
			List<CyColumnIdentifier> columns = ChartUtil.getSortedColumnIdentifiers(options.getAttributePrefix(),
					dataSets, columnDescriptor , columnIdFactory);
	
			List<Color> colors = ChartUtil.getChartColors(chartOptions);
			List<Double> range = ChartUtil.calculateGlobalRange(options.getNetworkView().getModel(), columns);
			double min = range.get(0) ;
			double max = range.get(1);
			
			String posMaxLabel = max > 0 ? String.format("%.2f", max) : "N/A";
			Color posMaxColor = colors.get(0);
			Color posMinColor = colors.get(colors.size()/2);
			ColorLegendPanel clpPos = new ColorLegendPanel(posMaxColor, posMinColor, posMaxLabel, "0", false);
			JLabel posLabel = new JLabel("Positive");
			SwingUtil.makeSmall(posLabel);
			
			GroupLayout layout = (GroupLayout) p.getLayout();
			SequentialGroup horizontal = layout.createSequentialGroup()
				.addGap(0, 0, Short.MAX_VALUE)
				.addGroup(layout.createParallelGroup(Alignment.CENTER, false)
					.addComponent(posLabel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addComponent(clpPos, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				);
			ParallelGroup vertical = layout.createParallelGroup(Alignment.CENTER, false)
				.addGroup(layout.createSequentialGroup()
					.addComponent(posLabel)
					.addComponent(clpPos)
				);
	
			
			if(data == ChartData.NES_VALUE) { // need to show negative range
				String negMinLabel = min < 0 ? String.format("%.2f", min) : "N/A";
				Color negMaxColor = colors.get(colors.size()-1);
				Color negMinColor = colors.get(colors.size()/2);
				ColorLegendPanel clpNeg = new ColorLegendPanel(negMinColor, negMaxColor, "0", negMinLabel, false);
				JLabel negLabel = new JLabel("Negative");
				SwingUtil.makeSmall(negLabel);
				
				horizontal.addGroup(layout.createParallelGroup(Alignment.CENTER, false)
					.addComponent(negLabel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addComponent(clpNeg, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				);
				vertical.addGroup(layout.createSequentialGroup()
					.addComponent(negLabel)
					.addComponent(clpNeg)
				);
			}
			
			horizontal.addGap(0, 0, Short.MAX_VALUE);
			layout.setHorizontalGroup(horizontal);
			layout.setVerticalGroup(vertical);
			
			p.setVisible(true);
		} else {
			p.setVisible(false);
		}
	}
	
	
	private void updateNodeShapePanel() {
		JPanel p = getNodeShapePanel();
		p.removeAll();
		
		Icon gsIcon  = content.getGeneSetNodeShape();
		Icon sigIcon = content.getSignatureNodeShape();
		
		nodeShapeIcon1.setVisible(gsIcon != null);
		nodeShapeDesc1.setVisible(gsIcon != null);
		nodeShapeIcon1.setIcon(gsIcon);
		
		nodeShapeIcon2.setVisible(sigIcon != null);
		nodeShapeDesc2.setVisible(sigIcon != null);
		nodeShapeIcon2.setIcon(sigIcon);
		
		p.revalidate();
	}
	
	
	private void updateNodeChartPanel() {
		JPanel p = getNodeChartPanel();
		chartLegendPanel.removeAll();
		
		JFreeChart chart = content.getChart();
		if(chart == null) {
			p.setVisible(false);
		} else {
			ChartPanel chartPanel = new ChartPanel(chart);
			chartPanel.setPopupMenu(null);
			chartPanel.setMouseZoomable(false);
			
			JLabel titleLabel = new JLabel(content.getChartLabel());
			titleLabel.setHorizontalAlignment(JLabel.CENTER);
			makeSmall(titleLabel);
			
			chartLegendPanel.add(chartPanel, BorderLayout.CENTER);
			chartLegendPanel.add(titleLabel, BorderLayout.SOUTH);
			
			p.setVisible(true);
		}
		
		p.revalidate();
	}
	
	
	private void updateEdgeColorPanel() {
		JPanel p = getEdgeColorPanel();
		
		EnrichmentMap map = content.getEnrichmentMap();
		EMStyleOptions options = content.getStyleOptions();
		
		CyNetworkView netView = options.getNetworkView();
		VisualStyle style = netView != null ? visualMappingManager.getVisualStyle(netView) : null;
		
		JComponent[][] entries = null;
		
		if (map != null) {
			Dimension iconSize = new Dimension(LegendContent.LEGEND_ICON_SIZE, LegendContent.LEGEND_ICON_SIZE / 2);
			boolean distinctEdges = map.getParams().getCreateDistinctEdges();
			
			if (distinctEdges) {
				VisualMappingFunction<?, Paint> mf =
						style.getVisualMappingFunction(BasicVisualLexicon.EDGE_STROKE_UNSELECTED_PAINT);
				
				if (mf instanceof DiscreteMapping) {
					DiscreteMapping<?, Paint> dm = (DiscreteMapping<?, Paint>) mf;
					final Collator collator = Collator.getInstance();
					
					Map<Object, Paint> dmMap = new TreeMap<>((Object o1, Object o2) -> {
						if (Columns.EDGE_DATASET_VALUE_SIG.equals(o1)) return 1;
						if (Columns.EDGE_DATASET_VALUE_SIG.equals(o2)) return -1;
						return collator.compare("" + o1, "" + o2);
					});
					dmMap.putAll(dm.getAll());
					dmMap.remove(Columns.EDGE_DATASET_VALUE_COMPOUND);
					
					// Special case of 1 dataset with distinct edges and maybe signature genesets as well
					if (map.getDataSetCount() == 1) {
						Paint p1 = dmMap.remove(Columns.EDGE_INTERACTION_VALUE_OVERLAP);
						Paint p2 = dmMap.remove(Columns.EDGE_INTERACTION_VALUE_SIG);
						
						if (p1 != null)
							dmMap.put(map.getDataSetList().iterator().next().getName(), p1);
						if (p2 != null)
							dmMap.put(Columns.EDGE_DATASET_VALUE_SIG, p2);
					}
					
					if (!map.hasSignatureDataSets())
						dmMap.remove(Columns.EDGE_DATASET_VALUE_SIG);
					
					if (dmMap.size() > 0) {
						entries = new JComponent[dmMap.size()][2];
						int i = 0;
						
						for (Entry<?, Paint> e : dmMap.entrySet()) {
							Color color = null;
							
							if (e.getValue() instanceof Color)
								color = (Color) e.getValue();
								
							JLabel iconLabel = createColorLabel(color, iconSize);
							JLabel descLabel = new JLabel("" + e.getKey());
							
							if (Columns.EDGE_DATASET_VALUE_SIG.equals(e.getKey()))
								descLabel.setFont(descLabel.getFont().deriveFont(Font.ITALIC));
							
							entries[i++] = new JComponent[]{ iconLabel, descLabel };
						}
					}
				}
			}
			
			if (entries == null) {
				int rows = map.hasSignatureDataSets() ? 2 : 1;
				entries = new JComponent[rows][2];
				
				{
					JLabel iconLabel = createColorLabel(Colors.COMPOUND_EDGE_COLOR, iconSize);
					JLabel descLabel = new JLabel(Columns.EDGE_DATASET_VALUE_COMPOUND);
					descLabel.setFont(descLabel.getFont().deriveFont(Font.ITALIC));
					
					entries[0] = new JComponent[]{ iconLabel, descLabel };
				}
				
				if (rows == 2) {
					JLabel iconLabel = createColorLabel(Colors.SIG_EDGE_COLOR, iconSize);
					JLabel descLabel = new JLabel(Columns.EDGE_DATASET_VALUE_SIG);
					descLabel.setFont(descLabel.getFont().deriveFont(Font.ITALIC));
					
					entries[1] = new JComponent[]{ iconLabel, descLabel };
				}
			}
		}
		
		updateStyleLegendPanel(entries, p);
	}

	BasicCollapsiblePanel getNodeLegendPanel() {
		if (nodeLegendPanel == null) {
			nodeLegendPanel = new BasicCollapsiblePanel("Nodes (Gene Sets)");
			nodeLegendPanel.setCollapsed(false);
			
			final GroupLayout layout = new GroupLayout(nodeLegendPanel.getContentPane());
			nodeLegendPanel.getContentPane().setLayout(layout);
			layout.setAutoCreateContainerGaps(false);
			layout.setAutoCreateGaps(true);
			
			layout.setHorizontalGroup(layout.createParallelGroup(Alignment.CENTER, true)
					.addComponent(getNodeColorPanel(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
					.addComponent(getNodeShapePanel(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
					.addComponent(getNodeChartPanel(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
					.addComponent(getNodeChartColorPanel(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
			);
			layout.setVerticalGroup(layout.createSequentialGroup()
					.addComponent(getNodeColorPanel(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addComponent(getNodeShapePanel(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addComponent(getNodeChartPanel(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addComponent(getNodeChartColorPanel(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
			);
			
			if (isAquaLAF())
				nodeLegendPanel.setOpaque(false);
		}
		
		return nodeLegendPanel;
	}
	
	BasicCollapsiblePanel getEdgeLegendPanel() {
		if (edgeLegendPanel == null) {
			edgeLegendPanel = new BasicCollapsiblePanel("Edges (Similarity Between Gene Sets)");
			edgeLegendPanel.setCollapsed(false);
			
			final GroupLayout layout = new GroupLayout(edgeLegendPanel.getContentPane());
			edgeLegendPanel.getContentPane().setLayout(layout);
			layout.setAutoCreateContainerGaps(false);
			layout.setAutoCreateGaps(true);
			
			layout.setHorizontalGroup(layout.createParallelGroup(Alignment.CENTER, true)
					.addComponent(getEdgeColorPanel(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
			);
			layout.setVerticalGroup(layout.createSequentialGroup()
					.addComponent(getEdgeColorPanel(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
			);
			
			if (isAquaLAF())
				edgeLegendPanel.setOpaque(false);
		}
		
		return edgeLegendPanel;
	}
	
	private JPanel getNodeColorPanel() {
		if (nodeColorPanel == null) {
			nodeColorPanel = createStyleLegendPanel(null);
			nodeColorPanel.setToolTipText(LegendContent.NODE_COLOR_HEADER);
		}
		
		return nodeColorPanel;
	}
	
	private JPanel getNodeChartColorPanel() {
		if (nodeChartColorPanel == null) {
			nodeChartColorPanel = createStyleLegendPanel(null);
			nodeChartColorPanel.setToolTipText(LegendContent.NODE_CHART_HEADER);
		}
		
		return nodeChartColorPanel;
	}
	
	JPanel getNodeShapePanel() {
		if (nodeShapePanel == null) {
			nodeShapePanel = createStyleLegendPanel(null);
			nodeShapePanel.setToolTipText(LegendContent.NODE_SHAPE_HEADER);
			
			GroupLayout layout = (GroupLayout) nodeShapePanel.getLayout();

			layout.setHorizontalGroup(layout.createSequentialGroup()
					.addGap(0, 0, Short.MAX_VALUE)
					.addComponent(nodeShapeIcon1, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addComponent(nodeShapeDesc1, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addComponent(nodeShapeIcon2, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addComponent(nodeShapeDesc2, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addGap(0, 0, Short.MAX_VALUE)
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
	
	private JPanel getNodeChartPanel() {
		if (nodeChartPanel == null) {
			nodeChartPanel = createStyleLegendPanel(null);
			nodeChartPanel.setToolTipText("Node Charts");
			
			int h = 200;
			EMStyleOptions options = content.getStyleOptions();
			
			if (options.getChartOptions() != null) {
				if (options.getChartOptions().getType() == ChartType.HEAT_STRIPS && options.getDataSets().size() > 4)
					h = 300;
				else if (options.getChartOptions().getType() == ChartType.HEAT_MAP)
					h = Math.max(180, 62 + options.getDataSets().size() * 24);
			}
			
			GroupLayout layout = (GroupLayout) nodeChartPanel.getLayout();

			layout.setHorizontalGroup(layout.createSequentialGroup()
					.addComponent(chartLegendPanel, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
			);
			layout.setVerticalGroup(layout.createSequentialGroup()
					.addComponent(chartLegendPanel, PREFERRED_SIZE, h, h)
			);
		}
		
		return nodeChartPanel;
	}
	
	private JPanel getEdgeColorPanel() {
		if (edgeColorPanel == null) {
			edgeColorPanel = createStyleLegendPanel(null);
			edgeColorPanel.setToolTipText("Edge Stroke Color");
		}
		
		return edgeColorPanel;
	}

	private JPanel createStyleLegendPanel(JComponent[][] entries) {
		JPanel p = new JPanel();
		p.setBorder(DEF_LEGEND_BORDER);
		p.setBackground(DEF_LEGEND_BG);
		
		updateStyleLegendPanel(entries, p);
		
		return p;
	}

	private void updateStyleLegendPanel(JComponent[][] entries, JPanel p) {
		p.removeAll();
		
		GroupLayout layout = new GroupLayout(p);
		p.setLayout(layout);
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(true);
		
		if (entries != null) {
			ParallelGroup hGroup = layout.createParallelGroup(Alignment.LEADING, true);
			SequentialGroup vGroup = layout.createSequentialGroup();
			
			layout.setHorizontalGroup(hGroup);
			layout.setVerticalGroup(vGroup);
			
			for (JComponent[] row : entries) {
				makeSmall(row[0], row[1]);
				
				hGroup.addGroup(layout.createSequentialGroup()
						.addComponent(row[0], PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
						.addComponent(row[1], PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				);
				vGroup.addGroup(layout.createParallelGroup(Alignment.CENTER, false)
						.addComponent(row[0])
						.addComponent(row[1])
				);
			}
		}
		
		p.revalidate();
	}
	
	private JLabel createColorLabel(Color color, Dimension size) {
		JLabel iconLabel = new JLabel();
		iconLabel.setOpaque(color != null);
		iconLabel.setPreferredSize(size);
		iconLabel.setMinimumSize(size);
		
		if (color != null)
			iconLabel.setBackground(color);
		
		return iconLabel;
	}
	
}
