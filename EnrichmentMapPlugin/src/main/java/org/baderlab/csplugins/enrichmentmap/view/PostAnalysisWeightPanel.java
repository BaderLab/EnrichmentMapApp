package org.baderlab.csplugins.enrichmentmap.view;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import org.baderlab.csplugins.enrichmentmap.FilterParameters.Builder;
import org.baderlab.csplugins.enrichmentmap.FilterType;
import org.baderlab.csplugins.enrichmentmap.PostAnalysisParameters;
import org.baderlab.csplugins.enrichmentmap.model.DataSet;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.model.GeneExpressionMatrix;
import org.baderlab.csplugins.enrichmentmap.model.Ranking;
import org.cytoscape.application.swing.CySwingApplication;

@SuppressWarnings("serial")
public class PostAnalysisWeightPanel extends CollapsiblePanel {

	private static final String LABEL_TEST = "Test:";
	private static final String LABEL_DATASET = "Data Set:";
	private static final String LABEL_RANKS = "Ranks:";

	private static final double HYPERGOM_DEFAULT = 0.25;
	
	private final CySwingApplication application;

	private EnrichmentMap map;

	// Universe sizes
	private int universeGmt;
	private int universeExpression;
	private int universeIntersection;

	private JComboBox<String> datasetCombo;
	private JComboBox<String> rankingCombo;
	private JComboBox<FilterType> rankTestCombo;
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
	
	private Map<FilterType,Double> savedFilterValues = FilterType.createMapOfDefaults();

	
	public PostAnalysisWeightPanel(CySwingApplication application) {
		super("Edge Weight Parameters");
		this.application = application;
		
		// override the default value for HYPERGEOM
		savedFilterValues.put(FilterType.HYPERGEOM, HYPERGOM_DEFAULT);
		
		createPanel();
	}
	

	private void createPanel() {
		JPanel selectPanel = createRankTestSelectPanel();

		JPanel hypergeomCard = createHypergeomPanel();
		JPanel mannWhittCard = createMannWhittPanel();
		JPanel warnCard = createWarningPanel();

		cardPanel = new JPanel(new CardLayout());
		cardPanel.add(mannWhittCard, FilterType.MANN_WHIT_TWO_SIDED.toString());
		cardPanel.add(mannWhittCard, FilterType.MANN_WHIT_GREATER.toString());
		cardPanel.add(mannWhittCard, FilterType.MANN_WHIT_LESS.toString());
		cardPanel.add(hypergeomCard, FilterType.HYPERGEOM.toString());
		cardPanel.add(new JPanel(),  FilterType.PERCENT.toString());
		cardPanel.add(new JPanel(),  FilterType.NUMBER.toString());
		cardPanel.add(new JPanel(),  FilterType.SPECIFIC.toString());
		cardPanel.add(warnCard, "warn");

		getContentPane().add(selectPanel, BorderLayout.NORTH);
		getContentPane().add(cardPanel, BorderLayout.CENTER);
	}

	private JPanel createWarningPanel() {
		JPanel warnPanel = new JPanel(new FlowLayout(FlowLayout.LEADING));

		try {
			Font iconFont = Font.createFont(Font.TRUETYPE_FONT, getClass().getResourceAsStream("/fonts/fontawesome-webfont.ttf"));
			Font iconFontSized = iconFont.deriveFont(Font.PLAIN, new JLabel().getFont().getSize2D());

			JLabel warnIcon = new JLabel();
			warnIcon.setFont(iconFontSized);
			warnIcon.setText("\uf071"); // warn icon

			warnPanel.add(warnIcon);
		} catch(FontFormatException | IOException e) {
			e.printStackTrace();
		}

		JLabel label = new JLabel("Mann-Whitney requires ranks.");

		warnPanel.add(label);

		JPanel panel = new JPanel(new BorderLayout());
		panel.add(warnPanel, BorderLayout.NORTH);
		return panel;
	}

	@SuppressWarnings("unchecked")
	private JPanel createRankTestSelectPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

		JPanel cutoffLabel = new JPanel();
		cutoffLabel.add(new JLabel("Select Cutoff:"));
		panel.add(cutoffLabel);

