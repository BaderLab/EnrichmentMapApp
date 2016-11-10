package org.baderlab.csplugins.enrichmentmap.view.mastermap;

import java.awt.BorderLayout;
import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.baderlab.csplugins.enrichmentmap.util.SwingUtil;
import org.baderlab.csplugins.enrichmentmap.view.util.CardDialogCallback;
import org.baderlab.csplugins.enrichmentmap.view.util.CardDialogPage;
import org.baderlab.csplugins.enrichmentmap.view.util.CheckboxData;
import org.baderlab.csplugins.enrichmentmap.view.util.CheckboxListPanel;
import org.cytoscape.util.swing.FileChooserFilter;
import org.cytoscape.util.swing.FileUtil;
import org.cytoscape.util.swing.LookAndFeelUtil;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class GenericOneExpressionPage implements CardDialogPage {

	@Inject private FileUtil fileUtil;
	
	@Inject private CutoffPropertiesPanel cutoffPanel;
	
	private CardDialogCallback callback;
	private CheckboxListPanel<Path> checkboxListPanel;
	
	
	@Override
	public String getID() {
		return "mastermap.GenericOneExpressionPage";
	}

	@Override
	public String getPageTitle() {
		return "Create Enrichment Map";
	}

	@Override
	public String getPageComboText() {
		return "Generic/gProfiler - 0/1 expression file";
	}
	
	@Override
	public void finish() {
		System.out.println("BOOYAH!");
	}

	
	@Override
	public JPanel createBodyPanel(CardDialogCallback callback) {
		this.callback = callback;
		
		JPanel textFieldPanel = createTextFieldPanel();
		JPanel enrichmentPanel = createEnrichmentFileListPanel();
		
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(textFieldPanel, BorderLayout.NORTH);
		panel.add(enrichmentPanel, BorderLayout.CENTER);
		
		return panel;
	}

	
	private JPanel createTextFieldPanel() {
		JLabel gmtLabel = new JLabel("GMT File (optional):");
		JLabel extLabel = new JLabel("Expression File (optional):");
		
		JTextField gmtPathText = new JTextField();
		JTextField expPathText = new JTextField();
		
		JButton gmtBrowseButton = new JButton("Browse...");
		JButton expBrowseButton = new JButton("Browse...");
		
		gmtBrowseButton.addActionListener(e -> browse(gmtPathText, true));
		expBrowseButton.addActionListener(e -> browse(expPathText, false));
		
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
	
	
	private void browse(JTextField textField, boolean forGMT) {
		List<FileChooserFilter> filters;
		String title;
		if(forGMT) {
			title = "GMT Files";
			filters = Arrays.asList(new FileChooserFilter("gmt Files", "gmt")); 
		} else {
			title = "Expression Files";
			FileChooserFilter gct = new FileChooserFilter("gct Files", "gct");          
	        FileChooserFilter rnk = new FileChooserFilter("rnk Files", "rnk");
	        FileChooserFilter txt = new FileChooserFilter("txt Files", "txt");
	        filters = Arrays.asList(gct, rnk, txt);
		}
		
		File file = fileUtil.getFile(callback.getDialogFrame(), title, FileUtil.LOAD, filters);
		if(file != null)
			textField.setText(file.getPath());
	}
	
	
	private List<File> browseEnrichments() {
		FileChooserFilter xls = new FileChooserFilter("gct Files", "xls");          
        FileChooserFilter bgo = new FileChooserFilter("rnk Files", "bgo");
        FileChooserFilter txt = new FileChooserFilter("txt Files", "txt");
        FileChooserFilter tsv = new FileChooserFilter("tsv Files", "tsv");
        List<FileChooserFilter> filters = Arrays.asList(xls, bgo, txt, tsv);
        File[] files = fileUtil.getFiles(callback.getDialogFrame(), "Enrichment Files", FileUtil.LOAD, filters);
        return files == null ? Collections.emptyList() : Arrays.asList(files);
	}
	
	
	private JPanel createEnrichmentFileListPanel() {
		checkboxListPanel = new CheckboxListPanel<>(true);
		checkboxListPanel.setAddButtonCallback((checkboxListModel) -> {
			List<File> files = browseEnrichments();
			for(File file : files) {
				checkboxListModel.addElement(new CheckboxData<Path>(file.getPath(), file.toPath()));
			}
		});
		
		JPanel panel = new JPanel(new BorderLayout());
		panel.setBorder(LookAndFeelUtil.createTitledBorder("Root folder containing GSEA result folders"));
		panel.add(checkboxListPanel, BorderLayout.CENTER);
		return panel;
	}
}
