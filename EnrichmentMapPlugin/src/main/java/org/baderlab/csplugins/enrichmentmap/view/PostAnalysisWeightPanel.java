package org.baderlab.csplugins.enrichmentmap.view;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import org.baderlab.csplugins.enrichmentmap.FilterParameters;
import org.baderlab.csplugins.enrichmentmap.FilterParameters.FilterType;
import org.baderlab.csplugins.enrichmentmap.PostAnalysisParameters;
import org.baderlab.csplugins.enrichmentmap.model.DataSet;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.model.GeneExpressionMatrix;
import org.baderlab.csplugins.enrichmentmap.model.Ranking;
import org.cytoscape.application.swing.CySwingApplication;

@SuppressWarnings("serial")
public class PostAnalysisWeightPanel extends CollapsiblePanel {

	private final CySwingApplication application;
	
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
	
	private JRadioButton gmtRadioButton;
	private JRadioButton intersectionRadioButton;
	private JRadioButton expressionSetRadioButton;
	private JRadioButton userDefinedRadioButton;
    private JFormattedTextField universeSelectionTextField;
	
    private DefaultComboBoxModel<String> rankingModel;
    private DefaultComboBoxModel<String> datasetModel;
	private EnablementComboBoxRenderer rankingEnablementRenderer;
    
    private JPanel cardPanel;
    
	public PostAnalysisWeightPanel(CySwingApplication application) {
		super("Edge Weight Calculation Parameters");
		this.application = application;
		createPanel();
	}
	
	
	private void createPanel() {
		JPanel selectPanel = createRankTestSelectPanel();
		
		JPanel hypergeomCard = createHypergeomPanel();
		JPanel mannWhittCard = createMannWhittPanel();
		JPanel warnCard = createWarningPanel();
		
		cardPanel = new JPanel(new CardLayout());
		cardPanel.add(mannWhittCard, FilterType.MANN_WHIT.toString());
		cardPanel.add(hypergeomCard, FilterType.HYPERGEOM.toString());
		cardPanel.add(new JPanel(), FilterType.PERCENT.toString());
		cardPanel.add(new JPanel(), FilterType.NUMBER.toString());
		cardPanel.add(new JPanel(), FilterType.SPECIFIC.toString());
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
		} catch (FontFormatException | IOException e) {
			e.printStackTrace();
		}
		
		JLabel label = new JLabel(FilterType.MANN_WHIT.display + " requires ranks.");
		
