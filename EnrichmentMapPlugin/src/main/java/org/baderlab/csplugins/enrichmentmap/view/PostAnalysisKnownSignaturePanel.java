package org.baderlab.csplugins.enrichmentmap.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JToolTip;

import org.baderlab.csplugins.enrichmentmap.EnrichmentMapUtils;
import org.baderlab.csplugins.enrichmentmap.PostAnalysisParameters;
import org.baderlab.csplugins.enrichmentmap.model.DataSet;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.model.JMultiLineToolTip;
import org.baderlab.csplugins.enrichmentmap.model.Ranking;
import org.baderlab.csplugins.enrichmentmap.model.SetOfGeneSets;
import org.baderlab.csplugins.enrichmentmap.task.LoadSignatureGMTFilesTask;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.io.util.StreamUtil;
import org.cytoscape.util.swing.FileChooserFilter;
import org.cytoscape.util.swing.FileUtil;
import org.cytoscape.work.swing.DialogTaskManager;

@SuppressWarnings("serial")
public class PostAnalysisKnownSignaturePanel extends JPanel {

	private final PostAnalysisInputPanel parentPanel;
	
	private final CyApplicationManager cyApplicationManager;
    private final CySwingApplication application;
	private final StreamUtil streamUtil;
	private final DialogTaskManager dialog;
	private final FileUtil fileUtil;
	
	// 'Known Signature Panel' parameters
	private EnrichmentMap map;
    private PostAnalysisParameters paParams;
   
    private JFormattedTextField knownSigUniverseSelectionTextField;
	private JFormattedTextField knownSignatureGMTFileNameTextField;
	private JComboBox knownSignatureRankTestCombo;
	private JFormattedTextField knownSignatureRankTestTextField;
	private JRadioButton KnownSigGMTRadioButton;
	private JRadioButton KnownSigExpressionSetRadioButton;
	private JRadioButton KnownSigIntersectionRadioButton;
	private JRadioButton KnownSigUserDefinedRadioButton;
	private JComboBox knownSigRankingCombo;
	private JComboBox knownSigDatasetCombo;
	
	private int defaultColumns = 15;
	
