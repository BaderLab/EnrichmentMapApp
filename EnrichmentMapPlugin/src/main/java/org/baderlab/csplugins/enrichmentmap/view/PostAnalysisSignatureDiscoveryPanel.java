package org.baderlab.csplugins.enrichmentmap.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URL;
import java.util.Collections;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolTip;
import javax.swing.ScrollPaneConstants;

import org.baderlab.csplugins.enrichmentmap.PostAnalysisParameters;
import org.baderlab.csplugins.enrichmentmap.PostAnalysisParameters.FilterMetric;
import org.baderlab.csplugins.enrichmentmap.actions.LoadSignatureSetsActionListener;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.model.JMultiLineToolTip;
import org.baderlab.csplugins.enrichmentmap.model.SetOfGeneSets;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.io.util.StreamUtil;
import org.cytoscape.util.swing.FileUtil;
import org.cytoscape.work.swing.DialogTaskManager;

@SuppressWarnings("serial")
public class PostAnalysisSignatureDiscoveryPanel extends JPanel {

	private static final String NO_FILTER = "-- no filter --";
	
	private final PostAnalysisInputPanel parentPanel;
	
    private final CyApplicationManager cyApplicationManager;
    private final CySwingApplication application;
	private final StreamUtil streamUtil;
	private final DialogTaskManager dialog;
	private final FileUtil fileUtil;
	
	private final static int RIGHT = 0, DOWN = 1, UP = 2, LEFT = 3; // image States
	
	private PostAnalysisParameters paParams;
	private EnrichmentMap map;
    
    private JFormattedTextField signatureDiscoveryGMTFileNameTextField;
		
    private PostAnalysisWeightPanel weightPanel;
    
	private JLabel avail_sig_sets_counter_label;
	private JList<String> avail_sig_sets_field;
    private CollapsiblePanel signature_genesets;
    private JPanel signaturePanel;
    private JList<String> selected_sig_sets_field;
    private JLabel selected_sig_sets_counter_label;
    
    private DefaultListModel<String> avail_sig_sets;
    private DefaultListModel<String> selected_sig_sets;
    
//    private JRadioButton filter;
//    private JRadioButton nofilter;
    private JFormattedTextField filterTextField;
    private JComboBox<Object> filterTypeCombo;
    
    
    
	public PostAnalysisSignatureDiscoveryPanel(
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
		
		createSignatureDiscoveryOptionsPanel();
	}
	
	
    private void createSignatureDiscoveryOptionsPanel() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        //Gene set file panel
        CollapsiblePanel gmtPanel = createSignatureDiscoveryGMTPanel();
        gmtPanel.setCollapsed(false);
        
        //signature collapsible panel
        signature_genesets = new CollapsiblePanel("Signature Genesets");
        signature_genesets.setLayout(new BorderLayout());
        signature_genesets.setCollapsed(false);


        signaturePanel = new JPanel();
        signaturePanel.setLayout(new BoxLayout(signaturePanel, BoxLayout.Y_AXIS));
        //signaturePanel.setPreferredSize(new Dimension(280, 300));
        signaturePanel.setAlignmentX((float) 0.0); //LEFT
        
        
//        //TODO: Make SearchBox functional
//        // search Box:
//        JFormattedTextField searchBox = new JFormattedTextField();
//        searchBox.setName("Search");
//        signaturePanel.add(searchBox);
        
//        avail_sig_sets = paParams.getSignatureSetNames(); 
//        selected_sig_sets = paParams.getSelectedSignatureSetNames();
        
        //List of all Signature Genesets 
        JPanel availableLabel = new JPanel(new FlowLayout());
        availableLabel.add(new JLabel("Available Signature-Genesets:"));
        avail_sig_sets_counter_label = new JLabel("(0)");
        availableLabel.add(avail_sig_sets_counter_label);
        signaturePanel.add(availableLabel);
        avail_sig_sets_field = new JList<>();
        
