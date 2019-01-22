package org.baderlab.csplugins.enrichmentmap.view.util;

import static org.baderlab.csplugins.enrichmentmap.view.util.SwingUtil.makeSmall;

import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

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
		setCellRenderer(new CheckBoxCellRenderer());
		
		addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				int index = locationToIndex(e.getPoint());
				
				if (index != -1) {
					if (getSelectionModel().isSelectedIndex(index))
						getSelectionModel().clearSelection();
					
					CheckboxData<T> data = (CheckboxData<T>) getModel().getElementAt(index);
					data.setSelected(!data.isSelected());
					repaint();
				}
			}
		});
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	}
	
	public void selectAll() {
	    int end = getModel().getSize() - 1;
	    if(end >= 0) {
	    	setSelectionInterval(0, end);
	    }
	}
	
	public void selectNone() {
		clearSelection();
	}
	
	public List<T> getSelectedData() {
		List<T> list = new ArrayList<>();
		int size = getModel().getSize();
		
		for (int i = 0; i < size; i++) {
			CheckboxData<T> cb = (CheckboxData<T>) getModel().getElementAt(i);
			if (cb.isSelected()) {
				list.add(cb.getData());
			}
		}
		
		return list;
	}

	private class CheckBoxCellRenderer implements ListCellRenderer<CheckboxData<T>> {

		final JCheckBox checkbox = new JCheckBox();
		final SelectedPropertyChangeListener selectedListener = new SelectedPropertyChangeListener();
		
		CheckBoxCellRenderer() {
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
			
			data.removePropertyChangeListener("selected", selectedListener);
			data.addPropertyChangeListener("selected", selectedListener);
			
			return checkbox;
		}
		
		private class SelectedPropertyChangeListener implements PropertyChangeListener {

			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				checkbox.setSelected(Boolean.TRUE.equals(evt.getNewValue()));
			}
		}
	}
}