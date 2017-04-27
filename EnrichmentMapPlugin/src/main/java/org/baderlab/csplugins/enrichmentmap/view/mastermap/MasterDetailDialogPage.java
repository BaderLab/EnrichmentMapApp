package org.baderlab.csplugins.enrichmentmap.view.mastermap;

import static com.google.common.base.Strings.isNullOrEmpty;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.io.File;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.LayoutStyle;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.border.Border;

import org.baderlab.csplugins.enrichmentmap.model.DataSetFiles;
import org.baderlab.csplugins.enrichmentmap.model.EMCreationParameters;
import org.baderlab.csplugins.enrichmentmap.model.EMCreationParameters.SimilarityMetric;
import org.baderlab.csplugins.enrichmentmap.model.EMDataSet.Method;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentResultFilterParams.NESFilter;
import org.baderlab.csplugins.enrichmentmap.model.LegacySupport;
import org.baderlab.csplugins.enrichmentmap.resolver.DataSetParameters;
import org.baderlab.csplugins.enrichmentmap.resolver.ResolverTask;
import org.baderlab.csplugins.enrichmentmap.task.CreateEnrichmentMapTaskFactory;
import org.baderlab.csplugins.enrichmentmap.view.mastermap.ErrorMessageDialog.MessageType;
import org.baderlab.csplugins.enrichmentmap.view.util.CardDialogCallback;
import org.baderlab.csplugins.enrichmentmap.view.util.CardDialogPage;
import org.baderlab.csplugins.enrichmentmap.view.util.FileBrowser;
import org.baderlab.csplugins.enrichmentmap.view.util.IterableListModel;
import org.baderlab.csplugins.enrichmentmap.view.util.SwingUtil;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.FinishStatus;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.TaskObserver;
import org.cytoscape.work.swing.DialogTaskManager;

import com.google.inject.Inject;
import com.google.inject.Provider;

@SuppressWarnings("serial")
public class MasterDetailDialogPage implements CardDialogPage {

	@Inject private IconManager iconManager;
	@Inject private DialogTaskManager dialogTaskManager;
	
	@Inject private LegacySupport legacySupport;
	@Inject private CutoffPropertiesPanel cutoffPanel;
	@Inject private Provider<EditCommonPanel> commonPanelProvider;
	@Inject private EditDataSetPanel.Factory dataSetPanelFactory;
	@Inject private CreateEnrichmentMapTaskFactory.Factory taskFactoryFactory;
	@Inject private ErrorMessageDialog.Factory errorMessageDialogFactory;
	
	private EditCommonPanel commonPanel;
	private DataSetListItem commonParams;
	
	private DataSetList dataSetList;
	private IterableListModel<DataSetListItem> dataSetListModel;
	private JPanel dataSetDetailPanel;
	private CardLayout cardLayout;
	
	private JButton deleteButton;
	private JButton scanButton;
	
	private JCheckBox distinctEdgesCheckbox;
	private CardDialogCallback callback;
	
	
	@Override
	public String getID() {
		return getClass().getSimpleName();
	}

	@Override
	public String getPageTitle() {
		return "Create Enrichment Map";
	}

	@Override
	public String getPageComboText() {
		return "Master/Detail - Experimental";
	}
	
