package org.baderlab.csplugins.enrichmentmap.view.mastermap;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;
import static org.baderlab.csplugins.enrichmentmap.view.util.SwingUtil.makeSmall;
import static org.baderlab.csplugins.enrichmentmap.view.util.SwingUtil.validatePathTextField;

import java.awt.Color;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Optional;

import javax.annotation.Nullable;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.baderlab.csplugins.enrichmentmap.AfterInjection;
import org.baderlab.csplugins.enrichmentmap.model.EMDataSet.Method;
import org.baderlab.csplugins.enrichmentmap.parsers.ClassFileReaderTask;
import org.baderlab.csplugins.enrichmentmap.resolver.DataSetParameters;
import org.baderlab.csplugins.enrichmentmap.view.util.ComboItem;
import org.baderlab.csplugins.enrichmentmap.view.util.FileBrowser;
import org.baderlab.csplugins.enrichmentmap.view.util.SwingUtil;
import org.cytoscape.util.swing.FileUtil;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.util.swing.LookAndFeelUtil;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

@SuppressWarnings("serial")
public class EditDataSetCompactPanel extends JPanel {
	
	@Inject private FileUtil fileUtil;
	@Inject private IconManager iconManager;
	
	private JComboBox<ComboItem<Method>> analysisTypeCombo;
	private JTextField nameText;
	private JTextField enrichments1Text;
	private JTextField enrichments2Text;
	private JTextField expressionsText;
	private JTextField ranksText;
	private JTextField classesText;
	private JTextField positiveText;
	private JTextField negativeText;
	private JLabel enrichments2Label;
	private JButton enrichments2Browse;
	
	private Color textFieldForeground;
	
	private final @Nullable DataSetParameters initDataSet;
	
	
	public interface Factory {
		EditDataSetCompactPanel create(@Nullable DataSetParameters initDataSet);
	}
	
	@Inject
	public EditDataSetCompactPanel(@Assisted @Nullable DataSetParameters initDataSet) {
		this.initDataSet = initDataSet;
	}
	
