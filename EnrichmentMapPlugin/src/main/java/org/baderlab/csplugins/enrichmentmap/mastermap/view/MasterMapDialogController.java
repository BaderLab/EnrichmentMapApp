package org.baderlab.csplugins.enrichmentmap.mastermap.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.FlowLayout;
import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.stream.Stream;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import org.baderlab.csplugins.enrichmentmap.dialog.NiceDialogCallback;
import org.baderlab.csplugins.enrichmentmap.dialog.NiceDialogController;
import org.baderlab.csplugins.enrichmentmap.dialog.NiceDialogCallback.Message;
import org.baderlab.csplugins.enrichmentmap.view.AboutPanel;

import com.google.inject.Singleton;

@Singleton
public class MasterMapDialogController implements NiceDialogController {
	
	private JTextField pathTextField;
	private CheckboxList checkboxList;
	private CheckboxListModel checkboxListModel;
	
	private JButton selectAllButton;
	private JButton selectNoneButton;
	
	private NiceDialogCallback callback;
	private JPanel panel;
	
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
		return new Dimension(600, 500);
	}
	
	
	@Override
	public Icon getIcon() {
		URL iconURL = AboutPanel.class.getResource("enrichmentmap_logo.png");
		ImageIcon original = new ImageIcon(iconURL);
		Image scaled = original.getImage().getScaledInstance(80, 49, Image.SCALE_SMOOTH);
		return new ImageIcon(scaled);
	}
	
	@Override
	public JPanel createBodyPanel(NiceDialogCallback callback) {
		this.callback = callback;
		
		panel = new JPanel(new BorderLayout());
		
		JPanel analysisPanel = createAnalysisTypePanel();
		JPanel browsePanel   = createBrowsePanel();
		JPanel listPanel     = createListPanel();
		
		JPanel body = new JPanel(new BorderLayout());
		body.add(browsePanel, BorderLayout.NORTH);
		body.add(listPanel, BorderLayout.CENTER);
		
		analysisPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
		
		panel.add(analysisPanel, BorderLayout.NORTH);
		panel.add(body, BorderLayout.CENTER);
		
		callback.setFinishButtonEnabled(false);
		
		return panel;
	}


	@Override
	public void finish() {
		System.out.println("CREATE MASTERMAP BOOYAKASHA");
		for(CheckboxData data : checkboxListModel) {
			System.out.println(data.getData());
		}
	}
	
	
	private JPanel createAnalysisTypePanel() {
		JLabel label = new JLabel("Analysis Type:");
		
		JRadioButton buttonGsea = new JRadioButton("GSEA");
		JRadioButton buttonGeneric = new JRadioButton("Generic/g:Profiler");
		
		ButtonGroup buttonGroup = new ButtonGroup();
		buttonGroup.add(buttonGsea);
		buttonGroup.add(buttonGeneric);
		buttonGsea.setSelected(true);
		buttonGeneric.setEnabled(false); // Temporary
		
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEADING));
		buttonPanel.add(buttonGsea);
		buttonPanel.add(buttonGeneric);
		
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(label, BorderLayout.NORTH);
		panel.add(buttonPanel, BorderLayout.CENTER);
		return panel;
	}
	
	private JPanel createBrowsePanel() {
		JPanel panel = new JPanel(new BorderLayout());
		JLabel label = new JLabel("Root folder containing GSEA result folders");
		
		pathTextField = new JTextField();
		JButton browseButton = new JButton("Browse...");
		browseButton.addActionListener(e -> browseForRootFolder());
		
		panel.add(label, BorderLayout.NORTH);
		panel.add(pathTextField, BorderLayout.CENTER);
		panel.add(browseButton, BorderLayout.EAST);
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
		
		checkboxListModel.clear();
		callback.clearMessage();
		selectAllButton.setEnabled(true);
		selectNoneButton.setEnabled(true);
		
		if(!root.isDirectory()) {
			callback.setMessage(Message.ERROR, "Not a folder");
			return;
		}
		
		try {
			try(Stream<Path> contents = Files.list(path)) {
				contents
				.filter(Files::isDirectory)
				.filter(new GSEAFolderPredicate())
				.map(folder -> new CheckboxData(folder.getFileName().toString(), folder.getFileName().toAbsolutePath().toString()))
				.forEach(checkboxListModel::addElement);
			}
			
		} catch(IOException e) {
			callback.setMessage(Message.ERROR, "Cannot read folder contents");
			e.printStackTrace();
			return;
		}
		
		if(checkboxListModel.isEmpty()) {
			callback.setMessage(Message.ERROR, "The chosen folder does not contain any GSEA results folders");
			selectAllButton.setEnabled(false);
			selectNoneButton.setEnabled(false);
		}
	}
	
	
	private JPanel createListPanel() {
		JPanel panel = new JPanel(new BorderLayout());
		
		checkboxListModel = new CheckboxListModel();
		checkboxList = new CheckboxList(checkboxListModel);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setViewportView(checkboxList);
		
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.TRAILING));
		selectAllButton  = new JButton("Select All");
		selectNoneButton = new JButton("Select None");
		
		selectAllButton.addActionListener(e -> {
			checkboxListModel.forEach(cb -> cb.setSelected(true));
			checkboxList.invalidate();
			checkboxList.repaint();
		});
		selectNoneButton.addActionListener(e -> {
			checkboxListModel.forEach(cb -> cb.setSelected(false));
			checkboxList.invalidate();
			checkboxList.repaint();
		});
		
		selectAllButton.setEnabled(false);
		selectNoneButton.setEnabled(false);
		
		buttonPanel.add(selectAllButton);
		buttonPanel.add(selectNoneButton);
		
		panel.add(scrollPane, BorderLayout.CENTER);
		panel.add(buttonPanel, BorderLayout.SOUTH);
		
		return panel;
	}

	
}
