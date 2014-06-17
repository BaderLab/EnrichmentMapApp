package org.baderlab.csplugins.enrichmentmap.view;

import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;

import org.baderlab.csplugins.enrichmentmap.actions.AutoAnnotatorAction;
import org.baderlab.csplugins.enrichmentmap.actions.ShowAboutPanelAction;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.OpenBrowser;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.presentation.annotations.AnnotationManager;

/**
 * @author arkadyark
 * <p>
 * Date   June 16, 2014<br>
 * Time   11:26:32 AM<br>
 */
public class AutoAnnotatePanel extends JPanel implements CytoPanelComponent {

	private static final long serialVersionUID = 7901088595186775935L;

	public AutoAnnotatePanel(CyApplicationManager cyApplicationManagerRef, 
			CyNetworkViewManager cyNetworkViewManagerRef, CySwingApplication cySwingApplicationRef,
			OpenBrowser openBrowserRef, CyNetworkManager cyNetworkManagerRef, AnnotationManager annotationManager,
			CyServiceRegistrar registrar){
		JPanel mainPanel = createMainPanel(cyApplicationManagerRef, cyNetworkViewManagerRef, cySwingApplicationRef,
				openBrowserRef, cyNetworkManagerRef, annotationManager, registrar);
	}
	
	private JPanel createMainPanel(CyApplicationManager cyApplicationManagerRef,
			CyNetworkViewManager cyNetworkViewManagerRef, CySwingApplication cySwingApplicationRef,
			OpenBrowser openBrowserRef, CyNetworkManager cyNetworkManagerRef, AnnotationManager annotationManager,
			CyServiceRegistrar registrar) {
		
		JPanel mainPanel = new JPanel();
		
		JComboBox networkDropdown = new JComboBox();
		
        Map<String, String> serviceProperties = new HashMap<String, String>();
        serviceProperties.put("inMenuBar", "true");
		   serviceProperties.put("preferredMenu", "Apps.EnrichmentMap");
        AutoAnnotatorAction autoAnnotateAction = new AutoAnnotatorAction(serviceProperties,cyApplicationManagerRef, 
        		cyNetworkViewManagerRef, cySwingApplicationRef, openBrowserRef, cyNetworkManagerRef, annotationManager, registrar);	
        for (CyNetwork network : cyNetworkManagerRef.getNetworkSet()) {
        	networkDropdown.addItem(network);
        }
        
        final JComboBox clusterColumnDropdown = new JComboBox();

        networkDropdown.addItemListener(new ItemListener(){
        	public void itemStateChanged(ItemEvent itemEvent) {
                CyNetwork network = (CyNetwork) itemEvent.getItem();
                clusterColumnDropdown.removeAllItems();
                for (CyColumn column : network.getDefaultNodeTable().getColumns()) {
                	clusterColumnDropdown.addItem(column.getName());
                }
              }
        	
        });
        
        
        JButton confirmButton = new JButton("Choose network");
        confirmButton.addActionListener(autoAnnotateAction);
        
        
        return mainPanel;
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
		return "Annotation Panel";
	}
	
}