	@Override
	public void finish() {
		if(!validateInput())
			return;
		
		String prefix = legacySupport.getNextAttributePrefix();
		SimilarityMetric similarityMetric = cutoffPanel.getSimilarityMetric();
		double pvalue = cutoffPanel.getPValue();
		double qvalue = cutoffPanel.getQValue();
		NESFilter nesFilter = cutoffPanel.getNESFilter();
		double cutoff = cutoffPanel.getCutoff();
		double combined = cutoffPanel.getCombinedConstant();
		Optional<Integer> minExperiments = cutoffPanel.getMinimumExperiments();
		
		EMCreationParameters params = 
			new EMCreationParameters(prefix, pvalue, qvalue, nesFilter, minExperiments, similarityMetric, cutoff, combined);
		
		params.setCreateDistinctEdges(distinctEdgesCheckbox.isSelected());
		
		List<DataSetParameters> dataSets = 
				dataSetListModel.toList().stream()
				.map(DataSetListItem::getDetailPanel)
				.map(DetailPanel::createDataSetParameters)
				.filter(x -> x != null)
				.collect(Collectors.toList());
		
		// Overwrite all the expression files if the common file has been provided
		String exprPath = commonPanel.getExpressionFile();
		if(!isNullOrEmpty(exprPath)) {
			for(DataSetParameters dsp : dataSets) {
				dsp.getFiles().setExpressionFileName(exprPath);
			}
		}
		
		// Overwrite all the gmt files if a common file has been provided
		String gmtPath = commonPanel.getGmtFile();
		if(!isNullOrEmpty(gmtPath)) {
			for(DataSetParameters dsp : dataSets) {
				dsp.getFiles().setGMTFileName(gmtPath);
			}
		}
		
		CreateEnrichmentMapTaskFactory taskFactory = taskFactoryFactory.create(params, dataSets);
		TaskIterator tasks = taskFactory.createTaskIterator();
		
		// Close this dialog after the progress dialog finishes normally
		tasks.append(new AbstractTask() {
			public void run(TaskMonitor taskMonitor) {
				callback.close();
			}
		});
		
		dialogTaskManager.execute(tasks);
	}
	
	
	@Override
	public JPanel createBodyPanel(CardDialogCallback callback) {
		this.callback = callback;
		this.commonPanel = commonPanelProvider.get();
		
		commonParams = new DataSetListItem(commonPanel);
				
		JPanel dataPanel = createDataSetPanel();
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(dataPanel, BorderLayout.CENTER);
		panel.add(cutoffPanel, BorderLayout.SOUTH);
		
		updateButtonEnablement();
		return panel;
	}

	
	private JPanel createDataSetPanel() {
		JPanel titlePanel = createTitlePanel();
		
		dataSetListModel = new IterableListModel<>();
		dataSetList = new DataSetList(dataSetListModel);
		dataSetList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		dataSetList.addListSelectionListener(e -> selectItem(dataSetList.getSelectedValue()));
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setViewportView(dataSetList);
		
		dataSetDetailPanel = new JPanel(new BorderLayout());
		dataSetDetailPanel.setBorder(BorderFactory.createLineBorder(UIManager.getColor("Separator.foreground"))); 
		cardLayout = new CardLayout();
		dataSetDetailPanel.setLayout(cardLayout);
		
		// Blank page
		dataSetDetailPanel.add(new EditNothingPanel(), "nothing");
		
		// Common page
		dataSetListModel.addElement(commonParams);
		dataSetDetailPanel.add(commonParams.getDetailPanel().getPanel(), commonParams.id);
		
		distinctEdgesCheckbox = new JCheckBox("Create separate edges for each dataset");
		SwingUtil.makeSmall(distinctEdgesCheckbox);
		
		JPanel checkboxPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
		checkboxPanel.add(distinctEdgesCheckbox);
		
		// Make the NORTH area of both panels the same size
		titlePanel.doLayout();
		checkboxPanel.setPreferredSize(titlePanel.getPreferredSize());
		
		JPanel leftPanel = new JPanel(new BorderLayout());
		leftPanel.add(titlePanel, BorderLayout.NORTH);
		leftPanel.add(scrollPane, BorderLayout.CENTER);
		
		JPanel rightPanel = new JPanel(new BorderLayout());
		rightPanel.add(checkboxPanel, BorderLayout.NORTH);
		rightPanel.add(dataSetDetailPanel, BorderLayout.CENTER);
		
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
		splitPane.setBorder(BorderFactory.createEmptyBorder(2,2,2,2));
		splitPane.setResizeWeight(0.3);
		
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(splitPane, BorderLayout.CENTER);
		
		return panel;
	}
	
	
	private JPanel createTitlePanel() {
		JLabel label = new JLabel("Data Sets:");
		SwingUtil.makeSmall(label);
		
		JButton addButton = SwingUtil.createIconButton(iconManager, IconManager.ICON_PLUS, "Add Data Set");
		scanButton = SwingUtil.createIconButton(iconManager, IconManager.ICON_FOLDER_O, "Scan Folder for Data Sets");
		deleteButton = SwingUtil.createIconButton(iconManager, IconManager.ICON_TRASH_O, "Delete Data Set");
		
		addButton.addActionListener(e -> addNewDataSetToList());
		deleteButton.addActionListener(e -> deleteSelectedItems());
		scanButton.addActionListener(e -> scan());
		
		JPanel panel = new JPanel();
		GroupLayout layout = new GroupLayout(panel);
		panel.setLayout(layout);
		layout.setAutoCreateGaps(true);
		layout.setAutoCreateContainerGaps(false);
		
		layout.setHorizontalGroup(layout.createSequentialGroup()
			.addComponent(label)
			.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
			.addComponent(scanButton)
			.addComponent(addButton)
			.addComponent(deleteButton)
		);
		
		layout.setVerticalGroup(layout.createParallelGroup(Alignment.BASELINE)
			.addComponent(label)
			.addComponent(scanButton)
			.addComponent(addButton)
			.addComponent(deleteButton)
		);
		
		return panel;
	}
	
	
	private void addNewDataSetToList() {
		int n = dataSetListModel.size();
		DataSetParameters params = new DataSetParameters("Data Set " + n, Method.GSEA, new DataSetFiles());
		addDataSetToList(params);
	}
	
