/**
 **                       EnrichmentMap Cytoscape Plugin
 **
 ** Copyright (c) 2008-2009 Bader Lab, Donnelly Centre for Cellular and Biomolecular
 ** Research, University of Toronto
 **
 ** Contact: http://www.baderlab.org
 **
 ** Code written by: Ruth Isserlin
 ** Authors: Daniele Merico, Ruth Isserlin, Oliver Stueker, Gary D. Bader
 **
 ** This library is free software; you can redistribute it and/or modify it
 ** under the terms of the GNU Lesser General Public License as published
 ** by the Free Software Foundation; either version 2.1 of the License, or
 ** (at your option) any later version.
 **
 ** This library is distributed in the hope that it will be useful, but
 ** WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 ** MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 ** documentation provided hereunder is on an "as is" basis, and
 ** University of Toronto
 ** has no obligations to provide maintenance, support, updates,
 ** enhancements or modifications.  In no event shall the
 ** University of Toronto
 ** be liable to any party for direct, indirect, special,
 ** incidental or consequential damages, including lost profits, arising
 ** out of the use of this software and its documentation, even if
 ** University of Toronto
 ** has been advised of the possibility of such damage.
 ** See the GNU Lesser General Public License for more details.
 **
 ** You should have received a copy of the GNU Lesser General Public License
 ** along with this library; if not, write to the Free Software Foundation,
 ** Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 **
 **/

// $Id$
// $LastChangedDate$
// $LastChangedRevision$
// $LastChangedBy$
// $HeadURL$

package org.baderlab.csplugins.enrichmentmap;

import cytoscape.view.CytoscapeDesktop;
import cytoscape.view.cytopanels.CytoPanel;
import cytoscape.Cytoscape;
import cytoscape.data.readers.TextFileReader;
import cytoscape.util.CyFileFilter;
import cytoscape.util.FileUtil;
import cytoscape.util.OpenBrowser;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.text.DecimalFormat;
import java.io.File;
import java.util.HashMap;

/**
 * Created by
 * User: risserlin
 * Date: Jun 16, 2009
 * Time: 11:13:12 AM
 */
public class EnrichmentMapInputPanel extends JPanel {


    CollapsiblePanel Parameters;
    CollapsiblePanel datasets;

    CollapsiblePanel dataset1;
    CollapsiblePanel dataset2;

    JPanel DatasetsPanel;

    DecimalFormat decFormat; // used in the formatted text fields

    private EnrichmentMapParameters params;

    //Genesets file related components
    private JFormattedTextField GMTFileNameTextField;

    private JFormattedTextField GCTFileName1TextField;
    private JFormattedTextField GCTFileName2TextField;

    private JFormattedTextField Dataset1FileNameTextField;
    private JFormattedTextField Dataset1FileName2TextField;

    private JFormattedTextField Dataset2FileNameTextField;
    private JFormattedTextField Dataset2FileName2TextField;

    private JFormattedTextField Dataset1RankFileTextField;
    private JFormattedTextField Dataset2RankFileTextField;

    private JFormattedTextField Dataset1Phenotype1TextField;
    private JFormattedTextField Dataset1Phenotype2TextField;
    private JFormattedTextField Dataset2Phenotype1TextField;
    private JFormattedTextField Dataset2Phenotype2TextField;

    private JFormattedTextField pvalueTextField;
    private JFormattedTextField qvalueTextField;
    private JFormattedTextField coeffecientTextField;

    private JRadioButton gsea;
    private JRadioButton generic;
    private JRadioButton overlap;
    private JRadioButton jaccard;

    private int defaultColumns = 15;

    public static String gct_instruction = "Please select the expression file (.gct), (.rpt)...";
    public static String gmt_instruction = "Please select the Gene Set file (.gmt)...";
    public static String dataset_instruction = "Please select the GSEA Result file (.txt)...";
    public static String rank_instruction = "Please select the rank file (.txt), (.rnk)...";

    //tool tips
    private static String gmtTip = "File specifying gene sets.\n" + "Format: geneset name <tab> description <tab> gene ...";
    private static String gctTip = "File with gene expression values.\n" + "Format: gene <tab> description <tab> expression value <tab> ...";
    private static String datasetTip = "File specifying enrichment results.\n";
    private static String rankTip = "File specifying ranked genes.\n" + "Format: gene <tab> score or statistic";


    public EnrichmentMapInputPanel() {

        decFormat = new DecimalFormat();
        decFormat.setParseIntegerOnly(false);

        setLayout(new BorderLayout());

        CytoscapeDesktop desktop = Cytoscape.getDesktop();
        CytoPanel cytoPanel = desktop.getCytoPanel(SwingConstants.EAST);
        //cytoPanel.addCytoPanelListener();

        //get the current enrichment map parameters
        //params = EnrichmentMapManager.getInstance().getParameters(Cytoscape.getCurrentNetwork().getIdentifier());
         params = new EnrichmentMapParameters();

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

        //Add all the vertically alligned components to the main panel
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
                                          OpenBrowser.openURL("http://www.baderlab.org/Software/EnrichmentMap");
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
           panel.setBorder(BorderFactory.createTitledBorder("Analysis Type"));

           gsea = new JRadioButton("GSEA", params.isGSEA());
           generic = new JRadioButton("Generic", !params.isGSEA());

           gsea.setActionCommand("GSEA");
           generic.setActionCommand("Generic");

           gsea.addActionListener(new java.awt.event.ActionListener() {
                               public void actionPerformed(java.awt.event.ActionEvent evt) {
                                   selectAnalysisTypeActionPerformed(evt);
                               }
           });
           generic.addActionListener(new java.awt.event.ActionListener() {
                               public void actionPerformed(java.awt.event.ActionEvent evt) {
                                   selectAnalysisTypeActionPerformed(evt);
                               }
           });
           ButtonGroup analysisOptions = new ButtonGroup();
           analysisOptions.add(gsea);
           analysisOptions.add(generic);


           c.gridx = 0;
           c.gridwidth = 3;
           c.gridy = 0;
           gridbag.setConstraints(gsea, c);
           panel.add(gsea);
           c.gridy = 1;
           gridbag.setConstraints(generic, c);
           panel.add(generic);


          JPanel topPanel = new JPanel();
          topPanel.setLayout(new BorderLayout());
          topPanel.add(buttonsPanel, BorderLayout.EAST);
          topPanel.add(panel, BorderLayout.CENTER);

           return topPanel;
       }

       /**
        * Creates a collapsable panel that holds collapsable user inputs
        *
        * @return collapsablePanel
        */
       private CollapsiblePanel createOptionsPanel() {
           CollapsiblePanel collapsiblePanel = new CollapsiblePanel("User Input");

           JPanel panel = new JPanel();
           panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

           //Gene set file panel
           CollapsiblePanel GMTPanel = createGMTPanel();
           GMTPanel.setCollapsed(false);

           //Dataset1 collapsible panel
           DatasetsPanel = new JPanel();
           DatasetsPanel.setLayout(new BoxLayout(DatasetsPanel, BoxLayout.Y_AXIS));

           datasets = new CollapsiblePanel("Datasets");
           datasets.setLayout(new BorderLayout());

           dataset1 = createDataset1Panel();
           dataset1.setCollapsed(false);

           dataset2 = createDataset2Panel();
           datasets.setCollapsed(false);

           DatasetsPanel.add(dataset1);
           DatasetsPanel.add(dataset2);

           datasets.getContentPane().add(DatasetsPanel, BorderLayout.NORTH);

           //Parameters collapsible panel
           CollapsiblePanel ParametersPanel = createParametersPanel();
           ParametersPanel.setCollapsed(false);

           panel.add(GMTPanel);
           panel.add(datasets);
           //panel.add(dataset1);
           //panel.add(dataset2);
           panel.add(ParametersPanel);

           collapsiblePanel.getContentPane().add(panel, BorderLayout.NORTH);
           return collapsiblePanel;
       }