	@AfterInjection
	private void createContents() {
		JLabel nameLabel = new JLabel("Name:");
		nameText = new JTextField();
		textFieldForeground = nameText.getForeground();
		nameText.setText(initDataSet != null ? initDataSet.getName() : null);
		
		JLabel analysisLabel = new JLabel("Analysis Type:");
		analysisTypeCombo = new JComboBox<>();
		analysisTypeCombo.addItem(new ComboItem<>(Method.GSEA, Method.GSEA.getLabel()));
		analysisTypeCombo.addItem(new ComboItem<>(Method.Generic, Method.Generic.getLabel()));
		analysisTypeCombo.addItem(new ComboItem<>(Method.Specialized, Method.Specialized.getLabel()));

		JLabel enrichmentsLabel = new JLabel("Enrichments:");
		enrichments1Text = new JTextField();
		JButton enrichmentsBrowse = createBrowseButton();
		enrichmentsBrowse.addActionListener(e -> browse(enrichments1Text, FileBrowser.Filter.ENRICHMENT));
		
		enrichments2Label = new JLabel("Enrichments 2:");
		enrichments2Text = new JTextField();
		enrichments2Text.setText(initDataSet != null ? initDataSet.getFiles().getEnrichmentFileName1() : null);
		enrichments2Browse = createBrowseButton();
		enrichments2Browse.addActionListener(e -> browse(enrichments2Text, FileBrowser.Filter.ENRICHMENT));
		
		JLabel expressionsLabel = new JLabel("Expressions:");
		expressionsText = new JTextField();
		expressionsText.setText(initDataSet != null ? initDataSet.getFiles().getExpressionFileName() : null);
		JButton expressionsBrowse = createBrowseButton();
		expressionsBrowse.addActionListener(e -> browse(expressionsText, FileBrowser.Filter.EXPRESSION));
		
		makeSmall(nameLabel, nameText, analysisLabel, analysisTypeCombo);
		makeSmall(enrichmentsLabel, enrichments1Text, enrichmentsBrowse);
		makeSmall(enrichments2Label, enrichments2Text, enrichments2Browse);
		makeSmall(expressionsLabel, expressionsText, expressionsBrowse);
		
		JLabel ranksLabel = new JLabel("Ranks:");
		ranksText = new JTextField();
		ranksText.setText(initDataSet != null ? initDataSet.getFiles().getRankedFile() : null);
		JButton ranksBrowse = createBrowseButton();
		ranksBrowse.addActionListener(e -> browse(ranksText, FileBrowser.Filter.RANK));
		
		JLabel classesLabel = new JLabel("Classes:");
		classesText = new JTextField();
		classesText.setText(initDataSet != null ? initDataSet.getFiles().getClassFile() : null);
		JButton classesBrowse = createBrowseButton();
		classesText.getDocument().addDocumentListener(SwingUtil.simpleDocumentListener(this::updateClasses));
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
		
		
		GroupLayout layout = new GroupLayout(this);
		setLayout(layout);
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(true);
	   		
		layout.setHorizontalGroup(
			layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup(Alignment.TRAILING)
					.addComponent(nameLabel)
					.addComponent(analysisLabel)
					.addComponent(enrichmentsLabel)
					.addComponent(enrichments2Label)
					.addComponent(expressionsLabel)
					.addComponent(ranksLabel)
					.addComponent(classesLabel)
				)
				.addGroup(layout.createParallelGroup(Alignment.LEADING, true)
					.addComponent(nameText, 0, DEFAULT_SIZE, Short.MAX_VALUE)
					.addComponent(analysisTypeCombo, 0, DEFAULT_SIZE, Short.MAX_VALUE)
					.addComponent(enrichments1Text, 0, DEFAULT_SIZE, Short.MAX_VALUE)
					.addComponent(enrichments2Text, 0, DEFAULT_SIZE, Short.MAX_VALUE)
					.addComponent(expressionsText, 0, DEFAULT_SIZE, Short.MAX_VALUE)
					.addComponent(ranksText,   0, DEFAULT_SIZE, Short.MAX_VALUE)
					.addComponent(classesText, 0, DEFAULT_SIZE, Short.MAX_VALUE)
					.addGroup(layout.createSequentialGroup()
						.addComponent(positive)
						.addComponent(positiveText, 75, 75, 75)
						.addGap(20)
						.addComponent(negative)
						.addComponent(negativeText, 75, 75, 75)
					)
				)
				.addGroup(layout.createParallelGroup()
					.addComponent(enrichmentsBrowse)
					.addComponent(enrichments2Browse)
					.addComponent(expressionsBrowse)
					.addComponent(ranksBrowse)
					.addComponent(classesBrowse)
				)
		);
		
		layout.setVerticalGroup(
			layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup(Alignment.BASELINE)
					.addComponent(nameLabel)
					.addComponent(nameText)
				)
				.addGroup(layout.createParallelGroup(Alignment.BASELINE)
					.addComponent(analysisLabel)
					.addComponent(analysisTypeCombo)
				)
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
		
   		if(LookAndFeelUtil.isAquaLAF())
   			setOpaque(false);
	}
	
	
	private void browse(JTextField textField, FileBrowser.Filter filter) {
		Optional<Path> path = FileBrowser.browse(fileUtil, this, filter);
		path.map(Path::toString).ifPresent(textField::setText);
		//validateInput();
	}
	
	
	private void updateClasses() {
		if(positiveText.getText().trim().isEmpty() && negativeText.getText().trim().isEmpty() && validatePathTextField(classesText, textFieldForeground, true)) {
			String classFile = classesText.getText();
			String[] phenotypes = ClassFileReaderTask.parseClasses(classFile);
			if(phenotypes != null) {
				LinkedHashSet<String> distinctOrdererd = new LinkedHashSet<>();
				for(String p : phenotypes) {
					distinctOrdererd.add(p);
				}
				if(distinctOrdererd.size() >= 2) {
					Iterator<String> iter = distinctOrdererd.iterator();
					positiveText.setText(iter.next());
					negativeText.setText(iter.next());
				}
			}
		}
	}
	
	public JButton createBrowseButton() {
		JButton button = new JButton(IconManager.ICON_ELLIPSIS_H);
		button.setFont(iconManager.getIconFont(10.0f));
		button.setToolTipText("Browse...");
		if(LookAndFeelUtil.isAquaLAF()) {
			button.putClientProperty("JButton.buttonType", "gradient");
			button.putClientProperty("JComponent.sizeVariant", "small");
		}
		return button;
	}

}
