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

package org.baderlab.csplugins.enrichmentmap.view;

import javax.swing.*;

import org.baderlab.csplugins.enrichmentmap.EnrichmentMapParameters;
import org.baderlab.csplugins.enrichmentmap.EnrichmentMapUtils;
import org.baderlab.csplugins.enrichmentmap.actions.EnrichmentMapBuildMapEvent;
import org.baderlab.csplugins.enrichmentmap.actions.EnrichmentMapParseInputEvent;
import org.baderlab.csplugins.enrichmentmap.actions.ShowAboutPanelAction;
import org.baderlab.csplugins.enrichmentmap.model.DataSetFiles;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.model.JMultiLineToolTip;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.view.layout.CyLayoutAlgorithm;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.io.util.StreamUtil;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyTableFactory;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.session.CySessionManager;
import org.cytoscape.task.edit.MapTableToNetworkTablesTaskFactory;
import org.cytoscape.util.swing.FileChooserFilter;
import org.cytoscape.util.swing.FileUtil;
import org.cytoscape.util.swing.OpenBrowser;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.cytoscape.work.TaskManager;
import org.cytoscape.work.swing.DialogTaskManager;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.text.DecimalFormat;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

/**
 * Created by
 * User: risserlin
 * Date: Jun 16, 2009
 * Time: 11:13:12 AM
 * <p>
 * Enrichment map User input Panel
 */
public class EnrichmentMapInputPanel extends JPanel implements CytoPanelComponent {
    /**
     * 
     */
	//services required
		private StreamUtil streamUtil;
	    private CyApplicationManager applicationManager;
	    private CyNetworkManager networkManager;
	    private CyNetworkViewManager networkViewManager;
	    private CyNetworkViewFactory networkViewFactory;
	    private CyNetworkFactory networkFactory;
	    private CyTableFactory tableFactory;
	    private CyTableManager tableManager;
	    
	    private VisualMappingManager visualMappingManager;
	    private VisualStyleFactory visualStyleFactory;
	    
	    //we will need all three mappers
	    private VisualMappingFunctionFactory vmfFactoryContinuous;
	    private VisualMappingFunctionFactory vmfFactoryDiscrete;
	    private VisualMappingFunctionFactory vmfFactoryPassthrough;
	    
	    private CyLayoutAlgorithmManager layoutManager;
	    private  MapTableToNetworkTablesTaskFactory mapTableToNetworkTable;
	    //
	    private DialogTaskManager dialog;
	    private CySessionManager sessionManager;
	
	private CySwingApplication application;
	private OpenBrowser browser;
	private FileUtil fileUtil;
    private CyServiceRegistrar registrar;
    
    private EnrichmentMapInputPanel empanel;
	
    private static final long serialVersionUID = -7837369382106745874L;

    CollapsiblePanel Parameters;
    CollapsiblePanel datasets;

    CollapsiblePanel dataset1;
    CollapsiblePanel dataset2;

    JPanel DatasetsPanel;

    DecimalFormat decFormat; // used in the formatted text fields

    private EnrichmentMapParameters params;
    
    private DataSetFiles dataset1files = new DataSetFiles();
    private DataSetFiles dataset2files = new DataSetFiles();
    

    //Genesets file related components
    //user specified file names
    private JFormattedTextField GMTFileNameTextField;

    private JFormattedTextField GCTFileName1TextField;
    private JFormattedTextField GCTFileName2TextField;

    private JFormattedTextField Dataset1FileNameTextField;
    private JFormattedTextField Dataset1FileName2TextField;

    private JFormattedTextField Dataset2FileNameTextField;
    private JFormattedTextField Dataset2FileName2TextField;

    private JFormattedTextField Dataset1RankFileTextField;
    private JFormattedTextField Dataset2RankFileTextField;

    //user specified terms and cut offs
    private JFormattedTextField Dataset1Phenotype1TextField;
    private JFormattedTextField Dataset1Phenotype2TextField;
    private JFormattedTextField Dataset2Phenotype1TextField;
    private JFormattedTextField Dataset2Phenotype2TextField;

    private JFormattedTextField pvalueTextField;
    private JFormattedTextField qvalueTextField;
    private JFormattedTextField coeffecientTextField;
    private JFormattedTextField combinedConstantTextField;

    //flags
    private JRadioButton gsea;
    private JRadioButton generic;
    private JRadioButton david;
    private JRadioButton overlap;
    private JRadioButton jaccard;
    private JRadioButton combined;

    private int defaultColumns = 15;

    //instruction text
    public static String gct_instruction = "Please select the expression file (.gct), (.rpt)...";
    public static String gmt_instruction = "Please select the Gene Set file (.gmt)...";
    public static String dataset_instruction = "Please select the GSEA Result file (.txt)...";
    public static String rank_instruction = "Please select the rank file (.txt), (.rnk)...";

    //tool tips
    private static String gmtTip = "File specifying gene sets.\n" + "Format: geneset name <tab> description <tab> gene ...";
    private static String gctTip = "File with gene expression values.\n" + "Format: gene <tab> description <tab> expression value <tab> ...";
    private static String datasetTip = "File specifying enrichment results.\n";
    private static String rankTip = "File specifying ranked genes.\n" + "Format: gene <tab> score or statistic";
    
    private boolean similarityCutOffChanged = false;
    private boolean LoadedFromRpt_dataset1 = false;
    private boolean LoadedFromRpt_dataset2 = false;
    
    /**
     * Constructor
     */
    public EnrichmentMapInputPanel(CyNetworkFactory networkFactory, CyApplicationManager applicationManager, 
    		CyNetworkManager networkManager, CyNetworkViewManager networkViewManager,
    		CyTableFactory tableFactory,CyTableManager tableManager,CyNetworkViewFactory networkViewFactory,
    		VisualMappingManager visualMappingManager,VisualStyleFactory visualStyleFactory,
    		VisualMappingFunctionFactory vmfFactoryContinuous, VisualMappingFunctionFactory vmfFactoryDiscrete,
    	     VisualMappingFunctionFactory vmfFactoryPassthrough,DialogTaskManager dialog, CySessionManager sessionManager, 
    	     CySwingApplication application, OpenBrowser browser,FileUtil fileUtil, StreamUtil streamUtil,CyServiceRegistrar registrar,
    	     CyLayoutAlgorithmManager layoutManager, MapTableToNetworkTablesTaskFactory mapTableToNetworkTable) {

    		this.empanel = this;
        decFormat = new DecimalFormat();
        decFormat.setParseIntegerOnly(false);
        this.networkFactory = networkFactory;
        this.applicationManager = applicationManager;
        this.networkManager = networkManager;
        this.networkViewManager	= networkViewManager;
        this.tableFactory = tableFactory;
        this.tableManager = tableManager;
        this.networkViewFactory = networkViewFactory;
        this.streamUtil = streamUtil;
        
        this.visualMappingManager = visualMappingManager;
        this.visualStyleFactory = visualStyleFactory;
        
        this.vmfFactoryContinuous = vmfFactoryContinuous;
        this.vmfFactoryDiscrete = vmfFactoryDiscrete;
        this.vmfFactoryPassthrough = vmfFactoryPassthrough;
        
        this.layoutManager = layoutManager;
        this.mapTableToNetworkTable = mapTableToNetworkTable;
        
        this.dialog = dialog;
        
        this.sessionManager = sessionManager;
        this.application = application;
        this.browser = browser;
        this.fileUtil = fileUtil;
        this.streamUtil = streamUtil;
        this.registrar = registrar;
        setLayout(new BorderLayout());        

        //get the current enrichment map parameters
        //params = EnrichmentMapManager.getInstance().getParameters(Cytoscape.getCurrentNetwork().getIdentifier());
         params = new EnrichmentMapParameters(sessionManager,streamUtil);

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
        * Creates a JPanel containing analysis type (GSEA or generic)  radio buttons and links to additional information
        *
        * @return panel containing the analysis type (GSEA or generic) option buttons
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
           
           //TODO add about box action listener
           //about.addActionListener(new ShowAboutPanelAction(null, null, null, application, browser));

           //add button to do bulk EM Creation
           //TODO: button for bulk EM from main EM interface not implemented
           JButton bulk = new JButton("Bulk EM");
           /*bulk.addActionListener(new BulkEMCreationAction(null,null,null,application,fileUtil,fileFilterUtil));
			*/
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

           c_buttons.gridy = 2;
            gridbag_buttons.setConstraints(bulk, c_buttons);
            buttonsPanel.add(bulk);


           JPanel panel = new JPanel();

           GridBagLayout gridbag = new GridBagLayout();
           GridBagConstraints c = new GridBagConstraints();
           panel.setLayout(gridbag);

           c.weighty = 1;
           c.weightx = 1;
           c.insets = new Insets(0,0,0,0);
           c.fill = GridBagConstraints.HORIZONTAL;
           panel.setBorder(BorderFactory.createTitledBorder("Analysis Type"));

           if(params.getMethod().equalsIgnoreCase(EnrichmentMapParameters.method_GSEA)){
                gsea = new JRadioButton(EnrichmentMapParameters.method_GSEA, true);
                generic = new JRadioButton(EnrichmentMapParameters.method_generic, false);
                david = new JRadioButton(EnrichmentMapParameters.method_DAVID, false);
           }else if(params.getMethod().equalsIgnoreCase(EnrichmentMapParameters.method_generic)){
                gsea = new JRadioButton(EnrichmentMapParameters.method_GSEA, false);
                generic = new JRadioButton(EnrichmentMapParameters.method_generic, true);
                david = new JRadioButton(EnrichmentMapParameters.method_DAVID, false);
           }else if(params.getMethod().equalsIgnoreCase(EnrichmentMapParameters.method_DAVID)){
                gsea = new JRadioButton(EnrichmentMapParameters.method_GSEA, false);
                generic = new JRadioButton(EnrichmentMapParameters.method_generic, false);
                david = new JRadioButton(EnrichmentMapParameters.method_DAVID, true);
           }

           gsea.setActionCommand(EnrichmentMapParameters.method_GSEA);
           generic.setActionCommand(EnrichmentMapParameters.method_generic);
           david.setActionCommand(EnrichmentMapParameters.method_DAVID);

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
           david.addActionListener(new java.awt.event.ActionListener() {
                               public void actionPerformed(java.awt.event.ActionEvent evt) {
                                   selectAnalysisTypeActionPerformed(evt);
                               }
           });
           ButtonGroup analysisOptions = new ButtonGroup();
           analysisOptions.add(gsea);
           analysisOptions.add(generic);
           analysisOptions.add(david);


           c.gridx = 0;
           c.gridwidth = 3;
           c.gridy = 0;
           gridbag.setConstraints(gsea, c);
           panel.add(gsea);
           c.gridy = 1;
           gridbag.setConstraints(generic, c);
           panel.add(generic);
           c.gridy = 2;
           gridbag.setConstraints(david, c);
           panel.add(david);

          JPanel topPanel = new JPanel();
          topPanel.setLayout(new BorderLayout());
          topPanel.add(buttonsPanel, BorderLayout.EAST);
          topPanel.add(panel, BorderLayout.CENTER);

           return topPanel;
       }

