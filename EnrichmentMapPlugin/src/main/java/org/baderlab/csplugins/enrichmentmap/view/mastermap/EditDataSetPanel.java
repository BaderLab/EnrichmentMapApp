package org.baderlab.csplugins.enrichmentmap.view.mastermap;

import static com.google.common.base.Strings.isNullOrEmpty;
import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;
import static org.baderlab.csplugins.enrichmentmap.view.util.SwingUtil.makeSmall;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;

import javax.annotation.Nullable;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JComboBox;
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
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.util.swing.LookAndFeelUtil;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

@SuppressWarnings("serial")
public class EditDataSetPanel extends JPanel implements DetailPanel {
	 
	public static final String PROP_NAME = "dataSetName";
	
	@Inject private PathTextField.Factory pathTextFactory;
	
	private JComboBox<ComboItem<Method>> analysisTypeCombo;
	private PathTextField nameText;
	private PathTextField enrichments1Text;
	private PathTextField enrichments2Text;
	private PathTextField expressionsText;
	private PathTextField gmtText;
	private PathTextField ranksText;
	private PathTextField classesText;
	private JTextField positiveText;
	private JTextField negativeText;
	
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
		
		if(!enrichments1Text.isEmpty())
			files.setEnrichmentFileName1(enrichments1Text.getText());
		if(!enrichments2Text.isEmpty())
			files.setEnrichmentFileName2(enrichments2Text.getText());
		if(!expressionsText.isEmpty())
			files.setExpressionFileName(expressionsText.getText());
		if(!gmtText.isEmpty())
			files.setGMTFileName(gmtText.getText());
		if(!ranksText.isEmpty())
			files.setRankedFile(ranksText.getText());
		if(!classesText.isEmpty())
			files.setClassFile(classesText.getText());
		
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
		nameText = pathTextFactory.create("* Name:", null);
		nameText.setText(initDataSet != null ? initDataSet.getName() : null);
		nameText.getTextField().getDocument().addDocumentListener(SwingUtil.simpleDocumentListener(() -> 
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
		makeSmall(analysisLabel, analysisTypeCombo);

		enrichments1Text = pathTextFactory.create("* Enrichments:", FileBrowser.Filter.ENRICHMENT);
		enrichments1Text.setText(initDataSet != null ? initDataSet.getFiles().getEnrichmentFileName1() : null);
		
		enrichments2Text = pathTextFactory.create("Enrichments 2:", FileBrowser.Filter.ENRICHMENT);
		enrichments2Text.setText(initDataSet != null ? initDataSet.getFiles().getEnrichmentFileName2() : null);
		
		gmtText = pathTextFactory.create("GMT:", FileBrowser.Filter.GMT);
		gmtText.setText(initDataSet != null ? initDataSet.getFiles().getGMTFileName() : null);
		
		expressionsText = pathTextFactory.create("Expressions:", FileBrowser.Filter.EXPRESSION);
		expressionsText.setText(initDataSet != null ? initDataSet.getFiles().getExpressionFileName() : null);
		
		ranksText = pathTextFactory.create("Ranks:", FileBrowser.Filter.RANK);
		ranksText.setText(initDataSet != null ? initDataSet.getFiles().getRankedFile() : null);
		
		classesText = pathTextFactory.create("Classes:", FileBrowser.Filter.CLASS);
		classesText.setText(initDataSet != null ? initDataSet.getFiles().getClassFile() : null);
		classesText.getTextField().getDocument().addDocumentListener(SwingUtil.simpleDocumentListener(this::updateClasses));
		
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
					.addComponent(nameText.getLabel())
					.addComponent(analysisLabel)
					.addComponent(enrichments1Text.getLabel())
					.addComponent(enrichments2Text.getLabel())
					.addComponent(expressionsText.getLabel())
					.addComponent(gmtText.getLabel())
					.addComponent(ranksText.getLabel())
					.addComponent(classesText.getLabel())
				)
				.addGroup(layout.createParallelGroup(Alignment.LEADING, true)
					.addComponent(nameText.getTextField(), 0, DEFAULT_SIZE, Short.MAX_VALUE)
					.addComponent(analysisTypeCombo, 0, DEFAULT_SIZE, Short.MAX_VALUE)
					.addComponent(enrichments1Text.getTextField(), 0, DEFAULT_SIZE, Short.MAX_VALUE)
					.addComponent(enrichments2Text.getTextField(), 0, DEFAULT_SIZE, Short.MAX_VALUE)
					.addComponent(expressionsText.getTextField(), 0, DEFAULT_SIZE, Short.MAX_VALUE)
					.addComponent(gmtText.getTextField(), 0, DEFAULT_SIZE, Short.MAX_VALUE)
					.addComponent(ranksText.getTextField(),   0, DEFAULT_SIZE, Short.MAX_VALUE)
					.addComponent(classesText.getTextField(), 0, DEFAULT_SIZE, Short.MAX_VALUE)
					.addGroup(layout.createSequentialGroup()
						.addComponent(positive)
						.addComponent(positiveText, 75, 75, 75)
						.addGap(20)
						.addComponent(negative)
						.addComponent(negativeText, 75, 75, 75)
					)
				)
				.addGroup(layout.createParallelGroup()
					.addComponent(enrichments1Text.getBrowseButton())
					.addComponent(enrichments2Text.getBrowseButton())
					.addComponent(expressionsText.getBrowseButton())
					.addComponent(gmtText.getBrowseButton())
					.addComponent(ranksText.getBrowseButton())
					.addComponent(classesText.getBrowseButton())
				)
		);
		