	private void addDataSetToList(DataSetParameters params) {
		EditDataSetPanel panel = dataSetPanelFactory.create(params);
		panel.addPropertyChangeListener(EditDataSetPanel.PROP_NAME, e ->
			((IterableListModel<?>)dataSetList.getModel()).update()
		);
		DataSetListItem item = new DataSetListItem(panel);
		dataSetListModel.addElement(item);
		dataSetDetailPanel.add(panel, item.id);
		dataSetList.setSelectedValue(item, true);
	}
	
	
	private void deleteSelectedItems() {
		for(DataSetListItem item : dataSetList.getSelectedValuesList()) {
			if(item != commonParams) {
				dataSetListModel.removeElement(item);
				dataSetDetailPanel.remove(item.getDetailPanel().getPanel());
			}
		}
	}
	
	private void selectItem(DataSetListItem params) {
		cardLayout.show(dataSetDetailPanel, params == null ? "nothing" : params.id);
		updateButtonEnablement();
	}
	
	
	private void updateButtonEnablement() {
		deleteButton.setEnabled(dataSetListModel.getSize() > 0 && dataSetList.getSelectedIndex() > 0);
		callback.setFinishButtonEnabled(dataSetListModel.size() > 1);
	}
	
	
	@Override
	public void extraButtonClicked(String actionCommand) {
		if(MasterMapDialogParameters.RESET_BUTTON_ACTION_COMMAND.equals(actionCommand)) {
			reset();
		}
	}
	
	private void reset() {
		int result = JOptionPane.showConfirmDialog(callback.getDialogFrame(), 
				"Clear inputs and restore default values?", "EnrichmentMap: Reset", JOptionPane.OK_CANCEL_OPTION);
		if(result == JOptionPane.OK_OPTION) {
			distinctEdgesCheckbox.setSelected(false);
			cutoffPanel.reset();
			commonPanel.reset();
			for(DataSetListItem item : dataSetListModel.toList()) {
				if(item != commonParams) {
					dataSetListModel.removeElement(item);
					dataSetDetailPanel.remove(item.getDetailPanel().getPanel());
				}
			}
			callback.setFinishButtonEnabled(false);
		}
	}
	
	private void scan() {
		Optional<File> rootFolder = FileBrowser.browseForRootFolder(callback.getDialogFrame());
		if(rootFolder.isPresent()) {
			scanButton.setEnabled(false);
			ResolverTask task = new ResolverTask(rootFolder.get());
			
			dialogTaskManager.execute(new TaskIterator(task), new TaskObserver() {
				
				@Override
				public void taskFinished(ObservableTask task) {
					@SuppressWarnings("unchecked")
					List<DataSetParameters> datasets = task.getResults(List.class);
					if(!datasets.isEmpty()) {
						datasets.forEach(MasterDetailDialogPage.this::addDataSetToList);
						dataSetList.setSelectedValue(datasets.get(0), true);
					}
				}
				
				@Override
				public void allFinished(FinishStatus finishStatus) {
					scanButton.setEnabled(true);
					updateButtonEnablement();
				}
			});
		}
	}
	
