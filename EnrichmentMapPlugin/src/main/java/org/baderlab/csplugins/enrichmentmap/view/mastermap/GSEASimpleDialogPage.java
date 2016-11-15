package org.baderlab.csplugins.enrichmentmap.view.mastermap;

import java.awt.BorderLayout;
import java.awt.FileDialog;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JTextField;

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
import org.baderlab.csplugins.enrichmentmap.view.util.CheckboxData;
import org.baderlab.csplugins.enrichmentmap.view.util.CheckboxListModel;
import org.baderlab.csplugins.enrichmentmap.view.util.CheckboxListPanel;
import org.cytoscape.util.swing.LookAndFeelUtil;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.swing.DialogTaskManager;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class GSEASimpleDialogPage implements CardDialogPage {
	
	@Inject private DialogTaskManager taskManager;
	@Inject private CutoffPropertiesPanel cutoffPanel;
	@Inject private LegacySupport legacySupport;
	@Inject private MasterMapGSEATaskFactory.Factory taskFactoryFactory;
	
	private CardDialogCallback callback;
	private JPanel panel;
	
	private JTextField pathTextField;
	private CheckboxListPanel<Path> checkboxListPanel;
	
	
	@Override
	public String getID() {
		return "mastermap.GSEASimpleDialogPage";
	}
	
	@Override
	public String getPageTitle() {
		return "Create Enrichment Map from GSEA Results";
	}
	
	@Override
	public String getPageComboText() {
		return "GSEA - Common Root Folder";
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
		
		List<Path> paths = checkboxListPanel.getSelectedDataItems();
		List<DataSetParameters> dataSets = paths.stream().map(MasterMapGSEATaskFactory::toDataSetParametersGSEA).collect(Collectors.toList());
		
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
		
		JPanel gseaPanel = createGSEAPanel();
		
		panel = new JPanel();
		GroupLayout layout = new GroupLayout(panel);
		panel.setLayout(layout);
		layout.setAutoCreateContainerGaps(false);
		layout.setAutoCreateGaps(true);
		
		layout.setHorizontalGroup(
			layout.createParallelGroup()
				.addComponent(gseaPanel)
				.addComponent(cutoffPanel)
		);
		layout.setVerticalGroup(
			layout.createSequentialGroup()
				.addComponent(gseaPanel)
				.addComponent(cutoffPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
		);
		
		callback.setFinishButtonEnabled(false);
		
		return panel;
	}


	private JPanel createBrowsePanel() {
		JPanel panel = new JPanel(new BorderLayout());
		
		pathTextField = new JTextField();
		JButton browseButton = new JButton("Browse...");
		SwingUtil.makeSmall(browseButton);
		browseButton.addActionListener(e -> browseForRootFolder());
		
		panel.add(pathTextField, BorderLayout.CENTER);
		panel.add(browseButton, BorderLayout.EAST);
		panel.setOpaque(false);
		return panel;
	}
	
	
	private JPanel createGSEAPanel() {
		JPanel browsePanel = createBrowsePanel();
		
		checkboxListPanel = new CheckboxListPanel<>();
		checkboxListPanel.setOpaque(false);
		
		JPanel panel = new JPanel(new BorderLayout());
		panel.setBorder(LookAndFeelUtil.createTitledBorder("Root folder containing GSEA result folders"));
		
		final GroupLayout layout = new GroupLayout(panel);
		panel.setLayout(layout);
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(true);
		
		layout.setHorizontalGroup(layout.createParallelGroup(Alignment.LEADING, true)
			.addComponent(browsePanel)
			.addComponent(checkboxListPanel)
		);
		layout.setVerticalGroup(layout.createSequentialGroup()
			.addGap(10, 10, 10)
			.addComponent(browsePanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
			.addComponent(checkboxListPanel, 100, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
		);
		
		if (LookAndFeelUtil.isAquaLAF())
			panel.setOpaque(false);
   		
   		return panel;
	}
	
	private void browseForRootFolder() {
		final String osName = System.getProperty("os.name");
		
		Optional<File> rootFolder;
		if(osName.startsWith("Mac"))
			rootFolder = browseForRootFolderMac();
		else
			rootFolder = browseForRootFolderSwing();

		rootFolder.ifPresent(this::setRootFolder);
	}
	
	
	private Optional<File> browseForRootFolderMac() {
		final String property = System.getProperty("apple.awt.fileDialogForDirectories");
		System.setProperty("apple.awt.fileDialogForDirectories", "true");
		try {
			FileDialog chooser = new FileDialog(callback.getDialogFrame(), "Choose GSEA Root Folder", FileDialog.LOAD);
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
	    chooser.setDialogTitle("Select GSEA Root Folder");
	    chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
	    chooser.setAcceptAllFileFilterUsed(false);
	    if (chooser.showOpenDialog(panel) == JFileChooser.APPROVE_OPTION) { 
	    	return Optional.of(chooser.getSelectedFile());
	    }
	    return Optional.empty();
	}
	
	
	private void setRootFolder(File root) {
		Path path = root.toPath();
		pathTextField.setText(path.toString());
		
		CheckboxListModel<Path> model = checkboxListPanel.getModel();
		
		model.clear();
		callback.clearMessage();
		
		if(!root.isDirectory()) {
			callback.setMessage(Message.ERROR, "Not a folder");
			return;
		}
		
		try {
			checkboxListPanel.getModel();
			try(Stream<Path> contents = Files.list(path)) {
				contents
				.filter(Files::isDirectory)
				.filter(new GSEAFolderPredicate())
				.map(folder -> new CheckboxData<>(folder.getFileName().toString(), folder))
				.forEach(model::addElement);
			}
			
			for(CheckboxData<Path> checkbox : model) {
				checkbox.addPropertyChangeListener("selected", evt -> updateBuildButton());
			}
		} catch(IOException e) {
			callback.setMessage(Message.ERROR, "Cannot read folder contents");
			e.printStackTrace();
			return;
		}
		
		if(model.isEmpty()) {
			callback.setMessage(Message.ERROR, "The chosen folder does not contain any GSEA results folders");
			updateBuildButton();
		}
	}
	
	private void updateBuildButton() {
		boolean hasSelected = checkboxListPanel.getModel().stream().anyMatch(cb -> cb.isSelected());
		callback.setFinishButtonEnabled(hasSelected);
	}

	
}
