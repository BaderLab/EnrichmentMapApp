package org.baderlab.csplugins.enrichmentmap.autoannotate.view;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import org.baderlab.csplugins.enrichmentmap.autoannotate.model.AnnotationSet;
import org.baderlab.csplugins.enrichmentmap.autoannotate.model.Cluster;
import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.presentation.annotations.Annotation;
import org.cytoscape.view.presentation.annotations.AnnotationManager;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.presentation.property.DefaultVisualizableVisualProperty;

/**
 * @author arkadyark
 * <p>
 * Date   July 2, 2014<br>
 * Time   14:35:53 PM<br>
 */

public class AnnotationDisplayPanel extends JPanel implements CytoPanelComponent{

	private static final long serialVersionUID = 6589442061666054048L;
	
	private JPanel mainPanel;
	private HashMap<String, AnnotationSet> clusterSets;
	private int annotationCounter;
	private HashMap<AnnotationSet, JPanel> clustersToTables;



	public AnnotationDisplayPanel() {
		this.clusterSets = new HashMap<String, AnnotationSet>();
		this.clustersToTables = new HashMap<AnnotationSet, JPanel>();
		annotationCounter = 0;
		this.mainPanel = createMainPanel();
		setLayout(new BorderLayout());
		add(mainPanel, BorderLayout.NORTH);
	}
	
	public void addClusters(AnnotationSet clusters) {
		String annotationSetName = "Annotation Set " + String.valueOf(++annotationCounter);
		clusters.setName(annotationSetName);
		clusterSets.put(annotationSetName, clusters);
		JComboBox clusterSetDropdown = (JComboBox) mainPanel.getComponent(0);

		JPanel clusterTable = createClusterSetTablePanel(clusters);
		clustersToTables.put(clusters, clusterTable);
		JScrollPane clusterTableScroll = new JScrollPane(clusterTable);
		clusterTableScroll.setColumnHeaderView(((JTable) clusterTable.getComponent(0)).getTableHeader());
		clusterTableScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		add(clusterTableScroll, BorderLayout.WEST);

		clusterSetDropdown.addItem(clusters); // Automatically sets selected
		clusterSetDropdown.setSelectedIndex(clusterSetDropdown.getItemCount()-1);
	}
	
	public void removeClusters(AnnotationSet clusters) {
		JComboBox clusterSetDropdown = (JComboBox) mainPanel.getComponent(0);
		clusterSetDropdown.removeItem(clusterSets.get(clusters.name));
		clusterSets.remove(clusters.name);
	}
	
	private JPanel createMainPanel() {
		JPanel mainPanel = new JPanel();
		
		final JComboBox clusterSetDropdown = new JComboBox(); // Final so that it can be accessed by ActionListener
		clusterSetDropdown.addItemListener(new ItemListener(){
			public void itemStateChanged(ItemEvent itemEvent) {
				if (itemEvent.getStateChange() == ItemEvent.SELECTED) {
					AnnotationSet clusters = (AnnotationSet) itemEvent.getItem();
					clusters.drawAnnotations();							
					clustersToTables.get(clusters).getParent().getParent().setVisible(true); // Show selected table
					((JPanel) clusterSetDropdown.getParent()).updateUI();
				}
				
				if (itemEvent.getStateChange() == ItemEvent.DESELECTED) {
					AnnotationSet clusters = (AnnotationSet) itemEvent.getItem();
	         		clusters.eraseAnnotations();
					clustersToTables.get(clusters).getParent().getParent().setVisible(false);
					((JPanel) clusterSetDropdown.getParent()).updateUI();
				}
            }
		});

		mainPanel.add(clusterSetDropdown);
		
        // Button to remove all annotations
        JButton clearButton = new JButton("Remove Annotation Set");
        ActionListener clearActionListener = new ActionListener(){
        	public void actionPerformed(ActionEvent e) {
        		AnnotationSet clusters = (AnnotationSet) clusterSetDropdown.getSelectedItem();
        		CyNetworkView networkView = clusters.clusterSet.firstEntry().getValue().getNetworkView();
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
         		clusters.eraseAnnotations();
         		
         		clusterSetDropdown.removeItem(clusterSetDropdown.getSelectedItem());
         		remove(clustersToTables.get(clusters).getParent());
         		clustersToTables.remove(clusters);
        	}
        };
        clearButton.addActionListener(clearActionListener); 
        mainPanel.add(clearButton);
        
        // Button to remove all annotations
        JButton updateButton = new JButton("Update Annotation Set");
        ActionListener updateActionListener = new ActionListener(){
        	public void actionPerformed(ActionEvent e) {
        		AnnotationSet clusters = (AnnotationSet) clusterSetDropdown.getSelectedItem(); 
        		clusters.updateCoordinates();
        		clusters.eraseAnnotations(); 
        		clusters.drawAnnotations();
        	}
        };
        updateButton.addActionListener(updateActionListener); 
        mainPanel.add(updateButton);
        
		return mainPanel;
	}
	
	private JPanel createClusterSetTablePanel(final AnnotationSet clusters) {
		
		JPanel tablePanel = new JPanel();
		
		DefaultTableModel model = new DefaultTableModel() {
			private static final long serialVersionUID = -1277709187563893042L;

			@Override
		    public boolean isCellEditable(int row, int column) {
		        return column == 0 ? false : true;
		    }
		};
		model.addColumn("Cluster number");
		model.addColumn("Label");

		final JTable table = new JTable(model); // Final to be able to use inside of listener
		
		model.addTableModelListener(new TableModelListener() { // Update the label value
			@Override
			public void tableChanged(TableModelEvent e) {
				if (e.getType() == TableModelEvent.UPDATE || e.getColumn() == 1) {
					int editedRowIndex = e.getFirstRow() == table.getSelectedRow()? e.getLastRow() : e.getFirstRow(); 
					Cluster editedCluster = clusters.clusterSet.get(editedRowIndex + 1);
					editedCluster.setLabel((String) table.getValueAt(editedRowIndex, 1));
					editedCluster.erase();
					editedCluster.drawAnnotations();
				}
			}
		});
		for (Cluster cluster : clusters.clusterSet.values()) {
			Object[] rowData = {cluster , cluster.getLabel()};
			model.addRow(rowData);
		}
		
		table.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (! e.getValueIsAdjusting()) { // Down-click and up-click are separate events
					int selectedRowIndex = table.getSelectedRow();
					Cluster selectedCluster = (Cluster) table.getValueAt(selectedRowIndex, 0); // Final to use inside of 
					selectedCluster.select();
				}
			}
		});
		
		Dimension d = table.getPreferredSize();
		table.setPreferredScrollableViewportSize(d);
		
		tablePanel.add(table, BorderLayout.CENTER);
		return tablePanel;
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
