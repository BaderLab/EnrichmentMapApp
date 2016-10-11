package org.baderlab.csplugins.enrichmentmap.view;

import static javax.swing.GroupLayout.DEFAULT_SIZE;
import static javax.swing.GroupLayout.PREFERRED_SIZE;
import static org.baderlab.csplugins.enrichmentmap.util.SwingUtil.makeSmall;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;

import javax.swing.ButtonGroup;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.LayoutStyle.ComponentPlacement;

import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMapParameters;
import org.baderlab.csplugins.enrichmentmap.util.SwingUtil;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.io.util.StreamUtil;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.session.CySessionManager;
import org.cytoscape.util.swing.BasicCollapsiblePanel;
import org.cytoscape.util.swing.FileChooserFilter;
import org.cytoscape.util.swing.FileUtil;
import org.cytoscape.util.swing.LookAndFeelUtil;

/**
 * Created by IntelliJ IDEA.
 * User: User
 * Date: 1/28/11
 * Time: 9:17 AM
 * To change this template use File | Settings | File Templates.
 */
@SuppressWarnings("serial")
public class BulkEMCreationPanel extends JPanel implements CytoPanelComponent{

    private FileUtil fileUtil;
    private CySwingApplication application;
    private CyServiceRegistrar registrar;
    private StreamUtil streamUtil;
    private CyApplicationManager applicationManager;
    private CySessionManager cySessionManager;
    
    private EnrichmentMapParameters params;

	// Genesets file related components
    // user specified file names
    private JFormattedTextField gseaResultsDirTextField;

    //directory for GMT and GCT if they are different than the RPT file
    private JFormattedTextField gmtDirectoryTextField;
    private JFormattedTextField gctDirectoryTextField;

    private JFormattedTextField pvalueTextField;
    private JFormattedTextField qvalueTextField;
    private JFormattedTextField coeffecientTextField;

    private JFormattedTextField lowerLimitField;
    private JFormattedTextField upperLimitField;

    private JRadioButton gsea;
    private JRadioButton generic;
    private JRadioButton overlap;
    private JRadioButton jaccard;
    private JRadioButton combined;

    private JRadioButton oneSessionRadio;
    private JRadioButton multiSessionRadio;

    DecimalFormat decFormat; // used in the formatted text fields
    NumberFormat numFormat;

    private int defaultColumns = 15;
    
    private boolean similarityCutOffChanged = false;
    