        JScrollPane avail_sig_sets_scroll = new JScrollPane(    
                avail_sig_sets_field, 
                ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, 
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        avail_sig_sets_scroll.setPreferredSize(new Dimension(250, 200));
        avail_sig_sets_scroll.setMinimumSize(new Dimension(250, 150));
        avail_sig_sets_scroll.setMaximumSize(new Dimension(290, 300));
        signaturePanel.add(avail_sig_sets_scroll);
        

        //(Un-)Select-Buttons
        Icon[] icons = createArrowIcons();
        JPanel selectButtonPanel = new JPanel();
        selectButtonPanel.add(new JPanel()); //spacer
        selectButtonPanel.setLayout(new BoxLayout(selectButtonPanel, BoxLayout.X_AXIS));
        JButton selectButton = new JButton(icons[DOWN]);
        selectButton.getSize().width=30;
        selectButtonPanel.add(selectButton);
        selectButtonPanel.add(new JPanel()); //spacer
        JButton unselectButton = new JButton(icons[UP]);
        unselectButton.getSize().width=30;
        selectButtonPanel.add(unselectButton);
        selectButtonPanel.add(new JPanel()); //spacer
        signaturePanel.add(selectButtonPanel);

        //List of selected Signature Genesets 
        JPanel selectedLabel = new JPanel();
        selectedLabel.add( new JLabel("Selected Signature-Genesets:"));
        selected_sig_sets_counter_label = new JLabel("(0)");
        selectedLabel.add(selected_sig_sets_counter_label);
        signaturePanel.add(selectedLabel);
        selected_sig_sets_field = new JList<>();

        JScrollPane selected_sig_sets_scroll = new JScrollPane(    
                selected_sig_sets_field, 
                ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, 
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        selected_sig_sets_scroll.setPreferredSize(new Dimension(250, 100));
        selected_sig_sets_scroll.setMinimumSize(new Dimension(250, 100));
        selected_sig_sets_scroll.setMaximumSize(new Dimension(290, 200));
        signaturePanel.add(selected_sig_sets_scroll);
        
        // Add clear panels button
        JPanel clearButtonPanel = new JPanel();
        clearButtonPanel.setLayout(new FlowLayout());
        JButton clearButton = new JButton("Clear Signature Genesets");
        clearButtonPanel.add(clearButton);
        signaturePanel.add(clearButtonPanel);
        
        //ActionListener for clear button
        clearButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				paParams.setSignatureGenesets(new SetOfGeneSets());
		        avail_sig_sets.clear();
		        avail_sig_sets_field.clearSelection();
		        setAvSigCount(0);
		        
		        selected_sig_sets.clear();
		        selected_sig_sets_field.clearSelection();
		        setSelSigCount(0);			
		   }
        });
 
        //ActionListeners for (Un-)SelectButtons
        selectButton.addActionListener(new ActionListener() {
			@Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                int[] selected = avail_sig_sets_field.getSelectedIndices();
                for (int i = selected.length; i > 0 ; i--  ) {
                    selected_sig_sets.addElement( avail_sig_sets.get(selected[i-1]) );
                    avail_sig_sets.remove(selected[i-1]);
                }
                setSelSigCount(selected_sig_sets.size());
                setAvSigCount(avail_sig_sets.size());
            }
        });        
        unselectButton.addActionListener(new ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                int[] selected = selected_sig_sets_field.getSelectedIndices();
                for (int i = selected.length; i > 0 ; i--  ) {
                    avail_sig_sets.addElement( selected_sig_sets.get(selected[i-1]) );
                    selected_sig_sets.remove(selected[i-1]);
                }
                
                //Sort the Genesets:
                List<String> setNamesArray = Collections.list(avail_sig_sets.elements());
                Collections.sort(setNamesArray);
                avail_sig_sets.removeAllElements();
                for (String name : setNamesArray) {
                    avail_sig_sets.addElement(name);
                }
                setAvSigCount(avail_sig_sets.size());
                setSelSigCount(selected_sig_sets.size());            
            }
        });
        signature_genesets.getContentPane().add(signaturePanel, BorderLayout.NORTH);
        
        //Parameters collapsible panel
        weightPanel = new PostAnalysisWeightPanel(application);
        weightPanel.setCollapsed(false);
        
        add(gmtPanel);
        add(signature_genesets);
        add(weightPanel);        
    }
 
    /**
     * @return CollapsiblePanel for choosing and loading GMT and SignatureGMT Geneset-Files 
     */
    private CollapsiblePanel createSignatureDiscoveryGMTPanel() {
        CollapsiblePanel collapsiblePanel = new CollapsiblePanel("Gene-Sets");

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        
        //add SigGMT file
        JLabel SigGMTLabel = new JLabel("SigGMT:"){
            public JToolTip createToolTip() {
                return new JMultiLineToolTip();
            }
        };
        SigGMTLabel.setToolTipText(PostAnalysisInputPanel.gmtTip);
        JButton selectSigGMTFileButton = new JButton();
        signatureDiscoveryGMTFileNameTextField = new JFormattedTextField() ;
        signatureDiscoveryGMTFileNameTextField.setColumns(15);


        //components needed for the directory load
        signatureDiscoveryGMTFileNameTextField.setFont(new java.awt.Font("Dialog",1,10));
        //GMTFileNameTextField.setText(gmt_instruction);
        signatureDiscoveryGMTFileNameTextField.addPropertyChangeListener("value", new FormattedTextFieldAction());


        selectSigGMTFileButton.setText("...");
        selectSigGMTFileButton.setMargin(new Insets(0,0,0,0));
        selectSigGMTFileButton.setActionCommand("Signature Discovery");
        selectSigGMTFileButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                parentPanel.chooseGMTFile(signatureDiscoveryGMTFileNameTextField);
            }
        });

        JPanel SigGMTPanel = new JPanel();
        SigGMTPanel.setLayout(new BorderLayout());

        SigGMTPanel.add(SigGMTLabel, BorderLayout.WEST);
        SigGMTPanel.add(signatureDiscoveryGMTFileNameTextField, BorderLayout.CENTER);
        SigGMTPanel.add(selectSigGMTFileButton, BorderLayout.EAST);
        //add the components to the panel
        
        panel.add(SigGMTPanel);

        
        CollapsiblePanel filterPanel = createFilterPanel();
		panel.add(filterPanel);

        //TODO: Maybe move loading SigGMT to File-selection Event add load button
        JButton loadButton = new JButton();
        loadButton.setText("Load Gene-Sets");
        loadButton.addActionListener(new LoadSignatureSetsActionListener(parentPanel, application, cyApplicationManager, dialog, streamUtil));
        
        loadButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(loadButton);
        
        collapsiblePanel.getContentPane().add(panel, BorderLayout.NORTH);
        return collapsiblePanel;
    }
    
    
    
    /**
     *  Create a sub-panel so the user can specify filters so when loading in Signature gene set files
     *  they can limit the genesets loaded in based on the how many genes overlap with the current EM analyzing.
     *
     *  @return CollapsiblePanel to set Filter on Postanalysis genesets
     */
    private CollapsiblePanel createFilterPanel(){
        CollapsiblePanel collapsiblePanel = new CollapsiblePanel("Filter");
        collapsiblePanel.setCollapsed(false);
        
        filterTextField = new JFormattedTextField() ;
        filterTextField.setColumns(4);
        filterTextField.addPropertyChangeListener("value", new FormattedTextFieldAction());

        //Two types of filters:
        // 1. filter by percent, i.e. the overlap between the signature geneset and EM geneset
        // has to be X percentage of the EM set it overlaps with for at least one geneset in the enrichment map
        // 2. filter by number, i.e. the overlap between the signature geneset and EM geneset
        // has to be X genes of the EM set it overlaps with for at least one geneset in the enrichment map
        // 3. filter by specificity, i.e looking for the signature genesets that are more specific than other genesets
        // for instance a drug A that targets only X and Y as opposed to drug B that targets X,y,L,M,N,O,P
        filterTypeCombo = new JComboBox<Object>();
        filterTypeCombo.addItem(NO_FILTER); // default
        filterTypeCombo.addItem(FilterMetric.HYPERGEOM);
        filterTypeCombo.addItem(FilterMetric.MANN_WHIT);
        filterTypeCombo.addItem(FilterMetric.PERCENT);
        filterTypeCombo.addItem(FilterMetric.NUMBER);
        filterTypeCombo.addItem(FilterMetric.SPECIFIC);

        filterTypeCombo.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if(filterTypeCombo.getSelectedItem().equals(NO_FILTER)) {
                	paParams.setFilter(false);
                	filterTextField.setEnabled(false);
                }
                else {
	                paParams.setFilter(true);
	                filterTextField.setEnabled(true);
	                FilterMetric filterMetric = (FilterMetric)filterTypeCombo.getSelectedItem();
	                paParams.setSignature_filterMetric(filterMetric);
	                switch(filterMetric) {
						case HYPERGEOM:
			                filterTextField.setValue(paParams.getSignature_Hypergeom_Cutoff());
							break;
						case MANN_WHIT:
		                	filterTextField.setValue(paParams.getSignature_Mann_Whit_Cutoff());
							break;
						case NUMBER:
						case PERCENT:
						case SPECIFIC:
		                    filterTextField.setValue(paParams.getFilterValue());
		                    break;
	                }
                }
            }
        });
       
        JPanel filterTypePanel = new JPanel(new BorderLayout());
        filterTypePanel.add(filterTypeCombo, BorderLayout.CENTER);
        filterTypePanel.add(filterTextField, BorderLayout.EAST);

        collapsiblePanel.getContentPane().add(filterTypePanel);
        return collapsiblePanel;
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

           if (source == signatureDiscoveryGMTFileNameTextField) {
                String value = signatureDiscoveryGMTFileNameTextField.getText();
                if(value.equalsIgnoreCase("") )
                    paParams.setSignatureGMTFileName(value);
                else if(signatureDiscoveryGMTFileNameTextField.getText().equalsIgnoreCase((String)e.getOldValue())){
                    //do nothing
                }
                else if(PostAnalysisInputPanel.checkFile(value).equals(Color.RED)){
                    JOptionPane.showMessageDialog(application.getJFrame(),message,"File name change entered is not a valid file name",JOptionPane.WARNING_MESSAGE);
                    signatureDiscoveryGMTFileNameTextField.setForeground(PostAnalysisInputPanel.checkFile(value));
                }
                else {
                    paParams.setSignatureGMTFileName(value);
//                    paParams.setSignatureSetNames(new DefaultListModel());
//                    paParams.setSelectedSignatureSetNames(new DefaultListModel());
                }
            } 
            else if (source == filterTextField) {
                Number value = (Number) filterTextField.getValue();
                //if the filter type is percent then make sure the number entered is between 0 and 100
                if(paParams.getSignature_filterMetric() == FilterMetric.HYPERGEOM){
                    if ((value != null) && (value.doubleValue() >= 0.0) && (value.intValue() <= 1.0)) {
                        paParams.setSignature_Hypergeom_Cutoff(value.doubleValue());
                    } else {
                        source.setValue(paParams.getDefault_signature_Hypergeom_Cutoff());
                        message += "The filter cutoff must be greater than or equal 0.0 and less than or equal to 1.0";
                        invalid = true;
                    }
                } else if(paParams.getSignature_filterMetric() == FilterMetric.MANN_WHIT){
                    if ((value != null) && (value.doubleValue() >= 0.0) && (value.intValue() <= 1.0)) {
                        paParams.setSignature_Mann_Whit_Cutoff(value.doubleValue());
                    } else {
                        source.setValue(paParams.getDefault_signature_Mann_Whit_Cutoff());
                        message += "The filter cutoff must be greater than or equal 0.0 and less than or equal to 1.0";
                        invalid = true;
                    }
                } else if(paParams.getSignature_filterMetric() == FilterMetric.PERCENT){
                    if ((value != null) && (value.intValue() >= 0) && (value.intValue() <= 100)) {
                        paParams.setFilterValue(value.intValue());
                    } else {
                        source.setValue(paParams.getFilterValue());
                        message += "The filter cutoff must be greater than or equal 0 and less than or equal to 100.";
                        invalid = true;
                    }
                }
                //if the filter type is NUMBER then it can be any number, zero or greater.
                else if(paParams.getSignature_filterMetric() == FilterMetric.NUMBER){
                    if ((value != null) && (value.intValue() >= 0)) {
                        paParams.setFilterValue(value.intValue());
                    } else {
                        source.setValue(paParams.getFilterValue());
                        message += "The filter cutoff must be greater than or equal 0.";
                        invalid = true;
                    }
                }
            }
            
            if (invalid) {
                JOptionPane.showMessageDialog(application.getJFrame(), message, "Parameter out of bounds", JOptionPane.WARNING_MESSAGE);
            }
        }
    }
    
    
    void resetPanel() {
    	paParams.setSignatureGenesets(new SetOfGeneSets());
    	
    	// Reset the text field
        signatureDiscoveryGMTFileNameTextField.setText("");
        signatureDiscoveryGMTFileNameTextField.setValue("");
        signatureDiscoveryGMTFileNameTextField.setToolTipText(null);
                    	       
        // Reset the List fields:
        paParams.getSignatureSetNames().clear();
        avail_sig_sets.clear();
        avail_sig_sets_field.clearSelection();
        setAvSigCount(0);
        
        paParams.getSelectedSignatureSetNames().clear();
        selected_sig_sets.clear();
        selected_sig_sets_field.clearSelection();
        setSelSigCount(0);

        // Reset the filter field
        paParams.setFilter(false);
        filterTypeCombo.setSelectedItem(NO_FILTER);
        weightPanel.resetPanel();
    }
    
    
    void updateContents(EnrichmentMap currentMap, PostAnalysisParameters paParams) {
		this.map = currentMap;
		this.paParams = paParams;
        
		weightPanel.updateContents(currentMap, paParams);
		
        filterTextField.setValue(paParams.getSignature_Hypergeom_Cutoff());
        
        if(paParams.isFilter())
        	filterTypeCombo.setSelectedItem(paParams.getDefault_signature_filterMetric());
        else
        	filterTypeCombo.setSelectedItem(NO_FILTER);
        
        avail_sig_sets = paParams.getSignatureSetNames(); 
        selected_sig_sets = paParams.getSelectedSignatureSetNames();
        
        avail_sig_sets_field.setModel(avail_sig_sets);
        selected_sig_sets_field.setModel(selected_sig_sets);
    }
    
    /**
     * @return Array with arrows UP, DOWN, LEFT and RIGHT
     */
    private ImageIcon[] createArrowIcons () {
        ImageIcon[] iconArrow = new ImageIcon[4];
        URL iconURL;
        //                         Oliver at 26/06/2009:  relative path works for me,
        //                         maybe need to change to org/baderlab/csplugins/enrichmentmap/resources/arrow_collapsed.gif
        iconURL = this.getClass().getResource("arrow_up.gif");
        if (iconURL != null) {
            iconArrow[UP] = new ImageIcon(iconURL);
        }
        iconURL = this.getClass().getResource("arrow_down.gif");
        if (iconURL != null) {
            iconArrow[DOWN] = new ImageIcon(iconURL);
        }
        iconURL = this.getClass().getResource("arrow_left.gif");
        if (iconURL != null) {
            iconArrow[LEFT] = new ImageIcon(iconURL);
        }
        iconURL = this.getClass().getResource("arrow_right.gif");
        if (iconURL != null) {
            iconArrow[RIGHT] = new ImageIcon(iconURL);
        }
        return iconArrow;
    }
    
    /**
     * jaccard or overlap radio button action listener
     *
     * @param evt
     */
    private void selectFilterActionPerformed(java.awt.event.ActionEvent evt) {
        if(evt.getActionCommand().equalsIgnoreCase("filter")){
            paParams.setFilter(true);
        }
        else if(evt.getActionCommand().equalsIgnoreCase("nofilter")){
            paParams.setFilter(false);
        }
    }
    
    
    /**
	 * Set available signature gene set count to specified value
	 * @param int avSigCount
	 * @return null
	 */
	public void setAvSigCount(int avSigCount) {
//		this.avail_sig_sets_count = avSigCount;
		this.avail_sig_sets_counter_label.setText("(" + Integer.toString(avSigCount) + ")");
	}
	
	/**
	 * Set selected signature gene set count to the 
	 * specified value
	 * @param int sigCount
	 * @return null
	 */
	public void setSelSigCount(int num) {
//		this.sel_sig_sets_count = num;
		this.selected_sig_sets_counter_label.setText("(" + Integer.toString(num) + ")");
	}
}
