package org.baderlab.csplugins.enrichmentmap.view.heatmap.table;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.RenderingHints;

import javax.swing.CellRendererPane;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JToolTip;
import javax.swing.SwingUtilities;
import javax.swing.plaf.basic.BasicToolTipUI;

import org.baderlab.csplugins.enrichmentmap.model.EMDataSet;

/**
 * An extension of JToolTip that supports text wrapping.
 * 
 * @CyAPI.InModule swing-util-api
 */
@SuppressWarnings("serial")
public class GradientLegendToolTip extends JToolTip {
	
	private final GradientLegendToolTipUI ui;
	
	public GradientLegendToolTip(JTable table) {
		ui = new GradientLegendToolTipUI(table);
		setComponent(table);
		updateUI();
	}

	@Override
	public void updateUI() {
		setUI(ui);
	}
}


class GradientLegendToolTipUI extends BasicToolTipUI {

	private final JTable table;
	
	private CellRendererPane rendererPane;
	private JPanel panel;
	
	GradientLegendToolTipUI(JTable table) {
		this.table = table;
		this.panel = new JPanel(new BorderLayout());
	}
	
	@Override
	public void installUI(JComponent c) {
		super.installUI(c);
		rendererPane = new CellRendererPane();
		c.add(rendererPane);
	}

	@Override
	public void uninstallUI(JComponent c) {
		super.uninstallUI(c);
		c.remove(rendererPane);
		rendererPane = null;
	}

	@Override
	public void paint(Graphics g, JComponent c) {
		Dimension size = c.getSize();
		panel.setBackground(c.getBackground());
		var g2 = (Graphics2D) g.create();
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		rendererPane.paintComponent(g2, panel, c, 1, 1, size.width - 1, size.height - 1, true);
		g2.dispose();
	}
	
	@Override
	public Dimension getPreferredSize(JComponent c) {
		Point p = MouseInfo.getPointerInfo().getLocation();
		SwingUtilities.convertPointFromScreen(p, table);
		int row = table.rowAtPoint(p);
		int col = table.columnAtPoint(p);
		
		if(row < 0 || col < HeatMapTableModel.DESC_COL_COUNT) {
			return new Dimension(0, 0);
		}
		
		HeatMapTableModel tableModel = (HeatMapTableModel) table.getModel();
		Object value = tableModel.getValueAt(row, col);
		if(!(value instanceof Double)) {
			return new Dimension(0, 0);
		}
		
		EMDataSet dataset = tableModel.getDataSet(col);
		HeatMapCellRenderer renderer = (HeatMapCellRenderer) table.getCellRenderer(row, col);
		DataSetColorRange colorRange = renderer.getRange(dataset, tableModel.getTransform());
		
		JPanel gradient = new GradientLegendPanel(colorRange);
		JLabel label = new JLabel(getValue(row, col));
        
		panel.removeAll();
		panel.add(label, BorderLayout.NORTH);
		panel.add(gradient, BorderLayout.CENTER);
		
		rendererPane.removeAll();
		rendererPane.add(panel);
		
		return new Dimension(160, 40);
	}

	
	private String getValue(int row, int col) {
		int modelRow = table.convertRowIndexToModel(row);
		int modelCol = table.convertColumnIndexToModel(col);
		
		HeatMapTableModel tableModel = (HeatMapTableModel) table.getModel();
		Object value = tableModel.getValueAt(modelRow, modelCol);
		
		if(value instanceof Number) {
			double d = ((Number)value).doubleValue();
			return HeatMapCellRenderer.getText(d);
		}
		
		return null;
	}
	
	@Override
	public Dimension getMinimumSize(JComponent c) {
		return panel == null ? super.getMinimumSize(c) : panel.getMinimumSize();
	}

	@Override
	public Dimension getMaximumSize(JComponent c) {
		return panel == null ? super.getMaximumSize(c) : panel.getMaximumSize();
	}
}