	private boolean validateInput() {
		ErrorMessageDialog dialog = null;
		
		// Check if the user provided a global expression file, warn if there are also per-dataset expression files.
		if(!isNullOrEmpty(commonPanel.getExpressionFile())) {
			for(DataSetListItem item : dataSetListModel.toList()) {
				DetailPanel panel = item.getDetailPanel();
				if(panel instanceof EditDataSetPanel && !isNullOrEmpty(((EditDataSetPanel)panel).getExpressionFileName())) {
					if(dialog == null)
						dialog = errorMessageDialogFactory.create(callback.getDialogFrame());
					List<String> messages = Arrays.asList("A common expression file has been provided. Per-dataset expression files will be ignored.");
					dialog.addSection(MessageType.WARN, commonPanel.getDisplayName(), commonPanel.getIcon(), messages);
					break;
				}
			}
		}
		
		// Check if the user provided a global gmt file, warn if there are also per-dataset gmt files.
		if(!isNullOrEmpty(commonPanel.getGmtFile())) {
			for(DataSetListItem item : dataSetListModel.toList()) {
				DetailPanel panel = item.getDetailPanel();
				if(panel instanceof EditDataSetPanel && !isNullOrEmpty(((EditDataSetPanel)panel).getGMTFileName())) {
					if(dialog == null)
						dialog = errorMessageDialogFactory.create(callback.getDialogFrame());
					List<String> messages = Arrays.asList("A common GMT file has been provided. Per-dataset GMT files will be ignored.");
					dialog.addSection(MessageType.WARN, commonPanel.getDisplayName(), commonPanel.getIcon(), messages);
					break;
				}
			}
		}
		
		// Check for input errors.
		for(DataSetListItem item : dataSetListModel.toList()) {
			DetailPanel panel = item.getDetailPanel();
			List<String> messages = panel.validateInput();
			if(!messages.isEmpty()) {
				if(dialog == null)
					dialog = errorMessageDialogFactory.create(callback.getDialogFrame());
				dialog.addSection(MessageType.ERROR, panel.getDisplayName(), panel.getIcon(), messages);
			}
		}
				
		if(dialog != null) {
			dialog.pack();
			dialog.setLocationRelativeTo(callback.getDialogFrame());
			dialog.setModal(true);
			dialog.setVisible(true);

			// This will always return false if the dialog has error messages. 
			// If the dialog only has warning messages then the user can choose to continue.
			return dialog.shouldContinue();
		}
		
		return true;
	}
	
	
	private static class DataSetListItem {
		private static final Iterator<String> idGenerator = Stream.iterate(0, x -> x + 1).map(String::valueOf).iterator();
		
		public final String id = idGenerator.next();
		public final DetailPanel detailPanel;
		
		public DataSetListItem(DetailPanel detailPanel) {
			this.detailPanel = detailPanel;
		}
		
		public DetailPanel getDetailPanel() {
			return detailPanel;
		}
	}
	
	private class DataSetList extends JList<DataSetListItem> {
		
		@Inject
		public DataSetList(ListModel<DataSetListItem> model) {
			setModel(model);
			setCellRenderer(new CellRenderer());
			setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		}
		
		private class CellRenderer implements ListCellRenderer<DataSetListItem> {

			@Override
			public Component getListCellRendererComponent(JList<? extends DataSetListItem> list,
					DataSetListItem listItem, int index, boolean isSelected, boolean cellHasFocus) {
				
				Color bgColor = UIManager.getColor(isSelected ? "Table.selectionBackground" : "Table.background");
				Color fgColor = UIManager.getColor(isSelected ? "Table.selectionForeground" : "Table.foreground");
				
				DetailPanel detail = listItem.getDetailPanel();
				JLabel iconLabel = new JLabel(" " + detail.getIcon() + "  ");
				iconLabel.setFont(iconManager.getIconFont(13.0f));
				iconLabel.setForeground(fgColor);
				
				String title = detail.getDisplayName();
				JLabel titleLabel = new JLabel(title);
				SwingUtil.makeSmall(titleLabel);
				titleLabel.setForeground(fgColor);
				
				JPanel panel = new JPanel(new BorderLayout());
				panel.add(iconLabel, BorderLayout.WEST);
				panel.add(titleLabel, BorderLayout.CENTER);
				
				panel.setBackground(bgColor);
				
				Border emptyBorder = BorderFactory.createEmptyBorder(2, 4, 2, 4);
				panel.setBorder(emptyBorder);
				
				return panel;
			}
		}
	}
	
}
