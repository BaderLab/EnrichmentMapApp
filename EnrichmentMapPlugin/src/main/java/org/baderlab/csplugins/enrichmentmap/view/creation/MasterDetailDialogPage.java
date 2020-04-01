package org.baderlab.csplugins.enrichmentmap.view.creation;

import static org.baderlab.csplugins.enrichmentmap.view.creation.CreationDialogParameters.COMMAND_BUTTON_ACTION;
import static org.baderlab.csplugins.enrichmentmap.view.creation.CreationDialogParameters.RESET_BUTTON_ACTION;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TooManyListenersException;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.swing.BorderFactory;
import javax.swing.DropMode;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
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
import org.baderlab.csplugins.enrichmentmap.model.DataSetParameters;
import org.baderlab.csplugins.enrichmentmap.model.EMCreationParameters;
import org.baderlab.csplugins.enrichmentmap.model.EMCreationParameters.EdgeStrategy;
import org.baderlab.csplugins.enrichmentmap.model.EMDataSet.Method;
import org.baderlab.csplugins.enrichmentmap.resolver.DataSetResolverTask;
import org.baderlab.csplugins.enrichmentmap.task.CreateEnrichmentMapTaskFactory;
import org.baderlab.csplugins.enrichmentmap.task.InitializeGenesetsOfInterestTask.MissingGenesetStrategy;
import org.baderlab.csplugins.enrichmentmap.task.MissingGenesetsException;
import org.baderlab.csplugins.enrichmentmap.util.TaskUtil;
import org.baderlab.csplugins.enrichmentmap.view.util.FileBrowser;
import org.baderlab.csplugins.enrichmentmap.view.util.GBCFactory;
import org.baderlab.csplugins.enrichmentmap.view.util.IterableListModel;
import org.baderlab.csplugins.enrichmentmap.view.util.SwingUtil;
import org.baderlab.csplugins.enrichmentmap.view.util.dialog.CardDialogCallback;
import org.baderlab.csplugins.enrichmentmap.view.util.dialog.CardDialogPage;
import org.baderlab.csplugins.enrichmentmap.view.util.dialog.ErrorMessageDialog;
import org.baderlab.csplugins.enrichmentmap.view.util.dialog.Message;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.work.FinishStatus;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskIterator;
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
	@Inject private PropertyManager propertyManager;
	
	@Inject private Provider<DetailCommonPanel> commonPanelProvider;
	@Inject private Provider<DetailGettingStartedPanel> nullPanelProvider;
	@Inject private Provider<CommandDisplayMediator> commandDisplayProvider;
	@Inject private DetailDataSetPanel.Factory dataSetPanelFactory;
	@Inject private CreateEnrichmentMapTaskFactory.Factory taskFactoryFactory;
	@Inject private ErrorMessageDialog.Factory errorMessageDialogFactory;

	@Inject private CutoffPropertiesPanel cutoffPanel;
	
	private NamePanel networkNamePanel;
	private DetailCommonPanel commonPanel;
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
	public String getPageComboText() {
		return "Create Enrichment Map";
	}

	@Override
	public void finish() {
		if(!validateInput()) {
			updateButtonEnablement();
			return;
		}
		
		EMCreationParameters params = getCreationParameters();
		List<DataSetParameters> dataSets = getDataSetParameters();
		
		CreateEnrichmentMapTaskFactory taskFactory = taskFactoryFactory.create(params, dataSets);
		TaskIterator tasks = taskFactory.createTaskIterator(MissingGenesetStrategy.FAIL_AT_END);
		
		// Attempt to run the tasks one time, if it fails because of missing genesets
		// inform the user and prompt if they want to run again ignoring the problems.
		dialogTaskManager.execute(tasks, TaskUtil.onFail(finishStatus -> {
			Exception e = finishStatus.getException();
			if(e instanceof MissingGenesetsException) {
				Collection<String> names = ((MissingGenesetsException)e).getMissingGenesetNames();
				boolean retry = promptForMissingGenesetRetry(names);
				if(retry) {
					// Run the tasks again but this time ignore the missing genesets
					TaskIterator retryTasks = taskFactory.createTaskIterator(MissingGenesetStrategy.IGNORE);
					dialogTaskManager.execute(retryTasks);
				}
			}
		}));
		
		callback.setFinishButtonEnabled(true);
		callback.close();
	}
	
	
	private boolean promptForMissingGenesetRetry(Collection<String> names) {
		int count = names.size();
		int limit = 10;
		
		String title = "There are " + count + " gene sets in the enrichment file that are missing from the GMT file. ";
		if(count > limit)
			title += "The first " + limit + " gene set names are listed below.";
		
		ErrorMessageDialog dialog = errorMessageDialogFactory.create(callback.getDialogFrame());
		List<Message> messages = names.stream().map(Message::warn).limit(limit).collect(Collectors.toList());
		dialog.addSection(messages, title, IconManager.ICON_WARNING);
		
		String bottomMessage = "<html>It is recommend that you click 'Cancel' and fix the errors in your enrichment and GMT files. <br>"
				+ "However, you may click 'Continue' to create the network without the missing gene sets.</html>";
		dialog.addSection(Collections.emptyList(), bottomMessage, null);
		
		dialog.showDontWarnAgain(false);
		dialog.setFinishButtonText("Continue");
		
		dialog.pack();
		dialog.setLocationRelativeTo(callback.getDialogFrame());
		dialog.setModal(true);
		dialog.setVisible(true);
		
		return dialog.shouldContinue();
	}
	
	
	private EMCreationParameters getCreationParameters() {
		EMCreationParameters params = cutoffPanel.getCreationParameters();
		params.setNetworkName(networkNamePanel.getNameText());
		return params;
	}
	
	
	private List<DataSetParameters> getDataSetParameters() {
		List<DataSetParameters> dataSets = dataSetListModel.toList().stream()
			.map(DataSetListItem::getDetailPanel)
			.map(DetailPanel::createDataSetParameters)
			.filter(x -> x != null)
			.collect(Collectors.toList());

		// Overwrite all the expression files if the common file has been provided
		if(commonPanel.hasExprFile()) {
			String exprPath = commonPanel.getExprFile();
			dataSets.forEach(dsp -> dsp.getFiles().setExpressionFileName(exprPath));
		}
		// Overwrite all the gmt files if a common file has been provided
		if(commonPanel.hasGmtFile()) {
			String gmtPath = commonPanel.getGmtFile();
			dataSets.forEach(dsp -> dsp.getFiles().setGMTFileName(gmtPath));
		}
		// Overwrite all the class files if a common file has been provided
		if(commonPanel.hasClassFile()) {
			String classPath = commonPanel.getClassFile();
			dataSets.forEach(dsp -> dsp.getFiles().setClassFile(classPath));
		}
		return dataSets;
	}
	
	
	private void createCommand() {
		EMCreationParameters params = getCreationParameters();
		List<DataSetParameters> dataSets = getDataSetParameters();
		
		if(networkNamePanel.isAutomatic())
			params.setNetworkName(null);
		
		String commonExprFile  = commonPanel.hasExprFile()  ? commonPanel.getExprFile()  : null;
		String commonGMTFile   = commonPanel.hasGmtFile()   ? commonPanel.getGmtFile()   : null;
		String commonClassFile = commonPanel.hasClassFile() ? commonPanel.getClassFile() : null;
		
		JDialog parent = callback.getDialogFrame();
		CommandDisplayMediator commandDisplayMediator = commandDisplayProvider.get();
		commandDisplayMediator.showCommand(parent, params, dataSets, commonExprFile, commonGMTFile, commonClassFile);
	}
	
	
	@Override
	public JPanel createBodyPanel(CardDialogCallback callback) {
		this.callback = callback;
		this.commonPanel = commonPanelProvider.get();
		commonParams = new DataSetListItem(commonPanel);
				
		JPanel dataPanel = createDataSetPanel();
		networkNamePanel = new NamePanel("Network Name");
		
		JPanel bottom = new JPanel(new GridBagLayout());
		bottom.add(networkNamePanel, GBCFactory.grid(0,0).insets(0).weightx(1.0).fill(GridBagConstraints.HORIZONTAL).get());
		bottom.add(cutoffPanel, GBCFactory.grid(0,1).insets(0).weightx(1.0).fill(GridBagConstraints.HORIZONTAL).get());
		
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(dataPanel, BorderLayout.CENTER);
		panel.add(bottom, BorderLayout.SOUTH);
		
		updateButtonEnablement();
		return panel;
	}

	
	private JPanel createDataSetPanel() {
		JPanel titlePanel = createTitlePanel();
		
		dataSetListModel = new IterableListModel<>();
		dataSetList = new DataSetList(dataSetListModel);
		dataSetList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		dataSetList.addListSelectionListener(e -> selectItem(dataSetList.getSelectedValue()));
		dataSetListModel.addListDataListener(SwingUtil.simpleListDataListener(this::updateAutomaticNetworkName));
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setViewportView(dataSetList);
		
		dataSetDetailPanel = new JPanel(new BorderLayout());
		dataSetDetailPanel.setBorder(BorderFactory.createLineBorder(UIManager.getColor("Separator.foreground"))); 
		cardLayout = new CardLayout();
		dataSetDetailPanel.setLayout(cardLayout);
		
		// Blank page
		DetailGettingStartedPanel nullPanel = nullPanelProvider.get();
		nullPanel.setScanButtonCallback(this::scanButtonClicked);
		dataSetDetailPanel.add(nullPanel, "nothing");
		
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
		scanButton   = SwingUtil.createIconButton(iconManager, IconManager.ICON_FOLDER_O, "Add data sets from folder (scan)");
		deleteButton = SwingUtil.createIconButton(iconManager, IconManager.ICON_TRASH_O, "Delete selected data sets");
		
		addButton.addActionListener(e -> addNewDataSetToList());
		deleteButton.addActionListener(e -> deleteSelectedItems());
		scanButton.addActionListener(e -> scanButtonClicked());
		
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
	
	@Override
	public void opened() {
		if(dataSetListModel.getSize() == 1) { // no data sets, only "common files" in list
			selectItem(null); // reset the detail panel, shows "getting started" message
			dataSetList.clearSelection();
		}
	}
	
	private void addDataSetToList(DataSetParameters params) {
		DetailDataSetPanel panel = dataSetPanelFactory.create(params);
		panel.addPropertyChangeListener(DetailDataSetPanel.PROP_NAME, e ->
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
	
	private void moveItemUp() {
		int index = dataSetList.getSelectedIndex();
		// can't move "common files" or top data set
		if(index >= 2) {
			swap(index, index - 1);
		}
	}
	
	private void moveItemDown() {
		int index = dataSetList.getSelectedIndex();
		if(index != 0 && index != dataSetListModel.getSize() - 1) {
			swap(index, index + 1);
		}
	}
	
	private void swap(int a, int b) {
        DataSetListItem itemA = dataSetListModel.getElementAt(a);
        DataSetListItem itemB = dataSetListModel.getElementAt(b);
        dataSetListModel.set(a, itemB);
        dataSetListModel.set(b, itemA);
        dataSetList.setSelectedIndex(b);
        dataSetList.ensureIndexIsVisible(b);
    }
	
	
	private void selectItem(DataSetListItem params) {
		cardLayout.show(dataSetDetailPanel, params == null ? "nothing" : params.id);
		dataSetDetailPanel.revalidate();
		updateButtonEnablement();
	}
	
	
	private void updateButtonEnablement() {
		deleteButton.setEnabled(dataSetListModel.getSize() > 0 && dataSetList.getSelectedIndex() > 0);
		callback.getExtraButton(COMMAND_BUTTON_ACTION).setEnabled(dataSetListModel.size() > 1);
		callback.setFinishButtonEnabled(dataSetListModel.size() > 1);
	}
	
	private void updateAutomaticNetworkName() {
		if(dataSetListModel.size() > 1) {
			String name = dataSetListModel.get(1).getDetailPanel().getDataSetName();
			networkNamePanel.setAutomaticName(name);
		}
	}
	
	@Override
	public void extraButtonClicked(String actionCommand) {
		switch(actionCommand) {
		case RESET_BUTTON_ACTION:
			reset();
			break;
		case COMMAND_BUTTON_ACTION:
			createCommand();
			break;
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
			
			if(dataSetListModel.getSize() == 1) { // no data sets, only "common files" in list
				selectItem(null); // reset the detail panel, shows "getting started" message
				dataSetList.clearSelection();
			}
		}
	}
	
	private void scanButtonClicked() {
		Optional<File> rootFolder = fileBrowser.browseForRootFolder(jframeProvider.get());
		if(rootFolder.isPresent()) {
			File root = rootFolder.get();
			runResolverTask(Arrays.asList(root));
		}
	}
	
	private void runResolverTask(List<File> files) {
		scanButton.setEnabled(false);
		DataSetResolverTask task = new DataSetResolverTask(files);
		
		dialogTaskManager.execute(new TaskIterator(task), new TaskObserver() {
			
			boolean foundDatasets = false;
			
			@Override
			public void taskFinished(ObservableTask task) {
				@SuppressWarnings("unchecked")
				List<DataSetParameters> datasets = task.getResults(List.class);
				if(!datasets.isEmpty()) {
					foundDatasets = true;
					datasets.sort(Comparator.comparing(DataSetParameters::getName));
					datasets.forEach(MasterDetailDialogPage.this::addDataSetToList);
					dataSetList.setSelectedValue(datasets.get(0), true);
				}
			}
			
			@Override
			public void allFinished(FinishStatus finishStatus) {
				scanButton.setEnabled(true);
				updateButtonEnablement();
				if(!foundDatasets) {
					JOptionPane.showMessageDialog(callback.getDialogFrame(), "No data sets found", "EnrichmentMap", JOptionPane.WARNING_MESSAGE);
				}
			}
		});
	}
	
	public DetailCommonPanel getCommonPanel() {
		return commonPanel;
	}
	
	
	private boolean validateInput() {
		ErrorMessageDialog dialog = errorMessageDialogFactory.create(callback.getDialogFrame());
		
		// Check if the user provided a global expression file, warn if there are also per-dataset expression files.
		if(commonPanel.hasExprFile() && editPanelStream().anyMatch(DetailDataSetPanel::hasExpressionFile)) {
			addCommonWarnSection(dialog, commonPanel, "expression");
		}
		// Check if the user provided a global gmt file, warn if there are also per-dataset gmt files.
		if(commonPanel.hasGmtFile() && editPanelStream().anyMatch(DetailDataSetPanel::hasGmtFile)) {
			addCommonWarnSection(dialog, commonPanel, "GMT");
		}
		// Check if the user provided a global gmt file, warn if there are also per-dataset gmt files.
		if(commonPanel.hasClassFile() && editPanelStream().anyMatch(DetailDataSetPanel::hasClassFile)) {
			addCommonWarnSection(dialog, commonPanel, "class");
		}
		
		// Warn when will distinct edges will always be the same.
		// 1) Common GMT and Common Expression
		// 2) Common GMT no filtering
		if(commonPanel.hasGmtFile() && cutoffPanel.getEdgeStrategy() == EdgeStrategy.DISTINCT) { // and the user has explicitly specified distinct edges
			if(commonPanel.hasExprFile()) {
				String message = "<html>When providing a common GMT and common expression file 'Separate' edges will all be the same.<br>"
						+ "It is recommended to select 'Combine Edges' or 'Automatic' in this case.</html>";
				dialog.addSection(Message.warn(message), commonPanel.getDisplayName(), commonPanel.getIcon());
			} else if(!cutoffPanel.getFilterGenesByExpressions()) {
				String message = "<html>When providing a common GMT and not filtering gene sets by expressions then 'Separate' edges will all be the same.<br>"
						+ "It is recommended to select 'Combine Edges' or 'Automatic' in this case.</html>";
				dialog.addSection(Message.warn(message), commonPanel.getDisplayName(), commonPanel.getIcon());
			}
		}
		
		{ // Check for duplicate data set names
			Map<String,Long> dataSetNameCount = editPanelStream()
				.map(DetailDataSetPanel::getDataSetName)
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
		
		if(networkNamePanel.getNameText().trim().isEmpty()) {
			dialog.addSection(Message.error("Network name is missing"), "Network Name", commonPanel.getIcon());
		}
			
		if(dialog.isEmpty())
			return true;
		if(!dialog.hasErrors() && !propertyManager.getValue(PropertyManager.CREATE_WARN))
			return true;
		
		dialog.pack();
		dialog.setLocationRelativeTo(callback.getDialogFrame());
		dialog.setModal(true);
		dialog.setVisible(true);
		// This will always return false if the dialog has error messages. 
		// If the dialog only has warning messages then the user can choose to continue.
		boolean shouldContinue = dialog.shouldContinue();
		if(dialog.isDontWarnAgain()) {
			propertyManager.setValue(PropertyManager.CREATE_WARN, false);
		}
		return shouldContinue;
	}
	
	
	private Stream<DetailDataSetPanel> editPanelStream() {
		return dataSetListModel.stream()
			.map(DataSetListItem::getDetailPanel)
			.filter(panel -> panel instanceof DetailDataSetPanel)
			.map(panel -> (DetailDataSetPanel)panel);
	}
	
	private static void addCommonWarnSection(ErrorMessageDialog dialog, DetailPanel panel, String name) {
		String message = "A common " + name + " file has been provided. Per-dataset " + name + " files will be ignored.";
		dialog.addSection(Message.warn(message), panel.getDisplayName(), panel.getIcon());
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
		
		private boolean isDragging;
		
		@Inject
		public DataSetList(ListModel<DataSetListItem> model) {
			setModel(model);
			setCellRenderer(new CellRenderer());
			setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
			setDropMode(DropMode.ON);
			setTransferHandler(new ResolverTaskTransferHandler(MasterDetailDialogPage.this::runResolverTask));
			addMouseListener(getContextMenuMouseListener());
			try {
				getDropTarget().addDropTargetListener(getDropTargetListener());
			} catch (TooManyListenersException e) { /* do nothing */ }
		}
		
		private class CellRenderer implements ListCellRenderer<DataSetListItem> {

			@Override
			public Component getListCellRendererComponent(JList<? extends DataSetListItem> list,
					DataSetListItem listItem, int index, boolean isSelected, boolean cellHasFocus) {
				
				Color bgColor = UIManager.getColor(isSelected | isDragging ? "Table.selectionBackground" : "Table.background");
				Color fgColor = UIManager.getColor(isSelected              ? "Table.selectionForeground" : "Table.foreground");
				
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
		
		
		private MouseListener getContextMenuMouseListener() {
			return new MouseAdapter() {
				@Override public void mousePressed(MouseEvent e) {
					showContextMenu(e);
				}
				@Override public void mouseReleased(MouseEvent e) {
					showContextMenu(e);
				}
				private void showContextMenu(MouseEvent e) {
					if(!e.isPopupTrigger())
						return;
					
					int index = locationToIndex(e.getPoint());
					if(index != -1 && getCellBounds(index,index).contains(e.getPoint())) {
						setSelectedIndex(index);
						
						JMenuItem deleteItem = new JMenuItem("Delete");
						deleteItem.addActionListener(ae -> deleteSelectedItems());
						JMenuItem upItem = new JMenuItem("Move Up");
						upItem.addActionListener(ae -> moveItemUp());
						JMenuItem downItem = new JMenuItem("Move Down");
						downItem.addActionListener(ae -> moveItemDown());
						
						deleteItem.setEnabled(index > 0);
						upItem.setEnabled(index > 1);
						downItem.setEnabled(index != 0 && index != getModel().getSize()-1);
						
						JPopupMenu menu = new JPopupMenu();
						menu.add(upItem);
						menu.add(downItem);
						menu.addSeparator();
						menu.add(deleteItem);
						menu.show(DataSetList.this, e.getX(), e.getY());
					}
				}
			};
		}
		
		private DropTargetListener getDropTargetListener() {
			return new DropTargetAdapter() {
				Color normalColor = getBackground();
				Color dragColor = UIManager.getColor("Table.selectionBackground");
				
				public void dragEnter(DropTargetDragEvent e) { 
					isDragging = true;
					setBackground(dragColor);   
				}
				public void dragExit(DropTargetEvent e) { 
					isDragging = false;
					setBackground(normalColor); 
				}
				public void drop(DropTargetDropEvent e) { 
					isDragging = false;
					setBackground(normalColor); 
				}
			};
		}
		
	}
	
}