	public BulkEMCreationPanel(CySwingApplication application, FileUtil fileUtil, CyServiceRegistrar registrar,
			CySessionManager sessionManager, StreamUtil streamUtil, CyApplicationManager applicationManager) {
		params = new EnrichmentMapParameters(sessionManager, streamUtil, applicationManager);
		this.streamUtil = streamUtil;
		this.cySessionManager = sessionManager;
		this.applicationManager = applicationManager;
		this.application = application;
		this.fileUtil = fileUtil;
		this.registrar = registrar;

		if (LookAndFeelUtil.isAquaLAF())
			setOpaque(false);

		// create the three main panels
		JPanel analysisTypePanel = createAnalysisTypePanel();
		JPanel optionsPanel = createOptionsPanel();
		JPanel bottomPanel = createBottomPanel();

		// Add all the vertically aligned components to the main panel
		final GroupLayout layout = new GroupLayout(this);
		setLayout(layout);
		layout.setAutoCreateContainerGaps(false);
		layout.setAutoCreateGaps(false);
		
		layout.setHorizontalGroup(layout.createParallelGroup(Alignment.CENTER, true)
				.addComponent(analysisTypePanel, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(optionsPanel, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(bottomPanel, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
		);
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addComponent(analysisTypePanel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				.addComponent(optionsPanel, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(bottomPanel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
		);
	}

	/**
	 * Creates a panel that holds main user inputs geneset files, datasets and parameters
	 */
	private JPanel createOptionsPanel() {
		JPanel gmtPanel = createGMTPanel();

		BasicCollapsiblePanel paramsPanel = createParametersPanel();
		paramsPanel.setCollapsed(true);

		BasicCollapsiblePanel gmtDirPanel = createDiffGMTGCTDirectory();
		gmtDirPanel.setCollapsed(true);

		JPanel panel = new JPanel();
		GroupLayout layout = new GroupLayout(panel);
		panel.setLayout(layout);
		layout.setAutoCreateContainerGaps(false);
		layout.setAutoCreateGaps(true);

		layout.setHorizontalGroup(layout.createParallelGroup(Alignment.CENTER, true)
				.addComponent(gmtPanel, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(paramsPanel, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
				.addComponent(gmtDirPanel, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
		);
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addComponent(gmtPanel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				.addComponent(paramsPanel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				.addComponent(gmtDirPanel, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
		);
		
		if (LookAndFeelUtil.isAquaLAF())
			panel.setOpaque(false);

		return panel;
	}

    /**
     * Creates a collapsible panel that holds the different paths for the GMT and GCt file.
     *
     * Only required when the user has moved the location of the these files and they are different
     * than the ones defined in the RPT file
     */
	private BasicCollapsiblePanel createDiffGMTGCTDirectory() {
		BasicCollapsiblePanel panel = new BasicCollapsiblePanel("Advanced");

		// add GMT file
		JLabel gmtLabel = new JLabel("Directory containing GMT file:");
		
		JButton selectGMTDirButton = new JButton("Browse...");
		
		gmtDirectoryTextField = new JFormattedTextField();
		gmtDirectoryTextField.setColumns(defaultColumns);
		// components needed for the directory load
		gmtDirectoryTextField.setFont(new java.awt.Font("Dialog", 1, 10));
		gmtDirectoryTextField.addPropertyChangeListener("value", new BulkEMCreationPanel.FormattedTextFieldAction());

		selectGMTDirButton.addActionListener((ActionEvent evt) -> {
			selectGMTDirButtonActionPerformed(evt);
		});

		// add GMT file
		JLabel gctLabel = new JLabel("Directory containing GCT file:");
		
		JButton selectGCTDirButton = new JButton("Browse...");
		gctDirectoryTextField = new JFormattedTextField();
		gctDirectoryTextField.setColumns(defaultColumns);

		// components needed for the directory load
		gctDirectoryTextField.addPropertyChangeListener("value", new BulkEMCreationPanel.FormattedTextFieldAction());

		selectGCTDirButton.addActionListener((ActionEvent evt) -> {
			selectGCTDirButtonActionPerformed(evt);
		});

		// add the ability to specify range of directories to use when building maps.
		// qvalue cutoff input
		String lowerTip =
				"<html>Sets the lower limit<br />" +
				"of the directory number you want to<br />" +
				"use when creating networks.</html>";
		
		JLabel lowerLimitLabel = new JLabel("Lower Limit:");
		lowerLimitLabel.setToolTipText(lowerTip);
		
		lowerLimitField = new JFormattedTextField(numFormat);
		lowerLimitField.setColumns(3);
		lowerLimitField.addPropertyChangeListener("value", new BulkEMCreationPanel.FormattedTextFieldAction());
		lowerLimitField.setToolTipText(lowerTip);
		lowerLimitField.setText("1");
		lowerLimitField.setValue(1);

		// add the ability to specify range of directories to use when building maps.
		// qvalue cutoff input
		String upperTip =
				"<html>Sets the upper limit<br />" +
				"of the directory number you want to<br />" +
				"use when creating networks.</html>";
		
		JLabel upperLimitLabel = new JLabel("Upper Limit:");
		upperLimitLabel.setToolTipText(upperTip);
		
		upperLimitField = new JFormattedTextField(numFormat);
		upperLimitField.setColumns(3);
		upperLimitField.addPropertyChangeListener("value", new BulkEMCreationPanel.FormattedTextFieldAction());
		upperLimitField.setToolTipText(upperTip);
		upperLimitField.setText("1");
		upperLimitField.setValue(1);

		// add check box to indicate if you want to create one session or multiple sessions
		oneSessionRadio = new JRadioButton("One Session File");
		oneSessionRadio.setActionCommand("onesession");
		oneSessionRadio.setSelected(true);
		multiSessionRadio = new JRadioButton("Multiple Session files");
		multiSessionRadio.setActionCommand("multisession");

		multiSessionRadio.setSelected(true);
		oneSessionRadio.setSelected(false);

		ButtonGroup sessionsgroup = new ButtonGroup();
		sessionsgroup.add(oneSessionRadio);
		sessionsgroup.add(multiSessionRadio);

		oneSessionRadio.addActionListener((ActionEvent evt) -> {
			selectOneMultiSessionActionPerformed(evt);
		});

		multiSessionRadio.addActionListener((ActionEvent evt) -> {
			selectOneMultiSessionActionPerformed(evt);
		});

		makeSmall(gmtLabel, gmtDirectoryTextField, selectGMTDirButton);
		makeSmall(gctLabel, gctDirectoryTextField, selectGCTDirButton);
		makeSmall(lowerLimitLabel, lowerLimitField, upperLimitLabel, upperLimitField);
		makeSmall(oneSessionRadio, multiSessionRadio);
		
		// add the components to the panel
		final GroupLayout layout = new GroupLayout(panel.getContentPane());
		panel.getContentPane().setLayout(layout);
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(!LookAndFeelUtil.isAquaLAF());
		
		layout.setHorizontalGroup(layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup(Alignment.TRAILING, true)
						.addComponent(gmtLabel)
						.addComponent(gctLabel)
						.addComponent(lowerLimitLabel)
						.addComponent(upperLimitLabel)
				)
				.addPreferredGap(ComponentPlacement.RELATED)
				.addGroup(layout.createParallelGroup(Alignment.LEADING, true)
						.addGroup(layout.createSequentialGroup()
								.addComponent(gmtDirectoryTextField, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
								.addComponent(selectGMTDirButton, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
						)
						.addGroup(layout.createSequentialGroup()
								.addComponent(gctDirectoryTextField, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
								.addComponent(selectGCTDirButton, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
						)
						.addComponent(lowerLimitField, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
						.addComponent(upperLimitField, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
						.addComponent(oneSessionRadio, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
						.addComponent(multiSessionRadio, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				)
		);
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup(Alignment.CENTER, false)
						.addComponent(gmtLabel)
						.addComponent(gmtDirectoryTextField, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
						.addComponent(selectGMTDirButton, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				)
				.addGroup(layout.createParallelGroup(Alignment.CENTER, false)
						.addComponent(gctLabel)
						.addComponent(gctDirectoryTextField, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
						.addComponent(selectGCTDirButton, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				)
				.addPreferredGap(ComponentPlacement.UNRELATED)
				.addGroup(layout.createParallelGroup(Alignment.CENTER, false)
						.addComponent(lowerLimitLabel)
						.addComponent(lowerLimitField, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				)
				.addGroup(layout.createParallelGroup(Alignment.CENTER, false)
						.addComponent(upperLimitLabel)
						.addComponent(upperLimitField, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
				)
				.addPreferredGap(ComponentPlacement.UNRELATED)
				.addComponent(oneSessionRadio)
				.addComponent(multiSessionRadio)
		);
		
		if (LookAndFeelUtil.isAquaLAF())
			panel.setOpaque(false);
		
		return panel;
    }

    /**
     * Creates a panel that holds gene set file specification
     */
	private JPanel createGMTPanel() {
		JButton selectGMTFileButton = new JButton("Browse...");
		gseaResultsDirTextField = new JFormattedTextField();
		gseaResultsDirTextField.setColumns(defaultColumns);

		// components needed for the directory load
		// GSEAResultsDirTextField.setText(gmt_instruction);
		gseaResultsDirTextField.addPropertyChangeListener("value", new BulkEMCreationPanel.FormattedTextFieldAction());

		selectGMTFileButton.addActionListener((ActionEvent evt) -> {
			selectGMTFileButtonActionPerformed(evt);
		});

		makeSmall(gseaResultsDirTextField, selectGMTFileButton);
		
		JPanel panel = new JPanel();
		panel.setBorder(LookAndFeelUtil.createTitledBorder("Gene Sets (GSEA Results directory)"));
		
		final GroupLayout layout = new GroupLayout(panel);
       	panel.setLayout(layout);
   		layout.setAutoCreateContainerGaps(true);
   		layout.setAutoCreateGaps(!LookAndFeelUtil.isAquaLAF());
   		
   		layout.setHorizontalGroup(layout.createSequentialGroup()
   				.addComponent(gseaResultsDirTextField, DEFAULT_SIZE, DEFAULT_SIZE, Short.MAX_VALUE)
   				.addComponent(selectGMTFileButton, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
   		);
   		layout.setVerticalGroup(layout.createParallelGroup(Alignment.CENTER, false)
   				.addComponent(gseaResultsDirTextField, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
   				.addComponent(selectGMTFileButton)
   		);
   		
   		if (LookAndFeelUtil.isAquaLAF())
			panel.setOpaque(false);

		// FIXME
//		if (!params.getMethod().equalsIgnoreCase(EnrichmentMapParameters.method_Specialized))
//			panel.add(newGMTPanel);

		return panel;
	}
	
	/**
	 * Creates a collapsable panel that holds parameter inputs
	 */
	private BasicCollapsiblePanel createParametersPanel() {
		BasicCollapsiblePanel panel = new BasicCollapsiblePanel("Parameters");
		
		if (LookAndFeelUtil.isAquaLAF())
			panel.setOpaque(false);

		// pvalue cutoff input
		String pvalueCutOffTip =
				"<html>Only genesets with a p-value less than<br />"+
				"the cutoff will be included.</html>";
		
		JLabel pvalueCutOffLabel = new JLabel("P-value Cutoff:");
		pvalueCutOffLabel.setToolTipText(pvalueCutOffTip);
		
		pvalueTextField = new JFormattedTextField(decFormat);
		pvalueTextField.setColumns(3);
		pvalueTextField.addPropertyChangeListener("value", new BulkEMCreationPanel.FormattedTextFieldAction());
		pvalueTextField.setToolTipText(pvalueCutOffTip);
		pvalueTextField.setText(Double.toString(params.getPvalue()));
		pvalueTextField.setValue(params.getPvalue());

		// qvalue cutoff input
		String qvalueCutOffTip =
				"<html>Only genesets with a FDR q-value less than<br />"+
                "the cutoff will be included.</html>";
		
		JLabel qvalueCutOffLabel = new JLabel("FDR Q-value Cutoff:");
		qvalueCutOffLabel.setToolTipText(qvalueCutOffTip);
		
		qvalueTextField = new JFormattedTextField(decFormat);
		qvalueTextField.setColumns(3);
		qvalueTextField.addPropertyChangeListener("value", new BulkEMCreationPanel.FormattedTextFieldAction());
		qvalueTextField.setToolTipText(qvalueCutOffTip);
		qvalueTextField.setText(Double.toString(params.getQvalue()));
		qvalueTextField.setValue(params.getQvalue());

		// coefficient cutoff input
		ButtonGroup jaccardOrOverlap;

		jaccard = new JRadioButton("Jaccard Coeffecient");
		jaccard.setActionCommand("jaccard");
		jaccard.setSelected(true);
		overlap = new JRadioButton("Overlap Coeffecient");
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
		
		jaccardOrOverlap = new ButtonGroup();
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
				"<html>Sets the Jaccard or Overlap coeffecient cutoff.<br />" +
				"only edges with a Jaccard or Overlap coffecient less than<br />" +
				"the cutoff will be added.</html>";
		
		JLabel coeffecientCutOffLabel = new JLabel("Cutoff:");
		coeffecientCutOffLabel.setToolTipText(coeffecientCutOffTip);
		
		coeffecientTextField = new JFormattedTextField(decFormat);
		coeffecientTextField.setColumns(3);
		coeffecientTextField.addPropertyChangeListener("value", new BulkEMCreationPanel.FormattedTextFieldAction());
		coeffecientTextField.setToolTipText(coeffecientCutOffTip);
		// coeffecientTextField.setText(Double.toString(params.getSimilarityCutOff()));
		coeffecientTextField.setValue(params.getSimilarityCutOff());
		similarityCutOffChanged = false; // reset for new Panel after .setValue(...) wrongly changed it to "true"

		makeSmall(pvalueCutOffLabel, pvalueTextField, qvalueCutOffLabel, qvalueTextField);
		makeSmall(jaccard, overlap, combined);
		makeSmall(coeffecientCutOffLabel, coeffecientTextField);
		
		// add the components to the panel
		final GroupLayout layout = new GroupLayout(panel.getContentPane());
		panel.getContentPane().setLayout(layout);
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(!LookAndFeelUtil.isAquaLAF());
		
		layout.setHorizontalGroup(layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup(Alignment.TRAILING, true)
						.addComponent(pvalueCutOffLabel)
						.addComponent(qvalueCutOffLabel)
						.addComponent(coeffecientCutOffLabel)
				)
				.addPreferredGap(ComponentPlacement.RELATED)
				.addGroup(layout.createParallelGroup(Alignment.LEADING, true)
						.addComponent(pvalueTextField, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
						.addComponent(qvalueTextField, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
						.addComponent(jaccard, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
						.addComponent(overlap, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
						.addComponent(combined, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
						.addComponent(coeffecientTextField, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
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
				.addPreferredGap(ComponentPlacement.UNRELATED)
				.addComponent(jaccard)
				.addComponent(overlap)
				.addComponent(combined)
				.addPreferredGap(ComponentPlacement.UNRELATED)
				.addGroup(layout.createParallelGroup(Alignment.CENTER, false)
						.addComponent(coeffecientCutOffLabel)
						.addComponent(coeffecientTextField)
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
                    message += "The Overlap/Jaccard Coeffecient cutoff must be between 0 and 1.";
                    invalid = true;
                }
            }else if(source == lowerLimitField){
                Number value = (Number) lowerLimitField.getValue();
                if ((value != null) && (value.intValue() >= 0)) {
                    params.setLowerlimit(value.intValue());
                } else {
                    source.setValue("1");
                    message += "The lower limit must be greater than 0.";
                    invalid = true;
                }
            }else if(source == upperLimitField){
                Number value = (Number) upperLimitField.getValue();
                if ((value != null) && (value.intValue() >= 0)) {
                    params.setUpperlimit(value.intValue());
                } else {
                    source.setValue("1");
                    message += "The upper limit must be greater than 0.";
                    invalid = true;
                }
            }else if (source == gseaResultsDirTextField) {
                String value = gseaResultsDirTextField.getText();
                if(value.equalsIgnoreCase("") )
                    params.setGSEAResultsDirName(value);
                else if(gseaResultsDirTextField.getText().equalsIgnoreCase((String)e.getOldValue())){
                   //do nothing
                }
                else if(checkFile(value).equals(Color.RED)){
                    JOptionPane.showMessageDialog(application.getJFrame(),message,"File name change entered is not a valid file name",JOptionPane.WARNING_MESSAGE);
                    gseaResultsDirTextField.setForeground(checkFile(value));
                }
               else
            	   params.getFiles().get(EnrichmentMap.DATASET1).setGMTFileName(value);
            }else if (source == gmtDirectoryTextField) {
                String value = gmtDirectoryTextField.getText();
                if(value.equalsIgnoreCase("") )
                    params.setGMTDirName(value);
                else if(gmtDirectoryTextField.getText().equalsIgnoreCase((String)e.getOldValue())){
                   //do nothing
                }
                else if(checkFile(value).equals(Color.RED)){
                    JOptionPane.showMessageDialog(application.getJFrame(),message,"Directory name change entered is not a valid file name",JOptionPane.WARNING_MESSAGE);
                    gmtDirectoryTextField.setForeground(checkFile(value));
                }
               else
            	   params.getFiles().get(EnrichmentMap.DATASET1).setGMTFileName(value);
            }else if (source == gctDirectoryTextField) {
                String value = gctDirectoryTextField.getText();
                if(value.equalsIgnoreCase("") )
                    params.setGCTDirName(value);
                else if(gctDirectoryTextField.getText().equalsIgnoreCase((String)e.getOldValue())){
                   //do nothing
                }
                else if(checkFile(value).equals(Color.RED)){
                    JOptionPane.showMessageDialog(application.getJFrame(),message,"Directory name change entered is not a valid file name",JOptionPane.WARNING_MESSAGE);
                    gctDirectoryTextField.setForeground(checkFile(value));
                }
               else
            	   params.getFiles().get(EnrichmentMap.DATASET1).setGMTFileName(value);
            }

            if (invalid) {
                JOptionPane.showMessageDialog(application.getJFrame(), message, "Parameter out of bounds", JOptionPane.WARNING_MESSAGE);
            }
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

//Action listeners for buttons in input panel

    private void selectOneMultiSessionActionPerformed(java.awt.event.ActionEvent evt) {
        if(evt.getActionCommand().equalsIgnoreCase("onesession")){
            params.setSessions(false);

        }
     else if(evt.getActionCommand().equalsIgnoreCase("multisession")){
            params.setSessions(true);
        }
    }


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
                similarityCutOffChanged = false; //reset after .setValue(...) wrongly changed it to "true"
            }
        }
     else if(evt.getActionCommand().equalsIgnoreCase("overlap")){
            params.setSimilarityMetric(EnrichmentMapParameters.SM_OVERLAP);
            if ( ! similarityCutOffChanged  ) {
                params.setSimilarityCutOff(params.getDefaultOverlapCutOff());
//                coeffecientTextField.setText( Double.toString(params.getSimilarityCutOff()) );
                coeffecientTextField.setValue( params.getSimilarityCutOff() );
                similarityCutOffChanged = false; //reset after .setValue(...) wrongly changed it to "true"
          }
        }
        else if(evt.getActionCommand().equalsIgnoreCase("combined")){
            params.setSimilarityMetric(EnrichmentMapParameters.SM_COMBINED);
            if ( ! similarityCutOffChanged ) {
                params.setSimilarityCutOff((params.getDefaultOverlapCutOff() * params.getCombinedConstant()) + ((1-params.getCombinedConstant()) * params.getDefaultJaccardCutOff()) );
//                coeffecientTextField.setText( Double.toString(params.getSimilarityCutOff()) );
                coeffecientTextField.setValue( params.getSimilarityCutOff() );
                similarityCutOffChanged = false; //reset after .setValue(...) wrongly changed it to "true"
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
           File file = fileUtil.getFile(SwingUtil.getWindowInstance(this),"Import gmt file", FileUtil.LOAD,all_filters);
           if(file != null) {
               gseaResultsDirTextField.setText(file.getParent());
               params.setGSEAResultsDirName(file.getParent());
           }
       }

     /**
     * gene set (gmt) file selector action listener
     *
     * @param evt
     */
     private void selectGMTDirButtonActionPerformed(
               java.awt.event.ActionEvent evt) {
    	 // Create FileFilter
         FileChooserFilter filter = new FileChooserFilter("All GMT Files","gmt" );          
         FileChooserFilter filter_gct = new FileChooserFilter("All GMT Files","gct" );
         FileChooserFilter filter_txt = new FileChooserFilter("All GMT Files","txt" );
         FileChooserFilter filter_xls = new FileChooserFilter("All GMT Files","xls" );
         
         //the set of filter (required by the file util method
         ArrayList<FileChooserFilter> all_filters = new ArrayList<FileChooserFilter>();
         all_filters.add(filter);
         all_filters.add(filter_gct);
         all_filters.add(filter_txt);
         all_filters.add(filter_xls);
         
           // Get the file name
           File file = fileUtil.getFile(SwingUtil.getWindowInstance(this), "GMT directory", FileUtil.LOAD,all_filters);
           if(file != null) {
               gmtDirectoryTextField.setText(file.getParent());
               params.setGMTDirName(file.getParent());
           }
       }


     /**
     * gene set (gmt) file selector action listener
     *
     * @param evt
     */
     private void selectGCTDirButtonActionPerformed(
               java.awt.event.ActionEvent evt) {
    	// Create FileFilter
         FileChooserFilter filter = new FileChooserFilter("All GMT Files","gmt" );          
         FileChooserFilter filter_gct = new FileChooserFilter("All GMT Files","gct" );
         FileChooserFilter filter_txt = new FileChooserFilter("All GMT Files","txt" );
         FileChooserFilter filter_xls = new FileChooserFilter("All GMT Files","xls" );
         
         //the set of filter (required by the file util method
         ArrayList<FileChooserFilter> all_filters = new ArrayList<FileChooserFilter>();
         all_filters.add(filter);
         all_filters.add(filter_gct);
         all_filters.add(filter_txt);
         all_filters.add(filter_xls);
         
           // Get the file name
           File file = fileUtil.getFile(SwingUtil.getWindowInstance(this),"GCT directory", FileUtil.LOAD,all_filters);
           if(file != null) {
               gctDirectoryTextField.setText(file.getParent());
               params.setGCTDirName(file.getParent());
           }
       }

	/**
	 * Utility method that creates a panel for buttons at the bottom of the Enrichment Map Panel
	 */
	private JPanel createBottomPanel() {
		JButton resetButton = new JButton("Reset");
		resetButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				resetPanel();
			}
		});

		JButton closeButton = new JButton("Close");
		closeButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				cancelButtonActionPerformed(evt);
			}
		});

		JButton importButton = new JButton("Build");
		// TODO:Add actionlistener
		// importButton.addActionListener(new
		// BuildBulkEnrichmentMapActionListener(this));

		JPanel panel = LookAndFeelUtil.createOkCancelPanel(importButton, closeButton, resetButton);
		
		if (LookAndFeelUtil.isAquaLAF())
			panel.setOpaque(false);

		return panel;
	}
        
        //TODO: implement un-register service for this window
        private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {
        		this.registrar.unregisterService(this, CytoPanelComponent.class);
        }

        public void close() {
        		this.registrar.unregisterService(this, CytoPanelComponent.class);
        }
    /**
         *  Clear the current panel and clear the params associated with this panel
         */
        private void resetPanel(){

            this.params = new EnrichmentMapParameters(cySessionManager, streamUtil, applicationManager);

            gseaResultsDirTextField.setText("");
            gseaResultsDirTextField.setToolTipText(null);

            pvalueTextField.setText(Double.toString(params.getPvalue()));
            qvalueTextField.setText(Double.toString(params.getQvalue()));
            coeffecientTextField.setText(Double.toString(params.getSimilarityCutOff()));

            pvalueTextField.setValue(params.getPvalue());
            qvalueTextField.setValue(params.getQvalue());
            coeffecientTextField.setValue(params.getSimilarityCutOff());
            //reset for cleared Panel after .setValue(...) wrongly changed it to "true"
            similarityCutOffChanged = false;

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

	private JPanel createAnalysisTypePanel() {
		if (params.getMethod().equalsIgnoreCase(EnrichmentMapParameters.method_GSEA)) {
			gsea = new JRadioButton(EnrichmentMapParameters.method_GSEA, true);
			generic = new JRadioButton(EnrichmentMapParameters.method_generic, false);
		} else if (params.getMethod().equalsIgnoreCase(EnrichmentMapParameters.method_generic)) {
			gsea = new JRadioButton(EnrichmentMapParameters.method_GSEA, false);
			generic = new JRadioButton(EnrichmentMapParameters.method_generic, true);
		} else if (params.getMethod().equalsIgnoreCase(EnrichmentMapParameters.method_Specialized)) {
			gsea = new JRadioButton(EnrichmentMapParameters.method_GSEA, false);
			generic = new JRadioButton(EnrichmentMapParameters.method_generic, false);
		}

		gsea.setActionCommand(EnrichmentMapParameters.method_GSEA);
		generic.setActionCommand(EnrichmentMapParameters.method_generic);

		gsea.addActionListener((ActionEvent evt) -> {
			selectAnalysisTypeActionPerformed(evt);
		});
		generic.addActionListener((ActionEvent evt) -> {
			selectAnalysisTypeActionPerformed(evt);
		});

		ButtonGroup analysisOptions = new ButtonGroup();
		analysisOptions.add(gsea);
		analysisOptions.add(generic);

		makeSmall(gsea, generic);
		
		JPanel panel = new JPanel();

		if (LookAndFeelUtil.isAquaLAF())
			panel.setOpaque(false);
          
		panel.setBorder(LookAndFeelUtil.createTitledBorder("Analysis Type"));
		final GroupLayout layout = new GroupLayout(panel);
		panel.setLayout(layout);
		layout.setAutoCreateContainerGaps(true);
		layout.setAutoCreateGaps(true);
	   		
  		layout.setHorizontalGroup(layout.createSequentialGroup()
  				.addGap(0, 0, Short.MAX_VALUE)
  				.addComponent(gsea)
  				.addComponent(generic)
  				.addGap(0, 0, Short.MAX_VALUE)
  		);
  		layout.setVerticalGroup(layout.createParallelGroup(Alignment.CENTER, true)
  				.addComponent(gsea, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
  				.addComponent(generic, PREFERRED_SIZE, DEFAULT_SIZE, PREFERRED_SIZE)
  		);
  		
        return panel;
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
      /*else if(analysisType.equalsIgnoreCase(EnrichmentMapParameters.method_DAVID))
          params.setMethod(EnrichmentMapParameters.method_DAVID);
      */
  }

	public Component getComponent() {
		
		return this;
	}

	public CytoPanelName getCytoPanelName() {
		
		return CytoPanelName.WEST;
	}

	public Icon getIcon() {
		
		return null;
	}

	public String getTitle() {
		
		return "Bulk Enrichment Map Input Panel";
	}
   
}
