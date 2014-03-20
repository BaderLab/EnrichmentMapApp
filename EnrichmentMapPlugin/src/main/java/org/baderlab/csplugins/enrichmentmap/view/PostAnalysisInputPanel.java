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

import javax.swing.*;

import org.baderlab.csplugins.enrichmentmap.EnrichmentMapManager;
import org.baderlab.csplugins.enrichmentmap.EnrichmentMapParameters;
import org.baderlab.csplugins.enrichmentmap.EnrichmentMapUtils;
import org.baderlab.csplugins.enrichmentmap.PostAnalysisParameters;
import org.baderlab.csplugins.enrichmentmap.actions.BuildPostAnalysisActionListener;
import org.baderlab.csplugins.enrichmentmap.actions.LoadSignatureSetsActionListener;
import org.baderlab.csplugins.enrichmentmap.actions.ShowAboutPanelAction;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.model.JMultiLineToolTip;
import org.baderlab.csplugins.enrichmentmap.task.LoadSignatureGMTFilesTask;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.io.util.StreamUtil;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.session.CySessionManager;
import org.cytoscape.util.swing.FileChooserFilter;
import org.cytoscape.util.swing.FileUtil;
import org.cytoscape.util.swing.OpenBrowser;
import org.cytoscape.work.swing.DialogTaskManager;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * Created by
 * @author revilo
 * <p>
 * Date July 9, 2009
 * 
 * Based on: EnrichmentMapInputPanel.java (302) by risserlin
 */

public class PostAnalysisInputPanel extends JPanel implements CytoPanelComponent {
    /**
     * 
     */
    private static final long serialVersionUID = 5472169142720323583L;
    
    private CyApplicationManager cyApplicationManager;
    private CySwingApplication application;
	private OpenBrowser browser;
	private FileUtil fileUtil;
	private CyServiceRegistrar registrar;
	private CySessionManager sessionManager;
	private StreamUtil streamUtil;
	private CyNetworkManager networkManager;
	private DialogTaskManager dialog;
	private CyEventHelper eventHelper;
    
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

    
    private PostAnalysisParameters paParams;

    // Analysis Type related components
    private JRadioButton signatureHub;

    
    //Genesets file related components
    private JFormattedTextField GMTFileNameTextField;
    private JFormattedTextField signatureGMTFileNameTextField;

    private JLabel avail_sig_sets_counter_label;
    private int avail_sig_sets_count = 0;
    private JLabel selected_sig_sets_counter_label;
    private int sel_sig_sets_count = 0;
    private JList avail_sig_sets_field;
    private JList selected_sig_sets_field;
    private DefaultListModel avail_sig_sets;
    private DefaultListModel selected_sig_sets;


    
    //Parameters related components
    private JComboBox sigCutoffCombo;
    private JFormattedTextField sigCutoffTextField;
    
    private int defaultColumns = 15;

    //
    //Texts:
    //
    
    public static String gmt_instruction = "Please select the Gene Set file (.gmt)...";
    public static String siggmt_instruction = "Please select the Signature Gene Set file (.gmt)...";
    //tool tips
    private static String gmtTip = "File specifying gene sets.\n" + "Format: geneset name <tab> description <tab> gene ...";
    
