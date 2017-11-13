package org.baderlab.csplugins.enrichmentmap.view.util;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.Alignment.CENTER;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import org.cytoscape.util.swing.LookAndFeelUtil;

@SuppressWarnings("serial")
public class CheckboxListPanel<T> extends JPanel {

	private CheckboxList<T> checkboxList;
	private CheckboxListModel<T> checkboxListModel;
	private JButton selectAllButton;
	private JButton selectNoneButton;

	public CheckboxListPanel() {
		this(false, false);
	}
	
	public CheckboxListPanel(boolean showAddButton, boolean showRemoveButton) {
		checkboxListModel = new CheckboxListModel<>();
		checkboxList = new CheckboxList<>(checkboxListModel);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setViewportView(checkboxList);
		
		selectAllButton  = new JButton("Select All");
		selectNoneButton = new JButton("Select None");
		
		selectAllButton.addActionListener(e -> {
			List<CheckboxData<T>> oldValue = getSelectedData();
			checkboxListModel.forEach(cb -> cb.setSelected(true));
			checkboxList.invalidate();
			checkboxList.repaint();
			updateSelectionButtons();
			firePropertyChange("selectedData", oldValue, getSelectedData());
		});
		selectNoneButton.addActionListener(e -> {
			List<CheckboxData<T>> oldValue = getSelectedData();
			checkboxListModel.forEach(cb -> cb.setSelected(false));
			checkboxList.invalidate();
			checkboxList.repaint();
			updateSelectionButtons();
			firePropertyChange("selectedData", oldValue, Collections.emptyList());
		});
		
		selectAllButton.setEnabled(false);
		selectNoneButton.setEnabled(false);
		
		checkboxListModel.addListDataListener(new ListDataListener() {
			@Override
			public void intervalRemoved(ListDataEvent e) {
				updateSelectionButtons();
			}
			@Override
			public void intervalAdded(ListDataEvent e) {
				updateSelectionButtons();
			}
			@Override
			public void contentsChanged(ListDataEvent e) {
				updateSelectionButtons();
			}
		});
		
		checkboxList.addListSelectionListener(evt -> {
			if (!evt.getValueIsAdjusting()) {
				updateSelectionButtons();
				firePropertyChange("selectedData", null, getSelectedData());
			}
		});
		
		SwingUtil.makeSmall(selectAllButton, selectNoneButton);
		LookAndFeelUtil.equalizeSize(selectAllButton, selectNoneButton);
		
		final GroupLayout layout = new GroupLayout(this);
		setLayout(layout);
		layout.setAutoCreateContainerGaps(false);
		layout.setAutoCreateGaps(!LookAndFeelUtil.isAquaLAF());
		
   		layout.setHorizontalGroup(layout.createParallelGroup(CENTER, true)
			.addComponent(scrollPane, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
   		);
   		layout.setVerticalGroup(layout.createSequentialGroup()
   			.addComponent(scrollPane, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
   		);
		
		if (LookAndFeelUtil.isAquaLAF())
			setOpaque(false);
	}
	
	public JButton getSelectAllButton() {
		return selectAllButton;
	}
	
	public JButton getSelectNoneButton() {
		return selectNoneButton;
	}
	
	private void updateSelectionButtons() {
		boolean enabled = isEnabled() && !checkboxListModel.isEmpty();
		List<CheckboxData<T>> selectedData = getSelectedData();
		
		selectAllButton.setEnabled(enabled && selectedData.size() < checkboxListModel.size());
		selectNoneButton.setEnabled(enabled && selectedData.size() > 0);
	}
	
	public CheckboxList<T> getCheckboxList() {
		return checkboxList;
	}
	
	public CheckboxListModel<T> getModel() {
		return checkboxListModel;
	}
	
	public List<T> getSelectedDataItems() {
		return checkboxListModel.stream()
				.filter(CheckboxData::isSelected)
				.map(CheckboxData::getData)
				.collect(Collectors.toList());
	}
	
	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		
		checkboxList.setEnabled(enabled);
		updateSelectionButtons();
	}
	
	public List<CheckboxData<T>> getSelectedData() {
		List<CheckboxData<T>> list = new ArrayList<>();
		int size = getModel().getSize();
		
		for (int i = 0; i < size; i++) {
			CheckboxData<T> data = (CheckboxData<T>) getModel().getElementAt(i);
			
			if (data.isSelected())
				list.add(data);
		}
		
		return list;
	}
}
