package org.baderlab.csplugins.enrichmentmap.view.util;

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
public class CheckboxList<T> extends JList<CheckboxData<T>> {

	public CheckboxList(ListModel<CheckboxData<T>> model) {
		setModel(model);
		create();
	}

	private void create() {
		setCellRenderer(new CellRenderer());
		addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				int index = locationToIndex(e.getPoint());
				if(index != -1) {
					CheckboxData<T> checkbox = (CheckboxData<T>) getModel().getElementAt(index);
					checkbox.setSelected(!checkbox.isSelected());
					repaint();
				}
			}
		});
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	}
	
	
	private class CellRenderer implements ListCellRenderer<CheckboxData<T>> {

		private final Border noFocusBorder = BorderFactory.createEmptyBorder(1, 1, 1, 1);
		
		@Override
		public Component getListCellRendererComponent(JList<? extends CheckboxData<T>> list, CheckboxData<T> data, int index,
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

