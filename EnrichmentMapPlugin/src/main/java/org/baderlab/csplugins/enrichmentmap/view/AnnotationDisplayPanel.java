package org.baderlab.csplugins.enrichmentmap.view;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import org.baderlab.csplugins.enrichmentmap.autoannotate.Cluster;
import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.presentation.annotations.Annotation;
import org.cytoscape.view.presentation.annotations.AnnotationManager;

/**
 * @author arkadyark
 * <p>
 * Date   July 2, 2014<br>
 * Time   14:35:53 PM<br>
 */

public class AnnotationDisplayPanel extends JPanel implements CytoPanelComponent{

	private static final long serialVersionUID = 6589442061666054048L;
	
	private JPanel mainPanel;
	private ArrayList<ArrayList<Cluster>> clusterSet;
	private ArrayList<JTable> tables;
	private JTable currentTable;
	private ArrayList<Cluster> currentClusterSet;
	private int annotationCounter;



	public AnnotationDisplayPanel() {
		this.clusterSet = new ArrayList<ArrayList<Cluster>>();
		this.tables = new ArrayList<JTable>(); 
		this.annotationCounter = 0;
		this.mainPanel = createMainPanel();
		add(mainPanel, BorderLayout.CENTER);
	}
	
	public void addClusters(ArrayList<Cluster> clusters) {
		annotationCounter++;
		if (!clusterSet.contains(clusters)) clusterSet.add(clusters);
		JComboBox clusterSetDropdown = (JComboBox) mainPanel.getComponent(0);
		mainPanel.add(createClusterSetTable(clusters));
		clusterSetDropdown.addItem("Annotation Set " + String.valueOf(annotationCounter)); // Automatically sets selected
		clusterSetDropdown.setSelectedIndex(annotationCounter-1);
	}
	
	public void removeClusters(ArrayList<Cluster> clusters) {
		JComboBox clusterSetDropdown = (JComboBox) mainPanel.getComponent(0);
		clusterSetDropdown.removeItem(clusterSet.indexOf(clusters));
	}
	
	private JPanel createMainPanel() {
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));
		
		final JComboBox clusterSetDropdown = new JComboBox(); // Final so that it can be accessed by ActionListener
		clusterSetDropdown.addItemListener(new ItemListener(){
			public void itemStateChanged(ItemEvent itemEvent) {
				if (itemEvent.getStateChange() == ItemEvent.SELECTED) {
					String clusterSetName = (String) itemEvent.getItem();
					int clusterIndex = Integer.valueOf(clusterSetName.substring("Annotation Set ".length()));
					currentClusterSet = clusterSet.get(clusterIndex-1);
					if (tables.size() > 0) {
						currentTable.setVisible(false); // Hide currently showing table
						currentTable = tables.get(clusterIndex-1);
						currentTable.setVisible(true); // Show selected table
					}
               }
            }
		});
		mainPanel.add(clusterSetDropdown);
		
        // Button to remove all annotations
        JButton clearButton = new JButton("Remove Annotation Set");
        ActionListener clearActionListener = new ActionListener(){
        	public void actionPerformed(ActionEvent e) {
        		AnnotationManager annotationManager = currentClusterSet.get(0).getAnnotationManager();
        		CyNetworkView networkView = currentClusterSet.get(0).getNetworkView();
        		CyNetwork network = networkView.getModel();
        		// Delete WordInfo column created by WordCloud
         		for (CyColumn column : network.getDefaultNodeTable().getColumns()) {
         			String name = column.getName();
         			if (name.equals("WC_Word") || name.equals("WC_FontSize") || name.equals("WC_Cluster") || name.equals("WC_Number")) {
         				// Problem - leaves them floating around the cloud manager in WordCloud - may have to do another Tuneable Task
         				network.getDefaultNodeTable().deleteColumn(name);
         			}
         		}
        		// Delete all annotations
         		for (Cluster cluster : currentClusterSet) {
         			cluster.getTextAnnotation().removeAnnotation();
         			cluster.getEllipse().removeAnnotation();
         		}
         		clusterSetDropdown.removeItem(clusterSetDropdown.getSelectedItem());
         		currentTable.setVisible(false);
        	}
        };
        clearButton.addActionListener(clearActionListener); 
        mainPanel.add(clearButton);
        
        clusterSetDropdown.setAlignmentX(LEFT_ALIGNMENT);
        
		return mainPanel;
	}
	
	private JTable createClusterSetTable(ArrayList<Cluster> clusters) {
		DefaultTableModel model = new DefaultTableModel();
		model.addColumn("Cluster number");
		model.addColumn("Cluster label");

		JTable table = new JTable(model);
		
		for (int i = 0; i < clusters.size(); i++) {
			Object[] rowData = {"Cluster " + clusters.get(i).getClusterNumber(), clusters.get(i).getLabel()};
			model.addRow(rowData);
		}
		JScrollPane displayTableScroll = new JScrollPane(table);
		displayTableScroll.add(table);
		if (currentTable != null) currentTable.setVisible(false); // Hide currently showing table
        currentTable = table;
        currentTable.setVisible(true); // Show selected table

		tables.add(table);
		table.setAlignmentX(LEFT_ALIGNMENT);
		
		return table;
	}
	
	@Override
	public Component getComponent() {
		return this;
	}

	@Override
	public CytoPanelName getCytoPanelName() {
		// TODO Auto-generated method stub
		return CytoPanelName.SOUTH;
	}

	@Override
	public Icon getIcon() {
		return null;
	}

	@Override
	public String getTitle() {
		return "Annotation Display";
	}

}
