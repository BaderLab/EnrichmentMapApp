package org.baderlab.csplugins.enrichmentmap.view.util;

import static org.baderlab.csplugins.enrichmentmap.view.util.SwingUtil.makeSmall;

import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;

@SuppressWarnings("serial")
public class CheckboxList<T> extends JList<CheckboxData<T>> {

	public CheckboxList(ListModel<CheckboxData<T>> model) {
		setModel(model);
		create();
	}

	private void create() {
		setCellRenderer(new CellRenderer());
		addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				int index = locationToIndex(e.getPoint());
				
				if (index != -1) {
					CheckboxData<T> checkbox = (CheckboxData<T>) getModel().getElementAt(index);
					checkbox.setSelected(!checkbox.isSelected());
					repaint();
				}
			}
		});
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	}
	
	private class CellRenderer implements ListCellRenderer<CheckboxData<T>> {

		JCheckBox checkbox = new JCheckBox();
		
		CellRenderer() {
			checkbox.setFocusPainted(false);
			checkbox.setBorderPainted(false);
			checkbox.setBackground(UIManager.getColor("Table.background"));
			
			makeSmall(checkbox);
		}
		
		@Override
		public Component getListCellRendererComponent(JList<? extends CheckboxData<T>> list, CheckboxData<T> data,
				int index, boolean isSelected, boolean cellHasFocus) {
			checkbox.setText(data.getDisplay());
			checkbox.setSelected(data.isSelected());
			checkbox.setEnabled(list.isEnabled());
			
			data.addPropertyChangeListener("selected", evt -> {
				checkbox.setSelected(Boolean.TRUE.equals(evt.getNewValue()));
			});
			
			return checkbox;
		}
	}
}
