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

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;
import static org.baderlab.csplugins.enrichmentmap.view.util.SwingUtil.makeSmall;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFormattedTextField;
import javax.swing.JFormattedTextField.AbstractFormatter;
import javax.swing.JFormattedTextField.AbstractFormatterFactory;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.ScrollPaneConstants;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.text.InternationalFormatter;

import org.baderlab.csplugins.enrichmentmap.AfterInjection;
import org.baderlab.csplugins.enrichmentmap.EnrichmentMapBuildProperties;
import org.baderlab.csplugins.enrichmentmap.model.DataSet;
import org.baderlab.csplugins.enrichmentmap.model.DataSet.Method;
import org.baderlab.csplugins.enrichmentmap.model.DataSetFiles;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMapManager;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMapParameters;
import org.baderlab.csplugins.enrichmentmap.model.LegacySupport;
import org.baderlab.csplugins.enrichmentmap.task.EnrichmentMapBuildMapTaskFactory;
import org.baderlab.csplugins.enrichmentmap.util.ResultTaskObserver;
import org.baderlab.csplugins.enrichmentmap.view.util.SwingUtil;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.io.util.StreamUtil;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.BasicCollapsiblePanel;
import org.cytoscape.util.swing.FileChooserFilter;
import org.cytoscape.util.swing.FileUtil;
import org.cytoscape.util.swing.LookAndFeelUtil;
import org.cytoscape.work.swing.DialogTaskManager;

import com.google.inject.Inject;
import com.google.inject.Provider;

@Deprecated
@SuppressWarnings("serial")
public class EnrichmentMapInputPanel extends JPanel implements CytoPanelComponent {

	@Inject private StreamUtil streamUtil;
	@Inject private DialogTaskManager dialog;
	@Inject private CySwingApplication application;
	@Inject private FileUtil fileUtil;
	@Inject private CyServiceRegistrar registrar;
	
	@Inject private EnrichmentMapBuildMapTaskFactory.Factory taskFactoryFactory;
	@Inject private Provider<EnrichmentMapParameters> emParamsFactory;
	@Inject private EnrichmentMapManager emManager;
	@Inject private LegacySupport legacySupport;
	
	private EnrichmentMapParameters params;
	
    BasicCollapsiblePanel Parameters;

    JPanel dataset1Panel;
    BasicCollapsiblePanel dataset2Panel;

    JPanel datasetsPanel;

    DecimalFormat decFormat; // used in the formatted text fields
    
    private DataSetFiles dataset1files = new DataSetFiles();
    private DataSetFiles dataset2files = new DataSetFiles();
    

    //Genesets file related components
    //user specified file names
    private JFormattedTextField gmtFileNameTextField;

    private JFormattedTextField gctFileName1TextField;
    private JFormattedTextField gctFileName2TextField;

    private JFormattedTextField dataset1FileNameTextField;
    private JFormattedTextField dataset1FileName2TextField;

    private JFormattedTextField dataset2FileNameTextField;
    private JFormattedTextField dataset2FileName2TextField;

    private JFormattedTextField ds1RanksTextField;
    private JFormattedTextField ds2RanksTextField;
    
    private JFormattedTextField ds1ClassesTextField;
    private JFormattedTextField ds2ClassesTextField;

    //user specified terms and cut offs
    private JFormattedTextField ds1Phenotype1TextField;
    private JFormattedTextField ds1Phenotype2TextField;
    private JFormattedTextField ds2Phenotype1TextField;
    private JFormattedTextField ds2Phenotype2TextField;

    private JFormattedTextField pvalueTextField;
    private JFormattedTextField qvalueTextField;
    private JFormattedTextField coeffecientTextField;
    private JFormattedTextField combinedConstantTextField;

    //flags
    private JRadioButton gseaRadio;
    private JRadioButton genericRadio;
    private JRadioButton davidRadio;
    private JRadioButton overlap;
    private JRadioButton jaccard;
    private JRadioButton combined;

    private JCheckBox scinot;
    
    private int defaultColumns = 15;

    //instruction text
    public static String gct_instruction = "Please select the expression file (.gct), (.rpt)...";
    public static String gmt_instruction = "Please select the Gene Set file (.gmt)...";
    public static String dataset_instruction = "Please select the GSEA Result file (.txt)...";
    public static String rank_instruction = "Please select the rank file (.txt), (.rnk)...";

    //tool tips
    protected static String gmtTip = "<html>File specifying gene sets.<br />" + "Format: geneset name &lt;tab&gt; description &lt;tab&gt; gene ...</html>";
    private static String gctTip = "<html>File with gene expression values.<br />" + "Format: gene &lt;tab&gt; description &lt;tab&gt; expression value &lt;tab&gt; ...</html>";
    private static String datasetTip = "<html>File specifying enrichment results.</html>";
    private static String rankTip = "<html>File specifying ranked genes.<br />" + "Format: gene &lt;tab&gt; score or statistic</html>";
    private static String classTip = "<html>File specifying the classes of each sample in expression file.<br />" + "Format: see GSEA website</html>";

    private boolean similarityCutOffChanged = false;
    private boolean LoadedFromRpt_dataset1 = false;
    private boolean LoadedFromRpt_dataset2 = false;
    private boolean panelUpdate = false;
    
