package org.baderlab.csplugins.enrichmentmap.view.creation;

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
import org.baderlab.csplugins.enrichmentmap.model.DataSetParameters;
import org.baderlab.csplugins.enrichmentmap.model.EMDataSet.Method;
import org.baderlab.csplugins.enrichmentmap.parsers.ClassFileReaderTask;
import org.baderlab.csplugins.enrichmentmap.view.util.ComboItem;
import org.baderlab.csplugins.enrichmentmap.view.util.FileBrowser;
import org.baderlab.csplugins.enrichmentmap.view.util.SwingUtil;
import org.baderlab.csplugins.enrichmentmap.view.util.dialog.Message;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.util.swing.LookAndFeelUtil;

import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

@SuppressWarnings("serial")
public class DetailDataSetPanel extends JPanel implements DetailPanel {
	 
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
	
	private final @Nullable DataSetParameters initDataSet;
	
	
	public interface Factory {
		DetailDataSetPanel create(@Nullable DataSetParameters initDataSet);
	}
	
	@Inject
	public DetailDataSetPanel(@Assisted @Nullable DataSetParameters initDataSet) {
		this.initDataSet = initDataSet;
	}
	
	@Override
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
		if(!enrichments2Text.isEmpty() && method == Method.GSEA)
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
		if(!isNullOrEmpty(positive)) {
			files.setPhenotype1(positive);
		}
		if(!isNullOrEmpty(negative)) {
			files.setPhenotype2(negative);
		}
		
