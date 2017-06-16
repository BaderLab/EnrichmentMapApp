package org.baderlab.csplugins.enrichmentmap.view.creation;

import static com.google.common.base.Strings.isNullOrEmpty;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JFrame;
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

import org.baderlab.csplugins.enrichmentmap.PropertyManager;
import org.baderlab.csplugins.enrichmentmap.model.DataSetFiles;
import org.baderlab.csplugins.enrichmentmap.model.EMCreationParameters;
import org.baderlab.csplugins.enrichmentmap.model.EMCreationParameters.EdgeStrategy;
import org.baderlab.csplugins.enrichmentmap.model.EMCreationParameters.SimilarityMetric;
import org.baderlab.csplugins.enrichmentmap.model.EMDataSet.Method;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentResultFilterParams.NESFilter;
import org.baderlab.csplugins.enrichmentmap.model.LegacySupport;
import org.baderlab.csplugins.enrichmentmap.resolver.DataSetParameters;
import org.baderlab.csplugins.enrichmentmap.resolver.ResolverTask;
import org.baderlab.csplugins.enrichmentmap.task.CreateEnrichmentMapTaskFactory;
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
	@Inject private Provider<JFrame> jframeProvider;
	@Inject private FileBrowser fileBrowser;
	
	@Inject private LegacySupport legacySupport;
	@Inject private CutoffPropertiesPanel cutoffPanel;
	@Inject private Provider<EditCommonPanel> commonPanelProvider;
	@Inject private EditDataSetPanel.Factory dataSetPanelFactory;
	@Inject private CreateEnrichmentMapTaskFactory.Factory taskFactoryFactory;
	@Inject private ErrorMessageDialog.Factory errorMessageDialogFactory;
	@Inject private PropertyManager propertyManager;
	
	private EditCommonPanel commonPanel;
	private DataSetListItem commonParams;
	
	private DataSetList dataSetList;
	private IterableListModel<DataSetListItem> dataSetListModel;
	private JPanel dataSetDetailPanel;
	private CardLayout cardLayout;
	
	private JButton deleteButton;
	private JButton scanButton;
	
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
		EdgeStrategy edgeStrategy = cutoffPanel.getEdgeStrategy();
		
		EMCreationParameters params = 
			new EMCreationParameters(prefix, pvalue, qvalue, nesFilter, minExperiments, similarityMetric, cutoff, combined, edgeStrategy);
		
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
		
