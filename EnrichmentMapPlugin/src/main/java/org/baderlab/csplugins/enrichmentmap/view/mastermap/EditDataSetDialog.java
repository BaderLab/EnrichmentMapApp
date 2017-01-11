package org.baderlab.csplugins.enrichmentmap.view.mastermap;

import static com.google.common.base.Strings.isNullOrEmpty;
import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;
import static org.baderlab.csplugins.enrichmentmap.view.util.SwingUtil.makeSmall;
import static org.baderlab.csplugins.enrichmentmap.view.util.SwingUtil.simpleDocumentListener;
import static org.baderlab.csplugins.enrichmentmap.view.util.SwingUtil.validatePathTextField;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;
import javax.swing.AbstractAction;
import javax.swing.ButtonGroup;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import org.baderlab.csplugins.enrichmentmap.model.DataSet.Method;
import org.baderlab.csplugins.enrichmentmap.model.DataSetFiles;
import org.baderlab.csplugins.enrichmentmap.parsers.DatasetLineParser;
import org.baderlab.csplugins.enrichmentmap.resolver.DataSetParameters;
import org.baderlab.csplugins.enrichmentmap.resolver.GSEAResolver;
import org.baderlab.csplugins.enrichmentmap.view.util.FileBrowser;
import org.cytoscape.util.swing.BasicCollapsiblePanel;
import org.cytoscape.util.swing.FileUtil;
import org.cytoscape.util.swing.LookAndFeelUtil;

/**
 * MKTODO set the phenotypes automatically by scanning the classes file
 * MKTODO add validation
 */
@SuppressWarnings("serial")
public class EditDataSetDialog extends JDialog {
	
	private final FileUtil fileUtil;
	
	private JRadioButton gseaRadio;
	private JRadioButton genericRadio;
	private JRadioButton davidRadio;
	
	private JTextField enrichments1Text;
	private JTextField enrichments2Text;
	private JTextField expressionsText;
	private JTextField ranksText;
	private JTextField classesText;
	private JTextField positiveText;
	private JTextField negativeText;
	
	private BasicCollapsiblePanel advancedPanel;
	private JLabel enrichments2Label;
	private JButton okButton;
	private JButton rptButton;
	private JButton enrichments2Browse;
	
	private String[] classes;
	
	boolean okClicked = false;
	

	public EditDataSetDialog(JDialog parent, FileUtil fileUtil, @Nullable DataSetParameters initDataSet) {
		super(parent, null, true);
		this.fileUtil = fileUtil;
		setMinimumSize(new Dimension(650, 450));
		setResizable(true);
		setTitle(initDataSet == null ? "Add Enrichment Results" : "Edit Enrichment Results");
		createContents(initDataSet);
		pack();
		setLocationRelativeTo(parent);
		validateInput();
	}
	
	
	public DataSetParameters open() {
		setVisible(true); // must be modal for this to work
		if(!okClicked)
			return null;
		return createDataSetParameters();
	}
	
