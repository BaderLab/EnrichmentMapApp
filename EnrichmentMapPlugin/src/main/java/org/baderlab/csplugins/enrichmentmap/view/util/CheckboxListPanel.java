package org.baderlab.csplugins.enrichmentmap.view.util;

import static org.cytoscape.util.swing.LookAndFeelUtil.isAquaLAF;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

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
		this(false);
	}
	
	public CheckboxListPanel(boolean addRemoveButtons) {
		checkboxListModel = new CheckboxListModel<>();
		checkboxList = new CheckboxList<>(checkboxListModel);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setViewportView(checkboxList);
		
		selectAllButton  = new JButton("Select All");
		selectNoneButton = new JButton("Select None");
		addButton = new JButton("Add...");
		removeButton = new JButton("Remove");
		
		selectAllButton.addActionListener(e -> {
			checkboxListModel.forEach(cb -> cb.setSelected(true));
			checkboxList.invalidate();
			checkboxList.repaint();
			
		});
		selectNoneButton.addActionListener(e -> {
			checkboxListModel.forEach(cb -> cb.setSelected(false));
			checkboxList.invalidate();
			checkboxList.repaint();
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
		
		addButton.setVisible(addRemoveButtons);
		removeButton.setVisible(addRemoveButtons);
		
		selectAllButton.setEnabled(false);
		selectNoneButton.setEnabled(false);
		
		checkboxListModel.addListDataListener(new ListDataListener() {
			@Override
			public void intervalRemoved(ListDataEvent e) {
				updateButtons();
			}
			@Override
			public void intervalAdded(ListDataEvent e) {
				updateButtons();
			}
			@Override
			public void contentsChanged(ListDataEvent e) {
				updateButtons();
			}
		});
		
		if (isAquaLAF()) {
			selectAllButton.putClientProperty("JButton.buttonType", "gradient");
			selectAllButton.putClientProperty("JComponent.sizeVariant", "small");
			selectNoneButton.putClientProperty("JButton.buttonType", "gradient");
			selectNoneButton.putClientProperty("JComponent.sizeVariant", "small");
		}
		
		LookAndFeelUtil.equalizeSize(selectAllButton, selectNoneButton);
		
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.TRAILING));
		buttonPanel.add(selectAllButton);
		buttonPanel.add(selectNoneButton);
		buttonPanel.setOpaque(false);
		
		setLayout(new BorderLayout());
		add(scrollPane, BorderLayout.CENTER);
		add(buttonPanel, BorderLayout.SOUTH);
		
		if (LookAndFeelUtil.isAquaLAF()) {
			setOpaque(false);
			buttonPanel.setOpaque(false);
		}
	}
	
	public void setAddButtonCallback(Consumer<CheckboxListModel<T>> consumer) {
		this.addButtonCallback = Optional.ofNullable(consumer);
	}
	
	private void updateButtons() {
		boolean enabled = isEnabled() && !checkboxListModel.isEmpty();
		selectAllButton.setEnabled(enabled);
		selectNoneButton.setEnabled(enabled);
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
		updateButtons();
	}
}
