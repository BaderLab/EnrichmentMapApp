package org.baderlab.csplugins.enrichmentmap.view.postanalysis;

import static java.awt.GridBagConstraints.EAST;
import static java.awt.GridBagConstraints.NONE;
import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;
import static org.baderlab.csplugins.enrichmentmap.view.util.SwingUtil.makeSmall;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.GridBagLayout;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingUtilities;

import org.baderlab.csplugins.enrichmentmap.AfterInjection;
import org.baderlab.csplugins.enrichmentmap.model.EMDataSet;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.model.GeneExpressionMatrix;
import org.baderlab.csplugins.enrichmentmap.model.PostAnalysisFilterType;
import org.baderlab.csplugins.enrichmentmap.model.PostAnalysisParameters.UniverseType;
import org.baderlab.csplugins.enrichmentmap.model.Ranking;
import org.baderlab.csplugins.enrichmentmap.task.postanalysis.FilterMetric;
import org.baderlab.csplugins.enrichmentmap.task.postanalysis.FilterMetricSet;
import org.baderlab.csplugins.enrichmentmap.view.EnablementComboBoxRenderer;
import org.baderlab.csplugins.enrichmentmap.view.util.GBCFactory;
import org.baderlab.csplugins.enrichmentmap.view.util.SwingUtil;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.util.swing.LookAndFeelUtil;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

@SuppressWarnings("serial")
public class PAWeightPanel extends JPanel {
	
	public static final String PROPERTY_PARAMETERS = "property_parameters";
	
	private static final String WARN_CARD = "warn";
	private static final String MANN_WHIT_CARD = "mannWhitney";
	private static final String EMPTY_CARD = "empty";

	private static final String LABEL_CUTOFF = "Cutoff:";
	private static final String LABEL_TEST   = "Test:";

	private static final double HYPERGOM_DEFAULT = 0.25;
	
	@Inject private IconManager iconManager;
	
	private final EnrichmentMap map;
	
	private JComboBox<String> datasetCombo;
	private JComboBox<PostAnalysisFilterType> rankTestCombo;
	private JFormattedTextField rankTestTextField;
	
	private JRadioButton gmtRadioButton;
	private JRadioButton intersectionRadioButton;
	private JRadioButton expressionSetRadioButton;
	private JRadioButton userDefinedRadioButton;
	private JFormattedTextField universeSelectionTextField;
	
	private JLabel iconLabel;
	private JLabel warnLabel;
	
	private DefaultComboBoxModel<String> datasetModel;
	private EnablementComboBoxRenderer<PostAnalysisFilterType> rankingEnablementRenderer;
    private JPanel cardPanel;
    private Map<PostAnalysisFilterType,Double> savedFilterValues = PostAnalysisFilterType.createMapOfDefaultsNumbers();
    
    private JPanel mannWhitCard;
    private Map<String,String> mannWhitRanks = new HashMap<>();
    
    private boolean rankTestUpdating = false;
    

    public interface Factory {
    		PAWeightPanel create(EnrichmentMap map);
    }
    
    @Inject
	public PAWeightPanel(@Assisted EnrichmentMap map) {
		savedFilterValues.put(PostAnalysisFilterType.HYPERGEOM, HYPERGOM_DEFAULT);
		this.map = map;
	}
	