    public PostAnalysisInputPanel(CyApplicationManager cyApplicationManager, CySwingApplication application, 
    		OpenBrowser browser,FileUtil fileUtil, CySessionManager sessionManager,
    		StreamUtil streamUtil,CyServiceRegistrar registrar,
    		CyNetworkManager networkManager,
    		DialogTaskManager dialog,CyEventHelper eventHelper) {
    	
    		this.cyApplicationManager = cyApplicationManager;
    		this.application = application;
        this.browser = browser;
        this.fileUtil = fileUtil;
        this.registrar = registrar;
        this.sessionManager = sessionManager;
        this.streamUtil = streamUtil;
        this.networkManager = networkManager;
        this.dialog = dialog;
        this.eventHelper = eventHelper;
    		
        //initialize paParams
        this.paParams = new PostAnalysisParameters(sessionManager,streamUtil,cyApplicationManager);
        
        decFormat = new DecimalFormat();
        decFormat.setParseIntegerOnly(false);

        setLayout(new BorderLayout());

        //create the three main panels: scope, advanced options, and bottom
        JPanel AnalysisTypePanel = createAnalysisTypePanel();

        //Put the options panel into a scroll pain

        CollapsiblePanel OptionsPanel = createOptionsPanel();
        OptionsPanel.setCollapsed(false);
        JScrollPane scroll = new JScrollPane(OptionsPanel);
        //scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        JPanel bottomPanel = createBottomPanel();

        //Since the advanced options panel is being added to the center of this border layout
        //it will stretch it's height to fit the main panel.  To prevent this we create an
        //additional border layout panel and add advanced options to it's north compartment
        JPanel advancedOptionsContainer = new JPanel(new BorderLayout());
        advancedOptionsContainer.add(scroll, BorderLayout.CENTER);

        //Add all the vertically aligned components to the main panel
        add(AnalysisTypePanel,BorderLayout.NORTH);
        add(advancedOptionsContainer,BorderLayout.CENTER);
        add(bottomPanel,BorderLayout.SOUTH);

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
                browser.openURL(EnrichmentMapUtils.userManualUrl);
            }
        });

        JButton about = new JButton("About");
        Map<String, String> serviceProperties = new HashMap<String, String>();
        serviceProperties.put("inMenuBar", "true");
		   serviceProperties.put("preferredMenu", "Apps.EnrichmentMap");
        about.addActionListener(new ShowAboutPanelAction(serviceProperties , cyApplicationManager, null, application, browser));
		   
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

        signatureHub = new JRadioButton("Signature Hubs", paParams.isSignatureHub());

        signatureHub.setActionCommand("Signature Hubs");

        signatureHub.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectAnalysisTypeActionPerformed(evt);
            }
        });
        signatureHub.setSelected(true);
        
        ButtonGroup analysisOptions = new ButtonGroup();
        analysisOptions.add(signatureHub);


        c.gridx = 0;
        c.gridwidth = 3;
        c.gridy = 0;
        gridbag.setConstraints(signatureHub, c);
        panel.add(signatureHub);

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
    private CollapsiblePanel createOptionsPanel() {
        CollapsiblePanel collapsiblePanel = new CollapsiblePanel("User Input");

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        //Gene set file panel
        CollapsiblePanel GMTPanel = createGMTPanel();
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
        CollapsiblePanel ParametersPanel = createParametersPanel();
        ParametersPanel.setCollapsed(false);
        
        panel.add(GMTPanel);
        panel.add(signature_genesets);
        panel.add(ParametersPanel);
        
        collapsiblePanel.getContentPane().add(panel, BorderLayout.NORTH);
        return collapsiblePanel;
    }

    /**
     * @return CollapsiblePanel for choosing and loading GMT and SignatureGMT Geneset-Files 
     */
    private CollapsiblePanel createGMTPanel() {
        CollapsiblePanel collapsiblePanel = new CollapsiblePanel("Gene-Sets");

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        //add GMT file
        JLabel GMTLabel = new JLabel("GMT:"){
            /**
             * 
             */
            private static final long serialVersionUID = 5799024396588991328L;

            public JToolTip createToolTip() {
                return new JMultiLineToolTip();
            }
        };
        GMTLabel.setToolTipText(gmtTip);
        JButton selectGMTFileButton = new JButton();
        GMTFileNameTextField = new JFormattedTextField() ;
        GMTFileNameTextField.setColumns(defaultColumns);


        //components needed for the directory load
        GMTFileNameTextField.setFont(new java.awt.Font("Dialog",1,10));
        //GMTFileNameTextField.setText(gmt_instruction);
        GMTFileNameTextField.addPropertyChangeListener("value",new PostAnalysisInputPanel.FormattedTextFieldAction());

        GMTFileNameTextField.setText( paParams.getGMTFileName() );
        GMTFileNameTextField.setValue( paParams.getGMTFileName() );
        if (! (GMTFileNameTextField.getText().equals("") ) ) {
            GMTFileNameTextField.setToolTipText(GMTFileNameTextField.getText());
        }

        selectGMTFileButton.setText("...");
        selectGMTFileButton.setMargin(new Insets(0,0,0,0));
        selectGMTFileButton
        .addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectGMTFileButtonActionPerformed(evt);
            }
        });

        JPanel GMTPanel = new JPanel();
        GMTPanel.setLayout(new BorderLayout());

        GMTPanel.add(GMTLabel,BorderLayout.WEST);
        GMTPanel.add( GMTFileNameTextField, BorderLayout.CENTER);
        GMTPanel.add( selectGMTFileButton, BorderLayout.EAST);

        //add the components to the panel
        panel.add(GMTPanel);

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
        signatureGMTFileNameTextField = new JFormattedTextField() ;
        signatureGMTFileNameTextField.setColumns(defaultColumns);


        //components needed for the directory load
        signatureGMTFileNameTextField.setFont(new java.awt.Font("Dialog",1,10));
        //GMTFileNameTextField.setText(gmt_instruction);
        signatureGMTFileNameTextField.addPropertyChangeListener("value",new PostAnalysisInputPanel.FormattedTextFieldAction());


        selectSigGMTFileButton.setText("...");
        selectSigGMTFileButton.setMargin(new Insets(0,0,0,0));
        selectSigGMTFileButton
        .addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectSignatureGMTFileButtonActionPerformed(evt);
            }
        });

        JPanel SigGMTPanel = new JPanel();
        SigGMTPanel.setLayout(new BorderLayout());

        SigGMTPanel.add( SigGMTLabel,BorderLayout.WEST);
        SigGMTPanel.add( signatureGMTFileNameTextField, BorderLayout.CENTER);
        SigGMTPanel.add( selectSigGMTFileButton, BorderLayout.EAST);
        //add the components to the panel
        panel.add(SigGMTPanel);

        panel.add(createFilterPanel());

        //TODO: Maybe move loading SigGMT to File-selection Event
        //add load button
        JButton loadButton = new JButton();
        loadButton.setText("Load Gene-Sets");
        loadButton.addActionListener(new LoadSignatureSetsActionListener(this, cyApplicationManager, dialog,streamUtil));
        loadButton.setPreferredSize(new Dimension(100,10));
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
        CollapsiblePanel collapsiblePanel = new CollapsiblePanel("Filters");
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(2,1));
        //create radio button
        ButtonGroup filters;

        //radio button for filter or no-filter.  Defaults to no-filter
        filter = new JRadioButton("Filter By");
        filter.setActionCommand("filter");
        filter.setSelected(true);
        nofilter = new JRadioButton("No filter");
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
                if ( filterItems[PostAnalysisParameters.PERCENT].equals( selectedChoice.getSelectedItem() ) ) {
                    paParams.setSignature_filterMetric(PostAnalysisParameters.PERCENT);

                } else if ( filterItems[PostAnalysisParameters.NUMBER].equals( selectedChoice.getSelectedItem() ) ) {
                    paParams.setSignature_filterMetric(PostAnalysisParameters.NUMBER);

                }else if ( filterItems[PostAnalysisParameters.SPECIFIC].equals( selectedChoice.getSelectedItem() ) ) {
                    paParams.setSignature_filterMetric(PostAnalysisParameters.SPECIFIC);

                }
            }
        });

        filterTextField = new JFormattedTextField() ;
        filterTextField.setColumns(4);
        filterTextField.setValue(paParams.getFilterValue());
        filterTextField.addPropertyChangeListener("value", new PostAnalysisInputPanel.FormattedTextFieldAction());

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
    private CollapsiblePanel createParametersPanel() {
        String[] sigCutoffItems = PostAnalysisParameters.sigCutoffItems;
        CollapsiblePanel collapsiblePanel = new CollapsiblePanel("Parameters");
        
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        JPanel cutoffLabel = new JPanel();
        cutoffLabel.add(new JLabel("Select Cutoff:"));
        panel.add(cutoffLabel);
        
        JPanel cutoffPanel = new JPanel();
        cutoffPanel.setLayout(new BoxLayout(cutoffPanel, BoxLayout.X_AXIS));
        sigCutoffCombo = new JComboBox();
        sigCutoffCombo.addItem(sigCutoffItems[PostAnalysisParameters.HYPERGEOM]);
        sigCutoffCombo.addItem(sigCutoffItems[PostAnalysisParameters.ABS_NUMBER]);
//        sigCutoffCombo.addItem(sigCutoffItems[PostAnalysisParameters.JACCARD]);
//        sigCutoffCombo.addItem(sigCutoffItems[PostAnalysisParameters.OVERLAP]);
        sigCutoffCombo.addItem(sigCutoffItems[PostAnalysisParameters.DIR_OVERLAP]);
        sigCutoffCombo.setSelectedItem(sigCutoffItems[paParams.getDefault_signature_CutoffMetric()]);


        //JFormattedTextField
        sigCutoffTextField = new JFormattedTextField(decFormat);
        sigCutoffTextField.setColumns(3);
        if (paParams.getDefault_signature_CutoffMetric() == PostAnalysisParameters.HYPERGEOM)
            sigCutoffTextField.setValue(paParams.getSignature_Hypergeom_Cutoff());
        else if (paParams.getDefault_signature_CutoffMetric() == PostAnalysisParameters.ABS_NUMBER)
            sigCutoffTextField.setValue(paParams.getSignature_absNumber_Cutoff());
        else if (paParams.getDefault_signature_CutoffMetric() == PostAnalysisParameters.JACCARD)
            sigCutoffTextField.setValue(paParams.getSignature_Jaccard_Cutoff());
        else if (paParams.getDefault_signature_CutoffMetric() == PostAnalysisParameters.OVERLAP)
            sigCutoffTextField.setValue(paParams.getSignature_Overlap_Cutoff());
        else if (paParams.getDefault_signature_CutoffMetric() == PostAnalysisParameters.DIR_OVERLAP)
            sigCutoffTextField.setValue(paParams.getSignature_DirOverlap_Cutoff());
        else {
            //Handle Unsupported Default_signature_CutoffMetric Error
            String message = "This Cutoff metric is not supported.";
            JOptionPane.showMessageDialog(application.getJFrame(), message, "Parameter out of bounds", JOptionPane.WARNING_MESSAGE);
        }

        
        //Add Action Listeners
        sigCutoffCombo.addActionListener( new ActionListener() { 
            String[] sigCutoffItems = PostAnalysisParameters.sigCutoffItems;
            public void actionPerformed( ActionEvent e ) 
            { 
              JComboBox selectedChoice = (JComboBox) e.getSource(); 
              if ( sigCutoffItems[PostAnalysisParameters.HYPERGEOM].equals( selectedChoice.getSelectedItem() ) ) {
                  paParams.setSignature_CutoffMetric(PostAnalysisParameters.HYPERGEOM);
                  sigCutoffTextField.setValue(paParams.getSignature_Hypergeom_Cutoff());
              } else if ( sigCutoffItems[PostAnalysisParameters.ABS_NUMBER].equals( selectedChoice.getSelectedItem() ) ) {
                  paParams.setSignature_CutoffMetric(PostAnalysisParameters.ABS_NUMBER);
                  sigCutoffTextField.setValue(paParams.getSignature_absNumber_Cutoff());
              } else if ( sigCutoffItems[PostAnalysisParameters.JACCARD].equals( selectedChoice.getSelectedItem() ) ) {
                  paParams.setSignature_CutoffMetric(PostAnalysisParameters.JACCARD);
                  sigCutoffTextField.setValue(paParams.getSignature_Jaccard_Cutoff());
              } else if ( sigCutoffItems[PostAnalysisParameters.OVERLAP].equals( selectedChoice.getSelectedItem() ) ) {
                  paParams.setSignature_CutoffMetric(PostAnalysisParameters.OVERLAP);
                  sigCutoffTextField.setValue(paParams.getSignature_Overlap_Cutoff());
              } else if ( sigCutoffItems[PostAnalysisParameters.DIR_OVERLAP].equals( selectedChoice.getSelectedItem() ) ) {
                  paParams.setSignature_CutoffMetric(PostAnalysisParameters.DIR_OVERLAP);
                  sigCutoffTextField.setValue(paParams.getSignature_DirOverlap_Cutoff());
              }
                 
            } 
          } ); 
        sigCutoffTextField.addPropertyChangeListener("value", new PostAnalysisInputPanel.FormattedTextFieldAction());

        cutoffPanel.add(sigCutoffCombo);
        cutoffPanel.add(sigCutoffTextField);

        panel.add(cutoffPanel);
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
     * Handles setting for the text field parameters that are numbers.
     * Makes sure that the numbers make sense.
     */
    private class FormattedTextFieldAction implements PropertyChangeListener {
        public void propertyChange(PropertyChangeEvent e) {
            JFormattedTextField source = (JFormattedTextField) e.getSource();

            String message = "The value you have entered is invalid.\n";
            boolean invalid = false;

            if (source == GMTFileNameTextField) {
                String value = GMTFileNameTextField.getText();
                if(value.equalsIgnoreCase("") )
                    paParams.setGMTFileName(value);
                else if(GMTFileNameTextField.getText().equalsIgnoreCase((String)e.getOldValue())){
                    //do nothing
                }
                else if(checkFile(value).equals(Color.RED)){
                    JOptionPane.showMessageDialog(application.getJFrame(),message,"File name change entered is not a valid file name",JOptionPane.WARNING_MESSAGE);
                    GMTFileNameTextField.setForeground(checkFile(value));
                }
                else
                    paParams.setGMTFileName(value);
            } 
            else if (source == signatureGMTFileNameTextField) {
                String value = signatureGMTFileNameTextField.getText();
                if(value.equalsIgnoreCase("") )
                    paParams.setSignatureGMTFileName(value);
                else if(signatureGMTFileNameTextField.getText().equalsIgnoreCase((String)e.getOldValue())){
                    //do nothing
                }
                else if(checkFile(value).equals(Color.RED)){
                    JOptionPane.showMessageDialog(application.getJFrame(),message,"File name change entered is not a valid file name",JOptionPane.WARNING_MESSAGE);
                    signatureGMTFileNameTextField.setForeground(checkFile(value));
                }
                else
                    paParams.setSignatureGMTFileName(value);
            } 
            else if (source == sigCutoffTextField) {
                Number value = (Number) sigCutoffTextField.getValue();
                if (paParams.getSignature_CutoffMetric() == PostAnalysisParameters.HYPERGEOM) {
                    if ((value != null) && (value.doubleValue() >= 0.0) && (value.doubleValue() <= 1.0)) {
                        paParams.setSignature_Hypergeom_Cutoff(value.doubleValue());
                    } else {
                        source.setValue(paParams.getSignature_Hypergeom_Cutoff());
                        message += "The Hypergeometric-pValue cutoff must be greater or equal than 0.0 and less than or equal to 1.0.";
                        invalid = true;
                    }
                }
                else if (paParams.getSignature_CutoffMetric() == PostAnalysisParameters.ABS_NUMBER) {
                    if ((value != null) && (value.intValue() >= 0) ) {
                        paParams.setSignature_absNumber_Cutoff(value.intValue());
                    } else {
                        source.setValue(paParams.getSignature_absNumber_Cutoff());
                        message += "The \"Number of common genes\" cutoff must be a non-negative Integer (0 or larger).";
                        invalid = true;
                    }
                }
                else if (paParams.getSignature_CutoffMetric() == PostAnalysisParameters.JACCARD) {
                    if ((value != null) && (value.doubleValue() > 0.0) && (value.doubleValue() <= 1.0)) {
                        paParams.setSignature_Jaccard_Cutoff(value.doubleValue());
                    } else {
                        source.setValue(paParams.getSignature_Jaccard_Cutoff());
                        message += "The Jaccard Coefficient cutoff must be greater than 0.0 and less than or equal to 1.0.";
                        invalid = true;
                    }
                }
                else if (paParams.getSignature_CutoffMetric() == PostAnalysisParameters.OVERLAP) {
                    if ((value != null) && (value.doubleValue() > 0.0) && (value.doubleValue() <= 1.0)) {
                        paParams.setSignature_Overlap_Cutoff(value.doubleValue());
                    } else {
                        source.setValue(paParams.getSignature_Overlap_Cutoff());
                        message += "The Overlap Coefficient cutoff must be greater than 0.0 and less than or equal to 1.0.";
                        invalid = true;
                    }
                }
                else if (paParams.getSignature_CutoffMetric() == PostAnalysisParameters.DIR_OVERLAP) {
                    if ((value != null) && (value.doubleValue() > 0.0) && (value.doubleValue() <= 1.0)) {
                        paParams.setSignature_DirOverlap_Cutoff(value.doubleValue());
                    } else {
                        source.setValue(paParams.getSignature_DirOverlap_Cutoff());
                        message += "The Overlap Coefficient cutoff must be greater than 0.0 and less than or equal to 1.0.";
                        invalid = true;
                    }
                }
                else {
                    message = "This Cutoff metric is not supported.";
                    invalid = true;
                }
            }
            else if (source == filterTextField) {
                Number value = (Number) filterTextField.getValue();
                //if the filter type is percent then make sure the number entered is between 0 and 100
                if(paParams.getSignature_filterMetric() == paParams.PERCENT){
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
                JOptionPane.showMessageDialog(application.getJFrame(), message, "Parameter out of bounds", JOptionPane.WARNING_MESSAGE);
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
        importButton.addActionListener(new BuildPostAnalysisActionListener(this, sessionManager, streamUtil, networkManager, cyApplicationManager, dialog,eventHelper));
        importButton.setEnabled(true);

        panel.add(resetButton);
        panel.add(closeButton);
        panel.add(importButton);

        return panel;
    }
    
    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {
    		this.registrar.unregisterService(this, CytoPanelComponent.class);
        

    }

    public void close() {
    		this.registrar.unregisterService(this, CytoPanelComponent.class);
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

        if(analysisType.equalsIgnoreCase("Signature Hubs"))
            paParams.setSignatureHub(true);
        else
            paParams.setSignatureHub(false);

        //before clearing the panel find out which panels where collapsed so we maintain its current state.
        boolean datasets_collapsed = signature_genesets.isCollapsed();

        signature_genesets.remove(signaturePanel);
        signaturePanel.remove(dataset1);
        signaturePanel.remove(dataset2);

        signaturePanel.revalidate();
        signature_genesets.getContentPane().add(signaturePanel, BorderLayout.NORTH);
        signature_genesets.setCollapsed(datasets_collapsed);
        signature_genesets.revalidate();

    }
    
        
    /**
     * Event Handler for selectGMTFileButton.<p>
     * Opens a file browser dialog to select the GMTFile.
     * 
     * @param evt
     */
    private void selectGMTFileButtonActionPerformed(
            java.awt.event.ActionEvent evt) {
    	// Create FileFilter
        FileChooserFilter filter = new FileChooserFilter("All GMT Files","gmt" );          
        
        //the set of filter (required by the file util method
        ArrayList<FileChooserFilter> all_filters = new ArrayList<FileChooserFilter>();
        all_filters.add(filter);
        // Get the file name
        File file = fileUtil.getFile(EnrichmentMapUtils.getWindowInstance(this),"Import GMT File", FileUtil.LOAD,all_filters  );
                
        if(file != null) {
            GMTFileNameTextField.setForeground(checkFile(file.getAbsolutePath()));
            GMTFileNameTextField.setText(file.getAbsolutePath());
            GMTFileNameTextField.setValue(file.getAbsolutePath());
            paParams.setGMTFileName(file.getAbsolutePath());
            GMTFileNameTextField.setToolTipText(file.getAbsolutePath());
        }
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
        FileChooserFilter filter = new FileChooserFilter("All GMT Files","gmt" );          
        
        //the set of filter (required by the file util method
        ArrayList<FileChooserFilter> all_filters = new ArrayList<FileChooserFilter>();
        all_filters.add(filter);
        // Get the file name
        File file = fileUtil.getFile(EnrichmentMapUtils.getWindowInstance(this),"Import Signature GMT File", FileUtil.LOAD,all_filters  );
        
        if(file != null) {
            signatureGMTFileNameTextField.setForeground(checkFile(file.getAbsolutePath()));
            signatureGMTFileNameTextField.setText(file.getAbsolutePath());
            signatureGMTFileNameTextField.setValue(file.getAbsolutePath());
            paParams.setSignatureGMTFileName(file.getAbsolutePath());
            signatureGMTFileNameTextField.setToolTipText(file.getAbsolutePath());
        }
    }
    
    /**
     * Clear the current panel and clear the paParams associated with this panel
     */
    //TODO:create action for the resetPanel 
    private void resetPanel(){
    	
    	//TODO:create action for the resetPanel 
        this.paParams = new PostAnalysisParameters(EnrichmentMapManager.getInstance().getMap(cyApplicationManager.getCurrentNetwork().getSUID()));

        //Post Analysis Type:
        signatureHub.setSelected(true);
        
        //Gene-Sets Panel
        this.GMTFileNameTextField.setText("");
        this.GMTFileNameTextField.setValue("");
        this.GMTFileNameTextField.setToolTipText(null);
        this.signatureGMTFileNameTextField.setText("");
        this.signatureGMTFileNameTextField.setValue("");
        this.signatureGMTFileNameTextField.setToolTipText(null);

        // reset the List fields:
        this.avail_sig_sets = this.paParams.getSignatureSetNames();
        this.avail_sig_sets_field.setModel(avail_sig_sets);
        this.avail_sig_sets_field.clearSelection();
        this.setAvSigCount(0);
        
        this.selected_sig_sets = this.paParams.getSelectedSignatureSetNames();
        this.selected_sig_sets_field.setModel(selected_sig_sets);
        this.selected_sig_sets_field.clearSelection();
        this.setSelSigCount(0);

        //Parameters Panel:
        // select default metric in ComboBox
        paParams.setSignature_CutoffMetric(paParams.getDefault_signature_CutoffMetric());
        sigCutoffCombo.setSelectedIndex(paParams.getSignature_CutoffMetric());
        // reset Text Field
        switch (paParams.getSignature_CutoffMetric()) {
        case PostAnalysisParameters.HYPERGEOM:
            sigCutoffTextField.setValue(paParams.getSignature_Hypergeom_Cutoff());
            break;
        case PostAnalysisParameters.ABS_NUMBER:
            sigCutoffTextField.setValue(paParams.getSignature_absNumber_Cutoff());
            break;
        case PostAnalysisParameters.JACCARD:
            sigCutoffTextField.setValue(paParams.getSignature_Jaccard_Cutoff());
            break;
        case PostAnalysisParameters.OVERLAP:
            sigCutoffTextField.setValue(paParams.getSignature_Overlap_Cutoff());
            break;
        default:
            //Handle Unsupported Default_signature_CutoffMetric Error
            String message = "This Cutoff metric is not supported.";
            JOptionPane.showMessageDialog(application.getJFrame(), message, "Parameter out of bounds", JOptionPane.WARNING_MESSAGE);
            break;
        }

        filter.setSelected(true);
        nofilter.setSelected(false);
        paParams.setFilter(false);
        this.filterTextField.setValue(paParams.getFilterValue());

        paParams.setSignature_filterMetric(paParams.getDefault_signature_filterMetric());
        this.filterTypeCombo.setSelectedItem(paParams.getSignature_filterMetric());

    }

    /**
     * Refresh content of PostAnalysisInputPanel when Network is changed or Panel is re-opend.
     * 
     * @param current_params
     */
    public void updateContents(EnrichmentMapParameters current_params){
               
        // create instance of PostAnalysisParameters an initialize with EnrichmentMapParameters
        paParams = EnrichmentMapManager.getInstance().getMap(cyApplicationManager.getCurrentNetwork().getSUID()).getPaParams();

    	
    		this.paParams = EnrichmentMapManager.getInstance().getMap(cyApplicationManager.getCurrentNetwork().getSUID()).getPaParams();
   		
    		
       
    			// Gene-Set Files:
    			GMTFileNameTextField.setText(this.paParams.getGMTFileName());
    			GMTFileNameTextField.setValue(this.paParams.getGMTFileName());
    			signatureGMTFileNameTextField.setText(this.paParams.getSignatureGMTFileName());
    			signatureGMTFileNameTextField.setValue(this.paParams.getSignatureGMTFileName());
        
    			// Gene-Set Selection:
    			this.avail_sig_sets    = this.paParams.getSignatureSetNames();
    			this.avail_sig_sets_field.setModel(this.avail_sig_sets);
    			this.selected_sig_sets = this.paParams.getSelectedSignatureSetNames();
    			this.selected_sig_sets_field.setModel(this.selected_sig_sets);
        
        //Parameters:
        this.sigCutoffCombo.setSelectedIndex(this.paParams.getSignature_CutoffMetric());
        
        switch (this.paParams.getSignature_CutoffMetric()) {
        case PostAnalysisParameters.HYPERGEOM:
            sigCutoffTextField.setValue(this.paParams.getSignature_Hypergeom_Cutoff());
            break;
        case PostAnalysisParameters.ABS_NUMBER:
            sigCutoffTextField.setValue(this.paParams.getSignature_absNumber_Cutoff());
            break;
        case PostAnalysisParameters.JACCARD:
            sigCutoffTextField.setValue(this.paParams.getSignature_Jaccard_Cutoff());
            break;
        case PostAnalysisParameters.OVERLAP:
            sigCutoffTextField.setValue(this.paParams.getSignature_Overlap_Cutoff());
            break;

        default:
            //Handle Unsupported Default_signature_CutoffMetric Error
            String message = "This Cutoff metric is not supported.";
            JOptionPane.showMessageDialog(application.getJFrame(), message, "Parameter out of bounds", JOptionPane.WARNING_MESSAGE);
            break;
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

	public Component getComponent() {
		
		return this;
	}

	public CytoPanelName getCytoPanelName() {
		
		return CytoPanelName.WEST;
	}

	public Icon getIcon() {
		URL EMIconURL = this.getClass().getResource("enrichmentmap_logo_notext_small.png");
        ImageIcon EMIcon = null;
        if (EMIconURL != null) {
            EMIcon = new ImageIcon(EMIconURL);
        }
		return EMIcon;
	}

	public String getTitle() {
		
		return "Post Analysis Input Panel";
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
		this.avail_sig_sets_count = 0;
		this.avail_sig_sets_counter_label.setText("(" + Integer.toString(avSigCount) + ")");
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
    	
    	public PaPanelActionListener(PostAnalysisInputPanel paPanel) {
    		this.paPanel = paPanel;
    	}
    	
		public void actionPerformed(ActionEvent arg0) {
			
		}
    }
    
}
