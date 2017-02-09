package org.baderlab.csplugins.enrichmentmap.view.util;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;
import static javax.swing.GroupLayout.Alignment.CENTER;
import static org.cytoscape.util.swing.LookAndFeelUtil.isAquaLAF;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
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
	private JButton addButton;
	private JButton removeButton;
	
	private Optional<Consumer<CheckboxListModel<T>>> addButtonCallback = Optional.empty(); // :)
	
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
		addButton = new JButton("Add...");
		removeButton = new JButton("Remove");
		
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
		addButton.addActionListener(e -> {
			addButtonCallback.ifPresent(cb -> cb.accept(checkboxListModel));
			checkboxList.invalidate();
			checkboxList.repaint();
		});
		removeButton.addActionListener(e -> {
			checkboxList.getSelectedValuesList().forEach(checkboxListModel::removeElement);
			checkboxList.invalidate();
			checkboxList.repaint();
		});
		
		addButton.setVisible(showAddButton);
		removeButton.setVisible(showRemoveButton);
		
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
		
		if (isAquaLAF()) {
			addButton.putClientProperty("JButton.buttonType", "gradient");
			addButton.putClientProperty("JComponent.sizeVariant", "small");
			removeButton.putClientProperty("JButton.buttonType", "gradient");
			removeButton.putClientProperty("JComponent.sizeVariant", "small");
			selectAllButton.putClientProperty("JButton.buttonType", "gradient");
			selectAllButton.putClientProperty("JComponent.sizeVariant", "small");
			selectNoneButton.putClientProperty("JButton.buttonType", "gradient");
			selectNoneButton.putClientProperty("JComponent.sizeVariant", "small");
		}
		
		LookAndFeelUtil.equalizeSize(selectAllButton, selectNoneButton);
		
		final GroupLayout layout = new GroupLayout(this);
		setLayout(layout);
		layout.setAutoCreateContainerGaps(false);
		layout.setAutoCreateGaps(!LookAndFeelUtil.isAquaLAF());
		
   		layout.setHorizontalGroup(layout.createParallelGroup(CENTER, true)
				.addComponent(scrollPane, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addGroup(layout.createSequentialGroup()
						.addComponent(addButton, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
						.addComponent(removeButton, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
						.addGap(20,  20, Short.MAX_VALUE)
						.addComponent(selectAllButton, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
						.addComponent(selectNoneButton, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
   				)
   		);
   		layout.setVerticalGroup(layout.createSequentialGroup()
   				.addComponent(scrollPane, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
   				.addGroup(layout.createParallelGroup(CENTER, false)
						.addComponent(addButton, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
						.addComponent(removeButton, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
						.addComponent(selectAllButton, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
						.addComponent(selectNoneButton, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
   				)
   		);
		
		if (LookAndFeelUtil.isAquaLAF())
			setOpaque(false);
	}
	
	public JButton getAddButton() {
		return addButton;
	}
	
	public JButton getRemoveButton() {
		return removeButton;
	}
	
	public void setAddButtonCallback(Consumer<CheckboxListModel<T>> consumer) {
		addButtonCallback = Optional.ofNullable(consumer);
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
		addButton.setEnabled(enabled);
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