        private CollapsiblePanel createGMTPanel() {
            CollapsiblePanel collapsiblePanel = new CollapsiblePanel("Gene Sets");

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
            GMTFileNameTextField.addPropertyChangeListener("value",new EnrichmentMapInputPanel.FormattedTextFieldAction());


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

           collapsiblePanel.getContentPane().add(panel, BorderLayout.NORTH);
           return collapsiblePanel;

        }

       private CollapsiblePanel createDataset1Panel() {
           CollapsiblePanel collapsiblePanel = new CollapsiblePanel("Dataset 1");

           JPanel panel = new JPanel();
           panel.setLayout(new GridLayout(0, 1));

           //add GCT file
           JLabel GCTLabel = new JLabel("Expression:"){
            public JToolTip createToolTip() {
                return new JMultiLineToolTip();
                }
            };
           GCTLabel.setToolTipText(gctTip);

           JButton selectGCTFileButton = new JButton();
           GCTFileName1TextField = new JFormattedTextField();
           GCTFileName1TextField.setColumns(defaultColumns);

            //components needed for the directory load
            GCTFileName1TextField.setFont(new java.awt.Font("Dialog",1,10));
            //GCTFileName1TextField.setText(gct_instruction);

            selectGCTFileButton.setText("...");
            selectGCTFileButton.setMargin(new Insets(0,0,0,0));
            selectGCTFileButton
                .addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        selectGCTFileButtonActionPerformed(evt);
                    }
                });

           JPanel GCTPanel = new JPanel();
           GCTPanel.setLayout(new BorderLayout());

           GCTPanel.add(GCTLabel,BorderLayout.WEST);
           GCTPanel.add( GCTFileName1TextField, BorderLayout.CENTER);
           GCTPanel.add( selectGCTFileButton, BorderLayout.EAST);

           //add Results1 file
           JLabel Results1Label = new JLabel("Enrichments 1:"){
            public JToolTip createToolTip() {
                return new JMultiLineToolTip();
                }
            };
           Results1Label.setToolTipText(datasetTip);
           if(!params.isGSEA())
                Results1Label.setText("Enrichments:");

           JButton selectResults1FileButton = new JButton();
           Dataset1FileNameTextField = new JFormattedTextField();
           Dataset1FileNameTextField.setColumns(defaultColumns);

            //components needed for the directory load
            Dataset1FileNameTextField.setFont(new java.awt.Font("Dialog",1,10));
            //Dataset1FileNameTextField.setText(dataset_instruction);

            selectResults1FileButton.setText("...");
           selectResults1FileButton.setMargin(new Insets(0,0,0,0));
            selectResults1FileButton
                .addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        selectDataset1FileButtonActionPerformed(evt);
                    }
                });

           JPanel Results1Panel = new JPanel();
           Results1Panel.setLayout(new BorderLayout());

           Results1Panel.add(Results1Label,BorderLayout.WEST);
           Results1Panel.add( Dataset1FileNameTextField, BorderLayout.CENTER);
           Results1Panel.add( selectResults1FileButton, BorderLayout.EAST);


            //add Results2 file
           JLabel Results2Label = new JLabel("Enrichments 2:"){
            public JToolTip createToolTip() {
                return new JMultiLineToolTip();
                }
            };
           Results2Label.setToolTipText(datasetTip);

           JButton selectResults2FileButton = new JButton();
           Dataset1FileName2TextField = new JFormattedTextField();
           Dataset1FileName2TextField.setColumns(defaultColumns);

            //components needed for the directory load
            Dataset1FileName2TextField.setFont(new java.awt.Font("Dialog",1,10));
            //Dataset1FileName2TextField.setText(dataset_instruction);

            selectResults2FileButton.setText("...");
            selectResults2FileButton.setMargin(new Insets(0,0,0,0));
            selectResults2FileButton
                .addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        selectDataset1File2ButtonActionPerformed(evt);
                    }
                });

           JPanel Results2Panel = new JPanel();
           Results2Panel.setLayout(new BorderLayout());

           Results2Panel.add(Results2Label,BorderLayout.WEST);
           Results2Panel.add( Dataset1FileName2TextField, BorderLayout.CENTER);
           Results2Panel.add( selectResults2FileButton, BorderLayout.EAST);

           //add the components to the panel
           panel.add(GCTPanel);
           panel.add(Results1Panel);
           if(params.isGSEA())
                panel.add(Results2Panel);

           collapsiblePanel.getContentPane().add(panel, BorderLayout.NORTH);
           collapsiblePanel.getContentPane().add(createAdvancedDatasetOptions(1),BorderLayout.SOUTH);
           return collapsiblePanel;

       }

    private CollapsiblePanel createDataset2Panel() {
           CollapsiblePanel collapsiblePanel = new CollapsiblePanel("Dataset 2");

           JPanel panel = new JPanel();
           panel.setLayout(new GridLayout(0, 1));

           //add GCT file
           JLabel GCTLabel = new JLabel("Expression:"){
            public JToolTip createToolTip() {
                return new JMultiLineToolTip();
                }
            };
            GCTLabel.setToolTipText(gctTip);
           JButton selectGCTFileButton = new JButton();
           GCTFileName2TextField = new JFormattedTextField();
           GCTFileName2TextField.setColumns(defaultColumns);

            //components needed for the directory load
            GCTFileName2TextField.setFont(new java.awt.Font("Dialog",1,10));

            selectGCTFileButton.setText("...");
            selectGCTFileButton.setMargin(new Insets(0,0,0,0));
            selectGCTFileButton
                .addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        selectGCTFileButton2ActionPerformed(evt);
                    }
                });

           JPanel GCTPanel = new JPanel();
           GCTPanel.setLayout(new BorderLayout());

           GCTPanel.add(GCTLabel,BorderLayout.WEST);
           GCTPanel.add( GCTFileName2TextField, BorderLayout.CENTER);
           GCTPanel.add( selectGCTFileButton, BorderLayout.EAST);

           //add Results1 file

           JLabel Results1Label = new JLabel("Enrichments 1:"){
            public JToolTip createToolTip() {
                return new JMultiLineToolTip();
                }
            };

            if(!params.isGSEA())
                Results1Label.setText("Enrichments:");
            Results1Label.setToolTipText(datasetTip);

           JButton selectResults1FileButton = new JButton();
           Dataset2FileNameTextField = new JFormattedTextField();
           Dataset2FileNameTextField.setColumns(defaultColumns);

            //components needed for the directory load
            Dataset2FileNameTextField.setFont(new java.awt.Font("Dialog",1,10));
            //Dataset2FileNameTextField.setText(dataset_instruction);

            selectResults1FileButton.setText("...");
            selectResults1FileButton.setMargin(new Insets(0,0,0,0));
            selectResults1FileButton
                .addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        selectDataset2FileButtonActionPerformed(evt);
                    }
                });

           JPanel Results1Panel = new JPanel();
           Results1Panel.setLayout(new BorderLayout());

           Results1Panel.add(Results1Label,BorderLayout.WEST);
           Results1Panel.add( Dataset2FileNameTextField, BorderLayout.CENTER);
           Results1Panel.add( selectResults1FileButton, BorderLayout.EAST);

            //add Results2 file
           JLabel Results2Label = new JLabel("Enrichments 2:"){
            public JToolTip createToolTip() {
                return new JMultiLineToolTip();
                }
            };
           Results2Label.setToolTipText(datasetTip);
           JButton selectResults2FileButton = new JButton();
           Dataset2FileName2TextField = new JFormattedTextField();
           Dataset2FileName2TextField.setColumns(defaultColumns);
            //components needed for the directory load
            Dataset2FileName2TextField.setFont(new java.awt.Font("Dialog",1,10));
            //Dataset2FileName2TextField.setText(dataset_instruction);

            selectResults2FileButton.setText("...");
            selectResults2FileButton.setMargin(new Insets(0,0,0,0));
            selectResults2FileButton
                .addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        selectDataset2File2ButtonActionPerformed(evt);
                    }
                });

           JPanel Results2Panel = new JPanel();
           Results2Panel.setLayout(new BorderLayout());

           Results2Panel.add(Results2Label,BorderLayout.WEST);
           Results2Panel.add( Dataset2FileName2TextField, BorderLayout.CENTER);
           Results2Panel.add( selectResults2FileButton, BorderLayout.EAST);

           //add the components to the panel
           panel.add(GCTPanel);
           panel.add(Results1Panel);
           if(params.isGSEA())
                panel.add(Results2Panel);

           collapsiblePanel.getContentPane().add(panel, BorderLayout.NORTH);
           collapsiblePanel.getContentPane().add(createAdvancedDatasetOptions(2),BorderLayout.SOUTH);
           return collapsiblePanel;

       }

    private CollapsiblePanel createAdvancedDatasetOptions(int dataset){
           //create a panel for advanced options
            CollapsiblePanel Advanced = new CollapsiblePanel("Advanced");

           JPanel advancedpanel = new JPanel();
           advancedpanel.setLayout(new BorderLayout());

            //add Ranks file
           JLabel RanksLabel = new JLabel("Ranks:"){
                    public JToolTip createToolTip() {
                        return new JMultiLineToolTip();
                    }
                };
           RanksLabel.setToolTipText(rankTip);
           JButton selectRanksFileButton = new JButton();

            if(dataset ==1 ){
                Dataset1RankFileTextField = new JFormattedTextField();
                Dataset1RankFileTextField.setColumns(defaultColumns);
                Dataset1RankFileTextField.setFont(new java.awt.Font("Dialog",1,10));
                //Dataset1RankFileTextField.setText(rank_instruction);
                 selectRanksFileButton.setText("...");
                selectRanksFileButton.setMargin(new Insets(0,0,0,0));
                selectRanksFileButton
                .addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        selectRank1FileButtonActionPerformed(evt);
                    }
                });
            }
            else {
               Dataset2RankFileTextField = new JFormattedTextField();
               Dataset2RankFileTextField.setColumns(defaultColumns);
               Dataset2RankFileTextField.setFont(new java.awt.Font("Dialog",1,10));
               //Dataset2RankFileTextField.setText(rank_instruction);

               selectRanksFileButton.setText("...");
               selectRanksFileButton.setMargin(new Insets(0,0,0,0));
               selectRanksFileButton
                .addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        selectRank2FileButtonActionPerformed(evt);
                    }
                });

            }


           JPanel RanksPanel = new JPanel();
           RanksPanel.setLayout(new BorderLayout());

           RanksPanel.add(RanksLabel,BorderLayout.WEST);
           if(dataset ==1)
                RanksPanel.add( Dataset1RankFileTextField, BorderLayout.CENTER);
           else
                RanksPanel.add( Dataset2RankFileTextField, BorderLayout.CENTER);
           RanksPanel.add( selectRanksFileButton, BorderLayout.EAST);

           //add Phenotypes
           JLabel PhenotypesLabel = new JLabel("Phenotypes:");
           JLabel vsLabel = new JLabel("VS.");
           if(dataset == 1){
               Dataset1Phenotype1TextField= new JFormattedTextField("UP");
               Dataset1Phenotype1TextField.setColumns(4);
               Dataset1Phenotype1TextField.addPropertyChangeListener("value",new EnrichmentMapInputPanel.FormattedTextFieldAction());

                Dataset1Phenotype2TextField= new JFormattedTextField("DOWN");
               Dataset1Phenotype2TextField.setColumns(4);
               Dataset1Phenotype2TextField.addPropertyChangeListener("value",new EnrichmentMapInputPanel.FormattedTextFieldAction());

           }
            else {
               Dataset2Phenotype1TextField= new JFormattedTextField("UP");
               Dataset2Phenotype1TextField.setColumns(4);
               Dataset2Phenotype1TextField.addPropertyChangeListener("value",new EnrichmentMapInputPanel.FormattedTextFieldAction());

               Dataset2Phenotype2TextField= new JFormattedTextField("DOWN");
               Dataset2Phenotype2TextField.setColumns(4);
               Dataset2Phenotype2TextField.addPropertyChangeListener("value",new EnrichmentMapInputPanel.FormattedTextFieldAction());

           }
           JPanel PhenotypesPanel = new JPanel();
           PhenotypesPanel.setLayout(new FlowLayout());

           PhenotypesPanel.add(PhenotypesLabel);
            if(dataset == 1){
                PhenotypesPanel.add( Dataset1Phenotype1TextField);
                PhenotypesPanel.add(vsLabel);
                PhenotypesPanel.add( Dataset1Phenotype2TextField);
            }
            else{
                PhenotypesPanel.add( Dataset2Phenotype1TextField);
                PhenotypesPanel.add(vsLabel);
                PhenotypesPanel.add( Dataset2Phenotype2TextField);
            }

           advancedpanel.add(RanksPanel,BorderLayout.NORTH);
           advancedpanel.add(PhenotypesPanel, BorderLayout.SOUTH);
           Advanced.getContentPane().add(advancedpanel,BorderLayout.NORTH);

        return Advanced;
    }

       /**
        * Creates a collapsable panel that holds parameter inputs
        *
        * @return panel containing the parameter inputs
        */
       private CollapsiblePanel createParametersPanel() {
           CollapsiblePanel collapsiblePanel = new CollapsiblePanel("Parameters");

           JPanel panel = new JPanel();
           panel.setLayout(new GridLayout(0, 1));

           //pvalue cutoff input
           JLabel pvalueCutOffLabel = new JLabel("P-value Cutoff");
           pvalueTextField = new JFormattedTextField(decFormat);
           pvalueTextField.setColumns(3);
           pvalueTextField.addPropertyChangeListener("value", new EnrichmentMapInputPanel.FormattedTextFieldAction());
           String pvalueCutOffTip = "Sets the p-value cutoff \n" +
                   "only genesets with a p-value less than \n"+
                    "the cutoff will be included.";
           pvalueTextField.setToolTipText(pvalueCutOffTip);
           pvalueTextField.setText(Double.toString(params.getPvalue()));

           JPanel pvalueCutOffPanel = new JPanel();
           pvalueCutOffPanel.setLayout(new BorderLayout());
           pvalueCutOffPanel.setToolTipText(pvalueCutOffTip);

           pvalueCutOffPanel.add(pvalueCutOffLabel, BorderLayout.WEST);
           pvalueCutOffPanel.add(pvalueTextField, BorderLayout.EAST);


            //qvalue cutoff input
           JLabel qvalueCutOffLabel = new JLabel("Q-value Cutoff");
           qvalueTextField = new JFormattedTextField(decFormat);
           qvalueTextField.setColumns(3);
           qvalueTextField.addPropertyChangeListener("value", new EnrichmentMapInputPanel.FormattedTextFieldAction());
           String qvalueCutOffTip = "Sets the FDR q-value cutoff \n" +
                   "only genesets with a FDR q-value less than \n"+
                    "the cutoff will be included.";
           qvalueTextField.setToolTipText(qvalueCutOffTip);
           qvalueTextField.setText(Double.toString(params.getQvalue()));

           JPanel qvalueCutOffPanel = new JPanel();
           qvalueCutOffPanel.setLayout(new BorderLayout());
           qvalueCutOffPanel.setToolTipText(qvalueCutOffTip);

           qvalueCutOffPanel.add(qvalueCutOffLabel, BorderLayout.WEST);
           qvalueCutOffPanel.add(qvalueTextField, BorderLayout.EAST);

           //coeffecient cutoff input

           ButtonGroup jaccardOrOverlap;

           jaccard = new JRadioButton("Jaccard Coeffecient");
           jaccard.setActionCommand("jaccard");
           jaccard.setSelected(true);
           overlap = new JRadioButton("Overlap Coeffecient");
           overlap.setActionCommand("overlap");
           if ( params.isJaccard() ) {
               jaccard.setSelected(true);
               overlap.setSelected(false);
           } else {
               jaccard.setSelected(false);
               overlap.setSelected(true);
           }
           jaccardOrOverlap = new javax.swing.ButtonGroup();
           jaccardOrOverlap.add(jaccard);
           jaccardOrOverlap.add(overlap);

           jaccard.addActionListener(new java.awt.event.ActionListener() {
                        public void actionPerformed(java.awt.event.ActionEvent evt) {
                               selectJaccardOrOverlapActionPerformed(evt);
                        }
                  });

           overlap.addActionListener(new java.awt.event.ActionListener() {
                        public void actionPerformed(java.awt.event.ActionEvent evt) {
                               selectJaccardOrOverlapActionPerformed(evt);
                        }
                  });

           //create a panel for the two buttons
           JPanel index_buttons = new JPanel();
           index_buttons.setLayout(new BorderLayout());
           index_buttons.add(jaccard, BorderLayout.NORTH);
           index_buttons.add(overlap, BorderLayout.SOUTH);

           JLabel coeffecientCutOffLabel = new JLabel("Cutoff");
           coeffecientTextField = new JFormattedTextField(decFormat);
           coeffecientTextField.setColumns(3);
           coeffecientTextField.addPropertyChangeListener("value", new EnrichmentMapInputPanel.FormattedTextFieldAction());
           String coeffecientCutOffTip = "Sets the Jaccard or Overlap coeffecient cutoff \n" +
                             "only edges with a Jaccard or Overlap coffecient less than \n"+
                              "the cutoff will be added.";
          coeffecientTextField.setToolTipText(coeffecientCutOffTip);
          coeffecientTextField.setText(Double.toString(params.getJaccardCutOff()));

          JPanel coeffecientCutOffPanel = new JPanel();
          coeffecientCutOffPanel.setLayout(new BorderLayout());
          coeffecientCutOffPanel.setToolTipText(coeffecientCutOffTip);

          coeffecientCutOffPanel.add(index_buttons,BorderLayout.WEST);
          coeffecientCutOffPanel.add(coeffecientCutOffLabel, BorderLayout.CENTER);
          coeffecientCutOffPanel.add(coeffecientTextField, BorderLayout.EAST);

           //add the components to the panel
           panel.add(pvalueCutOffPanel);
           panel.add(qvalueCutOffPanel);
           //panel.add(coeffecientCutOffPanel);

           collapsiblePanel.getContentPane().add(panel, BorderLayout.NORTH);
           collapsiblePanel.getContentPane().add(coeffecientCutOffPanel, BorderLayout.SOUTH);
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

            if (source == pvalueTextField) {
                Number value = (Number) pvalueTextField.getValue();
                if ((value != null) && (value.doubleValue() > 0.0) && (value.doubleValue() <= 1)) {
                    params.setPvalue(value.doubleValue());
                } else {
                    source.setValue(params.getPvalue());
                    message += "The pvalue cutoff must be greater than or equal 0 and less than or equal to 1.";
                    invalid = true;
                }
            } else if (source == qvalueTextField) {
                Number value = (Number) qvalueTextField.getValue();
                if ((value != null) && (value.doubleValue() >= 0.0) && (value.doubleValue() <= 100.0)) {
                    params.setQvalue(value.doubleValue());
                } else {
                    source.setValue(params.getQvalue());
                    message += "The FDR q-value cutoff must be between 0 and 100.";
                    invalid = true;
                }
            }else if (source == coeffecientTextField) {
                Number value = (Number) coeffecientTextField.getValue();
                if ((value != null) && (value.doubleValue() >= 0.0) && (value.doubleValue() <= 1.0)) {
                    params.setJaccardCutOff(value.doubleValue());
                    params.setJaccardCutOffChanged(true);
                } else {
                    source.setValue(params.getJaccardCutOff());
                    message += "The Overlap/Jaccard Coeffecient cutoff must be between 0 and 1.";
                    invalid = true;
                }
            }else if (source == GMTFileNameTextField) {
                String value = GMTFileNameTextField.getText();
                if(value.equalsIgnoreCase("") )
                    params.setGMTFileName(value);
                else if(checkFile(value).equals(Color.RED)){
                    JOptionPane.showMessageDialog(Cytoscape.getDesktop(),message,"File name change entered is not a valid file name",JOptionPane.WARNING_MESSAGE);
                    GMTFileNameTextField.setForeground(checkFile(value));
                }
               else
                    params.setGMTFileName(value);
            }else if (source == GCTFileName1TextField) {
                String value = GCTFileName1TextField.getText();
                if(value.equalsIgnoreCase("") )
                    params.setGCTFileName1(value);
                else if(checkFile(value).equals(Color.RED)){
                    JOptionPane.showMessageDialog(Cytoscape.getDesktop(),message,"File name change entered is not a valid file name",JOptionPane.WARNING_MESSAGE);
                    GCTFileName1TextField.setForeground(checkFile(value));
                }
               else
                    params.setGCTFileName1(value);
            }else if (source == GCTFileName2TextField) {
                String value = GCTFileName2TextField.getText();
                if(value.equalsIgnoreCase("") )
                    params.setGCTFileName2(value);
                else if(checkFile(value).equals(Color.RED)){
                    JOptionPane.showMessageDialog(Cytoscape.getDesktop(),message,"File name change entered is not a valid file name",JOptionPane.WARNING_MESSAGE);
                    GCTFileName2TextField.setForeground(checkFile(value));
                }
               else
                    params.setGCTFileName2(value);
            }else if (source == Dataset1FileNameTextField) {
                String value = Dataset1FileNameTextField.getText();
                if(value.equalsIgnoreCase("") )
                    params.setEnrichmentDataset1FileName1(value);
                else if(checkFile(value).equals(Color.RED)){
                    JOptionPane.showMessageDialog(Cytoscape.getDesktop(),message,"File name change entered is not a valid file name",JOptionPane.WARNING_MESSAGE);
                    Dataset1FileNameTextField.setForeground(checkFile(value));
                }
               else
                    params.setEnrichmentDataset1FileName1(value);
            }else if (source == Dataset1FileName2TextField) {
                String value = Dataset1FileName2TextField.getText();
                if(value.equalsIgnoreCase("") )
                    params.setEnrichmentDataset1FileName2(value);
                else if(checkFile(value).equals(Color.RED)){
                    JOptionPane.showMessageDialog(Cytoscape.getDesktop(),message,"File name change entered is not a valid file name",JOptionPane.WARNING_MESSAGE);
                    Dataset1FileName2TextField.setForeground(checkFile(value));
                }
               else
                    params.setEnrichmentDataset1FileName2(value);
            }else if (source == Dataset2FileNameTextField) {
                String value = Dataset2FileNameTextField.getText();
                if(value.equalsIgnoreCase("") )
                    params.setEnrichmentDataset2FileName1(value);
                else if(checkFile(value).equals(Color.RED)){
                    JOptionPane.showMessageDialog(Cytoscape.getDesktop(),message,"File name change entered is not a valid file name",JOptionPane.WARNING_MESSAGE);
                    Dataset2FileNameTextField.setForeground(checkFile(value));
                }
               else
                    params.setEnrichmentDataset2FileName1(value);
            }else if (source == Dataset2FileName2TextField) {
                String value = Dataset2FileName2TextField.getText();
                if(value.equalsIgnoreCase("") )
                    params.setEnrichmentDataset2FileName2(value);
                else if(checkFile(value).equals(Color.RED)){
                    JOptionPane.showMessageDialog(Cytoscape.getDesktop(),message,"File name change entered is not a valid file name",JOptionPane.WARNING_MESSAGE);
                    Dataset2FileName2TextField.setForeground(checkFile(value));
                }
               else
                    params.setEnrichmentDataset2FileName2(value);
            }else if (source == Dataset1Phenotype1TextField) {
                String value = Dataset1Phenotype1TextField.getText();
                params.setDataset1Phenotype1(value);
            }
            else if (source == Dataset1Phenotype2TextField) {
                String value = Dataset1Phenotype2TextField.getText();
                params.setDataset1Phenotype2(value);
            }
            else if (source == Dataset2Phenotype1TextField) {
                String value = Dataset2Phenotype1TextField.getText();
                params.setDataset2Phenotype1(value);
            }
            else if (source == Dataset2Phenotype2TextField) {
                String value = Dataset2Phenotype2TextField.getText();
                params.setDataset2Phenotype2(value);
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

        importButton.setText("Build Enrichment Map");
        importButton.addActionListener(new BuildEnrichmentMapActionListener(this));
        importButton.setEnabled(true);

        panel.add(resetButton);
        panel.add(closeButton);
        panel.add(importButton);

        return panel;
    }

      private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {
           // TODO add your handling code here:
          CytoscapeDesktop desktop = Cytoscape.getDesktop();

          CytoPanel cytoPanel = desktop.getCytoPanel(SwingConstants.WEST);
          cytoPanel.remove(this);

       }

    public void close() {
            // TODO add your handling code here:
            CytoscapeDesktop desktop = Cytoscape.getDesktop();

          CytoPanel cytoPanel = desktop.getCytoPanel(SwingConstants.WEST);
          cytoPanel.remove(this);
        }


   private void populateFieldsFromRpt(File rptFile, boolean dataset1){


        TextFileReader reader = new TextFileReader(rptFile.getAbsolutePath());
        reader.read();
        String fullText = reader.getText();

        //Create a hashmap to contain all the values in the rpt file.
        HashMap rpt = new HashMap();

        String [] lines = fullText.split("\n");

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            String[] tokens = line.split("\t");
            //there should be two values on each line of the rpt file.
            if(tokens.length == 2 )
                rpt.put(tokens[0] ,tokens[1]);
            else if (tokens.length == 3)
                rpt.put(tokens[0] + " "+ tokens[1],tokens[2]);
        }

         //set all the variables based on the parameters in the rpt file
        //parameters needed
        String timestamp = (String)rpt.get("producer_timestamp");
        String method = (String)rpt.get("producer_class");
        method = method.split("\\p{Punct}")[2];
        String out_dir = (String)rpt.get("param out");
        String data = (String)rpt.get("param res");
        String label = (String)rpt.get("param rpt_label");
        String classes = (String)rpt.get("param cls");
        String gmt = (String)rpt.get("param gmx");
        String gmt_nopath =  gmt.substring(gmt.lastIndexOf(File.separator)+1, gmt.length()-1);

        String phenotype1 = "na";
        String phenotype2 = "na";
        //phenotypes are specified after # in the parameter cls and are separated by _versus_
        //but phenotypes are only specified for classic GSEA, not PreRanked.
        if(classes != null && method.equalsIgnoreCase("Gsea")){
            String[] classes_split = classes.split("#");
            String phenotypes = classes_split[1];
            String[] phenotypes_split = phenotypes.split("_versus_");
            phenotype1 = phenotypes_split[0];
            phenotype2 = phenotypes_split[1];

            if(dataset1){
                params.setClassFile1(classes_split[0]);
                params.setDataset1Phenotype1(phenotype1);
                params.setDataset1Phenotype2(phenotype2);
            }
            else{
                params.setClassFile2(classes_split[0]);
                params.setDataset2Phenotype1(phenotype1);
                params.setDataset2Phenotype2(phenotype2);
            }
        }

       //check to see if the method is normal or pre-ranked GSEA.
        //If it is pre-ranked the data file is contained in a different field
        else if(method.equalsIgnoreCase("GseaPreranked")){
            data = (String)rpt.get("param rnk");
            phenotype1 = "na_pos";
            phenotype2 = "na_neg";

            if(dataset1){
                params.setDataset1Phenotype1(phenotype1);
                params.setDataset1Phenotype2(phenotype2);

                Dataset1Phenotype1TextField.setText(phenotype1);
                Dataset1Phenotype2TextField.setText(phenotype2);
            }
            else{
                params.setDataset2Phenotype1(phenotype1);
                params.setDataset2Phenotype2(phenotype2);

                Dataset2Phenotype1TextField.setText(phenotype1);
                Dataset2Phenotype2TextField.setText(phenotype2);
            }
        }

        else{
            JOptionPane.showMessageDialog(this,"The class field in the rpt file has been modified or doesn't specify a class file\n but the analysis is a classic GSEA not PreRanked.  ");
        }

        //check to see if the rpt file path is the same as the one specified in the
        //rpt file.
        //if it isn't then assume that the rpt file has the right file names but if the files specified in the rpt
        //don't exist then use the path for the rpt to change the file paths.

        String results1 = "";
         String results2 = "";
        String ranks = "";



        //files built directly from the rpt specification
        //try these files first
        results1 = "" + out_dir + File.separator + label + "."+ method + "." + timestamp + File.separator + "gsea_report_for_" + phenotype1 + "_" + timestamp + ".xls";
        results2 = "" + out_dir + File.separator + label + "."+ method + "." + timestamp + File.separator + "gsea_report_for_" + phenotype2 + "_" + timestamp + ".xls";
        ranks = "" + out_dir + File.separator + label + "."+ method + "." + timestamp + File.separator + "ranked_gene_list_" + phenotype1 + "_versus_" + phenotype2 +"_" + timestamp + ".xls";
        if(!((checkFile(results1) == Color.BLACK) && (checkFile(results2) == Color.BLACK) && (checkFile(ranks) == Color.BLACK))){
            if(!(rptFile.getAbsolutePath().substring(0,(rptFile.getAbsolutePath()).lastIndexOf(File.separator))).equalsIgnoreCase(out_dir)){

                    //trim the last File Separator
                    String new_dir = rptFile.getAbsolutePath().substring(0,rptFile.getAbsolutePath().lastIndexOf(File.separator));
                    results1 = new_dir + File.separator + "gsea_report_for_" + phenotype1 + "_" + timestamp + ".xls";
                    results2 = new_dir + File.separator + "gsea_report_for_" + phenotype2 + "_" + timestamp + ".xls";
                    ranks = new_dir + File.separator + "ranked_gene_list_" + phenotype1 + "_versus_" + phenotype2 +"_" + timestamp + ".xls";

                    //If after trying the directory that the rpt file is in doesn't produce valid file names, revert to what
                    //is specified in the rpt.
                    if(!((checkFile(results1) == Color.BLACK) && (checkFile(results2) == Color.BLACK) && (checkFile(ranks) == Color.BLACK))){
                        results1 = "" + out_dir + File.separator + label + "."+ method + "." + timestamp + File.separator + "gsea_report_for_" + phenotype1 + "_" + timestamp + ".xls";
                        results2 = "" + out_dir + File.separator + label + "."+ method + "." + timestamp + File.separator + "gsea_report_for_" + phenotype2 + "_" + timestamp + ".xls";
                        ranks = "" + out_dir + File.separator + label + "."+ method + "." + timestamp + File.separator + "ranked_gene_list_" + phenotype1 + "_versus_" + phenotype2 +"_" + timestamp + ".xls";
                    }
                    else{
                        out_dir = new_dir;
                    }
            }

        }

        if(dataset1){

                //check to see the file exists and can be read
                //check to see if the gmt file has already been set
                if(params.getGMTFileName() == null || params.getGMTFileName().equalsIgnoreCase("")){
                    GMTFileNameTextField.setForeground(checkFile(gmt));
                    GMTFileNameTextField.setText(gmt);
                    params.setGMTFileName(gmt);
                    GMTFileNameTextField.setToolTipText(gmt);
                }

                if(!params.getGMTFileName().equalsIgnoreCase(gmt)){
                    JOptionPane.showMessageDialog(this,"This analysis GMT file does not match the previous dataset loaded.");
                }
                else{
                    GCTFileName1TextField.setForeground(checkFile(data));
                    GCTFileName1TextField.setText(data);
                    params.setGCTFileName1(data);
                    params.setData(true);
                    GCTFileName1TextField.setToolTipText(data);

                    Dataset1RankFileTextField.setForeground(checkFile(ranks));
                    Dataset1RankFileTextField.setText(ranks);
                    params.setDataset1RankedFile(ranks);
                    Dataset1RankFileTextField.setToolTipText(ranks);

                    params.setEnrichmentDataset1FileName1(results1);
                    params.setEnrichmentDataset1FileName2(results2);

                    this.setDatasetnames(results1,results2,dataset1);
                }
        }
        else{
           if(params.getGMTFileName() == null || params.getGMTFileName().equalsIgnoreCase("")){
                GMTFileNameTextField.setForeground(checkFile(gmt));
                GMTFileNameTextField.setText(gmt);
                params.setGMTFileName(gmt);
                GMTFileNameTextField.setToolTipText(gmt);
           }
           if(!params.getGMTFileName().equalsIgnoreCase(gmt)){
                JOptionPane.showMessageDialog(this,"This analysis GMT file does not match the previous dataset loaded.");
           }
           else{
                GCTFileName2TextField.setForeground(checkFile(data));
                GCTFileName2TextField.setText(data);
                params.setGCTFileName2(data);
                params.setData2(true);
                GCTFileName2TextField.setToolTipText(data);

                Dataset2RankFileTextField.setForeground(checkFile(ranks));
                Dataset2RankFileTextField.setText(ranks);
                params.setDataset2RankedFile(ranks);
                Dataset2RankFileTextField.setToolTipText(ranks);

                params.setEnrichmentDataset2FileName1(results1);
                params.setEnrichmentDataset2FileName2(results2);
                params.setDataset2RankedFile(ranks);
                this.setDatasetnames(results1,results2,dataset1);
           }
        }
    }

    protected void setDatasetnames(String file1, String file2, boolean dataset1){

           if(dataset1){
               Dataset1FileNameTextField.setForeground(checkFile(file1));
               Dataset1FileNameTextField.setText(file1 );
               Dataset1FileNameTextField.setToolTipText(file1 );

               Dataset1FileName2TextField.setForeground(checkFile(file2));
               Dataset1FileName2TextField.setText(file2 );
               Dataset1FileName2TextField.setToolTipText(file2 );
           }
           else{
               Dataset2FileNameTextField.setForeground(checkFile(file1));
               Dataset2FileNameTextField.setText(file1 );
               Dataset2FileNameTextField.setToolTipText(file1 );

               Dataset2FileName2TextField.setForeground(checkFile(file2));
               Dataset2FileName2TextField.setText(file2 );
               Dataset2FileName2TextField.setToolTipText(file2 );
           }
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

      if(analysisType.equalsIgnoreCase("GSEA"))
          params.setGSEA(true);
      else
          params.setGSEA(false);

      //before clearing the panel find out which panels where collapsed so we maintain its current state.
      boolean datasets_collapsed = datasets.isCollapsed();
      boolean dataset1_collapsed = dataset1.isCollapsed();
      boolean dataset2_collapsed = dataset2.isCollapsed();

      datasets.remove(DatasetsPanel);
      DatasetsPanel.remove(dataset1);
      DatasetsPanel.remove(dataset2);

      dataset1 =  createDataset1Panel();
      dataset1.setCollapsed(dataset1_collapsed);
      DatasetsPanel.add(dataset1);

      dataset2 =  createDataset2Panel();
      dataset2.setCollapsed(dataset2_collapsed);
      DatasetsPanel.add(dataset2);

      DatasetsPanel.revalidate();
      datasets.getContentPane().add(DatasetsPanel, BorderLayout.NORTH);
      datasets.setCollapsed(datasets_collapsed);
      datasets.revalidate();

      UpdatePanel(this.params);

  }
  /*Given a set of parameters, update the panel to contain the values that are
  * defined in this set of parameters
  */
  private void UpdatePanel(EnrichmentMapParameters params){

      //check to see if the user had already entered anything into the newly created Dataset Frame
      if(params.getEnrichmentDataset1FileName1()!=null){
        Dataset1FileNameTextField.setText(params.getEnrichmentDataset1FileName1());
        Dataset1FileNameTextField.setToolTipText(params.getEnrichmentDataset1FileName1());
      }
      if(params.getEnrichmentDataset2FileName1()!=null){
        Dataset2FileNameTextField.setText(params.getEnrichmentDataset2FileName1());
        Dataset2FileNameTextField.setToolTipText(params.getEnrichmentDataset2FileName1());
      }
      if(params.getGCTFileName1() != null){
          GCTFileName1TextField.setText(params.getGCTFileName1());
          GCTFileName1TextField.setToolTipText(params.getGCTFileName1());
      }
      if(params.getGCTFileName2() != null){
          GCTFileName2TextField.setText(params.getGCTFileName2());
          GCTFileName2TextField.setToolTipText(params.getGCTFileName2());
      }
      if(params.getDataset1RankedFile() != null){
          Dataset1RankFileTextField.setText(params.getDataset1RankedFile());
          Dataset1RankFileTextField.setToolTipText(params.getDataset1RankedFile());
      }
      if(params.getDataset2RankedFile() != null){
          Dataset2RankFileTextField.setText(params.getDataset2RankedFile());
          Dataset2RankFileTextField.setToolTipText(params.getDataset2RankedFile());
      }

      //update the phenotypes
      if(params.getDataset1Phenotype1() != null){
          Dataset1Phenotype1TextField.setText(params.getDataset1Phenotype1());
          Dataset1Phenotype1TextField.setToolTipText(params.getDataset1Phenotype1());
      }
      if(params.getDataset1Phenotype2() != null){
          Dataset1Phenotype2TextField.setText(params.getDataset1Phenotype2());
          Dataset1Phenotype2TextField.setToolTipText(params.getDataset1Phenotype2());
      }
      if(params.getDataset2Phenotype1() != null){
          Dataset2Phenotype1TextField.setText(params.getDataset2Phenotype1());
          Dataset2Phenotype1TextField.setToolTipText(params.getDataset2Phenotype1());
      }
      if(params.getDataset2Phenotype2() != null){
          Dataset2Phenotype2TextField.setText(params.getDataset2Phenotype2());
          Dataset2Phenotype2TextField.setToolTipText(params.getDataset2Phenotype2());
      }

      //Special case with Enrichment results file 2 (there should only be two enrichment
      //Files if the analysis specified is GSEA.  If the user has loaded from an RPT and
      //then changes the type of analysis there shouldn't be an extra file
      if(params.isGSEA()){
           if(params.getEnrichmentDataset1FileName2()!=null){
                Dataset1FileName2TextField.setText(params.getEnrichmentDataset1FileName2());
                Dataset1FileName2TextField.setToolTipText(params.getEnrichmentDataset1FileName2());
            }
            if(params.getEnrichmentDataset2FileName2()!=null){
                Dataset2FileName2TextField.setText(params.getEnrichmentDataset2FileName2());
                Dataset2FileName2TextField.setToolTipText(params.getEnrichmentDataset2FileName2());
            }
      }
      else{
            if((params.getEnrichmentDataset1FileName2()!=null) || (params.getEnrichmentDataset2FileName2()!=null)){
                JOptionPane.showMessageDialog(this,"Running Enrichment Map with Generic input " +
                        "allows for only one enrichment results file.\n  The second file specified has been removed.");
                if(params.getEnrichmentDataset1FileName2()!=null)
                    params.setEnrichmentDataset1FileName2(null);

                if(params.getEnrichmentDataset2FileName2()!=null)
                    params.setEnrichmentDataset2FileName2(null);

            }
      }
  }

  private void selectJaccardOrOverlapActionPerformed(java.awt.event.ActionEvent evt) {
        if(evt.getActionCommand().equalsIgnoreCase("jaccard")){
            params.setJaccard(true);
            if ( ! params.isJaccardCutOffChanged() ) {
                params.setJaccardCutOff( params.getDefaultJaccardCutOff() );
                coeffecientTextField.setText( Double.toString(params.getJaccardCutOff()) );
            }
        }
     else if(evt.getActionCommand().equalsIgnoreCase("overlap")){
            params.setJaccard(false);
            if ( ! params.isJaccardCutOffChanged() ) {
                params.setJaccardCutOff(params.getDefaultOverlapCutOff());
                coeffecientTextField.setText( Double.toString(params.getJaccardCutOff()) );
          }
        }
     else{
            JOptionPane.showMessageDialog(this,"Invalid Jaccard Radio Button action command");
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
               params.setGMTFileName(file.getAbsolutePath());
               GMTFileNameTextField.setToolTipText(file.getAbsolutePath());
           }
       }

         private void selectGCTFileButtonActionPerformed(
               java.awt.event.ActionEvent evt) {

//         Create FileFilter
           CyFileFilter filter = new CyFileFilter();

           // Add accepted File Extensions
           filter.addExtension("gct");
           filter.addExtension("rpt");
           filter.addExtension("rnk");
           filter.addExtension("txt");
           filter.setDescription("All GCT files");

           // Get the file name
           File file = FileUtil.getFile("Import GCT File", FileUtil.LOAD,
                        new CyFileFilter[] { filter });
           if(file != null) {

               if(file.getPath().contains(".rpt")){
                   //The file loaded is an rpt file --> populate the fields based on the
                   populateFieldsFromRpt(file,true);

               }
               else{
                    GCTFileName1TextField.setForeground(checkFile(file.getAbsolutePath()));
                    GCTFileName1TextField.setText(file.getAbsolutePath());
                    params.setGCTFileName1(file.getAbsolutePath());
                    GCTFileName1TextField.setToolTipText(file.getAbsolutePath());
               }
               params.setData(true);

           }
       }
    private void selectGCTFileButton2ActionPerformed(
             java.awt.event.ActionEvent evt) {

//         Create FileFilter
         CyFileFilter filter = new CyFileFilter();

         // Add accepted File Extensions
         filter.addExtension("gct");
         filter.addExtension("txt");
         filter.addExtension("rnk");
         filter.addExtension("rpt");
         filter.setDescription("All GCT files");

         // Get the file name
         File file = FileUtil.getFile("Import GCT File", FileUtil.LOAD,
                      new CyFileFilter[] { filter });
         if(file != null) {
             if(file.getPath().contains(".rpt")){
                      //The file loaded is an rpt file --> populate the fields based on the
                      populateFieldsFromRpt(file,false);
                  }
             else{
               GCTFileName2TextField.setForeground(checkFile(file.getAbsolutePath()));
               GCTFileName2TextField.setText(file.getAbsolutePath());
               params.setGCTFileName2(file.getAbsolutePath());
               GCTFileName2TextField.setToolTipText(file.getAbsolutePath());
             }
             params.setTwoDatasets(true);
             params.setData2(true);
         }
     }

       private void selectDataset1FileButtonActionPerformed(
               java.awt.event.ActionEvent evt) {

//         Create FileFilter
        CyFileFilter filter = new CyFileFilter();

        // Add accepted File Extensions
        filter.addExtension("txt");
        filter.addExtension("rpt");
        filter.addExtension("xls");
        filter.setDescription("All result files");

        // Get the file name
         File file = FileUtil.getFile("import dataset result file", FileUtil.LOAD, new CyFileFilter[]{ filter });

        if(file != null) {
             if(file.getPath().contains(".rpt")){
                      //The file loaded is an rpt file --> populate the fields based on the
                      populateFieldsFromRpt(file,false);

                  }
             else{
                Dataset1FileNameTextField.setForeground(checkFile(file.getAbsolutePath()));
                Dataset1FileNameTextField.setText(file.getAbsolutePath() );
                params.setEnrichmentDataset1FileName1(file.getAbsolutePath());
                Dataset1FileNameTextField.setToolTipText(file.getAbsolutePath() );
             }

        }
    }

       private void selectDataset1File2ButtonActionPerformed(
               java.awt.event.ActionEvent evt) {

//         Create FileFilter
        CyFileFilter filter = new CyFileFilter();

        // Add accepted File Extensions
        filter.addExtension("txt");
        filter.addExtension("rpt");
        filter.addExtension("xls");
        filter.setDescription("All result files");

        // Get the file name
         File file = FileUtil.getFile("import dataset result file", FileUtil.LOAD, new CyFileFilter[]{ filter });

        if(file != null) {
             if(file.getPath().contains(".rpt")){
                      //The file loaded is an rpt file --> populate the fields based on the
                      populateFieldsFromRpt(file,false);
                  }
             else{
                Dataset1FileName2TextField.setForeground(checkFile(file.getAbsolutePath()));
                Dataset1FileName2TextField.setText(file.getAbsolutePath() );
                params.setEnrichmentDataset1FileName2(file.getAbsolutePath());
                Dataset1FileName2TextField.setToolTipText(file.getAbsolutePath() );
             }

        }
    }

    private void selectDataset2FileButtonActionPerformed(
             java.awt.event.ActionEvent evt) {

//         Create FileFilter
      CyFileFilter filter = new CyFileFilter();

      // Add accepted File Extensions
      filter.addExtension("txt");
      filter.addExtension("xls");
      filter.addExtension("rpt");
      filter.setDescription("All result files");

      // Get the file name
       File file = FileUtil.getFile("import dataset result file", FileUtil.LOAD, new CyFileFilter[]{ filter });

      if(file != null) {
           if(file.getPath().contains(".rpt")){
                      //The file loaded is an rpt file --> populate the fields based on the
                      populateFieldsFromRpt(file,false);
                  }
             else{
              Dataset2FileNameTextField.setForeground(checkFile(file.getAbsolutePath()));
              Dataset2FileNameTextField.setText(file.getAbsolutePath() );
              params.setEnrichmentDataset2FileName1(file.getAbsolutePath());
              Dataset2FileNameTextField.setToolTipText(file.getAbsolutePath() );
           }
           params.setTwoDatasets(true);
      }
  }

     private void selectDataset2File2ButtonActionPerformed(
             java.awt.event.ActionEvent evt) {

//         Create FileFilter
      CyFileFilter filter = new CyFileFilter();

      // Add accepted File Extensions
      filter.addExtension("txt");
      filter.addExtension("xls");
      filter.addExtension("rpt");
      filter.setDescription("All result files");

      // Get the file name
       File file = FileUtil.getFile("import dataset result file", FileUtil.LOAD, new CyFileFilter[]{ filter });

      if(file != null) {
           if(file.getPath().contains(".rpt")){
                      //The file loaded is an rpt file --> populate the fields based on the
                      populateFieldsFromRpt(file,false);

                  }
             else{
              Dataset2FileName2TextField.setForeground(checkFile(file.getAbsolutePath()));
              Dataset2FileName2TextField.setText(file.getAbsolutePath() );
              params.setEnrichmentDataset2FileName2(file.getAbsolutePath());
              Dataset2FileName2TextField.setToolTipText(file.getAbsolutePath() );
           }
           params.setTwoDatasets(true);
      }
  }


     private void selectRank1FileButtonActionPerformed(
               java.awt.event.ActionEvent evt) {

//         Create FileFilter
        CyFileFilter filter = new CyFileFilter();

        // Add accepted File Extensions
        filter.addExtension("txt");
        filter.addExtension("rnk");
        filter.setDescription("All result files");

        // Get the file name
         File file = FileUtil.getFile("import rank file", FileUtil.LOAD, new CyFileFilter[]{ filter });

        if(file != null) {
                Dataset1RankFileTextField.setForeground(checkFile(file.getAbsolutePath()));
                Dataset1RankFileTextField.setText(file.getAbsolutePath() );
                params.setDataset1RankedFile(file.getAbsolutePath());
                Dataset1RankFileTextField.setToolTipText(file.getAbsolutePath() );


        }
    }

     private void selectRank2FileButtonActionPerformed(
               java.awt.event.ActionEvent evt) {

//         Create FileFilter
        CyFileFilter filter = new CyFileFilter();

        // Add accepted File Extensions
        filter.addExtension("txt");
        filter.addExtension("rnk");
        filter.setDescription("All result files");

        // Get the file name
         File file = FileUtil.getFile("import rank file", FileUtil.LOAD, new CyFileFilter[]{ filter });

        if(file != null) {
                Dataset2RankFileTextField.setForeground(checkFile(file.getAbsolutePath()));
                Dataset2RankFileTextField.setText(file.getAbsolutePath() );
                params.setDataset2RankedFile(file.getAbsolutePath());
                Dataset2RankFileTextField.setToolTipText(file.getAbsolutePath() );


        }
    }

    //Clear the current panel and clear the params associated with this panel
    private void resetPanel(){

        this.params = new EnrichmentMapParameters();

        GMTFileNameTextField.setText("");

        GCTFileName1TextField.setText("");
        GCTFileName2TextField.setText("");

        Dataset1FileNameTextField.setText("");
        Dataset1FileName2TextField.setText("");

        Dataset2FileNameTextField.setText("");
        Dataset2FileName2TextField.setText("");

        Dataset1RankFileTextField.setText("");
        Dataset2RankFileTextField.setText("");

        Dataset1Phenotype1TextField.setText(params.getDataset1Phenotype1());
        Dataset1Phenotype2TextField.setText(params.getDataset1Phenotype2());
        Dataset2Phenotype1TextField.setText(params.getDataset2Phenotype1());
        Dataset2Phenotype2TextField.setText(params.getDataset2Phenotype2());

        pvalueTextField.setText(Double.toString(params.getPvalue()));
        qvalueTextField.setText(Double.toString(params.getQvalue()));
        coeffecientTextField.setText(Double.toString(params.getJaccardCutOff()));

        gsea.setSelected(params.isGSEA());
        generic.setSelected(!params.isGSEA());
        jaccard.setSelected(params.isJaccard());
        overlap.setSelected(!params.isJaccard());
    }

    public void updateContents(EnrichmentMapParameters current_params){
        this.params = new EnrichmentMapParameters(current_params);


        GMTFileNameTextField.setText(current_params.getGMTFileName());

        GCTFileName1TextField.setText(current_params.getGCTFileName1());
        GCTFileName2TextField.setText(current_params.getGCTFileName2());

        Dataset1FileNameTextField.setText(current_params.getEnrichmentDataset1FileName1());
        Dataset1FileName2TextField.setText(current_params.getEnrichmentDataset1FileName2());

        Dataset2FileNameTextField.setText(current_params.getEnrichmentDataset2FileName1());
        Dataset2FileName2TextField.setText(current_params.getEnrichmentDataset2FileName2());

        Dataset1RankFileTextField.setText(current_params.getDataset1RankedFile());
        Dataset2RankFileTextField.setText(current_params.getDataset2RankedFile());

        Dataset1Phenotype1TextField.setText(current_params.getDataset1Phenotype1());
        Dataset1Phenotype2TextField.setText(current_params.getDataset1Phenotype2());
        Dataset2Phenotype1TextField.setText(current_params.getDataset2Phenotype1());
        Dataset2Phenotype2TextField.setText(current_params.getDataset2Phenotype2());

        pvalueTextField.setText(Double.toString(current_params.getPvalue()));
        qvalueTextField.setText(Double.toString(current_params.getQvalue()));
        coeffecientTextField.setText(Double.toString(current_params.getJaccardCutOff()));

        gsea.setSelected(current_params.isGSEA());
        generic.setSelected(!current_params.isGSEA());
        jaccard.setSelected(current_params.isJaccard());
        overlap.setSelected(!current_params.isJaccard());
    }

    public EnrichmentMapParameters getParams() {
        return params;
    }

    public void setParams(EnrichmentMapParameters params) {
        this.params = params;
    }
}
