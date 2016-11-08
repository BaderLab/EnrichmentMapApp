package org.baderlab.csplugins.enrichmentmap.view.postanalysis;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;
import static org.baderlab.csplugins.enrichmentmap.util.SwingUtil.makeSmall;

import java.awt.CardLayout;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;

import org.baderlab.csplugins.enrichmentmap.model.DataSet;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.model.PostAnalysisFilterParameters;
import org.baderlab.csplugins.enrichmentmap.model.PostAnalysisFilterType;
import org.baderlab.csplugins.enrichmentmap.model.GeneExpressionMatrix;
import org.baderlab.csplugins.enrichmentmap.model.PostAnalysisParameters;
import org.baderlab.csplugins.enrichmentmap.model.Ranking;
import org.baderlab.csplugins.enrichmentmap.view.EnablementComboBoxRenderer;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.util.swing.LookAndFeelUtil;

@SuppressWarnings("serial")
public class PostAnalysisWeightPanel extends JPanel {

	private static final String LABEL_CUTOFF = "Cutoff:";
	private static final String LABEL_TEST = "Test:";
	private static final String LABEL_DATASET = "Data Set:";
	private static final String LABEL_RANKS = "Ranks:";

	private static final double HYPERGOM_DEFAULT = 0.25;
	
	private EnrichmentMap map;
	
	// Universe sizes
	private int universeGmt;
	private int universeExpression;
	private int universeIntersection;
	
	private JComboBox<String> datasetCombo;
	private JComboBox<String> rankingCombo;
	private JComboBox<PostAnalysisFilterType> rankTestCombo;
	private JFormattedTextField rankTestTextField;
	
	private JRadioButton gmtRadioButton;
	private JRadioButton intersectionRadioButton;
	private JRadioButton expressionSetRadioButton;
	private JRadioButton userDefinedRadioButton;
	private JFormattedTextField universeSelectionTextField;
	
    private DefaultComboBoxModel<String> rankingModel;
    private DefaultComboBoxModel<String> datasetModel;
	private EnablementComboBoxRenderer rankingEnablementRenderer;
    
    private JPanel cardPanel;
    
    private Map<PostAnalysisFilterType,Double> savedFilterValues = PostAnalysisFilterType.createMapOfDefaults();
    
    private final CyServiceRegistrar serviceRegistrar;
    
	public PostAnalysisWeightPanel(CyServiceRegistrar serviceRegistrar) {
		this.serviceRegistrar = serviceRegistrar;
		
		// override the default value for HYPERGEOM
		savedFilterValues.put(PostAnalysisFilterType.HYPERGEOM, HYPERGOM_DEFAULT);
		
		createContents();
	}
	
