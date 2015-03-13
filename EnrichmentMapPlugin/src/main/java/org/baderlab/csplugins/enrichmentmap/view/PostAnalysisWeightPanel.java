package org.baderlab.csplugins.enrichmentmap.view;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import org.baderlab.csplugins.enrichmentmap.PostAnalysisParameters;
import org.baderlab.csplugins.enrichmentmap.model.DataSet;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.model.GeneSet;
import org.baderlab.csplugins.enrichmentmap.model.Ranking;

@SuppressWarnings("serial")
public class PostAnalysisWeightPanel extends CollapsiblePanel {

	private PostAnalysisParameters paParams;
	private EnrichmentMap map;
	
    private JComboBox<String> datasetCombo;
	private JComboBox<String> rankingCombo;
	private JComboBox<String> rankTestCombo;
	private JFormattedTextField rankTestTextField;
	
	private JRadioButton gmtRadioButton;
	private JRadioButton intersectionRadioButton;
	private JRadioButton expressionSetRadioButton;
	private JRadioButton userDefinedRadioButton;
    private JFormattedTextField universeSelectionTextField;
	
    private DefaultComboBoxModel<String> rankingModel;
    private DefaultComboBoxModel<String> datasetModel;
	
    
	public PostAnalysisWeightPanel() {
		super("Edge Weight Calculation Parameters");
		createPanel();
	}
	
	
	private void createPanel() {
		JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        
        datasetCombo = new JComboBox<>();
        // Dataset model is already initialized
        datasetModel = new DefaultComboBoxModel<>();
        datasetCombo.setModel(datasetModel);
        datasetCombo.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
            	JComboBox<?> selectedChoice = (JComboBox<?>) e.getSource();
            	String dataset = (String)selectedChoice.getSelectedItem();
            	if(dataset == null)
            		return;
            	paParams.setSignature_dataSet(dataset);
            	updateUniverseSize();
            }
        });
        panel.add(datasetCombo);
        
        rankingModel = new DefaultComboBoxModel<>();
        rankingCombo = new JComboBox<>();
        rankingCombo.setModel(rankingModel);
        rankingCombo.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
            	paParams.setSignature_rankFile((String)rankingCombo.getSelectedItem());
            }
        });
        panel.add(rankingCombo);
        
        rankTestTextField = new JFormattedTextField();
        rankTestTextField.addPropertyChangeListener("value", new FormattedTextFieldAction());
        
        rankTestCombo = new JComboBox<>();
        rankTestCombo.addItem(PostAnalysisParameters.filterItems[PostAnalysisParameters.MANN_WHIT]);
        rankTestCombo.addItem(PostAnalysisParameters.filterItems[PostAnalysisParameters.HYPERGEOM]);
        rankTestCombo.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
                if (PostAnalysisParameters.filterItems[PostAnalysisParameters.MANN_WHIT].equals(rankTestCombo.getSelectedItem())) {
                    paParams.setSignature_rankTest(PostAnalysisParameters.MANN_WHIT);
                    rankTestTextField.setValue(paParams.getSignature_Mann_Whit_Cutoff());
                } else if (PostAnalysisParameters.filterItems[PostAnalysisParameters.HYPERGEOM].equals(rankTestCombo.getSelectedItem())) {
                    paParams.setSignature_rankTest(PostAnalysisParameters.HYPERGEOM);
                    rankTestTextField.setValue(paParams.getSignature_Hypergeom_Cutoff());
                }
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
        
        // Create Universe selection panel
        CollapsiblePanel universeSelectionPanel = new CollapsiblePanel("Advanced Hypergeometric Universe");
        universeSelectionPanel.setCollapsed(true);
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
        
        ButtonGroup universeSelectionOptions = new ButtonGroup();
        universeSelectionOptions.add(gmtRadioButton);
        universeSelectionOptions.add(expressionSetRadioButton);
        universeSelectionOptions.add(intersectionRadioButton);
        universeSelectionOptions.add(userDefinedRadioButton);

        c.gridx = 0;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.gridy = 0;
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
        universeSelectionTextField = new JFormattedTextField();
        universeSelectionTextField.addPropertyChangeListener("value", new FormattedTextFieldAction());
        universeSelectionTextField.setEditable(false);
        gridbag.setConstraints(universeSelectionTextField, c);
        radioButtonsPanel.add(universeSelectionTextField);
        
        universeSelectionPanel.getContentPane().add(radioButtonsPanel, BorderLayout.WEST);
               
        panel.add(universeSelectionPanel);
        
        getContentPane().add(panel, BorderLayout.NORTH);
    }

	
	private class UniverseSelectActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			int size = 0;
			switch(e.getActionCommand()) {
				case "GMT":
					size = map.getDataset(paParams.getSignature_dataSet()).getDatasetGenes().size();
		            universeSelectionTextField.setText(Integer.toString(size));
		        	universeSelectionTextField.setEditable(false);
		        	break;
				case "Expression Set":
		            size = map.getDataset(paParams.getSignature_dataSet()).getExpressionSets().getNumGenes();
		            universeSelectionTextField.setText(Integer.toString(size));
		            universeSelectionTextField.setEditable(false);
		            break;
				case "Intersection":
	            	universeSelectionTextField.setEditable(false);
	            	break;
				case "User Defined":
		            universeSelectionTextField.setEditable(true);
		            break;
			}
			paParams.setUniverseSize(size);
		}
	}
	
	
	private class FormattedTextFieldAction implements PropertyChangeListener {
        public void propertyChange(PropertyChangeEvent e) {
        	JFormattedTextField source = (JFormattedTextField) e.getSource();
	        if (source == rankTestTextField) {
	        	String value = rankTestTextField.getText();
	            String[] filterItems = PostAnalysisParameters.filterItems;
	        	if (rankTestCombo.getSelectedItem().equals(filterItems[PostAnalysisParameters.MANN_WHIT])) {
	        		paParams.setSignature_Mann_Whit_Cutoff(Double.parseDouble(value));
	        	}
	        	if (rankTestCombo.getSelectedItem().equals(filterItems[PostAnalysisParameters.HYPERGEOM])) {
	        		paParams.setSignature_Hypergeom_Cutoff(Double.parseDouble(value));
	        	}
	        }
	        else if (source == universeSelectionTextField) {
	        	String value = universeSelectionTextField.getText();
	        	paParams.setUniverseSize(Integer.parseInt(value));
	        }
        }
	}
	
	
	void resetPanel() {
        rankTestCombo.setSelectedItem(PostAnalysisParameters.filterItems[paParams.getDefault_signature_rankTest()]);
    }
    
    
    void updateContents(EnrichmentMap currentMap, PostAnalysisParameters paParams) {
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
        
		HashMap<String, GeneSet> EnrichmentGenesets = map.getAllGenesets();
        Set<Integer> EnrichmentGenes = new HashSet<Integer>();
        for (Iterator<String> i = map.getAllGenesets().keySet().iterator(); i.hasNext(); ) {
            String setName = i.next();
            EnrichmentGenes.addAll(EnrichmentGenesets.get(setName).getGenes());
        }
        
		universeSelectionTextField.setText(Integer.toString(EnrichmentGenes.size()));
		
		updateUniverseSize();
        
        String[] filterItems = PostAnalysisParameters.filterItems;
        rankTestCombo.setSelectedItem(filterItems[paParams.getDefault_signature_rankTest()]);
    }
    
    
    private void updateUniverseSize() {
    	String signature_dataSet = paParams.getSignature_dataSet();
		DataSet dataset = map.getDataset(signature_dataSet);
    	int universeSize = 0;
    	if (dataset != null) {
    		universeSize = dataset.getDatasetGenes().size();
    	}
    	
    	paParams.setUniverseSize(universeSize);
        
        gmtRadioButton.setText("GMT (" + universeSize + ")");
        
        int expressionSetSize = map.getDataset(signature_dataSet).getExpressionSets().getNumGenes();
        expressionSetRadioButton.setText("Expression Set (" + expressionSetSize + ")");
        
        HashSet<Integer> intersection = map.getDataset(signature_dataSet).getDatasetGenes();
    	intersection.retainAll(map.getDataset(signature_dataSet).getExpressionSets().getGeneIds());
        intersectionRadioButton.setText("Intersection (" + intersection.size() + ")");
    }
}