//		System.out.println(params);
		
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
		dataSetList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
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
		
		JPanel leftPanel = new JPanel(new BorderLayout());
		leftPanel.add(titlePanel, BorderLayout.NORTH);
		leftPanel.add(scrollPane, BorderLayout.CENTER);
		
		JPanel rightPanel = new JPanel(new BorderLayout());
		rightPanel.add(dataSetDetailPanel, BorderLayout.CENTER);
		
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
		splitPane.setBorder(BorderFactory.createEmptyBorder(2,2,2,2));
		splitPane.setResizeWeight(0.2);
		
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(splitPane, BorderLayout.CENTER);
		
		return panel;
	}
	
	private JPanel createTitlePanel() {
		JLabel label = new JLabel("Data Sets:");
		SwingUtil.makeSmall(label);
		
		JButton addButton = SwingUtil.createIconButton(iconManager, IconManager.ICON_PLUS, "Add data set from files");
		scanButton = SwingUtil.createIconButton(iconManager, IconManager.ICON_FOLDER_O, "Add data sets from folder (scan)");
		deleteButton = SwingUtil.createIconButton(iconManager, IconManager.ICON_TRASH_O, "Delete selected data sets");
		
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
		dataSetDetailPanel.revalidate();
		updateButtonEnablement();
	}
	
	
	private void updateButtonEnablement() {
		deleteButton.setEnabled(dataSetListModel.getSize() > 0 && dataSetList.getSelectedIndex() > 0);
		callback.setFinishButtonEnabled(dataSetListModel.size() > 1);
	}
	
	
	@Override
	public void extraButtonClicked(String actionCommand) {
		if(CreationDialogParameters.RESET_BUTTON_ACTION_COMMAND.equals(actionCommand)) {
			reset();
		}
	}
	
	private void reset() {
		int result = JOptionPane.showConfirmDialog(callback.getDialogFrame(), 
				"Clear inputs and restore default values?", "EnrichmentMap: Reset", JOptionPane.OK_CANCEL_OPTION);
		if(result == JOptionPane.OK_OPTION) {
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
		Optional<File> rootFolder = fileBrowser.browseForRootFolder(jframeProvider.get());
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
	
	
	public EditCommonPanel getCommonPanel() {
		return commonPanel;
	}
	
	private boolean validateInput() {
		ErrorMessageDialog dialog = errorMessageDialogFactory.create(callback.getDialogFrame());
		
		// Check if the user provided a global expression file, warn if there are also per-dataset expression files.
		if(commonPanel.hasExpressionFile()) {
			for(DataSetListItem item : dataSetListModel.toList()) {
				DetailPanel panel = item.getDetailPanel();
				if(panel instanceof EditDataSetPanel && !isNullOrEmpty(((EditDataSetPanel)panel).getExpressionFileName())) {
					String message = "A common expression file has been provided. Per-dataset expression files will be ignored.";
					dialog.addSection(Message.warn(message), commonPanel.getDisplayName(), commonPanel.getIcon());
					break;
				}
			}
		}
		
		// Check if the user provided a global gmt file, warn if there are also per-dataset gmt files.
		if(commonPanel.hasGmtFile()) {
			for(DataSetListItem item : dataSetListModel.toList()) {
				DetailPanel panel = item.getDetailPanel();
				if(panel instanceof EditDataSetPanel && !isNullOrEmpty(((EditDataSetPanel)panel).getGMTFileName())) {
					String message = "A common GMT file has been provided. Per-dataset GMT files will be ignored.";
					dialog.addSection(Message.warn(message), commonPanel.getDisplayName(), commonPanel.getIcon());
					break;
				}
			}
		}
		
		{ // Check for duplicate data set names
			Map<String,Long> dataSetNameCount = dataSetListModel.stream()
				.map(DataSetListItem::getDetailPanel)
				.filter(panel -> panel instanceof EditDataSetPanel)
				.map(panel -> (EditDataSetPanel)panel)
				.map(EditDataSetPanel::getDataSetName)
				.collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
			
			List<Message> messages = new ArrayList<>();
			dataSetNameCount.forEach((name,count) -> {
				if(count > 1) {
					messages.add(Message.error("Duplicate data set name:  '" + name + "'"));
				}
			});
			
			if(!messages.isEmpty()) {
				dialog.addSection(messages, "Duplicate Data Set Names", commonPanel.getIcon());
			}
		}
		
		// Check for input errors.
		for(DataSetListItem item : dataSetListModel.toList()) {
			DetailPanel panel = item.getDetailPanel();
			List<Message> messages = panel.validateInput(this);
			if(!messages.isEmpty()) {
				dialog.addSection(messages, panel.getDisplayName(), panel.getIcon());
			}
		}
			
		if(dialog.isEmpty())
			return true;
		if(!dialog.hasErrors() && !propertyManager.getShowCreateWarnings())
			return true;
		
		dialog.pack();
		dialog.setLocationRelativeTo(callback.getDialogFrame());
		dialog.setModal(true);
		dialog.setVisible(true);
		// This will always return false if the dialog has error messages. 
		// If the dialog only has warning messages then the user can choose to continue.
		boolean shouldContinue = dialog.shouldContinue();
		if(dialog.isDontWarnAgain()) {
			propertyManager.setShowCreateWarnings(false);
		}
		return shouldContinue;
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
