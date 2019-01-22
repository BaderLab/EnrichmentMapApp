package org.baderlab.csplugins.enrichmentmap.task.tunables;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;
import static javax.swing.GroupLayout.Alignment.CENTER;

import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import org.baderlab.csplugins.enrichmentmap.view.util.CheckboxData;
import org.baderlab.csplugins.enrichmentmap.view.util.CheckboxList;
import org.baderlab.csplugins.enrichmentmap.view.util.CheckboxListModel;
import org.baderlab.csplugins.enrichmentmap.view.util.SwingUtil;
import org.cytoscape.util.swing.LookAndFeelUtil;

@SuppressWarnings("serial")
public class GeneListPanel extends JPanel {

	private CheckboxList<String> checkboxList;
	private CheckboxListModel<String> checkboxListModel;
	
	private JButton selectAllButton;
	private JButton selectNoneButton;
	private JButton selectEdgeButton;
	
	public GeneListPanel(List<String> genes, Set<String> leadingEdge) {
		checkboxListModel = new CheckboxListModel<>();
		genes.stream().sorted().forEach(gene -> {
			checkboxListModel.addElement(new CheckboxData<>(gene, gene, true));
		});
		
		checkboxList = new CheckboxList<>(checkboxListModel);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setViewportView(checkboxList);
		
		selectAllButton  = new JButton("Select All");
		selectNoneButton = new JButton("Select None");
		selectEdgeButton = new JButton("Select Leading Edge");
		
		selectAllButton .addActionListener(selectionListener(cb -> cb.setSelected(true)));
		selectNoneButton.addActionListener(selectionListener(cb -> cb.setSelected(false)));
		selectEdgeButton.addActionListener(selectionListener(cb -> cb.setSelected(leadingEdge.contains(cb.getData()))));
		
		selectAllButton.setEnabled(false);
		selectNoneButton.setEnabled(false);
		selectEdgeButton.setEnabled(true);
		selectEdgeButton.setVisible(!leadingEdge.isEmpty());
		
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
		
		SwingUtil.makeSmall(selectAllButton, selectEdgeButton, selectNoneButton);		
		LookAndFeelUtil.equalizeSize(selectAllButton, selectNoneButton, selectEdgeButton);
		
		final GroupLayout layout = new GroupLayout(this);
		setLayout(layout);
		layout.setAutoCreateContainerGaps(false);
		layout.setAutoCreateGaps(!LookAndFeelUtil.isAquaLAF());
		
   		layout.setHorizontalGroup(layout.createSequentialGroup()
				.addComponent(scrollPane, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addGroup(layout.createParallelGroup(CENTER, true)
						.addComponent(selectAllButton, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
						.addComponent(selectNoneButton, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
						.addComponent(selectEdgeButton, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
   				)
   		);
   		layout.setVerticalGroup(layout.createParallelGroup()
   				.addComponent(scrollPane, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
   				.addGroup(layout.createSequentialGroup()
						.addComponent(selectAllButton)
						.addComponent(selectNoneButton)
						.addGap(20)
						.addComponent(selectEdgeButton)
   				)
   		);
		
		if (LookAndFeelUtil.isAquaLAF())
			setOpaque(false);
		
		updateSelectionButtons();
	}
	
	
	private ActionListener selectionListener(Consumer<CheckboxData<String>> action) {
		return e -> {
			List<CheckboxData<String>> oldValue = getSelectedData();
			checkboxListModel.forEach(action);
			checkboxList.invalidate();
			checkboxList.repaint();
			updateSelectionButtons();
			firePropertyChange("selectedData", oldValue, getSelectedData());
		};
	}
	
	private void updateSelectionButtons() {
		boolean enabled = isEnabled() && !checkboxListModel.isEmpty();
		List<CheckboxData<String>> selectedData = getSelectedData();
		
		selectAllButton.setEnabled(enabled && selectedData.size() < checkboxListModel.size());
		selectNoneButton.setEnabled(enabled && selectedData.size() > 0);
	}
	
	public CheckboxList<String> getCheckboxList() {
		return checkboxList;
	}
	
	public CheckboxListModel<String> getModel() {
		return checkboxListModel;
	}
	
	public List<String> getSelectedDataItems() {
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
	
	public List<CheckboxData<String>> getSelectedData() {
		List<CheckboxData<String>> list = new ArrayList<>();
		int size = getModel().getSize();
		
		for (int i = 0; i < size; i++) {
			CheckboxData<String> data = (CheckboxData<String>) getModel().getElementAt(i);
			
			if (data.isSelected())
				list.add(data);
		}
		
		return list;
	}
}