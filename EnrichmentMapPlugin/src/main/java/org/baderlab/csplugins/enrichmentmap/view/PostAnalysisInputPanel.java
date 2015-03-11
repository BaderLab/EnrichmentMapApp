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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;

import org.baderlab.csplugins.enrichmentmap.EnrichmentMapUtils;
import org.baderlab.csplugins.enrichmentmap.PostAnalysisParameters;
import org.baderlab.csplugins.enrichmentmap.actions.BuildPostAnalysisActionListener;
import org.baderlab.csplugins.enrichmentmap.actions.ShowAboutPanelAction;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.io.util.StreamUtil;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.session.CySessionManager;
import org.cytoscape.util.swing.FileUtil;
import org.cytoscape.util.swing.OpenBrowser;
import org.cytoscape.work.swing.DialogTaskManager;

/**
 * Created by
 * @author revilo
 * <p>
 * Date July 9, 2009
 * 
 * Based on: EnrichmentMapInputPanel.java (302) by risserlin
 */

public class PostAnalysisInputPanel extends JPanel implements CytoPanelComponent {
    
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
    
    
    private JRadioButton knownSignature;
    private JRadioButton signatureDiscovery;
    
    // Top level panel for signature discovery or known signature
    private CollapsiblePanel userInputPanel;
    
    private JPanel optionsPanel; // value is set to either signatureDiscoveryPanel or knownSignaturePanel
    private PostAnalysisSignatureDiscoveryPanel signatureDiscoveryPanel;
    private PostAnalysisKnownSignaturePanel knownSignaturePanel;
    
    
    
    
    protected static String gmtTip = "File specifying gene sets.\n" + "Format: geneset name <tab> description <tab> gene ...";
    protected static String gmt_instruction = "Please select the Gene Set file (.gmt)...";
    protected static String siggmt_instruction = "Please select the Signature Gene Set file (.gmt)...";
    //tool tips

    
//    private EnrichmentMap map;
    private PostAnalysisParameters paParams;
    
    
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
    		
        
        // Create the two main panels, set the default one
        signatureDiscoveryPanel = new PostAnalysisSignatureDiscoveryPanel(this, cyApplicationManager, application, streamUtil, dialog, fileUtil);
        knownSignaturePanel = new PostAnalysisKnownSignaturePanel(this, cyApplicationManager, application, streamUtil, dialog, fileUtil);
        optionsPanel = knownSignaturePanel; // default
       
        userInputPanel = new CollapsiblePanel("User Input");
        userInputPanel.setCollapsed(false);
        userInputPanel.add(optionsPanel);

        setLayout(new BorderLayout());
        
        JPanel analysisTypePanel = createAnalysisTypePanel();
        add(analysisTypePanel, BorderLayout.NORTH);
        
        JPanel advancedOptionsContainer = new JPanel(new BorderLayout());
        JScrollPane scrollPane = new JScrollPane(userInputPanel);
        advancedOptionsContainer.add(scrollPane, BorderLayout.CENTER);
        add(advancedOptionsContainer,BorderLayout.CENTER);
        
        JPanel bottomPanel = createBottomPanel();
        add(bottomPanel, BorderLayout.SOUTH);
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
        
        // Known Signature
        knownSignature = new JRadioButton("Known Signature", optionsPanel == knownSignaturePanel);
        knownSignature.setActionCommand("Known Signature");
        knownSignature.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectAnalysisTypeActionPerformed(evt);
            }
        });        
        knownSignature.setSelected(true);

        // Signature Discovery
        signatureDiscovery = new JRadioButton("Signature Discovery", optionsPanel == signatureDiscoveryPanel);
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
    
    
    private void cancelButtonActionPerformed(ActionEvent evt) {
    	close();
    }

    public void close() {
    	registrar.unregisterService(this, CytoPanelComponent.class);
    }


    static Color checkFile(String filename){
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
            paParams.setSignatureHub(true);
        	userInputPanel.remove(optionsPanel);
        	optionsPanel = signatureDiscoveryPanel;
        	userInputPanel.add(optionsPanel);
        	optionsPanel.revalidate();
        } else {
            paParams.setSignatureHub(false);
        	paParams.setFilter(false);
        	userInputPanel.remove(optionsPanel);
        	optionsPanel = knownSignaturePanel;
        	userInputPanel.add(optionsPanel);
        	optionsPanel.revalidate();
        }
    }
    
    
    /**
     * Clear the current panel and clear the paParams associated with each panel
     */
    private void resetPanel() {
    	knownSignaturePanel.resetPanel();
    	signatureDiscoveryPanel.resetPanel();
    }

    /**
     * Refresh content of PostAnalysisInputPanel when Network is changed or Panel is re-opened.
     * 
     * @param current_params
     */
    public void updateContents(EnrichmentMap currentMap) {
		if(currentMap != null) {
			this.paParams = new PostAnalysisParameters(sessionManager, streamUtil, cyApplicationManager);
			this.paParams.copyFrom(currentMap.getPaParams());
			
			signatureDiscoveryPanel.updateContents(currentMap, paParams);
			knownSignaturePanel.updateContents(currentMap, paParams);
		}
    }
    

	public PostAnalysisParameters getPaParams() {
        return paParams;
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
	 * Set available signature gene set count to specified value
	 * @param int avSigCount
	 * @return null
	 */
	public void setAvSigCount(int avSigCount) {
		if (signatureDiscovery.isSelected()) {
			signatureDiscoveryPanel.setAvSigCount(avSigCount);
		}
	}
	
}