	private DataSetParameters createDataSetParameters() {
		String name = "My DataSet";
		Method method = getMethod();
		
		DataSetFiles files = new DataSetFiles();
		
		String enrichmentFileName1 = enrichments1Text.getText();
		if(!isNullOrEmpty(enrichmentFileName1))
			files.setEnrichmentFileName1(enrichmentFileName1);
		
		String enrichmentFileName2 = enrichments2Text.getText();
		if(!isNullOrEmpty(enrichmentFileName2))
			files.setEnrichmentFileName1(enrichmentFileName2);
		
		String expressionFileName = expressionsText.getText();
		if(!isNullOrEmpty(expressionFileName))
			files.setExpressionFileName(expressionFileName);
		
		String ranksFileName = ranksText.getText();
		if(!isNullOrEmpty(ranksFileName))
			files.setRankedFile(ranksFileName);
		
		String classesFileName = classesText.getText();
		if(!isNullOrEmpty(classesFileName))
			files.setClassFile(classesFileName);
		
		String positive = positiveText.getText();
		String negative = negativeText.getText();
		if(!isNullOrEmpty(positive) && !isNullOrEmpty(negative) && classes != null) {
			files.setPhenotype1(positive);
			files.setPhenotype2(negative);
			files.setTemp_class1(classes);
		}
		
		return new DataSetParameters(name, method, files);
	}
	
	
	private void createContents(@Nullable DataSetParameters initDataSet) {
		JPanel analysisTypePanel = createAnalysisTypePanel(initDataSet);
		JPanel textFieldPanel = createTextFieldPanel(initDataSet);
		JPanel phenotypePanel = createPhenotypesPanel(initDataSet);
		JPanel buttonPanel = createButtonPanel();
		
		Container contentPane = this.getContentPane();
		GroupLayout layout = new GroupLayout(contentPane);
		contentPane.setLayout(layout);
		
		layout.setHorizontalGroup(
			layout.createParallelGroup()
				.addComponent(analysisTypePanel)
				.addComponent(textFieldPanel)
				.addComponent(phenotypePanel)
				.addComponent(buttonPanel)
		);
		layout.setVerticalGroup(
			layout.createSequentialGroup()
				.addComponent(analysisTypePanel)
				.addComponent(textFieldPanel)
				.addComponent(phenotypePanel)
				.addGap(0, 0, Short.MAX_VALUE)
				.addComponent(buttonPanel)
		);
	}
	

