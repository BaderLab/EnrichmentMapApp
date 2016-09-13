package org.baderlab.csplugins.enrichmentmap.mastermap;

import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.border.Border;

@SuppressWarnings("serial")
public class CheckboxList extends JList<JCheckBox> {

	public CheckboxList(ListModel<JCheckBox> model) {
		setModel(model);
		create();
	}

	private void create() {
		setCellRenderer(new CellRenderer());
		addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				int index = locationToIndex(e.getPoint());
				if(index != -1) {
					JCheckBox checkbox = (JCheckBox) getModel().getElementAt(index);
					checkbox.setSelected(!checkbox.isSelected());
					repaint();
				}
			}
		});
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	}
	
	
	private class CellRenderer implements ListCellRenderer<JCheckBox> {

		private final Border noFocusBorder = BorderFactory.createEmptyBorder(1, 1, 1, 1);
		
		@Override
		public Component getListCellRendererComponent(JList<? extends JCheckBox> list, JCheckBox checkbox, int index,
				boolean isSelected, boolean cellHasFocus) {

			checkbox.setBackground(isSelected ? getSelectionBackground() : getBackground());
			checkbox.setForeground(isSelected ? getSelectionForeground() : getForeground());
			checkbox.setEnabled(isEnabled());
			checkbox.setFont(getFont());
			checkbox.setFocusPainted(false);
			checkbox.setBorderPainted(true);
			checkbox.setBorder(isSelected ? UIManager.getBorder("List.focusCellHighlightBorder") : noFocusBorder);
			return checkbox;
		}

	}

}