           /**
            * Creates a collapsible panel that holds main user inputs geneset files, datasets and parameters
            *
            * @return collapsablePanel - main analysis panel
            */
           private CollapsiblePanel createOptionsPanel() {
               CollapsiblePanel collapsiblePanel = new CollapsiblePanel("User Input");

               JPanel panel = new JPanel();
               panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

               //Gene set file panel
               CollapsiblePanel GMTcollapsiblePanel = createGMTPanel();
               GMTcollapsiblePanel.setCollapsed(false);

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

               panel.add(GMTcollapsiblePanel);
               panel.add(datasets);
               panel.add(ParametersPanel);

               collapsiblePanel.getContentPane().add(panel, BorderLayout.NORTH);
               return collapsiblePanel;
           }

    /**
     * Creates a collapsible panel that holds gene set file specification
     *
     * @return collapsible panel - gmt gene set file specification interface
     */
        private CollapsiblePanel createGMTPanel() {
            CollapsiblePanel collapsiblePanel = new CollapsiblePanel("Gene Sets");

            JPanel panel = new JPanel();
            panel.setLayout(new GridLayout(0, 1));

            //add GMT file
            JLabel GMTLabel = new JLabel("GMT:"){
                 /**
                 * 
                 */
                private static final long serialVersionUID = -122741876830022713L;

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

           JPanel newGMTPanel = new JPanel();
           newGMTPanel.setLayout(new BorderLayout());

           newGMTPanel.add(GMTLabel,BorderLayout.WEST);
           newGMTPanel.add( GMTFileNameTextField, BorderLayout.CENTER);
           newGMTPanel.add( selectGMTFileButton, BorderLayout.EAST);

           //add the components to the panel
           if(!params.getMethod().equalsIgnoreCase(EnrichmentMapParameters.method_DAVID))
                panel.add(newGMTPanel);

           collapsiblePanel.getContentPane().add(panel, BorderLayout.NORTH);
           return collapsiblePanel;

        }

    /**
     * Creates a collapsible panel that holds dataset 1 file specifications
     *
     * @return  Collapsible panel with dataset 1 file specification interface
     */
       private CollapsiblePanel createDataset1Panel() {
           CollapsiblePanel collapsiblePanel = new CollapsiblePanel("Dataset 1");

           JPanel panel = new JPanel();
           panel.setLayout(new GridLayout(0, 1));

           //add GCT file
           JLabel GCTLabel = new JLabel("Expression:"){
            /**
             * 
             */
            private static final long serialVersionUID = -1021506153608619217L;

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
            /**
             * 
             */
            private static final long serialVersionUID = 4890287742836119482L;

            public JToolTip createToolTip() {
                return new JMultiLineToolTip();
                }
            };
           Results1Label.setToolTipText(datasetTip);
           if(!params.getMethod().equalsIgnoreCase(EnrichmentMapParameters.method_GSEA))
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
            /**
             * 
             */
            private static final long serialVersionUID = 8462720651589188103L;

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
            //don't add the expression file to the David  results.
           //if(!params.getMethod().equalsIgnoreCase(EnrichmentMapParameters.method_DAVID))
                panel.add(GCTPanel);
           panel.add(Results1Panel);
           if(params.getMethod().equalsIgnoreCase(EnrichmentMapParameters.method_GSEA))
                panel.add(Results2Panel);

           collapsiblePanel.getContentPane().add(panel, BorderLayout.NORTH);
           if(!params.getMethod().equalsIgnoreCase(EnrichmentMapParameters.method_DAVID))
                collapsiblePanel.getContentPane().add(createAdvancedDatasetOptions(1),BorderLayout.SOUTH);
           return collapsiblePanel;

       }

    /**
     * Creates a collapsible panel that holds dataset 2 file specification
     *
     * @return Collapsible panel that holds dataset2 file specification interface
     */
        private CollapsiblePanel createDataset2Panel() {
           CollapsiblePanel collapsiblePanel = new CollapsiblePanel("Dataset 2");

           JPanel panel = new JPanel();
           panel.setLayout(new GridLayout(0, 1));

           //add GCT file
           JLabel GCTLabel = new JLabel("Expression:"){
            /**
             * 
             */
            private static final long serialVersionUID = 2369686770191667604L;

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
            /**
             * 
             */
            private static final long serialVersionUID = -1405865291417154L;

            public JToolTip createToolTip() {
                return new JMultiLineToolTip();
                }
            };

            if(!params.getMethod().equalsIgnoreCase(EnrichmentMapParameters.method_GSEA))
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
            /**
             * 
             */
            private static final long serialVersionUID = -5178668573493553453L;

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
            //don't add the expression file to the David  results.
           //if(!params.getMethod().equalsIgnoreCase(EnrichmentMapParameters.method_DAVID))
                panel.add(GCTPanel);
           panel.add(Results1Panel);
           if(params.getMethod().equalsIgnoreCase(EnrichmentMapParameters.method_GSEA))
                panel.add(Results2Panel);

           collapsiblePanel.getContentPane().add(panel, BorderLayout.NORTH);
           if(!params.getMethod().equalsIgnoreCase(EnrichmentMapParameters.method_DAVID))
                collapsiblePanel.getContentPane().add(createAdvancedDatasetOptions(2),BorderLayout.SOUTH);
           return collapsiblePanel;

       }

    /**
     * Creates a collapsible panel that holds the advanced options specifications (rank file, phenotypes)
     *
     * @param dataset - whether this collapsible advanced panel is for dataset 1 or dataset 2
     * @return Collapsible panel that holds the advanced options specification interface
     */
    private CollapsiblePanel createAdvancedDatasetOptions(int dataset){
           //create a panel for advanced options
            CollapsiblePanel Advanced = new CollapsiblePanel("Advanced");

           JPanel advancedpanel = new JPanel();
           advancedpanel.setLayout(new BorderLayout());

            //add Ranks file
           JLabel RanksLabel = new JLabel("Ranks:"){
           /**
             * 
             */
            private static final long serialVersionUID = 4549754054012943869L;

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
        * @return panel containing the parameter specification interface
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
           pvalueTextField.setValue(params.getPvalue());

           JPanel pvalueCutOffPanel = new JPanel();
           pvalueCutOffPanel.setLayout(new BorderLayout());
           pvalueCutOffPanel.setToolTipText(pvalueCutOffTip);

           pvalueCutOffPanel.add(pvalueCutOffLabel, BorderLayout.WEST);
           pvalueCutOffPanel.add(pvalueTextField, BorderLayout.EAST);


            //qvalue cutoff input
           JLabel qvalueCutOffLabel = new JLabel("FDR Q-value Cutoff");
           qvalueTextField = new JFormattedTextField(decFormat);
           qvalueTextField.setColumns(3);
           qvalueTextField.addPropertyChangeListener("value", new EnrichmentMapInputPanel.FormattedTextFieldAction());
           String qvalueCutOffTip = "Sets the FDR q-value cutoff \n" +
                   "only genesets with a FDR q-value less than \n"+
                    "the cutoff will be included.";
           qvalueTextField.setToolTipText(qvalueCutOffTip);
           qvalueTextField.setText(Double.toString(params.getQvalue()));
           qvalueTextField.setValue(params.getQvalue());

           JPanel qvalueCutOffPanel = new JPanel();
           qvalueCutOffPanel.setLayout(new BorderLayout());
           qvalueCutOffPanel.setToolTipText(qvalueCutOffTip);

           qvalueCutOffPanel.add(qvalueCutOffLabel, BorderLayout.WEST);
           qvalueCutOffPanel.add(qvalueTextField, BorderLayout.EAST);

           //Coeffecient cutoff input

           ButtonGroup jaccardOrOverlap;

           jaccard = new JRadioButton("Jaccard Coefficient");
           jaccard.setActionCommand("jaccard");
           jaccard.setSelected(true);
           overlap = new JRadioButton("Overlap Coefficient");
           overlap.setActionCommand("overlap");
           combined = new JRadioButton("Jaccard+Overlap Combined");
           combined.setActionCommand("combined");
           if ( params.getSimilarityMetric().equalsIgnoreCase(EnrichmentMapParameters.SM_JACCARD) ) {
               jaccard.setSelected(true);
               overlap.setSelected(false);
               combined.setSelected(false);
           } else if ( params.getSimilarityMetric().equalsIgnoreCase(EnrichmentMapParameters.SM_OVERLAP)){
               jaccard.setSelected(false);
               overlap.setSelected(true);
               combined.setSelected(false);
           }
           else if ( params.getSimilarityMetric().equalsIgnoreCase(EnrichmentMapParameters.SM_COMBINED)){
               jaccard.setSelected(false);
               overlap.setSelected(false);
               combined.setSelected(true);
           }
           jaccardOrOverlap = new javax.swing.ButtonGroup();
           jaccardOrOverlap.add(jaccard);
           jaccardOrOverlap.add(overlap);
           jaccardOrOverlap.add(combined);

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
           combined.addActionListener(new java.awt.event.ActionListener() {
               public void actionPerformed(java.awt.event.ActionEvent evt) {
                   selectJaccardOrOverlapActionPerformed(evt);
               }
           });

           //create a panel for the two buttons
           JPanel index_buttons = new JPanel();
           index_buttons.setLayout(new BorderLayout());
           index_buttons.add(jaccard, BorderLayout.NORTH);
           index_buttons.add(overlap, BorderLayout.CENTER);
           index_buttons.add(combined, BorderLayout.SOUTH);

           JLabel coeffecientCutOffLabel = new JLabel("Cutoff");
           coeffecientTextField = new JFormattedTextField(decFormat);
           coeffecientTextField.setColumns(3);
           coeffecientTextField.addPropertyChangeListener("value", new EnrichmentMapInputPanel.FormattedTextFieldAction());
           String coeffecientCutOffTip = "Sets the Jaccard or Overlap coefficient cutoff \n" +
                             "only edges with a Jaccard or Overlap coefficient less than \n"+
                              "the cutoff will be added.";
          coeffecientTextField.setToolTipText(coeffecientCutOffTip);
//          coeffecientTextField.setText(Double.toString(params.getSimilarityCutOff()));
          coeffecientTextField.setValue(params.getSimilarityCutOff());
          similarityCutOffChanged = false; //reset for new Panel after .setValue(...) wrongly changed it to "true"

          //Add a box to specify the constant used in created the combined value
           JLabel combinedCutoff = new JLabel("Combined Constant");
           combinedConstantTextField =new JFormattedTextField(decFormat);
           combinedConstantTextField.setColumns(3);
           combinedConstantTextField.addPropertyChangeListener("value", new FormattedTextFieldAction());
           combinedConstantTextField.setValue(0.5);

           JPanel kconstantPanel = new JPanel();
           kconstantPanel.setLayout(new BorderLayout());
           kconstantPanel.add(combinedCutoff, BorderLayout.WEST);
           kconstantPanel.add(combinedConstantTextField, BorderLayout.EAST);

          JPanel coeffecientCutOffPanel = new JPanel();
          coeffecientCutOffPanel.setLayout(new BorderLayout());
          coeffecientCutOffPanel.setToolTipText(coeffecientCutOffTip);

          coeffecientCutOffPanel.add(index_buttons,BorderLayout.WEST);
          coeffecientCutOffPanel.add(coeffecientCutOffLabel, BorderLayout.CENTER);
          coeffecientCutOffPanel.add(coeffecientTextField, BorderLayout.EAST);
           coeffecientCutOffPanel.add(kconstantPanel, BorderLayout.SOUTH);

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
                    params.setSimilarityCutOff(value.doubleValue());
                    similarityCutOffChanged = true;
                } else {
                    source.setValue(params.getSimilarityCutOff());
                    message += "The Overlap/Jaccard Coefficient cutoff must be between 0 and 1.";
                    invalid = true;
                }
            }else if (source == combinedConstantTextField) {
                Number value = (Number) combinedConstantTextField.getValue();
                if ((value != null) && (value.doubleValue() >= 0.0) && (value.doubleValue() <= 1.0)) {
                    params.setCombinedConstant(value.doubleValue());

                    //if the similarity cutoff is equal to the default then updated it to reflect what it should be given the value of k
                    if(!similarityCutOffChanged && params.getSimilarityMetric().equalsIgnoreCase(EnrichmentMapParameters.SM_COMBINED))
                        params.setSimilarityCutOff( (params.getDefaultOverlapCutOff() * value.doubleValue()) + ((1-value.doubleValue()) * params.getDefaultJaccardCutOff()) );

                    //params.setCombinedConstantCutOffChanged(true);
                } else {
                    source.setValue(0.5);
                    message += "The combined Overlap/Jaccard Coefficient constant must be between 0 and 1.";
                    invalid = true;
                }
            }
            else if (source == GMTFileNameTextField) {
                String value = GMTFileNameTextField.getText();
                if(value.equalsIgnoreCase("") )
                		dataset1files.setGMTFileName(value);
                else if(GMTFileNameTextField.getText().equalsIgnoreCase((String)e.getOldValue())){
                   //do nothing
                }
                else if(checkFile(value).equals(Color.RED)){
                    JOptionPane.showMessageDialog(application.getJFrame(),message,"File name change entered is not a valid file name",JOptionPane.WARNING_MESSAGE);
                    GMTFileNameTextField.setForeground(checkFile(value));
                }
               else
            	   		dataset1files.setGMTFileName(value);
            }else if (source == GCTFileName1TextField) {
                String value = GCTFileName1TextField.getText();
                if(value.equalsIgnoreCase("") ){
                		dataset1files.setExpressionFileName(value);
                    params.setData(false);
                }
                else if(GCTFileName1TextField.getText().equalsIgnoreCase((String)e.getOldValue())){
                   //do nothing
                }
                else if(checkFile(value).equals(Color.RED)){
                    JOptionPane.showMessageDialog(application.getJFrame(),message,"File name change entered is not a valid file name",JOptionPane.WARNING_MESSAGE);
                    GCTFileName1TextField.setForeground(checkFile(value));
                }
               else
            	   		dataset1files.setExpressionFileName(value);
            }else if (source == GCTFileName2TextField) {
                String value = GCTFileName2TextField.getText();
                if(value.equalsIgnoreCase("") ){
                		dataset2files.setExpressionFileName(value);                	
                    params.setData2(false);
                }
                else if(GCTFileName2TextField.getText().equalsIgnoreCase((String)e.getOldValue())){
                   //do nothing
                }
                else if(checkFile(value).equals(Color.RED)){
                    JOptionPane.showMessageDialog(application.getJFrame(),message,"File name change entered is not a valid file name",JOptionPane.WARNING_MESSAGE);
                    GCTFileName2TextField.setForeground(checkFile(value));
                }
               else{           	   
            		   dataset2files.setExpressionFileName(value);               		
               }   
            }else if (source == Dataset1FileNameTextField) {
                String value = Dataset1FileNameTextField.getText();
                if(value.equalsIgnoreCase("") )
                		dataset1files.setEnrichmentFileName1(value);
                 else if(Dataset1FileNameTextField.getText().equalsIgnoreCase((String)e.getOldValue())){
                   //do nothing
                }else if(checkFile(value).equals(Color.RED)){
                    JOptionPane.showMessageDialog(application.getJFrame(),message,"File name change entered is not a valid file name",JOptionPane.WARNING_MESSAGE);
                    Dataset1FileNameTextField.setForeground(checkFile(value));
                }
               else
            	   		dataset1files.setEnrichmentFileName1(value);
            }else if (source == Dataset1FileName2TextField) {
                String value = Dataset1FileName2TextField.getText();
                if(value.equalsIgnoreCase("") )
                		dataset1files.setEnrichmentFileName2(value);
                else if(Dataset1FileName2TextField.getText().equalsIgnoreCase((String)e.getOldValue())){
                   //do nothing
                }else if(checkFile(value).equals(Color.RED)){
                    JOptionPane.showMessageDialog(application.getJFrame(),message,"File name change entered is not a valid file name",JOptionPane.WARNING_MESSAGE);
                    Dataset1FileName2TextField.setForeground(checkFile(value));
                }
               else
            	   		dataset1files.setEnrichmentFileName2(value);
            }else if (source == Dataset2FileNameTextField) {
                String value = Dataset2FileNameTextField.getText();
                if(value.equalsIgnoreCase("") ){
                
                		dataset2files.setEnrichmentFileName1(value);
               		
                }
                else if(Dataset2FileNameTextField.getText().equalsIgnoreCase((String)e.getOldValue())){
                   //do nothing
                }else if(checkFile(value).equals(Color.RED)){
                    JOptionPane.showMessageDialog(application.getJFrame(),message,"File name change entered is not a valid file name",JOptionPane.WARNING_MESSAGE);
                    Dataset2FileNameTextField.setForeground(checkFile(value));
                }
               else{
            	   
            	   		dataset2files.setEnrichmentFileName1(value);
               		
               }
            }else if (source == Dataset2FileName2TextField) {
                String value = Dataset2FileName2TextField.getText();
                if(value.equalsIgnoreCase("") ){                	
                		dataset2files.setEnrichmentFileName2(value);
               		
                }
                else if(Dataset2FileName2TextField.getText().equalsIgnoreCase((String)e.getOldValue())){
                   //do nothing
                }else if(checkFile(value).equals(Color.RED)){
                    JOptionPane.showMessageDialog(application.getJFrame(),message,"File name change entered is not a valid file name",JOptionPane.WARNING_MESSAGE);
                    Dataset2FileName2TextField.setForeground(checkFile(value));
                }
               else{            	   
            		   dataset2files.setEnrichmentFileName2(value);             		
               }
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

        importButton.setText("Build");
        //TODO:Add action listern for build network
        importButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
            	//make sure that the minimum information is set in the current set of parameters
               
            		//create a new params for the new EM and add the dataset files to it
            		EnrichmentMapParameters new_params = new EnrichmentMapParameters(sessionManager,streamUtil);
            		new_params.copy(empanel.getParams());
            		new_params.addFiles(EnrichmentMap.DATASET1, dataset1files);
            		if(!dataset2files.isEmpty())
            			new_params.addFiles(EnrichmentMap.DATASET2, dataset2files);
            	
                EnrichmentMap map = new EnrichmentMap(new_params);
                
                //EnrichmentMapParseInputEvent parseInput = new EnrichmentMapParseInputEvent(empanel,map , dialog,  streamUtil);
                //parseInput.build();
                                
               	EnrichmentMapBuildMapEvent buildmap = new EnrichmentMapBuildMapEvent(map,  
                    			applicationManager,networkManager,networkViewManager,networkViewFactory,networkFactory,tableFactory,tableManager, 
                    			visualMappingManager,visualStyleFactory,
                    			vmfFactoryContinuous, vmfFactoryDiscrete,vmfFactoryPassthrough, dialog,  streamUtil,layoutManager,mapTableToNetworkTable);
                buildmap.build();
                 
            }
        });
        //importButton.addActionListener(new BuildEnrichmentMapActionListener(this));
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

    /*
     * Populate fields based on edb directory.
     * From an edb directory we can get gene_sets.gmt, results.edb, .rnk file, .cls file
     * To trigger this auto populate users need to select the results.edb from the edb directory
     * 
     * @param edbFile - the results.edb File
     * @param dataset1 - which dataset edb is specified for
     */
    private void populateFieldsFromEdb(File edbFile, boolean dataset1){
    	
    	String gmt = "";
    	String cls = "";
    	String rnk = "";
    	
    	String currentDir = edbFile.getParent();
        File temp = new File(currentDir, "gene_sets.gmt");
        if(temp.exists())
        		gmt = temp.getAbsolutePath();
        
        //get the cls file
        File[] filenames = edbFile.getParentFile().listFiles();
        for(int i = 0; i< filenames.length;i++){
        	if(filenames[i].getName().endsWith(".cls"))
        		cls = filenames[i].getAbsolutePath();
        	if(filenames[i].getName().endsWith(".rnk"))
        		rnk = filenames[i].getAbsolutePath();
        }
        
        if(dataset1){
            //check to see the file exists and can be read
            //check to see if the gmt file has already been set
            if(dataset1files.getGMTFileName() == null || dataset1files.getGMTFileName().equalsIgnoreCase("")){
                GMTFileNameTextField.setForeground(checkFile(gmt));
                GMTFileNameTextField.setText(gmt);
                dataset1files.setGMTFileName(gmt);
                GMTFileNameTextField.setToolTipText(gmt);
            }


            boolean AutoPopulate = true;
            if(!dataset1files.getGMTFileName().equalsIgnoreCase(gmt)){
                //maybe the files are the same but they are in different directories
                File currentGMTFilename = new File(dataset1files.getGMTFileName());
                File newGMTFilename = new File(gmt);
                if(!currentGMTFilename.getName().equalsIgnoreCase(newGMTFilename.getName())){
                    int answer = JOptionPane.showConfirmDialog(this,"This analysis GMT file does not match the previous dataset loaded.\n If you want to use the current GMT file but still use the new rpt file to populated fields press YES,\n If you want to change the GMT file to the one in this rpt file and populate the fields with the rpt file press NO,\n to choose a different rpt file press CANCEL\n","GMT files name mismatch", JOptionPane.YES_NO_CANCEL_OPTION);
                    if(answer == JOptionPane.NO_OPTION){
                        GMTFileNameTextField.setForeground(checkFile(gmt));
                        GMTFileNameTextField.setText(gmt);
                        dataset1files.setGMTFileName(gmt);
                        GMTFileNameTextField.setToolTipText(gmt);
                    }
                    else if(answer == JOptionPane.CANCEL_OPTION)
                        AutoPopulate = false;
                }
            }
            if(AutoPopulate){
                
                Dataset1RankFileTextField.setForeground(checkFile(rnk));
                Dataset1RankFileTextField.setText(rnk);
                dataset1files.setRankedFile(rnk);
                Dataset1RankFileTextField.setToolTipText(rnk);

                dataset1files.setEnrichmentFileName1(edbFile.getAbsolutePath());
                this.setDatasetnames(edbFile.getAbsolutePath(),"",dataset1);
            }
    }
    else{
       if(dataset1files.getGMTFileName() == null || dataset1files.getGMTFileName().equalsIgnoreCase("")){
            GMTFileNameTextField.setForeground(checkFile(gmt));
            GMTFileNameTextField.setText(gmt);
            dataset1files.setGMTFileName(gmt);
            GMTFileNameTextField.setToolTipText(gmt);
       }

        boolean AutoPopulate = true;
       if(!dataset1files.getGMTFileName().equalsIgnoreCase(gmt)){
          //maybe the files are the same but they are in different directories
          File currentGMTFilename = new File(dataset1files.getGMTFileName());
          File newGMTFilename = new File(gmt);
          if(!currentGMTFilename.getName().equalsIgnoreCase(newGMTFilename.getName())){
              int answer = JOptionPane.showConfirmDialog(this,"This analysis GMT file does not match the previous dataset loaded.\n If you want to use the current GMT file but still use the new rpt file to populated fields press YES,\n If you want to change the GMT file to the one in this rpt file and populate the fields with the rpt file press NO,\n to choose a different rpt file press CANCEL\n","GMT files name mismatch", JOptionPane.YES_NO_CANCEL_OPTION);
              if(answer == JOptionPane.NO_OPTION){
                GMTFileNameTextField.setForeground(checkFile(gmt));
                GMTFileNameTextField.setText(gmt);
                dataset1files.setGMTFileName(gmt);
                GMTFileNameTextField.setToolTipText(gmt);
              }
              else if(answer == JOptionPane.CANCEL_OPTION)
                        AutoPopulate = false;
           }
       }
       if(AutoPopulate){

            Dataset2RankFileTextField.setForeground(checkFile(rnk));
            Dataset2RankFileTextField.setText(rnk);
            dataset2files.setRankedFile(rnk);
            Dataset2RankFileTextField.setToolTipText(rnk);

            dataset2files.setEnrichmentFileName1(edbFile.getAbsolutePath());
            this.setDatasetnames(edbFile.getAbsolutePath(),"",dataset1);
       }
    }
  }
    
    /**
     * An rpt file can be entered instead of a GCT/expression file, or any of the enrichment results files
     * If an rpt file is specified all the fields in the dataset (expression file, enrichment results files, rank files,
     * phenotypes and class files) are populated.
     *
     * @param rptFile - rpt (GSEA analysis parameters file) file name
     * @param dataset1 - which dataset rpt was specified for.
     */
   private void populateFieldsFromRpt(File rptFile, boolean dataset1){

       if(dataset1)
    	   		LoadedFromRpt_dataset1 = true;
       else
    	   		LoadedFromRpt_dataset2 = true;
       try{
    	   	InputStream reader = streamUtil.getInputStream(rptFile.getAbsolutePath());
        String fullText = new Scanner(reader,"UTF-8").useDelimiter("\\A").next();
        //reader.read();
        //String fullText = reader.getText();

        //Create a hashmap to contain all the values in the rpt file.
        HashMap<String, String> rpt = new HashMap<String, String>();

        String []lines = fullText.split("\r\n?|\n");

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
        String timestamp = (String)rpt.get("producer_timestamp");               // timestamp produced by GSEA
        String method = (String)rpt.get("producer_class");                      
        method = method.split("\\p{Punct}")[2];                                 // Gsea or GseaPreranked
        String out_dir = (String)rpt.get("param out");                          // output dir in which the GSEA-Jobdirs are supposed to be created
        String job_dir_name = null;                                             // name of the GSEA Job dir (excluding  out_dir + File.separator )
        String data = (String)rpt.get("param res");
        String label = (String)rpt.get("param rpt_label");
        String classes = (String)rpt.get("param cls");
        String gmt = (String)rpt.get("param gmx");
        
        //instead of using the original gmt file use the gmt file that is stored in
        // in the edb directory.  The edb directory should be in the same directory 
        // as the rpt file.  If it isn't use the original mechanism of finding gmt file
        String currentDir = rptFile.getParent();
        File temp = new File(currentDir, "edb/gene_sets.gmt");
        if(temp.exists())
        		gmt = temp.getAbsolutePath();
        
        //String gmt_nopath =  gmt.substring(gmt.lastIndexOf(File.separator)+1, gmt.length()-1);
        String gseaHtmlReportFile = (String)rpt.get("file");
        
        String phenotype1 = "na";
        String phenotype2 = "na";
        //phenotypes are specified after # in the parameter cls and are separated by _versus_
        //but phenotypes are only specified for classic GSEA, not PreRanked.
        if(classes != null && method.equalsIgnoreCase("Gsea")){
            String[] classes_split = classes.split("#");
            
            //only and try parse classes out of label if they are there
            if(classes_split.length >= 2 ){
            
            		String phenotypes = classes_split[1];
            		String[] phenotypes_split = phenotypes.split("_versus_");
            		if(phenotypes_split.length >= 2){
            			phenotype1 = phenotypes_split[0];
            			phenotype2 = phenotypes_split[1];

            			if(dataset1){
            				dataset1files.setClassFile(classes_split[0]);
            				params.setDataset1Phenotype1(phenotype1);
            				params.setDataset1Phenotype2(phenotype2);

            				Dataset1Phenotype1TextField.setText(phenotype1);
            				Dataset1Phenotype1TextField.setValue(phenotype1);
            				Dataset1Phenotype2TextField.setText(phenotype2);
            				Dataset1Phenotype2TextField.setValue(phenotype2);
            			}
            			else{
            				dataset2files.setClassFile(classes_split[0]);
            				params.setDataset2Phenotype1(phenotype1);
            				params.setDataset2Phenotype2(phenotype2);

            				Dataset2Phenotype1TextField.setText(phenotype1);
            				Dataset2Phenotype2TextField.setText(phenotype2);
            				Dataset2Phenotype1TextField.setValue(phenotype1);
            				Dataset2Phenotype2TextField.setValue(phenotype2);
            			}
            		}
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
                Dataset1Phenotype1TextField.setValue(phenotype1);
                Dataset1Phenotype2TextField.setValue(phenotype2);
            }
            else{
                params.setDataset2Phenotype1(phenotype1);
                params.setDataset2Phenotype2(phenotype2);

                Dataset2Phenotype1TextField.setText(phenotype1);
                Dataset2Phenotype2TextField.setText(phenotype2);
                Dataset2Phenotype1TextField.setValue(phenotype1);
                Dataset2Phenotype2TextField.setValue(phenotype2);
            }

            /*XXX: BEGIN optional parameters for phenotypes and expression matrix in rpt file from pre-ranked GSEA:
             * 
             * To do less manual work while creating Enrichment Maps from pre-ranked GSEA, I add the following optional parameters:
             * 
             * param{tab}phenotypes{tab}{phenotype1}_versus_{phenotype2}
             * param{tab}expressionMatrix{tab}{path_to_GCT_or_TXT_formated_expression_matrix}
             * 
             * added by revilo 2010-03-18:
             */
            if (rpt.containsKey("param phenotypes")){
                String phenotypes = (String)rpt.get("param phenotypes");
                String[] phenotypes_split = phenotypes.split("_versus_");
                if (dataset1){
                    Dataset1Phenotype1TextField.setValue(phenotypes_split[0]);
                    Dataset1Phenotype2TextField.setValue(phenotypes_split[1]);
                }
                else{
                    Dataset2Phenotype1TextField.setValue(phenotypes_split[0]);
                    Dataset2Phenotype2TextField.setValue(phenotypes_split[1]);
                }
            }
            if (rpt.containsKey("param expressionMatrix")){
                data = (String)rpt.get("param expressionMatrix");
            }
            /*XXX: END optional parameters for phenotypes and expression matrix in rpt file from pre-ranked GSEA */

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
        job_dir_name = label + "."+ method + "." + timestamp;
        results1 = "" + out_dir + File.separator + job_dir_name + File.separator + "gsea_report_for_" + phenotype1 + "_" + timestamp + ".xls";
        results2 = "" + out_dir + File.separator + job_dir_name + File.separator + "gsea_report_for_" + phenotype2 + "_" + timestamp + ".xls";
        ranks = "" + out_dir + File.separator + job_dir_name + File.separator + "ranked_gene_list_" + phenotype1 + "_versus_" + phenotype2 +"_" + timestamp + ".xls";
        if(!((checkFile(results1) == Color.BLACK) && (checkFile(results2) == Color.BLACK) && (checkFile(ranks) == Color.BLACK))){
            String out_dir_new = rptFile.getAbsolutePath();
            out_dir_new = out_dir_new.substring(0, out_dir_new.lastIndexOf(File.separator)); // drop rpt-filename
            out_dir_new = out_dir_new.substring(0, out_dir_new.lastIndexOf(File.separator)); // drop gsea report folder
            
            if( !(out_dir_new.equalsIgnoreCase(out_dir)) ){

//                    //trim the last File Separator
//                    String new_dir = rptFile.getAbsolutePath().substring(0,rptFile.getAbsolutePath().lastIndexOf(File.separator));
                    results1 = out_dir_new + File.separator + job_dir_name + File.separator + "gsea_report_for_" + phenotype1 + "_" + timestamp + ".xls";
                    results2 = out_dir_new + File.separator + job_dir_name + File.separator + "gsea_report_for_" + phenotype2 + "_" + timestamp + ".xls";
                    ranks = out_dir_new + File.separator + job_dir_name + File.separator + "ranked_gene_list_" + phenotype1 + "_versus_" + phenotype2 +"_" + timestamp + ".xls";

                    //If after trying the directory that the rpt file is in doesn't produce valid file names, revert to what
                    //is specified in the rpt.
                    if(!((checkFile(results1) == Color.BLACK) && (checkFile(results2) == Color.BLACK) && (checkFile(ranks) == Color.BLACK))){
                        results1 = "" + out_dir + File.separator + job_dir_name + File.separator + label + "."+ method + "." + timestamp + File.separator + "gsea_report_for_" + phenotype1 + "_" + timestamp + ".xls";
                        results2 = "" + out_dir + File.separator + job_dir_name + File.separator + label + "."+ method + "." + timestamp + File.separator + "gsea_report_for_" + phenotype2 + "_" + timestamp + ".xls";
                        ranks = "" + out_dir + File.separator + job_dir_name + File.separator + label + "."+ method + "." + timestamp + File.separator + "ranked_gene_list_" + phenotype1 + "_versus_" + phenotype2 +"_" + timestamp + ".xls";
                    }
                    else{
                        out_dir = out_dir_new;
                        gseaHtmlReportFile = "" + out_dir + File.separator + job_dir_name + File.separator + "index.html";
                    }
            }

        }

        if(dataset1){
                //check to see the file exists and can be read
                //check to see if the gmt file has already been set
                if(dataset1files.getGMTFileName() == null || dataset1files.getGMTFileName().equalsIgnoreCase("")){
                    GMTFileNameTextField.setForeground(checkFile(gmt));
                    GMTFileNameTextField.setText(gmt);
                    dataset1files.setGMTFileName(gmt);
                    GMTFileNameTextField.setToolTipText(gmt);
                }


                boolean AutoPopulate = true;
                if(!dataset1files.getGMTFileName().equalsIgnoreCase(gmt)){
                    //maybe the files are the same but they are in different directories
                    File currentGMTFilename = new File(dataset1files.getGMTFileName());
                    File newGMTFilename = new File(gmt);
                    if(!currentGMTFilename.getName().equalsIgnoreCase(newGMTFilename.getName())){
                        int answer = JOptionPane.showConfirmDialog(this,"This analysis GMT file does not match the previous dataset loaded.\n If you want to use the current GMT file but still use the new rpt file to populated fields press YES,\n If you want to change the GMT file to the one in this rpt file and populate the fields with the rpt file press NO,\n to choose a different rpt file press CANCEL\n","GMT files name mismatch", JOptionPane.YES_NO_CANCEL_OPTION);
                        if(answer == JOptionPane.NO_OPTION){
                            GMTFileNameTextField.setForeground(checkFile(gmt));
                            GMTFileNameTextField.setText(gmt);
                            dataset1files.setGMTFileName(gmt);
                            GMTFileNameTextField.setToolTipText(gmt);
                        }
                        else if(answer == JOptionPane.CANCEL_OPTION)
                            AutoPopulate = false;
                    }
                }
                if(AutoPopulate){
                    GCTFileName1TextField.setForeground(checkFile(data));
                    GCTFileName1TextField.setText(data);
                    dataset1files.setExpressionFileName(data);
                    params.setData(true);
                    GCTFileName1TextField.setToolTipText(data);

                    Dataset1RankFileTextField.setForeground(checkFile(ranks));
                    Dataset1RankFileTextField.setText(ranks);
                    dataset1files.setRankedFile(ranks);
                    Dataset1RankFileTextField.setToolTipText(ranks);

                    dataset1files.setEnrichmentFileName1(results1);
                    dataset1files.setEnrichmentFileName2(results2);
                    dataset1files.setGseaHtmlReportFile(gseaHtmlReportFile);
                    this.setDatasetnames(results1,results2,dataset1);
                }
        }
        else{
           if(dataset1files.getGMTFileName() == null || dataset1files.getGMTFileName().equalsIgnoreCase("")){
                GMTFileNameTextField.setForeground(checkFile(gmt));
                GMTFileNameTextField.setText(gmt);
                dataset1files.setGMTFileName(gmt);
                GMTFileNameTextField.setToolTipText(gmt);
           }

            boolean AutoPopulate = true;
           if(!dataset1files.getGMTFileName().equalsIgnoreCase(gmt)){
              //maybe the files are the same but they are in different directories
              File currentGMTFilename = new File(dataset1files.getGMTFileName());
              File newGMTFilename = new File(gmt);
              if(!currentGMTFilename.getName().equalsIgnoreCase(newGMTFilename.getName())){
                  int answer = JOptionPane.showConfirmDialog(this,"This analysis GMT file does not match the previous dataset loaded.\n If you want to use the current GMT file but still use the new rpt file to populated fields press YES,\n If you want to change the GMT file to the one in this rpt file and populate the fields with the rpt file press NO,\n to choose a different rpt file press CANCEL\n","GMT files name mismatch", JOptionPane.YES_NO_CANCEL_OPTION);
                  if(answer == JOptionPane.NO_OPTION){
                    GMTFileNameTextField.setForeground(checkFile(gmt));
                    GMTFileNameTextField.setText(gmt);
                    dataset1files.setGMTFileName(gmt);
                    GMTFileNameTextField.setToolTipText(gmt);
                  }
                  else if(answer == JOptionPane.CANCEL_OPTION)
                            AutoPopulate = false;
               }
           }
           if(AutoPopulate){
        	   
        	   
                GCTFileName2TextField.setForeground(checkFile(data));
                GCTFileName2TextField.setText(data);
                dataset2files.setExpressionFileName(data);
                params.setData2(true);
                GCTFileName2TextField.setToolTipText(data);

                Dataset2RankFileTextField.setForeground(checkFile(ranks));
                Dataset2RankFileTextField.setText(ranks);
                dataset2files.setRankedFile(ranks);
                Dataset2RankFileTextField.setToolTipText(ranks);

                dataset2files.setEnrichmentFileName1(results1);
                dataset2files.setEnrichmentFileName2(results2);
                dataset2files.setGseaHtmlReportFile(gseaHtmlReportFile);
//                params.setDataset2RankedFile(ranks);
                this.setDatasetnames(results1,results2,dataset1);
           }
        }
       }catch (IOException ie){
    	   		System.out.println("unable to open rpt file: " + rptFile);
       }
    }

    /**
     * Sets the textfields for results file 1 and 2 for specified dataset
     *
     * @param file1 - enrichment results file 1 name
     * @param file2 - enrichment results file 2 name
     * @param dataset1 - which dataset (1 or 2) the files are specific for.
     */
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

    /**
     * Check to see if the file is readable.  returns a color indicating whether the file is readable.  Color is red
     * if the file is not readable so we can set the font color to red to show the user the file name was invalid.
     *
     * @param filename - name of file to checked
     * @return Color, red if the file is not readable and black if it is.
     */
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

    /**
     * Change the analysis type (either GSEA or Generic)
     * When the analysis type is changed the interface needs to be cleared and updated.
     *
     * @param evt
     */
  private void selectAnalysisTypeActionPerformed(ActionEvent evt){
      String analysisType = evt.getActionCommand();

      if(analysisType.equalsIgnoreCase(EnrichmentMapParameters.method_GSEA))
          params.setMethod(EnrichmentMapParameters.method_GSEA);
      else if(analysisType.equalsIgnoreCase(EnrichmentMapParameters.method_generic))
          params.setMethod(EnrichmentMapParameters.method_generic);
      else if(analysisType.equalsIgnoreCase(EnrichmentMapParameters.method_DAVID))
          params.setMethod(EnrichmentMapParameters.method_DAVID);

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

    /**
     * Given a set of parameters, update the panel to contain the values that are
     * defined in this set of parameters.  This methos is used when the type of analysis is
     * changed (gsea to generic or vice versa).  The user wants what ever info they have already
     * entered to transfered over even though they changed the type of analysis
     *
     * @param params - enrichment map paramters to use to update the panel
     */
  private void UpdatePanel(EnrichmentMapParameters params){
	  if(params.getFiles().containsKey(EnrichmentMap.DATASET1))
		  this.dataset1files = params.getFiles().get(EnrichmentMap.DATASET1);
	  if(params.getFiles().containsKey(EnrichmentMap.DATASET2))
		  this.dataset2files = params.getFiles().get(EnrichmentMap.DATASET2);
      //check to see if the user had already entered anything into the newly created Dataset Frame
      if(dataset1files.getEnrichmentFileName1()!=null){
        Dataset1FileNameTextField.setText(dataset1files.getEnrichmentFileName1());
        Dataset1FileNameTextField.setToolTipText(dataset1files.getEnrichmentFileName1());
      }
      
      if(dataset1files.getExpressionFileName() != null){
          GCTFileName1TextField.setText(dataset1files.getExpressionFileName());
          GCTFileName1TextField.setToolTipText(dataset1files.getExpressionFileName());
      }
      
      if(dataset1files.getRankedFile() != null){
          Dataset1RankFileTextField.setText(dataset1files.getRankedFile());
          Dataset1RankFileTextField.setToolTipText(dataset1files.getRankedFile());
      }
      
     
    	  if(dataset2files.getEnrichmentFileName1()!=null){
    		  Dataset2FileNameTextField.setText(dataset2files.getEnrichmentFileName1());
    		  Dataset2FileNameTextField.setToolTipText(dataset2files.getEnrichmentFileName1());
    	  }
    	  if(dataset2files.getExpressionFileName() != null){
    		  GCTFileName2TextField.setText(dataset2files.getExpressionFileName());
    		  GCTFileName2TextField.setToolTipText(dataset2files.getExpressionFileName());
    	  }  
    	  if(dataset2files.getRankedFile() != null){
    		  Dataset2RankFileTextField.setText(dataset2files.getRankedFile());
    		  Dataset2RankFileTextField.setToolTipText(dataset2files.getRankedFile());
    	  }
    	  
    	//Special case with Enrichment results file 2 (there should only be two enrichment
          //Files if the analysis specified is GSEA.  If the user has loaded from an RPT and
          //then changes the type of analysis there shouldn't be an extra file
          if(params.getMethod().equalsIgnoreCase(EnrichmentMapParameters.method_GSEA)){
                if(dataset2files.getEnrichmentFileName2()!=null){
                    Dataset2FileName2TextField.setText(dataset2files.getEnrichmentFileName2());
                    Dataset2FileName2TextField.setToolTipText(dataset2files.getEnrichmentFileName2());
                }
          }
          else{
                if( (dataset2files.getEnrichmentFileName2()!=null)){                    
                    if(dataset2files.getEnrichmentFileName2()!=null)
                    		dataset2files.setEnrichmentFileName2(null);

                }
          }
      

      //update the phenotypes
      if(params.getDataset1Phenotype1() != null){
          Dataset1Phenotype1TextField.setText(params.getDataset1Phenotype1());
          Dataset1Phenotype1TextField.setValue(params.getDataset1Phenotype1());
          Dataset1Phenotype1TextField.setToolTipText(params.getDataset1Phenotype1());
      }
      if(params.getDataset1Phenotype2() != null){
          Dataset1Phenotype2TextField.setText(params.getDataset1Phenotype2());
          Dataset1Phenotype2TextField.setValue(params.getDataset1Phenotype2());
          Dataset1Phenotype2TextField.setToolTipText(params.getDataset1Phenotype2());
      }
      if(params.getDataset2Phenotype1() != null){
          Dataset2Phenotype1TextField.setText(params.getDataset2Phenotype1());
          Dataset2Phenotype1TextField.setValue(params.getDataset2Phenotype1());
          Dataset2Phenotype1TextField.setToolTipText(params.getDataset2Phenotype1());
      }
      if(params.getDataset2Phenotype2() != null){
          Dataset2Phenotype2TextField.setText(params.getDataset2Phenotype2());
          Dataset2Phenotype2TextField.setValue(params.getDataset2Phenotype2());
          Dataset2Phenotype2TextField.setToolTipText(params.getDataset2Phenotype2());
      }

      //Special case with Enrichment results file 2 (there should only be two enrichment
      //Files if the analysis specified is GSEA.  If the user has loaded from an RPT and
      //then changes the type of analysis there shouldn't be an extra file
      if(params.getMethod().equalsIgnoreCase(EnrichmentMapParameters.method_GSEA)){
           if(dataset1files.getEnrichmentFileName2()!=null){
                Dataset1FileName2TextField.setText(dataset1files.getEnrichmentFileName2());
                Dataset1FileName2TextField.setToolTipText(dataset1files.getEnrichmentFileName2());
            }            
      }
      else{
            if((dataset1files.getEnrichmentFileName2()!=null) ){
                JOptionPane.showMessageDialog(this,"Running Enrichment Map with Generic input " +
                        "allows for only one enrichment results file.\n  The second file specified has been removed.");
                if(dataset1files.getEnrichmentFileName2()!=null)
                		dataset1files.setEnrichmentFileName2(null);

            }
      }
  }

    //Action listeners for buttons in input panel

    /**
     * jaccard or overlap radio button action listener
     *
     * @param evt
     */
    private void selectJaccardOrOverlapActionPerformed(java.awt.event.ActionEvent evt) {
        if(evt.getActionCommand().equalsIgnoreCase("jaccard")){
            params.setSimilarityMetric(EnrichmentMapParameters.SM_JACCARD);
            if ( ! similarityCutOffChanged ) {
                params.setSimilarityCutOff( params.getDefaultJaccardCutOff() );
//                coeffecientTextField.setText( Double.toString(params.getSimilarityCutOff()) );
                coeffecientTextField.setValue( params.getSimilarityCutOff() );
                similarityCutOffChanged = false;; //reset after .setValue(...) wrongly changed it to "true"
            }
        }
     else if(evt.getActionCommand().equalsIgnoreCase("overlap")){
            params.setSimilarityMetric(EnrichmentMapParameters.SM_OVERLAP);
            if ( ! similarityCutOffChanged ) {
                params.setSimilarityCutOff(params.getDefaultOverlapCutOff());
//                coeffecientTextField.setText( Double.toString(params.getSimilarityCutOff()) );
                coeffecientTextField.setValue( params.getSimilarityCutOff() );
                similarityCutOffChanged = false;; //reset after .setValue(...) wrongly changed it to "true"
          }
        }
        else if(evt.getActionCommand().equalsIgnoreCase("combined")){
            params.setSimilarityMetric(EnrichmentMapParameters.SM_COMBINED);
            if ( ! similarityCutOffChanged ) {
                params.setSimilarityCutOff((params.getDefaultOverlapCutOff() * params.getCombinedConstant()) + ((1-params.getCombinedConstant()) * params.getDefaultJaccardCutOff()) );
//                coeffecientTextField.setText( Double.toString(params.getSimilarityCutOff()) );
                coeffecientTextField.setValue( params.getSimilarityCutOff() );
                similarityCutOffChanged = false;; //reset after .setValue(...) wrongly changed it to "true"
          }
        }
     else{
            JOptionPane.showMessageDialog(this,"Invalid Jaccard Radio Button action command");
        }
    }

    /**
     * gene set (gmt) file selector action listener
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
               dataset1files.setGMTFileName(file.getAbsolutePath());
               GMTFileNameTextField.setToolTipText(file.getAbsolutePath());
           }
       }

    /**
     * gct/expression 1 file selector action listener
     *
     * @param evt
     */
      private void selectGCTFileButtonActionPerformed(
               java.awt.event.ActionEvent evt) {

    	  // Create FileFilter
          FileChooserFilter filter_gct = new FileChooserFilter("gct Files","gct" );          
          FileChooserFilter filter_rpt = new FileChooserFilter("rpt Files","rpt" );
          FileChooserFilter filter_rnk = new FileChooserFilter("rnk Files","rnk" );
          FileChooserFilter filter_txt = new FileChooserFilter("txt Files","txt" );
          FileChooserFilter filter_edb = new FileChooserFilter("edb Files","edb" );
          
          //the set of filter (required by the file util method
          ArrayList<FileChooserFilter> all_filters = new ArrayList<FileChooserFilter>();
          all_filters.add(filter_gct);
          all_filters.add(filter_rpt);
          all_filters.add(filter_rnk);
          all_filters.add(filter_txt);
          all_filters.add(filter_edb);
                     
           // Get the file name
           File file = fileUtil.getFile(EnrichmentMapUtils.getWindowInstance(this),"Import GCT File", FileUtil.LOAD, all_filters);
           if(file != null) {

               if(file.getPath().contains(".rpt")){
                   //The file loaded is an rpt file --> populate the fields based on the
                   populateFieldsFromRpt(file,true);

               }
               else if(file.getPath().endsWith(".edb")){
                   //The file loaded is an rpt file --> populate the fields based on the
                   populateFieldsFromEdb(file,true);
               }
               else{
                    GCTFileName1TextField.setForeground(checkFile(file.getAbsolutePath()));
                    GCTFileName1TextField.setText(file.getAbsolutePath());
                    dataset1files.setExpressionFileName(file.getAbsolutePath());
                    GCTFileName1TextField.setToolTipText(file.getAbsolutePath());
               }
               params.setData(true);

           }
       }

    /**
     * gct/expression 2 file selector action listener
     *
     * @param evt
     */
    private void selectGCTFileButton2ActionPerformed(
             java.awt.event.ActionEvent evt) {

    	 // Create FileFilter
        FileChooserFilter filter_gct = new FileChooserFilter("gct Files","gct" );          
        FileChooserFilter filter_rpt = new FileChooserFilter("rpt Files","rpt" );
        FileChooserFilter filter_rnk = new FileChooserFilter("rnk Files","rnk" );
        FileChooserFilter filter_txt = new FileChooserFilter("txt Files","txt" );
        FileChooserFilter filter_edb = new FileChooserFilter("edb Files","edb" );
        
        //the set of filter (required by the file util method
        ArrayList<FileChooserFilter> all_filters = new ArrayList<FileChooserFilter>();
        all_filters.add(filter_gct);
        all_filters.add(filter_rpt);
        all_filters.add(filter_rnk);
        all_filters.add(filter_txt);
        all_filters.add(filter_edb);
                   
         // Get the file name
         File file = fileUtil.getFile(EnrichmentMapUtils.getWindowInstance(this),"Import GCT File", FileUtil.LOAD, all_filters);
         if(file != null) {
             if(file.getPath().contains(".rpt")){
                      //The file loaded is an rpt file --> populate the fields based on the
                      populateFieldsFromRpt(file,false);
                  }
             else if(file.getPath().endsWith(".edb")){
                 //The file loaded is an rpt file --> populate the fields based on the
                 populateFieldsFromEdb(file,false);
             }
             else{
               GCTFileName2TextField.setForeground(checkFile(file.getAbsolutePath()));
               GCTFileName2TextField.setText(file.getAbsolutePath());
               //check to see that there is a dataset2
               
               dataset2files.setExpressionFileName(file.getAbsolutePath());
               
               GCTFileName2TextField.setToolTipText(file.getAbsolutePath());
             }
             params.setTwoDatasets(true);
             params.setData2(true);
         }
     }

    /**
     * enrichment results 1 file selector action listener
     *
     * @param evt
     */
     private void selectDataset1FileButtonActionPerformed(
               java.awt.event.ActionEvent evt) {

    	 // Create FileFilter
         FileChooserFilter filter_xls = new FileChooserFilter("gct Files","xls" );          
         FileChooserFilter filter_rpt = new FileChooserFilter("rpt Files","rpt" );
         FileChooserFilter filter_bgo = new FileChooserFilter("rnk Files","bgo" );
         FileChooserFilter filter_txt = new FileChooserFilter("txt Files","txt" );
         FileChooserFilter filter_edb = new FileChooserFilter("edb Files","edb" );
         
         //the set of filter (required by the file util method
         ArrayList<FileChooserFilter> all_filters = new ArrayList<FileChooserFilter>();
         all_filters.add(filter_xls);
         all_filters.add(filter_rpt);
         all_filters.add(filter_bgo);
         all_filters.add(filter_txt);
         all_filters.add(filter_edb);
                    
          // Get the file name
          File file = fileUtil.getFile(EnrichmentMapUtils.getWindowInstance(this),"Import dataset result File", FileUtil.LOAD, all_filters);

        if(file != null) {
             if(file.getPath().contains(".rpt")){
                      //The file loaded is an rpt file --> populate the fields based on the
                      populateFieldsFromRpt(file,true);

                  }
             else if(file.getPath().endsWith(".edb")){
                 //The file loaded is an rpt file --> populate the fields based on the
                 populateFieldsFromEdb(file,true);
             }
             else{
                Dataset1FileNameTextField.setForeground(checkFile(file.getAbsolutePath()));
                Dataset1FileNameTextField.setText(file.getAbsolutePath() );
                dataset1files.setEnrichmentFileName1(file.getAbsolutePath());
                Dataset1FileNameTextField.setToolTipText(file.getAbsolutePath() );
             }

        }
    }

    /**
     * enrichment results 2 file selector action listener
     *
     * @param evt
     */
    private void selectDataset1File2ButtonActionPerformed(
               java.awt.event.ActionEvent evt) {

    	 // Create FileFilter
        FileChooserFilter filter_xls = new FileChooserFilter("gct Files","xls" );          
        FileChooserFilter filter_rpt = new FileChooserFilter("rpt Files","rpt" );
        FileChooserFilter filter_bgo = new FileChooserFilter("rnk Files","bgo" );
        FileChooserFilter filter_txt = new FileChooserFilter("txt Files","txt" );
        FileChooserFilter filter_edb = new FileChooserFilter("edb Files","edb" );
        
        //the set of filter (required by the file util method
        ArrayList<FileChooserFilter> all_filters = new ArrayList<FileChooserFilter>();
        all_filters.add(filter_xls);
        all_filters.add(filter_rpt);
        all_filters.add(filter_bgo);
        all_filters.add(filter_txt);
        all_filters.add(filter_edb);
                   
         // Get the file name
         File file = fileUtil.getFile(EnrichmentMapUtils.getWindowInstance(this),"Import dataset result File", FileUtil.LOAD, all_filters);

        if(file != null) {
             if(file.getPath().contains(".rpt")){
                      //The file loaded is an rpt file --> populate the fields based on the
                      populateFieldsFromRpt(file,true);
                  }
             else if(file.getPath().endsWith(".edb")){
                 //The file loaded is an rpt file --> populate the fields based on the
                 populateFieldsFromEdb(file,true);
             }
             else{
                Dataset1FileName2TextField.setForeground(checkFile(file.getAbsolutePath()));
                Dataset1FileName2TextField.setText(file.getAbsolutePath() );
                dataset1files.setEnrichmentFileName2(file.getAbsolutePath());
                Dataset1FileName2TextField.setToolTipText(file.getAbsolutePath() );
             }

        }
    }

    /**
     * enrichment results 1 file selector action listener
     *
     * @param evt
     */
    private void selectDataset2FileButtonActionPerformed(
             java.awt.event.ActionEvent evt) {

    	 // Create FileFilter
        FileChooserFilter filter_xls = new FileChooserFilter("gct Files","xls" );          
        FileChooserFilter filter_rpt = new FileChooserFilter("rpt Files","rpt" );
        FileChooserFilter filter_bgo = new FileChooserFilter("rnk Files","bgo" );
        FileChooserFilter filter_txt = new FileChooserFilter("txt Files","txt" );
        FileChooserFilter filter_edb = new FileChooserFilter("edb Files","edb" );
        
        //the set of filter (required by the file util method
        ArrayList<FileChooserFilter> all_filters = new ArrayList<FileChooserFilter>();
        all_filters.add(filter_xls);
        all_filters.add(filter_rpt);
        all_filters.add(filter_bgo);
        all_filters.add(filter_txt);
        all_filters.add(filter_edb);
                   
         // Get the file name
         File file = fileUtil.getFile(EnrichmentMapUtils.getWindowInstance(this),"Import dataset result File", FileUtil.LOAD, all_filters);

      if(file != null) {
           if(file.getPath().contains(".rpt")){
                      //The file loaded is an rpt file --> populate the fields based on the
                      populateFieldsFromRpt(file,false);
                  }
           else if(file.getPath().endsWith(".edb")){
               //The file loaded is an rpt file --> populate the fields based on the
               populateFieldsFromEdb(file,false);
           }
             else{
              Dataset2FileNameTextField.setForeground(checkFile(file.getAbsolutePath()));
              Dataset2FileNameTextField.setText(file.getAbsolutePath() );
              
              dataset2files.setEnrichmentFileName1(file.getAbsolutePath());
              
              Dataset2FileNameTextField.setToolTipText(file.getAbsolutePath() );
           }
           params.setTwoDatasets(true);
      }
  }
    /**
     * enrichment results 2 file selector action listener
     *
     * @param evt
     */
     private void selectDataset2File2ButtonActionPerformed(
             java.awt.event.ActionEvent evt) {

    	 // Create FileFilter
         FileChooserFilter filter_xls = new FileChooserFilter("gct Files","xls" );          
         FileChooserFilter filter_rpt = new FileChooserFilter("rpt Files","rpt" );
         FileChooserFilter filter_bgo = new FileChooserFilter("rnk Files","bgo" );
         FileChooserFilter filter_txt = new FileChooserFilter("txt Files","txt" );
         FileChooserFilter filter_edb = new FileChooserFilter("edb Files","edb" );
         
         //the set of filter (required by the file util method
         ArrayList<FileChooserFilter> all_filters = new ArrayList<FileChooserFilter>();
         all_filters.add(filter_xls);
         all_filters.add(filter_rpt);
         all_filters.add(filter_bgo);
         all_filters.add(filter_txt);
         all_filters.add(filter_edb);
                    
          // Get the file name
          File file = fileUtil.getFile(EnrichmentMapUtils.getWindowInstance(this),"Import dataset result File", FileUtil.LOAD, all_filters);

      if(file != null) {
           if(file.getPath().contains(".rpt")){
                      //The file loaded is an rpt file --> populate the fields based on the
                      populateFieldsFromRpt(file,false);

                  }
           else if(file.getPath().endsWith(".edb")){
               //The file loaded is an rpt file --> populate the fields based on the
               populateFieldsFromEdb(file,false);
           }
             else{
              Dataset2FileName2TextField.setForeground(checkFile(file.getAbsolutePath()));
              Dataset2FileName2TextField.setText(file.getAbsolutePath() );
              
              dataset2files.setEnrichmentFileName2(file.getAbsolutePath());
            
              Dataset2FileName2TextField.setToolTipText(file.getAbsolutePath() );
           }
           params.setTwoDatasets(true);
      }
  }

     /**
     * ranks 1 file selector action listener
     *
     * @param evt
     */
     private void selectRank1FileButtonActionPerformed(
               java.awt.event.ActionEvent evt) {

         //For GSEA input, Check to see if there is already a rank file defined and if it was from the rpt
         if(params.getMethod().equalsIgnoreCase(EnrichmentMapParameters.method_GSEA) &&
        		 	LoadedFromRpt_dataset1 && !Dataset1RankFileTextField.getText().equalsIgnoreCase(""))
             JOptionPane.showMessageDialog(application.getJFrame(),"GSEA defined rank file is in a specific order and is used to calculate the leading edge.  \n If you change this file the leading edges will be calculated incorrectly.","Trying to change pre-defined GSEA rank file",JOptionPane.WARNING_MESSAGE);

         // Create FileFilter
         FileChooserFilter filter_rnk = new FileChooserFilter("rnk Files","rnk" );          
         FileChooserFilter filter_txt = new FileChooserFilter("txt Files","txt" );
         
         //the set of filter (required by the file util method
         ArrayList<FileChooserFilter> all_filters = new ArrayList<FileChooserFilter>();
         all_filters.add(filter_rnk);
         all_filters.add(filter_txt);

                    
          // Get the file name
          File file = fileUtil.getFile(EnrichmentMapUtils.getWindowInstance(this),"Import rank File", FileUtil.LOAD, all_filters);

        if(file != null) {
                Dataset1RankFileTextField.setForeground(checkFile(file.getAbsolutePath()));
                Dataset1RankFileTextField.setText(file.getAbsolutePath() );
                dataset1files.setRankedFile(file.getAbsolutePath());
                Dataset1RankFileTextField.setToolTipText(file.getAbsolutePath() );


        }
    }

    /**
     * ranks 2 file selector action listener
     *
     * @param evt
     */
     private void selectRank2FileButtonActionPerformed(
               java.awt.event.ActionEvent evt) {

        //For GSEA input, Check to see if there is already a rank file defined and if it was from the rpt
         if(params.getMethod().equalsIgnoreCase(EnrichmentMapParameters.method_GSEA) &&
        		 	LoadedFromRpt_dataset2 && !Dataset2RankFileTextField.getText().equalsIgnoreCase(""))
             JOptionPane.showMessageDialog(application.getJFrame(),"GSEA defined rank file is in a specific order and is used to calculate the leading edge.  \n If you change this file the leading edges will be calculated incorrectly.","Trying to change pre-defined GSEA rank file",JOptionPane.WARNING_MESSAGE);


      // Create FileFilter
         FileChooserFilter filter_rnk = new FileChooserFilter("rnk Files","rnk" );          
         FileChooserFilter filter_txt = new FileChooserFilter("txt Files","txt" );
         
         //the set of filter (required by the file util method
         ArrayList<FileChooserFilter> all_filters = new ArrayList<FileChooserFilter>();
         all_filters.add(filter_rnk);
         all_filters.add(filter_txt);

                    
          // Get the file name
          File file = fileUtil.getFile(EnrichmentMapUtils.getWindowInstance(this),"Import rank File", FileUtil.LOAD, all_filters);

        if(file != null) {
                Dataset2RankFileTextField.setForeground(checkFile(file.getAbsolutePath()));
                Dataset2RankFileTextField.setText(file.getAbsolutePath() );

                	dataset2files.setRankedFile(file.getAbsolutePath());
                
                Dataset2RankFileTextField.setToolTipText(file.getAbsolutePath() );


        }
    }

    /**
     *  Clear the current panel and clear the params associated with this panel
     */
    private void resetPanel(){

        this.params = new EnrichmentMapParameters();

        GMTFileNameTextField.setText("");
        GMTFileNameTextField.setToolTipText(null);

        GCTFileName1TextField.setText("");
        GCTFileName1TextField.setToolTipText(null);
        GCTFileName2TextField.setText("");
        GCTFileName2TextField.setToolTipText(null);

        Dataset1FileNameTextField.setText("");
        Dataset1FileNameTextField.setToolTipText(null);
        Dataset1FileName2TextField.setText("");
        Dataset1FileName2TextField.setToolTipText(null);

        Dataset2FileNameTextField.setText("");
        Dataset2FileNameTextField.setToolTipText(null);
        Dataset2FileName2TextField.setText("");
        Dataset2FileName2TextField.setToolTipText(null);

        Dataset1RankFileTextField.setText("");
        Dataset1RankFileTextField.setToolTipText(null);
        Dataset2RankFileTextField.setText("");
        Dataset2RankFileTextField.setToolTipText(null);

        Dataset1Phenotype1TextField.setText(params.getDataset1Phenotype1());
        Dataset1Phenotype2TextField.setText(params.getDataset1Phenotype2());
        Dataset2Phenotype1TextField.setText(params.getDataset2Phenotype1());
        Dataset2Phenotype2TextField.setText(params.getDataset2Phenotype2());

        Dataset1Phenotype1TextField.setValue(params.getDataset1Phenotype1());
        Dataset1Phenotype2TextField.setValue(params.getDataset1Phenotype2());
        Dataset2Phenotype1TextField.setValue(params.getDataset2Phenotype1());
        Dataset2Phenotype2TextField.setValue(params.getDataset2Phenotype2());

        pvalueTextField.setText(Double.toString(params.getPvalue()));
        qvalueTextField.setText(Double.toString(params.getQvalue()));
        coeffecientTextField.setText(Double.toString(params.getSimilarityCutOff()));

        pvalueTextField.setValue(params.getPvalue());
        qvalueTextField.setValue(params.getQvalue());
        coeffecientTextField.setValue(params.getSimilarityCutOff());
        //reset for cleared Panel after .setValue(...) wrongly changed it to "true"
        similarityCutOffChanged = false;

        if(params.getMethod().equalsIgnoreCase(EnrichmentMapParameters.method_GSEA)){
            gsea.setSelected(true);
            generic.setSelected(false);
            david.setSelected(false);
        }
        else if(params.getMethod().equalsIgnoreCase(EnrichmentMapParameters.method_generic)){
            gsea.setSelected(false);
            generic.setSelected(true);
            david.setSelected(false);
        }
        else if(params.getMethod().equalsIgnoreCase(EnrichmentMapParameters.method_DAVID)){
            gsea.setSelected(false);
            generic.setSelected(false);
            david.setSelected(true);
        }

        if(params.getSimilarityMetric().equalsIgnoreCase(EnrichmentMapParameters.SM_JACCARD)){
            jaccard.setSelected(true);
            overlap.setSelected(false);
            combined.setSelected(false);
        }
        else if(params.getSimilarityMetric().equalsIgnoreCase(EnrichmentMapParameters.SM_OVERLAP)){
            jaccard.setSelected(false);
            overlap.setSelected(true);
            combined.setSelected(false);
        }  else if(params.getSimilarityMetric().equalsIgnoreCase(EnrichmentMapParameters.SM_COMBINED)){
            jaccard.setSelected(false);
            overlap.setSelected(false);
            combined.setSelected(true);
        }

    }

    /**
     * Given a set of parameters, update the panel to contain the values that are
     * defined in this set of parameters.  
     *
     * @param current_params - enrichment map paramters to use to update the panel
     */
    public void updateContents(EnrichmentMapParameters current_params){
        this.params = new EnrichmentMapParameters();
        this.params.copy(current_params);
        
        if(params.getFiles().containsKey(EnrichmentMap.DATASET1))
  		  	this.dataset1files = params.getFiles().get(EnrichmentMap.DATASET1);
  	  	if(params.getFiles().containsKey(EnrichmentMap.DATASET2))
  	  		this.dataset2files = params.getFiles().get(EnrichmentMap.DATASET2);

        GMTFileNameTextField.setText(dataset1files.getGMTFileName());
        GCTFileName1TextField.setText(dataset1files.getExpressionFileName());
        Dataset1FileNameTextField.setText(dataset1files.getEnrichmentFileName1());
        Dataset1FileName2TextField.setText(dataset1files.getEnrichmentFileName2());
        Dataset1RankFileTextField.setText(dataset1files.getRankedFile());

        
        	GCTFileName2TextField.setText(dataset2files.getExpressionFileName());
        	Dataset2FileNameTextField.setText(dataset2files.getEnrichmentFileName1());
        	Dataset2FileName2TextField.setText(dataset2files.getEnrichmentFileName2());
        	Dataset2RankFileTextField.setText(dataset2files.getRankedFile());
        

        Dataset1Phenotype1TextField.setText(current_params.getDataset1Phenotype1());
        Dataset1Phenotype2TextField.setText(current_params.getDataset1Phenotype2());
        Dataset2Phenotype1TextField.setText(current_params.getDataset2Phenotype1());
        Dataset2Phenotype2TextField.setText(current_params.getDataset2Phenotype2());

        Dataset1Phenotype1TextField.setValue(current_params.getDataset1Phenotype1());
        Dataset1Phenotype2TextField.setValue(current_params.getDataset1Phenotype2());
        Dataset2Phenotype1TextField.setValue(current_params.getDataset2Phenotype1());
        Dataset2Phenotype2TextField.setValue(current_params.getDataset2Phenotype2());

        pvalueTextField.setText(Double.toString(current_params.getPvalue()));
        qvalueTextField.setText(Double.toString(current_params.getQvalue()));
        coeffecientTextField.setText(Double.toString(current_params.getSimilarityCutOff()));

        pvalueTextField.setValue(current_params.getPvalue());
        qvalueTextField.setValue(current_params.getQvalue());
        coeffecientTextField.setValue(current_params.getSimilarityCutOff());
        combinedConstantTextField.setValue(current_params.getCombinedConstant());

        if(current_params.getMethod().equalsIgnoreCase(EnrichmentMapParameters.method_GSEA)){
            gsea.setSelected(true);
            generic.setSelected(false);
            david.setSelected(false);
        }
        else if(current_params.getMethod().equalsIgnoreCase(EnrichmentMapParameters.method_generic)){
            gsea.setSelected(false);
            generic.setSelected(true);
            david.setSelected(false);
        }
        else if(current_params.getMethod().equalsIgnoreCase(EnrichmentMapParameters.method_DAVID)){
            gsea.setSelected(false);
            generic.setSelected(false);
            david.setSelected(true);
        }

        if(params.getSimilarityMetric().equalsIgnoreCase(EnrichmentMapParameters.SM_JACCARD)){
            jaccard.setSelected(true);
            overlap.setSelected(false);
            combined.setSelected(false);
        }
        else if(params.getSimilarityMetric().equalsIgnoreCase(EnrichmentMapParameters.SM_OVERLAP)){
            jaccard.setSelected(false);
            overlap.setSelected(true);
            combined.setSelected(false);
        }  else if(params.getSimilarityMetric().equalsIgnoreCase(EnrichmentMapParameters.SM_COMBINED)){
            jaccard.setSelected(false);
            overlap.setSelected(false);
            combined.setSelected(true);
        }
    }

    public EnrichmentMapParameters getParams() {
        return params;
    }

    public void setParams(EnrichmentMapParameters params) {
        this.params = params;
    }

	public DataSetFiles getDataset1files() {
		return dataset1files;
	}

	public void setDataset1files(DataSetFiles dataset1files) {
		this.dataset1files = dataset1files;
	}

	public DataSetFiles getDataset2files() {
		//if there is a gmt file for dataset 1 then copy it into Dataset 2
		//the current implementation of the interface only supports 2 datasets and the gmt file is
		//currently only associated with dataset1
		//only add the gmt file if there are other files defined for dataset2
		if(!dataset2files.isEmpty())
			if(dataset1files.getGMTFileName() != null && !dataset1files.getGMTFileName().equalsIgnoreCase(""))
				dataset2files.setGMTFileName(dataset1files.getGMTFileName());
		return dataset2files;
	}

	public void setDataset2files(DataSetFiles dataset2files) {
		this.dataset2files = dataset2files;
	}

	public Component getComponent() {
		// TODO Auto-generated method stub
		return this;
	}

	public CytoPanelName getCytoPanelName() {
		// TODO Auto-generated method stub
		return CytoPanelName.WEST;
	}

	public Icon getIcon() {
		//create an icon for the enrichment map panels
        URL EMIconURL = Thread.currentThread().getContextClassLoader().getResource("enrichmentmap_logo_notext_small.png");
        ImageIcon EMIcon = null;
        if (EMIconURL != null) {
            EMIcon = new ImageIcon(EMIconURL);
        }
		return EMIcon;
	}

	public String getTitle() {
		// TODO Auto-generated method stub
		return "Enrichment Map Input Panel";
	}
    
}
