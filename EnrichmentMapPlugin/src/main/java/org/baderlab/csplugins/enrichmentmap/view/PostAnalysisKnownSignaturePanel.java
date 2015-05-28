package org.baderlab.csplugins.enrichmentmap.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToolTip;

import org.baderlab.csplugins.enrichmentmap.PostAnalysisParameters;
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
   
    private PostAnalysisWeightPanel weightPanel;
    
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
        setLayout(new BorderLayout());

        //Gene set file panel
        CollapsiblePanel gmtPanel = createKnownSignatureGMTPanel();
        gmtPanel.setCollapsed(false);
        
        //Parameters collapsible panel
        weightPanel = new PostAnalysisWeightPanel(application);
        weightPanel.setCollapsed(false);
        
        add(gmtPanel, BorderLayout.NORTH);
        add(weightPanel, BorderLayout.CENTER);        
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
        selectSigGMTFileButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
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
     */
    private void selectSignatureGMTFileButtonActionPerformed(ActionEvent evt) {
    	File file = parentPanel.chooseGMTFile(knownSignatureGMTFileNameTextField);
    	if(file != null) {
            //Load in the GMT file
            //Manually fire the same action listener that is used by the signature discovery panel
            LoadSignatureSetsActionListener loadAction = new LoadSignatureSetsActionListener(parentPanel, application, cyApplicationManager, dialog, streamUtil);
            loadAction.setSelectAll(true);
            loadAction.actionPerformed(evt);
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
                    paParams.getSignatureSetNames().clear();
                    paParams.getSelectedSignatureSetNames().clear();
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
        
        weightPanel.resetPanel();
    }
    
    
    void updateContents(EnrichmentMap currentMap, PostAnalysisParameters paParams) {
    	this.map = currentMap;
		this.paParams = paParams;
		
		weightPanel.updateContents(currentMap, paParams);
    }
}