		warnPanel.add(label);
		
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(warnPanel, BorderLayout.NORTH);
		return panel;
	}

	
	@SuppressWarnings("unchecked")
	private JPanel createRankTestSelectPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		
		DecimalFormat decFormat = new DecimalFormat();
        decFormat.setParseIntegerOnly(false);
        rankTestTextField = new JFormattedTextField(decFormat);
        rankTestTextField.addPropertyChangeListener("value", new FormattedTextFieldAction());
        
        rankingEnablementRenderer = new EnablementComboBoxRenderer();
		rankTestCombo = new JComboBox<>();
		rankTestCombo.setRenderer(rankingEnablementRenderer);
		
        rankTestCombo.addItem(FilterType.MANN_WHIT);
        rankTestCombo.addItem(FilterType.HYPERGEOM);
        rankTestCombo.addItem(FilterType.NUMBER);
        rankTestCombo.addItem(FilterType.PERCENT);
        rankTestCombo.addItem(FilterType.SPECIFIC);
        
        rankTestCombo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	FilterType rankTest = (FilterType)rankTestCombo.getSelectedItem();
            	FilterParameters rankTestParams = paParams.getRankTestParameters();
        		rankTestParams.setType(rankTest);
        		rankTestTextField.setValue(rankTestParams.getValue(rankTest));
        		
        		CardLayout cardLayout = (CardLayout)cardPanel.getLayout();
        		if(rankTest == FilterType.MANN_WHIT && map.getAllRanks().isEmpty())
        			cardLayout.show(cardPanel, "warn");
        		else
        			cardLayout.show(cardPanel, rankTest.toString());
            }
        });
        
        panel.add(rankTestCombo);
        
        JPanel cutoffLabel = new JPanel();
        cutoffLabel.add(new JLabel("Select Cutoff:"));
        panel.add(cutoffLabel);
        
        JPanel cutoffPanel = new JPanel();
        cutoffPanel.setLayout(new BoxLayout(cutoffPanel, BoxLayout.X_AXIS));

        cutoffPanel.add(rankTestCombo);
        cutoffPanel.add(rankTestTextField);

        panel.add(cutoffPanel);
        
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
        c.insets = new Insets(0,0,0,0);
        c.fill = GridBagConstraints.HORIZONTAL;
        
        JPanel radioButtonsPanel = new JPanel();
        radioButtonsPanel.setLayout(gridbag);
        
    	gmtRadioButton = new JRadioButton();
        gmtRadioButton.setActionCommand("GMT");
        gmtRadioButton.addActionListener(new UniverseSelectActionListener());        
        gmtRadioButton.setSelected(true);
        
        expressionSetRadioButton = new JRadioButton();
        expressionSetRadioButton.setActionCommand("Expression Set");
        expressionSetRadioButton.addActionListener(new UniverseSelectActionListener());
        
        intersectionRadioButton = new JRadioButton();
        intersectionRadioButton.setActionCommand("Intersection");
        intersectionRadioButton.addActionListener(new UniverseSelectActionListener());
        
        userDefinedRadioButton = new JRadioButton("User Defined");
        userDefinedRadioButton.setActionCommand("User Defined");
        userDefinedRadioButton.addActionListener(new UniverseSelectActionListener());  
        
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
        universeSelectionTextField.addPropertyChangeListener("value", new FormattedTextFieldAction());
        universeSelectionTextField.setEnabled(false);
        gridbag.setConstraints(universeSelectionTextField, c);
        radioButtonsPanel.add(universeSelectionTextField);
        
        universeSelectionPanel.getContentPane().add(radioButtonsPanel, BorderLayout.CENTER);
               
        panel.add(universeSelectionPanel);
        return panel;
	}
	
	
	private JPanel createMannWhittPanel() {
		JPanel panel = new JPanel(new GridBagLayout());
		
		datasetCombo = new JComboBox<>();
        // Dataset model is already initialized
        datasetModel = new DefaultComboBoxModel<>();
        datasetCombo.setModel(datasetModel);
        datasetCombo.addActionListener(new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
            	String dataset = (String)datasetCombo.getSelectedItem();
            	if(dataset == null)
            		return;
            	paParams.setSignature_dataSet(dataset);
            	updateUniverseSize();
            }
        });
        
        rankingModel = new DefaultComboBoxModel<>();
        rankingCombo = new JComboBox<>();
        rankingCombo.setModel(rankingModel);
        rankingCombo.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
            	paParams.setSignature_rankFile((String)rankingCombo.getSelectedItem());
            }
        });
        
        
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(0,0,0,0);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.WEST;
        c.gridx = 0;
        c.gridy = 0;
        panel.add(datasetCombo, c);
        
        c.gridy = 1;
        panel.add(rankingCombo, c);
        
        c.gridy = 2;
        c.weighty = 1.0;
        c.weightx = 1.0;
        panel.add(new JLabel(""), c);
        
        return panel;
	}
	
	
	private class UniverseSelectActionListener implements ActionListener {
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
        public void propertyChange(PropertyChangeEvent e) {
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
		gmtRadioButton.setSelected(true);
        rankTestCombo.setSelectedItem(FilterType.MANN_WHIT);
        rankTestTextField.setValue(paParams.getRankTestParameters().getValue(FilterType.MANN_WHIT));
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
        
        Map<String,Ranking> rankingMap = map.getAllRanks();
        String[] rankingArray = rankingMap.keySet().toArray(new String[rankingMap.size()]);
        Arrays.sort(rankingArray);
        rankingModel.removeAllElements();
        for (String ranking : rankingArray) {
        	rankingModel.addElement(ranking);
        }
        
		updateUniverseSize();
        
		FilterParameters filterParams = paParams.getRankTestParameters();
		if(filterParams.getType() == FilterType.NO_FILTER) {
			filterParams.setType(FilterType.MANN_WHIT);
		}
		if(rankingArray.length == 0 && filterParams.getType() == FilterType.MANN_WHIT) {
			filterParams.setType(FilterType.HYPERGEOM);
		}
		
        rankTestCombo.setSelectedItem(filterParams.getType());
        double value = filterParams.getValue(filterParams.getType());
		rankTestTextField.setValue(value);
		
		rankingEnablementRenderer.enableIndex(0);
		if(rankingArray.length == 0)
			rankingEnablementRenderer.disableIndex(0);
    }
    
    
    private void updateUniverseSize() {
    	String signature_dataSet = paParams.getSignature_dataSet();
    	GeneExpressionMatrix expressionSets = map.getDataset(signature_dataSet).getExpressionSets();
    	
    	universeGmt = map.getNumberOfGenes();
    	universeExpression = expressionSets.getExpressionUniverse();
    	universeIntersection = expressionSets.getExpressionMatrix().size();
    	
        gmtRadioButton.setText("GMT (" + universeGmt + ")");
        expressionSetRadioButton.setText("Expression Set (" + universeExpression + ")");
        intersectionRadioButton.setText("Intersection (" + universeIntersection + ")");
        
        universeSelectionTextField.setValue(universeExpression);
        
        if(gmtRadioButton.isSelected())
        	paParams.setUniverseSize(universeGmt);
        else if(expressionSetRadioButton.isSelected())
        	paParams.setUniverseSize(universeExpression);
        else if(intersectionRadioButton.isSelected())
        	paParams.setUniverseSize(universeIntersection);
        else
        	paParams.setUniverseSize((Integer)universeSelectionTextField.getValue());
    }
    
    
}
