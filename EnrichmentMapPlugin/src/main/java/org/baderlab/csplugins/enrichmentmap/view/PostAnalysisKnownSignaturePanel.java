package org.baderlab.csplugins.enrichmentmap.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToolTip;

import org.baderlab.csplugins.enrichmentmap.EnrichmentMapUtils;
import org.baderlab.csplugins.enrichmentmap.PostAnalysisParameters;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.model.JMultiLineToolTip;
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
   
    private PostAnalysisWeightPanel parametersPanel;
    
	private JFormattedTextField knownSignatureGMTFileNameTextField;
	
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
     * @return collapsiblePanel to select Signature Genesets for Signature Analysis
     */
    private void createKnownSignatureOptionsPanel() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        //Gene set file panel
        CollapsiblePanel GMTPanel = createKnownSignatureGMTPanel();
        GMTPanel.setCollapsed(false);
        
        //Parameters collapsible panel
        parametersPanel = new PostAnalysisWeightPanel(application);
        parametersPanel.setCollapsed(false);
        
        add(GMTPanel);
        add(parametersPanel);        
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
        knownSignatureGMTFileNameTextField.setColumns(15);


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
                    paParams.setSignatureSetNames(new DefaultListModel<>());
                    paParams.setSelectedSignatureSetNames(new DefaultListModel<>());
                }
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
        
        parametersPanel.resetPanel();
    }
    
    
    void updateContents(EnrichmentMap currentMap, PostAnalysisParameters paParams) {
    	this.map = currentMap;
		this.paParams = paParams;
		
		parametersPanel.updateContents(currentMap, paParams);
    }
}
