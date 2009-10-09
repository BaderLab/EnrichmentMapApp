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

package org.baderlab.csplugins.enrichmentmap;

import cytoscape.view.CytoscapeDesktop;
import cytoscape.view.cytopanels.CytoPanel;
import cytoscape.Cytoscape;
import cytoscape.task.Task;
import cytoscape.task.TaskMonitor;
import cytoscape.task.ui.JTaskConfig;
import cytoscape.task.util.TaskManager;
import cytoscape.util.CyFileFilter;
import cytoscape.util.FileUtil;
import cytoscape.util.OpenBrowser;

import javax.swing.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.io.File;
import java.net.URL;

/**
 * Created by
 * @author revilo
 * @date July 9, 2009
 * 
 * Based on: EnrichmentMapInputPanel.java (302) by risserlin
 */
public class PostAnalysisInputPanel extends JPanel {
    final static int RIGHT = 0, DOWN = 1, UP = 2, LEFT = 3; // image States
    
    CollapsiblePanel Parameters;
    CollapsiblePanel signature_genesets;

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

    private JFormattedTextField GCTFileName1TextField;
    private JFormattedTextField GCTFileName2TextField;

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
    //tool tips
    private static String gmtTip = "File specifying gene sets.\n" + "Format: geneset name <tab> description <tab> gene ...";


