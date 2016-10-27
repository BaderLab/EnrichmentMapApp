package org.baderlab.csplugins.enrichmentmap.view.util;

import static org.cytoscape.util.swing.LookAndFeelUtil.isAquaLAF;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.List;
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
	
	
	public CheckboxListPanel() {
		checkboxListModel = new CheckboxListModel<>();
		checkboxList = new CheckboxList<>(checkboxListModel);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setViewportView(checkboxList);
		
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.TRAILING));
		selectAllButton  = new JButton("Select All");
		selectNoneButton = new JButton("Select None");
		
		selectAllButton.addActionListener(e -> {
			checkboxListModel.forEach(cb -> cb.setSelected(true));
			checkboxList.invalidate();
			checkboxList.repaint();
//			updateBuildButton();
		});
		selectNoneButton.addActionListener(e -> {
			checkboxListModel.forEach(cb -> cb.setSelected(false));
			checkboxList.invalidate();
			checkboxList.repaint();
//			updateBuildButton();
		});
		
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
		
		if(isAquaLAF()) {
			selectAllButton.putClientProperty("JButton.buttonType", "gradient");
			selectAllButton.putClientProperty("JComponent.sizeVariant", "small");
			selectNoneButton.putClientProperty("JButton.buttonType", "gradient");
			selectNoneButton.putClientProperty("JComponent.sizeVariant", "small");
		}
		
		LookAndFeelUtil.equalizeSize(selectAllButton, selectNoneButton);
		
		buttonPanel.add(selectAllButton);
		buttonPanel.add(selectNoneButton);
		buttonPanel.setOpaque(false);
		
		setLayout(new BorderLayout());
		add(scrollPane, BorderLayout.CENTER);
		add(buttonPanel, BorderLayout.SOUTH);
	}
	
	private void updateButtons() {
		boolean enabled = !checkboxListModel.isEmpty();
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
}
