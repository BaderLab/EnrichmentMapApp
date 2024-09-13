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
package org.baderlab.csplugins.enrichmentmap.view.heatmap.table;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.table.TableCellRenderer;

import org.baderlab.csplugins.enrichmentmap.PropertyManager;
import org.baderlab.csplugins.enrichmentmap.model.EMDataSet;
import org.baderlab.csplugins.enrichmentmap.model.PhenotypeHighlight;
import org.baderlab.csplugins.enrichmentmap.model.PhenotypeHighlight.Highlight;
import org.baderlab.csplugins.enrichmentmap.style.EMStyleBuilder;
import org.baderlab.csplugins.enrichmentmap.view.util.SwingUtil;

import com.google.inject.Inject;

/**
 * Flips column headers to vertical position.
 */
@SuppressWarnings("serial")
public class ColumnHeaderVerticalRenderer extends JPanel implements TableCellRenderer {

	public static final int MIN_HEIGHT = 65;
	
	@Inject private PropertyManager propertyManager;
	
	private final Color posColor;
	private final Color negColor;
	private final Color mixColor;
	private final Color defaultColor;
	
	private JLabel verticalLabel;
	private JPanel barPanel;
	
	public interface Factory {
		ColumnHeaderVerticalRenderer create();
	}

	@Inject
	public ColumnHeaderVerticalRenderer() {
		super(new BorderLayout());
		
		posColor = EMStyleBuilder.Colors.HEAT_MAP_HIGHLIGHT_POS;
		negColor = EMStyleBuilder.Colors.HEAT_MAP_HIGHLIGHT_NEG;
		mixColor = EMStyleBuilder.Colors.HEAT_MAP_HIGHLIGHT_MIX;
		defaultColor = UIManager.getColor("TableHeader.background");
		
		verticalLabel = createVerticalLabel();
		
		barPanel = new JPanel();
		barPanel.setPreferredSize(new Dimension(verticalLabel.getWidth(), 5));
		
		add(barPanel, BorderLayout.NORTH);
		add(verticalLabel, BorderLayout.CENTER);
	}
	

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
		var model = (HeatMapTableModel) table.getModel();
		var expressionName = value.toString();
		
		setVerticalText(expressionName);
		
		setToolTipText(getToolTipText(model, col, expressionName));
		
		barPanel.setBackground(getBarColor(model, col));
		setBackground(getBackgroundColor(model, col));
		
		return this;
	}
	
	
	private String abbreviate(String value) {
		int length = propertyManager.getValue(PropertyManager.HEATMAP_NAME_LENGTH);
		return SwingUtil.abbreviate(value, length);
	}
	
	
	private String getToolTipText(HeatMapTableModel model, int col, String expressionName) {
		var pheno = model.getHighlight(col);
		var compress = model.getCompress();
		
		var sb = new StringBuilder("<html>");
		
		if(compress.isDataSet()) {
			List<EMDataSet> datasets;
			if(model.getEnrichmentMap().isCommonExpressionValues()) {
				datasets = new ArrayList<>(model.getDataSetsInCurrentSelection());
			} else {
				datasets = List.of(model.getDataSet(col));
			}
			for (var dataset : datasets) {
				sb.append("<b>Dataset: </b>").append(dataset.getName()).append("<br>");
			}
		} 
		else {
			if(compress.isNone()) {
				sb.append("<b>Expression: </b>").append(expressionName).append("<br>");
			}
			sb.append("<b>Class: </b>").append(pheno.getName()).append("<br>");
			var datasets = getHighightDataSets(model, pheno);
			for(var dataset : datasets) {
				sb.append("<b>Dataset: </b>").append(dataset.getName());
				var highlight = pheno.getHighlight(dataset);
				if(highlight == Highlight.POSITIVE)
					sb.append(" (Positive)");
				else if (highlight == Highlight.NEGATIVE)
					sb.append(" (Negative)");
				sb.append("<br>");
			}
		}
		
		return sb.append("</html>").toString();
	}
	
	
	private Color getBackgroundColor(HeatMapTableModel model, int col) {
		var pheno = model.getHighlight(col);
		if(pheno == null)
			return defaultColor;
		
		var datasets = getHighightDataSets(model, pheno);
		
		boolean pos = false, neg = false;
		for(var dataset : datasets) {
			var highlight = pheno.getHighlight(dataset);
			if(highlight == Highlight.POSITIVE)
				pos = true;
			if(highlight == Highlight.NEGATIVE)
				neg = true;
		}
		
		if(pos && neg)
			return mixColor;
		if(pos)
			return posColor;
		if(neg)
			return negColor;
		
		return defaultColor;
	}
	
	
	private Color getBarColor(HeatMapTableModel model, int col) {
		var pheno = model.getHighlight(col);
		var compress = model.getCompress();
		var defcolor = defaultColor.darker();
		
		if(pheno == null)
			return defcolor;
		
		if(compress.isDataSet()) {
			if(model.getEnrichmentMap().isCommonExpressionValues()) {
				var selectedDataSets = model.getDataSetsInCurrentSelection();
				if(selectedDataSets.size() == 1) {
					return selectedDataSets.iterator().next().getColor();
				}
				return defcolor;
			} else {
				return model.getDataSet(col).getColor();
			}
		}
		
		var datasets = getHighightDataSets(model, pheno);
		
		if(datasets.isEmpty()) {
			return defcolor;
		} 
		if(datasets.size() == 1) {
			var dataset = datasets.iterator().next();
			return dataset.getColor();
		}
		
		return defcolor;
	}
	
	
	private static Set<EMDataSet> getHighightDataSets(HeatMapTableModel model, PhenotypeHighlight pheno) {
		var selectedDataSets = model.getDataSetsInCurrentSelection();
		var datasets = new HashSet<>(pheno.getDatasets());
		datasets.retainAll(selectedDataSets);
		return datasets;
	}
	
	
	private void setVerticalText(String value) {
		String labelText = abbreviate(value);
		Font font = UIManager.getFont("TableHeader.font");
		Color foreground = UIManager.getColor("TableHeader.foreground");
		var icon = new VerticalTextIcon(verticalLabel.getFontMetrics(font), foreground, false, labelText);
		verticalLabel.setIcon(icon);
		verticalLabel.setToolTipText(value);
		
		Dimension prefSize = verticalLabel.getPreferredSize();
		if(prefSize.height < MIN_HEIGHT) {
			verticalLabel.setPreferredSize(new Dimension(prefSize.width, MIN_HEIGHT));
		}
	}
	
	
	private static JLabel createVerticalLabel() {
		JLabel label = new JLabel();
		label.setBorder(UIManager.getBorder("TableHeader.cellBorder"));
		label.setVerticalAlignment(JLabel.BOTTOM);
		label.setHorizontalAlignment(JLabel.CENTER);
		return label;
	}
	
}