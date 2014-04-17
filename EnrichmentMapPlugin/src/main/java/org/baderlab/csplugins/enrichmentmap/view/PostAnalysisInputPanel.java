/*
 *                       EnrichmentMap Cytoscape Plugin
 *
 * Copyright (c) 2008-2009 Bader Lab, Donnelly Centre for Cellular and Biomolecular
 * Research, University of Toronto
 *
 * Contact: http://www.baderlab.org
 *
 * Code written by: Ruth Isserlin
 * Authors: Daniele Merico, Ruth Isserlin, Oliver Stueker, Gary D. Bader
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 * documentation provided hereunder is on an "as is" basis, and
 * University of Toronto
 * has no obligations to provide maintenance, support, updates,
 * enhancements or modifications.  In no event shall the
 * University of Toronto
 * be liable to any party for direct, indirect, special,
 * incidental or consequential damages, including lost profits, arising
 * out of the use of this software and its documentation, even if
 * University of Toronto
 * has been advised of the possibility of such damage.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 *
 */

// $Id$
// $LastChangedDate$
// $LastChangedRevision$
// $LastChangedBy$
// $HeadURL$

package org.baderlab.csplugins.enrichmentmap.view;

import cytoscape.view.CytoscapeDesktop;
import cytoscape.view.cytopanels.CytoPanel;
import cytoscape.Cytoscape;
import cytoscape.task.ui.JTaskConfig;
import cytoscape.task.util.TaskManager;
import cytoscape.util.CyFileFilter;
import cytoscape.util.FileUtil;
import cytoscape.util.OpenBrowser;

import javax.swing.*;

import org.baderlab.csplugins.enrichmentmap.EnrichmentMapManager;
import org.baderlab.csplugins.enrichmentmap.EnrichmentMapParameters;
import org.baderlab.csplugins.enrichmentmap.EnrichmentMapUtils;
import org.baderlab.csplugins.enrichmentmap.PostAnalysisParameters;
import org.baderlab.csplugins.enrichmentmap.actions.BuildPostAnalysisActionListener;
import org.baderlab.csplugins.enrichmentmap.actions.ShowAboutPanelAction;
import org.baderlab.csplugins.enrichmentmap.model.DataSet;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.model.GeneSet;
import org.baderlab.csplugins.enrichmentmap.model.JMultiLineToolTip;
import org.baderlab.csplugins.enrichmentmap.model.Ranking;
import org.baderlab.csplugins.enrichmentmap.model.SetOfGeneSets;
import org.baderlab.csplugins.enrichmentmap.task.LoadSignatureGMTFilesTask;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.io.File;
import java.net.URL;

/**
 * Created by
 * @author revilo
 * <p>
 * Date July 9, 2009
 * 
 * Based on: EnrichmentMapInputPanel.java (302) by risserlin
 */

public class PostAnalysisInputPanel extends JPanel {
    /**
     * 
     */
    private static final long serialVersionUID = 5472169142720323583L;

    final static int RIGHT = 0, DOWN = 1, UP = 2, LEFT = 3; // image States

    CollapsiblePanel Parameters;
    CollapsiblePanel signature_genesets;

    CollapsiblePanel filters;
    private JRadioButton filter;
    private JRadioButton nofilter;
    private JFormattedTextField filterTextField;
    private JComboBox filterTypeCombo;

    CollapsiblePanel dataset1;
    CollapsiblePanel dataset2;

    JPanel signaturePanel;

    DecimalFormat decFormat; // used in the formatted text fields

    // For determining universe size
    HashSet<Integer> EnrichmentGenes;
    
    private PostAnalysisParameters paParams;
    
	// 'Known Signature Panel' parameters
    private JPanel knownSignaturePanel;
    private PostAnalysisParameters knownSigPaParams;
    private JRadioButton knownSignature;
    private JFormattedTextField knownSigUniverseSelectionTextField;
	private JFormattedTextField knownSignatureGMTFileNameTextField;
	private JComboBox knownSignatureRankTestCombo;
	private JFormattedTextField knownSignatureRankTestTextField;
	private JRadioButton KnownSigGMTRadioButton;
	private JRadioButton KnownSigExpressionSetRadioButton;
	private JRadioButton KnownSigIntersectionRadioButton;
	private JRadioButton KnownSigUserDefinedRadioButton;

	// 'Signature Discovery Panel' parameters
	private PostAnalysisParameters sigDiscoveryPaParams;
    private JRadioButton signatureDiscovery;
    private JFormattedTextField signatureDiscoveryGMTFileNameTextField;
    private JFormattedTextField sigDiscoveryUniverseSelectionTextField;
	private JPanel signatureDiscoveryPanel;
	private JComboBox sigDiscoveryRankingCombo;
	private JComboBox signatureDiscoveryRankTestCombo;
	private JFormattedTextField signatureDiscoveryRankTestTextField;
	private JRadioButton SigDiscoveryGMTRadioButton;
	private JRadioButton SigDiscoveryIntersectionRadioButton;
	private JRadioButton SigDiscoveryUserDefinedRadioButton;
	private JRadioButton SigDiscoveryExpressionSetRadioButton;

	private JLabel avail_sig_sets_counter_label;
    private int avail_sig_sets_count = 0;
    private JLabel selected_sig_sets_counter_label;
    private int sel_sig_sets_count = 0;
    private JList avail_sig_sets_field;
    private JList selected_sig_sets_field;
    private DefaultListModel avail_sig_sets;
    private DefaultListModel selected_sig_sets;
    
    JPanel optionsPanel;
    CollapsiblePanel userInputPanel;
    
    // These hashmaps are needed for edge weight calculation
    private HashMap<String, DataSet> datasetMap;
    private HashMap<String, Ranking> rankingMap;
    
    private DefaultComboBoxModel datasetModel;
    private DefaultComboBoxModel rankingModel;

    
    private int defaultColumns = 15;

    //
    //Texts:
    //
    
    public static String gmt_instruction = "Please select the Gene Set file (.gmt)...";
    public static String siggmt_instruction = "Please select the Signature Gene Set file (.gmt)...";
    //tool tips
    private static String gmtTip = "File specifying gene sets.\n" + "Format: geneset name <tab> description <tab> gene ...";
    
    private EnrichmentMap map;
    
