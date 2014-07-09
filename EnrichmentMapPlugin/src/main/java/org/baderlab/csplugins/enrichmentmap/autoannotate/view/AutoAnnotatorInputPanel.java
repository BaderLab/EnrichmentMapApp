package org.baderlab.csplugins.enrichmentmap.autoannotate.view;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
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
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.plaf.basic.BasicComboBoxRenderer;
import javax.swing.text.Position;

import org.baderlab.csplugins.enrichmentmap.autoannotate.task.AutoAnnotatorTaskFactory;
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
import org.cytoscape.view.presentation.annotations.Annotation;
import org.cytoscape.view.presentation.annotations.AnnotationManager;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.work.SynchronousTaskManager;
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
	protected CyNetworkView selectedView;
	protected AutoAnnotatorTaskFactory autoAnnotatorTaskFactory;
	private AnnotationDisplayPanel displayPanel;
	private JComboBox networkDropdown;

	public AutoAnnotatorInputPanel(CyApplicationManager cyApplicationManagerRef, 
			CyNetworkViewManager cyNetworkViewManagerRef, CySwingApplication cySwingApplicationRef,
			OpenBrowser openBrowserRef, CyNetworkManager cyNetworkManagerRef, AnnotationManager annotationManager,
			AnnotationDisplayPanel displayPanel, CyServiceRegistrar registrar, DialogTaskManager dialogTaskManager, CyEventHelper eventHelper){
		
		this.displayPanel = displayPanel;
		
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
        final JComboBox nameColumnDropdown = new JComboBox();

        nameColumnDropdown.addItemListener(new ItemListener(){
        	public void itemStateChanged(ItemEvent itemEvent) {
                nameColumnName = (String) itemEvent.getItem();
            }
        });
        
        // Give the user a choice of column with cluster numbers
        final JComboBox clusterColumnDropdown = new JComboBox();
        
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
        ActionListener autoAnnotateAction = new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				// networkID and clusterColumnName field are looked up only when the button is pressed
				autoAnnotatorTaskFactory = new AutoAnnotatorTaskFactory(cySwingApplicationRef, cyApplicationManagerRef, 
						cyNetworkViewManagerRef, cyNetworkManagerRef, annotationManager, displayPanel,
        				selectedView, clusterColumnName, nameColumnName, registrar, dialogTaskManager);
				dialogTaskManager.execute(autoAnnotatorTaskFactory.createTaskIterator());
			}
        };
        confirmButton.addActionListener(autoAnnotateAction);
        
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
	
    class NetworkViewRenderer extends BasicComboBoxRenderer {

		private static final long serialVersionUID = -5877635875395629866L;  

		@Override
		public Component getListCellRendererComponent(JList list, Object value, int index, 
				boolean isSelected, boolean cellHasFocus) {
            
			super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);  
  
            if (value != null) {
            	String label = ((CyNetworkView) value).getModel().toString();
            	int viewNumber = 1;
            	while (list.getNextMatch(label + " View " + String.valueOf(viewNumber), 0, Position.Bias.Forward) != -1) {
            		viewNumber++;
            	}
            	if (viewNumber > 1) {
            		label += " View " + String.valueOf(viewNumber);
            	}
                setText(label);
            } 
  
            return this;  
        }  
    }
}