	private JPanel createAnalysisTypePanel(@Nullable DataSetParameters initDataSet) {
		gseaRadio    = new JRadioButton("GSEA", true);
		genericRadio = new JRadioButton("generic/gProfiler");
		davidRadio   = new JRadioButton("DAVID/BiNGO/Great");

		makeSmall(gseaRadio, genericRadio, davidRadio);

		ButtonGroup buttonGroup = new ButtonGroup();
		buttonGroup.add(gseaRadio);
		buttonGroup.add(genericRadio);
		buttonGroup.add(davidRadio);
		
		gseaRadio.addActionListener(e -> updateEnablement());
		genericRadio.addActionListener(e -> updateEnablement());
		davidRadio.addActionListener(e -> updateEnablement());
		
		if(initDataSet != null) {
			gseaRadio.setSelected(initDataSet.getMethod() == Method.GSEA);
			genericRadio.setSelected(initDataSet.getMethod() == Method.Generic);
			davidRadio.setSelected(initDataSet.getMethod() == Method.Specialized);
		}

		JPanel panel = new JPanel();
		GroupLayout layout = new GroupLayout(panel);
		panel.setLayout(layout);
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(true);
	   		
   		layout.setHorizontalGroup(layout.createSequentialGroup()
			.addComponent(gseaRadio)
			.addComponent(genericRadio)
			.addComponent(davidRadio)
			.addGap(0, 0, Short.MAX_VALUE)
   		);
   		layout.setVerticalGroup(layout.createParallelGroup(Alignment.CENTER, true)
			.addComponent(gseaRadio,    PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
			.addComponent(genericRadio, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
			.addComponent(davidRadio,   PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
   		);
   		
   		panel.setBorder(LookAndFeelUtil.createTitledBorder("Analysis Type"));
   		if (LookAndFeelUtil.isAquaLAF())
			panel.setOpaque(false);
		return panel;
	}
	

	private JPanel createTextFieldPanel(@Nullable DataSetParameters initDataSet) {
		JLabel enrichmentsLabel = new JLabel("Enrichments:");
		enrichments1Text = new JTextField();
		enrichments1Text.setText(initDataSet != null ? initDataSet.getFiles().getEnrichmentFileName1() : null);
		JButton enrichmentsBrowse = new JButton("Browse...");
		enrichments1Text.getDocument().addDocumentListener(simpleDocumentListener(this::validateInput));
		enrichmentsBrowse.addActionListener(e -> browse(enrichments1Text, FileBrowser.Filter.ENRICHMENT));
		
		enrichments2Label = new JLabel("Enrichments 2:");
		enrichments2Text = new JTextField();
		enrichments2Text.setText(initDataSet != null ? initDataSet.getFiles().getEnrichmentFileName1() : null);
		enrichments2Browse = new JButton("Browse...");
		enrichments2Text.getDocument().addDocumentListener(simpleDocumentListener(this::validateInput));
		enrichments2Browse.addActionListener(e -> browse(enrichments2Text, FileBrowser.Filter.ENRICHMENT));
		
		JLabel expressionsLabel = new JLabel("Expressions:");
		expressionsText = new JTextField();
		expressionsText.setText(initDataSet != null ? initDataSet.getFiles().getExpressionFileName() : null);
		JButton expressionsBrowse = new JButton("Browse...");
		expressionsText.getDocument().addDocumentListener(simpleDocumentListener(this::validateInput));
		expressionsBrowse.addActionListener(e -> browse(expressionsText, FileBrowser.Filter.EXPRESSION));
		
		makeSmall(enrichmentsLabel, enrichments1Text, enrichmentsBrowse);
		makeSmall(enrichments2Label, enrichments2Text, enrichments2Browse);
		makeSmall(expressionsLabel, expressionsText, expressionsBrowse);
		
		JPanel panel = new JPanel();
		GroupLayout layout = new GroupLayout(panel);
		panel.setLayout(layout);
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(true);

		layout.setHorizontalGroup(
			layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup(Alignment.TRAILING)
					.addComponent(enrichmentsLabel)
					.addComponent(enrichments2Label)
					.addComponent(expressionsLabel)
				)
				.addGroup(layout.createParallelGroup(Alignment.LEADING, true)
					.addComponent(enrichments1Text, 0, DEFAULT_SIZE, Short.MAX_VALUE)
					.addComponent(enrichments2Text, 0, DEFAULT_SIZE, Short.MAX_VALUE)
					.addComponent(expressionsText, 0, DEFAULT_SIZE, Short.MAX_VALUE)
				)
				.addGroup(layout.createParallelGroup()
					.addComponent(enrichmentsBrowse)
					.addComponent(enrichments2Browse)
					.addComponent(expressionsBrowse)
				)
		);
		
		layout.setVerticalGroup(
			layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup(Alignment.BASELINE)
					.addComponent(enrichmentsLabel)
					.addComponent(enrichments1Text)
					.addComponent(enrichmentsBrowse)
				)
				.addGroup(layout.createParallelGroup(Alignment.BASELINE)
					.addComponent(enrichments2Label)
					.addComponent(enrichments2Text)
					.addComponent(enrichments2Browse)
				)
				.addGroup(layout.createParallelGroup(Alignment.BASELINE)
					.addComponent(expressionsLabel)
					.addComponent(expressionsText)
					.addComponent(expressionsBrowse)
				)
		);
		
		panel.setBorder(LookAndFeelUtil.createTitledBorder("Data Files"));
   		if (LookAndFeelUtil.isAquaLAF())
			panel.setOpaque(false);
		return panel;
	}
	
	
	private JPanel createPhenotypesPanel(@Nullable DataSetParameters initDataSet) {
		JLabel ranksLabel = new JLabel("Ranks:");
		ranksText = new JTextField();
		ranksText.setText(initDataSet != null ? initDataSet.getFiles().getRankedFile() : null);
		JButton ranksBrowse = new JButton("Browse...");
		ranksText.getDocument().addDocumentListener(simpleDocumentListener(this::validateInput));
		ranksBrowse.addActionListener(e -> browse(ranksText, FileBrowser.Filter.RANK));
		
		JLabel classesLabel = new JLabel("Classes:");
		classesText = new JTextField();
		classesText.setText(initDataSet != null ? initDataSet.getFiles().getClassFile() : null);
		JButton classesBrowse = new JButton("Browse...");
		classesText.getDocument().addDocumentListener(simpleDocumentListener(this::updateClasses));
		classesBrowse.addActionListener(e -> browse(classesText, FileBrowser.Filter.CLASS));
		
		makeSmall(ranksLabel, ranksText, ranksBrowse);
		makeSmall(classesLabel, classesText, classesBrowse);
		
		JLabel positive = new JLabel("Positive:");
		JLabel negative = new JLabel("Negative:");
		positiveText = new JTextField();
		negativeText = new JTextField();
		positiveText.setText(initDataSet != null ? initDataSet.getFiles().getPhenotype1() : null);
		negativeText.setText(initDataSet != null ? initDataSet.getFiles().getPhenotype2() : null);
		
		makeSmall(positive, negative, positiveText, negativeText);
		
		advancedPanel = new BasicCollapsiblePanel("Advanced");
		GroupLayout layout = new GroupLayout(advancedPanel.getContentPane());
		advancedPanel.getContentPane().setLayout(layout);
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(true);
		
		if(initDataSet != null) {
			DataSetFiles files = initDataSet.getFiles();
			if(!isNullOrEmpty(files.getClassFile()) || !isNullOrEmpty(files.getRankedFile())) {
				advancedPanel.setCollapsed(false);
			}
		}

		layout.setHorizontalGroup(
			layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup(Alignment.TRAILING)
					.addComponent(ranksLabel)
					.addComponent(classesLabel)
				)
				.addGroup(layout.createParallelGroup(Alignment.LEADING, true)
					.addComponent(ranksText,   0, DEFAULT_SIZE, Short.MAX_VALUE)
					.addComponent(classesText, 0, DEFAULT_SIZE, Short.MAX_VALUE)
					.addGroup(layout.createSequentialGroup()
						.addComponent(positive)
						.addComponent(positiveText, 100, 100, 100)
						.addGap(20)
						.addComponent(negative)
						.addComponent(negativeText, 100, 100, 100)
					)
				)
				.addGroup(layout.createParallelGroup()
					.addComponent(ranksBrowse)
					.addComponent(classesBrowse)
				)
		);
		
		layout.setVerticalGroup(
			layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup(Alignment.BASELINE)
					.addComponent(ranksLabel)
					.addComponent(ranksText)
					.addComponent(ranksBrowse)
				)
				.addGroup(layout.createParallelGroup(Alignment.BASELINE)
					.addComponent(classesLabel)
					.addComponent(classesText)
					.addComponent(classesBrowse)
				)
				.addGroup(layout.createParallelGroup(Alignment.BASELINE)
					.addComponent(positive)
					.addComponent(positiveText, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addComponent(negative)
					.addComponent(negativeText, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				)
		);
			
   		if (LookAndFeelUtil.isAquaLAF())
   			advancedPanel.setOpaque(false);
		return advancedPanel;
	}
	
	
	private JPanel createButtonPanel() {
		okButton = new JButton(new AbstractAction("OK") {
			public void actionPerformed(ActionEvent e) {
				okClicked = true;
				dispose();
			}
		});
		JButton cancelButton = new JButton(new AbstractAction("Cancel") {
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});
		rptButton = new JButton(new AbstractAction("Load RPT file...") { 
			public void actionPerformed(ActionEvent e) {
				loadRptFile();
			}
		});

		JPanel buttonPanel = LookAndFeelUtil.createOkCancelPanel(okButton, cancelButton, rptButton);
		LookAndFeelUtil.setDefaultOkCancelKeyStrokes(getRootPane(), okButton.getAction(), cancelButton.getAction());
		getRootPane().setDefaultButton(okButton);
		return buttonPanel;
	}
	
	
	private void updateEnablement() {
		boolean isGsea = gseaRadio.isSelected();
		rptButton.setEnabled(isGsea);
		enrichments2Label.setEnabled(isGsea);
		enrichments2Text.setEnabled(isGsea);
		enrichments2Browse.setEnabled(isGsea);
	}
	
	
	private void validateInput() {
		boolean valid = true;
		valid &= validatePathTextField(enrichments1Text);
		valid &= validatePathTextField(enrichments2Text);
		valid &= validatePathTextField(expressionsText);
		valid &= validatePathTextField(ranksText);
		valid &= validatePathTextField(classesText);
		okButton.setEnabled(valid);
	}

	
	private void loadRptFile() {
		Optional<Path> rptPath = FileBrowser.browse(fileUtil, this, FileBrowser.Filter.RPT);
		boolean error = false;
		if(rptPath.isPresent()) {
			try {
				Optional<DataSetParameters> dataset = GSEAResolver.resolveRPTFile(rptPath.get());
				if(dataset.isPresent()) {
					DataSetFiles files = dataset.get().getFiles();
					setText(enrichments1Text, files.getEnrichmentFileName1());
					setText(enrichments2Text, files.getEnrichmentFileName2());
					setText(expressionsText, files.getExpressionFileName());
					setText(ranksText, files.getRankedFile());
					setText(classesText, files.getClassFile());
					setText(positiveText, files.getPhenotype1());
					setText(negativeText, files.getPhenotype2());
					
					// MKTODO expand the advanced section
					advancedPanel.setCollapsed(false);
				} else {
					error = true;
				}
			} catch (IOException e) {
				error = true;
			}
		}
		if(error) {
			JOptionPane.showMessageDialog(this, "Could not load the RPT file.", "Error loading RPT file", JOptionPane.WARNING_MESSAGE);
		}
	}
	
	private static void setText(JTextField textField, String s) {
		textField.setText(isNullOrEmpty(s) ? "" : s);
	}
	
	
	private void updateClasses() {
		if(positiveText.getText().trim().isEmpty() && negativeText.getText().trim().isEmpty() && validatePathTextField(classesText)) {
			String classFile = classesText.getText();
			List<String> phenotypes = parseClasses(classFile);
			if(phenotypes != null) {
				LinkedHashSet<String> distinctOrdererd = new LinkedHashSet<>(phenotypes);
				if(distinctOrdererd.size() >= 2) {
					Iterator<String> iter = distinctOrdererd.iterator();
					positiveText.setText(iter.next());
					negativeText.setText(iter.next());
				}
			}
		}
	}
	
	private static List<String> parseClasses(String classFile) {
		if (isNullOrEmpty(classFile))
			return Arrays.asList("NA_pos", "NA_neg");

		File f = new File(classFile);
		if(!f.exists())
			return null;

		try {
			List<String> lines = DatasetLineParser.readLines(classFile, 4);

			/*
			 * GSEA class files will have 3 lines in the following format: 6 2 1
			 * # R9C_8W WT_8W R9C_8W R9C_8W R9C_8W WT_8W WT_8W WT_8W
			 * 
			 * If the file has 3 lines assume it is a GSEA and get the
			 * phenotypes from the third line. If the file only has 1 line
			 * assume that it is a generic class file and get the phenotypes
			 * from the single line
			 * the class file can be split by a space or a tab
			 */
			if(lines.size() >= 3)
				return Arrays.asList(lines.get(2).split("\\s"));
			else if(lines.size() == 1)
				return Arrays.asList(lines.get(0).split("\\s"));
			else
				return null;
			
		} catch (IOException ie) {
			System.err.println("unable to open class file: " + classFile);
			return null;
		}
	}
	
	
	private void browse(JTextField textField, FileBrowser.Filter filter) {
		Optional<Path> path = FileBrowser.browse(fileUtil, this, filter);
		path.map(Path::toString).ifPresent(textField::setText);
		validateInput();
	}
	
	
	private Method getMethod() {
		if(gseaRadio.isSelected())
			return Method.GSEA;
		else if(genericRadio.isSelected())
			return Method.Generic;
		else
			return Method.Specialized;
	}
}