    @AfterInjection
	private void createContents() {
		decFormat = new DecimalFormat();
        decFormat.setParseIntegerOnly(false);
        
        setMinimumSize(new Dimension(420, 480));
        setPreferredSize(new Dimension(440, 600));
        
        //get the current enrichment map parameters
        //params = EnrichmentMapManager.getInstance().getParameters(Cytoscape.getCurrentNetwork().getIdentifier());
	     params = emParamsFactory.get();
	
	     if (LookAndFeelUtil.isAquaLAF())
	    	 setOpaque(false);
         
        //create the three main panels: scope, advanced options, and bottom
        JPanel analysisTypePanel = createAnalysisTypePanel();

        //Put the options panel into a scroll pane
        JPanel optionsPanel = createOptionsPanel();
        
        JScrollPane scrollPane = new JScrollPane(optionsPanel);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getViewport().setBackground(UIManager.getColor("Panel.background"));

        JPanel bottomPanel = createBottomPanel();

        final GroupLayout layout = new GroupLayout(this);
		setLayout(layout);
		layout.setAutoCreateContainerGaps(false);
		layout.setAutoCreateGaps(false);
		
		layout.setHorizontalGroup(layout.createParallelGroup(Alignment.CENTER, true)
				.addComponent(analysisTypePanel, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(scrollPane, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(bottomPanel, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
		);
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addComponent(analysisTypePanel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				.addComponent(scrollPane, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(bottomPanel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
		);
    }

    /**
	 * Creates a JPanel containing analysis type (GSEA or generic) radio buttons and links to additional information
	 */
	private JPanel createAnalysisTypePanel() {
		// Added a string to the radio button on generic pointing out that this is the route for gprofiler.
		// Did not change the static variable as it might be stored and used in older sessions.
		String genericTxt = EnrichmentMapParameters.method_generic + "/gProfiler";

		if (params.getMethod().equalsIgnoreCase(EnrichmentMapParameters.method_GSEA)) {
			gseaRadio = new JRadioButton(EnrichmentMapParameters.method_GSEA, true);
			genericRadio = new JRadioButton(genericTxt, false);
			davidRadio = new JRadioButton(EnrichmentMapParameters.method_Specialized, false);
		} else if (params.getMethod().equalsIgnoreCase(EnrichmentMapParameters.method_generic)) {
			gseaRadio = new JRadioButton(EnrichmentMapParameters.method_GSEA, false);
			genericRadio = new JRadioButton(genericTxt, true);
			davidRadio = new JRadioButton(EnrichmentMapParameters.method_Specialized, false);
		} else if (params.getMethod().equalsIgnoreCase(EnrichmentMapParameters.method_Specialized)) {
			gseaRadio = new JRadioButton(EnrichmentMapParameters.method_GSEA, false);
			genericRadio = new JRadioButton(genericTxt, false);
			davidRadio = new JRadioButton(EnrichmentMapParameters.method_Specialized, true);
		}

		gseaRadio.setActionCommand(EnrichmentMapParameters.method_GSEA);
		genericRadio.setActionCommand(EnrichmentMapParameters.method_generic);
		davidRadio.setActionCommand(EnrichmentMapParameters.method_Specialized);

		gseaRadio.addActionListener((ActionEvent evt) -> {
			selectAnalysisTypeActionPerformed(evt);
		});
		genericRadio.addActionListener((ActionEvent evt) -> {
			selectAnalysisTypeActionPerformed(evt);
		});
		davidRadio.addActionListener((ActionEvent evt) -> {
			selectAnalysisTypeActionPerformed(evt);
		});

		makeSmall(gseaRadio, genericRadio, davidRadio);

		ButtonGroup analysisOptions = new ButtonGroup();
		analysisOptions.add(gseaRadio);
		analysisOptions.add(genericRadio);
		analysisOptions.add(davidRadio);

		JPanel panel = new JPanel();
		panel.setBorder(LookAndFeelUtil.createTitledBorder("Analysis Type"));
		
		final GroupLayout layout = new GroupLayout(panel);
		panel.setLayout(layout);
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(true);
	   		
   		layout.setHorizontalGroup(layout.createSequentialGroup()
   				.addGap(0, 0, Short.MAX_VALUE)
   				.addComponent(gseaRadio)
   				.addComponent(genericRadio)
   				.addComponent(davidRadio)
   				.addGap(0, 0, Short.MAX_VALUE)
   		);
   		layout.setVerticalGroup(layout.createParallelGroup(Alignment.CENTER, true)
   				.addComponent(gseaRadio, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
   				.addComponent(genericRadio, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
   				.addComponent(davidRadio, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
   		);
   		
   		if (LookAndFeelUtil.isAquaLAF())
			panel.setOpaque(false);

		return panel;
	}

	/**
	 * Creates a panel that holds main user inputs geneset files, datasets and parameters
	 */
	private OptionsPanel createOptionsPanel() {
		JPanel gmtPanel = createGMTPanel();
		
		BasicCollapsiblePanel paramsPanel = createParametersPanel();
		paramsPanel.setCollapsed(true);

		OptionsPanel panel = new OptionsPanel();
		{
			GroupLayout layout = new GroupLayout(panel);
			panel.setLayout(layout);
			layout.setAutoCreateContainerGaps(false);
			layout.setAutoCreateGaps(true);
	
			layout.setHorizontalGroup(layout.createParallelGroup(Alignment.CENTER, true)
					.addComponent(gmtPanel, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
					.addComponent(getDatasetsPanel(), DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
					.addComponent(paramsPanel, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
			);
			layout.setVerticalGroup(layout.createSequentialGroup()
					.addComponent(gmtPanel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addComponent(getDatasetsPanel(), PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
					.addComponent(paramsPanel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
			);
		}
		
		if (LookAndFeelUtil.isAquaLAF())
			panel.setOpaque(false);

		return panel;
	}
	
	private JPanel getDatasetsPanel() {
		if (datasetsPanel == null) {
			datasetsPanel = new JPanel();
			datasetsPanel.setLayout(new BoxLayout(datasetsPanel, BoxLayout.Y_AXIS));
			
			if (LookAndFeelUtil.isAquaLAF())
				datasetsPanel.setOpaque(false);
			
			updateDatasetsPanel();
		}
		
		return datasetsPanel;
	}
	
	private void updateDatasetsPanel() {
		// before clearing the panel find out which panels where collapsed so we maintain its current state.
		boolean collapseDs2 = dataset2Panel == null || dataset2Panel.isCollapsed();

		getDatasetsPanel().removeAll();

		dataset1Panel = createDataset1Panel();

		dataset2Panel = createDataset2Panel();
		dataset2Panel.setCollapsed(collapseDs2);

		getDatasetsPanel().add(dataset1Panel);
		getDatasetsPanel().add(dataset2Panel);
		getDatasetsPanel().add(Box.createVerticalGlue());
		getDatasetsPanel().revalidate();
	}

	/**
	 * Creates a panel that holds gene set file specification
	 */
	private JPanel createGMTPanel() {
		// add GMT file
		JButton selectGMTFileButton = new JButton("Browse...");
		selectGMTFileButton.setToolTipText(gmtTip);
		
		gmtFileNameTextField = new JFormattedTextField();
		gmtFileNameTextField.setToolTipText(gmtTip);
		gmtFileNameTextField.setColumns(defaultColumns);
		// components needed for the directory load
		gmtFileNameTextField.addPropertyChangeListener("value", new EnrichmentMapInputPanel.FormattedTextFieldAction());

		selectGMTFileButton.addActionListener((ActionEvent evt) -> {
			selectGMTFileButtonActionPerformed(evt);
		});
        
		makeSmall(gmtFileNameTextField, selectGMTFileButton);
		
		JPanel panel = new JPanel();
		panel.setBorder(LookAndFeelUtil.createTitledBorder("GMT File (contains gene-set/pathway definitions)"));
		
       	final GroupLayout layout = new GroupLayout(panel);
       	panel.setLayout(layout);
   		layout.setAutoCreateContainerGaps(true);
   		layout.setAutoCreateGaps(!LookAndFeelUtil.isAquaLAF());
   		
   		layout.setHorizontalGroup(layout.createSequentialGroup()
   				.addComponent(gmtFileNameTextField, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
   				.addComponent(selectGMTFileButton, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
   		);
   		layout.setVerticalGroup(layout.createParallelGroup(Alignment.CENTER, false)
   				.addComponent(gmtFileNameTextField, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
   				.addComponent(selectGMTFileButton)
   		);
   		
   		if (LookAndFeelUtil.isAquaLAF())
			panel.setOpaque(false);

   		// FIXME
//      if(!params.getMethod().equalsIgnoreCase(EnrichmentMapParameters.method_Specialized))
//      	panel.add(newGMTPanel);
   		
		return panel;
	}

	/**
	 * Creates a panel that holds dataset 1 file specifications
	 */
	private JPanel createDataset1Panel() {
		// add GCT file
		JLabel gctLabel = new JLabel("Expression:");
		gctLabel.setToolTipText(gctTip);

		JButton selectGCTFileButton = new JButton("Browse...");
		selectGCTFileButton.setToolTipText(gctTip);
		
		gctFileName1TextField = new JFormattedTextField();
		gctFileName1TextField.setToolTipText(gctTip);
		gctFileName1TextField.setColumns(defaultColumns);

		// components needed for the directory load
		gctFileName1TextField.addPropertyChangeListener("value",
				new EnrichmentMapInputPanel.FormattedTextFieldAction());

		// GCTFileName1TextField.setText(gct_instruction);

		selectGCTFileButton.addActionListener((ActionEvent evt) -> {
			selectGCTFileButtonActionPerformed(evt);
		});

		boolean isGSEA = params.getMethod().equalsIgnoreCase(EnrichmentMapParameters.method_GSEA);
		
		// add Results1 file
		JLabel results1Label = new JLabel(isGSEA ? "Enrichments 1:" : "Enrichments:");
		results1Label.setToolTipText(datasetTip);
		
		JButton selectResults1FileButton = new JButton("Browse...");
		selectResults1FileButton.setToolTipText(datasetTip);
		
		dataset1FileNameTextField = new JFormattedTextField();
		dataset1FileNameTextField.setToolTipText(datasetTip);
		dataset1FileNameTextField.setColumns(defaultColumns);

		// components needed for the directory load
		dataset1FileNameTextField.addPropertyChangeListener("value",
				new EnrichmentMapInputPanel.FormattedTextFieldAction());

		selectResults1FileButton.addActionListener((ActionEvent evt) -> {
			selectDataset1FileButtonActionPerformed(evt);
		});

		// add Results2 file
		JLabel results2Label = new JLabel("Enrichments 2:");
		results2Label.setToolTipText(datasetTip);

		JButton selectResults2FileButton = new JButton("Browse...");
		selectResults2FileButton.setToolTipText(datasetTip);
		
		dataset1FileName2TextField = new JFormattedTextField();
		dataset1FileName2TextField.setToolTipText(datasetTip);
		dataset1FileName2TextField.setColumns(defaultColumns);

		// components needed for the directory load
		dataset1FileName2TextField.addPropertyChangeListener("value",
				new EnrichmentMapInputPanel.FormattedTextFieldAction());

		selectResults2FileButton.addActionListener((ActionEvent evt) -> {
			selectDataset1File2ButtonActionPerformed(evt);
		});

		makeSmall(gctLabel, gctFileName1TextField, selectGCTFileButton);
   		makeSmall(results1Label, dataset1FileNameTextField, selectResults1FileButton);
   		makeSmall(results2Label, dataset1FileName2TextField, selectResults2FileButton);
   		
   		results2Label.setVisible(isGSEA);
   		dataset1FileName2TextField.setEnabled(isGSEA);
   		dataset1FileName2TextField.setVisible(isGSEA);
   		selectResults2FileButton.setEnabled(isGSEA);
   		selectResults2FileButton.setVisible(isGSEA);
		
		JPanel panel = new JPanel();
		panel.setBorder(LookAndFeelUtil.createTitledBorder("Dataset 1"));
		panel.setAlignmentX(Component.CENTER_ALIGNMENT);
		panel.setMaximumSize(new Dimension(Short.MAX_VALUE, panel.getPreferredSize().height));
		
		final GroupLayout layout = new GroupLayout(panel);
		panel.setLayout(layout);
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(!LookAndFeelUtil.isAquaLAF());
		
		ParallelGroup hGroup = layout.createParallelGroup(Alignment.CENTER, true);
		SequentialGroup vGroup = layout.createSequentialGroup();
		
		layout.setHorizontalGroup(hGroup
				.addGroup(layout.createSequentialGroup()
						.addGroup(layout.createParallelGroup(Alignment.TRAILING, true)
								.addComponent(gctLabel)
								.addComponent(results1Label)
								.addComponent(results2Label)
						)
						.addPreferredGap(ComponentPlacement.RELATED)
						.addGroup(layout.createParallelGroup(Alignment.LEADING, true)
								.addGroup(layout.createSequentialGroup()
										.addComponent(gctFileName1TextField, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
										.addComponent(selectGCTFileButton, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
								)
								.addGroup(layout.createSequentialGroup()
										.addComponent(dataset1FileNameTextField, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
										.addComponent(selectResults1FileButton, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
								)
								.addGroup(layout.createSequentialGroup()
										.addComponent(dataset1FileName2TextField, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
										.addComponent(selectResults2FileButton, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
								)
								
						)
				)
		);
		layout.setVerticalGroup(vGroup
				.addGroup(layout.createParallelGroup(Alignment.CENTER, false)
						.addComponent(gctLabel)
						.addComponent(gctFileName1TextField, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
						.addComponent(selectGCTFileButton)
				)
				.addGroup(layout.createParallelGroup(Alignment.CENTER, false)
						.addComponent(results1Label)
						.addComponent(dataset1FileNameTextField, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
						.addComponent(selectResults1FileButton)
				)
				.addGroup(layout.createParallelGroup(Alignment.CENTER, false)
						.addComponent(results2Label)
						.addComponent(dataset1FileName2TextField, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
						.addComponent(selectResults2FileButton)
				)
		);

		if (!params.getMethod().equalsIgnoreCase(EnrichmentMapParameters.method_Specialized)) {
			BasicCollapsiblePanel advancedDatasetOptionsPanel = createAdvancedDatasetOptions(1);
			
			hGroup.addComponent(advancedDatasetOptionsPanel);
			vGroup.addPreferredGap(ComponentPlacement.UNRELATED).addComponent(advancedDatasetOptionsPanel);
		}
		
		if (LookAndFeelUtil.isAquaLAF())
			panel.setOpaque(false);
		
		return panel;
	}

    /**
     * Creates a panel that holds dataset 2 file specification
     */
	private BasicCollapsiblePanel createDataset2Panel() {
		// add GCT file
		JLabel gctLabel = new JLabel("Expression:");
		gctLabel.setToolTipText(gctTip);
		
		JButton selectGCTFileButton = new JButton("Browse...");
		selectGCTFileButton.setToolTipText(gctTip);
		
		gctFileName2TextField = new JFormattedTextField();
		gctFileName2TextField.setToolTipText(gctTip);
		gctFileName2TextField.setColumns(defaultColumns);

		// components needed for the directory load
		gctFileName2TextField.addPropertyChangeListener("value",
				new EnrichmentMapInputPanel.FormattedTextFieldAction());

		selectGCTFileButton.addActionListener((ActionEvent evt) -> {
			selectGCTFileButton2ActionPerformed(evt);
		});

		boolean isGSEA = params.getMethod().equalsIgnoreCase(EnrichmentMapParameters.method_GSEA);
		
		// add Results1 file
		JLabel results1Label = new JLabel(isGSEA ? "Enrichments 1:" : "Enrichments:");
		results1Label.setToolTipText(datasetTip);

		JButton selectResults1FileButton = new JButton("Browse...");
		selectResults1FileButton.setToolTipText(datasetTip);
		
		dataset2FileNameTextField = new JFormattedTextField();
		dataset2FileNameTextField.setToolTipText(datasetTip);
		dataset2FileNameTextField.setColumns(defaultColumns);

		// components needed for the directory load
		dataset2FileNameTextField.addPropertyChangeListener("value",
				new EnrichmentMapInputPanel.FormattedTextFieldAction());

		// Dataset2FileNameTextField.setText(dataset_instruction);

		selectResults1FileButton.addActionListener((ActionEvent evt) -> {
			selectDataset2FileButtonActionPerformed(evt);
		});

		// add Results2 file
		JLabel results2Label = new JLabel("Enrichments 2:");
		results2Label.setToolTipText(datasetTip);
		
		JButton selectResults2FileButton = new JButton("Browse...");
		selectResults2FileButton.setToolTipText(datasetTip);
		
		dataset2FileName2TextField = new JFormattedTextField();
		dataset2FileName2TextField.setToolTipText(datasetTip);
		dataset2FileName2TextField.setColumns(defaultColumns);
		// components needed for the directory load
		dataset2FileName2TextField.addPropertyChangeListener("value",
				new EnrichmentMapInputPanel.FormattedTextFieldAction());

		selectResults2FileButton.addActionListener((ActionEvent evt) -> {
			selectDataset2File2ButtonActionPerformed(evt);
		});
		
		makeSmall(gctLabel, gctFileName2TextField, selectGCTFileButton);
   		makeSmall(results1Label, dataset2FileNameTextField, selectResults1FileButton);
   		makeSmall(results2Label, dataset2FileName2TextField, selectResults2FileButton);
   		
   		results2Label.setVisible(isGSEA);
   		dataset2FileName2TextField.setEnabled(isGSEA);
   		dataset2FileName2TextField.setVisible(isGSEA);
   		selectResults2FileButton.setEnabled(isGSEA);
   		selectResults2FileButton.setVisible(isGSEA);

		BasicCollapsiblePanel panel = new BasicCollapsiblePanel("Dataset 2");
		panel.setAlignmentX(Component.CENTER_ALIGNMENT);
		panel.setMaximumSize(new Dimension(Short.MAX_VALUE, panel.getPreferredSize().height));
		
		final GroupLayout layout = new GroupLayout(panel.getContentPane());
		panel.getContentPane().setLayout(layout);
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(!LookAndFeelUtil.isAquaLAF());
		
		ParallelGroup hGroup = layout.createParallelGroup(Alignment.CENTER, true);
		SequentialGroup vGroup = layout.createSequentialGroup();
		
		layout.setHorizontalGroup(hGroup
				.addGroup(layout.createSequentialGroup()
						.addGroup(layout.createParallelGroup(Alignment.TRAILING, true)
								.addComponent(gctLabel)
								.addComponent(results1Label)
								.addComponent(results2Label)
						)
						.addPreferredGap(ComponentPlacement.RELATED)
						.addGroup(layout.createParallelGroup(Alignment.LEADING, true)
								.addGroup(layout.createSequentialGroup()
										.addComponent(gctFileName2TextField, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
										.addComponent(selectGCTFileButton, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
								)
								.addGroup(layout.createSequentialGroup()
										.addComponent(dataset2FileNameTextField, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
										.addComponent(selectResults1FileButton, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
								)
								.addGroup(layout.createSequentialGroup()
										.addComponent(dataset2FileName2TextField, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
										.addComponent(selectResults2FileButton, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
								)
								
						)
				)
		);
		layout.setVerticalGroup(vGroup
				.addGroup(layout.createParallelGroup(Alignment.CENTER, false)
						.addComponent(gctLabel)
						.addComponent(gctFileName2TextField, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
						.addComponent(selectGCTFileButton)
				)
				.addGroup(layout.createParallelGroup(Alignment.CENTER, false)
						.addComponent(results1Label)
						.addComponent(dataset2FileNameTextField, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
						.addComponent(selectResults1FileButton)
				)
				.addGroup(layout.createParallelGroup(Alignment.CENTER, false)
						.addComponent(results2Label)
						.addComponent(dataset2FileName2TextField, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
						.addComponent(selectResults2FileButton)
				)
		);

		if (!params.getMethod().equalsIgnoreCase(EnrichmentMapParameters.method_Specialized)) {
			BasicCollapsiblePanel advancedDatasetOptionsPanel = createAdvancedDatasetOptions(2);
			
			hGroup.addComponent(advancedDatasetOptionsPanel);
			vGroup.addPreferredGap(ComponentPlacement.UNRELATED).addComponent(advancedDatasetOptionsPanel);
		}
		
		if (LookAndFeelUtil.isAquaLAF())
			panel.setOpaque(false);

		return panel;
	}

    /**
     * Creates a collapsible panel that holds the advanced options specifications (rank file, phenotypes)
     *
     * @param dataset - whether this collapsible advanced panel is for dataset 1 or dataset 2
     * @return Collapsible panel that holds the advanced options specification interface
     */
	private BasicCollapsiblePanel createAdvancedDatasetOptions(int dataset) {
		// create a panel for advanced options
		BasicCollapsiblePanel panel = new BasicCollapsiblePanel("Advanced");
		panel.setCollapsed(true);

		if (LookAndFeelUtil.isAquaLAF())
			panel.setOpaque(false);

		// add Ranks file
		JLabel ranksLabel = new JLabel("Ranks:");
		ranksLabel.setToolTipText(rankTip);
		
		JButton selectRanksFileButton = new JButton("Browse...");
		selectRanksFileButton.setToolTipText(rankTip);

		// add class file
		JLabel classesLabel = new JLabel("Classes:");
		classesLabel.setToolTipText(classTip);
		
		JButton selectClassFileButton = new JButton("Browse...");
		selectClassFileButton.setToolTipText(classTip);

		final JFormattedTextField ranksTextField;
		final JFormattedTextField classesTextField;
		
		if (dataset == 1) {
			ds1RanksTextField = new JFormattedTextField();
			ds1RanksTextField.setColumns(defaultColumns);
			ds1RanksTextField.addPropertyChangeListener("value",
					new EnrichmentMapInputPanel.FormattedTextFieldAction());

			// Dataset1RankFileTextField.setText(rank_instruction);
			selectRanksFileButton.addActionListener((ActionEvent evt) -> {
				selectRank1FileButtonActionPerformed(evt);
			});

			ds1ClassesTextField = new JFormattedTextField();
			ds1ClassesTextField.setColumns(defaultColumns);
			ds1ClassesTextField.addPropertyChangeListener("value",
					new EnrichmentMapInputPanel.FormattedTextFieldAction());
			selectClassFileButton.addActionListener((ActionEvent evt) -> {
				selectClass1FileButtonActionPerformed(evt);
			});
			
			ranksTextField = ds1RanksTextField;
			classesTextField = ds1ClassesTextField;
		} else {
			ds2RanksTextField = new JFormattedTextField();
			ds2RanksTextField.setColumns(defaultColumns);
			ds2RanksTextField.addPropertyChangeListener("value",
					new EnrichmentMapInputPanel.FormattedTextFieldAction());

			// Dataset2RankFileTextField.setText(rank_instruction);

			selectRanksFileButton.addActionListener((ActionEvent evt) -> {
				selectRank2FileButtonActionPerformed(evt);
			});

			ds2ClassesTextField = new JFormattedTextField();
			ds2ClassesTextField.setColumns(defaultColumns);
			ds2ClassesTextField.addPropertyChangeListener("value",
					new EnrichmentMapInputPanel.FormattedTextFieldAction());

			selectClassFileButton.addActionListener((ActionEvent evt) -> {
				selectClass2FileButtonActionPerformed(evt);
			});
			
			ranksTextField = ds2RanksTextField;
			classesTextField = ds2ClassesTextField;
		}
		
		ranksTextField.setToolTipText(rankTip);
		classesTextField.setToolTipText(classTip);

		// add Phenotypes
		JLabel phenotypesLabel = new JLabel("Phenotypes:");
		JLabel vsLabel = new JLabel("VS.");
		
		final JFormattedTextField phenotype1TextField;
		final JFormattedTextField phenotype2TextField;
		
		if (dataset == 1) {
			ds1Phenotype1TextField = new JFormattedTextField("UP");
			ds1Phenotype1TextField.setColumns(4);
			ds1Phenotype1TextField.addPropertyChangeListener("value",
					new EnrichmentMapInputPanel.FormattedTextFieldAction());

			ds1Phenotype2TextField = new JFormattedTextField("DOWN");
			ds1Phenotype2TextField.setColumns(4);
			ds1Phenotype2TextField.addPropertyChangeListener("value",
					new EnrichmentMapInputPanel.FormattedTextFieldAction());

			phenotype1TextField = ds1Phenotype1TextField;
			phenotype2TextField = ds1Phenotype2TextField;
		} else {
			ds2Phenotype1TextField = new JFormattedTextField("UP");
			ds2Phenotype1TextField.setColumns(4);
			ds2Phenotype1TextField.addPropertyChangeListener("value",
					new EnrichmentMapInputPanel.FormattedTextFieldAction());

			ds2Phenotype2TextField = new JFormattedTextField("DOWN");
			ds2Phenotype2TextField.setColumns(4);
			ds2Phenotype2TextField.addPropertyChangeListener("value",
					new EnrichmentMapInputPanel.FormattedTextFieldAction());

			phenotype1TextField = ds2Phenotype1TextField;
			phenotype2TextField = ds2Phenotype2TextField;			
		}
		
		makeSmall(ranksLabel, ranksTextField, selectRanksFileButton);
		makeSmall(classesLabel, classesTextField, selectClassFileButton);
		makeSmall(phenotypesLabel, vsLabel, phenotype1TextField, phenotype2TextField);
		
		final GroupLayout layout = new GroupLayout(panel.getContentPane());
		panel.getContentPane().setLayout(layout);
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(!LookAndFeelUtil.isAquaLAF());
		
		ParallelGroup hGroup = layout.createParallelGroup(Alignment.CENTER, true);
		SequentialGroup vGroup = layout.createSequentialGroup();
		
		layout.setHorizontalGroup(hGroup
				.addGroup(layout.createSequentialGroup()
						.addGroup(layout.createParallelGroup(Alignment.TRAILING, true)
								.addComponent(ranksLabel)
								.addComponent(classesLabel)
								.addComponent(phenotypesLabel)
						)
						.addPreferredGap(ComponentPlacement.RELATED)
						.addGroup(layout.createParallelGroup(Alignment.LEADING, true)
								.addGroup(layout.createSequentialGroup()
										.addComponent(ranksTextField, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
										.addComponent(selectRanksFileButton, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
								)
								.addGroup(layout.createSequentialGroup()
										.addComponent(classesTextField, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
										.addComponent(selectClassFileButton, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
								)
								.addGroup(layout.createSequentialGroup()
										.addComponent(phenotype1TextField,PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
										.addComponent(vsLabel,PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
										.addComponent(phenotype2TextField,PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
								)
								
						)
				)
		);
		layout.setVerticalGroup(vGroup
				.addGroup(layout.createParallelGroup(Alignment.CENTER, false)
						.addComponent(ranksLabel)
						.addComponent(ranksTextField, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
						.addComponent(selectRanksFileButton)
				)
				.addGroup(layout.createParallelGroup(Alignment.CENTER, false)
						.addComponent(classesLabel)
						.addComponent(classesTextField, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
						.addComponent(selectClassFileButton, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				)
				.addGroup(layout.createParallelGroup(Alignment.CENTER, false)
						.addComponent(phenotypesLabel)
						.addComponent(phenotype1TextField, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
						.addComponent(vsLabel)
						.addComponent(phenotype2TextField, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				)
		);

		return panel;
	}

	/**
	 * Creates a collapsable panel that holds parameter inputs
	 *
	 * @return panel containing the parameter specification interface
	 */
	private BasicCollapsiblePanel createParametersPanel() {
		int precision = 6;
		BasicCollapsiblePanel panel = new BasicCollapsiblePanel("Parameters");

		if (LookAndFeelUtil.isAquaLAF())
			panel.setOpaque(false);

		// Add check box to turn on scientific notation for pvalue and qvalue
		scinot = new JCheckBox("Scientific Notation");
		scinot.setToolTipText(
				"Allows pvalue and q-value to be entered in scientific notation, i.e. 5E-3 instead of 0.005");
		scinot.addActionListener((ActionEvent evt) -> {
			selectScientificNotationActionPerformed(evt);
		});

		// P-value Cutoff input
		String pvalueCutOffTip =
				"<html>Only genesets with a p-value less than<br />" +
				"the cutoff will be included.</html>";
		
		JLabel pvalueCutOffLabel = new JLabel("P-value Cutoff:");
		pvalueCutOffLabel.setToolTipText(pvalueCutOffTip);
		
		pvalueTextField = new JFormattedTextField(decFormat);
		pvalueTextField.setColumns(precision);
		pvalueTextField.setHorizontalAlignment(JTextField.RIGHT);
		pvalueTextField.addPropertyChangeListener("value", new EnrichmentMapInputPanel.FormattedTextFieldAction());
		pvalueTextField.setToolTipText(pvalueCutOffTip);
		pvalueTextField.setText(Double.toString(params.getPvalue()));
		pvalueTextField.setValue(params.getPvalue());

		// Q-value Cutoff input
		String qvalueCutOffTip =
				"<html>Only genesets with a FDR q-value less than<br />" +
				"the cutoff will be included.</html>";
		
		JLabel qvalueCutOffLabel = new JLabel("FDR Q-value Cutoff:");
		qvalueCutOffLabel.setToolTipText(qvalueCutOffTip);
		
		qvalueTextField = new JFormattedTextField(decFormat);
		qvalueTextField.setColumns(precision);
		qvalueTextField.setHorizontalAlignment(JTextField.RIGHT);
		qvalueTextField.addPropertyChangeListener("value", new EnrichmentMapInputPanel.FormattedTextFieldAction());
		qvalueTextField.setToolTipText(qvalueCutOffTip);
		qvalueTextField.setText(Double.toString(params.getQvalue()));
		qvalueTextField.setValue(params.getQvalue());

		// Coefficient Cutoff input
		JLabel similarityCutoff = new JLabel("Similarity Cutoff:");
		
		jaccard = new JRadioButton("Jaccard Coefficient");
		jaccard.setActionCommand("jaccard");
		jaccard.setSelected(true);
		
		overlap = new JRadioButton("Overlap Coefficient");
		overlap.setActionCommand("overlap");
		
		combined = new JRadioButton("Jaccard+Overlap Combined");
		combined.setActionCommand("combined");
		
		if (params.getSimilarityMetric().equalsIgnoreCase(EnrichmentMapParameters.SM_JACCARD)) {
			jaccard.setSelected(true);
			overlap.setSelected(false);
			combined.setSelected(false);
		} else if (params.getSimilarityMetric().equalsIgnoreCase(EnrichmentMapParameters.SM_OVERLAP)) {
			jaccard.setSelected(false);
			overlap.setSelected(true);
			combined.setSelected(false);
		} else if (params.getSimilarityMetric().equalsIgnoreCase(EnrichmentMapParameters.SM_COMBINED)) {
			jaccard.setSelected(false);
			overlap.setSelected(false);
			combined.setSelected(true);
		}
		
		ButtonGroup jaccardOrOverlap = new ButtonGroup();
		jaccardOrOverlap.add(jaccard);
		jaccardOrOverlap.add(overlap);
		jaccardOrOverlap.add(combined);

		jaccard.addActionListener((ActionEvent evt) -> {
			selectJaccardOrOverlapActionPerformed(evt);
		});
		overlap.addActionListener((ActionEvent evt) -> {
			selectJaccardOrOverlapActionPerformed(evt);
		});
		combined.addActionListener((ActionEvent evt) -> {
			selectJaccardOrOverlapActionPerformed(evt);
		});

		String coeffecientCutOffTip = 
				"<html>Sets the Jaccard or Overlap coefficient cutoff.<br />" +
				"Only edges with a Jaccard or Overlap coefficient less than<br />" +
				"the cutoff will be added.</html>";
		
		JLabel coeffecientCutOffLabel = new JLabel("Cutoff:");
		coeffecientCutOffLabel.setToolTipText(coeffecientCutOffTip);
		
		coeffecientTextField = new JFormattedTextField(decFormat);
		coeffecientTextField.setColumns(precision);
		coeffecientTextField.setHorizontalAlignment(JTextField.RIGHT);
		coeffecientTextField.addPropertyChangeListener("value", new EnrichmentMapInputPanel.FormattedTextFieldAction());
		coeffecientTextField.setToolTipText(coeffecientCutOffTip);
		// coeffecientTextField.setText(Double.toString(params.getSimilarityCutOff()));
		coeffecientTextField.setValue(params.getSimilarityCutOff());
		similarityCutOffChanged = false; // reset for new Panel after .setValue(...) wrongly changed it to "true"

		// Add a box to specify the constant used in created the combined value
		JLabel combinedConstantLabel = new JLabel("Combined Constant:");
		
		combinedConstantTextField = new JFormattedTextField(decFormat);
		combinedConstantTextField.setColumns(precision);
		combinedConstantTextField.setHorizontalAlignment(JTextField.RIGHT);
		combinedConstantTextField.addPropertyChangeListener("value", new FormattedTextFieldAction());
		combinedConstantTextField.setValue(0.5);

		makeSmall(pvalueCutOffLabel, pvalueTextField, qvalueCutOffLabel, qvalueTextField, scinot);
		makeSmall(similarityCutoff, jaccard, overlap, combined);
		makeSmall(coeffecientCutOffLabel, coeffecientTextField, combinedConstantLabel, combinedConstantTextField);
		
		// add the components to the panel
		final GroupLayout layout = new GroupLayout(panel.getContentPane());
		panel.getContentPane().setLayout(layout);
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(!LookAndFeelUtil.isAquaLAF());
		
		layout.setHorizontalGroup(layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup(Alignment.TRAILING, true)
						.addComponent(pvalueCutOffLabel)
						.addComponent(qvalueCutOffLabel)
						.addComponent(similarityCutoff)
						.addComponent(coeffecientCutOffLabel)
						.addComponent(combinedConstantLabel)
				)
				.addPreferredGap(ComponentPlacement.RELATED)
				.addGroup(layout.createParallelGroup(Alignment.LEADING, true)
						.addComponent(pvalueTextField, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
						.addComponent(qvalueTextField, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
						.addComponent(scinot, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
						.addComponent(jaccard, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
						.addComponent(overlap, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
						.addComponent(combined, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
						.addComponent(coeffecientTextField, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
						.addComponent(combinedConstantTextField, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				)
		);
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup(Alignment.CENTER, false)
						.addComponent(pvalueCutOffLabel)
						.addComponent(pvalueTextField)
				)
				.addGroup(layout.createParallelGroup(Alignment.CENTER, false)
						.addComponent(qvalueCutOffLabel)
						.addComponent(qvalueTextField)
				)
				.addComponent(scinot)
				.addPreferredGap(ComponentPlacement.UNRELATED)
				.addGroup(layout.createParallelGroup(Alignment.CENTER, false)
						.addComponent(similarityCutoff)
						.addComponent(jaccard)
				)
				.addComponent(overlap)
				.addComponent(combined)
				.addPreferredGap(ComponentPlacement.UNRELATED)
				.addGroup(layout.createParallelGroup(Alignment.CENTER, false)
						.addComponent(coeffecientCutOffLabel)
						.addComponent(coeffecientTextField)
				)
				.addGroup(layout.createParallelGroup(Alignment.CENTER, false)
						.addComponent(combinedConstantLabel)
						.addComponent(combinedConstantTextField)
				)
		);
		
		return panel;
	}

     /**
     * Handles setting for the text field parameters that are numbers.
     * Makes sure that the numbers make sense.
     */
     private class FormattedTextFieldAction implements PropertyChangeListener {
    	@Override
        public void propertyChange(PropertyChangeEvent e) {
            JFormattedTextField source = (JFormattedTextField) e.getSource();

            String message = "The value you have entered is invalid.";
            boolean invalid = false;

            if(source == pvalueTextField || source == qvalueTextField || source == coeffecientTextField || source == combinedConstantTextField){
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
            		if (invalid) 
            			JOptionPane.showMessageDialog(application.getJFrame(), message, "Parameter out of bounds", JOptionPane.WARNING_MESSAGE);
            
            } 
            //boxes taht are text but not files
            else if(source == ds1Phenotype1TextField || source == ds1Phenotype2TextField || source == ds2Phenotype1TextField || source == ds2Phenotype2TextField){
            		if (source == ds1Phenotype1TextField) {
                    String value = ds1Phenotype1TextField.getText();
                    dataset1files.setPhenotype1(value);                
                }
                else if (source == ds1Phenotype2TextField) {
                    String value = ds1Phenotype2TextField.getText();
                    dataset1files.setPhenotype2(value);
                }

                else if (source == ds2Phenotype1TextField) {
                    String value = ds2Phenotype1TextField.getText();
                    dataset2files.setPhenotype1(value);
                }
                else if (source == ds2Phenotype2TextField) {
                    String value = ds2Phenotype2TextField.getText();
                    dataset2files.setPhenotype2(value);
                }
            }
            //the rest of the text textboxes
            else{
            		String value = source.getText();
            		if (source == gctFileName1TextField) {
            			dataset1files.setExpressionFileName(value);
            		}else if (source == gctFileName2TextField) {
            			dataset2files.setExpressionFileName(value); 
            		}else if (source == dataset1FileNameTextField) 
            			dataset1files.setEnrichmentFileName1(value);            	   		
            		else if (source == dataset1FileName2TextField) 
            			dataset1files.setEnrichmentFileName2(value);              
            		else if (source == dataset2FileNameTextField) 
            			dataset2files.setEnrichmentFileName1(value);               		
            		else if (source == dataset2FileName2TextField) 
            			dataset2files.setEnrichmentFileName2(value);                             		            		
            		else if (source == ds1RanksTextField)
            			dataset1files.setRankedFile(value);            		
            		else if (source == ds2RanksTextField)
            			dataset2files.setRankedFile(value);           	                 		
            		else if (source == ds1ClassesTextField)
            			dataset1files.setClassFile(value);            		
            		else if (source == ds2ClassesTextField)
            			dataset2files.setClassFile(value);          	                 		
            		else if (source == gmtFileNameTextField) 
            			dataset1files.setGMTFileName(value);                            
            		
            		Color found = Color.black;
            		if(!value.equalsIgnoreCase("")){
            			found = checkFile(value);
            			source.setForeground(found);
            		}
            		
            		//For all the files warn the user if the new file is still not found
            		if(found.equals(Color.RED) && !LoadedFromRpt_dataset1 && !LoadedFromRpt_dataset2 && !panelUpdate)
            			JOptionPane.showMessageDialog(application.getJFrame(), message, "File name change entered is not a valid file name", JOptionPane.WARNING_MESSAGE);
            			
            	}//end of else for Text boxes that are text and files
                       
        }
    }


	/**
	 * Utility method that creates a panel for buttons at the bottom of the Enrichment Map Panel
	 */
	private JPanel createBottomPanel() {
		JButton helpButton = SwingUtil.createOnlineHelpButton(EnrichmentMapBuildProperties.USER_MANUAL_URL,
				"Online Manual...", registrar);
		
		JButton resetButton = new JButton("Reset");
		resetButton.addActionListener(e -> resetPanel());

		JButton closeButton = new JButton("Close");
		closeButton.addActionListener(this::cancelButtonActionPerformed);

		JButton importButton = new JButton("Build");
		importButton.addActionListener(e -> build());

		JPanel panel = LookAndFeelUtil.createOkCancelPanel(importButton, closeButton, helpButton, resetButton);

		if (LookAndFeelUtil.isAquaLAF())
			panel.setOpaque(false);

		return panel;
	}
	
	private void build() {
		//create a new params for the new EM and add the dataset files to it
		EnrichmentMapParameters new_params = emParamsFactory.get();
		new_params.copy(EnrichmentMapInputPanel.this.getParams());
		new_params.addFiles(LegacySupport.DATASET1, dataset1files);
		if(!dataset2files.isEmpty())
			new_params.addFiles(LegacySupport.DATASET2, dataset2files);

		String prefix = legacySupport.getNextAttributePrefix();
		new_params.setAttributePrefix(prefix);
		EnrichmentMap map = new EnrichmentMap(new_params.getCreationParameters(), registrar);
		
		// TEMPORARY
		// This code is kind of ugly because it is bridging the gap between the old EnrichmentMapParameters and the new style
		Method method = EnrichmentMapParameters.stringToMethod(new_params.getMethod());
		map.addDataSet(LegacySupport.DATASET1, new DataSet(map, LegacySupport.DATASET1, method, dataset1files));
		if(!dataset2files.isEmpty())
			map.addDataSet(LegacySupport.DATASET2, new DataSet(map, LegacySupport.DATASET2, method, dataset2files));

		//EnrichmentMapParseInputEvent parseInput = new EnrichmentMapParseInputEvent(empanel,map , dialog,  streamUtil);
		//parseInput.build();

		//add observer to catch if the input is a GREAT file so we can determine which p-value to use
		ResultTaskObserver observer = new ResultTaskObserver();

		EnrichmentMapBuildMapTaskFactory buildmap = taskFactoryFactory.create(map);
		//buildmap.build();
		dialog.execute(buildmap.createTaskIterator(), observer);

		//After the network is built register the HeatMap and Parameters panel
		emManager.showPanels();
	}

	private void cancelButtonActionPerformed(ActionEvent evt) {
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
                gmtFileNameTextField.setForeground(checkFile(gmt));
                gmtFileNameTextField.setText(gmt);
                dataset1files.setGMTFileName(gmt);
                gmtFileNameTextField.setToolTipText(gmt);
            }


            boolean AutoPopulate = true;
            if(!dataset1files.getGMTFileName().equalsIgnoreCase(gmt)){
                //maybe the files are the same but they are in different directories
                File currentGMTFilename = new File(dataset1files.getGMTFileName());
                File newGMTFilename = new File(gmt);
                if(!currentGMTFilename.getName().equalsIgnoreCase(newGMTFilename.getName())){
                    int answer = JOptionPane.showConfirmDialog(this,"This analysis GMT file does not match the previous dataset loaded.\n If you want to use the current GMT file but still use the new rpt file to populated fields press YES,\n If you want to change the GMT file to the one in this rpt file and populate the fields with the rpt file press NO,\n to choose a different rpt file press CANCEL\n","GMT files name mismatch", JOptionPane.YES_NO_CANCEL_OPTION);
                    if(answer == JOptionPane.NO_OPTION){
                        gmtFileNameTextField.setForeground(checkFile(gmt));
                        gmtFileNameTextField.setText(gmt);
                        dataset1files.setGMTFileName(gmt);
                        gmtFileNameTextField.setToolTipText(gmt);
                    }
                    else if(answer == JOptionPane.CANCEL_OPTION)
                        AutoPopulate = false;
                }
            }
            if(AutoPopulate){
                
                ds1RanksTextField.setForeground(checkFile(rnk));
                ds1RanksTextField.setText(rnk);
                dataset1files.setRankedFile(rnk);
                ds1RanksTextField.setToolTipText(rnk);

                dataset1files.setEnrichmentFileName1(edbFile.getAbsolutePath());
                this.setDatasetnames(edbFile.getAbsolutePath(),"",dataset1);
            }
    }
    else{
       if(dataset1files.getGMTFileName() == null || dataset1files.getGMTFileName().equalsIgnoreCase("")){
            gmtFileNameTextField.setForeground(checkFile(gmt));
            gmtFileNameTextField.setText(gmt);
            dataset1files.setGMTFileName(gmt);
            gmtFileNameTextField.setToolTipText(gmt);
       }

        boolean AutoPopulate = true;
       if(!dataset1files.getGMTFileName().equalsIgnoreCase(gmt)){
          //maybe the files are the same but they are in different directories
          File currentGMTFilename = new File(dataset1files.getGMTFileName());
          File newGMTFilename = new File(gmt);
          if(!currentGMTFilename.getName().equalsIgnoreCase(newGMTFilename.getName())){
              int answer = JOptionPane.showConfirmDialog(this,"This analysis GMT file does not match the previous dataset loaded.\n If you want to use the current GMT file but still use the new rpt file to populated fields press YES,\n If you want to change the GMT file to the one in this rpt file and populate the fields with the rpt file press NO,\n to choose a different rpt file press CANCEL\n","GMT files name mismatch", JOptionPane.YES_NO_CANCEL_OPTION);
              if(answer == JOptionPane.NO_OPTION){
                gmtFileNameTextField.setForeground(checkFile(gmt));
                gmtFileNameTextField.setText(gmt);
                dataset1files.setGMTFileName(gmt);
                gmtFileNameTextField.setToolTipText(gmt);
              }
              else if(answer == JOptionPane.CANCEL_OPTION)
                        AutoPopulate = false;
           }
       }
       if(AutoPopulate){

            ds2RanksTextField.setForeground(checkFile(rnk));
            ds2RanksTextField.setText(rnk);
            dataset2files.setRankedFile(rnk);
            ds2RanksTextField.setToolTipText(rnk);
            
            //dataset 2 needs the gmt file as well
            dataset2files.setGMTFileName(gmt);
            
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
        
        //use the original gmt if we can find it.  If we can't find it resort to using the the one from edb directory
        //with two datasets this will be a problem as the gmt files are filtered by the expression file
        // before being stored in the edb directory
        String currentDir = rptFile.getParent();
        if(!(new File(gmt)).exists()){
        		File temp = new File(currentDir, "edb/gene_sets.gmt");
        		if(temp.exists())
        			gmt = temp.getAbsolutePath();
        }
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
            				dataset1files.setTemp_class1(setClasses(classes_split[0]));
            				dataset1files.setPhenotype1(phenotype1);
            				dataset1files.setPhenotype2(phenotype2);

            				ds1Phenotype1TextField.setText(phenotype1);
            				ds1Phenotype1TextField.setValue(phenotype1);
            				ds1Phenotype2TextField.setText(phenotype2);
            				ds1Phenotype2TextField.setValue(phenotype2);
            				ds1ClassesTextField.setValue(classes_split[0]);
            				ds1ClassesTextField.setForeground(checkFile(classes_split[0]));
            			}
            			else{
            				dataset2files.setClassFile(classes_split[0]);
            				dataset2files.setTemp_class1(setClasses(classes_split[0]));
            				dataset2files.setPhenotype1(phenotype1);
            				dataset2files.setPhenotype2(phenotype2);

            				ds2Phenotype1TextField.setText(phenotype1);
            				ds2Phenotype2TextField.setText(phenotype2);
            				ds2Phenotype1TextField.setValue(phenotype1);
            				ds2Phenotype2TextField.setValue(phenotype2);
            				ds2ClassesTextField.setValue(classes_split[0]);
            				ds2ClassesTextField.setForeground(checkFile(classes_split[0]));
                			
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
            		dataset1files.setPhenotype1(phenotype1);
            		dataset1files.setPhenotype2(phenotype2);

                ds1Phenotype1TextField.setText(phenotype1);
                ds1Phenotype2TextField.setText(phenotype2);
                ds1Phenotype1TextField.setValue(phenotype1);
                ds1Phenotype2TextField.setValue(phenotype2);
            }
            else{
            		dataset2files.setPhenotype1(phenotype1);
            		dataset2files.setPhenotype2(phenotype2);

                ds2Phenotype1TextField.setText(phenotype1);
                ds2Phenotype2TextField.setText(phenotype2);
                ds2Phenotype1TextField.setValue(phenotype1);
                ds2Phenotype2TextField.setValue(phenotype2);
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
                    ds1Phenotype1TextField.setValue(phenotypes_split[0]);
                    ds1Phenotype2TextField.setValue(phenotypes_split[1]);
                }
                else{
                    ds2Phenotype1TextField.setValue(phenotypes_split[0]);
                    ds2Phenotype2TextField.setValue(phenotypes_split[1]);
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
                    gseaHtmlReportFile = "" + out_dir_new + File.separator + job_dir_name + File.separator + "index.html";
                    
                    //If after trying the directory that the rpt file is in doesn't produce valid file names, revert to what
                    //is specified in the rpt.
                    if(!(checkFile(results1) == Color.BLACK))
                    	results1 = "" + out_dir + File.separator + job_dir_name + File.separator + label + "."+ method + "." + timestamp + File.separator + "gsea_report_for_" + phenotype1 + "_" + timestamp + ".xls";
                    if(!(checkFile(results2) == Color.BLACK))
                    	results2 = "" + out_dir + File.separator + job_dir_name + File.separator + label + "."+ method + "." + timestamp + File.separator + "gsea_report_for_" + phenotype2 + "_" + timestamp + ".xls";
                    if(!(checkFile(ranks) == Color.BLACK))                   	               	
                    	ranks = "" + out_dir + File.separator + job_dir_name + File.separator + label + "."+ method + "." + timestamp + File.separator + "ranked_gene_list_" + phenotype1 + "_versus_" + phenotype2 +"_" + timestamp + ".xls";                    
                    if(!(checkFile(gseaHtmlReportFile) == Color.BLACK))                   	               	
                    	gseaHtmlReportFile = "" + out_dir + File.separator + job_dir_name + File.separator + "index.html";
                    
            }

        }

        if(dataset1){
                //check to see the file exists and can be read
                //check to see if the gmt file has already been set
                if(dataset1files.getGMTFileName() == null || dataset1files.getGMTFileName().equalsIgnoreCase("")){
                    gmtFileNameTextField.setForeground(checkFile(gmt));
                    gmtFileNameTextField.setText(gmt);
                    dataset1files.setGMTFileName(gmt);
                    gmtFileNameTextField.setToolTipText(gmt);
                }


                boolean AutoPopulate = true;
                if(!dataset1files.getGMTFileName().equalsIgnoreCase(gmt)){
                    //maybe the files are the same but they are in different directories
                    File currentGMTFilename = new File(dataset1files.getGMTFileName());
                    File newGMTFilename = new File(gmt);
                    if(!currentGMTFilename.getName().equalsIgnoreCase(newGMTFilename.getName())){
                        int answer = JOptionPane.showConfirmDialog(this,"This analysis GMT file does not match the previous dataset loaded.\n If you want to use the current GMT file but still use the new rpt file to populated fields press YES,\n If you want to change the GMT file to the one in this rpt file and populate the fields with the rpt file press NO,\n to choose a different rpt file press CANCEL\n","GMT files name mismatch", JOptionPane.YES_NO_CANCEL_OPTION);
                        if(answer == JOptionPane.NO_OPTION){
                            gmtFileNameTextField.setForeground(checkFile(gmt));
                            gmtFileNameTextField.setText(gmt);
                            dataset1files.setGMTFileName(gmt);
                            gmtFileNameTextField.setToolTipText(gmt);
                        }
                        else if(answer == JOptionPane.CANCEL_OPTION)
                            AutoPopulate = false;
                    }
                }
                if(AutoPopulate){
                    gctFileName1TextField.setForeground(checkFile(data));
                    gctFileName1TextField.setText(data);
                    dataset1files.setExpressionFileName(data);
                    gctFileName1TextField.setToolTipText(data);

                    ds1RanksTextField.setForeground(checkFile(ranks));
                    ds1RanksTextField.setText(ranks);
                    dataset1files.setRankedFile(ranks);
                    ds1RanksTextField.setToolTipText(ranks);

                    dataset1files.setEnrichmentFileName1(results1);
                    dataset1files.setEnrichmentFileName2(results2);
                    dataset1files.setGseaHtmlReportFile(gseaHtmlReportFile);
                    this.setDatasetnames(results1,results2,dataset1);
                }
        }
        else{
           if(dataset1files.getGMTFileName() == null || dataset1files.getGMTFileName().equalsIgnoreCase("")){
                gmtFileNameTextField.setForeground(checkFile(gmt));
                gmtFileNameTextField.setText(gmt);
                dataset1files.setGMTFileName(gmt);
                gmtFileNameTextField.setToolTipText(gmt);
           }

            boolean AutoPopulate = true;
           if(!dataset1files.getGMTFileName().equalsIgnoreCase(gmt)){
              //maybe the files are the same but they are in different directories
              File currentGMTFilename = new File(dataset1files.getGMTFileName());
              File newGMTFilename = new File(gmt);
              if(!currentGMTFilename.getName().equalsIgnoreCase(newGMTFilename.getName())){
                  int answer = JOptionPane.showConfirmDialog(this,"This analysis GMT file does not match the previous dataset loaded.\n If you want to use the current GMT file but still use the new rpt file to populated fields press YES,\n If you want to change the GMT file to the one in this rpt file and populate the fields with the rpt file press NO,\n to choose a different rpt file press CANCEL\n","GMT files name mismatch", JOptionPane.YES_NO_CANCEL_OPTION);
                  if(answer == JOptionPane.NO_OPTION){
                    gmtFileNameTextField.setForeground(checkFile(gmt));
                    gmtFileNameTextField.setText(gmt);
                    dataset1files.setGMTFileName(gmt);
                    gmtFileNameTextField.setToolTipText(gmt);
                  }
                  else if(answer == JOptionPane.CANCEL_OPTION)
                            AutoPopulate = false;
               }
           }
           if(AutoPopulate){
        	   
               dataset2files.setGMTFileName(gmt);

               gctFileName2TextField.setForeground(checkFile(data));
                gctFileName2TextField.setText(data);
                dataset2files.setExpressionFileName(data);
                gctFileName2TextField.setToolTipText(data);

                ds2RanksTextField.setForeground(checkFile(ranks));
                ds2RanksTextField.setText(ranks);
                dataset2files.setRankedFile(ranks);
                ds2RanksTextField.setToolTipText(ranks);

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
               dataset1FileNameTextField.setForeground(checkFile(file1));
               dataset1FileNameTextField.setText(file1 );
               dataset1FileNameTextField.setToolTipText(file1 );

               dataset1FileName2TextField.setForeground(checkFile(file2));
               dataset1FileName2TextField.setText(file2 );
               dataset1FileName2TextField.setToolTipText(file2 );
           }
           else{
               dataset2FileNameTextField.setForeground(checkFile(file1));
               dataset2FileNameTextField.setText(file1 );
               dataset2FileNameTextField.setToolTipText(file1 );

               dataset2FileName2TextField.setForeground(checkFile(file2));
               dataset2FileName2TextField.setText(file2 );
               dataset2FileName2TextField.setToolTipText(file2 );
           }
       }

    /**
     * Parse class file (The class file is a GSEA specific file that specifyies which phenotype
     * each column of the expression file belongs to.)  The class file can only be associated with
     * an analysis when dataset specifications are specified initially using an rpt file.
     *
     * @param classFile - name of class file
     * @return String array of the phenotypes of each column in the expression array
     */
    private String[] setClasses(String classFile){

        File f = new File(classFile);

        //deal with legacy issue, if a session file has the class file set but
        //it didn't actually save the classes yet.
        if(!f.exists()){
           return null;
        }        
        //check to see if the file was opened successfully

        if(!classFile.equalsIgnoreCase(null)) {
        		try{
        			InputStream reader = streamUtil.getInputStream(classFile);
        			String fullText2 = new Scanner(reader,"UTF-8").useDelimiter("\\A").next();


        			String[] lines2 = fullText2.split("\r\n?|\n");
        			
        			/*
        			 * GSEA class files will have 3 lines in the following format:
        			 * 	6 2 1
						# R9C_8W WT_8W
						R9C_8W R9C_8W R9C_8W WT_8W WT_8W WT_8W 
						
						If the file has 3 lines assume it is a GSEA and get the phenotypes from the third line.
						If the file only has 1 line assume that it is a generic class file and get the phenotypes from the single line

        			 */

        			//the class file can be split by a space or a tab
        			String[] classes=null;
        			if(lines2.length >= 3)
        				classes = lines2[2].split("\\s");
        			else if(lines2.length == 1)
        				classes = lines2[0].split("\\s");


        			//the third line of the class file defines the classes
        			return classes;
        		}catch (IOException ie){
        	   		System.out.println("unable to open class file: " + classFile);
        	   		return null;
           }
        }
        else{
            String[] def_pheno = {"Na_pos","NA_neg"};
            return def_pheno;
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
	private void selectAnalysisTypeActionPerformed(ActionEvent evt) {
		String analysisType = evt.getActionCommand();

		if (analysisType.equalsIgnoreCase(EnrichmentMapParameters.method_GSEA))
			params.setMethod(EnrichmentMapParameters.method_GSEA);
		else if (analysisType.equalsIgnoreCase(EnrichmentMapParameters.method_generic))
			params.setMethod(EnrichmentMapParameters.method_generic);
		else if (analysisType.equalsIgnoreCase(EnrichmentMapParameters.method_Specialized))
			params.setMethod(EnrichmentMapParameters.method_Specialized);

		updatePanel();
	}

	/**
	 * update the panel to contain the values that are
	 * defined in the stored dataset files.  This methos is used when the type of analysis is
	 * changed (gsea to generic or vice versa).  The user wants what ever info they have already
	 * entered to transfered over even though they changed the type of analysis
	 *
	 */
	private void updatePanel() {
		// no need to change the gmt file as it should stay the same no matter which radio button was selected
		updateDatasetsPanel();

		// dataset 1
		if (this.dataset1files != null) {
			// check to see if the user had already entered anything into the newly created Dataset Frame
			if (dataset1files.getEnrichmentFileName1() != null) {
				String file = dataset1files.getEnrichmentFileName1();
				dataset1FileNameTextField.setText(file);
				dataset1FileNameTextField.setToolTipText(file);
				dataset1FileNameTextField.setForeground(checkFile(new File(file).getAbsolutePath()));
			}

			if (dataset1files.getExpressionFileName() != null) {
				gctFileName1TextField.setText(dataset1files.getExpressionFileName());
				gctFileName1TextField.setToolTipText(dataset1files.getExpressionFileName());
			}
      
			if (dataset1files.getRankedFile() != null) {
				ds1RanksTextField.setText(dataset1files.getRankedFile());
				ds1RanksTextField.setToolTipText(dataset1files.getRankedFile());
			}

			if (dataset2files.getEnrichmentFileName1() != null) {
				dataset2FileNameTextField.setText(dataset2files.getEnrichmentFileName1());
				dataset2FileNameTextField.setToolTipText(dataset2files.getEnrichmentFileName1());
			}
			if (dataset2files.getExpressionFileName() != null) {
				gctFileName2TextField.setText(dataset2files.getExpressionFileName());
				gctFileName2TextField.setToolTipText(dataset2files.getExpressionFileName());
			}
			if (dataset2files.getRankedFile() != null) {
				ds2RanksTextField.setText(dataset2files.getRankedFile());
				ds2RanksTextField.setToolTipText(dataset2files.getRankedFile());
			}

			// Special case with Enrichment results file 2 (there should only be two enrichment
			if (dataset1files.getExpressionFileName() != null) {
				String file = dataset1files.getExpressionFileName();
				gctFileName1TextField.setText(file);
				gctFileName1TextField.setToolTipText(file);
				gctFileName1TextField.setForeground(checkFile(new File(file).getAbsolutePath()));
			}

			if (dataset1files.getRankedFile() != null) {
				String file = dataset1files.getRankedFile();
				ds1RanksTextField.setText(file);
				ds1RanksTextField.setToolTipText(file);
				ds1RanksTextField.setForeground(checkFile(new File(file).getAbsolutePath()));
			}
			if (dataset1files.getPhenotype1() != null) {
				ds1Phenotype1TextField.setText(dataset1files.getPhenotype1());
				ds1Phenotype1TextField.setValue(dataset1files.getPhenotype1());
				ds1Phenotype1TextField.setToolTipText(dataset1files.getPhenotype1());
			}
			if (dataset1files.getPhenotype2() != null) {
				ds1Phenotype2TextField.setText(dataset1files.getPhenotype2());
				ds1Phenotype2TextField.setValue(dataset1files.getPhenotype2());
				ds1Phenotype2TextField.setToolTipText(dataset1files.getPhenotype2());
			}

			// Special case with Enrichment results file 2 (there should only be two enrichment
			// Files if the analysis specified is GSEA. If the user has loaded from an RPT and
			// then changes the type of analysis there shouldn't be an extra file
			if (params.getMethod().equalsIgnoreCase(EnrichmentMapParameters.method_GSEA)) {
				if (dataset1files.getEnrichmentFileName2() != null) {
					String file = dataset1files.getEnrichmentFileName2();
					dataset1FileName2TextField.setText(file);
					dataset1FileName2TextField.setToolTipText(file);
					dataset1FileName2TextField.setForeground(checkFile(new File(file).getAbsolutePath()));
				}
			} else {
				if ((dataset1files.getEnrichmentFileName2() != null)) {
					JOptionPane.showMessageDialog(this, "Running Enrichment Map with Generic input "
							+ "allows for only one enrichment results file.\n  The second file specified has been removed.");
					if (dataset1files.getEnrichmentFileName2() != null)
						dataset1files.setEnrichmentFileName2(null);

				}
			}
		}
		
		if (this.dataset2files != null) {
			if (dataset2files.getEnrichmentFileName1() != null) {
				String file = dataset2files.getEnrichmentFileName1();
				dataset2FileNameTextField.setText(file);
				dataset2FileNameTextField.setToolTipText(file);
				dataset2FileNameTextField.setForeground(checkFile(new File(file).getAbsolutePath()));
			}
			if (dataset2files.getExpressionFileName() != null) {
				String file = dataset2files.getExpressionFileName();
				gctFileName2TextField.setText(file);
				gctFileName2TextField.setToolTipText(file);
				gctFileName2TextField.setForeground(checkFile(new File(file).getAbsolutePath()));
			}
			if (dataset2files.getRankedFile() != null) {
				String file = dataset2files.getRankedFile();
				ds2RanksTextField.setText(file);
				ds2RanksTextField.setToolTipText(file);
				ds2RanksTextField.setForeground(checkFile(new File(file).getAbsolutePath()));
			}
			
			// update the phenotypes
			if (dataset2files.getPhenotype1() != null) {
				ds2Phenotype1TextField.setText(dataset2files.getPhenotype1());
				ds2Phenotype1TextField.setValue(dataset2files.getPhenotype1());
				ds2Phenotype1TextField.setToolTipText(dataset2files.getPhenotype1());
			}
			if (dataset2files.getPhenotype2() != null) {
				ds2Phenotype2TextField.setText(dataset2files.getPhenotype2());
				ds2Phenotype2TextField.setValue(dataset2files.getPhenotype2());
				ds2Phenotype2TextField.setToolTipText(dataset2files.getPhenotype2());
			}

			// Special case with Enrichment results file 2 (there should only be two enrichment
			// Files if the analysis specified is GSEA. If the user has loaded from an RPT and
			// then changes the type of analysis there shouldn't be an extra file
			if (params.getMethod().equalsIgnoreCase(EnrichmentMapParameters.method_GSEA)) {
				if (dataset2files.getEnrichmentFileName2() != null) {
					String file = dataset2files.getEnrichmentFileName2();
					dataset2FileName2TextField.setText(file);
					dataset2FileName2TextField.setToolTipText(file);
					dataset2FileName2TextField.setForeground(checkFile(new File(file).getAbsolutePath()));
				}
			} else {
				if ((dataset2files.getEnrichmentFileName2() != null)) {
					if (dataset2files.getEnrichmentFileName2() != null)
						dataset2files.setEnrichmentFileName2(null);

				}
			}
		}
	}

    //Action listeners for buttons in input panel

  	/**
  	 * Activate or de-activate scientific notation
  	 * 
  	 */
  	private void selectScientificNotationActionPerformed(ActionEvent evt){
  		if(scinot.isSelected()){
  			this.pvalueTextField.setFormatterFactory(new AbstractFormatterFactory() {

  		        @Override
  		        public AbstractFormatter getFormatter(JFormattedTextField tf) {
  		            NumberFormat format= new DecimalFormat("0.######E00");
  		            format.setMinimumFractionDigits(0);
		            format.setMaximumFractionDigits(12);
  		            InternationalFormatter formatter = new InternationalFormatter(format);
  		            formatter.setAllowsInvalid(true);
  		            return formatter;
  		        }
  		    });
  			
  			this.qvalueTextField.setFormatterFactory(new AbstractFormatterFactory() {

  		        @Override
  		        public AbstractFormatter getFormatter(JFormattedTextField tf) {
  		            NumberFormat format= new DecimalFormat("0.######E00");
  		            format.setMinimumFractionDigits(0);
		            format.setMaximumFractionDigits(12);
  		            InternationalFormatter formatter = new InternationalFormatter(format);
  		            formatter.setAllowsInvalid(true);
  		            return formatter;
  		        }
  		    });
  			
  		}
  		else{
  			this.pvalueTextField.setFormatterFactory(new AbstractFormatterFactory() {

  		        @Override
  		        public AbstractFormatter getFormatter(JFormattedTextField tf) {
  		            NumberFormat format= new DecimalFormat();
  		            format.setMinimumFractionDigits(1);
  		            format.setMaximumFractionDigits(12);
  		            InternationalFormatter formatter = new InternationalFormatter(format);
  		            formatter.setAllowsInvalid(true);
  		            return formatter;
  		        }
  		    });
  			this.qvalueTextField.setFormatterFactory(new AbstractFormatterFactory() {

  		        @Override
  		        public AbstractFormatter getFormatter(JFormattedTextField tf) {
  		            NumberFormat format= new DecimalFormat();
  		            format.setMinimumFractionDigits(1);
  		            format.setMaximumFractionDigits(12);
  		            InternationalFormatter formatter = new InternationalFormatter(format);
  		            formatter.setAllowsInvalid(true);
  		            return formatter;
  		        }
  		    });
  		}
  	}
  	
    /**
     * jaccard or overlap radio button action listener
     */
    private void selectJaccardOrOverlapActionPerformed(ActionEvent evt) {
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
	private void selectGMTFileButtonActionPerformed(ActionEvent evt) {

            // Create FileFilter
           FileChooserFilter filter = new FileChooserFilter("All GMT Files","gmt" );          
           
           //the set of filter (required by the file util method
           ArrayList<FileChooserFilter> all_filters = new ArrayList<FileChooserFilter>();
           all_filters.add(filter);
           // Get the file name
           File file = fileUtil.getFile(SwingUtil.getWindowInstance(this),"Import GMT File", FileUtil.LOAD,all_filters  );
           if(file != null) {
               gmtFileNameTextField.setForeground(checkFile(file.getAbsolutePath()));
               gmtFileNameTextField.setText(file.getAbsolutePath());
               dataset1files.setGMTFileName(file.getAbsolutePath());
               gmtFileNameTextField.setToolTipText(file.getAbsolutePath());
           }
       }

    /**
     * gct/expression 1 file selector action listener
     */
	private void selectGCTFileButtonActionPerformed(ActionEvent evt) {
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
           File file = fileUtil.getFile(SwingUtil.getWindowInstance(this),"Import GCT File", FileUtil.LOAD, all_filters);
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
                    gctFileName1TextField.setForeground(checkFile(file.getAbsolutePath()));
                    gctFileName1TextField.setText(file.getAbsolutePath());
                    dataset1files.setExpressionFileName(file.getAbsolutePath());
                    gctFileName1TextField.setToolTipText(file.getAbsolutePath());
               }
           }
       }

	/**
	 * gct/expression 2 file selector action listener
	 */
	private void selectGCTFileButton2ActionPerformed(ActionEvent evt) {
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
         File file = fileUtil.getFile(SwingUtil.getWindowInstance(this),"Import GCT File", FileUtil.LOAD, all_filters);
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
               gctFileName2TextField.setForeground(checkFile(file.getAbsolutePath()));
               gctFileName2TextField.setText(file.getAbsolutePath());
               //check to see that there is a dataset2
               
               dataset2files.setExpressionFileName(file.getAbsolutePath());
               
               gctFileName2TextField.setToolTipText(file.getAbsolutePath());
             }
             params.setTwoDatasets(true);
         }
     }

	/**
	 * enrichment results 1 file selector action listener
	 */
	private void selectDataset1FileButtonActionPerformed(ActionEvent evt) {
		// Create FileFilter
         FileChooserFilter filter_xls = new FileChooserFilter("gct Files","xls" );          
         FileChooserFilter filter_rpt = new FileChooserFilter("rpt Files","rpt" );
         FileChooserFilter filter_bgo = new FileChooserFilter("rnk Files","bgo" );
         FileChooserFilter filter_txt = new FileChooserFilter("txt Files","txt" );
         FileChooserFilter filter_edb = new FileChooserFilter("edb Files","edb" );
         FileChooserFilter filter_tsv = new FileChooserFilter("tsv Files","tsv" );
         
         //the set of filter (required by the file util method
         ArrayList<FileChooserFilter> all_filters = new ArrayList<FileChooserFilter>();
         all_filters.add(filter_xls);
         all_filters.add(filter_rpt);
         all_filters.add(filter_bgo);
         all_filters.add(filter_txt);
         all_filters.add(filter_edb);
         all_filters.add(filter_tsv);
         
          // Get the file name
          File file = fileUtil.getFile(SwingUtil.getWindowInstance(this),"Import dataset result File", FileUtil.LOAD, all_filters);

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
                dataset1FileNameTextField.setForeground(checkFile(file.getAbsolutePath()));
                dataset1FileNameTextField.setText(file.getAbsolutePath() );
                dataset1files.setEnrichmentFileName1(file.getAbsolutePath());
                dataset1FileNameTextField.setToolTipText(file.getAbsolutePath() );
             }

        }
    }

    /**
	 * enrichment results 2 file selector action listener
	 */
	private void selectDataset1File2ButtonActionPerformed(ActionEvent evt) {
    	 // Create FileFilter
        FileChooserFilter filter_xls = new FileChooserFilter("gct Files","xls" );          
        FileChooserFilter filter_rpt = new FileChooserFilter("rpt Files","rpt" );
        FileChooserFilter filter_bgo = new FileChooserFilter("rnk Files","bgo" );
        FileChooserFilter filter_txt = new FileChooserFilter("txt Files","txt" );
        FileChooserFilter filter_edb = new FileChooserFilter("edb Files","edb" );
        FileChooserFilter filter_tsv = new FileChooserFilter("tsv Files","tsv" );
        
        //the set of filter (required by the file util method
        ArrayList<FileChooserFilter> all_filters = new ArrayList<FileChooserFilter>();
        all_filters.add(filter_xls);
        all_filters.add(filter_rpt);
        all_filters.add(filter_bgo);
        all_filters.add(filter_txt);
        all_filters.add(filter_edb);
        all_filters.add(filter_tsv);
                   
         // Get the file name
         File file = fileUtil.getFile(SwingUtil.getWindowInstance(this),"Import dataset result File", FileUtil.LOAD, all_filters);

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
                dataset1FileName2TextField.setForeground(checkFile(file.getAbsolutePath()));
                dataset1FileName2TextField.setText(file.getAbsolutePath() );
                dataset1files.setEnrichmentFileName2(file.getAbsolutePath());
                dataset1FileName2TextField.setToolTipText(file.getAbsolutePath() );
             }

        }
    }

	/**
	 * enrichment results 1 file selector action listener
	 */
	private void selectDataset2FileButtonActionPerformed(ActionEvent evt) {
		// Create FileFilter
        FileChooserFilter filter_xls = new FileChooserFilter("gct Files","xls" );          
        FileChooserFilter filter_rpt = new FileChooserFilter("rpt Files","rpt" );
        FileChooserFilter filter_bgo = new FileChooserFilter("rnk Files","bgo" );
        FileChooserFilter filter_txt = new FileChooserFilter("txt Files","txt" );
        FileChooserFilter filter_edb = new FileChooserFilter("edb Files","edb" );
        FileChooserFilter filter_tsv = new FileChooserFilter("tsv Files","tsv" );
        
        //the set of filter (required by the file util method
        ArrayList<FileChooserFilter> all_filters = new ArrayList<FileChooserFilter>();
        all_filters.add(filter_xls);
        all_filters.add(filter_rpt);
        all_filters.add(filter_bgo);
        all_filters.add(filter_txt);
        all_filters.add(filter_edb);
        all_filters.add(filter_tsv);
                   
         // Get the file name
         File file = fileUtil.getFile(SwingUtil.getWindowInstance(this),"Import dataset result File", FileUtil.LOAD, all_filters);

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
              dataset2FileNameTextField.setForeground(checkFile(file.getAbsolutePath()));
              dataset2FileNameTextField.setText(file.getAbsolutePath() );
              
              dataset2files.setEnrichmentFileName1(file.getAbsolutePath());
              
              dataset2FileNameTextField.setToolTipText(file.getAbsolutePath() );
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
             ActionEvent evt) {

    	 // Create FileFilter
         FileChooserFilter filter_xls = new FileChooserFilter("gct Files","xls" );          
         FileChooserFilter filter_rpt = new FileChooserFilter("rpt Files","rpt" );
         FileChooserFilter filter_bgo = new FileChooserFilter("rnk Files","bgo" );
         FileChooserFilter filter_txt = new FileChooserFilter("txt Files","txt" );
         FileChooserFilter filter_edb = new FileChooserFilter("edb Files","edb" );
         FileChooserFilter filter_tsv = new FileChooserFilter("tsv Files","tsv" );
         
         //the set of filter (required by the file util method
         ArrayList<FileChooserFilter> all_filters = new ArrayList<FileChooserFilter>();
         all_filters.add(filter_xls);
         all_filters.add(filter_rpt);
         all_filters.add(filter_bgo);
         all_filters.add(filter_txt);
         all_filters.add(filter_edb);
         all_filters.add(filter_tsv);
         
          // Get the file name
          File file = fileUtil.getFile(SwingUtil.getWindowInstance(this),"Import dataset result File", FileUtil.LOAD, all_filters);

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
              dataset2FileName2TextField.setForeground(checkFile(file.getAbsolutePath()));
              dataset2FileName2TextField.setText(file.getAbsolutePath() );
              
              dataset2files.setEnrichmentFileName2(file.getAbsolutePath());
            
              dataset2FileName2TextField.setToolTipText(file.getAbsolutePath() );
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
               ActionEvent evt) {

         //For GSEA input, Check to see if there is already a rank file defined and if it was from the rpt
         if(params.getMethod().equalsIgnoreCase(EnrichmentMapParameters.method_GSEA) &&
        		 	LoadedFromRpt_dataset1 && !ds1RanksTextField.getText().equalsIgnoreCase(""))
             JOptionPane.showMessageDialog(application.getJFrame(),"GSEA defined rank file is in a specific order and is used to calculate the leading edge.  \n If you change this file the leading edges will be calculated incorrectly.","Trying to change pre-defined GSEA rank file",JOptionPane.WARNING_MESSAGE);

         // Create FileFilter
         FileChooserFilter filter_rnk = new FileChooserFilter("rnk Files","rnk" );          
         FileChooserFilter filter_txt = new FileChooserFilter("txt Files","txt" );
         
         //the set of filter (required by the file util method
         ArrayList<FileChooserFilter> all_filters = new ArrayList<FileChooserFilter>();
         all_filters.add(filter_rnk);
         all_filters.add(filter_txt);

                    
          // Get the file name
          File file = fileUtil.getFile(SwingUtil.getWindowInstance(this),"Import rank File", FileUtil.LOAD, all_filters);

        if(file != null) {
                ds1RanksTextField.setForeground(checkFile(file.getAbsolutePath()));
                ds1RanksTextField.setText(file.getAbsolutePath() );
                dataset1files.setRankedFile(file.getAbsolutePath());
                ds1RanksTextField.setToolTipText(file.getAbsolutePath() );


        }
    }

     /**
      * class 1 file selector action listener
      *
      * @param evt
      */
      private void selectClass1FileButtonActionPerformed(
                ActionEvent evt) {
          
    	  	// Create FileFilter
          FileChooserFilter filter_cls = new FileChooserFilter("cls Files","cls" );          
          FileChooserFilter filter_txt = new FileChooserFilter("txt Files","txt" );
          
          //the set of filter (required by the file util method
          ArrayList<FileChooserFilter> all_filters = new ArrayList<FileChooserFilter>();
          all_filters.add(filter_cls);
          all_filters.add(filter_txt);
         
         // Get the file name
          File file = fileUtil.getFile(SwingUtil.getWindowInstance(this),"Import class file", FileUtil.LOAD, all_filters);
          
         if(file != null) {
                 ds1ClassesTextField.setForeground(checkFile(file.getAbsolutePath()));
                 ds1ClassesTextField.setText(file.getAbsolutePath() );
                 dataset1files.setClassFile(file.getAbsolutePath());
 				 dataset1files.setTemp_class1(setClasses(file.getAbsolutePath()));
                 ds1ClassesTextField.setToolTipText(file.getAbsolutePath() );


         }
     }
     
      /**
       * class 1 file selector action listener
       *
       * @param evt
       */
       private void selectClass2FileButtonActionPerformed(
                 ActionEvent evt) {
           
    	   // Create FileFilter
           FileChooserFilter filter_cls = new FileChooserFilter("cls Files","cls" );          
           FileChooserFilter filter_txt = new FileChooserFilter("txt Files","txt" );
           
           //the set of filter (required by the file util method
           ArrayList<FileChooserFilter> all_filters = new ArrayList<FileChooserFilter>();
           all_filters.add(filter_cls);
           all_filters.add(filter_txt);
          
          // Get the file name
           File file = fileUtil.getFile(SwingUtil.getWindowInstance(this),"Import class file", FileUtil.LOAD, all_filters);
           
          if(file != null) {
                  ds2ClassesTextField.setForeground(checkFile(file.getAbsolutePath()));
                  ds2ClassesTextField.setText(file.getAbsolutePath() );
                  dataset2files.setClassFile(file.getAbsolutePath());
  				  dataset2files.setTemp_class1(setClasses(file.getAbsolutePath()));
                  ds2ClassesTextField.setToolTipText(file.getAbsolutePath() );


          }
      }
      
      
    /**
     * ranks 2 file selector action listener
     *
     * @param evt
     */
     private void selectRank2FileButtonActionPerformed(
               ActionEvent evt) {

        //For GSEA input, Check to see if there is already a rank file defined and if it was from the rpt
         if(params.getMethod().equalsIgnoreCase(EnrichmentMapParameters.method_GSEA) &&
        		 	LoadedFromRpt_dataset2 && !ds2RanksTextField.getText().equalsIgnoreCase(""))
             JOptionPane.showMessageDialog(application.getJFrame(),"GSEA defined rank file is in a specific order and is used to calculate the leading edge.  \n If you change this file the leading edges will be calculated incorrectly.","Trying to change pre-defined GSEA rank file",JOptionPane.WARNING_MESSAGE);


      // Create FileFilter
         FileChooserFilter filter_rnk = new FileChooserFilter("rnk Files","rnk" );          
         FileChooserFilter filter_txt = new FileChooserFilter("txt Files","txt" );
         
         //the set of filter (required by the file util method
         ArrayList<FileChooserFilter> all_filters = new ArrayList<FileChooserFilter>();
         all_filters.add(filter_rnk);
         all_filters.add(filter_txt);

                    
          // Get the file name
          File file = fileUtil.getFile(SwingUtil.getWindowInstance(this),"Import rank File", FileUtil.LOAD, all_filters);

        if(file != null) {
                ds2RanksTextField.setForeground(checkFile(file.getAbsolutePath()));
                ds2RanksTextField.setText(file.getAbsolutePath() );

                	dataset2files.setRankedFile(file.getAbsolutePath());
                
                ds2RanksTextField.setToolTipText(file.getAbsolutePath() );


        }
    }

    /**
     *  Clear the current panel and clear the params associated with this panel
     */
    private void resetPanel() {
        this.params = emParamsFactory.get();
        this.panelUpdate = false;
        //reset the datafiles as well
        this.dataset1files = new DataSetFiles();
        this.dataset2files = new DataSetFiles();

        gmtFileNameTextField.setText("");
        gmtFileNameTextField.setToolTipText(null);
        gmtFileNameTextField.setForeground(Color.black);

        gctFileName1TextField.setText("");
        gctFileName1TextField.setToolTipText(null);
        gctFileName1TextField.setForeground(Color.black);
        gctFileName2TextField.setText("");
        gctFileName2TextField.setToolTipText(null);
        gctFileName2TextField.setForeground(Color.black);

        dataset1FileNameTextField.setText("");
        dataset1FileNameTextField.setToolTipText(null);
        dataset1FileNameTextField.setForeground(Color.black);
        dataset1FileName2TextField.setText("");
        dataset1FileName2TextField.setToolTipText(null);
        dataset1FileName2TextField.setForeground(Color.black);

        dataset2FileNameTextField.setText("");
        dataset2FileNameTextField.setToolTipText(null);
        dataset2FileNameTextField.setForeground(Color.black);
        dataset2FileName2TextField.setText("");
        dataset2FileName2TextField.setToolTipText(null);
        dataset2FileName2TextField.setForeground(Color.black);

        ds1RanksTextField.setText("");
        ds1RanksTextField.setToolTipText(null);
        ds1RanksTextField.setForeground(Color.black);
        ds2RanksTextField.setText("");
        ds2RanksTextField.setToolTipText(null);
        ds2RanksTextField.setForeground(Color.black);

        ds1Phenotype1TextField.setText(DataSetFiles.default_pheno1);
        ds1Phenotype2TextField.setText(DataSetFiles.default_pheno2);
        ds2Phenotype1TextField.setText(DataSetFiles.default_pheno1);
        ds2Phenotype2TextField.setText(DataSetFiles.default_pheno2);

        ds1Phenotype1TextField.setValue(DataSetFiles.default_pheno1);
        ds1Phenotype2TextField.setValue(DataSetFiles.default_pheno2);
        ds2Phenotype1TextField.setValue(DataSetFiles.default_pheno1);
        ds2Phenotype2TextField.setValue(DataSetFiles.default_pheno2);
        
        ds1ClassesTextField.setText("");
        ds2ClassesTextField.setText("");

        pvalueTextField.setText(Double.toString(params.getPvalue()));
        qvalueTextField.setText(Double.toString(params.getQvalue()));
        coeffecientTextField.setText(Double.toString(params.getSimilarityCutOff()));

        pvalueTextField.setValue(params.getPvalue());
        qvalueTextField.setValue(params.getQvalue());
        coeffecientTextField.setValue(params.getSimilarityCutOff());
        //reset for cleared Panel after .setValue(...) wrongly changed it to "true"
        similarityCutOffChanged = false;

        if(params.getMethod().equalsIgnoreCase(EnrichmentMapParameters.method_GSEA)){
            gseaRadio.setSelected(true);
            genericRadio.setSelected(false);
            davidRadio.setSelected(false);
        }
        else if(params.getMethod().equalsIgnoreCase(EnrichmentMapParameters.method_generic)){
            gseaRadio.setSelected(false);
            genericRadio.setSelected(true);
            davidRadio.setSelected(false);
        }
        else if(params.getMethod().equalsIgnoreCase(EnrichmentMapParameters.method_Specialized)){
            gseaRadio.setSelected(false);
            genericRadio.setSelected(false);
            davidRadio.setSelected(true);
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
    public void updateContents(EnrichmentMap map) {
    	resetPanel();
    	
        this.params = emParamsFactory.get();
        this.params.copyValuesFrom(map);
        this.panelUpdate = true;
        
        if(params.getFiles().containsKey(LegacySupport.DATASET1))
  		  	this.dataset1files = params.getFiles().get(LegacySupport.DATASET1);
  	  	if(params.getFiles().containsKey(LegacySupport.DATASET2))
  	  		this.dataset2files = params.getFiles().get(LegacySupport.DATASET2);

  	    gmtFileNameTextField.setText((dataset1files.getGMTFileName() == null)? "" :dataset1files.getGMTFileName());
  	    gmtFileNameTextField.setForeground(checkFile(gmtFileNameTextField.getText()));
  	    gctFileName1TextField.setText((dataset1files.getExpressionFileName() == null)? "":dataset1files.getExpressionFileName());
  	    gctFileName1TextField.setForeground(checkFile(gctFileName1TextField.getText()));
  	    dataset1FileNameTextField.setText((dataset1files.getEnrichmentFileName1()==null)?"":dataset1files.getEnrichmentFileName1());
  	    dataset1FileNameTextField.setForeground(checkFile(dataset1FileNameTextField.getText()));
  	    dataset1FileName2TextField.setText((dataset1files.getEnrichmentFileName2()==null)?"":dataset1files.getEnrichmentFileName2());
  	    dataset1FileName2TextField.setForeground(checkFile(dataset1FileName2TextField.getText()));
  	    ds1RanksTextField.setText((dataset1files.getRankedFile()==null)?"":dataset1files.getRankedFile());
  	    ds1RanksTextField.setForeground(checkFile(ds1RanksTextField.getText()));
        
        gctFileName2TextField.setText((dataset2files.getExpressionFileName() == null)? "":dataset2files.getExpressionFileName());
        gctFileName2TextField.setForeground(checkFile(gctFileName2TextField.getText()));
        dataset2FileNameTextField.setText((dataset2files.getEnrichmentFileName1()==null)?"":dataset2files.getEnrichmentFileName1());
        dataset2FileNameTextField.setForeground(checkFile(dataset2FileNameTextField.getText()));
        dataset2FileName2TextField.setText((dataset2files.getEnrichmentFileName2()==null)?"":dataset2files.getEnrichmentFileName2());
        dataset2FileName2TextField.setForeground(checkFile(dataset2FileName2TextField.getText()));
        ds2RanksTextField.setText((dataset2files.getRankedFile()==null)?"":dataset2files.getRankedFile());
        ds2RanksTextField.setForeground(checkFile(ds2RanksTextField.getText()));

        ds1Phenotype1TextField.setText(dataset1files.getPhenotype1());
        ds1Phenotype2TextField.setText(dataset1files.getPhenotype2());
        ds2Phenotype1TextField.setText(dataset2files.getPhenotype1());
        ds2Phenotype2TextField.setText(dataset2files.getPhenotype2());

        ds1Phenotype1TextField.setValue(dataset1files.getPhenotype1());
        ds1Phenotype2TextField.setValue(dataset1files.getPhenotype2());
        ds2Phenotype1TextField.setValue(dataset2files.getPhenotype1());
        ds2Phenotype2TextField.setValue(dataset2files.getPhenotype2());
        
        ds1ClassesTextField.setValue(dataset1files.getClassFile());
        ds2ClassesTextField.setValue(dataset2files.getClassFile());

        pvalueTextField.setText(Double.toString(params.getPvalue()));
        qvalueTextField.setText(Double.toString(params.getQvalue()));
        coeffecientTextField.setText(Double.toString(params.getSimilarityCutOff()));

        pvalueTextField.setValue(params.getPvalue());
        qvalueTextField.setValue(params.getQvalue());
        coeffecientTextField.setValue(params.getSimilarityCutOff());
        combinedConstantTextField.setValue(params.getCombinedConstant());

        if(params.getMethod().equalsIgnoreCase(EnrichmentMapParameters.method_GSEA)){
            gseaRadio.setSelected(true);
            genericRadio.setSelected(false);
            davidRadio.setSelected(false);
        }
        else if(params.getMethod().equalsIgnoreCase(EnrichmentMapParameters.method_generic)){
            gseaRadio.setSelected(false);
            genericRadio.setSelected(true);
            davidRadio.setSelected(false);
        }
        else if(params.getMethod().equalsIgnoreCase(EnrichmentMapParameters.method_Specialized)){
            gseaRadio.setSelected(false);
            genericRadio.setSelected(false);
            davidRadio.setSelected(true);
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
		
		//check to see if any of the textboxes have been updated manually
		//Take the textbox value over the one in datasetFiles object
		if(!gctFileName1TextField.getText().equalsIgnoreCase(this.dataset1files.getExpressionFileName()))
			this.dataset1files.setExpressionFileName(gctFileName1TextField.getText());

		if(!dataset1FileNameTextField.getText().equalsIgnoreCase(this.dataset1files.getEnrichmentFileName1()))
			this.dataset1files.setEnrichmentFileName1(dataset1FileNameTextField.getText());
		if(!dataset1FileName2TextField.getText().equalsIgnoreCase(this.dataset1files.getEnrichmentFileName2()))
			this.dataset1files.setEnrichmentFileName2(dataset1FileName2TextField.getText());
		
		if(!ds1RanksTextField.getText().equalsIgnoreCase(this.dataset1files.getRankedFile()))
			this.dataset1files.setRankedFile(ds1RanksTextField.getText());
	
		if(!ds1ClassesTextField.getText().equalsIgnoreCase(this.dataset1files.getClassFile()))
			this.dataset1files.setClassFile(ds1ClassesTextField.getText());
		
		if(!ds1Phenotype1TextField.getText().equalsIgnoreCase(this.dataset1files.getPhenotype1()))
			this.dataset1files.setPhenotype1(ds1Phenotype1TextField.getText());
		if(!ds1Phenotype2TextField.getText().equalsIgnoreCase(this.dataset1files.getPhenotype2()))
			this.dataset1files.setPhenotype2(ds1Phenotype2TextField.getText());
		
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
		
		//check to see if any of the textboxes have been updated manually
		//Take the textbox value over the one in datasetFiles object
		if(!gctFileName2TextField.getText().equalsIgnoreCase(this.dataset2files.getExpressionFileName()))
					this.dataset2files.setExpressionFileName(gctFileName2TextField.getText());

		if(!dataset2FileNameTextField.getText().equalsIgnoreCase(this.dataset2files.getEnrichmentFileName1()))
					this.dataset2files.setEnrichmentFileName1(dataset2FileNameTextField.getText());
		if(!dataset2FileName2TextField.getText().equalsIgnoreCase(this.dataset2files.getEnrichmentFileName2()))
					this.dataset2files.setEnrichmentFileName2(dataset2FileName2TextField.getText());
				
		if(!ds2RanksTextField.getText().equalsIgnoreCase(this.dataset2files.getRankedFile()))
					this.dataset2files.setRankedFile(ds2RanksTextField.getText());
			
		if(!ds2ClassesTextField.getText().equalsIgnoreCase(this.dataset2files.getClassFile()))
					this.dataset2files.setClassFile(ds2ClassesTextField.getText());
				
		if(!ds2Phenotype1TextField.getText().equalsIgnoreCase(this.dataset2files.getPhenotype1()))
					this.dataset2files.setPhenotype1(ds2Phenotype1TextField.getText());
		if(!ds2Phenotype2TextField.getText().equalsIgnoreCase(this.dataset2files.getPhenotype2()))
					this.dataset2files.setPhenotype2(ds2Phenotype2TextField.getText());
				
		
		return dataset2files;
	}

	public void setDataset2files(DataSetFiles dataset2files) {
		this.dataset2files = dataset2files;
	}

	public Component getComponent() {
		return this;
	}

	public CytoPanelName getCytoPanelName() {
		return CytoPanelName.WEST;
	}

	public Icon getIcon() {
		//create an icon for the enrichment map panels
        URL EMIconURL = this.getClass().getResource("enrichmentmap_logo_notext_small.png");
        ImageIcon EMIcon = null;
        if (EMIconURL != null) {
            EMIcon = new ImageIcon(EMIconURL);
        }
		return EMIcon;
	}

	public String getTitle() {
		return "Enrichment Map Input";
	}
    
	private class OptionsPanel extends JPanel implements Scrollable {
		
		@Override
		public Dimension getPreferredScrollableViewportSize() {
			return getPreferredSize();
		}

		@Override
		public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
			return 10;
		}

		@Override
		public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
			return ((orientation == SwingConstants.VERTICAL) ? visibleRect.height : visibleRect.width) - 10;
		}

		@Override
		public boolean getScrollableTracksViewportWidth() {
			return true;
		}

		@Override
		public boolean getScrollableTracksViewportHeight() {
			return false;
		}
	}
}