		DecimalFormat decFormat = new DecimalFormat();
		decFormat.setParseIntegerOnly(false);
		rankTestTextField = new JFormattedTextField(decFormat);
		rankTestTextField.addPropertyChangeListener("value", e -> {
			StringBuilder message = new StringBuilder("The value you have entered is invalid.\n");
			Number number = (Number) rankTestTextField.getValue();
			FilterType filterType = getFilterType();
			
			Optional<Double> value = PostAnalysisInputPanel.validateAndGetFilterValue(number, filterType, message);
			double def = filterType == FilterType.HYPERGEOM ? HYPERGOM_DEFAULT : filterType.defaultValue;
			savedFilterValues.put(filterType, value.orElse(def));
			if(!value.isPresent()) {
				rankTestTextField.setValue(def);
				JOptionPane.showMessageDialog(application.getJFrame(), message.toString(), "Parameter out of bounds", JOptionPane.WARNING_MESSAGE);
			}
		});

		rankingEnablementRenderer = new EnablementComboBoxRenderer();
		rankTestCombo = new JComboBox<>();
		rankTestCombo.setRenderer(rankingEnablementRenderer);

		rankTestCombo.addItem(FilterType.MANN_WHIT_TWO_SIDED);
		rankTestCombo.addItem(FilterType.MANN_WHIT_GREATER);
		rankTestCombo.addItem(FilterType.MANN_WHIT_LESS);
		rankTestCombo.addItem(FilterType.HYPERGEOM);
		rankTestCombo.addItem(FilterType.NUMBER);
		rankTestCombo.addItem(FilterType.PERCENT);
		rankTestCombo.addItem(FilterType.SPECIFIC);

		rankTestCombo.addActionListener(e -> {
			FilterType filterType = (FilterType) rankTestCombo.getSelectedItem();
			rankTestTextField.setValue(savedFilterValues.get(filterType));

			CardLayout cardLayout = (CardLayout) cardPanel.getLayout();
			if(filterType.isMannWhitney() && map.getAllRanks().isEmpty())
				cardLayout.show(cardPanel, "warn");
			else
				cardLayout.show(cardPanel, filterType.toString());
		});

		panel.add(create3CellGridPanel(LABEL_TEST, rankTestCombo, rankTestTextField));

		datasetCombo = new JComboBox<>();
		// Dataset model is already initialized
		datasetModel = new DefaultComboBoxModel<>();
		datasetCombo.setModel(datasetModel);
		datasetCombo.addActionListener(e -> {
			String dataset = (String) datasetCombo.getSelectedItem();
			if(dataset == null)
				return;
			updateUniverseSize(dataset);
		});

		panel.add(create3CellGridPanel(LABEL_DATASET, datasetCombo, null));

