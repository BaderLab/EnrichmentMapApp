package org.baderlab.csplugins.enrichmentmap.view.postanalysis;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;

import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.model.GeneSet;
import org.baderlab.csplugins.enrichmentmap.model.SetOfGeneSets;
import org.baderlab.csplugins.enrichmentmap.parsers.GMTFileReaderTask;
import org.baderlab.csplugins.enrichmentmap.task.postanalysis.FilterMetric;
import org.baderlab.csplugins.enrichmentmap.util.ResultTaskObserver;
import org.baderlab.csplugins.enrichmentmap.view.creation.NamePanel;
import org.baderlab.csplugins.enrichmentmap.view.util.CardDialogCallback;
import org.baderlab.csplugins.enrichmentmap.view.util.CardDialogPage;
import org.baderlab.csplugins.enrichmentmap.view.util.FileBrowser;
import org.baderlab.csplugins.enrichmentmap.view.util.GBCFactory;
import org.baderlab.csplugins.enrichmentmap.view.util.SwingUtil;
import org.cytoscape.util.swing.FileUtil;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.util.swing.LookAndFeelUtil;
import org.cytoscape.work.FinishStatus;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskObserver;
import org.cytoscape.work.swing.DialogTaskManager;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.assistedinject.Assisted;

public class PADialogPage implements CardDialogPage {

	@Inject private PAWeightPanel.Factory weightPanelFactory;
	@Inject private FileUtil fileUtil;
	@Inject private DialogTaskManager dialogTaskManager;
	@Inject private Provider<JFrame> jFrameProvider;
	@Inject private IconManager iconManager;
	
	private final EnrichmentMap map;
	private FilterMetric filterMetric = new FilterMetric.NoFilter();
	private List<SigGeneSetDescriptor> loadedGeneSets = Collections.emptyList();
	private CardDialogCallback callback;
	
	private NamePanel namePanel;
	private PAWeightPanel weightPanel;
	private JTable table;
	private SigGeneSetTableModel tableModel;
	
	private JButton selectAllButton;
	private JButton selectNoneButton;
//	private JButton filterButton;
	private JLabel statusLabel;
	
	
	public interface Factory {
		PADialogPage create(EnrichmentMap map);
	}
	
	@Inject
	public PADialogPage(@Assisted EnrichmentMap map) {
		this.map = map;
	}
	
	@Override
	public String getID() {
		return getClass().getSimpleName();
	}

	@Override
	public String getPageComboText() {
		return PADialogParameters.TITLE;
	}

	@Override
	public void finish() {

	}
	
	@Override
	public JPanel createBodyPanel(CardDialogCallback callback) {
		this.callback = callback;
		
		JPanel geneSetsPanel = createGeneSetsPanel();
		namePanel = new NamePanel("Data Set Name");
		weightPanel = weightPanelFactory.create(map);
		
		JPanel bottom = new JPanel(new GridBagLayout());
		bottom.add(weightPanel, GBCFactory.grid(0,0).insets(0).weightx(1.0).fill(GridBagConstraints.HORIZONTAL).get());
		bottom.add(namePanel,   GBCFactory.grid(0,1).insets(0).weightx(1.0).fill(GridBagConstraints.HORIZONTAL).get());
		
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(geneSetsPanel, BorderLayout.CENTER);
		panel.add(bottom, BorderLayout.SOUTH);
		
		weightPanel.addPropertyChangeListener(PAWeightPanel.PROPERTY_PARAMETERS, e -> runFilterTask());
		
		return panel;
	}
	
	
	private JPanel createGeneSetsPanel() {
		JPanel panel = new JPanel();
		
		JLabel title = new JLabel("Signature Gene Sets");
		JPanel tablePanel = createTablePanel();
		
		JButton loadFileButton = new JButton("Load from File...");
		JButton loadWebButton = new JButton("Load from Web...");
		selectAllButton = new JButton("Select All");
		selectNoneButton = new JButton("Select None");
//		filterButton = new JButton("Filter...");
		loadWebButton.setEnabled(false);
		statusLabel = new JLabel();
		
		loadFileButton.addActionListener(e -> loadFromFile());
		selectAllButton .addActionListener(e -> tableModel.setAllWanted(true));
		selectNoneButton.addActionListener(e -> tableModel.setAllWanted(false));
//		filterButton.addActionListener(e -> filterSigGeneSets());
		
		SwingUtil.makeSmall(title, loadFileButton, loadWebButton, selectAllButton, selectNoneButton, statusLabel);
		LookAndFeelUtil.equalizeSize(loadFileButton, loadWebButton, selectAllButton, selectNoneButton);
		
		updateTableArea(Collections.emptyList());
		
		final GroupLayout layout = new GroupLayout(panel);
		panel.setLayout(layout);
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(true);
		
		layout.setHorizontalGroup(layout.createParallelGroup()
			.addComponent(title)
			.addGroup(layout.createSequentialGroup()
				.addComponent(tablePanel)
				.addGroup(layout.createParallelGroup()
					.addComponent(loadFileButton)
					.addComponent(loadWebButton)
					.addComponent(selectAllButton)
					.addComponent(selectNoneButton)
//					.addComponent(filterButton)
				)
			)
			.addComponent(statusLabel)
		);
		
		layout.setVerticalGroup(layout.createSequentialGroup()
			.addComponent(title)
			.addGroup(layout.createParallelGroup()
				.addComponent(tablePanel)
				.addGroup(layout.createSequentialGroup()
					.addComponent(loadFileButton)
					.addComponent(loadWebButton)
					.addGap(20)
					.addComponent(selectAllButton)
					.addComponent(selectNoneButton)
					.addGap(20)
//					.addComponent(filterButton)
				)
			)
			.addComponent(statusLabel)
		);
		
		if(LookAndFeelUtil.isAquaLAF())
			panel.setOpaque(false);
		return panel;
	}
	
	
	private JPanel createTablePanel() {
		table = new JTable();
		JScrollPane scrollPane = new JScrollPane(table);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		table.setFillsViewportHeight(true);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		table.setCellSelectionEnabled(false);
		table.setRowSelectionAllowed(true);
		table.setAutoCreateRowSorter(true);
		table.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		
		JTableHeader header = table.getTableHeader();
		header.setReorderingAllowed(false);
		
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(scrollPane, BorderLayout.CENTER);
		panel.setOpaque(false);
		return panel;
	}
	
	
	private void updateSelectionButtons() {
		List<SigGeneSetDescriptor> descriptors = tableModel.getGeneSetDescriptors();
		int selectedCount = (int)descriptors.stream().filter(SigGeneSetDescriptor::isWanted).count();
//		filterButton.setEnabled(true);
		if(descriptors.isEmpty()) {
			selectAllButton.setEnabled(false);
			selectNoneButton.setEnabled(false);
//			filterButton.setEnabled(false);
		} else if(selectedCount == descriptors.size()) {
			selectAllButton.setEnabled(false);
			selectNoneButton.setEnabled(true);
		} else if(selectedCount == 0) {
			selectAllButton.setEnabled(true);
			selectNoneButton.setEnabled(false);
		} else {
			selectAllButton.setEnabled(true);
			selectNoneButton.setEnabled(true);
		}
	}
	
	
	private static List<SigGeneSetDescriptor> createDescriptors(SetOfGeneSets setOfGeneSets) {
		List<SigGeneSetDescriptor> descriptors;
		if(setOfGeneSets == null) {
			descriptors = Collections.emptyList();
		} else {
			descriptors = new ArrayList<>(setOfGeneSets.size());
			for(GeneSet geneSet : setOfGeneSets.getGeneSets().values()) {
				descriptors.add(new SigGeneSetDescriptor(geneSet, 0));
			}
		}
		return descriptors;
	}
	
	
	private void runFilterTask() {
//		filterButton.setEnabled(false);
		SigGeneSetFilterTask task = new SigGeneSetFilterTask(map, loadedGeneSets, filterMetric);
		
		dialogTaskManager.execute(new TaskIterator(task), new TaskObserver() {
			
			@Override
			public void taskFinished(ObservableTask task) {
				@SuppressWarnings("unchecked")
				List<SigGeneSetDescriptor> filteredGeneSets = task.getResults(List.class);
				updateTableArea(filteredGeneSets);
			}
			
			@Override
			public void allFinished(FinishStatus finishStatus) {
//				filterButton.setEnabled(true);
				updateSelectionButtons();
			}
		});
	}
	
