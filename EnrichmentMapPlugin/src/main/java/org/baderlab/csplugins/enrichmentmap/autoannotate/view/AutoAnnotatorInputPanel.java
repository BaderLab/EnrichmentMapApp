package org.baderlab.csplugins.enrichmentmap.autoannotate.view;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.baderlab.csplugins.enrichmentmap.autoannotate.task.AutoAnnotatorTaskFactory;
import org.baderlab.csplugins.enrichmentmap.autoannotate.model.NetworkViewRenderer;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.OpenBrowser;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.presentation.annotations.AnnotationManager;
import org.cytoscape.work.swing.DialogTaskManager;

/**
 * @author arkadyark
 * <p>
 * Date   June 16, 2014<br>
 * Time   11:26:32 AM<br>
 */

public class AutoAnnotatorInputPanel extends JPanel implements CytoPanelComponent {

	private static final long serialVersionUID = 7901088595186775935L;
	private String clusterColumnName;
	private String nameColumnName;
	public CyNetworkView selectedView;
	protected AutoAnnotatorTaskFactory autoAnnotatorTaskFactory;
	private AnnotationDisplayPanel displayPanel;
	private JComboBox networkDropdown;
	public JComboBox clusterColumnDropdown;
	public JComboBox nameColumnDropdown;
	private int annotationSetNumber;

	public AutoAnnotatorInputPanel(CyApplicationManager cyApplicationManagerRef, 
			CyNetworkViewManager cyNetworkViewManagerRef, CySwingApplication cySwingApplicationRef,
			OpenBrowser openBrowserRef, CyNetworkManager cyNetworkManagerRef, AnnotationManager annotationManager,
			AnnotationDisplayPanel displayPanel, CyServiceRegistrar registrar, DialogTaskManager dialogTaskManager, CyEventHelper eventHelper){
		
		this.displayPanel = displayPanel;
		annotationSetNumber = 1;
		JPanel mainPanel = createMainPanel(cyApplicationManagerRef, cyNetworkViewManagerRef, cySwingApplicationRef,
				openBrowserRef, cyNetworkManagerRef, annotationManager, registrar, dialogTaskManager, eventHelper);
		add(mainPanel,BorderLayout.CENTER);
	}
	
	private JPanel createMainPanel(final CyApplicationManager cyApplicationManagerRef,
			final CyNetworkViewManager cyNetworkViewManagerRef, final CySwingApplication cySwingApplicationRef,
			final OpenBrowser openBrowserRef, final CyNetworkManager cyNetworkManagerRef, final AnnotationManager annotationManager,
			final CyServiceRegistrar registrar, final DialogTaskManager dialogTaskManager, final CyEventHelper eventHelper) {
		
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));
		
		
        // Give the user a choice of networks to annotate
        networkDropdown = new JComboBox();
        networkDropdown.setRenderer( new NetworkViewRenderer() );  
        
        // Give the user a choice of column with gene names
        nameColumnDropdown = new JComboBox();

        nameColumnDropdown.addItemListener(new ItemListener(){
        	public void itemStateChanged(ItemEvent itemEvent) {
                nameColumnName = (String) itemEvent.getItem();
            }
        });
        
        // Give the user a choice of column with cluster numbers
        clusterColumnDropdown = new JComboBox();
        
        clusterColumnDropdown.addItemListener(new ItemListener(){
        	public void itemStateChanged(ItemEvent itemEvent) {
                clusterColumnName = (String) itemEvent.getItem();
            }
        });
        
        networkDropdown.addItemListener(new ItemListener(){
        	public void itemStateChanged(ItemEvent itemEvent) {
        		if (itemEvent.getStateChange() == ItemEvent.SELECTED) {
	        		selectedView = (CyNetworkView) itemEvent.getItem();
	        		CyNetwork network = selectedView.getModel();
	        		// Update column name dropdowns
	        		clusterColumnDropdown.removeAllItems();
	        		nameColumnDropdown.removeAllItems();
	        		for (CyColumn column : network.getDefaultNodeTable().getColumns()) {
	        			clusterColumnDropdown.addItem(column.getName());
	        			nameColumnDropdown.addItem(column.getName());
	        		}

        		} else if(itemEvent.getStateChange() == ItemEvent.DESELECTED) {
	        		clusterColumnDropdown.removeAllItems();
	        		nameColumnDropdown.removeAllItems();
        		}
        	}
        });
        
        JButton confirmButton = new JButton("Annotate!");
        
        final Map<String, String> serviceProperties = new HashMap<String, String>();
        serviceProperties.put("inMenuBar", "true");
		serviceProperties.put("preferredMenu", "Apps.EnrichmentMap");
        ActionListener annotateAction = new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				// networkID and clusterColumnName field are looked up only when the button is pressed
				autoAnnotatorTaskFactory = new AutoAnnotatorTaskFactory(cySwingApplicationRef, cyApplicationManagerRef, 
						cyNetworkViewManagerRef, cyNetworkManagerRef, annotationManager, displayPanel,
        				selectedView, clusterColumnName, nameColumnName, annotationSetNumber, registrar, dialogTaskManager);
				dialogTaskManager.execute(autoAnnotatorTaskFactory.createTaskIterator());
				annotationSetNumber++;
			}
        };
        confirmButton.addActionListener(annotateAction);
        
        JLabel networkDropdownLabel = new JLabel("Select the network to annotate:");
        JLabel clusterColumnDropdownLabel = new JLabel("Select the column with the clusters:"); // ambiguous phrasing?
        JLabel nameColumnDropdownLabel = new JLabel("Select the column with the gene set names:"); // ambiguous phrasing?       
        
        networkDropdownLabel.setAlignmentX(LEFT_ALIGNMENT);
        networkDropdown.setAlignmentX(LEFT_ALIGNMENT);
        nameColumnDropdownLabel.setAlignmentX(LEFT_ALIGNMENT);
        nameColumnDropdown.setAlignmentX(LEFT_ALIGNMENT);
        clusterColumnDropdownLabel.setAlignmentX(LEFT_ALIGNMENT);
        clusterColumnDropdown.setAlignmentX(LEFT_ALIGNMENT);
        confirmButton.setAlignmentX(LEFT_ALIGNMENT);
        
        mainPanel.add(networkDropdownLabel);
        mainPanel.add(networkDropdown);
        mainPanel.add(nameColumnDropdownLabel);
        mainPanel.add(nameColumnDropdown);
        mainPanel.add(clusterColumnDropdownLabel);
        mainPanel.add(clusterColumnDropdown);
        mainPanel.add(confirmButton);
        
        
        return mainPanel;
	}

	public void addNetworkView(CyNetworkView view) {
		networkDropdown.addItem(view);
	}
	
	public void removeNetworkView(CyNetworkView view) {
		networkDropdown.removeItem(view);
	}
	
	@Override
	public Component getComponent() {
		return this;
	}

	@Override
	public CytoPanelName getCytoPanelName() {
		return CytoPanelName.WEST;
	}

	@Override
	public Icon getIcon() {
		return null;
	}

	@Override
	public String getTitle() {
		return "Annotation Input Panel";
	}
}