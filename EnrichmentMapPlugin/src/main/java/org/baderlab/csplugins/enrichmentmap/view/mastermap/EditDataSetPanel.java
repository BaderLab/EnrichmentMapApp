package org.baderlab.csplugins.enrichmentmap.view.mastermap;

import static com.google.common.base.Strings.isNullOrEmpty;
import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;
import static org.baderlab.csplugins.enrichmentmap.view.util.SwingUtil.makeSmall;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.baderlab.csplugins.enrichmentmap.AfterInjection;
import org.baderlab.csplugins.enrichmentmap.model.DataSetFiles;
import org.baderlab.csplugins.enrichmentmap.model.EMDataSet.Method;
import org.baderlab.csplugins.enrichmentmap.parsers.ClassFileReaderTask;
import org.baderlab.csplugins.enrichmentmap.resolver.DataSetParameters;
import org.baderlab.csplugins.enrichmentmap.view.util.ComboItem;
import org.baderlab.csplugins.enrichmentmap.view.util.FileBrowser;
import org.baderlab.csplugins.enrichmentmap.view.util.SwingUtil;
import org.cytoscape.util.swing.FileUtil;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.util.swing.LookAndFeelUtil;

import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.assistedinject.Assisted;

@SuppressWarnings("serial")
public class EditDataSetPanel extends JPanel implements DetailPanel {
	 
	public static final String PROP_NAME = "dataSetName";
	
	@Inject private FileUtil fileUtil;
	@Inject private IconManager iconManager;
	@Inject private Provider<JFrame> jframe;
	
	private JComboBox<ComboItem<Method>> analysisTypeCombo;
	private JTextField nameText;
	private JTextField enrichments1Text;
	private JTextField enrichments2Text;
	private JTextField expressionsText;
	private JTextField gmtText;
	private JTextField ranksText;
	private JTextField classesText;
	private JTextField positiveText;
	private JTextField negativeText;
	private JLabel enrichments2Label;
	private JButton enrichments2Browse;
	
	private String[] classes;
	
	private final @Nullable DataSetParameters initDataSet;
	
	
	public interface Factory {
		EditDataSetPanel create(@Nullable DataSetParameters initDataSet);
	}
	
	@Inject
	public EditDataSetPanel(@Assisted @Nullable DataSetParameters initDataSet) {
		this.initDataSet = initDataSet;
	}
	
	public String getDataSetName() {
		return nameText.getText();
	}
	
	@Override
	public String getDisplayName() {
		String m = analysisTypeCombo.getSelectedItem().toString();
		return nameText.getText() + "  (" + m + ")";
	}
	
	@Override
	public JPanel getPanel() {
		return this;
	}

	@Override
	public String getIcon() {
		return IconManager.ICON_FILE_TEXT_O;
	}
	
