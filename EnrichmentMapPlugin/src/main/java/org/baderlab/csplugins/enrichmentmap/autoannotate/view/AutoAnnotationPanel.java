package org.baderlab.csplugins.enrichmentmap.autoannotate.view;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import org.baderlab.csplugins.enrichmentmap.EnrichmentMapManager;
import org.baderlab.csplugins.enrichmentmap.autoannotate.AutoAnnotationManager;
import org.baderlab.csplugins.enrichmentmap.autoannotate.AutoAnnotationParameters;
import org.baderlab.csplugins.enrichmentmap.autoannotate.AutoAnnotationUtils;
import org.baderlab.csplugins.enrichmentmap.autoannotate.action.AutoAnnotationActions;
import org.baderlab.csplugins.enrichmentmap.autoannotate.model.AnnotationSet;
import org.baderlab.csplugins.enrichmentmap.autoannotate.model.Cluster;
import org.baderlab.csplugins.enrichmentmap.heatmap.HeatMapParameters;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.application.swing.CytoPanel;
import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.command.CommandExecutorTaskFactory;
import org.cytoscape.group.CyGroup;
import org.cytoscape.group.CyGroupFactory;
import org.cytoscape.group.CyGroupManager;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableUtil;
import org.cytoscape.model.events.RowSetRecord;
import org.cytoscape.util.swing.BasicCollapsiblePanel;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.work.SynchronousTaskManager;

/**
 * @author arkadyark
 * <p>
 * Date   June 16, 2014<br>
 * Time   11:26:32 AM<br>
 */

public class AutoAnnotationPanel extends JPanel implements CytoPanelComponent {

	private static final long serialVersionUID = 7901088595186775935L;

	private static final String defaultButtonString = "Use clusterMaker defaults";
	private static final String specifyColumnButtonString = "Select cluster column";

	// Dropdown menus
	private JComboBox<String> nameColumnDropdown;
	private JComboBox<String> clusterColumnDropdown;
	private JComboBox<String> clusterAlgorithmDropdown;

	// Radio buttons to choose between clusterMaker defaults and a cluster column
	private ButtonGroup clusterButtonGroup;
	private JRadioButton defaultButton;
	private JRadioButton specifyColumnButton;

	// Label that shows the name of the selected network (updates)
	private JLabel networkLabel;
	// Used to update panel on network selection
	private HashMap<CyNetworkView, JComboBox<AnnotationSet>> networkViewToClusterSetDropdown;
	// Used to update table on annotation set selection
	private HashMap<AnnotationSet, JTable> clustersToTables;
	// Used to store the output table
	private JPanel outputPanel;
	// Reference needed to hide when not needed
	private JPanel bottomButtonPanel;
	
	private CyNetworkView selectedView;
	private CyNetwork selectedNetwork;

	private EnrichmentMapManager emManager;
	private AutoAnnotationManager autoAnnotationManager;
	private AutoAnnotationParameters params;
	
	private JCheckBox layoutCheckBox;
	private JCheckBox groupsCheckBox;

	private DisplayOptionsPanel displayOptionsPanel;

	// Keeps track of when selection of a cluster is happening, to ignore events this fires
	private boolean selecting = false;
	// Keeps track of when annotation is happening, to ignore events this fires
	private boolean annotating = false;

	public AutoAnnotationPanel(CySwingApplication application, DisplayOptionsPanel displayOptionsPanel){
		this.clustersToTables = new HashMap<AnnotationSet, JTable>();
		this.networkViewToClusterSetDropdown = new HashMap<CyNetworkView, JComboBox<AnnotationSet>>();
		
		this.displayOptionsPanel = displayOptionsPanel;
		
		this.emManager = EnrichmentMapManager.getInstance();
		this.autoAnnotationManager = AutoAnnotationManager.getInstance();
		
		setLayout(new BorderLayout());
		setPreferredSize(new Dimension(500, 500));
		
		JPanel inputPanel = createInputPanel(); 
		
		bottomButtonPanel = createBottomButtonPanel();
		bottomButtonPanel.setAlignmentX(LEFT_ALIGNMENT);
		
		outputPanel = createOutputPanel();
		JScrollPane clusterTableScrollPane = new JScrollPane(outputPanel);
		
		add(inputPanel, BorderLayout.NORTH);
		add(clusterTableScrollPane, BorderLayout.CENTER);
		add(bottomButtonPanel, BorderLayout.SOUTH);
	}