	private void updateTableArea(List<SigGeneSetDescriptor> filteredGenesets) {
		tableModel = new SigGeneSetTableModel(filteredGenesets);
		table.setModel(tableModel);
		
		TableColumnModel columnModel = table.getColumnModel();
		columnModel.getColumn(0).setPreferredWidth(100);
		columnModel.getColumn(1).setPreferredWidth(550);
		columnModel.getColumn(2).setPreferredWidth(100);
		columnModel.getColumn(3).setPreferredWidth(100);
		
		tableModel.addTableModelListener(e -> updateSelectionButtons());
		updateSelectionButtons();
		updateStatusLabel();
	}
	
	private void updateStatusLabel() {
		String status;
		if(loadedGeneSets.isEmpty()) {
			status = "";
		} else {
			List<SigGeneSetDescriptor> filteredGenesets = tableModel.getGeneSetDescriptors();
			if(loadedGeneSets.size() == filteredGenesets.size() && loadedGeneSets.size() == 1) {
				status = "1 gene set loaded";
			} else if(loadedGeneSets.size() == filteredGenesets.size()) {
				status = MessageFormat.format("{0} gene sets loaded", loadedGeneSets.size());
			} else if(loadedGeneSets.size() == 1) {
				status = MessageFormat.format("1 gene set loaded, {0} removed from view", filteredGenesets.size());
			} else {
				status = MessageFormat.format("{0} gene sets loaded, {1} removed from view", loadedGeneSets.size(), filteredGenesets.size());
			}
		}
		statusLabel.setText(status);
	}
	
	
	private void loadFromFile() {
		Optional<Path> gmtPath = FileBrowser.browse(fileUtil, callback.getDialogFrame(), FileBrowser.Filter.GMT);
		if(gmtPath.isPresent()) {
			SetOfGeneSets setOfGeneSets = new SetOfGeneSets();
			GMTFileReaderTask gmtTask = new GMTFileReaderTask(map, gmtPath.get().toString(), setOfGeneSets);
			TaskIterator taskIterator = new TaskIterator(gmtTask);
			dialogTaskManager.execute(taskIterator, new ResultTaskObserver() {
				@Override
				public void allFinished(FinishStatus finishStatus) {
					filterMetric = new FilterMetric.NoFilter();
					loadedGeneSets = createDescriptors(setOfGeneSets);
					updateTableArea(loadedGeneSets);
				}
			});
		}
	}
	
	
//	private void filterSigGeneSets() {
//		PAFilterDialog dialog = new PAFilterDialog(jFrameProvider.get(), iconManager, map, filterMetric);
//		Optional<FilterMetric> result = dialog.open();
//		if(result.isPresent()) {
//			this.filterMetric = result.get();
//			runFilterTask();
//		}
//	}

}
