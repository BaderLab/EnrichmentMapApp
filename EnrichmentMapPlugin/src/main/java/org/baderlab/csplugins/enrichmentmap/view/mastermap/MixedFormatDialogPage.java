package org.baderlab.csplugins.enrichmentmap.view.mastermap;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FileDialog;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.border.Border;

import org.baderlab.csplugins.enrichmentmap.model.DataSetFiles;
import org.baderlab.csplugins.enrichmentmap.model.EMCreationParameters;
import org.baderlab.csplugins.enrichmentmap.model.EMCreationParameters.Method;
import org.baderlab.csplugins.enrichmentmap.model.EMCreationParameters.SimilarityMetric;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentResultFilterParams.NESFilter;
import org.baderlab.csplugins.enrichmentmap.model.LegacySupport;
import org.baderlab.csplugins.enrichmentmap.task.MasterMapGSEATaskFactory;
import org.baderlab.csplugins.enrichmentmap.util.SwingUtil;
import org.baderlab.csplugins.enrichmentmap.view.util.CardDialogCallback;
import org.baderlab.csplugins.enrichmentmap.view.util.CardDialogCallback.Message;
import org.baderlab.csplugins.enrichmentmap.view.util.CardDialogPage;
import org.baderlab.csplugins.enrichmentmap.view.util.GBCFactory;
import org.baderlab.csplugins.enrichmentmap.view.util.IterableListModel;
import org.cytoscape.util.swing.FileChooserFilter;
import org.cytoscape.util.swing.FileUtil;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.util.swing.LookAndFeelUtil;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.swing.DialogTaskManager;

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
	private IterableListModel<Path> gmtListModel;
	
	
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
			new EMCreationParameters(Method.GSEA, prefix, pvalue, qvalue, nesFilter, minExperiments, similarityMetric, cutoff, combined);
		
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
		JPanel gmtPanel  = createGMTPanel();
		
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
				.addComponent(gmtPanel) //, 130, 130, 130)
				.addComponent(cutoffPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
		);
		
		callback.setFinishButtonEnabled(true);
			
		return panel;
	}
	
	
	private JPanel createDataSetPanel() {
		dataSetListModel = new IterableListModel<>();
		JList<DataSetParameters> dataSetList = new DataSetList(dataSetListModel);
		return createAddRemovePanel(dataSetList, "Enrichment Data Sets (GSEA only at the moment)", this::browseForDataSets);
	}
	
	private JPanel createGMTPanel() {
		gmtListModel = new IterableListModel<>();
		JList<Path> gmtFileList = new GMTList(gmtListModel);
		return createAddRemovePanel(gmtFileList, "GMT Files", this::browseForGMTFiles);
	}
	
	
	private List<Path> browseForGMTFiles() {
		List<FileChooserFilter> filters = Arrays.asList(new FileChooserFilter("gmt Files", "gmt")); 
		File file = fileUtil.getFile(callback.getDialogFrame(), "GMT Files", FileUtil.LOAD, filters);
		return file == null ? Collections.emptyList() : Arrays.asList(file.toPath());
	}
	
	
	private List<DataSetParameters> browseForDataSets() {
		Optional<File> rootFolder = browseForRootFolder();
		if(rootFolder.isPresent()) {
			return scanRootFolder(rootFolder.get());
		}
		return Collections.emptyList();
	}
	
	
	private Optional<File> browseForRootFolder() {
		final String osName = System.getProperty("os.name");
		if(osName.startsWith("Mac"))
			return browseForRootFolderMac();
		else
			return browseForRootFolderSwing();
	}
	
	
	private Optional<File> browseForRootFolderMac() {
		final String property = System.getProperty("apple.awt.fileDialogForDirectories");
		System.setProperty("apple.awt.fileDialogForDirectories", "true");
		try {
			FileDialog chooser = new FileDialog(callback.getDialogFrame(), "Choose Root Folder", FileDialog.LOAD);
			chooser.setModal(true);
			chooser.setLocationRelativeTo(callback.getDialogFrame());
			chooser.setVisible(true);
			
			String file = chooser.getFile();
			String dir = chooser.getDirectory();
			
			if(file == null || dir == null) {
				return Optional.empty();
			}
			return Optional.of(new File(dir + File.separator + file));
		} finally {
			if(property != null) {
				System.setProperty("apple.awt.fileDialogForDirectories", property);
			}
		}
	}
	
	private Optional<File> browseForRootFolderSwing() {
		JFileChooser chooser = new JFileChooser(); 
	    chooser.setDialogTitle("Select Root Folder");
	    chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
	    chooser.setAcceptAllFileFilterUsed(false);
	    if (chooser.showOpenDialog(callback.getDialogFrame()) == JFileChooser.APPROVE_OPTION) { 
	    	return Optional.of(chooser.getSelectedFile());
	    }
	    return Optional.empty();
	}
	
	
	private List<DataSetParameters> scanRootFolder(File root) {
		Path path = root.toPath();
		callback.clearMessage();
		
		if(!root.isDirectory()) {
			callback.setMessage(Message.ERROR, "Not a folder");
			return Collections.emptyList();
		}
		
		try(Stream<Path> contents = Files.list(path)) {
			return contents
				.filter(Files::isDirectory)
				.filter(new GSEAFolderPredicate())
				.map(MasterMapGSEATaskFactory::toDataSetParametersGSEA)
				.collect(Collectors.toList());
		} catch(IOException e) {
			callback.setMessage(Message.ERROR, "Cannot read folder contents");
			e.printStackTrace();
			return Collections.emptyList();
		}
	}
	
	
	private <T> JPanel createAddRemovePanel(JList<T> list, String title, Supplier<List<T>> listSupplier) {
		JButton addButton = new JButton("Add...");
		JButton removeButton = new JButton("Remove");
		
		SwingUtil.makeSmall(addButton, removeButton);
		
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
					)
				)
		);
		
		panel.setBorder(LookAndFeelUtil.createTitledBorder(title));
		
		return panel;
	}

	
	
	private class GMTList extends JList<Path> {
		
		@Inject
		public GMTList(ListModel<Path> model) {
			setModel(model);
			setCellRenderer(new CellRenderer());
			setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		}
		
		private class CellRenderer implements ListCellRenderer<Path> {

			@Override
			public Component getListCellRendererComponent(JList<? extends Path> list, Path path, 
					int index, boolean isSelected, boolean cellHasFocus) {
				
				JLabel iconLabel = new JLabel(" " + IconManager.ICON_DATABASE);
				iconLabel.setFont(iconManager.getIconFont(13.0f));
				
				JLabel nameLabel = new JLabel("  " + path.getFileName().toString());
				nameLabel.setFont(nameLabel.getFont().deriveFont(LookAndFeelUtil.getSmallFontSize()));
				
				JPanel panel = new JPanel();
				panel.setLayout(new BorderLayout());
				panel.add(iconLabel, BorderLayout.WEST);
				panel.add(nameLabel, BorderLayout.CENTER);
				panel.setBackground(getBackground());
				
				return panel;
			}
		}
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
				
				JLabel title = new JLabel(dataSet.getName() + "  (GSEA)");
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
			
			
			private JPanel createFilePanel(DataSetFiles files) {
				JPanel filePanel = new JPanel(new GridBagLayout());
				int y = 0;
				
				y = addFileLabel("Enrichments: ", files.getEnrichmentFileName1(), filePanel, y);
				y = addFileLabel("GMT File: ",    files.getGMTFileName(),         filePanel, y);
				
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
