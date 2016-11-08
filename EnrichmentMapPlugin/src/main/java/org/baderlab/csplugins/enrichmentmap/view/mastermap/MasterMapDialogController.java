package org.baderlab.csplugins.enrichmentmap.view.mastermap;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import javax.swing.ButtonGroup;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import org.baderlab.csplugins.enrichmentmap.model.EMCreationParameters;
import org.baderlab.csplugins.enrichmentmap.model.EMCreationParameters.Method;
import org.baderlab.csplugins.enrichmentmap.model.EMCreationParameters.SimilarityMetric;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentResultFilterParams.NESFilter;
import org.baderlab.csplugins.enrichmentmap.model.LegacySupport;
import org.baderlab.csplugins.enrichmentmap.task.MasterMapGSEATaskFactory;
import org.baderlab.csplugins.enrichmentmap.util.SwingUtil;
import org.baderlab.csplugins.enrichmentmap.view.AboutDialog;
import org.baderlab.csplugins.enrichmentmap.view.util.CheckboxData;
import org.baderlab.csplugins.enrichmentmap.view.util.CheckboxListModel;
import org.baderlab.csplugins.enrichmentmap.view.util.CheckboxListPanel;
import org.baderlab.csplugins.enrichmentmap.view.util.NiceDialogCallback;
import org.baderlab.csplugins.enrichmentmap.view.util.NiceDialogCallback.Message;
import org.baderlab.csplugins.enrichmentmap.view.util.NiceDialogController;
import org.cytoscape.util.swing.LookAndFeelUtil;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.swing.DialogTaskManager;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class MasterMapDialogController implements NiceDialogController {
	
	@Inject private DialogTaskManager taskManager;
	@Inject private CutoffPropertiesPanel cutoffPanel;
	@Inject private LegacySupport legacySupport;
	@Inject private MasterMapGSEATaskFactory.Factory taskFactoryFactory;
	
	private NiceDialogCallback callback;
	private JPanel panel;
	
	private JRadioButton gseaRadio;
	private JRadioButton genericRadio;
	
	private JTextField pathTextField;
	private CheckboxListPanel<Path> checkboxListPanel;
	
	
	@Override
	public String getTitle() {
		return "MasterMap";
	}
	
	@Override
	public String getSubTitle() {
		return "Create MasterMap Network";
	}
	
	@Override
	public String getFinishButtonText() {
		return "Build";
	}
	
	@Override
	public Dimension getMinimumSize() {
		return new Dimension(750, 700);
	}
	

	@Override
	public void finish() {
		Method method = getMethod();
		String prefix = legacySupport.getNextAttributePrefix();
		SimilarityMetric similarityMetric = cutoffPanel.getSimilarityMetric();
		double pvalue = cutoffPanel.getPValue();
		double qvalue = cutoffPanel.getQValue();
		NESFilter nesFilter = cutoffPanel.getNESFilter();
		double cutoff = cutoffPanel.getCutoff();
		double combined = cutoffPanel.getCombinedConstant();
		Optional<Integer> minExperiments = cutoffPanel.getMinimumExperiments();
		
		EMCreationParameters params = 
			new EMCreationParameters(method, prefix, pvalue, qvalue, nesFilter, minExperiments, similarityMetric, cutoff, combined);
		
		List<Path> paths = checkboxListPanel.getSelectedDataItems();
		
		MasterMapGSEATaskFactory taskFactory = taskFactoryFactory.create(params, paths);
		TaskIterator tasks = taskFactory.createTaskIterator();
		
		// Close this dialog after the progress dialog finishes normally
		tasks.append(new AbstractTask() {
			public void run(TaskMonitor taskMonitor) {
				callback.close();
			}
		});
		
		taskManager.execute(tasks);
	}
	
	public Method getMethod() {
		if(gseaRadio.isSelected())
			return Method.GSEA;
		else
			return Method.Generic;
	}
	
	
	@Override
	public Icon getIcon() {
		URL iconURL = AboutDialog.class.getResource("enrichmentmap_logo.png");
		ImageIcon original = new ImageIcon(iconURL);
		Image scaled = original.getImage().getScaledInstance(80, 49, Image.SCALE_SMOOTH);
		return new ImageIcon(scaled);
	}
	
	@Override
	public JPanel createBodyPanel(NiceDialogCallback callback) {
		this.callback = callback;
		
		JPanel analysisPanel = createAnalysisTypePanel();
		JPanel gseaPanel     = createGSEAPanel();
		
		panel = new JPanel(new BorderLayout());
		GroupLayout layout = new GroupLayout(panel);
		panel.setLayout(layout);
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(true);
		
		layout.setHorizontalGroup(
			layout.createParallelGroup()
				.addComponent(analysisPanel)
				.addComponent(gseaPanel)
				.addComponent(cutoffPanel)
		);
		layout.setVerticalGroup(
			layout.createSequentialGroup()
				.addComponent(analysisPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
				.addComponent(gseaPanel)
				.addComponent(cutoffPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
		);
		
		callback.setFinishButtonEnabled(false);
		
		return panel;
	}

	
	private JPanel createAnalysisTypePanel() {
		gseaRadio = new JRadioButton("GSEA");
		genericRadio = new JRadioButton("Generic/g:Profiler");
		
		ButtonGroup buttonGroup = new ButtonGroup();
		buttonGroup.add(gseaRadio);
		buttonGroup.add(genericRadio);
		gseaRadio.setSelected(true);
		genericRadio.setEnabled(false); // Temporary
		
		SwingUtil.makeSmall(gseaRadio, genericRadio);

		JPanel panel = new JPanel();
		panel.setBorder(LookAndFeelUtil.createTitledBorder("Analysis Type"));
		
		final GroupLayout layout = new GroupLayout(panel);
		panel.setLayout(layout);
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(true);
	   		
   		layout.setHorizontalGroup(layout.createSequentialGroup()
			.addComponent(gseaRadio)
			.addComponent(genericRadio)
			.addGap(0, 0, Short.MAX_VALUE)
   		);
   		layout.setVerticalGroup(layout.createParallelGroup(Alignment.LEADING, true)
			.addComponent(gseaRadio, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
			.addComponent(genericRadio, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
   		);
   		
   		if (LookAndFeelUtil.isAquaLAF())
			panel.setOpaque(false);
   		
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
