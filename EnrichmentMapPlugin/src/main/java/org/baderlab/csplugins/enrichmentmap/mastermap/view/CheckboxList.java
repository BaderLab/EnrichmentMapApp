package org.baderlab.csplugins.enrichmentmap.mastermap.view;

import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.border.Border;

@SuppressWarnings("serial")
public class CheckboxList extends JList<CheckboxData> {

	public CheckboxList(ListModel<CheckboxData> model) {
		setModel(model);
		create();
	}

	private void create() {
		setCellRenderer(new CellRenderer());
		addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				int index = locationToIndex(e.getPoint());
				if(index != -1) {
					CheckboxData checkbox = (CheckboxData) getModel().getElementAt(index);
					checkbox.setSelected(!checkbox.isSelected());
					repaint();
				}
			}
		});
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	}
	
	
	private class CellRenderer implements ListCellRenderer<CheckboxData> {

		private final Border noFocusBorder = BorderFactory.createEmptyBorder(1, 1, 1, 1);
		
		@Override
		public Component getListCellRendererComponent(JList<? extends CheckboxData> list, CheckboxData data, int index,
				boolean isSelected, boolean cellHasFocus) {
			
			JCheckBox checkbox = new JCheckBox(data.getDisplay());
			checkbox.setSelected(data.isSelected());
			checkbox.setBackground(getBackground());
			checkbox.setForeground(getForeground());
			checkbox.setEnabled(isEnabled());
			checkbox.setFont(getFont());
			checkbox.setFocusPainted(false);
			checkbox.setBorderPainted(true);
			checkbox.setBorder(isSelected ? UIManager.getBorder("List.focusCellHighlightBorder") : noFocusBorder);
			
			data.addPropertyChangeListener("selected", new PropertyChangeListener() {
				@Override
				public void propertyChange(PropertyChangeEvent evt) {
					checkbox.setSelected(Boolean.TRUE.equals(evt.getNewValue()));
				}
			});
			
			return checkbox;
		}

	}

}

