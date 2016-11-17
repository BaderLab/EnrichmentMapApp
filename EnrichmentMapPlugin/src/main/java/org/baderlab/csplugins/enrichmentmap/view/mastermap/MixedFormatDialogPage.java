package org.baderlab.csplugins.enrichmentmap.view.mastermap;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.border.Border;

import org.baderlab.csplugins.enrichmentmap.model.DataSet.Method;
import org.baderlab.csplugins.enrichmentmap.model.DataSetFiles;
import org.baderlab.csplugins.enrichmentmap.model.EMCreationParameters;
import org.baderlab.csplugins.enrichmentmap.model.EMCreationParameters.SimilarityMetric;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentResultFilterParams.NESFilter;
import org.baderlab.csplugins.enrichmentmap.model.LegacySupport;
import org.baderlab.csplugins.enrichmentmap.parsers.PathTypeMatcher;
import org.baderlab.csplugins.enrichmentmap.task.MasterMapGSEATaskFactory;
import org.baderlab.csplugins.enrichmentmap.view.util.CardDialogCallback;
import org.baderlab.csplugins.enrichmentmap.view.util.CardDialogCallback.Message;
import org.baderlab.csplugins.enrichmentmap.view.util.CardDialogPage;
import org.baderlab.csplugins.enrichmentmap.view.util.FileBrowser;
import org.baderlab.csplugins.enrichmentmap.view.util.GBCFactory;
import org.baderlab.csplugins.enrichmentmap.view.util.IterableListModel;
import org.baderlab.csplugins.enrichmentmap.view.util.SwingUtil;
import org.cytoscape.util.swing.FileUtil;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.util.swing.LookAndFeelUtil;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.swing.DialogTaskManager;

import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.lowagie.text.Font;

@SuppressWarnings("serial")
public class MixedFormatDialogPage implements CardDialogPage {

	@Inject private DialogTaskManager taskManager;
	@Inject private FileUtil fileUtil;
	@Inject private IconManager iconManager;
	
	@Inject private LegacySupport legacySupport;
	@Inject private CutoffPropertiesPanel cutoffPanel;
	@Inject private MasterMapGSEATaskFactory.Factory taskFactoryFactory;
	
	private CardDialogCallback callback;
	
