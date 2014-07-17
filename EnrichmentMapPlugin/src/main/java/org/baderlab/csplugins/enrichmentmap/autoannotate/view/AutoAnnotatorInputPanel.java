package org.baderlab.csplugins.enrichmentmap.autoannotate.view;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.List;
import java.util.TreeMap;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import org.baderlab.csplugins.enrichmentmap.autoannotate.task.AutoAnnotatorTaskFactory;
import org.baderlab.csplugins.enrichmentmap.autoannotate.task.ClusterMakerTaskFactory;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.BasicCollapsiblePanel;
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
	private static final String defaultButtonString = "Use clusterMaker defaults";
	private static final String specifyColumnButtonString = "Select cluster column";
	private String nameColumnName;
	private AutoAnnotatorTaskFactory autoAnnotatorTaskFactory;
	private AutoAnnotatorDisplayPanel displayPanel;
	public JComboBox clusterColumnDropdown;
	public JComboBox nameColumnDropdown;
	private int annotationSetNumber;
	private JComboBox clusterAlgorithmDropdown;
	private ButtonGroup radioButtonGroup;
	private TreeMap<String, String> algorithmToColumnName;
	protected String clustering;
	private JRadioButton defaultButton;
	private JRadioButton specifyColumnButton;
	protected CyNetworkView selectedView;
	private CyTableManager tableManager;
	private JLabel networkLabel;

	public AutoAnnotatorInputPanel(CyApplicationManager cyApplicationManagerRef, 
			CyNetworkViewManager cyNetworkViewManagerRef, CySwingApplication cySwingApplicationRef,
			OpenBrowser openBrowserRef, CyNetworkManager cyNetworkManagerRef, AnnotationManager annotationManager,
			AutoAnnotatorDisplayPanel displayPanel, CyServiceRegistrar registrar, DialogTaskManager dialogTaskManager, 
			CyEventHelper eventHelper, CyTableManager tableManager){
		
		this.displayPanel = displayPanel;
		this.tableManager = tableManager;
		annotationSetNumber = 1;
		
		algorithmToColumnName = new TreeMap<String, String>();		
		algorithmToColumnName.put("Affinity Propagation Cluster", "__APCluster");
		algorithmToColumnName.put("Cluster Fuzzifier", "__fuzzifierCluster");
		algorithmToColumnName.put("Community cluster (GLay)", "__glayCluster");
		algorithmToColumnName.put("ConnectedComponents Cluster", "__ccCluster");
		algorithmToColumnName.put("Fuzzy C-Means Cluster", "__fcmlCluster");
		algorithmToColumnName.put("MCL Cluster", "__mclCluster");
		algorithmToColumnName.put("SCPS Cluster", "__scpsCluster");
		
		JPanel mainPanel = createMainPanel(cyApplicationManagerRef, cyNetworkViewManagerRef, cySwingApplicationRef,
				openBrowserRef, cyNetworkManagerRef, annotationManager, registrar, dialogTaskManager, eventHelper);
		add(mainPanel,BorderLayout.CENTER);
		setPreferredSize(new Dimension(500, getHeight()));
	}
	
	private JPanel createMainPanel(final CyApplicationManager cyApplicationManagerRef,
			final CyNetworkViewManager cyNetworkViewManagerRef, final CySwingApplication cySwingApplicationRef,
			final OpenBrowser openBrowserRef, final CyNetworkManager cyNetworkManagerRef, final AnnotationManager annotationManager,
			final CyServiceRegistrar registrar, final DialogTaskManager dialogTaskManager, final CyEventHelper eventHelper) {
		
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        
		networkLabel = new JLabel("No network selected");
		Font font = networkLabel.getFont();
		networkLabel.setFont(new Font(font.getFamily(), font.getStyle(), 18));
		mainPanel.add(networkLabel);
		
        // Give the user a choice of column with gene names
        nameColumnDropdown = new JComboBox();

        nameColumnDropdown.addItemListener(new ItemListener(){
        	public void itemStateChanged(ItemEvent itemEvent) {
                nameColumnName = (String) itemEvent.getItem();
            }
        });
        
        JPanel clusterPanel = createClusterPanel(); 
        
        JButton confirmButton = new JButton("Annotate!");

        ActionListener annotateAction = new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				String clusterColumnName = "";
				if (defaultButton.isSelected()) {
					// If using default clustermaker parameters
					String algorithm = (String) clusterAlgorithmDropdown.getSelectedItem();
					ClusterMakerTaskFactory clusterMakerTaskFactory = new ClusterMakerTaskFactory(selectedView, algorithm, dialogTaskManager, registrar);
					dialogTaskManager.execute(clusterMakerTaskFactory.createTaskIterator());
					clusterColumnName = algorithmToColumnName.get(algorithm);
					CyColumn column = selectedView.getModel().getDefaultNodeTable().getColumn(clusterColumnName);
					while (column == null) { // Give clusterMaker time to finish
						column = selectedView.getModel().getDefaultNodeTable().getColumn(clusterColumnName);
						continue;
					}
				} else if (specifyColumnButton.isSelected()) {
					// If using a user specified column
					clusterColumnName = (String) clusterColumnDropdown.getSelectedItem();
				}
				autoAnnotatorTaskFactory = new AutoAnnotatorTaskFactory(cySwingApplicationRef, cyApplicationManagerRef, 
						cyNetworkViewManagerRef, cyNetworkManagerRef, annotationManager, displayPanel, selectedView, 
						clusterColumnName, nameColumnName, annotationSetNumber, registrar, dialogTaskManager, tableManager);
				dialogTaskManager.execute(autoAnnotatorTaskFactory.createTaskIterator());
				annotationSetNumber++;	
			}
        };
        confirmButton.addActionListener(annotateAction);
        
        JLabel nameColumnDropdownLabel = new JLabel("   Select the column with the gene set descriptions:"); // ambiguous phrasing?       
        
        nameColumnDropdown.setAlignmentX(LEFT_ALIGNMENT);
        clusterPanel.setAlignmentX(LEFT_ALIGNMENT);
        
        mainPanel.add(nameColumnDropdownLabel);
        mainPanel.add(nameColumnDropdown);
        mainPanel.add(clusterPanel);
        mainPanel.add(confirmButton);
        
        return mainPanel;
	}

	private BasicCollapsiblePanel createClusterPanel() {
		BasicCollapsiblePanel clusterPanel = new BasicCollapsiblePanel("Advanced Clustering Options");
		
		JPanel innerPanel = new JPanel(); // To override default layout options of BasicCollapsiblePanel
		
		// Dropdown with all the available algorithms
		DefaultComboBoxModel model = new DefaultComboBoxModel();
		for (String algorithm : algorithmToColumnName.keySet()) {
			model.addElement(algorithm);
		}

		// To choose a clusterMaker algorithm
		clusterAlgorithmDropdown = new JComboBox(model);
		clusterAlgorithmDropdown.setPreferredSize(new Dimension(110, 30));
        // Alternatively, user can choose a clusterColumn themselves (if they've run clusterMaker themselves)
        clusterColumnDropdown = new JComboBox();
		clusterColumnDropdown.setPreferredSize(new Dimension(110, 30));

        // Only one dropdown visible at a time
        clusterAlgorithmDropdown.setVisible(true);
        clusterColumnDropdown.setVisible(false);
        
        clusterAlgorithmDropdown.setSelectedItem("MCL Cluster");
        
        JPanel dropdownPanel = new JPanel();
		dropdownPanel.add(clusterAlgorithmDropdown);
		dropdownPanel.add(clusterColumnDropdown);
		
        defaultButton = new JRadioButton(defaultButtonString);
        specifyColumnButton = new JRadioButton(specifyColumnButtonString);        
        defaultButton.addItemListener(new ItemListener() {
        	@Override
        	public void itemStateChanged(ItemEvent e) {
        		if (e.getStateChange() == ItemEvent.SELECTED) {
        			clusterAlgorithmDropdown.setVisible(true);
        		} else if (e.getStateChange() == ItemEvent.DESELECTED) {
        			clusterAlgorithmDropdown.setVisible(false);
        		}
        	}
        });
        specifyColumnButton.addItemListener(new ItemListener() {
        	@Override
        	public void itemStateChanged(ItemEvent e) {
        		if (e.getStateChange() == ItemEvent.SELECTED) {
        			clusterColumnDropdown.setVisible(true);
        		} else if (e.getStateChange() == ItemEvent.DESELECTED) {
        			clusterColumnDropdown.setVisible(false);
        		}
        	}
        });
        
        defaultButton.setSelected(true);
        
        // Group buttons together to make them mutually exclusive
        radioButtonGroup = new ButtonGroup();
        radioButtonGroup.add(defaultButton);
        radioButtonGroup.add(specifyColumnButton);
        
        JPanel radioButtonPanel = new JPanel();
		radioButtonPanel.setLayout(new BoxLayout(radioButtonPanel, BoxLayout.PAGE_AXIS));
        radioButtonPanel.add(defaultButton);
        radioButtonPanel.add(specifyColumnButton);
        
        innerPanel.add(radioButtonPanel);
        innerPanel.add(dropdownPanel);
        
        clusterPanel.add(innerPanel);
        
        return clusterPanel;
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

	public void updateSelectedView(CyNetworkView view) {
		nameColumnDropdown.removeAllItems();
		clusterColumnDropdown.removeAllItems();
		for (CyColumn column : view.getModel().getDefaultNodeTable().getColumns()) {
			if (column.getType() == String.class || (column.getType() == List.class && column.getListElementType() == String.class)) {
				nameColumnDropdown.addItem(column.getName());
			} else if (column.getType() == Integer.class || (column.getType() == List.class && column.getListElementType() == Integer.class)) {
				clusterColumnDropdown.addItem(column.getName());
			}
		}
		
		for (int i = 0; i < nameColumnDropdown.getItemCount(); i++) {
			if (nameColumnDropdown.getItemAt(i).getClass() == String.class) {
				if (((String) nameColumnDropdown.getItemAt(i)).contains("GS_DESCR")) {
					nameColumnDropdown.setSelectedIndex(i);
				}
			}
		}
		
		// Update the label with the network
		networkLabel.setText("  " + view.getModel().toString());
		((JPanel) networkLabel.getParent()).updateUI();
		selectedView = view;
	}

	public void updateColumnName(CyTable source, String oldColumnName,
			String newColumnName) {
		if (source == selectedView.getModel().getDefaultNodeTable()) {
			for (int i = 0; i < nameColumnDropdown.getItemCount(); i++) {
				if (nameColumnDropdown.getModel().getElementAt(i) == oldColumnName) {
					nameColumnDropdown.removeItem(oldColumnName);
					nameColumnDropdown.insertItemAt(newColumnName, i);
				}
			}
			for (int i = 0; i < clusterColumnDropdown.getItemCount(); i++) {
				if (clusterColumnDropdown.getModel().getElementAt(i) == oldColumnName) {
					clusterColumnDropdown.removeItem(oldColumnName);
					clusterColumnDropdown.insertItemAt(newColumnName, i);
				}
			}
		}
	}

	public void columnDeleted(CyTable source, String columnName) {
		if (source == selectedView.getModel().getDefaultNodeTable()) {
			for (int i = 0; i < nameColumnDropdown.getItemCount(); i++) {
				if (nameColumnDropdown.getModel().getElementAt(i) == columnName) {
					nameColumnDropdown.removeItem(columnName);
				}
			}
			for (int i = 0; i < clusterColumnDropdown.getItemCount(); i++) {
				if (clusterColumnDropdown.getModel().getElementAt(i) == columnName) {
					clusterColumnDropdown.removeItem(columnName);
				}
			}
		}
	}

	public void columnCreated(CyTable source, String columnName) {
		CyTable nodeTable = selectedView.getModel().getDefaultNodeTable();
		if (source == nodeTable) {
			CyColumn column = nodeTable.getColumn(columnName);
			if (column.getType() == String.class || (column.getType() == List.class && column.getListElementType() == String.class)) {
				nameColumnDropdown.addItem(column.getName());
			} else if (column.getType() == Integer.class || (column.getType() == List.class && column.getListElementType() == Integer.class)) {
				clusterColumnDropdown.addItem(column.getName());
			}
		}
	}
}