    public PostAnalysisInputPanel() {

        decFormat = new DecimalFormat();
        decFormat.setParseIntegerOnly(false);

        setLayout(new BorderLayout());

        CytoscapeDesktop desktop = Cytoscape.getDesktop();
        CytoPanel cytoPanel = desktop.getCytoPanel(SwingConstants.EAST);
        //cytoPanel.addCytoPanelListener();

        //get the current enrichment map parameters
        EnrichmentMapParameters emParams = EnrichmentMapManager.getInstance().getParameters(Cytoscape.getCurrentNetwork().getIdentifier());
        if (emParams == null){
            emParams = new EnrichmentMapParameters();
        }
        
        // create instance of PostAnalysisParameters an initialize with EnrichmentMapParameters
        paParams = emParams.getPaParams();

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
                OpenBrowser.openURL(Enrichment_Map_Plugin.userManualUrl);
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
     * Creates a collapsible panel that holds collapsible user inputs
     *
     * @return collapsiblePanel
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
        signaturePanel.setPreferredSize(new Dimension(280, 300));
        signaturePanel.setAlignmentX((float) 0.0); //LEFT
        

//        //TODO: Make SearchBox functional
//        // search Box:
//        JFormattedTextField searchBox = new JFormattedTextField();
//        searchBox.setName("Search");
//        signaturePanel.add(searchBox);
        
        avail_sig_sets = paParams.getSignatureSetNames(); 
        selected_sig_sets = paParams.getSelectedSignatureSetNames();

        //List of all Signature Genesets 
        JPanel availableLabel = new JPanel();
        availableLabel.add(new JLabel("available Signature-Genesets:"));
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
        selectedLabel.add( new JLabel("selected Signature-Genesets:") );
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
 
        //ActionListeners for (Un-)SelectButtons
        selectButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                int[] selected = avail_sig_sets_field.getSelectedIndices();
                for (int i = selected.length; i > 0 ; i--  ) {
                    selected_sig_sets.addElement( avail_sig_sets.get(selected[i-1]) );
                    avail_sig_sets.remove(selected[i-1]);
                }
            }
        });        
        unselectButton.addActionListener(new java.awt.event.ActionListener() {
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

    private CollapsiblePanel createGMTPanel() {
        CollapsiblePanel collapsiblePanel = new CollapsiblePanel("Gene-Sets");

        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(0, 1));

        //add GMT file
        JLabel GMTLabel = new JLabel("GMT:"){
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


        //TODO: Maybe move loading SigGMT to File-selection Event
        //add load button
        JButton loadButton = new JButton();
        loadButton.setText("Load Gene-Sets");
        loadButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadGenesetsButtonActionPerformed(evt);
            }
        });
        loadButton.setPreferredSize(new Dimension(100,10));
        panel.add(loadButton);
        
        
        collapsiblePanel.getContentPane().add(panel, BorderLayout.NORTH);
        return collapsiblePanel;

    }

    private CollapsiblePanel createParametersPanel() {
        CollapsiblePanel collapsiblePanel = new CollapsiblePanel("Parameters");
        
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        JPanel cutoffLabel = new JPanel();
        cutoffLabel.add(new JLabel("Select Cutoff:"));
        panel.add(cutoffLabel);
        
        JPanel cutoffPanel = new JPanel();
        cutoffPanel.setLayout(new BoxLayout(cutoffPanel, BoxLayout.X_AXIS));
        sigCutoffCombo = new JComboBox();
        sigCutoffCombo.addItem("Hypergeometric Test");
        sigCutoffCombo.addItem("Number of common genes");
        sigCutoffCombo.addItem("Jaccard Coefficient");
        sigCutoffCombo.addItem("Overlap Coefficient");
        sigCutoffCombo.setSelectedIndex(paParams.getDefault_signature_CutoffMetric());


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
        else {
            //Handle Unsupported Default_signature_CutoffMetric Error
            String message = "This Cutoff metric is not supported.";
            JOptionPane.showMessageDialog(Cytoscape.getDesktop(), message, "Parameter out of bounds", JOptionPane.WARNING_MESSAGE);
        }

        
        //Add Action Listeners
        sigCutoffCombo.addActionListener( new ActionListener() { 
            public void actionPerformed( ActionEvent e ) 
            { 
              JComboBox selectedChoice = (JComboBox) e.getSource(); 
              if ( "Hypergeometric Test".equals( selectedChoice.getSelectedItem() ) ) {
                  paParams.setSignature_CutoffMetric(PostAnalysisParameters.HYPERGEOM);
                  sigCutoffTextField.setValue(paParams.getSignature_Hypergeom_Cutoff());
              } else if ( "Number of common genes".equals( selectedChoice.getSelectedItem() ) ) {
                  paParams.setSignature_CutoffMetric(PostAnalysisParameters.ABS_NUMBER);
                  sigCutoffTextField.setValue(paParams.getSignature_absNumber_Cutoff());
              } else if ( "Jaccard Coefficient".equals( selectedChoice.getSelectedItem() ) ) {
                  paParams.setSignature_CutoffMetric(PostAnalysisParameters.JACCARD);
                  sigCutoffTextField.setValue(paParams.getSignature_Jaccard_Cutoff());
              } else if ( "Overlap Coefficient".equals( selectedChoice.getSelectedItem() ) ) {
                  paParams.setSignature_CutoffMetric(PostAnalysisParameters.OVERLAP);
                  sigCutoffTextField.setValue(paParams.getSignature_Overlap_Cutoff());
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
    

    private ImageIcon[] createArrowIcons () {
        ImageIcon[] iconArrow = new ImageIcon[4];
        URL iconURL;
        //                         Oliver at 26/06/2009:  relative path works for me,
        //                         maybe need to change to org/baderlab/csplugins/enrichmentmap/resources/arrow_collapsed.gif
        iconURL = Enrichment_Map_Plugin.class.getResource("resources/arrow_up.gif");
        if (iconURL != null) {
            iconArrow[UP] = new ImageIcon(iconURL);
        }
        iconURL = Enrichment_Map_Plugin.class.getResource("resources/arrow_down.gif");
        if (iconURL != null) {
            iconArrow[DOWN] = new ImageIcon(iconURL);
        }
        iconURL = Enrichment_Map_Plugin.class.getResource("resources/arrow_left.gif");
        if (iconURL != null) {
            iconArrow[LEFT] = new ImageIcon(iconURL);
        }
        iconURL = Enrichment_Map_Plugin.class.getResource("resources/arrow_right.gif");
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
                    JOptionPane.showMessageDialog(Cytoscape.getDesktop(),message,"File name change entered is not a valid file name",JOptionPane.WARNING_MESSAGE);
                    GMTFileNameTextField.setForeground(checkFile(value));
                }
                else
                    paParams.setGMTFileName(value);
            } 
            else if (source == signatureGMTFileNameTextField) {
                String value = signatureGMTFileNameTextField.getText();
                if(value.equalsIgnoreCase("") )
                    paParams.setGMTFileName(value);
                else if(signatureGMTFileNameTextField.getText().equalsIgnoreCase((String)e.getOldValue())){
                    //do nothing
                }
                else if(checkFile(value).equals(Color.RED)){
                    JOptionPane.showMessageDialog(Cytoscape.getDesktop(),message,"File name change entered is not a valid file name",JOptionPane.WARNING_MESSAGE);
                    signatureGMTFileNameTextField.setForeground(checkFile(value));
                }
                else
                    paParams.setGMTFileName(value);
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
                else {
                    message = "This Cutoff metric is not supported.";
                    invalid = true;
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
        // TODO probably unused:
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

        UpdatePanel(this.paParams);

    }
    
        
    /*Given a set of parameters, update the panel to contain the values that are
     * defined in this set of parameters
     */
    private void UpdatePanel(EnrichmentMapParameters paParams){
        //TODO: Check if needed
        //check to see if the user had already entered anything into the newly created Dataset Frame
        if(paParams.getGCTFileName1() != null){
            GCTFileName1TextField.setText(paParams.getGCTFileName1());
            GCTFileName1TextField.setToolTipText(paParams.getGCTFileName1());
        }
        if(paParams.getGCTFileName2() != null){
            GCTFileName2TextField.setText(paParams.getGCTFileName2());
            GCTFileName2TextField.setToolTipText(paParams.getGCTFileName2());
        }

        else{
            if((paParams.getEnrichmentDataset1FileName2()!=null) || (paParams.getEnrichmentDataset2FileName2()!=null)){
                JOptionPane.showMessageDialog(this,"Running Enrichment Map with Generic input " +
                "allows for only one enrichment results file.\n  The second file specified has been removed.");
                if(paParams.getEnrichmentDataset1FileName2()!=null)
                    paParams.setEnrichmentDataset1FileName2(null);

                if(paParams.getEnrichmentDataset2FileName2()!=null)
                    paParams.setEnrichmentDataset2FileName2(null);

            }
        }
    }

    private void selectGMTFileButtonActionPerformed(
            java.awt.event.ActionEvent evt) {

        //         Create FileFilter
        CyFileFilter filter = new CyFileFilter();

        // Add accepted File Extensions
        filter.addExtension("gmt");
        filter.setDescription("All GMT files");

        // Get the file name
        File file = FileUtil.getFile("Import GMT File", FileUtil.LOAD,
                new CyFileFilter[] { filter });
        if(file != null) {
            GMTFileNameTextField.setForeground(checkFile(file.getAbsolutePath()));
            GMTFileNameTextField.setText(file.getAbsolutePath());
            paParams.setGMTFileName(file.getAbsolutePath());
            GMTFileNameTextField.setToolTipText(file.getAbsolutePath());
        }
    }
    
    private void selectSignatureGMTFileButtonActionPerformed(
            java.awt.event.ActionEvent evt) {

        //         Create FileFilter
        CyFileFilter filter = new CyFileFilter();

        // Add accepted File Extensions
        filter.addExtension("gmt");
        filter.setDescription("All GMT files");

        // Get the file name
        File file = FileUtil.getFile("Import SigGMT File", FileUtil.LOAD,
                new CyFileFilter[] { filter });
        if(file != null) {
            signatureGMTFileNameTextField.setForeground(checkFile(file.getAbsolutePath()));
            signatureGMTFileNameTextField.setText(file.getAbsolutePath());
            paParams.setSignatureGMTFileName(file.getAbsolutePath());
            signatureGMTFileNameTextField.setToolTipText(file.getAbsolutePath());
        }
    }
    
    private void loadGenesetsButtonActionPerformed(java.awt.event.ActionEvent evt) {
        //Load in the GMT file
        JTaskConfig config = new JTaskConfig();
        config.displayCancelButton(true);
        config.displayCloseButton(true);
        config.displayStatus(true);
        
        LoadGmtFilesTask load_GMTs = new LoadGmtFilesTask(this.paParams);
        boolean success = TaskManager.executeTask(load_GMTs, config);

    }
    
    //Clear the current panel and clear the paParams associated with this panel
    private void resetPanel(){
        this.paParams = new PostAnalysisParameters();
        
        //Post Analysis Type:
        signatureHub.setSelected(true);
        
        //Gene-Sets Panel
        this.GMTFileNameTextField.setText("");
        this.GMTFileNameTextField.setToolTipText(null);
        this.signatureGMTFileNameTextField.setText("");
        this.signatureGMTFileNameTextField.setToolTipText(null);

        // reset the List fields:
        this.avail_sig_sets = this.paParams.getSignatureSetNames();
        this.avail_sig_sets_field.setModel(avail_sig_sets);
        this.avail_sig_sets_field.clearSelection();
        
        this.selected_sig_sets = this.paParams.getSelectedSignatureSetNames();
        this.selected_sig_sets_field.setModel(selected_sig_sets);
        this.selected_sig_sets_field.clearSelection();
        
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
            JOptionPane.showMessageDialog(Cytoscape.getDesktop(), message, "Parameter out of bounds", JOptionPane.WARNING_MESSAGE);
            break;
        }

    }

    public void updateContents(EnrichmentMapParameters current_params){
        //TODO: modify for PostAnalysis
        this.paParams = current_params.getPaParams();
        
        // Gene-Set Files:
        GMTFileNameTextField.setText(this.paParams.getGMTFileName());
        signatureGMTFileNameTextField.setText(this.paParams.getSignatureGMTFileName());
        
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
            JOptionPane.showMessageDialog(Cytoscape.getDesktop(), message, "Parameter out of bounds", JOptionPane.WARNING_MESSAGE);
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
    
    
    
    
    /**
     * @author revilo
     * @date   Jul 16, 2009
     * @time   5:50:59 PM
     *
     */
    private class LoadGmtFilesTask implements Task {
        private PostAnalysisParameters paParams = null;
        private TaskMonitor taskMonitor = null;
        private boolean interrupted = false;
        private boolean success = false;
        
        /**
         * constructor w/ TaskMonitor
         * @param paParams
         * @param taskMonitor
         */
        public LoadGmtFilesTask( PostAnalysisParameters paParams, TaskMonitor taskMonitor ){
            this( paParams );
            this.taskMonitor = taskMonitor;
        }
        
        /**
         * constructor w/o TaskMonitor
         * @param paParams
         */
        public LoadGmtFilesTask( PostAnalysisParameters paParams ){
            this.paParams = paParams;
        }

        /* (non-Javadoc)
         * @see cytoscape.task.Task#getTitle()
         */
        public String getTitle() {
            return new String("Loading Geneset Files...");
        }

        /* (non-Javadoc)
         * @see cytoscape.task.Task#halt()
         */
        public void halt() {
            this.interrupted = true;

        }

        /* (non-Javadoc)
         * @see cytoscape.task.Task#run()
         */
        public void run() {
            //now a Cytoscape Task (LoadSignatureGenesetsTask)
            try {
                try{
                    //Load the GSEA geneset file
                    GMTFileReaderTask gmtFile_1 = new GMTFileReaderTask(paParams, taskMonitor, 1);
                    gmtFile_1.run();
    
                    //Load the Disease Signature geneset file
                    GMTFileReaderTask gmtFile_2 = new GMTFileReaderTask(paParams, taskMonitor, 2);
                    gmtFile_2.run();
    
                } catch (OutOfMemoryError e) {
                    taskMonitor.setException(e,"Out of Memory. Please increase memory allotment for Cytoscape.");
                    return;
                }   catch(Exception e){
                    taskMonitor.setException(e,"unable to load GMT files");
                    return;
                }
                
                //Sort the Genesets:
                DefaultListModel signatureSetNames = paParams.getSignatureSetNames();
                Object[] setNamesArray = paParams.getSignatureGenesets().keySet().toArray();
                Arrays.sort( setNamesArray );
                
                for (int i = 0; i < setNamesArray.length; i++) {
                    if (interrupted)
                        throw new InterruptedException();
                    signatureSetNames.addElement(setNamesArray[i] );
                }
            
            } catch (InterruptedException e) {
                taskMonitor.setException(e, "loading of GMT files cancelled");
            }

        }

        /* (non-Javadoc)
         * @see cytoscape.task.Task#setTaskMonitor(cytoscape.task.TaskMonitor)
         */
        public void setTaskMonitor(TaskMonitor taskMonitor) {
            if (this.taskMonitor != null) {
                throw new IllegalStateException("Task Monitor is already set.");
            }
            this.taskMonitor = taskMonitor;
        }

    }
}