		return panel;
	}

	private JPanel createHypergeomPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

		// Create Universe selection panel
		CollapsiblePanel universeSelectionPanel = new CollapsiblePanel("Advanced Hypergeometric Universe");
		universeSelectionPanel.setCollapsed(false);
		universeSelectionPanel.getContentPane().setLayout(new BorderLayout());

		GridBagLayout gridbag = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		c.weighty = 1;
		c.weightx = 1;
		c.insets = new Insets(0, 0, 0, 0);
		c.fill = GridBagConstraints.HORIZONTAL;

		JPanel radioButtonsPanel = new JPanel();
		radioButtonsPanel.setLayout(gridbag);

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

		userDefinedRadioButton = new JRadioButton("User Defined");
		userDefinedRadioButton.setActionCommand("User Defined");
		userDefinedRadioButton.addActionListener(universeSelectActionListener);

		ButtonGroup buttonGroup = new ButtonGroup();
		buttonGroup.add(gmtRadioButton);
		buttonGroup.add(expressionSetRadioButton);
		buttonGroup.add(intersectionRadioButton);
		buttonGroup.add(userDefinedRadioButton);

		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = GridBagConstraints.REMAINDER;
		gridbag.setConstraints(gmtRadioButton, c);
		radioButtonsPanel.add(gmtRadioButton);

		c.gridy = 1;
		gridbag.setConstraints(expressionSetRadioButton, c);
		radioButtonsPanel.add(expressionSetRadioButton);

		c.gridy = 2;
		gridbag.setConstraints(intersectionRadioButton, c);
		radioButtonsPanel.add(intersectionRadioButton);

		c.gridy = 3;
		c.gridwidth = 2;
		gridbag.setConstraints(userDefinedRadioButton, c);
		radioButtonsPanel.add(userDefinedRadioButton);

		c.gridx = 2;
		DecimalFormat intFormat = new DecimalFormat();
		intFormat.setParseIntegerOnly(true);
		universeSelectionTextField = new JFormattedTextField(intFormat);
		universeSelectionTextField.setEnabled(false);
		
		universeSelectionTextField.addPropertyChangeListener("value", e -> {
			Number val = (Number) universeSelectionTextField.getValue();
			if(val == null || val.intValue() < 0) {
				universeSelectionTextField.setValue(1);
				JOptionPane.showMessageDialog(application.getJFrame(), "Universe value must be greater than zero", "Parameter out of bounds", JOptionPane.WARNING_MESSAGE);
			}
		});
		
		gridbag.setConstraints(universeSelectionTextField, c);
		radioButtonsPanel.add(universeSelectionTextField);

		universeSelectionPanel.getContentPane().add(radioButtonsPanel, BorderLayout.CENTER);

		panel.add(universeSelectionPanel);
		return panel;
	}

	private JPanel createMannWhittPanel() {
		rankingModel = new DefaultComboBoxModel<>();
		rankingCombo = new JComboBox<>();
		rankingCombo.setModel(rankingModel);

		JPanel panel = new JPanel(new BorderLayout());
		panel.add(create3CellGridPanel(LABEL_RANKS, rankingCombo, null), BorderLayout.NORTH);
		return panel;
	}

	
	/*
	 * This is a hackey way of getting the three comboboxes to line up properly
	 * without putting them in the same grid panel. I didn't want to put them in
	 * the same panel because I would have had to rewrite a big chunk of this
	 * file.
	 */
	private static JPanel create3CellGridPanel(String label, JComboBox<?> combo, JFormattedTextField textField) {
		int labelWidth = (int) new JLabel(LABEL_DATASET).getPreferredSize().getWidth(); // use the widest one

		JPanel panel = new JPanel(new GridBagLayout());

		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(0, 0, 0, 0);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.WEST;
		c.gridy = 0;
		c.gridx = 0;
		panel.add(Box.createHorizontalStrut(labelWidth), c);
		c.gridx = 1;
		panel.add(Box.createHorizontalStrut(1), c);
		c.gridx = 2;
		c.weightx = 1.0;
		panel.add(Box.createHorizontalStrut(1), c);

		c.weightx = 0.0;
		c.gridy = 1;

		c.gridx = 0;
		panel.add(new JLabel(label), c);
		c.gridx = 1;
		if(textField == null)
			c.gridwidth = 2;
		panel.add(combo, c);
		if(textField == null)
			return panel;
		c.gridx = 2;
		panel.add(textField, c);

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
		FilterType filterType = FilterType.MANN_WHIT_TWO_SIDED;
		rankTestCombo.setSelectedItem(filterType);
		rankTestTextField.setValue(filterType.defaultValue);
		
		savedFilterValues = FilterType.createMapOfDefaults();
		savedFilterValues.put(FilterType.HYPERGEOM, HYPERGOM_DEFAULT);
	}

	
	void initialize(EnrichmentMap currentMap) {
		this.map = currentMap;

		Map<String, DataSet> datasetMap = map.getDatasets();
		String[] datasetArray = datasetMap.keySet().toArray(new String[datasetMap.size()]);
		Arrays.sort(datasetArray);
		this.datasetModel.removeAllElements();
		for(String dataset : datasetArray) {
			datasetModel.addElement(dataset);
		}
		datasetCombo.setEnabled(datasetModel.getSize() > 1);
		if(datasetModel.getSize() > 0) {
			datasetCombo.setSelectedIndex(0);
		}

		Map<String, Ranking> rankingMap = map.getAllRanks();
		String[] rankingArray = rankingMap.keySet().toArray(new String[rankingMap.size()]);
		Arrays.sort(rankingArray);
		rankingModel.removeAllElements();
		for(String ranking : rankingArray) {
			rankingModel.addElement(ranking);
		}
		rankingCombo.setEnabled(rankingModel.getSize() > 1);
		if(rankingModel.getSize() > 0) {
			rankingCombo.setSelectedIndex(0);
		}

		FilterType typeToUse = rankingArray.length == 0 ? FilterType.HYPERGEOM : FilterType.MANN_WHIT_TWO_SIDED;
		rankTestCombo.setSelectedItem(typeToUse);
		rankTestTextField.setValue(typeToUse.defaultValue);

		rankingEnablementRenderer.enableIndex(0);
		if(rankingArray.length == 0) {
			rankingEnablementRenderer.disableIndex(0);
		}
	}
	
	
	protected FilterType getFilterType() {
		return (FilterType) rankTestCombo.getSelectedItem();
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
		Builder rankTestBuilder = builder.getRankTestParametersBuilder();
		rankTestBuilder.setType(getFilterType());
		rankTestBuilder.setValue(((Number) rankTestTextField.getValue()).doubleValue());
		
		builder.setSignature_dataSet(getDataSet());
		builder.setSignature_rankFile(getRankFile());
		builder.setUniverseSize(getUniverseSize());
	}

}
