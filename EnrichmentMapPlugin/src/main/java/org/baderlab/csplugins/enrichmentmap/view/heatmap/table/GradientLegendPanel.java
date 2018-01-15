package org.baderlab.csplugins.enrichmentmap.view.heatmap.table;

import static javax.swing.GroupLayout.DEFAULT_SIZE;

import java.awt.BorderLayout;
import java.util.Objects;
import java.util.Optional;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;
import javax.swing.JPanel;
import javax.swing.JTable;

import org.baderlab.csplugins.enrichmentmap.model.EMDataSet;
import org.baderlab.csplugins.org.mskcc.colorgradient.ColorGradientWidget;
import org.cytoscape.util.swing.LookAndFeelUtil;

@SuppressWarnings("serial")
public class GradientLegendPanel extends JPanel {
	
	public static final int DEFAULT_HEIGHT = 25;
	private static final int BORDER_WIDTH = 1;

	public GradientLegendPanel(JTable table) {
		Objects.requireNonNull(table);
		setLayout(new BorderLayout());
		setOpaque(false);
		
		table.getSelectionModel().addListSelectionListener(e -> handleChange(table));
		table.getModel().addTableModelListener(e -> handleChange(table));
		setBorder(BorderFactory.createEmptyBorder(BORDER_WIDTH, BORDER_WIDTH, BORDER_WIDTH, BORDER_WIDTH));
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
		layout.setAutoCreateContainerGaps(false);
		layout.setAutoCreateGaps(false);

		ParallelGroup hGroup = layout.createParallelGroup(Alignment.CENTER, true);
		SequentialGroup vGroup = layout.createSequentialGroup();
		layout.setHorizontalGroup(hGroup);
		layout.setVerticalGroup(vGroup);

		ColorGradientWidget legend = ColorGradientWidget.getInstance(null, range.getTheme(),
				range.getRange(), true, ColorGradientWidget.LEGEND_POSITION.NA);

		int h = DEFAULT_HEIGHT - 2 * BORDER_WIDTH;
		
		hGroup.addComponent(legend, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE);
		vGroup.addComponent(legend, h, h, h);
		
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
			HeatMapCellRenderer renderer = (HeatMapCellRenderer) table.getCellRenderer(row, col);
			Optional<DataSetColorRange> colorRange = renderer.getRange(dataset, tableModel.getTransform());
			if(colorRange.isPresent()) {
				JPanel panel = createExpressionLegendPanel(colorRange.get());
				add(panel, BorderLayout.CENTER);
			}
			revalidate();
		}
	}
}
