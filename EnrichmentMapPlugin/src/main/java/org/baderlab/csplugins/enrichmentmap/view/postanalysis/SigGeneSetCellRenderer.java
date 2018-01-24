package org.baderlab.csplugins.enrichmentmap.view.postanalysis;

import java.awt.Component;
import java.text.DecimalFormat;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

import org.baderlab.csplugins.enrichmentmap.view.util.SwingUtil;

public class SigGeneSetCellRenderer implements TableCellRenderer {

	private final DecimalFormat doubleFormat = new DecimalFormat("###.####");
	private final DecimalFormat scientificFormat = new DecimalFormat("0.####E00");

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
		SigGeneSetTableModel model = (SigGeneSetTableModel) table.getModel();
		
		JLabel label = new JLabel();
		label.setOpaque(true); //MUST do this for background to show up.
		SwingUtil.makeSmall(label);
		
		if(col == SigGeneSetTableModel.COL_SIMILARITY && value instanceof Number) {
			double val = ((Number)value).doubleValue();
			label.setToolTipText(String.valueOf(val));
			
			switch(model.getFilterType()) {
				case HYPERGEOM:
				case MANN_WHIT_GREATER:
				case MANN_WHIT_LESS:
				case MANN_WHIT_TWO_SIDED:
					if(!Double.isFinite(val))
						label.setText("#ERR");
					else if(val == 0.0)
						label.setText("0.0");
					else if(val < 0.0001)
						label.setText(scientificFormat.format(val));
					else
						label.setText(doubleFormat.format(val));
					break;
				case NUMBER:
				case SPECIFIC:
					label.setText("" + (int)val);
					break;
				case PERCENT:
					label.setText((int)val + "%");
					break;
				default:
					break;
			}
		} else {
			label.setText(String.valueOf(value));
		}
		
		int modelRow = table.convertRowIndexToModel(row);
		if(!model.getDescriptor(modelRow).passes()) {
			label.setEnabled(false);
		}

		if (isSelected) {
			label.setForeground(table.getSelectionForeground());
			label.setBackground(table.getSelectionBackground());
		} else {
			label.setBackground(table.getBackground());
		}

		return label;
	}

}
