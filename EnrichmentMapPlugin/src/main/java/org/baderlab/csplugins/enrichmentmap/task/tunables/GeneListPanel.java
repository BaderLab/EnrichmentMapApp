package org.baderlab.csplugins.enrichmentmap.task.tunables;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;
import static javax.swing.GroupLayout.Alignment.CENTER;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.swing.AbstractAction;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.view.heatmap.GSEALeadingEdgeRankingOption;
import org.baderlab.csplugins.enrichmentmap.view.heatmap.table.RankValue;
import org.baderlab.csplugins.enrichmentmap.view.util.CheckboxData;
import org.baderlab.csplugins.enrichmentmap.view.util.CheckboxList;
import org.baderlab.csplugins.enrichmentmap.view.util.CheckboxListModel;
import org.baderlab.csplugins.enrichmentmap.view.util.SwingUtil;
import org.cytoscape.util.swing.LookAndFeelUtil;

@SuppressWarnings("serial")
public class GeneListPanel extends JPanel {

	private EnrichmentMap map;
	private CheckboxList<String> checkboxList;
	private CheckboxListModel<String> checkboxListModel;
	
	private JButton selectAllButton;
	private JButton selectNoneButton;
	private JButton selectEdgeButton;
	private JButton selectExprButton;
	
	public GeneListPanel(GeneListTunable geneListTunable) {
		this.map = geneListTunable.getEnrichmentMap();
		List<String> genes = geneListTunable.getGenes();
		List<String> selected = geneListTunable.getSelectedGenes();
		List<String> expressionGenes = geneListTunable.getExpressionGenes();
		List<GSEALeadingEdgeRankingOption> leadingEdgeRanks = geneListTunable.getLeadingEdgeRanks();
		
		checkboxListModel = new CheckboxListModel<>();
		genes.stream().sorted().forEach(gene -> {
			boolean sel = selected.isEmpty() || selected.contains(gene);
			checkboxListModel.addElement(new CheckboxData<>(gene, gene, sel));
		});
		
		checkboxList = new CheckboxList<>(checkboxListModel);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setViewportView(checkboxList);
		
		selectAllButton  = new JButton("Select All");
		selectNoneButton = new JButton("Select None");
		selectEdgeButton = new JButton("Select Leading Edge");
		selectExprButton = new JButton("Select Genes With Expressions");
		
		selectAllButton .addActionListener(selectionListener(cb -> cb.setSelected(true)));
		selectNoneButton.addActionListener(selectionListener(cb -> cb.setSelected(false)));
		selectEdgeButton.addActionListener(e -> selectLeadingEdge(genes, leadingEdgeRanks));
		selectExprButton.addActionListener(e -> selectGenesWithExpressions(expressionGenes));
		
		selectAllButton.setEnabled(false);
		selectNoneButton.setEnabled(false);
		selectEdgeButton.setEnabled(true);
		selectEdgeButton.setVisible(!leadingEdgeRanks.isEmpty());
		selectExprButton.setVisible(!expressionGenes.isEmpty());
		
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
		
		SwingUtil.makeSmall(selectAllButton, selectEdgeButton, selectNoneButton, selectExprButton);		
		LookAndFeelUtil.equalizeSize(selectAllButton, selectNoneButton, selectEdgeButton, selectExprButton);
		
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
						.addComponent(selectExprButton, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
   				)
   		);
   		layout.setVerticalGroup(layout.createParallelGroup()
   				.addComponent(scrollPane, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
   				.addGroup(layout.createSequentialGroup()
						.addComponent(selectAllButton)
						.addComponent(selectNoneButton)
						.addGap(20)
						.addComponent(selectEdgeButton)
						.addComponent(selectExprButton)
   				)
   		);
		
		if (LookAndFeelUtil.isAquaLAF())
			setOpaque(false);
		
		updateSelectionButtons();
	}
	
	
	private void selectLeadingEdge(List<String> genes, List<GSEALeadingEdgeRankingOption> leadingEdgeRanks) {
		if(leadingEdgeRanks.size() == 1) {
			selectLeadingEdge(genes, leadingEdgeRanks.get(0));
		} 
		else if(leadingEdgeRanks.size() > 1) {
			JPopupMenu popup = new JPopupMenu();
			
			for(GSEALeadingEdgeRankingOption option : leadingEdgeRanks) {
				popup.add(new JMenuItem(new AbstractAction(option.getName()) {
					@Override
					public void actionPerformed(ActionEvent e) {
						selectLeadingEdge(genes, option);
					}
				}));
			}
			
			popup.show(selectEdgeButton, 0, selectEdgeButton.getHeight());
		}
	}
	
	private void selectLeadingEdge(List<String> genes, GSEALeadingEdgeRankingOption ranks) {
		List<CheckboxData<String>> oldValue = getSelectedData();
		List<Integer> geneKeys = genes.stream().map(map::getHashFromGene).collect(Collectors.toList());
		Map<Integer,RankValue> ranking = ranks.getRanking(geneKeys);
		
		checkboxListModel.forEach(checkBox -> {
			String geneName = checkBox.getData();
			Integer geneKey = map.getHashFromGene(geneName);
			RankValue rank = ranking.get(geneKey);
			boolean selected = rank != null && rank.isSignificant();
			checkBox.setSelected(selected);
		});
		
		fireCheckboxListUpdated(oldValue);
	}
	
	private void selectGenesWithExpressions(List<String> expressionGenes) {
		Set<String> exgSet = new HashSet<>(expressionGenes);
		List<CheckboxData<String>> oldValue = getSelectedData();
		
		checkboxListModel.forEach(checkBox -> {
			String geneName = checkBox.getData();
			boolean selected = exgSet.contains(geneName);
			checkBox.setSelected(selected);
		});
		
		fireCheckboxListUpdated(oldValue);
	}

	private ActionListener selectionListener(Consumer<CheckboxData<String>> action) {
		return e -> {
			List<CheckboxData<String>> oldValue = getSelectedData();
			checkboxListModel.forEach(action);
			fireCheckboxListUpdated(oldValue);
		};
	}
	
	private void fireCheckboxListUpdated(List<CheckboxData<String>> oldValue) {
		checkboxList.invalidate();
		checkboxList.repaint();
		updateSelectionButtons();
		firePropertyChange("selectedData", oldValue, getSelectedData());
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