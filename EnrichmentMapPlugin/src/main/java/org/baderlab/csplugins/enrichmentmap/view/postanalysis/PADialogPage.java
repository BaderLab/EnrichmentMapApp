package org.baderlab.csplugins.enrichmentmap.view.postanalysis;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableRowSorter;

import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.model.PostAnalysisFilterType;
import org.baderlab.csplugins.enrichmentmap.model.SetOfGeneSets;
import org.baderlab.csplugins.enrichmentmap.parsers.GMTFileReaderTask;
import org.baderlab.csplugins.enrichmentmap.task.postanalysis.PAMostSimilarTaskParallel;
import org.baderlab.csplugins.enrichmentmap.view.creation.NamePanel;
import org.baderlab.csplugins.enrichmentmap.view.util.CardDialogCallback;
import org.baderlab.csplugins.enrichmentmap.view.util.CardDialogPage;
import org.baderlab.csplugins.enrichmentmap.view.util.FileBrowser;
import org.baderlab.csplugins.enrichmentmap.view.util.GBCFactory;
import org.baderlab.csplugins.enrichmentmap.view.util.SwingUtil;
import org.cytoscape.util.swing.FileUtil;
import org.cytoscape.util.swing.LookAndFeelUtil;
import org.cytoscape.work.FinishStatus;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskObserver;
import org.cytoscape.work.swing.DialogTaskManager;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

public class PADialogPage implements CardDialogPage {

	@Inject private PAWeightPanel.Factory weightPanelFactory;
	@Inject private FileUtil fileUtil;
	@Inject private DialogTaskManager dialogTaskManager;
	
	private final EnrichmentMap map;
	private SetOfGeneSets loadedGeneSets = new SetOfGeneSets();
	private CardDialogCallback callback;
	
	private NamePanel namePanel;
	private PAWeightPanel weightPanel;
	private JTable table;
	private SigGeneSetTableModel tableModel;
	
	private JButton selectAllButton;
	private JButton selectNoneButton;
	private JButton selectPassedButton;
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
	
	@Override
	public void opened() {
		System.out.println("PADialogPage.opened()");
		weightPanel.updateMannWhitRanks();
	}
	