		layout.setVerticalGroup(
			layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup(Alignment.BASELINE)
					.addComponent(nameText.getLabel())
					.addComponent(nameText.getTextField())
				)
				.addGroup(layout.createParallelGroup(Alignment.BASELINE)
					.addComponent(analysisLabel)
					.addComponent(analysisTypeCombo)
				)
				.addGroup(layout.createParallelGroup(Alignment.BASELINE)
					.addComponent(enrichments1Text.getLabel())
					.addComponent(enrichments1Text.getTextField())
					.addComponent(enrichments1Text.getBrowseButton())
				)
				.addGroup(layout.createParallelGroup(Alignment.BASELINE)
					.addComponent(enrichments2Text.getLabel())
					.addComponent(enrichments2Text.getTextField())
					.addComponent(enrichments2Text.getBrowseButton())
				)
				.addGroup(layout.createParallelGroup(Alignment.BASELINE)
					.addComponent(expressionsText.getLabel())
					.addComponent(expressionsText.getTextField())
					.addComponent(expressionsText.getBrowseButton())
				)
				.addGroup(layout.createParallelGroup(Alignment.BASELINE)
					.addComponent(gmtText.getLabel())
					.addComponent(gmtText.getTextField())
					.addComponent(gmtText.getBrowseButton())
				)
				.addGroup(layout.createParallelGroup(Alignment.BASELINE)
					.addComponent(ranksText.getLabel())
					.addComponent(ranksText.getTextField())
					.addComponent(ranksText.getBrowseButton())
				)
				.addGroup(layout.createParallelGroup(Alignment.BASELINE)
					.addComponent(classesText.getLabel())
					.addComponent(classesText.getTextField())
					.addComponent(classesText.getBrowseButton())
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
	
	
	@Override
	public List<String> validateInput() {
		List<String> err = new ArrayList<>();
		if(nameText.isEmpty())
			err.add("Name field is empty.");
		if(enrichments1Text.isEmpty())
			err.add("Enrichments file path is empty.");
		if(!enrichments1Text.emptyOrReadable())
			err.add("Enrichments file path is not valid.");
		if(!enrichments2Text.emptyOrReadable())
			err.add("Enrichments 2 file path is not valid.");
		if(!expressionsText.emptyOrReadable())
			err.add("Expressions file path is not valid.");
		if(!gmtText.emptyOrReadable())
			err.add("GMT file path is not valid.");
		if(!ranksText.emptyOrReadable())
			err.add("Ranks file path is not valid.");
		if(!classesText.emptyOrReadable())
			err.add("Classes file path is not valid.");
		return err;
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
	
	
	private Method getMethod() {
		return analysisTypeCombo.getItemAt(analysisTypeCombo.getSelectedIndex()).getValue();
	}
	
	public String getExpressionFileName() {
		return expressionsText.getText();
	}
	
	public String getGMTFileName() {
		return gmtText.getText();
	}

}