    public PostAnalysisInputPanel() {

        decFormat = new DecimalFormat();
        decFormat.setParseIntegerOnly(false);

        setLayout(new BorderLayout());

        //get the current enrichment map parameters
        map = EnrichmentMapManager.getInstance().getMap(Cytoscape.getCurrentNetwork().getIdentifier());
        
        HashMap<String, GeneSet> EnrichmentGenesets = map.getAllGenesets();
        EnrichmentGenes = new HashSet<Integer>();
        for (Iterator<String> i = map.getAllGenesets().keySet().iterator(); i.hasNext(); ) {
            String setName = i.next();
            EnrichmentGenes.addAll(EnrichmentGenesets.get(setName).getGenes());
        }
        
        datasetMap = map.getDatasets();
        rankingMap = map.getAllRanks();
        EnrichmentMapParameters emParams = map.getParams();
        if(map != null){
        		emParams = map.getParams();
        		// create instance of PostAnalysisParameters an initialize with EnrichmentMapParameters
        		knownSigPaParams = map.getPaParams();
        }else
        		JOptionPane.showMessageDialog(this, "No Enrichment map was detected.  \nCan only perform Post Analysis on existing Enrichment map.");

        if (emParams == null)
            emParams = new EnrichmentMapParameters();
        if(knownSigPaParams == null)
        		knownSigPaParams = new PostAnalysisParameters();
        
        sigDiscoveryPaParams = new PostAnalysisParameters();
        sigDiscoveryPaParams.copyFrom(knownSigPaParams);
        
        paParams = knownSigPaParams;

        //create the three main panels: scope, advanced options, and bottom
        JPanel AnalysisTypePanel = createAnalysisTypePanel();

        //Put the options panel into a scroll pain

        userInputPanel = new CollapsiblePanel("User Input");
        userInputPanel.setCollapsed(false);
        optionsPanel = getKnownSignatureOptionsPanel();
        userInputPanel.getContentPane().add(optionsPanel);
        JScrollPane scrollPane = new JScrollPane(userInputPanel);
        //scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        JPanel bottomPanel = createBottomPanel();

        //Since the advanced options panel is being added to the center of this border layout
        //it will stretch it's height to fit the main panel.  To prevent this we create an
        //additional border layout panel and add advanced options to it's north compartment
        JPanel advancedOptionsContainer = new JPanel(new BorderLayout());
        advancedOptionsContainer.add(scrollPane, BorderLayout.CENTER);

        //Add all the vertically aligned components to the main panel
        add(AnalysisTypePanel,BorderLayout.NORTH);
        add(advancedOptionsContainer,BorderLayout.CENTER);
        add(bottomPanel,BorderLayout.SOUTH);

    }

	private JPanel getSignatureDiscoveryOptionsPanel() {
		if (signatureDiscoveryPanel == null) {
			signatureDiscoveryPanel = this.createSignatureDiscoveryOptionsPanel();
		}
		return signatureDiscoveryPanel;
	}