	private JPanel createGeneSetsPanel() {
		JPanel panel = new JPanel();
		
		JLabel title = new JLabel("Signature Gene Sets");
		JPanel tablePanel = createTablePanel();
		
		JButton loadFileButton = new JButton("Load from File...");
		JButton loadWebButton = new JButton("Load from Web...");
		selectAllButton = new JButton("Select All");
		selectNoneButton = new JButton("Select None");
		selectPassedButton = new JButton("Select Passing");
		loadWebButton.setEnabled(false);
		statusLabel = new JLabel();
		
		loadFileButton.addActionListener(e -> loadFromFile());
		
		selectAllButton .addActionListener(e -> {
			tableModel.setAllWanted(true);
			updateStatusLabel();
		});
		selectNoneButton.addActionListener(e -> {
			tableModel.setAllWanted(false);
			updateStatusLabel();
		});
		selectPassedButton.addActionListener(e -> {
			tableModel.setPassedWanted();
			updateStatusLabel();
		});
		
		SwingUtil.makeSmall(title, loadFileButton, loadWebButton, selectAllButton, selectNoneButton, statusLabel, selectPassedButton);
		LookAndFeelUtil.equalizeSize(loadFileButton, loadWebButton, selectAllButton, selectNoneButton, selectPassedButton);
		
		updateTableArea(Collections.emptyList(), PostAnalysisFilterType.NO_FILTER, false);
		
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
					.addComponent(selectPassedButton)
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
					.addGap(30)
					.addComponent(selectAllButton)
					.addComponent(selectNoneButton)
					.addComponent(selectPassedButton)
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
		
		table.setDefaultRenderer(String.class, new SigGeneSetCellRenderer());
		table.setDefaultRenderer(Integer.class, new SigGeneSetCellRenderer());
		table.setDefaultRenderer(Double.class, new SigGeneSetCellRenderer());
		
		JTableHeader header = table.getTableHeader();
		header.setReorderingAllowed(false);
		
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(scrollPane, BorderLayout.CENTER);
		panel.setOpaque(false);
		return panel;
	}
	
	
	private TaskObserver filterTaskObserver = new TaskObserver() {
		@Override
		@SuppressWarnings("unchecked")
		public void taskFinished(ObservableTask task) {
			if(task instanceof GMTFileReaderTask) {
				loadedGeneSets = task.getResults(SetOfGeneSets.class);
			}
			if(task instanceof PAMostSimilarTaskParallel) {
				List<SigGeneSetDescriptor> filteredGeneSets = task.getResults(List.class);
				PostAnalysisFilterType filterType = task.getResults(PostAnalysisFilterType.class);
				for(SigGeneSetDescriptor descriptor : filteredGeneSets) {
					descriptor.setWanted(descriptor.passes());
				}
				updateTableArea(filteredGeneSets, filterType, true);
			}
		}
		
		@Override
		public void allFinished(FinishStatus finishStatus) { }
	};

	
	private void runFilterTask() {
		PAMostSimilarTaskParallel task = new PAMostSimilarTaskParallel(map, loadedGeneSets, weightPanel.getResults());
		dialogTaskManager.execute(new TaskIterator(task), filterTaskObserver);
	}
	
	
	private void loadFromFile() {
		Optional<Path> gmtPath = FileBrowser.browse(fileUtil, callback.getDialogFrame(), FileBrowser.Filter.GMT);
		if(gmtPath.isPresent()) {
			SetOfGeneSets setOfGeneSets = new SetOfGeneSets();
			GMTFileReaderTask gmtTask = new GMTFileReaderTask(map, gmtPath.get().toString(), setOfGeneSets);
			PAMostSimilarTaskParallel filterTask = new PAMostSimilarTaskParallel(map, setOfGeneSets, weightPanel.getResults());
			dialogTaskManager.execute(new TaskIterator(gmtTask, filterTask), filterTaskObserver);
		}
	}
		
	
	private synchronized void updateTableArea(List<SigGeneSetDescriptor> filteredGenesets, PostAnalysisFilterType type, boolean preserveWidths) {
		System.out.println("PADialogPage.updateTableArea()");
		TableColumnModel columnModel = table.getColumnModel();
		
		int[] widths = {60, 510, 80, 230};
		if(preserveWidths) {
			widths = new int[] {
				columnModel.getColumn(0).getWidth(),
				columnModel.getColumn(1).getWidth(),
				columnModel.getColumn(2).getWidth(),
				columnModel.getColumn(3).getWidth()
			};
		} 
		
		table.setModel(tableModel = new SigGeneSetTableModel(filteredGenesets, type));
		
		columnModel.getColumn(0).setPreferredWidth(widths[0]);
		columnModel.getColumn(1).setPreferredWidth(widths[1]);
		columnModel.getColumn(2).setPreferredWidth(widths[2]);
		columnModel.getColumn(3).setPreferredWidth(widths[3]);
		
		// Auto-sort
		TableRowSorter<?> sorter = ((TableRowSorter<?>)table.getRowSorter());
		RowSorter.SortKey sortKey = new RowSorter.SortKey(SigGeneSetTableModel.COL_OVERLAP, getSortOrder(type));
		sorter.setSortKeys(Arrays.asList(sortKey));
		sorter.sort();
		
		updateStatusLabel();
	}
	
	
	private static SortOrder getSortOrder(PostAnalysisFilterType type) {
		return (type.isMannWhitney() || type == PostAnalysisFilterType.HYPERGEOM) ? SortOrder.ASCENDING : SortOrder.DESCENDING;
	}
	
	
	private void updateStatusLabel() {
		String status;
		if(loadedGeneSets.isEmpty()) {
			status = "";
		} else {
			int loaded = tableModel.getRowCount();
			int passed = tableModel.getPassedCount();
			int selected = tableModel.getSelectedCount();
			
			StringBuilder sb = new StringBuilder();
			sb.append(loaded).append(" gene sets loaded, ");
			sb.append(passed).append(" passed cutoff, ");
			sb.append(selected).append(" selected for import");
			status = sb.toString();
		}
		statusLabel.setText(status);
	}

}