	private JPanel createInputPanel() {
		JPanel inputPanel = new JPanel();
		inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.Y_AXIS));
		
		// Label showing the current network
		networkLabel = new JLabel("No network selected");
		Font font = networkLabel.getFont();
		networkLabel.setFont(new Font(font.getFamily(), font.getStyle(), 18));

		JLabel nameColumnDropdownLabel = new JLabel("   Select the column with the gene set descriptions:");
		// Gives the user a choice of column with gene names
		nameColumnDropdown = new JComboBox<String>();
		// Collapsible panel with advanced cluster options
		final BasicCollapsiblePanel advancedOptionsPanel = createAdvancedOptionsPanel(); 
		
		// Button to run the annotation
		JButton annotateButton = new JButton("Annotate!");
		ActionListener annotateAction = new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				boolean clusterMakerDefault = defaultButton.isSelected();
				String nameColumnName = (String) nameColumnDropdown.getSelectedItem();
				boolean layoutNodes = layoutCheckBox.isSelected();
				boolean useGroups = groupsCheckBox.isSelected();
				AutoAnnotationActions.annotateAction(selectedView, clusterMakerDefault,
						nameColumnName, layoutNodes, useGroups, 
						clusterAlgorithmDropdown, clusterColumnDropdown);
				advancedOptionsPanel.setCollapsed(true);
				// Update current params to newly created
				params = autoAnnotationManager.getNetworkViewToAutoAnnotationParameters().get(selectedView);
			}
		};
		annotateButton.addActionListener(annotateAction);
		annotateButton.setToolTipText("Create a new annotation set");

		inputPanel.add(networkLabel);
		inputPanel.add(nameColumnDropdownLabel);
		inputPanel.add(nameColumnDropdown);
		inputPanel.add(advancedOptionsPanel);
		inputPanel.add(annotateButton);
		
		networkLabel.setAlignmentX(CENTER_ALIGNMENT);
		nameColumnDropdownLabel.setAlignmentX(CENTER_ALIGNMENT);
		nameColumnDropdown.setAlignmentX(CENTER_ALIGNMENT);
		advancedOptionsPanel.setAlignmentX(CENTER_ALIGNMENT);
		annotateButton.setAlignmentX(CENTER_ALIGNMENT);
		
		return inputPanel;
	}
	
	private JPanel createOutputPanel() {
		JPanel outputPanel = new JPanel(new BorderLayout());

		JButton extractButton = new JButton("Extract");
		ActionListener extractActionListener = new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				AnnotationSet annotationSet = (AnnotationSet) networkViewToClusterSetDropdown.
						get(selectedView).getSelectedItem();
				JTable clusterTable = clustersToTables.get(annotationSet);
				AutoAnnotationActions.extractAction(annotationSet, clusterTable);
			}
		};
		extractButton.addActionListener(extractActionListener);
		extractButton.setToolTipText("Create a new cluster");
		
		// Button to merge two clusters
		JButton mergeButton = new JButton("Merge");
		ActionListener mergeActionListener = new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				AnnotationSet annotationSet = (AnnotationSet) networkViewToClusterSetDropdown.
						get(selectedView).getSelectedItem();
				JTable clusterTable = clustersToTables.get(annotationSet);
				AutoAnnotationActions.mergeAction(annotationSet, clusterTable);
			}
		};
		mergeButton.addActionListener(mergeActionListener);
		mergeButton.setToolTipText("Merge clusters into one");
		
		// Button to delete a cluster from an annotation set
		JButton deleteButton = new JButton("Delete");
		ActionListener deleteActionListener = new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				AnnotationSet annotationSet = (AnnotationSet) networkViewToClusterSetDropdown.
						get(selectedView).getSelectedItem();
				JTable clusterTable = clustersToTables.get(annotationSet);
				AutoAnnotationActions.deleteAction(annotationSet, clusterTable);
			}
		};
		deleteButton.addActionListener(deleteActionListener);
		deleteButton.setToolTipText("Delete selected cluster(s)");
		
		// Buttons to edit clusters
		JPanel outputButtonPanel = new JPanel();
		outputButtonPanel.add(new JLabel("Clusters:"));
		outputButtonPanel.add(extractButton);
		outputButtonPanel.add(mergeButton);
		outputButtonPanel.add(deleteButton);
		
		JPanel outputBottomPanel = new JPanel();
		outputBottomPanel.setLayout(new BoxLayout(outputBottomPanel, BoxLayout.PAGE_AXIS));
		
		outputBottomPanel.add(outputButtonPanel);
		
		outputPanel.add(outputBottomPanel, BorderLayout.SOUTH);
		
		return outputPanel;
	}
	
	private JPanel createBottomButtonPanel() {
		JPanel bottomButtonPanel = new JPanel();
		
		// Button to get remove an annotation set
		JButton removeButton = new JButton("Remove");
		ActionListener clearActionListener = new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				JComboBox<AnnotationSet> clusterSetDropdown = networkViewToClusterSetDropdown.get(selectedView);
				AnnotationSet annotationSet = (AnnotationSet) clusterSetDropdown.getSelectedItem();
				// Get rid of the table associated with this cluster set
				remove(clustersToTables.get(annotationSet).getParent());
				AutoAnnotationActions.removeAction(clusterSetDropdown, clustersToTables, params);
			}
		};
		removeButton.addActionListener(clearActionListener);
		removeButton.setToolTipText("Remove Annotation Set");

		// Button to update the current cluster set
		JButton updateButton = new JButton("Update");
		ActionListener updateActionListener = new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				AnnotationSet annotationSet = (AnnotationSet) networkViewToClusterSetDropdown.
						get(selectedView).getSelectedItem();
				AutoAnnotationActions.updateAction(annotationSet);
			}
		};
		updateButton.addActionListener(updateActionListener);
		updateButton.setToolTipText("Update Annotation Set");

		bottomButtonPanel = new JPanel();
		bottomButtonPanel.add(new JLabel("Annotation Sets:"));
		bottomButtonPanel.add(removeButton);
		bottomButtonPanel.add(updateButton);
		
		return bottomButtonPanel;
	}
	
	private BasicCollapsiblePanel createAdvancedOptionsPanel() {
		BasicCollapsiblePanel optionsPanel = new BasicCollapsiblePanel("Advanced Options");
		
		JPanel innerPanel = new JPanel(new BorderLayout());
		
		// Buttons to choose whether to use ClusterMaker or specify a column
		defaultButton = new JRadioButton(defaultButtonString);
		specifyColumnButton = new JRadioButton(specifyColumnButtonString);        
		defaultButton.addItemListener(new ItemListener() {
			// Ensure only one dropdown is shown at any time
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					clusterAlgorithmDropdown.setVisible(true);
					clusterColumnDropdown.setVisible(false);
				} else if (e.getStateChange() == ItemEvent.DESELECTED) {
					clusterAlgorithmDropdown.setVisible(false);
					clusterColumnDropdown.setVisible(true);
				}
			}
		});

		// Group buttons together to make them mutually exclusive
		clusterButtonGroup = new ButtonGroup();
		clusterButtonGroup.add(defaultButton);
		clusterButtonGroup.add(specifyColumnButton);

		JPanel clusterButtonPanel = new JPanel();
		clusterButtonPanel.setLayout(new BoxLayout(clusterButtonPanel, BoxLayout.PAGE_AXIS));
		clusterButtonPanel.add(defaultButton);
		clusterButtonPanel.add(specifyColumnButton);
		
		// Dropdown with all the available algorithms
		DefaultComboBoxModel<String> clusterDropdownModel = new DefaultComboBoxModel<String>();
		for (String algorithm : autoAnnotationManager.getAlgorithmToColumnName().keySet()) {
			clusterDropdownModel.addElement(algorithm);
		}

		// To choose a clusterMaker algorithm
		clusterAlgorithmDropdown = new JComboBox<String>(clusterDropdownModel);
		clusterAlgorithmDropdown.setPreferredSize(new Dimension(135, 30));
		// Alternatively, user can choose a cluster column themselves (if they've run clusterMaker themselves)
		clusterColumnDropdown = new JComboBox<String>();
		clusterColumnDropdown.setPreferredSize(new Dimension(135, 30));

		// Only one dropdown visible at a time
		clusterAlgorithmDropdown.setVisible(true);
		clusterColumnDropdown.setVisible(false);

		clusterAlgorithmDropdown.setSelectedItem("MCL Cluster");

		JPanel dropdownPanel = new JPanel();
		dropdownPanel.add(clusterAlgorithmDropdown);
		dropdownPanel.add(clusterColumnDropdown);
		
		// By default use clusterMaker defaults
		defaultButton.setSelected(true);
		
		// By default layout nodes by cluster
		layoutCheckBox = new JCheckBox("Layout nodes by cluster");
		layoutCheckBox.setSelected(false);

		// By default layout nodes by cluster
		groupsCheckBox = new JCheckBox("Create Groups for clusters");
		groupsCheckBox.setSelected(false);
		
		JPanel checkBoxPanel = new JPanel();
		checkBoxPanel.setLayout(new BoxLayout(checkBoxPanel, BoxLayout.Y_AXIS));
		checkBoxPanel.add(layoutCheckBox);
		checkBoxPanel.add(groupsCheckBox);
		
		JPanel nonClusterOptionPanel = new JPanel();
		nonClusterOptionPanel.add(checkBoxPanel);
		
		JPanel clusterOptionPanel = new JPanel(new BorderLayout());
		clusterOptionPanel.setBorder(BorderFactory.createTitledBorder("ClusterMaker Options"));
		clusterOptionPanel.add(clusterButtonPanel, BorderLayout.WEST);
		clusterOptionPanel.add(dropdownPanel, BorderLayout.EAST);
		
		innerPanel.add(clusterOptionPanel, BorderLayout.NORTH);
		innerPanel.add(nonClusterOptionPanel, BorderLayout.SOUTH);
		
		optionsPanel.add(innerPanel);
		
		return optionsPanel;
	}

	private JTable createClusterTable(final AnnotationSet annotationSet) {
		DefaultTableModel model = new DefaultTableModel() {
			private static final long serialVersionUID = -1277709187563893042L;

			Class<?>[] types = {Object.class, Integer.class};
			
			@Override
			public Class<?> getColumnClass(int columnIndex) {
				return this.types[columnIndex];
			}
			
			@Override
			public boolean isCellEditable(int row, int column) {
				return column == 0;
			}
		};
		model.addColumn("Cluster");
		model.addColumn("Number of nodes");

		final JTable table = new JTable(model); // Final to be able to use inside of listener
		table.setPreferredScrollableViewportSize(new Dimension(320, 250));
		table.getColumnModel().getColumn(0).setPreferredWidth(210);
		table.getColumnModel().getColumn(1).setPreferredWidth(100);

		model.addTableModelListener(new TableModelListener() { 
			// Update the label value
			@Override
			public void tableChanged(TableModelEvent e) {
				if (e.getType() == TableModelEvent.UPDATE && e.getColumn() == 0) {
					int editedRowIndex = table.getSelectedRow();
					// Get the cluster that was modified
					Iterator<Cluster> clusters = annotationSet.getClusterMap().values().iterator();
					ArrayList<Cluster> clustersNotInTable = new ArrayList<Cluster>();
					while (clusters.hasNext()) {
						clustersNotInTable.add(clusters.next());
					}
					for (int index = 0; index < table.getModel().getRowCount(); index++) {
						if (index != editedRowIndex) {
							clustersNotInTable.remove((Cluster) table.getValueAt(index, 0)); 
						}
					}
					// The modified cluster will be the only cluster left over
					if (clustersNotInTable.size() == 1) {
						Cluster editedCluster = clustersNotInTable.get(0);
						if (table.getValueAt(editedRowIndex, 0).getClass() == String.class) {
							editedCluster.setLabel((String) table.getValueAt(editedRowIndex, 0)); 
							table.setValueAt(editedCluster, editedRowIndex, 0); // Otherwise String stays in the table
							// Redraw cluster label since it has been edited
							editedCluster.eraseText();
							AutoAnnotationUtils.drawTextLabel(editedCluster);
						}
					}
				}
			}
		});
		// Populate the table model
		for (Cluster cluster : annotationSet.getClusterMap().values()) {
			// Each row contains the cluster (printed as a string, its label), and how many nodes it contains 
			Object[] rowData = {cluster, cluster.getSize()};
			model.addRow(rowData);
		}
		table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			@Override
			// Select the clusters that have been selected
			public void valueChanged(ListSelectionEvent e) {
				// Down-click and up-click are separate events, 
				//this makes only one of them fire
				if (! e.getValueIsAdjusting()) { 
					selecting = true;
					// Get the selected clusters from the selected rows
					int[] selectedRows = table.getSelectedRows();
					ArrayList<Cluster> selectedClusters = new ArrayList<Cluster>();
					for (int rowIndex=0; rowIndex < table.getRowCount(); rowIndex++) {
						boolean deselect = true;
						Cluster cluster = (Cluster) table.getModel().getValueAt(
								table.convertRowIndexToModel(rowIndex), 0);
						for (int selectedRow : selectedRows) {
							if (rowIndex == selectedRow) {
								// Selects the row and exits (to avoid deselecting it after)
								selectedClusters.add(cluster);
								deselect = false;
								break;
							}
						}
						if (deselect) {
							AutoAnnotationUtils.deselectCluster(cluster);
						}
					}
					setDisableHeatMapAutoFocus(true);
					SynchronousTaskManager<?> syncTaskManager = autoAnnotationManager.getSyncTaskManager();
					CommandExecutorTaskFactory executor = autoAnnotationManager.getCommandExecutor();
					for (Cluster cluster : selectedClusters) {
						AutoAnnotationUtils.selectCluster(cluster, executor, syncTaskManager);
					}
					// Deselect any previously selected nodes, WordCloud may have selected some
					for (CyRow row: selectedNetwork.getDefaultNodeTable().getAllRows()) {
						row.set(CyNetwork.SELECTED, false);
					}
					autoAnnotationManager.flushPayloadEvents();
					// Select nodes in the clusters
					for (Cluster cluster : selectedClusters) {
						for (CyNode node : cluster.getNodes()) {
							selectedNetwork.getRow(node).set(CyNetwork.SELECTED, true);
						}
					}
					
					// Selects the heatmap panel (if user tells it to)
					if (displayOptionsPanel.isHeatmapButtonSelected()) {
						CytoPanel southPanel = autoAnnotationManager.getSouthPanel();
						int index = southPanel.indexOfComponent(autoAnnotationManager.getHeatmapPanel());
						if (index != -1) {
							southPanel.setSelectedIndex(southPanel.indexOfComponent(autoAnnotationManager.getHeatmapPanel()));
						}
					}
					selecting = false;
				}
			}
		});
		// Make it so that the cluster table can be sorted
		table.setAutoCreateRowSorter(true);
		
		return table;
	}

	public void addClusters(AnnotationSet annotationSet) {
		params.addAnnotationSet(annotationSet);
		// If this is the panel's first AnnotationSet for this view
		CyNetworkView clusterView = params.getNetworkView();
		if (!networkViewToClusterSetDropdown.containsKey(clusterView)) {
			addNetworkView(clusterView);
		}

		// Create scrollable clusterTable
		JTable clusterTable = createClusterTable(annotationSet);
		JScrollPane clusterTableScroll = new JScrollPane(clusterTable);
		clusterTableScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		// Add the table to the panel
		outputPanel.add(clusterTableScroll, BorderLayout.CENTER);
		clustersToTables.put(annotationSet, clusterTable);
		// Add the annotation set to the dropdown
		JComboBox<AnnotationSet> clusterSetDropdown = networkViewToClusterSetDropdown.get(clusterView);
		clusterSetDropdown.addItem(annotationSet);
		// Select the most recently added annotation set
		clusterSetDropdown.setSelectedIndex(clusterSetDropdown.getItemCount()-1);
	}

	private void addNetworkView(CyNetworkView view) {
		// Create dropdown with cluster sets of this networkView
		JComboBox<AnnotationSet> annotationSetDropdown = new JComboBox<AnnotationSet>();
		annotationSetDropdown.addItemListener(new ItemListener(){
			// Switch the selected annotation set
			public void itemStateChanged(ItemEvent itemEvent) {
				CyGroupManager groupManager = autoAnnotationManager.getGroupManager();
				CyGroupFactory groupFactory = autoAnnotationManager.getGroupFactory();
				if (itemEvent.getStateChange() == ItemEvent.SELECTED) {
					AnnotationSet annotationSet = (AnnotationSet) itemEvent.getItem();
					displayOptionsPanel.setSelectedAnnotationSet(annotationSet);
					params.setSelectedAnnotationSet(annotationSet);
					// Update the selected annotation set
					annotationSet.updateCoordinates();
					String annotationSetName = annotationSet.getName();
					// Get the table where WordCloud results are stored
					Long clusterTableSUID = selectedNetwork.getDefaultNetworkTable().getRow(
							selectedNetwork.getSUID()).get(annotationSetName, Long.class);
					CyTable clusterSetTable = autoAnnotationManager.getTableManager().getTable(clusterTableSUID);
					for (Cluster cluster : annotationSet.getClusterMap().values()) {
						if (cluster.getEllipse() == null && cluster.getTextAnnotation() == null) {
							// Update the text label of the selected cluster
							AutoAnnotationUtils.updateClusterLabel(cluster, clusterSetTable);
						}
						// Redraw selected clusters
						AutoAnnotationUtils.drawCluster(cluster);
						// Recreate groups if being used
						if (annotationSet.usingGroups()) {
							/*CyGroup group = groupFactory.createGroup(selectedNetwork, cluster.getNodesToCoordinates().keySet().iterator().next(), true);
							ArrayList<CyNode> nodesWithoutGroupNode = new ArrayList<CyNode>(cluster.getNodesToCoordinates().keySet());
							nodesWithoutGroupNode.remove(0);
							group.addNodes(nodesWithoutGroupNode);*/
							
						
							//Create a Node with the Annotation Label to represent the group
							CyNode groupNode = selectedNetwork.addNode();
							selectedNetwork.getRow(groupNode).set(CyNetwork.NAME, cluster.getLabel());
							autoAnnotationManager.flushPayloadEvents();
							//selectedView.getNodeView(groupNode).setVisualProperty(BasicVisualLexicon.NODE_VISIBLE, false);
							//cluster.addNode(groupNode);
							
							CyGroup group = groupFactory.createGroup(selectedNetwork, groupNode,new ArrayList<CyNode>(cluster.getNodes()),null, true);							
							cluster.setGroup(group);
						}
					}
					setOutputVisibility(true);
					clustersToTables.get(annotationSet).getParent().getParent().setVisible(true); // Show selected table
					updateUI();
				} else if (itemEvent.getStateChange() == ItemEvent.DESELECTED) {
					AnnotationSet clusters = (AnnotationSet) itemEvent.getItem();
					for (Cluster cluster : clusters.getClusterMap().values()) {
						// Hide the annotations
						cluster.erase();
						// Remove the groups if being used
						CyGroup group = cluster.getGroup();
						if (group != null) {
							if (cluster.isCollapsed()) {
								cluster.getGroup().expand(selectedNetwork);
							}
							group.removeGroupFromNetwork(selectedNetwork);
							groupManager.destroyGroup(group);
							cluster.removeGroup();
						}
					}
					// Hide the table for deselected clusters
					if (clustersToTables.containsKey(clusters)) {
						// If it hasn't been deleted, in which case it'll be removed already
						clustersToTables.get(clusters).getParent().getParent().setVisible(false);
					}
					setOutputVisibility(false);
					updateUI();
				}
			}
		});
		outputPanel.add(annotationSetDropdown, BorderLayout.NORTH);
		networkViewToClusterSetDropdown.put(view, annotationSetDropdown);

		selectedView = view;
		selectedNetwork = view.getModel();
		params = autoAnnotationManager.getNetworkViewToAutoAnnotationParameters().get(view);

		networkLabel.setText(selectedNetwork.toString());
		updateUI();
	}
	
	public void setOutputVisibility(boolean b) {
		// Sets the visibility of the output related components
		outputPanel.getParent().getParent().setVisible(b);
		bottomButtonPanel.setVisible(b);
		if (b) {
			autoAnnotationManager.getDisplayOptionsPanelAction().actionPerformed(new ActionEvent("",0,""));
		} else {
			displayOptionsPanel.setVisible(b);
		}
	}
	
	public void updateSelectedView(CyNetworkView view) {
		selectedView = view;
		selectedNetwork = view.getModel();
		
		if (autoAnnotationManager.getNetworkViewToAutoAnnotationParameters().containsKey(selectedView)) {
			// There exists a cluster set for this view
			params = autoAnnotationManager.getNetworkViewToAutoAnnotationParameters().get(selectedView);
			if (!networkViewToClusterSetDropdown.containsKey(selectedView)) {
				// Params has just been loaded, add its annotationSets to the dropdown
				// Also adds its network view
				for (AnnotationSet annotationSet : params.getAnnotationSets().values()) {
					addClusters(annotationSet);
				}
				// Restore selected annotation set
				if (params.getSelectedAnnotationSet() != null) {
					networkViewToClusterSetDropdown.get(selectedView).setSelectedItem(params.getSelectedAnnotationSet());
				}
			}
			// Show the table and buttons
			setOutputVisibility(true);
		} else {
			// Hide/keep hidden the table and buttons
			setOutputVisibility(false);
		}
		
		// Repopulate the dropdown menus
		nameColumnDropdown.removeAllItems();
		clusterColumnDropdown.removeAllItems();
		for (CyColumn column : view.getModel().getDefaultNodeTable().getColumns()) {
			// Add string columns to nameColumnDropdown
			if (column.getType() == String.class) {
				nameColumnDropdown.addItem(column.getName());
			// Add integer/integer list columns to clusterColumnDropdown
			} else if (column.getType() == Integer.class || (column.getType() == List.class && column.getListElementType() == Integer.class)) {
				clusterColumnDropdown.addItem(column.getName());
			}
		}

		// Try to guess the appropriate columns
		for (int i = 0; i < nameColumnDropdown.getItemCount(); i++) {
			if (nameColumnDropdown.getItemAt(i).getClass() == String.class) {
				if (((String) nameColumnDropdown.getItemAt(i)).contains("GS_DESCR")) {
					nameColumnDropdown.setSelectedIndex(i);
				}
			}
		}

		// Update the label with the network name
		networkLabel.setText("  " + selectedNetwork.toString());
		updateUI();
		// Hide previous dropdown and table
		if (networkViewToClusterSetDropdown.containsKey(selectedView)) {
			JComboBox<?> currentDropdown = networkViewToClusterSetDropdown.get(selectedView);
			currentDropdown.setVisible(false);
			// Hide the scrollpane containing the clusterTable
			clustersToTables.get(currentDropdown.getSelectedItem()).getParent().getParent().setVisible(false);
		}

		// Show new dropdown and table
		if (networkViewToClusterSetDropdown.containsKey(selectedView)) {
			networkViewToClusterSetDropdown.get(selectedView).setVisible(true);
			clustersToTables.get(networkViewToClusterSetDropdown.get(selectedView).getSelectedItem()).getParent().getParent().setVisible(true);
		}
	}

	public JTable getClusterTable(AnnotationSet annotationSet) {
		return clustersToTables.get(annotationSet);
	}
	
	public void updateColumnName(CyTable source, String oldColumnName,
			String newColumnName) {
		// Column name has been changed, check if it is 
		// in the dropdowns and update if needed
		if (source == selectedNetwork.getDefaultNodeTable()) {
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

	public void removeNetworkView(CyNetworkView view) {
		if (networkViewToClusterSetDropdown.size() == 1 && networkViewToClusterSetDropdown.containsKey(view)) {
			networkLabel.setText("No network selected");
			nameColumnDropdown.removeAllItems();
			clusterColumnDropdown.removeAllItems();
			updateUI();
			if (networkViewToClusterSetDropdown.containsKey(view)) {
				JComboBox<?> clusterSetDropdown = networkViewToClusterSetDropdown.get(view);
				Container clusterTable = clustersToTables.get(networkViewToClusterSetDropdown.get(view).getSelectedItem()).getParent().getParent();
				clusterSetDropdown.getParent().remove(clusterSetDropdown);
				clusterTable.getParent().remove(clusterTable);
				networkViewToClusterSetDropdown.remove(view);
			}
		
			selectedView = null;
			selectedNetwork = null;
			params = null;
		}
	}

	public void columnDeleted(CyTable source, String columnName) {
		if (source == selectedNetwork.getDefaultNodeTable()) {
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
		CyTable nodeTable = selectedNetwork.getDefaultNodeTable();
		if (source == nodeTable) {
			CyColumn column = nodeTable.getColumn(columnName);
			if (column.getType() == String.class) {
				if (((DefaultComboBoxModel<String>) nameColumnDropdown.getModel()).getIndexOf(column) == -1) { // doesn't already contain column
					nameColumnDropdown.addItem(column.getName());
				}
			} else if (column.getType() == Integer.class || (column.getType() == List.class && column.getListElementType() == Integer.class)) {
				if (((DefaultComboBoxModel<String>) clusterColumnDropdown.getModel()).getIndexOf(column) == -1) { // doesn't already contain column
					clusterColumnDropdown.addItem(column.getName());					
				}
			}
		}
	}
	
	public void updateNodeSelection(CyTable source,
			Collection<RowSetRecord> columnRecords) {
		CyTable nodeTable = selectedNetwork.getDefaultNodeTable();
		List<CyNode> selectedNodes = CyTableUtil.getNodesInState(selectedNetwork, CyNetwork.SELECTED, true);
		// Ignore events when working on selection/deselection
		if (source.equals(nodeTable) && params != null && !selecting && !annotating) {
			AnnotationSet annotationSet = params.getSelectedAnnotationSet();
			for (Cluster cluster : annotationSet.getClusterMap().values()) {
				// Only consider deselecting selected clusters
				if (cluster.isSelected()) {
					boolean deselected = false;
					for (CyNode node : cluster.getNodes()) {
						CyRow nodeRow = selectedNetwork.getRow(node);
						for (RowSetRecord row : columnRecords) {
							if (nodeRow == row.getRow() && (Boolean) row.getValue() == false) {
								TableModel clusterTableModel = clustersToTables.get(annotationSet).getModel();
								ListSelectionModel clusterListSelectionModel = clustersToTables.get(annotationSet).getSelectionModel();
								for (int rowIndex = 0; rowIndex < clusterTableModel.getRowCount(); rowIndex++) {
									if (cluster.equals(clusterTableModel.getValueAt(rowIndex, 0))) {
										clusterListSelectionModel.removeSelectionInterval(rowIndex, rowIndex);
										autoAnnotationManager.flushPayloadEvents();
										break;
									}
								}
								deselected = true;
								break;
							}
						}
						if (deselected) break;
					}
				} else {
					boolean select = true;
					for (CyNode node : cluster.getNodes()) {
						if (!selectedNodes.contains(node)) {
							select = false;
							break;
						}
					}
					if (select) {
						TableModel clusterTableModel = clustersToTables.get(annotationSet).getModel();
						ListSelectionModel clusterListSelectionModel = clustersToTables.get(annotationSet).getSelectionModel();
						for (int rowIndex = 0; rowIndex < clusterTableModel.getRowCount(); rowIndex++) {
							if (cluster.equals(clusterTableModel.getValueAt(rowIndex, 0))) {
								clusterListSelectionModel.addSelectionInterval(rowIndex, rowIndex);
								autoAnnotationManager.flushPayloadEvents();
								break;
							}
						}
					}
				}
			}
		}
	}

	public void setHeatMapNoSort() {
		try {
			HeatMapParameters heatMapParameters = emManager.getMap(selectedNetwork.getSUID()).getParams().getHmParams();
			if (heatMapParameters != null) {
				heatMapParameters.setSort(HeatMapParameters.Sort.NONE);
			}
		} catch(NullPointerException e) {
			return;
		}
	}

	public void setDisableHeatMapAutoFocus(boolean b) {
		try {
			emManager.getMap(selectedNetwork.getSUID()).getParams().setDisableHeatmapAutofocus(b);
		} catch (NullPointerException e) {
			return;
		}
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
		//create an icon for the enrichment map panels
		URL EMIconURL = this.getClass().getResource("enrichmentmap_logo_notext_small.png");
		ImageIcon EMIcon = null;
		if (EMIconURL != null) {
			EMIcon = new ImageIcon(EMIconURL);
		}
		return EMIcon;
	}

	@Override
	public String getTitle() {
		return "Annotation Panel";
	}

	public AnnotationSet getSelectedAnnotationSet() {
		return params.getSelectedAnnotationSet();
	}

	public void setDisplayOptionsPanel(DisplayOptionsPanel displayOptionsPanel) {
		this.displayOptionsPanel = displayOptionsPanel; 
	}

	public void setAnnotating(boolean b) {
		annotating = b;
	}
}