	private IterableListModel<DataSetParameters> dataSetListModel;
	
	
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
		return "Mixed Format - Experimental";
	}
	
	@Override
	public void finish() {
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
		
		List<DataSetParameters> dataSets = dataSetListModel.toList();
		
		MasterMapGSEATaskFactory taskFactory = taskFactoryFactory.create(params, dataSets);
		TaskIterator tasks = taskFactory.createTaskIterator();
		
		// Close this dialog after the progress dialog finishes normally
		tasks.append(new AbstractTask() {
			public void run(TaskMonitor taskMonitor) {
				callback.close();
			}
		});
		
		taskManager.execute(tasks);
	}
	
	@Override
	public JPanel createBodyPanel(CardDialogCallback callback) {
		this.callback = callback;
		
		JPanel dataPanel = createDataSetPanel();
		JPanel gmtPanel  = createTextFieldPanel();
		
		JPanel panel = new JPanel();
		GroupLayout layout = new GroupLayout(panel);
		panel.setLayout(layout);
		layout.setAutoCreateGaps(true);
		
		layout.setHorizontalGroup(
			layout.createParallelGroup()
				.addComponent(dataPanel)
				.addComponent(gmtPanel)
				.addComponent(cutoffPanel)
		);
		layout.setVerticalGroup(
			layout.createSequentialGroup()
				.addComponent(dataPanel, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(gmtPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
				.addComponent(cutoffPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
		);
		
		callback.setFinishButtonEnabled(true);
			
		return panel;
	}
	
	
	private JPanel createDataSetPanel() {
		dataSetListModel = new IterableListModel<>();
		JList<DataSetParameters> dataSetList = new DataSetList(dataSetListModel);
		// MKTODO probably just inline createAddRemovePanel
		return createAddRemovePanel(dataSetList, "Enrichment Data Sets (0)", this::browseForDataSets);
	}
	
	
	private List<DataSetParameters> browseForDataSets() {
		Optional<File> rootFolder = FileBrowser.browseForRootFolder(callback.getDialogFrame());
		if(rootFolder.isPresent()) {
			return scanRootFolder(rootFolder.get());
		}
		return Collections.emptyList();
	}
	
	
	private List<DataSetParameters> scanRootFolder(File root) {
		callback.clearMessage();
		
		if(!root.isDirectory()) {
			callback.setMessage(Message.ERROR, "Not a folder");
			return Collections.emptyList();
		}
		
		Path path = root.toPath();
		List<DataSetParameters> dataSets = PathTypeMatcher.guessDataSets(path);
		return dataSets;
	}
	
	
	private JPanel createTextFieldPanel() {
		JLabel gmtLabel = new JLabel(" GMT File (optional):");
		JLabel extLabel = new JLabel(" Expression File (optional):");
		
		JTextField gmtPathText = new JTextField();
		JTextField expPathText = new JTextField();
		
		JButton gmtBrowseButton = new JButton("Browse...");
		JButton expBrowseButton = new JButton("Browse...");
		
		gmtBrowseButton.addActionListener(e -> FileBrowser.browseGMT(fileUtil, callback.getDialogFrame()));
		expBrowseButton.addActionListener(e -> FileBrowser.browseExpression(fileUtil, callback.getDialogFrame()));
		
		SwingUtil.makeSmall(gmtLabel, extLabel, gmtPathText, expPathText, gmtBrowseButton, expBrowseButton);
		
		JPanel panel = new JPanel();
		panel.setBorder(LookAndFeelUtil.createPanelBorder());
		
		GroupLayout layout = new GroupLayout(panel);
		panel.setLayout(layout);
		layout.setAutoCreateContainerGaps(false);
		layout.setAutoCreateGaps(true);

		layout.setHorizontalGroup(
			layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup(Alignment.TRAILING)
					.addComponent(gmtLabel)
					.addComponent(extLabel)
				)
				.addGroup(layout.createParallelGroup(Alignment.LEADING, true)
					.addComponent(gmtPathText, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
					.addComponent(expPathText, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				)
				.addGroup(layout.createParallelGroup()
					.addComponent(gmtBrowseButton)
					.addComponent(expBrowseButton)
				)
		);
		
		layout.setVerticalGroup(
			layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup(Alignment.BASELINE)
					.addComponent(gmtLabel)
					.addComponent(gmtPathText)
					.addComponent(gmtBrowseButton)
				)
				.addGroup(layout.createParallelGroup(Alignment.BASELINE)
					.addComponent(extLabel)
					.addComponent(expPathText)
					.addComponent(expBrowseButton)
				)
			);
		
		return panel;
	}
	
	private <T> JPanel createAddRemovePanel(JList<T> list, String title, Supplier<List<T>> listSupplier) {
		JButton addButton = new JButton("Add...");
		JButton removeButton = new JButton("Remove");
		JButton removeAllButton = new JButton("Clear");
		
		SwingUtil.makeSmall(addButton, removeButton, removeAllButton);
		
		// MKTODO check for duplicates, might even make sense to use a Set for the list model
		addButton.addActionListener(e -> {
			DefaultListModel<T> model = (DefaultListModel<T>)list.getModel();
			listSupplier.get().forEach(model::addElement);
		});
		
		removeButton.addActionListener(e -> {
			List<T> selected = list.getSelectedValuesList();
			DefaultListModel<T> model = (DefaultListModel<T>)list.getModel();
			selected.forEach(model::removeElement);
		});
		
		removeAllButton.addActionListener(e -> {
			DefaultListModel<T> model = (DefaultListModel<T>)list.getModel();
			model.clear();
		});
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setViewportView(list);
		
		JPanel panel = new JPanel();
		GroupLayout layout = new GroupLayout(panel);
		panel.setLayout(layout);
		layout.setAutoCreateGaps(true);
		
		int buttonWidth = 80;
		
		layout.setHorizontalGroup(
			layout.createSequentialGroup()
				.addComponent(scrollPane, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				.addGroup(layout.createParallelGroup()
					.addComponent(addButton, buttonWidth, buttonWidth, buttonWidth)
					.addComponent(removeButton, buttonWidth, buttonWidth, buttonWidth)
					.addComponent(removeAllButton, buttonWidth, buttonWidth, buttonWidth)
				)
		);
		layout.setVerticalGroup(
			layout.createSequentialGroup()
				.addGap(10, 10, 10)
				.addGroup(layout.createParallelGroup()
					.addComponent(scrollPane, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
					.addGroup(layout.createSequentialGroup()
						.addComponent(addButton)
						.addComponent(removeButton)
						.addComponent(removeAllButton)
					)
				)
		);
		
		panel.setBorder(LookAndFeelUtil.createTitledBorder(title));
		
		return panel;
	}


	private class DataSetList extends JList<DataSetParameters> {
		@Inject
		public DataSetList(ListModel<DataSetParameters> model) {
			setModel(model);
			setCellRenderer(new CellRenderer());
			setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		}
		
		private class CellRenderer implements ListCellRenderer<DataSetParameters> {

			@Override
			public Component getListCellRendererComponent(JList<? extends DataSetParameters> list, DataSetParameters dataSet, 
					int index, boolean isSelected, boolean cellHasFocus) {
				
				JLabel icon = new JLabel(" " + IconManager.ICON_FILE_TEXT + "  ");
				icon.setFont(iconManager.getIconFont(13.0f));
				
				JLabel title = new JLabel(dataSet.getName() + "  (" + methodToString(dataSet.getMethod()) + ")");
				SwingUtil.makeSmall(title);
				title.setFont(title.getFont().deriveFont(Font.BOLD));
				
				JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEADING));
				titlePanel.add(icon);
				titlePanel.add(title);
				titlePanel.setOpaque(false);
				
				JPanel filePanel = createFilePanel(dataSet.getFiles());
				filePanel.setBackground(getBackground());
				filePanel.setOpaque(false);
				
				JPanel panel = new JPanel(new BorderLayout());
				panel.add(titlePanel, BorderLayout.NORTH);
				panel.add(new JLabel("  "), BorderLayout.WEST);
				panel.add(filePanel, BorderLayout.CENTER);
				panel.setBackground(isSelected ? list.getSelectionBackground().brighter() : getBackground());
				
				Border emptyBorder = BorderFactory.createEmptyBorder(2, 4, 2, 4);
				Border lineBorder  = BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY);
				Border compound    = BorderFactory.createCompoundBorder(lineBorder, emptyBorder);
				panel.setBorder(compound);
				
				return panel;
			}
			
			private String methodToString(Method method) {
				switch(method) {
					case GSEA:        return "GSEA";
					case Generic:     return "Generic/gProfiler";
					case Specialized: return "DAVID/BINGO/Great";
					default:          return "Unknown";
				}
			}
			
			private JPanel createFilePanel(DataSetFiles files) {
				JPanel filePanel = new JPanel(new GridBagLayout());
				int y = 0;
				String enrichmentFileName1 = files.getEnrichmentFileName1();
				if(!Strings.isNullOrEmpty(enrichmentFileName1)) {
					y = addFileLabel("Enrichments: ", Paths.get(enrichmentFileName1).getFileName().toString(), filePanel, y);
				}
				String gmtFileName = files.getGMTFileName();
				if(!Strings.isNullOrEmpty(gmtFileName)) {
					y = addFileLabel("GMT File: ", Paths.get(gmtFileName).getFileName().toString(), filePanel, y);
				}
				return filePanel;
			}
			
			
			private int addFileLabel(String name, String path, JPanel filePanel, int y) {
				if(path == null)
					return y;
				JLabel type = new JLabel(name);
				JLabel comp = new JLabel(path);
				SwingUtil.makeSmall(type, comp);
				filePanel.add(type, GBCFactory.grid(0,y).get());
				filePanel.add(comp, GBCFactory.grid(1,y).weightx(1.0).get());
				return y+1;
			}
		}
	}
	
	
	
}
