package org.baderlab.csplugins.enrichmentmap.view;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;
import static org.baderlab.csplugins.enrichmentmap.util.SwingUtil.makeSmall;

import java.awt.CardLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DecimalFormat;
import java.util.Arrays;
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
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;

import org.baderlab.csplugins.enrichmentmap.FilterParameters;
import org.baderlab.csplugins.enrichmentmap.FilterParameters.FilterType;
import org.baderlab.csplugins.enrichmentmap.PostAnalysisParameters;
import org.baderlab.csplugins.enrichmentmap.model.DataSet;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.model.GeneExpressionMatrix;
import org.baderlab.csplugins.enrichmentmap.model.Ranking;
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
	
	private PostAnalysisParameters paParams;
	private EnrichmentMap map;
	
	// Universe sizes
	private int universeGmt;
	private int universeExpression;
	private int universeIntersection;
	
    private JComboBox<String> datasetCombo;
	private JComboBox<String> rankingCombo;
	private JComboBox<FilterType> rankTestCombo;
	private JFormattedTextField rankTestTextField;
	
	private JRadioButton gmtRadio;
	private JRadioButton intersectionRadio;
	private JRadioButton expressionSetRadio;
	private JRadioButton userDefinedRadio;
    private JFormattedTextField universeSelectionTextField;
	
    private DefaultComboBoxModel<String> rankingModel;
    private DefaultComboBoxModel<String> datasetModel;
	private EnablementComboBoxRenderer rankingEnablementRenderer;
    
    private JPanel cardPanel;
    
	private final CyServiceRegistrar serviceRegistrar;
    
	public PostAnalysisWeightPanel(CyServiceRegistrar serviceRegistrar) {
		this.serviceRegistrar = serviceRegistrar;
		
		init();
	}
	
	private void init() {
		setBorder(LookAndFeelUtil.createTitledBorder("Edge Weight Parameters"));
		
		JPanel selectPanel = createRankTestSelectPanel();
		JPanel hypergeomCard = createHypergeomPanel();
		JPanel mannWhittCard = createMannWhittPanel();
		JPanel warnCard = createWarningPanel();
		
		cardPanel = new JPanel(new CardLayout());
		cardPanel.add(mannWhittCard, FilterType.MANN_WHIT_TWO_SIDED.toString());
		cardPanel.add(mannWhittCard, FilterType.MANN_WHIT_GREATER.toString());
		cardPanel.add(mannWhittCard, FilterType.MANN_WHIT_LESS.toString());
		cardPanel.add(hypergeomCard, FilterType.HYPERGEOM.toString());
		cardPanel.add(createEmptyPanel(), FilterType.PERCENT.toString());
		cardPanel.add(createEmptyPanel(), FilterType.NUMBER.toString());
		cardPanel.add(createEmptyPanel(), FilterType.SPECIFIC.toString());
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
		rankTestTextField.addPropertyChangeListener("value", new FormattedTextFieldAction());

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
        
		rankTestCombo.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				FilterType rankTest = (FilterType) rankTestCombo.getSelectedItem();
				FilterParameters rankTestParams = paParams.getRankTestParameters();
				rankTestParams.setType(rankTest);
				rankTestTextField.setValue(rankTestParams.getValue(rankTest));

				CardLayout cardLayout = (CardLayout) cardPanel.getLayout();
				if (rankTest.isMannWhitney() && map.getAllRanks().isEmpty())
					cardLayout.show(cardPanel, "warn");
				else
					cardLayout.show(cardPanel, rankTest.toString());
			}
		});
        
		datasetCombo = new JComboBox<>();
		// Dataset model is already initialized
		datasetModel = new DefaultComboBoxModel<>();
		datasetCombo.setModel(datasetModel);
		datasetCombo.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String dataset = (String) datasetCombo.getSelectedItem();
				if (dataset == null)
					return;
				paParams.setSignature_dataSet(dataset);
				updateUniverseSize(dataset);
			}
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
    	gmtRadio = new JRadioButton();
        gmtRadio.setActionCommand("GMT");
        gmtRadio.addActionListener(new UniverseSelectActionListener());        
        gmtRadio.setSelected(true);
        
        expressionSetRadio = new JRadioButton();
        expressionSetRadio.setActionCommand("Expression Set");
        expressionSetRadio.addActionListener(new UniverseSelectActionListener());
        
        intersectionRadio = new JRadioButton();
        intersectionRadio.setActionCommand("Intersection");
        intersectionRadio.addActionListener(new UniverseSelectActionListener());
        
        userDefinedRadio = new JRadioButton("User Defined:");
        userDefinedRadio.setActionCommand("User Defined");
        userDefinedRadio.addActionListener(new UniverseSelectActionListener());  
        
        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(gmtRadio);
        buttonGroup.add(expressionSetRadio);
        buttonGroup.add(intersectionRadio);
        buttonGroup.add(userDefinedRadio);

        DecimalFormat intFormat = new DecimalFormat();
        intFormat.setParseIntegerOnly(true);
        universeSelectionTextField = new JFormattedTextField(intFormat);
        universeSelectionTextField.addPropertyChangeListener("value", new FormattedTextFieldAction());
        universeSelectionTextField.setEnabled(false);
        
        makeSmall(gmtRadio, expressionSetRadio, intersectionRadio, userDefinedRadio, universeSelectionTextField);
        
        JPanel panel = new JPanel();
		panel.setBorder(LookAndFeelUtil.createTitledBorder("Advanced Hypergeometric Universe"));
		
		final GroupLayout layout = new GroupLayout(panel);
		panel.setLayout(layout);
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(!LookAndFeelUtil.isAquaLAF());
		
		layout.setHorizontalGroup(layout.createParallelGroup(Alignment.LEADING, true)
				.addComponent(gmtRadio)
				.addComponent(expressionSetRadio)
				.addComponent(intersectionRadio)
				.addGroup(layout.createSequentialGroup()
						.addComponent(userDefinedRadio, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
						.addPreferredGap(ComponentPlacement.RELATED)
						.addComponent(universeSelectionTextField, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				)
		);
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addComponent(gmtRadio, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				.addComponent(expressionSetRadio, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				.addComponent(intersectionRadio, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				.addGroup(layout.createParallelGroup(Alignment.CENTER, false)
						.addComponent(userDefinedRadio, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
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
		rankingCombo.addActionListener((ActionEvent e) -> {
			paParams.setSignature_rankFile((String) rankingCombo.getSelectedItem());
		});
        
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
	
	private class UniverseSelectActionListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			int size = 0;
			
			switch(e.getActionCommand()) {
				case "GMT":
					size = universeGmt;
		        	universeSelectionTextField.setEnabled(false);
		        	break;
				case "Expression Set":
		            size = universeExpression;
		            universeSelectionTextField.setEnabled(false);
		            break;
				case "Intersection":
					size = universeIntersection;
	            	universeSelectionTextField.setEnabled(false);
	            	break;
				case "User Defined":
					size = ((Number)universeSelectionTextField.getValue()).intValue();
		            universeSelectionTextField.setEnabled(true);
		            break;
			}
			
			paParams.setUniverseSize(size);
		}
	}
	
	
	private class FormattedTextFieldAction implements PropertyChangeListener {
		@Override
        public void propertyChange(PropertyChangeEvent e) {
			CySwingApplication application = serviceRegistrar.getService(CySwingApplication.class);
        	JFormattedTextField source = (JFormattedTextField) e.getSource();
	        if (source == rankTestTextField) {
	        	StringBuilder message = new StringBuilder("The value you have entered is invalid.\n");
            	boolean valid = PostAnalysisInputPanel.validateAndSetFilterValue(rankTestTextField, paParams.getRankTestParameters(), message);
            	if (!valid) {
                    JOptionPane.showMessageDialog(application.getJFrame(), message.toString(), "Parameter out of bounds", JOptionPane.WARNING_MESSAGE);
                }
	        }
	        else if (source == universeSelectionTextField) {
	        	Number val = (Number)universeSelectionTextField.getValue();
	        	if(val == null || val.intValue() < 0) {
	        		JOptionPane.showMessageDialog(application.getJFrame(), "Universe value must be greater than zero", "Parameter out of bounds", JOptionPane.WARNING_MESSAGE);
	        		universeSelectionTextField.setValue(val = 1);
	        	}
	        	paParams.setUniverseSize(val.intValue());
	        }
        }
	}
	
	void resetPanel() {
		gmtRadio.setSelected(true);
        rankTestCombo.setSelectedItem(FilterType.MANN_WHIT_TWO_SIDED);
        rankTestTextField.setValue(paParams.getRankTestParameters().getValue(FilterType.MANN_WHIT_TWO_SIDED));
    }
    
    void initialize(EnrichmentMap currentMap, PostAnalysisParameters paParams) {
    	this.map = currentMap;
		this.paParams = paParams;
		
		Map<String,DataSet> datasetMap = map.getDatasets();
        String[] datasetArray = datasetMap.keySet().toArray(new String[datasetMap.size()]);
        Arrays.sort(datasetArray);
        this.datasetModel.removeAllElements();
        for (String dataset : datasetArray) {
        	datasetModel.addElement(dataset);
        }
        datasetCombo.setEnabled(datasetModel.getSize() > 1);
        if(datasetModel.getSize() > 0) {
        	datasetCombo.setSelectedIndex(0);
        }
        
        Map<String,Ranking> rankingMap = map.getAllRanks();
        String[] rankingArray = rankingMap.keySet().toArray(new String[rankingMap.size()]);
        Arrays.sort(rankingArray);
        rankingModel.removeAllElements();
        for (String ranking : rankingArray) {
        	rankingModel.addElement(ranking);
        }
        rankingCombo.setEnabled(rankingModel.getSize() > 1);
        if(rankingModel.getSize() > 0) {
        	rankingCombo.setSelectedIndex(0);
        }
        
		FilterParameters filterParams = paParams.getRankTestParameters();
		if(filterParams.getType() == FilterType.NO_FILTER) {
			filterParams.setType(FilterType.MANN_WHIT_TWO_SIDED);
		}
		if(rankingArray.length == 0 && filterParams.getType().isMannWhitney()) {
			filterParams.setType(FilterType.HYPERGEOM);
		}
		
        rankTestCombo.setSelectedItem(filterParams.getType());
        double value = filterParams.getValue(filterParams.getType());
		rankTestTextField.setValue(value);
		
		rankingEnablementRenderer.enableIndex(0);
		if(rankingArray.length == 0) {
			rankingEnablementRenderer.disableIndex(0);
		}
    }
    
    private void updateUniverseSize(String signature_dataSet) {
    	GeneExpressionMatrix expressionSets = map.getDataset(signature_dataSet).getExpressionSets();
    	
    	universeGmt = map.getNumberOfGenes();
    	universeExpression = expressionSets.getExpressionUniverse();
    	universeIntersection = expressionSets.getExpressionMatrix().size();
    	
        gmtRadio.setText("GMT (" + universeGmt + ")");
        expressionSetRadio.setText("Expression Set (" + universeExpression + ")");
        intersectionRadio.setText("Intersection (" + universeIntersection + ")");
        
        universeSelectionTextField.setValue(universeExpression);
        
        if(gmtRadio.isSelected())
        	paParams.setUniverseSize(universeGmt);
        else if(expressionSetRadio.isSelected())
        	paParams.setUniverseSize(universeExpression);
        else if(intersectionRadio.isSelected())
        	paParams.setUniverseSize(universeIntersection);
        else
        	paParams.setUniverseSize((Integer)universeSelectionTextField.getValue());
    }
}