	@Override
	public DataSetParameters createDataSetParameters() {
		String name = nameText.getText().trim();
		Method method = getMethod();
		
		DataSetFiles files = new DataSetFiles();
		
		String enrichmentFileName1 = enrichments1Text.getText();
		if(!isNullOrEmpty(enrichmentFileName1))
			files.setEnrichmentFileName1(enrichmentFileName1);
		
		String enrichmentFileName2 = enrichments2Text.getText();
		if(!isNullOrEmpty(enrichmentFileName2))
			files.setEnrichmentFileName2(enrichmentFileName2);
		
		String expressionFileName = expressionsText.getText();
		if(!isNullOrEmpty(expressionFileName))
			files.setExpressionFileName(expressionFileName);
		
		String gmtFileName = gmtText.getText();
		if(!isNullOrEmpty(gmtFileName))
			files.setGMTFileName(gmtFileName);
		
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
	
	
	@AfterInjection
	private void createContents() {
		JLabel nameLabel = new JLabel("* Name:");
		nameText = new JTextField();
		nameText.setText(initDataSet != null ? initDataSet.getName() : null);
		nameText.getDocument().addDocumentListener(SwingUtil.simpleDocumentListener(() -> 
			firePropertyChange(PROP_NAME, null, getDisplayName())
		));
		
		JLabel analysisLabel = new JLabel("* Analysis Type:");
		analysisTypeCombo = new JComboBox<>();
		analysisTypeCombo.addItem(new ComboItem<>(Method.GSEA, Method.GSEA.getLabel()));
		analysisTypeCombo.addItem(new ComboItem<>(Method.Generic, Method.Generic.getLabel()));
		analysisTypeCombo.addItem(new ComboItem<>(Method.Specialized, Method.Specialized.getLabel()));
		analysisTypeCombo.addActionListener(e -> 
			firePropertyChange(PROP_NAME, null, getDisplayName())
		);

		JLabel enrichmentsLabel = new JLabel("* Enrichments:");
		enrichments1Text = new JTextField();
		enrichments1Text.setText(initDataSet != null ? initDataSet.getFiles().getEnrichmentFileName1() : null);
		JButton enrichmentsBrowse = createBrowseButton(iconManager);
		enrichmentsBrowse.addActionListener(e -> browse(enrichments1Text, FileBrowser.Filter.ENRICHMENT));
		
		enrichments2Label = new JLabel("Enrichments 2:");
		enrichments2Text = new JTextField();
		enrichments2Text.setText(initDataSet != null ? initDataSet.getFiles().getEnrichmentFileName2() : null);
		enrichments2Browse = createBrowseButton(iconManager);
		enrichments2Browse.addActionListener(e -> browse(enrichments2Text, FileBrowser.Filter.ENRICHMENT));
		
		JLabel gmtLabel = new JLabel("GMT:");
		gmtText = new JTextField();
		gmtText.setText(initDataSet != null ? initDataSet.getFiles().getGMTFileName() : null);
		JButton gmtBrowse = createBrowseButton(iconManager);
		gmtBrowse.addActionListener(e -> browse(gmtText, FileBrowser.Filter.GMT));
		
		JLabel expressionsLabel = new JLabel("Expressions:");
		expressionsText = new JTextField();
		expressionsText.setText(initDataSet != null ? initDataSet.getFiles().getExpressionFileName() : null);
		JButton expressionsBrowse = createBrowseButton(iconManager);
		expressionsBrowse.addActionListener(e -> browse(expressionsText, FileBrowser.Filter.EXPRESSION));
		
		makeSmall(nameLabel, nameText, analysisLabel, analysisTypeCombo);
		makeSmall(enrichmentsLabel, enrichments1Text, enrichmentsBrowse);
		makeSmall(enrichments2Label, enrichments2Text, enrichments2Browse);
		makeSmall(expressionsLabel, expressionsText, expressionsBrowse);
		makeSmall(gmtLabel, gmtText, gmtBrowse);
		
		JLabel ranksLabel = new JLabel("Ranks:");
		ranksText = new JTextField();
		ranksText.setText(initDataSet != null ? initDataSet.getFiles().getRankedFile() : null);
		JButton ranksBrowse = createBrowseButton(iconManager);
		ranksBrowse.addActionListener(e -> browse(ranksText, FileBrowser.Filter.RANK));
		
		JLabel classesLabel = new JLabel("Classes:");
		classesText = new JTextField();
		classesText.setText(initDataSet != null ? initDataSet.getFiles().getClassFile() : null);
		JButton classesBrowse = createBrowseButton(iconManager);
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
					.addComponent(gmtLabel)
					.addComponent(ranksLabel)
					.addComponent(classesLabel)
				)
				.addGroup(layout.createParallelGroup(Alignment.LEADING, true)
					.addComponent(nameText, 0, DEFAULT_SIZE, Short.MAX_VALUE)
					.addComponent(analysisTypeCombo, 0, DEFAULT_SIZE, Short.MAX_VALUE)
					.addComponent(enrichments1Text, 0, DEFAULT_SIZE, Short.MAX_VALUE)
					.addComponent(enrichments2Text, 0, DEFAULT_SIZE, Short.MAX_VALUE)
					.addComponent(expressionsText, 0, DEFAULT_SIZE, Short.MAX_VALUE)
					.addComponent(gmtText, 0, DEFAULT_SIZE, Short.MAX_VALUE)
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
					.addComponent(gmtBrowse)
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
					.addComponent(gmtLabel)
					.addComponent(gmtText)
					.addComponent(gmtBrowse)
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
		Optional<Path> path = FileBrowser.browse(fileUtil, jframe.get(), filter);
		path.map(Path::toString).ifPresent(textField::setText);
	}
	
	@Override
	public List<String> validateInput() {
		List<String> err = new ArrayList<>();
		if(Strings.isNullOrEmpty(nameText.getText())) {
			err.add("Name field is empty.");
		}
		if(Strings.isNullOrEmpty(enrichments1Text.getText())) {
			err.add("Enrichments file path is empty.");
		} 
		if(!emptyOrReadable(enrichments1Text)) {
			err.add("Enrichments file path is not valid.");
		}
		if(!emptyOrReadable(enrichments2Text)) {
			err.add("Enrichments 2 file path is not valid.");
		}
		if(!emptyOrReadable(expressionsText)) {
			err.add("Expressions file path is not valid.");
		}
		if(!emptyOrReadable(gmtText)) {
			err.add("GMT file path is not valid.");
		}
		if(!emptyOrReadable(ranksText)) {
			err.add("Ranks file path is not valid.");
		}
		if(!emptyOrReadable(classesText)) {
			err.add("Classes file path is not valid.");
		}
		return err;
	}
	
	
	public static boolean emptyOrReadable(JTextField textField) {
		String text = textField.getText();
		return Strings.isNullOrEmpty(text) || Files.isReadable(Paths.get(text));
	}
	
	
	private void updateClasses() {
		if(positiveText.getText().trim().isEmpty() && negativeText.getText().trim().isEmpty() 
				&& Files.isReadable(Paths.get(classesText.getText()))) {
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
	
	public static JButton createBrowseButton(IconManager iconManager) {
		JButton button = new JButton(IconManager.ICON_ELLIPSIS_H);
		button.setFont(iconManager.getIconFont(10.0f));
		button.setToolTipText("Browse...");
		if(LookAndFeelUtil.isAquaLAF()) {
			button.putClientProperty("JButton.buttonType", "gradient");
			button.putClientProperty("JComponent.sizeVariant", "small");
		}
		return button;
	}
	
	private Method getMethod() {
		return analysisTypeCombo.getItemAt(analysisTypeCombo.getSelectedIndex()).getValue();
	}

}