	private DefaultComboBoxModel rankingModel;
	private DefaultComboBoxModel datasetModel;
	
	
	public PostAnalysisKnownSignaturePanel(
			PostAnalysisInputPanel parentPanel,
			CyApplicationManager cyApplicationManager,
			CySwingApplication application,
			StreamUtil streamUtil,
			DialogTaskManager dialog,
			FileUtil fileUtil) {
		
		this.parentPanel = parentPanel;
		this.cyApplicationManager = cyApplicationManager;
		this.application = application;
		this.streamUtil = streamUtil;
		this.dialog = dialog;
		this.fileUtil = fileUtil;
		
		createKnownSignatureOptionsPanel();
	}
	/**
     * @return CollapsiblePanel to set PostAnalysisParameters 
     */
    private CollapsiblePanel createKnownSignatureParametersPanel() {
        CollapsiblePanel collapsiblePanel = new CollapsiblePanel("Edge Weight Calculation Parameters");
        
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        
        datasetModel = new DefaultComboBoxModel();
        knownSigDatasetCombo = new JComboBox<>();
        knownSigDatasetCombo.setModel(datasetModel);
        knownSigDatasetCombo.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
            	JComboBox selectedChoice = (JComboBox) e.getSource();
            	String dataset = (String)selectedChoice.getSelectedItem();
            	paParams.setSignature_dataSet(dataset);
    	        DataSet datasetObj = map.getDataset(paParams.getSignature_dataSet());
    	    	int universeSize = 0;
    	    	if (datasetObj != null) {
    	    		universeSize = datasetObj.getDatasetGenes().size();
    	    	}
    	    	paParams.setUniverseSize(universeSize);
            	if (KnownSigGMTRadioButton != null) {
            		KnownSigGMTRadioButton.setText("GMT (" + universeSize + ")");
            	}
            	datasetObj = map.getDataset(dataset);
            	int expressionSetSize = 0;
            	if (datasetObj != null) {
            		expressionSetSize = datasetObj.getExpressionSets().getNumGenes();
            	}
            	if (KnownSigExpressionSetRadioButton != null) {
            		KnownSigExpressionSetRadioButton.setText("Expression Set (" + expressionSetSize + ")");
            	}
            	if (KnownSigIntersectionRadioButton != null) {
            		
            	}
            }
        });
        panel.add(knownSigDatasetCombo);
        
        rankingModel = new DefaultComboBoxModel();
        knownSigRankingCombo = new JComboBox<>();
        knownSigRankingCombo.setModel(rankingModel);
        knownSigRankingCombo.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
            	JComboBox selectedChoice = (JComboBox) e.getSource();
            	paParams.setSignature_rankFile((String)selectedChoice.getSelectedItem());
            }
        });
        panel.add(knownSigRankingCombo);
        
        knownSignatureRankTestTextField = new JFormattedTextField();
        knownSignatureRankTestTextField.addPropertyChangeListener("value", new FormattedTextFieldAction());
        
        String[] filterItems = PostAnalysisParameters.filterItems;
        knownSignatureRankTestCombo = new JComboBox();
        knownSignatureRankTestCombo.addItem(filterItems[PostAnalysisParameters.MANN_WHIT]);
        knownSignatureRankTestCombo.addItem(filterItems[PostAnalysisParameters.HYPERGEOM]);
        knownSignatureRankTestCombo.addActionListener( new ActionListener() {
            String[] filterItems = PostAnalysisParameters.filterItems;
            public void actionPerformed( ActionEvent e ) {
                JComboBox selectedChoice = (JComboBox) e.getSource();
                if (filterItems[PostAnalysisParameters.MANN_WHIT].equals( selectedChoice.getSelectedItem())) {
                    paParams.setSignature_rankTest(PostAnalysisParameters.MANN_WHIT);
                    knownSignatureRankTestTextField.setValue(paParams.getSignature_Mann_Whit_Cutoff());
                } else if (filterItems[PostAnalysisParameters.HYPERGEOM].equals( selectedChoice.getSelectedItem())) {
                    paParams.setSignature_rankTest(PostAnalysisParameters.HYPERGEOM);
                    knownSignatureRankTestTextField.setValue(paParams.getSignature_Hypergeom_Cutoff());
                }
            }
        });
        panel.add(knownSignatureRankTestCombo);
        
        JPanel cutoffLabel = new JPanel();
        cutoffLabel.add(new JLabel("Select Cutoff:"));
        panel.add(cutoffLabel);
        
        JPanel cutoffPanel = new JPanel();
        cutoffPanel.setLayout(new BoxLayout(cutoffPanel, BoxLayout.X_AXIS));
        cutoffPanel.add(knownSignatureRankTestCombo);
        cutoffPanel.add(knownSignatureRankTestTextField);

        panel.add(cutoffPanel);
        
        // Create Universe selection panel
        CollapsiblePanel universeSelectionPanel = new CollapsiblePanel("Advanced Hypergeometric Universe");
        universeSelectionPanel.setCollapsed(true);
        universeSelectionPanel.getContentPane().setLayout(new BorderLayout());
        JPanel radioButtonsPanel = new JPanel();
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        c.weighty = 1;
        c.weightx = 1;
        c.insets = new Insets(0,0,0,0);
        c.fill = GridBagConstraints.HORIZONTAL;
        radioButtonsPanel.setLayout(gridbag);
        
    	KnownSigGMTRadioButton = new JRadioButton();
        KnownSigGMTRadioButton.setActionCommand("GMT");
        KnownSigGMTRadioButton.addActionListener(new ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectKnownSigUniverseActionPerformed(evt);
            }
        });
        KnownSigGMTRadioButton.setSelected(true);

        KnownSigExpressionSetRadioButton = new JRadioButton();
        KnownSigExpressionSetRadioButton.setActionCommand("Expression Set");
        KnownSigExpressionSetRadioButton.addActionListener(new ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectKnownSigUniverseActionPerformed(evt);
            }
        });
        KnownSigIntersectionRadioButton = new JRadioButton();
        KnownSigIntersectionRadioButton.setActionCommand("Intersection");
        KnownSigIntersectionRadioButton.addActionListener(new ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectKnownSigUniverseActionPerformed(evt);
            }
        });
        KnownSigUserDefinedRadioButton = new JRadioButton("User Defined");
        KnownSigUserDefinedRadioButton.setActionCommand("User Defined");
        KnownSigUserDefinedRadioButton.addActionListener(new ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectKnownSigUniverseActionPerformed(evt);
            }
        });
        
        ButtonGroup universeSelectionOptions = new ButtonGroup();
        universeSelectionOptions.add(KnownSigGMTRadioButton);
        universeSelectionOptions.add(KnownSigExpressionSetRadioButton);
        universeSelectionOptions.add(KnownSigIntersectionRadioButton);
        universeSelectionOptions.add(KnownSigUserDefinedRadioButton);

        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = GridBagConstraints.REMAINDER;
        gridbag.setConstraints(KnownSigGMTRadioButton, c);
        radioButtonsPanel.add(KnownSigGMTRadioButton);
        
        c.gridy = 1;
        gridbag.setConstraints(KnownSigExpressionSetRadioButton, c);
        radioButtonsPanel.add(KnownSigExpressionSetRadioButton);

        c.gridy = 2;
        gridbag.setConstraints(KnownSigIntersectionRadioButton, c);
        radioButtonsPanel.add(KnownSigIntersectionRadioButton);
        
        c.gridy = 3;
        c.gridwidth = 2;
        gridbag.setConstraints(KnownSigUserDefinedRadioButton, c);
        radioButtonsPanel.add(KnownSigUserDefinedRadioButton);
        
        knownSigUniverseSelectionTextField = new JFormattedTextField();
        knownSigUniverseSelectionTextField.addPropertyChangeListener("value", new FormattedTextFieldAction());
        knownSigUniverseSelectionTextField.setEditable(false);
        
        c.gridx = 2;
        gridbag.setConstraints(knownSigUniverseSelectionTextField, c);
        radioButtonsPanel.add(knownSigUniverseSelectionTextField);
        
        universeSelectionPanel.getContentPane().add(radioButtonsPanel, BorderLayout.WEST);
               
        panel.add(universeSelectionPanel);
       
        collapsiblePanel.getContentPane().add(panel, BorderLayout.NORTH);
        return collapsiblePanel;
    }
    
    
    /**
     * @return collapsiblePanel to select Signature Genesets for Signature Analysis
     */
    private void createKnownSignatureOptionsPanel() {
//        JPanel panel = new JPanel();
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        //Gene set file panel
        CollapsiblePanel GMTPanel = createKnownSignatureGMTPanel();
        GMTPanel.setCollapsed(false);
        
        //Parameters collapsible panel
        CollapsiblePanel ParametersPanel = createKnownSignatureParametersPanel();
        ParametersPanel.setCollapsed(false);
        
        add(GMTPanel);
        add(ParametersPanel);        
    }
    
	/**
     * @return CollapsiblePanel for choosing and loading GMT and SignatureGMT Geneset-Files 
     */
    private CollapsiblePanel createKnownSignatureGMTPanel() {
        CollapsiblePanel collapsiblePanel = new CollapsiblePanel("Gene-Sets");

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        //add SigGMT file
        JLabel SigGMTLabel = new JLabel("SigGMT:") {
            public JToolTip createToolTip() {
                return new JMultiLineToolTip();
            }
        };
        SigGMTLabel.setToolTipText(PostAnalysisInputPanel.gmtTip);
        JButton selectSigGMTFileButton = new JButton();
        knownSignatureGMTFileNameTextField = new JFormattedTextField() ;
        knownSignatureGMTFileNameTextField.setColumns(defaultColumns);


        //components needed for the directory load
        knownSignatureGMTFileNameTextField.setFont(new java.awt.Font("Dialog",1,10));
        //GMTFileNameTextField.setText(gmt_instruction);
        knownSignatureGMTFileNameTextField.addPropertyChangeListener("value",new FormattedTextFieldAction());


        selectSigGMTFileButton.setText("...");
        selectSigGMTFileButton.setMargin(new Insets(0,0,0,0));
        selectSigGMTFileButton.setActionCommand("Known Signature");
        selectSigGMTFileButton
        .addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectSignatureGMTFileButtonActionPerformed(evt);
            }
        });

        JPanel SigGMTPanel = new JPanel();
        SigGMTPanel.setLayout(new BorderLayout());

        SigGMTPanel.add( SigGMTLabel,BorderLayout.WEST);
        SigGMTPanel.add( knownSignatureGMTFileNameTextField, BorderLayout.CENTER);
        SigGMTPanel.add( selectSigGMTFileButton, BorderLayout.EAST);
        //add the components to the panel
        panel.add(SigGMTPanel);   
        
        collapsiblePanel.getContentPane().add(panel, BorderLayout.NORTH);
        return collapsiblePanel;

    }
    
    
    private void selectKnownSigUniverseActionPerformed(ActionEvent evt){
        String analysisType = evt.getActionCommand();
    	int size = 0;
        if (analysisType.equalsIgnoreCase("GMT")) {
        	size = map.getDataset(paParams.getSignature_dataSet()).getDatasetGenes().size();
            knownSigUniverseSelectionTextField.setText(Integer.toString(size));
            knownSigUniverseSelectionTextField.setEditable(false);
        } else if (analysisType.equalsIgnoreCase("Expression Set")) {
        	size = map.getDataset(paParams.getSignature_dataSet()).getExpressionSets().getNumGenes();
        	knownSigUniverseSelectionTextField.setText(Integer.toString(size));
        	knownSigUniverseSelectionTextField.setEditable(false);
        } else if (analysisType.equalsIgnoreCase("Intersection")) {
        	HashSet<Integer> intersection = map.getDataset(paParams.getSignature_dataSet()).getDatasetGenes();
        	intersection.retainAll(map.getDataset(paParams.getSignature_dataSet()).getExpressionSets().getGeneIds());
        	knownSigUniverseSelectionTextField.setText(Integer.toString(intersection.size()));
        	knownSigUniverseSelectionTextField.setEditable(false);
        } else if (analysisType.equalsIgnoreCase("User Defined")) {
        	knownSigUniverseSelectionTextField.setEditable(true);
        }
        paParams.setUniverseSize(size);
    }
    
    

    /**
     * Event Handler for selectSignatureGMTFileButton.<p>
     * Opens a file browser dialog to select the SignatureGMTFile.
     * 
     * @param evt
     */
    private void selectSignatureGMTFileButtonActionPerformed(ActionEvent evt) {
        FileChooserFilter filter = new FileChooserFilter("All GMT Files","gmt" );          
        
        //the set of filter (required by the file util method
        ArrayList<FileChooserFilter> all_filters = new ArrayList<FileChooserFilter>();
        all_filters.add(filter);
        // Get the file name
        File file = fileUtil.getFile(EnrichmentMapUtils.getWindowInstance(this),"Import Signature GMT File", FileUtil.LOAD,all_filters  );
        
        if(file != null) {
            knownSignatureGMTFileNameTextField.setForeground(PostAnalysisInputPanel.checkFile(file.getAbsolutePath()));
            knownSignatureGMTFileNameTextField.setText(file.getAbsolutePath());
            knownSignatureGMTFileNameTextField.setValue(file.getAbsolutePath());
            paParams.setSignatureGMTFileName(file.getAbsolutePath());
            //Load in the GMT file
            
            String errors = paParams.checkGMTfiles();
            if (errors.equalsIgnoreCase("")) {
            	LoadSignatureGMTFilesTask load_GMTs = new LoadSignatureGMTFilesTask(map, paParams, streamUtil, parentPanel);
                /*boolean success =*/ 
            	dialog.execute(load_GMTs.createTaskIterator());
            } else {
                JOptionPane.showMessageDialog(application.getJFrame(),errors,"Invalid Input",JOptionPane.WARNING_MESSAGE);
            }
            paParams.setSelectedSignatureSetNames(paParams.getSignatureSetNames());
            knownSignatureGMTFileNameTextField.setToolTipText(file.getAbsolutePath());
        }
    }
    
    /**
     * Handles setting for the text field parameters that are numbers.
     * Makes sure that the numbers make sense.
     */
    private class FormattedTextFieldAction implements PropertyChangeListener {
        public void propertyChange(PropertyChangeEvent e) {
            JFormattedTextField source = (JFormattedTextField) e.getSource();

            String message = "The value you have entered is invalid.\n";
            boolean invalid = false;

            if (source == knownSignatureGMTFileNameTextField) {
                String value = knownSignatureGMTFileNameTextField.getText();
                if(value.equalsIgnoreCase("") )
                    paParams.setSignatureGMTFileName(value);
                else if(knownSignatureGMTFileNameTextField.getText().equalsIgnoreCase((String)e.getOldValue())){
                    //do nothing
                }
                else if(PostAnalysisInputPanel.checkFile(value).equals(Color.RED)){
                    JOptionPane.showMessageDialog(application.getJFrame(),message,"File name change entered is not a valid file name",JOptionPane.WARNING_MESSAGE);
                    knownSignatureGMTFileNameTextField.setForeground(PostAnalysisInputPanel.checkFile(value));
                }
                else {
                    paParams.setSignatureGMTFileName(value);
                    paParams.setSignatureSetNames(new DefaultListModel());
                    paParams.setSelectedSignatureSetNames(new DefaultListModel());
                }
            } 
            else if (source == knownSignatureRankTestTextField) {
            	String value = knownSignatureRankTestTextField.getText();
                String[] filterItems = PostAnalysisParameters.filterItems;
            	if (knownSignatureRankTestCombo.getSelectedItem().equals(filterItems[PostAnalysisParameters.MANN_WHIT])) {
            		paParams.setSignature_Mann_Whit_Cutoff(Double.parseDouble(value));
            	}
            	if (knownSignatureRankTestCombo.getSelectedItem().equals(filterItems[PostAnalysisParameters.HYPERGEOM])) {
            		paParams.setSignature_Hypergeom_Cutoff(Double.parseDouble(value));
            	}
            }
            else if (source == knownSigUniverseSelectionTextField) {
            	String value = knownSigUniverseSelectionTextField.getText();
            	paParams.setUniverseSize(Integer.parseInt(value));
            }
            
            if (invalid) {
                JOptionPane.showMessageDialog(application.getJFrame(), message, "Parameter out of bounds", JOptionPane.WARNING_MESSAGE);
            }
        }
    }
    
    void resetPanel() {
    	paParams.setSignatureGenesets(new SetOfGeneSets());
    	//Gene-Sets Panel
        knownSignatureGMTFileNameTextField.setText("");
        knownSignatureGMTFileNameTextField.setValue("");
        knownSignatureGMTFileNameTextField.setToolTipText(null);
        
        String[] filterItems = PostAnalysisParameters.filterItems;
        knownSignatureRankTestCombo.setSelectedItem(filterItems[paParams.getDefault_signature_rankTest()]);
    }
    
    
    void updateContents(EnrichmentMap currentMap, PostAnalysisParameters paParams) {
    	this.map = currentMap;
		this.paParams = paParams;
		
		// MKTODO what to do with this?
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
        
        DataSet dataset = map.getDataset(paParams.getSignature_dataSet());
    	int universeSize = 0;
    	if (dataset != null) {
    		universeSize = dataset.getDatasetGenes().size();
    	}
        paParams.setUniverseSize(universeSize);
        knownSigUniverseSelectionTextField.setText(Integer.toString(universeSize));
        
        KnownSigGMTRadioButton.setText("GMT (" + universeSize + ")");
        
        int expressionSetSize = map.getDataset(paParams.getSignature_dataSet()).getExpressionSets().getNumGenes();
        KnownSigExpressionSetRadioButton.setText("Expression Set (" + expressionSetSize + ")");
        
        HashSet<Integer> intersection = map.getDataset(paParams.getSignature_dataSet()).getDatasetGenes();
    	intersection.retainAll(map.getDataset(paParams.getSignature_dataSet()).getExpressionSets().getGeneIds());
        KnownSigIntersectionRadioButton.setText("Intersection (" + intersection.size() + ")");
	
        String[] filterItems = PostAnalysisParameters.filterItems;
        knownSignatureRankTestCombo.setSelectedItem(filterItems[paParams.getDefault_signature_rankTest()]);
    }
}