		return new DataSetParameters(name, method, files);
	}
	
	
	@AfterInjection
	private void createContents() {
		createBody();
		if(initDataSet != null) {
			initialize(initDataSet);
		}
	}
	
	
	private void createBody() {
		nameText = pathTextFactory.create("* Name:", null);
		nameText.getTextField().getDocument().addDocumentListener(SwingUtil.simpleDocumentListener(() -> 
			firePropertyChange(PROP_NAME, null, getDisplayName())
		));
		
		JLabel analysisLabel = new JLabel("* Analysis Type:");
		analysisTypeCombo = new JComboBox<>();
		analysisTypeCombo.addItem(new ComboItem<>(Method.GSEA, Method.GSEA.getLabel()));
		analysisTypeCombo.addItem(new ComboItem<>(Method.Generic, Method.Generic.getLabel()));
		analysisTypeCombo.addItem(new ComboItem<>(Method.Specialized, Method.Specialized.getLabel()));
		analysisTypeCombo.addActionListener(e -> {
			updateLabels();
			firePropertyChange(PROP_NAME, null, getDisplayName());
		});
		makeSmall(analysisLabel, analysisTypeCombo);

		enrichments1Text = pathTextFactory.create("* Enrichments:", FileBrowser.Filter.ENRICHMENT);
		enrichments2Text = pathTextFactory.create("* Enrichments Neg:", FileBrowser.Filter.ENRICHMENT);
		gmtText = pathTextFactory.create("GMT:", FileBrowser.Filter.GMT);
		expressionsText = pathTextFactory.create("Expressions:", FileBrowser.Filter.EXPRESSION);
		ranksText = pathTextFactory.create("Ranks:", FileBrowser.Filter.RANK);
		classesText = pathTextFactory.create("Classes:", FileBrowser.Filter.CLASS);
		
		classesText.getTextField().getDocument().addDocumentListener(SwingUtil.simpleDocumentListener(this::preFillPhenotypes));
		enrichments1Text.getTextField().getDocument().addDocumentListener(SwingUtil.simpleDocumentListener(this::updateLabels));
		gmtText.getTextField().getDocument().addDocumentListener(SwingUtil.simpleDocumentListener(this::updateLabels));
		
		enrichments2Text.getLabel().setVisible(false);
		enrichments2Text.getTextField().setVisible(false);
		enrichments2Text.getBrowseButton().setVisible(false);
		
		JLabel phenotypesLabel = new JLabel("Phenotypes:");
		JLabel positive = new JLabel("  Positive:");
		JLabel negative = new JLabel("Negative:");
		positiveText = new JTextField();
		negativeText = new JTextField();
		makeSmall(phenotypesLabel, positive, negative, positiveText, negativeText);

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
					.addComponent(phenotypesLabel)
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
						.addComponent(positiveText, 90, 90, Short.MAX_VALUE)
						.addGap(10)
						.addComponent(negative)
						.addComponent(negativeText, 90, 90, Short.MAX_VALUE)
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
					.addComponent(expressionsText.getLabel())
					.addComponent(expressionsText.getTextField())
					.addComponent(expressionsText.getBrowseButton())
				)
				.addGroup(layout.createParallelGroup(Alignment.BASELINE)
					.addComponent(classesText.getLabel())
					.addComponent(classesText.getTextField())
					.addComponent(classesText.getBrowseButton())
				)
				.addGroup(layout.createParallelGroup(Alignment.BASELINE)
					.addComponent(phenotypesLabel)
					.addComponent(positive)
					.addComponent(positiveText, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addComponent(negative)
					.addComponent(negativeText, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				)
		);
		
   		if(LookAndFeelUtil.isAquaLAF())
   			setOpaque(false);
	}
	
	
	private void updateLabels() {
		switch(getMethod()) {
			case Generic:
			case Specialized:
				enrichments2Text.setVisible(false);
				if(enrichments1Text.isEmpty() == gmtText.isEmpty()) {
					enrichments1Text.getLabel().setText("* Enrichments:");
					gmtText.getLabel().setText("* GMT:");
				} else if(enrichments1Text.isEmpty()) {
					enrichments1Text.getLabel().setText("Enrichments:");
					gmtText.getLabel().setText("* GMT:");
				} else if(gmtText.isEmpty()) {
					enrichments1Text.getLabel().setText("* Enrichments:");
					gmtText.getLabel().setText("GMT:");
				}
				break;
			case GSEA:
				enrichments1Text.getLabel().setText("* Enrichments Pos:");
				enrichments2Text.setVisible(true);
				gmtText.getLabel().setText("* GMT:");
				break;
		}
	}
	
	private void initialize(DataSetParameters initDataSet) {
		nameText.setText(initDataSet.getName());
		DataSetFiles files = initDataSet.getFiles();
		enrichments1Text.setText(files.getEnrichmentFileName1());
		enrichments2Text.setText(files.getEnrichmentFileName2());
		gmtText.setText(files.getGMTFileName());
		expressionsText.setText(files.getExpressionFileName());
		ranksText.setText(files.getRankedFile());
		classesText.setText(files.getClassFile());
		positiveText.setText(files.getPhenotype1());
		negativeText.setText(files.getPhenotype2());
		analysisTypeCombo.setSelectedItem(ComboItem.of(initDataSet.getMethod()));
	}

	
	@Override
	public List<Message> validateInput(MasterDetailDialogPage parent) {
		List<Message> messages = new ArrayList<>();
		if(nameText.isEmpty())
			messages.add(Message.error("Name field is empty."));
		if(enrichments1Text.isEmpty() && gmtText.isEmpty())
			messages.add(Message.error("Enrichments file or GMT file is required."));
		if(!enrichments1Text.emptyOrReadable() && getMethod() == Method.GSEA)
			messages.add(Message.error("Enrichments Pos file path is not valid."));
		if(!enrichments1Text.emptyOrReadable() && getMethod() != Method.GSEA)
			messages.add(Message.error("Enrichments file path is not valid."));
		if(!enrichments2Text.emptyOrReadable())
			messages.add(Message.error("Enrichments Neg file path is not valid."));
		if(!expressionsText.emptyOrReadable())
			messages.add(Message.error("Expressions file path is not valid."));
		if(!gmtText.emptyOrReadable())
			messages.add(Message.error("GMT file path is not valid."));
		if(!ranksText.emptyOrReadable())
			messages.add(Message.error("Ranks file path is not valid."));
		if(!classesText.emptyOrReadable())
			messages.add(Message.error("Classes file path is not valid."));
		
		if(gmtText.isReadable() && !parent.getCommonPanel().map(cp -> cp.hasGmtFile()).orElse(false)) {
			String parentDir = gmtText.getPath().getParent().getFileName().toString();
			if("edb".equalsIgnoreCase(parentDir)) {
				messages.add(Message.warn("Using GMT file from EDB directory. This GMT file was filtered by "
						+ "the expressions and may effect the universe size when adding signature gene sets."));
			}
		}		
		
		return messages;
	}
	
	
	private void preFillPhenotypes() {
		String classFile = classesText.getText();
		if(!Strings.isNullOrEmpty(classFile) && Files.isReadable(Paths.get(classFile))) {
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
	
	public String getExpressionFile() {
		return expressionsText.getText();
	}
	
	public String getGMTFile() {
		return gmtText.getText();
	}
	
	public String getClassFile() {
		return classesText.getText();
	}
	
	public boolean hasExpressionFile() {
		return !isNullOrEmpty(getExpressionFile());
	}
	
	public boolean hasGmtFile() {
		return !isNullOrEmpty(getGMTFile());
	}
	
	public boolean hasClassFile() {
		return !isNullOrEmpty(getClassFile());
	}


}