	/**
     * Creates a JPanel containing scope radio buttons
     *
     * @return panel containing the scope option buttons
     */
    private JPanel createAnalysisTypePanel() {

        JPanel buttonsPanel = new JPanel();
        GridBagLayout gridbag_buttons = new GridBagLayout();
        GridBagConstraints c_buttons = new GridBagConstraints();
        buttonsPanel.setLayout(gridbag_buttons);
        buttonsPanel.setBorder(BorderFactory.createTitledBorder("Info:"));

        JButton help = new JButton("Online Manual");
        help.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                OpenBrowser.openURL(EnrichmentMapUtils.userManualUrl);
            }
        });

        JButton about = new JButton("About");
        about.addActionListener(new ShowAboutPanelAction());

        c_buttons.weighty = 1;
        c_buttons.weightx = 1;
        c_buttons.insets = new Insets(0,0,0,0);
        c_buttons.gridx = 0;
        c_buttons.gridwidth = 1;
        c_buttons.gridy = 0;
        c_buttons.fill = GridBagConstraints.HORIZONTAL;

        c_buttons.gridy = 0;
        gridbag_buttons.setConstraints(about, c_buttons);
        buttonsPanel.add(about);

        c_buttons.gridy = 1;
        gridbag_buttons.setConstraints(help, c_buttons);
        buttonsPanel.add(help);


        JPanel panel = new JPanel();

        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        panel.setLayout(gridbag);

        c.weighty = 1;
        c.weightx = 1;
        c.insets = new Insets(0,0,0,0);
        c.fill = GridBagConstraints.HORIZONTAL;
        panel.setBorder(BorderFactory.createTitledBorder("Post Analysis Type"));
        
        // Known Signature
        knownSignature = new JRadioButton("Known Signature", paParams.isKnownSignature());
        knownSignature.setActionCommand("Known Signature");
        knownSignature.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectAnalysisTypeActionPerformed(evt);
            }
        });        
        knownSignature.setSelected(true);

        // Signature Discovery
        signatureDiscovery = new JRadioButton("Signature Discovery", paParams.isSignatureDiscovery());
        signatureDiscovery.setActionCommand("Signature Discovery");
        signatureDiscovery.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectAnalysisTypeActionPerformed(evt);
            }
        });

        ButtonGroup analysisOptions = new ButtonGroup();
        analysisOptions.add(knownSignature);
        analysisOptions.add(signatureDiscovery);

        c.gridx = 0;
        c.gridwidth = 3;
        c.gridy = 0;
        gridbag.setConstraints(knownSignature, c);
        panel.add(knownSignature);
        
        c.gridy = 1;
        gridbag.setConstraints(signatureDiscovery, c);
        panel.add(signatureDiscovery);
        
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BorderLayout());
        topPanel.add(buttonsPanel, BorderLayout.EAST);
        topPanel.add(panel, BorderLayout.CENTER);

        return topPanel;
    }
    
    /* ************************************************* *
     *            Functions to return Panels             *
     * ************************************************* */

    /**
     * @return collapsiblePanel to select Signature Genesets for Signature Analysis
     */
    private JPanel createSignatureDiscoveryOptionsPanel() {

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        //Gene set file panel
        CollapsiblePanel GMTPanel = createSignatureDiscoveryGMTPanel();
        GMTPanel.setCollapsed(false);

        
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
        
        avail_sig_sets = paParams.getSignatureSetNames(); 
        selected_sig_sets = paParams.getSelectedSignatureSetNames();
        
        //List of all Signature Genesets 
        JPanel availableLabel = new JPanel(new FlowLayout());
        availableLabel.add(new JLabel("Available Signature-Genesets:"));
        availableLabel.add(this.createAvSigCountLabel());
        signaturePanel.add(availableLabel);
        avail_sig_sets_field = new JList(avail_sig_sets);
        
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
        selectedLabel.add(this.createSelSigCountLabel());
        signaturePanel.add(selectedLabel);
        selected_sig_sets_field = new JList(selected_sig_sets);

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
        clearButton.addActionListener(new PaPanelActionListener(this) {
			public void actionPerformed(ActionEvent e) {
				this.paPanel.paParams.setSignatureGenesets(new SetOfGeneSets());
		        this.paPanel.avail_sig_sets.clear();
		        this.paPanel.avail_sig_sets_field.clearSelection();
		        this.paPanel.setAvSigCount(0);
		        
		        this.paPanel.selected_sig_sets.clear();
		        this.paPanel.selected_sig_sets_field.clearSelection();
		        this.paPanel.setSelSigCount(0);			
		   }
        }); {}
 
        //ActionListeners for (Un-)SelectButtons
        selectButton.addActionListener(new PaPanelActionListener(this) {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                int[] selected = avail_sig_sets_field.getSelectedIndices();
                for (int i = selected.length; i > 0 ; i--  ) {
                    selected_sig_sets.addElement( avail_sig_sets.get(selected[i-1]) );
                    avail_sig_sets.remove(selected[i-1]);
                }
                this.paPanel.setSelSigCount(selected_sig_sets.size());
                this.paPanel.setAvSigCount(avail_sig_sets.size());
            }
        });        
        unselectButton.addActionListener(new PaPanelActionListener(this) {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                int[] selected = selected_sig_sets_field.getSelectedIndices();
                for (int i = selected.length; i > 0 ; i--  ) {
                    avail_sig_sets.addElement( selected_sig_sets.get(selected[i-1]) );
                    selected_sig_sets.remove(selected[i-1]);
                }
                
                //Sort the Genesets:
                Object[] setNamesArray = avail_sig_sets.toArray();
                Arrays.sort( setNamesArray );
                avail_sig_sets.removeAllElements();
                for (int i = 0; i < setNamesArray.length; i++) {
                    avail_sig_sets.addElement(setNamesArray[i] );
                }
                this.paPanel.setAvSigCount(avail_sig_sets.size());
                this.paPanel.setSelSigCount(selected_sig_sets.size());            
            }
        });
        signature_genesets.getContentPane().add(signaturePanel, BorderLayout.NORTH);
        
        //Parameters collapsible panel
        CollapsiblePanel ParametersPanel = createSignatureDiscoveryParametersPanel();
        ParametersPanel.setCollapsed(false);
        
        panel.add(GMTPanel);
        panel.add(signature_genesets);
        panel.add(ParametersPanel);        
        return panel;
    }
    
    /**
     * @return collapsiblePanel to select Signature Genesets for Signature Analysis
     */
    private JPanel createKnownSignatureOptionsPanel() {

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        //Gene set file panel
        CollapsiblePanel GMTPanel = createKnownSignatureGMTPanel();
        GMTPanel.setCollapsed(false);
        
        //Parameters collapsible panel
        CollapsiblePanel ParametersPanel = createKnownSignatureParametersPanel();
        ParametersPanel.setCollapsed(false);
        
        panel.add(GMTPanel);
        panel.add(ParametersPanel);        
        return panel;
    }
    
    /**
     * @return CollapsiblePanel for choosing and loading GMT and SignatureGMT Geneset-Files 
     */
    private CollapsiblePanel createKnownSignatureGMTPanel() {
        CollapsiblePanel collapsiblePanel = new CollapsiblePanel("Gene-Sets");

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        //add SigGMT file
        JLabel SigGMTLabel = new JLabel("SigGMT:"){
            /**
             * 
             */
            private static final long serialVersionUID = 8826340546360207691L;

            public JToolTip createToolTip() {
                return new JMultiLineToolTip();
            }
        };
        SigGMTLabel.setToolTipText(gmtTip);
        JButton selectSigGMTFileButton = new JButton();
        knownSignatureGMTFileNameTextField = new JFormattedTextField() ;
        knownSignatureGMTFileNameTextField.setColumns(defaultColumns);


        //components needed for the directory load
        knownSignatureGMTFileNameTextField.setFont(new java.awt.Font("Dialog",1,10));
        //GMTFileNameTextField.setText(gmt_instruction);
        knownSignatureGMTFileNameTextField.addPropertyChangeListener("value",new PostAnalysisInputPanel.FormattedTextFieldAction());


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

    /**
     * @return CollapsiblePanel for choosing and loading GMT and SignatureGMT Geneset-Files 
     */
    private CollapsiblePanel createSignatureDiscoveryGMTPanel() {
        CollapsiblePanel collapsiblePanel = new CollapsiblePanel("Gene-Sets");

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        
        //add SigGMT file
        JLabel SigGMTLabel = new JLabel("SigGMT:"){
            /**
             * 
             */
            private static final long serialVersionUID = 8826340546360207691L;

            public JToolTip createToolTip() {
                return new JMultiLineToolTip();
            }
        };
        SigGMTLabel.setToolTipText(gmtTip);
        JButton selectSigGMTFileButton = new JButton();
        signatureDiscoveryGMTFileNameTextField = new JFormattedTextField() ;
        signatureDiscoveryGMTFileNameTextField.setColumns(defaultColumns);


        //components needed for the directory load
        signatureDiscoveryGMTFileNameTextField.setFont(new java.awt.Font("Dialog",1,10));
        //GMTFileNameTextField.setText(gmt_instruction);
        signatureDiscoveryGMTFileNameTextField.addPropertyChangeListener("value",new PostAnalysisInputPanel.FormattedTextFieldAction());


        selectSigGMTFileButton.setText("...");
        selectSigGMTFileButton.setMargin(new Insets(0,0,0,0));
        selectSigGMTFileButton.setActionCommand("Signature Discovery");
        selectSigGMTFileButton
        .addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectSignatureGMTFileButtonActionPerformed(evt);
            }
        });

        JPanel SigGMTPanel = new JPanel();
        SigGMTPanel.setLayout(new BorderLayout());

        SigGMTPanel.add( SigGMTLabel,BorderLayout.WEST);
        SigGMTPanel.add( signatureDiscoveryGMTFileNameTextField, BorderLayout.CENTER);
        SigGMTPanel.add( selectSigGMTFileButton, BorderLayout.EAST);
        //add the components to the panel
        panel.add(SigGMTPanel);

        panel.add(createFilterPanel());

        //TODO: Maybe move loading SigGMT to File-selection Event
        //add load button
        JButton loadButton = new JButton();
        loadButton.setText("Load Gene-Sets");
        loadButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadGenesetsButtonActionPerformed(evt);
            }
        });
        //loadButton.setPreferredSize(new Dimension(100,10));
        JPanel loadButtonPanel = new JPanel();
        loadButtonPanel.setLayout(new FlowLayout());
        loadButtonPanel.add(loadButton);
        panel.add(loadButtonPanel);        
        
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
        CollapsiblePanel collapsiblePanel = new CollapsiblePanel("Filters");
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(2,1));
        //create radio button
        ButtonGroup filters;

        //radio button for filter or no-filter.  Defaults to no-filter
        filter = new JRadioButton("Filter By");
        filter.setActionCommand("filter");
        filter.setSelected(true);
        paParams.setFilter(true);
        nofilter = new JRadioButton("No Filter");
        nofilter.setActionCommand("nofilter");
        nofilter.setSelected(false);

        filters = new ButtonGroup();
        filters.add(filter);
        filters.add(nofilter);

        //action listener for filter radio button.
        filter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectFilterActionPerformed(evt);
            }
        });
        nofilter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectFilterActionPerformed(evt);
            }
        });

        JPanel filtersPanel = new JPanel();
        filtersPanel.setLayout(new BoxLayout(filtersPanel, BoxLayout.Y_AXIS));

        filtersPanel.add(nofilter);

        String[] filterItems = PostAnalysisParameters.filterItems;
        
        filterTextField = new JFormattedTextField() ;
        filterTextField.setColumns(4);
        filterTextField.setValue(paParams.getSignature_Hypergeom_Cutoff());
        filterTextField.addPropertyChangeListener("value", new PostAnalysisInputPanel.FormattedTextFieldAction());

        //Two types of filters:
        // 1. filter by percent, i.e. the overlap between the signature geneset and EM geneset
        // has to be X percentage of the EM set it overlaps with for at least one geneset in the enrichment map
        // 2. filter by number, i.e. the overlap between the signature geneset and EM geneset
        // has to be X genes of the EM set it overlaps with for at least one geneset in the enrichment map
        // 3. filter by specificity, i.e looking for the signature genesets that are more specific than other genesets
        // for instance a drug A that targets only X and Y as opposed to drug B that targets X,y,L,M,N,O,P
        JPanel filterTypePanel = new JPanel();
        filterTypePanel.setLayout(new BorderLayout());
        filterTypeCombo = new JComboBox();
        filterTypeCombo.addItem(filterItems[PostAnalysisParameters.HYPERGEOM]);
        filterTypeCombo.addItem(filterItems[PostAnalysisParameters.MANN_WHIT]);
        filterTypeCombo.addItem(filterItems[PostAnalysisParameters.PERCENT]);
        filterTypeCombo.addItem(filterItems[PostAnalysisParameters.NUMBER]);
        filterTypeCombo.addItem(filterItems[PostAnalysisParameters.SPECIFIC]);
        filterTypeCombo.setSelectedItem(paParams.getDefault_signature_filterMetric());

        //Add Action listener for filter type drop down menu
        filterTypeCombo.addActionListener( new ActionListener() {
            String[] filterItems = PostAnalysisParameters.filterItems;
            public void actionPerformed( ActionEvent e )
            {
                JComboBox selectedChoice = (JComboBox) e.getSource();
                if ( filterItems[PostAnalysisParameters.HYPERGEOM].equals( selectedChoice.getSelectedItem() ) ) {
                    paParams.setSignature_filterMetric(PostAnalysisParameters.HYPERGEOM);
                    filterTextField.setValue(paParams.getSignature_Hypergeom_Cutoff());
                } else if ( filterItems[PostAnalysisParameters.MANN_WHIT].equals( selectedChoice.getSelectedItem() ) ) {
                	paParams.setSignature_filterMetric(PostAnalysisParameters.MANN_WHIT);
                	filterTextField.setValue(paParams.getSignature_Mann_Whit_Cutoff());
                } else if ( filterItems[PostAnalysisParameters.PERCENT].equals( selectedChoice.getSelectedItem() ) ) {
                    paParams.setSignature_filterMetric(PostAnalysisParameters.PERCENT);
                    filterTextField.setValue(paParams.getFilterValue());
                } else if ( filterItems[PostAnalysisParameters.NUMBER].equals( selectedChoice.getSelectedItem() ) ) {
                    paParams.setSignature_filterMetric(PostAnalysisParameters.NUMBER);
                    filterTextField.setValue(paParams.getFilterValue());
                }else if ( filterItems[PostAnalysisParameters.SPECIFIC].equals( selectedChoice.getSelectedItem() ) ) {
                    paParams.setSignature_filterMetric(PostAnalysisParameters.SPECIFIC);
                    filterTextField.setValue(paParams.getFilterValue());
                }
            }
        });

        filterTypePanel.add(filter,BorderLayout.WEST);
        filterTypePanel.add(filterTypeCombo, BorderLayout.CENTER);
        filterTypePanel.add(filterTextField, BorderLayout.EAST);
        panel.add(filterTypePanel);
        panel.add(filtersPanel);

        collapsiblePanel.getContentPane().add(panel);
        return collapsiblePanel;
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
     * @return CollapsiblePanel to set PostAnalysisParameters 
     */
    private CollapsiblePanel createKnownSignatureParametersPanel() {
        CollapsiblePanel collapsiblePanel = new CollapsiblePanel("Edge Weight Calculation Parameters");
        
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        
        String[] datasetArray = datasetMap.keySet().toArray(new String[datasetMap.size()]);
        Arrays.sort(datasetArray);
        datasetModel = new DefaultComboBoxModel();
        for (String dataset : datasetArray) {
        	datasetModel.addElement(dataset);
        }
        JComboBox knownSigDatasetCombo = new JComboBox();
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
        knownSigDatasetCombo.setSelectedIndex(0);
        panel.add(knownSigDatasetCombo);
        
        String[] rankingArray = rankingMap.keySet().toArray(new String[rankingMap.size()]);
        Arrays.sort(rankingArray);
        rankingModel = new DefaultComboBoxModel();
        for (String ranking : rankingArray) {
        	rankingModel.addElement(ranking);
        }
        JComboBox knownSigRankingCombo = new JComboBox();
        knownSigRankingCombo.setModel(rankingModel);
        knownSigRankingCombo.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
            	JComboBox selectedChoice = (JComboBox) e.getSource();
            	paParams.setSignature_rankFile((String)selectedChoice.getSelectedItem());
            }
        });
        knownSigRankingCombo.setSelectedIndex(0);
        panel.add(knownSigRankingCombo);
        
        knownSignatureRankTestTextField = new JFormattedTextField();
        knownSignatureRankTestTextField.addPropertyChangeListener("value", new PostAnalysisInputPanel.FormattedTextFieldAction());
        
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
        knownSignatureRankTestCombo.setSelectedItem(filterItems[paParams.getDefault_signature_rankTest()]);
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
        
        DataSet dataset = map.getDataset(paParams.getSignature_dataSet());
    	int universeSize = 0;
    	if (dataset != null) {
    		universeSize = dataset.getDatasetGenes().size();
    	}
    	KnownSigGMTRadioButton = new JRadioButton("GMT (" + universeSize + ")");
        KnownSigGMTRadioButton.setActionCommand("GMT");
        KnownSigGMTRadioButton.addActionListener(new ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectKnownSigUniverseActionPerformed(evt);
            }
        });
        KnownSigGMTRadioButton.setSelected(true);
    	int expressionSetSize = map.getDataset(paParams.getSignature_dataSet()).getExpressionSets().getNumGenes();
        KnownSigExpressionSetRadioButton = new JRadioButton("Expression Set (" + expressionSetSize + ")");
        KnownSigExpressionSetRadioButton.setActionCommand("Expression Set");
        KnownSigExpressionSetRadioButton.addActionListener(new ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectKnownSigUniverseActionPerformed(evt);
            }
        });
    	HashSet<Integer> intersection = map.getDataset(paParams.getSignature_dataSet()).getDatasetGenes();
    	intersection.retainAll(map.getDataset(paParams.getSignature_dataSet()).getExpressionSets().getGeneIds());
        KnownSigIntersectionRadioButton = new JRadioButton("Intersection (" + intersection.size() + ")");
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
        knownSigUniverseSelectionTextField.addPropertyChangeListener("value", new PostAnalysisInputPanel.FormattedTextFieldAction());
        knownSigUniverseSelectionTextField.setText(Integer.toString(EnrichmentGenes.size()));
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
     * @return CollapsiblePanel to set PostAnalysisParameters 
     */
    private CollapsiblePanel createSignatureDiscoveryParametersPanel() {
        CollapsiblePanel collapsiblePanel = new CollapsiblePanel("Edge Weight Calculation Parameters");
        
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        
        JComboBox sigDiscoveryDatasetCombo = new JComboBox();
        // Dataset model is already initialized
        sigDiscoveryDatasetCombo.setModel(datasetModel);
        sigDiscoveryDatasetCombo.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
            	JComboBox selectedChoice = (JComboBox) e.getSource();
            	String dataset = (String)selectedChoice.getSelectedItem();
            	paParams.setSignature_dataSet(dataset);
    	        DataSet datasetObj = map.getDataset(paParams.getSignature_dataSet());
    	    	int universeSize = 0;
    	    	if (datasetObj != null) {
    	    		universeSize = datasetObj.getDatasetGenes().size();
    	    	}            	paParams.setUniverseSize(universeSize);
            	if (SigDiscoveryGMTRadioButton != null) {
            		SigDiscoveryGMTRadioButton.setText("GMT (" + universeSize + ")");
            	}
            	int expressionSetSize = map.getDataset(dataset).getExpressionSets().getNumGenes();
            	if (SigDiscoveryExpressionSetRadioButton != null) {
            		SigDiscoveryExpressionSetRadioButton.setText("Expression Set (" + expressionSetSize + ")");
            	}
            	if (SigDiscoveryIntersectionRadioButton != null) {
            		
            	}            
            }
        });
        sigDiscoveryDatasetCombo.setSelectedIndex(0);
        panel.add(sigDiscoveryDatasetCombo);
        
        sigDiscoveryRankingCombo = new JComboBox();
        sigDiscoveryRankingCombo.setModel(rankingModel);
        sigDiscoveryRankingCombo.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
            	JComboBox selectedChoice = (JComboBox) e.getSource();
            	paParams.setSignature_rankFile((String)selectedChoice.getSelectedItem());
            }
        });
        sigDiscoveryRankingCombo.setSelectedIndex(0);
        panel.add(sigDiscoveryRankingCombo);
        
        signatureDiscoveryRankTestTextField = new JFormattedTextField();
        signatureDiscoveryRankTestTextField.addPropertyChangeListener("value", new PostAnalysisInputPanel.FormattedTextFieldAction());
        
        String[] filterItems = PostAnalysisParameters.filterItems;
        signatureDiscoveryRankTestCombo = new JComboBox();
        signatureDiscoveryRankTestCombo.addItem(filterItems[PostAnalysisParameters.MANN_WHIT]);
        signatureDiscoveryRankTestCombo.addItem(filterItems[PostAnalysisParameters.HYPERGEOM]);
        signatureDiscoveryRankTestCombo.addActionListener( new ActionListener() {
            String[] filterItems = PostAnalysisParameters.filterItems;
            public void actionPerformed( ActionEvent e ) {
                JComboBox selectedChoice = (JComboBox) e.getSource();
                if (filterItems[PostAnalysisParameters.MANN_WHIT].equals( selectedChoice.getSelectedItem())) {
                    paParams.setSignature_rankTest(PostAnalysisParameters.MANN_WHIT);
                    signatureDiscoveryRankTestTextField.setValue(paParams.getSignature_Mann_Whit_Cutoff());
                } else if (filterItems[PostAnalysisParameters.HYPERGEOM].equals( selectedChoice.getSelectedItem())) {
                    paParams.setSignature_rankTest(PostAnalysisParameters.HYPERGEOM);
                    signatureDiscoveryRankTestTextField.setValue(paParams.getSignature_Hypergeom_Cutoff());
                }
            }
        });
        signatureDiscoveryRankTestCombo.setSelectedItem(filterItems[paParams.getDefault_signature_rankTest()]);
        panel.add(signatureDiscoveryRankTestCombo);
        
        JPanel cutoffLabel = new JPanel();
        cutoffLabel.add(new JLabel("Select Cutoff:"));
        panel.add(cutoffLabel);
        
        JPanel cutoffPanel = new JPanel();
        cutoffPanel.setLayout(new BoxLayout(cutoffPanel, BoxLayout.X_AXIS));

        cutoffPanel.add(signatureDiscoveryRankTestCombo);
        cutoffPanel.add(signatureDiscoveryRankTestTextField);

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
        
        DataSet dataset = map.getDataset(paParams.getSignature_dataSet());
    	int universeSize = 0;
    	if (dataset != null) {
    		universeSize = dataset.getDatasetGenes().size();
    	}
    	
    	SigDiscoveryGMTRadioButton = new JRadioButton("GMT (" + universeSize + ")");
        SigDiscoveryGMTRadioButton.setActionCommand("GMT");
        SigDiscoveryGMTRadioButton.addActionListener(new ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectSigDiscoveryUniverseActionPerformed(evt);
            }
        });        
        SigDiscoveryGMTRadioButton.setSelected(true);
    	int expressionSetSize = map.getDataset(paParams.getSignature_dataSet()).getExpressionSets().getNumGenes();
        SigDiscoveryExpressionSetRadioButton = new JRadioButton("Expression Set (" + expressionSetSize + ")");
        SigDiscoveryExpressionSetRadioButton.setActionCommand("Expression Set");
        SigDiscoveryExpressionSetRadioButton.addActionListener(new ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectSigDiscoveryUniverseActionPerformed(evt);
            }
        });    
    	HashSet<Integer> intersection = map.getDataset(paParams.getSignature_dataSet()).getDatasetGenes();
    	intersection.retainAll(map.getDataset(paParams.getSignature_dataSet()).getExpressionSets().getGeneIds());
        SigDiscoveryIntersectionRadioButton = new JRadioButton("Intersection (" + intersection.size() + ")");
        SigDiscoveryIntersectionRadioButton.setActionCommand("Intersection");
        SigDiscoveryIntersectionRadioButton.addActionListener(new ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectSigDiscoveryUniverseActionPerformed(evt);
            }
        });    
        SigDiscoveryUserDefinedRadioButton = new JRadioButton("User Defined");
        SigDiscoveryUserDefinedRadioButton.setActionCommand("User Defined");
        SigDiscoveryUserDefinedRadioButton.addActionListener(new ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectSigDiscoveryUniverseActionPerformed(evt);
            }
        });  
        
        ButtonGroup universeSelectionOptions = new ButtonGroup();
        universeSelectionOptions.add(SigDiscoveryGMTRadioButton);
        universeSelectionOptions.add(SigDiscoveryExpressionSetRadioButton);
        universeSelectionOptions.add(SigDiscoveryIntersectionRadioButton);
        universeSelectionOptions.add(SigDiscoveryUserDefinedRadioButton);

        c.gridx = 0;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.gridy = 0;
        gridbag.setConstraints(SigDiscoveryGMTRadioButton, c);
        radioButtonsPanel.add(SigDiscoveryGMTRadioButton);
        
        c.gridy = 1;
        gridbag.setConstraints(SigDiscoveryExpressionSetRadioButton, c);
        radioButtonsPanel.add(SigDiscoveryExpressionSetRadioButton);

        c.gridy = 2;
        gridbag.setConstraints(SigDiscoveryIntersectionRadioButton, c);
        radioButtonsPanel.add(SigDiscoveryIntersectionRadioButton);
        
        c.gridy = 3;
        c.gridwidth = 2;
        gridbag.setConstraints(SigDiscoveryUserDefinedRadioButton, c);
        radioButtonsPanel.add(SigDiscoveryUserDefinedRadioButton);
        
        c.gridx = 2;
        sigDiscoveryUniverseSelectionTextField = new JFormattedTextField();
        sigDiscoveryUniverseSelectionTextField.addPropertyChangeListener("value", new PostAnalysisInputPanel.FormattedTextFieldAction());
        sigDiscoveryUniverseSelectionTextField.setText(Integer.toString(EnrichmentGenes.size()));
        sigDiscoveryUniverseSelectionTextField.setEditable(false);
        gridbag.setConstraints(sigDiscoveryUniverseSelectionTextField, c);
        radioButtonsPanel.add(sigDiscoveryUniverseSelectionTextField);
        
        universeSelectionPanel.getContentPane().add(radioButtonsPanel, BorderLayout.WEST);
               
        panel.add(universeSelectionPanel);
        
        collapsiblePanel.getContentPane().add(panel, BorderLayout.NORTH);
        return collapsiblePanel;
    }

    

    /**
     * @return Array with arrows UP, DOWN, LEFT and RIGHT
     */
    private ImageIcon[] createArrowIcons () {
        ImageIcon[] iconArrow = new ImageIcon[4];
        URL iconURL;
        //                         Oliver at 26/06/2009:  relative path works for me,
        //                         maybe need to change to org/baderlab/csplugins/enrichmentmap/resources/arrow_collapsed.gif
        iconURL = Thread.currentThread().getContextClassLoader().getResource("arrow_up.gif");
        if (iconURL != null) {
            iconArrow[UP] = new ImageIcon(iconURL);
        }
        iconURL = Thread.currentThread().getContextClassLoader().getResource("arrow_down.gif");
        if (iconURL != null) {
            iconArrow[DOWN] = new ImageIcon(iconURL);
        }
        iconURL = Thread.currentThread().getContextClassLoader().getResource("arrow_left.gif");
        if (iconURL != null) {
            iconArrow[LEFT] = new ImageIcon(iconURL);
        }
        iconURL = Thread.currentThread().getContextClassLoader().getResource("arrow_right.gif");
        if (iconURL != null) {
            iconArrow[RIGHT] = new ImageIcon(iconURL);
        }
        return iconArrow;
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
                else if(checkFile(value).equals(Color.RED)){
                    JOptionPane.showMessageDialog(Cytoscape.getDesktop(),message,"File name change entered is not a valid file name",JOptionPane.WARNING_MESSAGE);
                    knownSignatureGMTFileNameTextField.setForeground(checkFile(value));
                }
                else {
                    paParams.setSignatureGMTFileName(value);
                    paParams.setSignatureSetNames(new DefaultListModel());
                    paParams.setSelectedSignatureSetNames(new DefaultListModel());
                }
            } else if (source == signatureDiscoveryGMTFileNameTextField) {
                String value = signatureDiscoveryGMTFileNameTextField.getText();
                if(value.equalsIgnoreCase("") )
                    paParams.setSignatureGMTFileName(value);
                else if(signatureDiscoveryGMTFileNameTextField.getText().equalsIgnoreCase((String)e.getOldValue())){
                    //do nothing
                }
                else if(checkFile(value).equals(Color.RED)){
                    JOptionPane.showMessageDialog(Cytoscape.getDesktop(),message,"File name change entered is not a valid file name",JOptionPane.WARNING_MESSAGE);
                    signatureDiscoveryGMTFileNameTextField.setForeground(checkFile(value));
                }
                else {
                    paParams.setSignatureGMTFileName(value);
//                    paParams.setSignatureSetNames(new DefaultListModel());
//                    paParams.setSelectedSignatureSetNames(new DefaultListModel());
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
            else if (source == signatureDiscoveryRankTestTextField) {
            	String value = signatureDiscoveryRankTestTextField.getText();
            	paParams.setSignature_Mann_Whit_Cutoff(Double.parseDouble(value));
            }
            else if (source == knownSigUniverseSelectionTextField) {
            	String value = knownSigUniverseSelectionTextField.getText();
            	paParams.setUniverseSize(Integer.parseInt(value));
            }
            else if (source == sigDiscoveryUniverseSelectionTextField) {
            	String value = sigDiscoveryUniverseSelectionTextField.getText();
            	paParams.setUniverseSize(Integer.parseInt(value));
            }
            else if (source == filterTextField) {
                Number value = (Number) filterTextField.getValue();
                //if the filter type is percent then make sure the number entered is between 0 and 100
                if(paParams.getSignature_filterMetric() == paParams.HYPERGEOM){
                    if ((value != null) && (value.doubleValue() >= 0.0) && (value.intValue() <= 1.0)) {
                        paParams.setSignature_Hypergeom_Cutoff(value.doubleValue());
                    } else {
                        source.setValue(paParams.getDefault_signature_Hypergeom_Cutoff());
                        message += "The filter cutoff must be greater than or equal 0.0 and less than or equal to 1.0";
                        invalid = true;
                    }
                } else if(paParams.getSignature_filterMetric() == paParams.MANN_WHIT){
                    if ((value != null) && (value.doubleValue() >= 0.0) && (value.intValue() <= 1.0)) {
                        paParams.setSignature_Mann_Whit_Cutoff(value.doubleValue());
                    } else {
                        source.setValue(paParams.getDefault_signature_Mann_Whit_Cutoff());
                        message += "The filter cutoff must be greater than or equal 0.0 and less than or equal to 1.0";
                        invalid = true;
                    }
                } else if(paParams.getSignature_filterMetric() == paParams.PERCENT){
                    if ((value != null) && (value.intValue() >= 0) && (value.intValue() <= 100)) {
                        paParams.setFilterValue(value.intValue());
                    } else {
                        source.setValue(paParams.getFilterValue());
                        message += "The filter cutoff must be greater than or equal 0 and less than or equal to 100.";
                        invalid = true;
                    }
                }
                //if the filter type is NUMBER then it can be any number, zero or greater.
                else if(paParams.getSignature_filterMetric() == paParams.NUMBER){
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
                JOptionPane.showMessageDialog(Cytoscape.getDesktop(), message, "Parameter out of bounds", JOptionPane.WARNING_MESSAGE);
            }
        }
    }


    /**
     * Utility method that creates a panel for buttons at the bottom of the Enrichment Map Panel
     *
     * @return a flow layout panel containing the build map and cancel buttons
     */
    private JPanel createBottomPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout());

        JButton closeButton = new JButton();
        JButton importButton = new JButton();

        JButton resetButton = new JButton ("Reset");
        resetButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resetPanel();
            }
        });

        closeButton.setText("Close");
        closeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        importButton.setText("Run");
        importButton.addActionListener(new BuildPostAnalysisActionListener(this));
        importButton.setEnabled(true);

        panel.add(resetButton);
        panel.add(closeButton);
        panel.add(importButton);

        return panel;
    }

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {
        CytoscapeDesktop desktop = Cytoscape.getDesktop();

        CytoPanel cytoPanel = desktop.getCytoPanel(SwingConstants.WEST);

        //set the input window to null in the instance
        EnrichmentMapManager.getInstance().setAnalysisWindow(null);

        cytoPanel.remove(this);

    }

    public void close() {
        CytoscapeDesktop desktop = Cytoscape.getDesktop();

        CytoPanel cytoPanel = desktop.getCytoPanel(SwingConstants.WEST);

        //set the input window to null in the instance
        EnrichmentMapManager.getInstance().setAnalysisWindow(null);

        cytoPanel.remove(this);
    }


    public Color checkFile(String filename){
        //check to see if the files exist and are readable.
        //if the file is unreadable change the color of the font to red
        //otherwise the font should be black.
        if(filename != null){
            File tempfile = new File(filename);
            if(!tempfile.canRead())
                return Color.RED;
        }
        return Color.BLACK;
    }


    private void selectAnalysisTypeActionPerformed(ActionEvent evt){
        String analysisType = evt.getActionCommand();

        if(analysisType.equalsIgnoreCase("Signature Discovery")) {
        	paParams = sigDiscoveryPaParams;
            paParams.setUniverseSize(EnrichmentGenes.size());
            paParams.setSignatureHub(true);
        	userInputPanel.getContentPane().remove(optionsPanel);
        	optionsPanel = getSignatureDiscoveryOptionsPanel();
        	userInputPanel.getContentPane().add(optionsPanel);
        	optionsPanel.revalidate();
//            signature_genesets.remove(signaturePanel);
//            signaturePanel.remove(dataset1);
//            signaturePanel.remove(dataset2);
//            signaturePanel.revalidate();
            //before clearing the panel find out which panels where collapsed so we maintain its current state.
            boolean datasets_collapsed = signature_genesets.isCollapsed();
            signature_genesets.getContentPane().add(signaturePanel, BorderLayout.NORTH);
            signature_genesets.setCollapsed(datasets_collapsed);
            signature_genesets.revalidate();
        } else {
        	paParams = knownSigPaParams;
            paParams.setUniverseSize(EnrichmentGenes.size());
            paParams.setSignatureHub(false);
        	paParams.setFilter(false);
        	userInputPanel.getContentPane().remove(optionsPanel);
        	optionsPanel = getKnownSignatureOptionsPanel();
        	userInputPanel.getContentPane().add(optionsPanel);
        	optionsPanel.revalidate();
        }
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
    
    private void selectSigDiscoveryUniverseActionPerformed(ActionEvent evt){
        String analysisType = evt.getActionCommand();
    	int size = 0;
        if (analysisType.equalsIgnoreCase("GMT")) {
        	size = map.getDataset(paParams.getSignature_dataSet()).getDatasetGenes().size();
            sigDiscoveryUniverseSelectionTextField.setText(Integer.toString(size));
        	sigDiscoveryUniverseSelectionTextField.setEditable(false);
        } else if (analysisType.equalsIgnoreCase("Expression Set")) {
        	size = map.getDataset(paParams.getSignature_dataSet()).getExpressionSets().getNumGenes();
            sigDiscoveryUniverseSelectionTextField.setText(Integer.toString(size));
        	sigDiscoveryUniverseSelectionTextField.setEditable(false);
        } else if (analysisType.equalsIgnoreCase("Intersection")) {
        	sigDiscoveryUniverseSelectionTextField.setEditable(false);
        } else if (analysisType.equalsIgnoreCase("User Defined")) {
        	sigDiscoveryUniverseSelectionTextField.setEditable(true);
        }
        paParams.setUniverseSize(size);
    }
        
    private JPanel getKnownSignatureOptionsPanel() {
    	if (knownSignaturePanel == null) {
    		knownSignaturePanel = this.createKnownSignatureOptionsPanel();
    	}
		return knownSignaturePanel;
	}
    
    /**
     * Event Handler for selectSignatureGMTFileButton.<p>
     * Opens a file browser dialog to select the SignatureGMTFile.
     * 
     * @param evt
     */
    private void selectSignatureGMTFileButtonActionPerformed(
            java.awt.event.ActionEvent evt) {

        // Create FileFilter
        CyFileFilter filter = new CyFileFilter();

        // Add accepted File Extensions
        filter.addExtension("gmt");
        filter.setDescription("All GMT files");

        // Get the file name
        File file = FileUtil.getFile("Import SigGMT File", FileUtil.LOAD,
                new CyFileFilter[] { filter });
        if(file != null) {
        	if (evt.getActionCommand().equalsIgnoreCase("Known Signature")) {
	            knownSignatureGMTFileNameTextField.setForeground(checkFile(file.getAbsolutePath()));
	            knownSignatureGMTFileNameTextField.setText(file.getAbsolutePath());
	            knownSignatureGMTFileNameTextField.setValue(file.getAbsolutePath());
	            paParams.setSignatureGMTFileName(file.getAbsolutePath());
	            //Load in the GMT file
	            JTaskConfig config = new JTaskConfig();
	            config.displayCancelButton(true);
	            config.displayCloseButton(true);
	            config.displayStatus(true);
	            
	            String errors = paParams.checkGMTfiles();
	            if (errors.equalsIgnoreCase("")) {
	            	LoadSignatureGMTFilesTask load_GMTs = new LoadSignatureGMTFilesTask(map, paParams, this);
	                /*boolean success =*/ 
	            	TaskManager.executeTask(load_GMTs, config);
	            } else {
	                JOptionPane.showMessageDialog(Cytoscape.getDesktop(),errors,"Invalid Input",JOptionPane.WARNING_MESSAGE);
	            }
	            paParams.setSelectedSignatureSetNames(paParams.getSignatureSetNames());
	            knownSignatureGMTFileNameTextField.setToolTipText(file.getAbsolutePath());
        	} else {
	            signatureDiscoveryGMTFileNameTextField.setForeground(checkFile(file.getAbsolutePath()));
	            signatureDiscoveryGMTFileNameTextField.setText(file.getAbsolutePath());
	            signatureDiscoveryGMTFileNameTextField.setValue(file.getAbsolutePath());
	            paParams.setSignatureGMTFileName(file.getAbsolutePath());
	            signatureDiscoveryGMTFileNameTextField.setToolTipText(file.getAbsolutePath());
        	}
        }
    }
    
    /**
     * Event handler for "Load Genesets" Button.
     * 
     * @param evt
     */
    private void loadGenesetsButtonActionPerformed(java.awt.event.ActionEvent evt) {
        //Load in the GMT file
        JTaskConfig config = new JTaskConfig();
        config.displayCancelButton(true);
        config.displayCloseButton(true);
        config.displayStatus(true);
        
        String errors = paParams.checkGMTfiles();
        if (errors.equalsIgnoreCase("")) {
        	LoadSignatureGMTFilesTask load_GMTs = new LoadSignatureGMTFilesTask(map, this.paParams, this);
            /*boolean success =*/ 
        	TaskManager.executeTask(load_GMTs, config);
        } else {
            JOptionPane.showMessageDialog(Cytoscape.getDesktop(),errors,"Invalid Input",JOptionPane.WARNING_MESSAGE);
        }

    }
    
    /**
     * Clear the current panel and clear the paParams associated with this panel
     */
    private void resetPanel(){
    	
        this.paParams.setSignatureGenesets(new SetOfGeneSets());

        if (knownSignaturePanel != null) {
            //Gene-Sets Panel
            this.knownSignatureGMTFileNameTextField.setText("");
            this.knownSignatureGMTFileNameTextField.setValue("");
            this.knownSignatureGMTFileNameTextField.setToolTipText(null);
            
	        String[] filterItems = PostAnalysisParameters.filterItems;
	        this.knownSignatureRankTestCombo.setSelectedItem(filterItems[paParams.getDefault_signature_rankTest()]);
        }
        
        if (signatureDiscoveryPanel != null) {
        	
        	// Reset the text field
            this.signatureDiscoveryGMTFileNameTextField.setText("");
            this.signatureDiscoveryGMTFileNameTextField.setValue("");
            this.signatureDiscoveryGMTFileNameTextField.setToolTipText(null);
                        	       
	        // Reset the List fields:
            this.paParams.getSignatureSetNames().clear();
	        this.avail_sig_sets.clear();
	        this.avail_sig_sets_field.clearSelection();
	        this.setAvSigCount(0);
	        
	        this.paParams.getSelectedSignatureSetNames().clear();
	        this.selected_sig_sets.clear();
	        this.selected_sig_sets_field.clearSelection();
	        this.setSelSigCount(0);
	
	        // Reset the filter field
	        filter.setSelected(true);
	        paParams.setFilter(true);
	        String[] filterItems = PostAnalysisParameters.filterItems;
	        this.filterTypeCombo.setSelectedItem(filterItems[paParams.getDefault_signature_filterMetric()]);
	        this.signatureDiscoveryRankTestCombo.setSelectedItem(filterItems[paParams.getDefault_signature_rankTest()]);
        }

    }

    /**
     * Refresh content of PostAnalysisInputPanel when Network is changed or Panel is re-opened.
     * 
     * @param current_params
     */
    public void updateContents(EnrichmentMapParameters current_params){
		EnrichmentMap currentMap = EnrichmentMapManager.getInstance().getMap(Cytoscape.getCurrentNetwork().getIdentifier());
		if(currentMap != null){
			
			this.map = currentMap;
			
			this.knownSigPaParams = this.map.getPaParams();
			
	        // Gene-Set Files:
	        this.knownSignatureGMTFileNameTextField.setText(this.knownSigPaParams.getSignatureGMTFileName());
	        this.knownSignatureGMTFileNameTextField.setValue(this.knownSigPaParams.getSignatureGMTFileName());
	        
	        this.sigDiscoveryPaParams = new PostAnalysisParameters();
	        this.sigDiscoveryPaParams.copyFrom(this.knownSigPaParams);
	        
	        this.paParams = this.knownSigPaParams;
	        this.paParams.setSignatureHub(false);
	        
	        String[] datasetArray = this.map.getDatasets().keySet().toArray(new String[datasetMap.size()]);
	        Arrays.sort(datasetArray);
	        this.datasetModel.removeAllElements();
	        for (String dataset : datasetArray) {
	        	datasetModel.addElement(dataset);
	        }
	        
	        String[] rankingArray = this.map.getAllRanks().keySet().toArray(new String[rankingMap.size()]);
	        Arrays.sort(rankingArray);
	        rankingModel.removeAllElements();
	        for (String ranking : rankingArray) {
	        	rankingModel.addElement(ranking);
	        }
	        
	        HashMap<String, GeneSet> EnrichmentGenesets = map.getAllGenesets();
	        EnrichmentGenes = new HashSet<Integer>();
	        for (Iterator<String> i = map.getAllGenesets().keySet().iterator(); i.hasNext(); ) {
	            String setName = i.next();
	            EnrichmentGenes.addAll(EnrichmentGenesets.get(setName).getGenes());
	        }
	        
	        DataSet dataset = map.getDataset(paParams.getSignature_dataSet());
	    	int universeSize = 0;
	    	if (dataset != null) {
	    		universeSize = dataset.getDatasetGenes().size();
	    	}
	        paParams.setUniverseSize(universeSize);
	        knownSigUniverseSelectionTextField.setText(Integer.toString(universeSize));
		}
    }
    
    /* ************************************************* *
     *                 getters and setters               *
     * ************************************************* */

    public PostAnalysisParameters getPaParams() {
        return paParams;
    }

    public void setPaParams(PostAnalysisParameters paParams) {
        this.paParams = paParams;
    }
    
    /**
     * Create the available signature counter label
     * @param null
     * @return JLabel avSigCount
     */
    public JLabel createAvSigCountLabel() {
		if (this.avail_sig_sets_counter_label == null) {
			this.avail_sig_sets_counter_label = new JLabel("(0)");
		}
		return this.avail_sig_sets_counter_label;
    }
	
	/**
	 * Set available signature gene set count to specified value
	 * @param int avSigCount
	 * @return null
	 */
	public void setAvSigCount(int avSigCount) {
		if (signatureDiscovery.isSelected()) {
			this.avail_sig_sets_count = avSigCount;
			this.avail_sig_sets_counter_label.setText("(" + Integer.toString(avSigCount) + ")");
		}
	}
	
	/**
	 * Get available signature gene set count
	 * @param null
	 * @return int avSigCount
	 */
	public int getAvSigCount() {
		return this.avail_sig_sets_count;
	}
	
    /**
     * Create the selected signature counter label
     * @param null
     * @return JLabel selSigCount
     */
    public JLabel createSelSigCountLabel() {
		if (this.selected_sig_sets_counter_label == null) {
			this.selected_sig_sets_counter_label = new JLabel("(0)");
		}
		return this.selected_sig_sets_counter_label;
    }
	
	/**
	 * Set selected signature gene set count to the 
	 * specified value
	 * @param int sigCount
	 * @return null
	 */
	public void setSelSigCount(int num) {
		this.sel_sig_sets_count = num;
		this.selected_sig_sets_counter_label.setText("(" + Integer.toString(num) + ")");
	}
	
	/**
	 * Get selected signature gene set count
	 * @param null
	 * @return int selSigCount
	 */
	public int getSelSigCount() {
		return this.sel_sig_sets_count;
	}

    /**
     * @author revilo
     * <p>
     * Date   Jul 16, 2009<br>
     * Time   5:50:59 PM<br>
     *
     */
    private class PaPanelActionListener implements ActionListener {
    	protected PostAnalysisInputPanel paPanel = null;
    	protected int universeSize = 0;
    	
    	public PaPanelActionListener(PostAnalysisInputPanel paPanel) {
    		this.paPanel = paPanel;
    		this.universeSize = this.paPanel.paParams.getUniverseSize();
    	}
    	
		public void actionPerformed(ActionEvent arg0) {
			
		}
    }
    
}