	@AfterInjection
	private void createContents() {
		setBorder(LookAndFeelUtil.createPanelBorder());
		JPanel selectPanel = createRankTestSelectPanel();
		
		JPanel hypeCard = createHypergeomCard();
		mannWhitCard = createMannWhittCard();
		JPanel warnCard = createWarningCard();
		
		cardPanel = new JPanel(new CardLayout());
		cardPanel.add(mannWhitCard, MANN_WHIT_CARD);
		cardPanel.add(hypeCard, PostAnalysisFilterType.HYPERGEOM.name());
		cardPanel.add(createEmptyPanel(), PostAnalysisFilterType.PERCENT.name());
		cardPanel.add(createEmptyPanel(), PostAnalysisFilterType.NUMBER.name());
		cardPanel.add(createEmptyPanel(), PostAnalysisFilterType.SPECIFIC.name());
		cardPanel.add(createEmptyPanel(), EMPTY_CARD);
		cardPanel.add(warnCard, WARN_CARD);
		
		JLabel title = new JLabel("Edge Weight Parameters");
		SwingUtil.makeSmall(title);
        
        GroupLayout layout = new GroupLayout(this);
        setLayout(layout);
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(true);

		layout.setHorizontalGroup(layout.createParallelGroup()
			.addComponent(title)
			.addGroup(layout.createSequentialGroup()
				.addComponent(selectPanel, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(cardPanel, 300, 300, 300)
			)
		);
		layout.setVerticalGroup(layout.createSequentialGroup()
			.addComponent(title)
			.addGroup(layout.createParallelGroup(Alignment.CENTER, true)
				.addComponent(selectPanel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				.addComponent(cardPanel, 130, 130, 130)
			)
		);

		if (LookAndFeelUtil.isAquaLAF()) {
			setOpaque(false);
			cardPanel.setOpaque(false);
		}
		
		initialize();
	}
	
	
	private static JPanel createEmptyPanel() {
		JPanel panel = new JPanel();
		if (LookAndFeelUtil.isAquaLAF())
			panel.setOpaque(false);
		return panel;
	}
	
	
	private JPanel createWarningCard() {
		JLabel iconLabel = new JLabel(IconManager.ICON_WARNING);
		iconLabel.setFont(iconManager.getIconFont(16.0f));
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
	
	private void showWarning(String message) {
		warnLabel.setText(message == null ? "" : " " + message);
		iconLabel.setVisible(message != null);
		warnLabel.setVisible(message != null);
	}
	
	
	@SuppressWarnings("unchecked")
	private JPanel createRankTestSelectPanel() {
		JLabel testLabel = new JLabel(LABEL_TEST);
		JLabel cuttofLabel = new JLabel(LABEL_CUTOFF);
		JLabel dataSetLabel = new JLabel("Data Set:");

		DecimalFormat decFormat = new DecimalFormat();
		decFormat.setParseIntegerOnly(false);
		rankTestTextField = new JFormattedTextField(decFormat);
		rankTestTextField.setColumns(6);
		rankTestTextField.setHorizontalAlignment(JTextField.RIGHT);
		rankTestTextField.addPropertyChangeListener("value", e -> {
			String text = rankTestTextField.getText();
			try {
				double val = Double.parseDouble(text);
				PostAnalysisFilterType filterType = getFilterType();
				savedFilterValues.put(filterType, val);
				showWarning(filterType.isValid(val) ? null : filterType.getErrorMessage());
			} catch(NumberFormatException ex) {
				showWarning("Not a number");
			}
			
			if(!rankTestUpdating)
				firePropertyChange(PROPERTY_PARAMETERS, false, true);
		});

		rankingEnablementRenderer = new EnablementComboBoxRenderer<>();
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
			rankTestUpdating = true;
			PostAnalysisFilterType filterType = (PostAnalysisFilterType) rankTestCombo.getSelectedItem();
			rankTestTextField.setValue(savedFilterValues.get(filterType));
			CardLayout cardLayout = (CardLayout) cardPanel.getLayout();

			if(filterType.isMannWhitney() && map.getAllRanks().isEmpty())
				cardLayout.show(cardPanel, WARN_CARD);
			else if(filterType.isMannWhitney())
				cardLayout.show(cardPanel, MANN_WHIT_CARD);
			else
				cardLayout.show(cardPanel, filterType.name());
			firePropertyChange(PROPERTY_PARAMETERS, false, true);
			rankTestUpdating = false;
		});
		
		datasetCombo = new JComboBox<>();
		// Dataset model is already initialized
		datasetModel = new DefaultComboBoxModel<>();
		datasetCombo.setModel(datasetModel);
		datasetCombo.addActionListener(e -> {
			rankTestUpdating = true;
			updateUniverseSize(getDataSet());
			firePropertyChange(PROPERTY_PARAMETERS, false, true);
			rankTestUpdating = false;
		});
		
		iconLabel = new JLabel(IconManager.ICON_WARNING);
		iconLabel.setFont(iconManager.getIconFont(16.0f));
		iconLabel.setForeground(LookAndFeelUtil.getWarnColor());
		warnLabel = new JLabel("warn");
		iconLabel.setVisible(false);
		warnLabel.setVisible(false);
        
		makeSmall(testLabel, cuttofLabel, rankTestCombo, rankTestTextField);
		makeSmall(dataSetLabel, datasetCombo, iconLabel, warnLabel);
        
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
					.addGroup(layout.createSequentialGroup()
						.addComponent(rankTestTextField, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
						.addComponent(iconLabel)
						.addComponent(warnLabel)
					)
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
				.addComponent(iconLabel)
				.addComponent(warnLabel)
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
	
	
	private JPanel createHypergeomCard() {
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
		
		gmtRadioButton.addActionListener(e -> firePropertyChange(PROPERTY_PARAMETERS, false, true));
		expressionSetRadioButton.addActionListener(e -> firePropertyChange(PROPERTY_PARAMETERS, false, true));
		intersectionRadioButton.addActionListener(e -> firePropertyChange(PROPERTY_PARAMETERS, false, true));
		userDefinedRadioButton.addActionListener(e -> firePropertyChange(PROPERTY_PARAMETERS, false, true));
		
		DecimalFormat intFormat = new DecimalFormat();
		intFormat.setParseIntegerOnly(true);
		universeSelectionTextField = new JFormattedTextField(intFormat);
		universeSelectionTextField.addPropertyChangeListener("value", e -> {
			Number val = (Number) universeSelectionTextField.getValue();
			if (val == null || val.intValue() < 0) {
				universeSelectionTextField.setValue(1);
				JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(this), "Universe value must be greater than zero",
						"Parameter out of bounds", JOptionPane.WARNING_MESSAGE);
			}
			if(!rankTestUpdating)
				firePropertyChange(PROPERTY_PARAMETERS, false, true);
		});
		universeSelectionTextField.setEnabled(false);

		gmtRadioButton.setText("GMT");
		expressionSetRadioButton.setText("Expression Set");
		intersectionRadioButton.setText("Intersection");
		universeSelectionTextField.setValue(map.getNumberOfGenes());
		
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
	
	
	private JPanel createMannWhittCard() {
		JPanel panel = new JPanel();
		JLabel title = new JLabel("Ranks to use for Mann-Whitney test");
		SwingUtil.makeSmall(title);
		
		List<EMDataSet> dataSets = map.getDataSetList();

		JPanel body = new JPanel(new GridBagLayout());
		int y = 0;
		for(EMDataSet dataset : dataSets) {
			final String dataSetName = dataset.getName();
			JLabel label = new JLabel(dataSetName + ":");
			JComboBox<String> combo = new JComboBox<>();
			for(String ranksName : dataset.getAllRanksNames()) {
				combo.addItem(ranksName);
			}
			SwingUtil.makeSmall(label, combo);
			if(combo.getItemCount() <= 1) {
				combo.setEnabled(false);
			}
			
			String ranksName = mannWhitRanks.get(dataSetName);
			if(ranksName == null)
				mannWhitRanks.put(dataSetName, combo.getSelectedItem().toString());
			else
				combo.setSelectedItem(ranksName);
			
			combo.addActionListener(e -> {
				String ranks = combo.getSelectedItem().toString();
				mannWhitRanks.put(dataSetName, ranks);
				firePropertyChange(PROPERTY_PARAMETERS, false, true);
			});
			
			body.add(label, GBCFactory.grid(0,y).weightx(.5).anchor(EAST).fill(NONE).get());
			body.add(combo, GBCFactory.grid(1,y).weightx(.5).get());
			y++;
		}
		
		JPanel container = new JPanel(new BorderLayout());
		container.add(body, BorderLayout.NORTH);
		
		JScrollPane scrollPane = new JScrollPane(container);
		scrollPane.setAlignmentX(TOP_ALIGNMENT);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		
		panel.setLayout(new BorderLayout());
		panel.add(title, BorderLayout.NORTH);
		panel.add(scrollPane, BorderLayout.CENTER);
		panel.setOpaque(false);
		return panel;
	}	
	
	
	
	private void initialize() {
		datasetModel.removeAllElements();
		datasetModel.addElement("-- All Data Sets --");
		for (String dataset : map.getDataSetNames()) {
			datasetModel.addElement(dataset);
		}
		datasetCombo.setEnabled(datasetModel.getSize() > 2);
		if (datasetModel.getSize() > 0) {
			datasetCombo.setSelectedIndex(0);
		}

		Map<String, Ranking> rankingMap = map.getAllRanks();
		String[] rankingArray = rankingMap.keySet().toArray(new String[rankingMap.size()]);
		Arrays.sort(rankingArray);

		PostAnalysisFilterType typeToUse = rankingArray.length == 0 ? PostAnalysisFilterType.HYPERGEOM : PostAnalysisFilterType.MANN_WHIT_TWO_SIDED;
		rankTestCombo.setSelectedItem(typeToUse);
		rankTestTextField.setValue(typeToUse.defaultValue);

		rankingEnablementRenderer.enableAll();
		if (rankingArray.length == 0) {
			rankingEnablementRenderer.disableItems(
					PostAnalysisFilterType.MANN_WHIT_TWO_SIDED, 
					PostAnalysisFilterType.MANN_WHIT_LESS, 
					PostAnalysisFilterType.MANN_WHIT_GREATER);
		}
	}
	
	
	public void updateMannWhitRanks() {
		CardLayout cardLayout = (CardLayout) cardPanel.getLayout();
		boolean showing = mannWhitCard.isVisible();
		if(showing)
			cardLayout.show(cardPanel, EMPTY_CARD);
		remove(mannWhitCard);
		mannWhitCard = createMannWhittCard();
		cardPanel.add(mannWhitCard, MANN_WHIT_CARD);
		if(showing)
			cardLayout.show(cardPanel, MANN_WHIT_CARD);
	}
	
	
	private void updateUniverseSize(String dataset) {
		if(dataset == null) {
			gmtRadioButton.setText("GMT");
			expressionSetRadioButton.setText("Expression Set");
			intersectionRadioButton.setText("Intersection");
			universeSelectionTextField.setValue(map.getNumberOfGenes());
		}
		else {
			gmtRadioButton.setText("GMT (" + getUniverse(dataset, UniverseType.GMT) + ")");
			expressionSetRadioButton.setText("Expression Set (" + getUniverse(dataset, UniverseType.EXPRESSION_SET) + ")");
			intersectionRadioButton.setText("Intersection (" + getUniverse(dataset, UniverseType.INTERSECTION) + ")");
			universeSelectionTextField.setValue(getUniverse(dataset, UniverseType.EXPRESSION_SET));
		}
	}
	
	private int getUniverse(String dataset, UniverseType type) {
		GeneExpressionMatrix expressionSets = map.getDataSet(dataset).getExpressionSets();
		switch(type) {
			default:
			case GMT:
				return map.getNumberOfGenes();
			case EXPRESSION_SET:
				return expressionSets.getExpressionUniverse();
			case INTERSECTION:
				return expressionSets.getExpressionMatrix().size();
			case USER_DEFINED:
				return getUserDefinedUniverseSize();
		}
	}
	
	public UniverseType getUniverseType() {
		if(gmtRadioButton.isSelected())
			return UniverseType.GMT;
		else if(expressionSetRadioButton.isSelected())
			return UniverseType.EXPRESSION_SET;
		else if(intersectionRadioButton.isSelected())
			return UniverseType.INTERSECTION;
		else if(userDefinedRadioButton.isSelected())
			return UniverseType.USER_DEFINED;
		return UniverseType.GMT;
	}
	
	public PostAnalysisFilterType getFilterType() {
		return (PostAnalysisFilterType) rankTestCombo.getSelectedItem();
	}
	
	
	public List<String> getDataSets() {
		if(datasetCombo.getSelectedIndex() == 0) {
			List<String> datasets = new ArrayList<>(datasetModel.getSize() - 1);
			for(int i = 1; i < datasetModel.getSize(); i++) {
				datasets.add(datasetModel.getElementAt(i));
			}
			return datasets;
		}
		else
			return Collections.singletonList((String)datasetCombo.getSelectedItem());
	}
	
	
	public String getDataSet() {
		if(datasetCombo.getSelectedIndex() == 0)
			return null;
		else
			return (String)datasetCombo.getSelectedItem();
	}
	
	public int getUserDefinedUniverseSize() {
		return ((Number)universeSelectionTextField.getValue()).intValue();
	}
	
	
	public FilterMetricSet getResults() {
		FilterMetricSet results = new FilterMetricSet(getFilterType());
		for(String dataset : getDataSets()) {
			FilterMetric metric = createFilterMetric(dataset);
			if(metric != null)
				results.put(dataset, metric);
		}
		return results;
	}
	
	public FilterMetric createFilterMetric(String dataset) {
		String text = rankTestTextField.getText();
		double value = Double.parseDouble(text);
		PostAnalysisFilterType type = getFilterType();
		
		switch(type) {
			case NO_FILTER:
				return new FilterMetric.NoFilter();
			case NUMBER:
				return new FilterMetric.Number(value);
			case PERCENT:
				return new FilterMetric.Percent(value);
			case SPECIFIC:
				return new FilterMetric.Specific(value);
			case HYPERGEOM:
				UniverseType universeType = getUniverseType();
				int universe = getUniverse(dataset, universeType);
				return new FilterMetric.Hypergeom(value, universe);
			case MANN_WHIT_TWO_SIDED:
			case MANN_WHIT_GREATER:
			case MANN_WHIT_LESS:
				String rankingName = mannWhitRanks.get(dataset);
				Ranking ranking = map.getDataSet(dataset).getRanks().get(rankingName);
				return new FilterMetric.MannWhit(value, ranking, type);
			default:
				return null;
		}
	}
}