	private void createContents() {
		setBorder(LookAndFeelUtil.createTitledBorder("Edge Weight Parameters"));

		JPanel selectPanel = createRankTestSelectPanel();
		JPanel hypergeomCard = createHypergeomPanel();
		JPanel mannWhittCard = createMannWhittPanel();
		JPanel warnCard = createWarningPanel();
		
		cardPanel = new JPanel(new CardLayout());
		cardPanel.add(mannWhittCard, PostAnalysisFilterType.MANN_WHIT_TWO_SIDED.toString());
		cardPanel.add(mannWhittCard, PostAnalysisFilterType.MANN_WHIT_GREATER.toString());
		cardPanel.add(mannWhittCard, PostAnalysisFilterType.MANN_WHIT_LESS.toString());
		cardPanel.add(hypergeomCard, PostAnalysisFilterType.HYPERGEOM.toString());
		cardPanel.add(createEmptyPanel(), PostAnalysisFilterType.PERCENT.toString());
		cardPanel.add(createEmptyPanel(), PostAnalysisFilterType.NUMBER.toString());
		cardPanel.add(createEmptyPanel(), PostAnalysisFilterType.SPECIFIC.toString());
		cardPanel.add(warnCard, "warn");
        
        GroupLayout layout = new GroupLayout(this);
        setLayout(layout);
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(true);

		layout.setHorizontalGroup(layout.createParallelGroup(Alignment.CENTER, true)
				.addComponent(selectPanel, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(cardPanel, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
		);
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addComponent(selectPanel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				.addComponent(cardPanel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
		);

		if (LookAndFeelUtil.isAquaLAF()) {
			setOpaque(false);
			cardPanel.setOpaque(false);
		}
	}

	private JPanel createEmptyPanel() {
		JPanel panel = new JPanel();

		if (LookAndFeelUtil.isAquaLAF())
			panel.setOpaque(false);

		return panel;
	}

	private JPanel createWarningPanel() {
		JLabel iconLabel = new JLabel(IconManager.ICON_WARNING);
		iconLabel.setFont(serviceRegistrar.getService(IconManager.class).getIconFont(16.0f));
		iconLabel.setForeground(LookAndFeelUtil.getWarnColor());

		JLabel msgLabel = new JLabel("Mann-Whitney requires ranks.");

		JPanel panel = new JPanel();
		final GroupLayout layout = new GroupLayout(panel);
		panel.setLayout(layout);
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(true);

		layout.setHorizontalGroup(layout.createSequentialGroup()
				.addGap(0, 0, Short.MAX_VALUE)
				.addComponent(iconLabel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				.addComponent(msgLabel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				.addGap(0, 0, Short.MAX_VALUE)
		);
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addGap(0, 0, Short.MAX_VALUE)
				.addGroup(layout.createParallelGroup(Alignment.CENTER, false)
						.addComponent(iconLabel)
						.addComponent(msgLabel)
				)
				.addGap(0, 0, Short.MAX_VALUE)
		);

		if (LookAndFeelUtil.isAquaLAF())
			panel.setOpaque(false);

		return panel;
	}
	
	@SuppressWarnings("unchecked")
	private JPanel createRankTestSelectPanel() {
		JLabel testLabel = new JLabel(LABEL_TEST);
		JLabel cuttofLabel = new JLabel(LABEL_CUTOFF);
		JLabel dataSetLabel = new JLabel(LABEL_DATASET);

		DecimalFormat decFormat = new DecimalFormat();
		decFormat.setParseIntegerOnly(false);
		rankTestTextField = new JFormattedTextField(decFormat);
		rankTestTextField.setColumns(6);
		rankTestTextField.setHorizontalAlignment(JTextField.RIGHT);
		rankTestTextField.addPropertyChangeListener("value", e -> {
			StringBuilder message = new StringBuilder("The value you have entered is invalid.\n");
			Number number = (Number) rankTestTextField.getValue();
			PostAnalysisFilterType filterType = getFilterType();

			Optional<Double> value = PostAnalysisInputPanel.validateAndGetFilterValue(number, filterType, message);
			double def = filterType == PostAnalysisFilterType.HYPERGEOM ? HYPERGOM_DEFAULT : filterType.defaultValue;
			savedFilterValues.put(filterType, value.orElse(def));
			
			if (!value.isPresent()) {
				rankTestTextField.setValue(def);
				CySwingApplication application = serviceRegistrar.getService(CySwingApplication.class);
				JOptionPane.showMessageDialog(application.getJFrame(), message.toString(), "Parameter out of bounds",
						JOptionPane.WARNING_MESSAGE);
			}
		});

		rankingEnablementRenderer = new EnablementComboBoxRenderer();
		rankTestCombo = new JComboBox<>();
		rankTestCombo.setRenderer(rankingEnablementRenderer);

		rankTestCombo.addItem(PostAnalysisFilterType.MANN_WHIT_TWO_SIDED);
		rankTestCombo.addItem(PostAnalysisFilterType.MANN_WHIT_GREATER);
		rankTestCombo.addItem(PostAnalysisFilterType.MANN_WHIT_LESS);
		rankTestCombo.addItem(PostAnalysisFilterType.HYPERGEOM);
		rankTestCombo.addItem(PostAnalysisFilterType.NUMBER);
		rankTestCombo.addItem(PostAnalysisFilterType.PERCENT);
		rankTestCombo.addItem(PostAnalysisFilterType.SPECIFIC);
        
		rankTestCombo.addActionListener(e -> {
			PostAnalysisFilterType filterType = (PostAnalysisFilterType) rankTestCombo.getSelectedItem();
			rankTestTextField.setValue(savedFilterValues.get(filterType));
			CardLayout cardLayout = (CardLayout) cardPanel.getLayout();

			if (filterType.isMannWhitney() && map.getAllRanks().isEmpty())
				cardLayout.show(cardPanel, "warn");
			else
				cardLayout.show(cardPanel, filterType.toString());
		});
		
		datasetCombo = new JComboBox<>();
		// Dataset model is already initialized
		datasetModel = new DefaultComboBoxModel<>();
		datasetCombo.setModel(datasetModel);
		datasetCombo.addActionListener(e -> {
			String dataset = (String) datasetCombo.getSelectedItem();
			if (dataset == null)
				return;
			
			updateUniverseSize(dataset);
		});
        
		makeSmall(testLabel, cuttofLabel, rankTestCombo, rankTestTextField);
		makeSmall(dataSetLabel, datasetCombo);
        
        JPanel panel = new JPanel();
        final GroupLayout layout = new GroupLayout(panel);
		panel.setLayout(layout);
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(!LookAndFeelUtil.isAquaLAF());
		
		layout.setHorizontalGroup(layout.createParallelGroup(Alignment.CENTER, true)
				.addGroup(layout.createSequentialGroup()
						.addGroup(layout.createParallelGroup(Alignment.TRAILING, true)
								.addComponent(testLabel)
								.addComponent(cuttofLabel)
								.addComponent(dataSetLabel)
						)
						.addGroup(layout.createParallelGroup(Alignment.LEADING, true)
								.addComponent(rankTestCombo, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
								.addComponent(rankTestTextField, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
								.addComponent(datasetCombo, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
						)
				)
		);
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup(Alignment.CENTER, false)
						.addComponent(testLabel)
						.addComponent(rankTestCombo, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				)
				.addGroup(layout.createParallelGroup(Alignment.CENTER, false)
						.addComponent(cuttofLabel)
						.addComponent(rankTestTextField, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				)
				.addPreferredGap(ComponentPlacement.UNRELATED)
				.addGroup(layout.createParallelGroup(Alignment.CENTER, false)
						.addComponent(dataSetLabel)
						.addComponent(datasetCombo, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				)
		);

		if (LookAndFeelUtil.isAquaLAF())
			panel.setOpaque(false);

		return panel;
	}

	private JPanel createHypergeomPanel() {
		ActionListener universeSelectActionListener = e -> {
			boolean enable = e.getActionCommand().equals("User Defined");
			universeSelectionTextField.setEnabled(enable);
		};
		
		gmtRadioButton = new JRadioButton();
		gmtRadioButton.setActionCommand("GMT");
		gmtRadioButton.addActionListener(universeSelectActionListener);
		gmtRadioButton.setSelected(true);

		expressionSetRadioButton = new JRadioButton();
		expressionSetRadioButton.setActionCommand("Expression Set");
		expressionSetRadioButton.addActionListener(universeSelectActionListener);

		intersectionRadioButton = new JRadioButton();
		intersectionRadioButton.setActionCommand("Intersection");
		intersectionRadioButton.addActionListener(universeSelectActionListener);

		userDefinedRadioButton = new JRadioButton("User Defined:");
		userDefinedRadioButton.setActionCommand("User Defined");
		userDefinedRadioButton.addActionListener(universeSelectActionListener);

		ButtonGroup buttonGroup = new ButtonGroup();
		buttonGroup.add(gmtRadioButton);
		buttonGroup.add(expressionSetRadioButton);
		buttonGroup.add(intersectionRadioButton);
		buttonGroup.add(userDefinedRadioButton);
		
		DecimalFormat intFormat = new DecimalFormat();
		intFormat.setParseIntegerOnly(true);
		universeSelectionTextField = new JFormattedTextField(intFormat);
		universeSelectionTextField.addPropertyChangeListener("value", e -> {
			Number val = (Number) universeSelectionTextField.getValue();
			if (val == null || val.intValue() < 0) {
				universeSelectionTextField.setValue(1);
				CySwingApplication application = serviceRegistrar.getService(CySwingApplication.class);
				JOptionPane.showMessageDialog(application.getJFrame(), "Universe value must be greater than zero",
						"Parameter out of bounds", JOptionPane.WARNING_MESSAGE);
			}
		});
		universeSelectionTextField.setEnabled(false);

		makeSmall(gmtRadioButton, expressionSetRadioButton, intersectionRadioButton, userDefinedRadioButton, universeSelectionTextField);

		JPanel panel = new JPanel();
		panel.setBorder(LookAndFeelUtil.createTitledBorder("Advanced Hypergeometric Universe"));

		final GroupLayout layout = new GroupLayout(panel);
		panel.setLayout(layout);
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(!LookAndFeelUtil.isAquaLAF());
		
		layout.setHorizontalGroup(layout.createParallelGroup(Alignment.LEADING, true)
				.addComponent(gmtRadioButton)
				.addComponent(expressionSetRadioButton)
				.addComponent(intersectionRadioButton)
				.addGroup(layout.createSequentialGroup()
						.addComponent(userDefinedRadioButton, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
						.addPreferredGap(ComponentPlacement.RELATED)
						.addComponent(universeSelectionTextField, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				)
		);
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addComponent(gmtRadioButton, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				.addComponent(expressionSetRadioButton, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				.addComponent(intersectionRadioButton, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				.addGroup(layout.createParallelGroup(Alignment.CENTER, false)
						.addComponent(userDefinedRadioButton, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
						.addComponent(universeSelectionTextField, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				)
		);

		if (LookAndFeelUtil.isAquaLAF())
			panel.setOpaque(false);

		return panel;
	}
	
	
	private JPanel createMannWhittPanel() {
		JLabel ranksLabel = new JLabel(LABEL_RANKS);

		rankingModel = new DefaultComboBoxModel<>();
		rankingCombo = new JComboBox<>();
		rankingCombo.setModel(rankingModel);

		makeSmall(ranksLabel, rankingCombo);

		JPanel panel = new JPanel();
		final GroupLayout layout = new GroupLayout(panel);
		panel.setLayout(layout);
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(!LookAndFeelUtil.isAquaLAF());

		layout.setHorizontalGroup(layout.createSequentialGroup()
				.addComponent(ranksLabel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				.addPreferredGap(ComponentPlacement.RELATED)
				.addComponent(rankingCombo, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
		);
		layout.setVerticalGroup(layout.createParallelGroup(Alignment.CENTER, false)
				.addComponent(ranksLabel)
				.addComponent(rankingCombo, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
		);

		if (LookAndFeelUtil.isAquaLAF())
			panel.setOpaque(false);

		return panel;
	}	
	
	private void updateUniverseSize(String signature_dataSet) {
		GeneExpressionMatrix expressionSets = map.getDataset(signature_dataSet).getExpressionSets();

		universeGmt = map.getNumberOfGenes();
		universeExpression = expressionSets.getExpressionUniverse();
		universeIntersection = expressionSets.getExpressionMatrix().size();

		gmtRadioButton.setText("GMT (" + universeGmt + ")");
		expressionSetRadioButton.setText("Expression Set (" + universeExpression + ")");
		intersectionRadioButton.setText("Intersection (" + universeIntersection + ")");

		universeSelectionTextField.setValue(universeExpression);
	}
	
	void resetPanel() {
		gmtRadioButton.setSelected(true);
		PostAnalysisFilterType filterType = PostAnalysisFilterType.MANN_WHIT_TWO_SIDED;
		rankTestCombo.setSelectedItem(filterType);
		rankTestTextField.setValue(filterType.defaultValue);
		
		savedFilterValues = PostAnalysisFilterType.createMapOfDefaults();
		savedFilterValues.put(PostAnalysisFilterType.HYPERGEOM, HYPERGOM_DEFAULT);
	}

	void initialize(EnrichmentMap currentMap) {
		this.map = currentMap;

		Map<String, DataSet> datasetMap = map.getDatasets();
		String[] datasetArray = datasetMap.keySet().toArray(new String[datasetMap.size()]);
		Arrays.sort(datasetArray);
		this.datasetModel.removeAllElements();
		for (String dataset : datasetArray) {
			datasetModel.addElement(dataset);
		}
		datasetCombo.setEnabled(datasetModel.getSize() > 1);
		if (datasetModel.getSize() > 0) {
			datasetCombo.setSelectedIndex(0);
		}

		Map<String, Ranking> rankingMap = map.getAllRanks();
		String[] rankingArray = rankingMap.keySet().toArray(new String[rankingMap.size()]);
		Arrays.sort(rankingArray);
		rankingModel.removeAllElements();
		for (String ranking : rankingArray) {
			rankingModel.addElement(ranking);
		}
		rankingCombo.setEnabled(rankingModel.getSize() > 1);
		if (rankingModel.getSize() > 0) {
			rankingCombo.setSelectedIndex(0);
		}

		PostAnalysisFilterType typeToUse = rankingArray.length == 0 ? PostAnalysisFilterType.HYPERGEOM : PostAnalysisFilterType.MANN_WHIT_TWO_SIDED;
		rankTestCombo.setSelectedItem(typeToUse);
		rankTestTextField.setValue(typeToUse.defaultValue);

		rankingEnablementRenderer.enableIndex(0);
		if (rankingArray.length == 0) {
			rankingEnablementRenderer.disableIndex(0);
		}
	}
	
	protected PostAnalysisFilterType getFilterType() {
		return (PostAnalysisFilterType) rankTestCombo.getSelectedItem();
	}
	
	protected String getDataSet() {
		return (String) datasetCombo.getSelectedItem();
	}
	
	protected String getRankFile() {
		return (String) rankingCombo.getSelectedItem();
	}
	
	protected int getUniverseSize() {
		int size = 0;
		if(gmtRadioButton.isSelected())
			size = universeGmt;
		else if(expressionSetRadioButton.isSelected())
			size = universeExpression;
		else if(intersectionRadioButton.isSelected())
			size = universeIntersection;
		else if(userDefinedRadioButton.isSelected())
			size = ((Number) universeSelectionTextField.getValue()).intValue();
		return size;
	}
	
	public void build(PostAnalysisParameters.Builder builder) {
		double value = ((Number) rankTestTextField.getValue()).doubleValue();
		PostAnalysisFilterParameters rankTest = new PostAnalysisFilterParameters(getFilterType(), value);
		
		builder.setRankTestParameters(rankTest);
		builder.setSignatureDataSet(getDataSet());
		builder.setSignatureRankFile(getRankFile());
		builder.setUniverseSize(getUniverseSize());
	}
}
