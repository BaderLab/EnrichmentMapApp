package org.baderlab.csplugins.enrichmentmap.view.heatmap.table;

import static javax.swing.GroupLayout.DEFAULT_SIZE;

import java.awt.BorderLayout;
import java.util.Objects;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;
import javax.swing.JPanel;
import javax.swing.JTable;

import org.baderlab.csplugins.enrichmentmap.model.EMDataSet;
import org.cytoscape.util.swing.LookAndFeelUtil;
import org.mskcc.colorgradient.ColorGradientWidget;

@SuppressWarnings("serial")
public class GradientLegendPanel extends JPanel {

	public GradientLegendPanel(JTable table) {
		Objects.requireNonNull(table);
		setLayout(new BorderLayout());
		setOpaque(false);
		
		table.getSelectionModel().addListSelectionListener(e -> handleChange(table));
		table.getModel().addTableModelListener(e -> handleChange(table));
	}
	
	private void handleChange(JTable table) {
		int row = table.getSelectedRow();
		int col = table.getSelectedColumn();
		if(row >=0 && col >= HeatMapTableModel.DESC_COL_COUNT) {
			renderLegend(table, row, col);
		} else {
			clear();
		}
	}
	
	private static JPanel createExpressionLegendPanel(DataSetColorRange range) {
		JPanel panel = new JPanel();
		
		GroupLayout layout = new GroupLayout(panel);
		panel.setLayout(layout);
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(!LookAndFeelUtil.isAquaLAF());

		ParallelGroup hGroup = layout.createParallelGroup(Alignment.CENTER, true);
		SequentialGroup vGroup = layout.createSequentialGroup();
		layout.setHorizontalGroup(hGroup);
		layout.setVerticalGroup(vGroup);

		ColorGradientWidget legend = ColorGradientWidget.getInstance("", range.getTheme(),
				range.getRange(), true, ColorGradientWidget.LEGEND_POSITION.NA);

		hGroup.addComponent(legend, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE);
		vGroup.addComponent(legend, 25, 25, 25);
		
		if (LookAndFeelUtil.isAquaLAF())
			panel.setOpaque(false);

		panel.revalidate();
		panel.setOpaque(false);
		return panel;
	}
	
	
	public void clear() {
		removeAll();
		revalidate();
	}
	
	private void renderLegend(JTable table, int row, int col) {
		HeatMapTableModel tableModel = (HeatMapTableModel) table.getModel();
		Object value = tableModel.getValueAt(row, col);
		removeAll();
		
		if(value instanceof Double) {
			EMDataSet dataset = tableModel.getDataSet(col);
			ColorRenderer renderer = (ColorRenderer) table.getCellRenderer(row, col);
			DataSetColorRange colorRange = renderer.getRange(dataset, tableModel.getTransform());
			
			JPanel panel = createExpressionLegendPanel(colorRange);
			
			add(panel, BorderLayout.CENTER);
			revalidate();
		}
	}
	
}
