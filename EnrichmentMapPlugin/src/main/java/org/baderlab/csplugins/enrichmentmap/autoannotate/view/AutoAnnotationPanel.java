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
import java.util.HashMap;
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
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;

import org.baderlab.csplugins.enrichmentmap.EnrichmentMapManager;
import org.baderlab.csplugins.enrichmentmap.autoannotate.AutoAnnotationManager;
import org.baderlab.csplugins.enrichmentmap.autoannotate.AutoAnnotationParameters;
import org.baderlab.csplugins.enrichmentmap.autoannotate.action.AnnotateButtonActionListener;
import org.baderlab.csplugins.enrichmentmap.autoannotate.action.ClusterTableSelctionAction;
import org.baderlab.csplugins.enrichmentmap.autoannotate.model.AnnotationSet;
import org.baderlab.csplugins.enrichmentmap.autoannotate.model.Cluster;
import org.baderlab.csplugins.enrichmentmap.autoannotate.model.ClusterTableModel;
import org.baderlab.csplugins.enrichmentmap.autoannotate.task.RemoveAnnotationTask;
import org.baderlab.csplugins.enrichmentmap.autoannotate.task.UpdateAnnotationTask;
import org.baderlab.csplugins.enrichmentmap.autoannotate.task.VisualizeClusterAnnotationTaskFactory;
import org.baderlab.csplugins.enrichmentmap.autoannotate.task.cluster.DeleteClusterTask;
import org.baderlab.csplugins.enrichmentmap.autoannotate.task.cluster.ExtractClusterTask;
import org.baderlab.csplugins.enrichmentmap.autoannotate.task.cluster.MergeClustersTask;
import org.baderlab.csplugins.enrichmentmap.autoannotate.task.cluster.UpdateClusterLabelTask;
import org.baderlab.csplugins.enrichmentmap.heatmap.HeatMapParameters;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.group.CyGroup;
import org.cytoscape.group.CyGroupFactory;
import org.cytoscape.group.CyGroupManager;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyTable;
import org.cytoscape.util.swing.BasicCollapsiblePanel;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.TaskIterator;

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
	//private HashMap<AnnotationSet, JTable> clustersToTables;
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

		//Hashmap tracking the views to the group of annotation sets
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
		AnnotateButtonActionListener annotateAction = new AnnotateButtonActionListener( this);
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
				ExtractClusterTask extract = new ExtractClusterTask(annotationSet);
				autoAnnotationManager.getDialogTaskManager().execute(new TaskIterator(extract));
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
				MergeClustersTask merge = new MergeClustersTask(annotationSet);
				autoAnnotationManager.getDialogTaskManager().execute(new TaskIterator(merge));				
			}
		};
		mergeButton.addActionListener(mergeActionListener);
		mergeButton.setToolTipText("Merge clusters into one");
		ActionListener deletActionListener = new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				AutoAnnotationManager.getInstance().getDialogTaskManager().execute(new TaskIterator(new DeleteClusterTask(params)));				
			}
		};
		// Button to delete a cluster from an annotation set
		JButton deleteButton = new JButton("Delete");	
		deleteButton.addActionListener(deletActionListener);
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
				int confirmation = JOptionPane.showConfirmDialog(null, "Are you sure you want to remove this annotation set? This cannot be undone.",
						"Remove confirmation", JOptionPane.YES_NO_OPTION);
				
				if (confirmation == JOptionPane.YES_OPTION) {
				
					JComboBox<AnnotationSet> clusterSetDropdown = networkViewToClusterSetDropdown.get(selectedView);
					AnnotationSet annotationSet = (AnnotationSet) clusterSetDropdown.getSelectedItem();
					// Get rid of the table associated with this cluster set
					remove(annotationSet.getClusterTable().getParent());
					RemoveAnnotationTask remove = new RemoveAnnotationTask(params);
					autoAnnotationManager.getDialogTaskManager().execute(new TaskIterator(remove));
					// Remove cluster set from dropdown
					clusterSetDropdown.removeItem(annotationSet);
				}
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
				UpdateAnnotationTask update = new UpdateAnnotationTask(annotationSet);
				autoAnnotationManager.getDialogTaskManager().execute(new TaskIterator(update));
				
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
		
		// Populate the table model
		Object[][] data = new Object[annotationSet.getClusterMap().size()][2];
		int i=0;
		for (Cluster cluster : annotationSet.getClusterMap().values()) {
			// Each row contains the cluster (printed as a string, its label), and how many nodes it contains
			data[i][0] = cluster;
			data[i][1] = cluster.getSize();
			i++;
		}
		String[] columnNames = {"Cluster","Number of nodes"};
		
		//Create a new Cluster Table Model
		ClusterTableModel model = new ClusterTableModel(columnNames, data,annotationSet.getClusterMap());

		JTable table = new JTable(model); 
		table.setPreferredScrollableViewportSize(new Dimension(320, 250));
		table.getColumnModel().getColumn(0).setPreferredWidth(210);
		table.getColumnModel().getColumn(1).setPreferredWidth(100);
				
		
		annotationSet.setClusterTable(table);
		
		table.getSelectionModel().addListSelectionListener(new ClusterTableSelctionAction(annotationSet));
		table.getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		
		// Make it so that the cluster table can be sorted
		table.setAutoCreateRowSorter(true);
	
		return table;
	}
	
	

	public void addClusters(AnnotationSet annotationSet, AutoAnnotationParameters params) {
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
		annotationSet.setClusterTable(clusterTable);
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
			// Switch the selected annotation set - this method is also called when you reload a session
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
					TaskIterator currentTasks = new TaskIterator();
					for (Cluster cluster : annotationSet.getClusterMap().values()) {
						if (cluster.getEllipse() == null && cluster.getTextAnnotation() == null) {
							// Update the text label of the selected cluster
							currentTasks.append(new UpdateClusterLabelTask(cluster, clusterSetTable));
						}
						// Redraw selected clusters
						VisualizeClusterAnnotationTaskFactory visualizeCluster = new VisualizeClusterAnnotationTaskFactory(cluster);
						currentTasks.append(visualizeCluster.createTaskIterator());

					}
					//execute all the cluster and label drawing tasks
					AutoAnnotationManager.getInstance().getDialogTaskManager().execute(currentTasks);
										
					setOutputVisibility(true);
					annotationSet.getClusterTable().getParent().getParent().setVisible(true); // Show selected table
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
						// Hide the table for deselected AnnotationSet clusters
						clusters.getClusterTable().getParent().getParent().setVisible(false);
					}
					
					
	
					setOutputVisibility(false);
					updateUI();
				}
			}
		});
		outputPanel.add(annotationSetDropdown, BorderLayout.NORTH);
		networkViewToClusterSetDropdown.put(view, annotationSetDropdown);

		selectedView = view;
		selectedNetwork = autoAnnotationManager.getApplicationManager().getCurrentNetwork();
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
		selectedNetwork = autoAnnotationManager.getApplicationManager().getCurrentNetwork();
		
		if (autoAnnotationManager.getNetworkViewToAutoAnnotationParameters().containsKey(selectedView)) {
			// There exists a cluster set for this view
			params = autoAnnotationManager.getNetworkViewToAutoAnnotationParameters().get(selectedView);
			if (!networkViewToClusterSetDropdown.containsKey(selectedView)) {
				// Params has just been loaded, add its annotationSets to the dropdown
				// Also adds its network view
				for (AnnotationSet annotationSet : params.getAnnotationSets().values()) {
					addClusters(annotationSet,params);
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
			getCurrentSelectedTable(selectedView).getParent().getParent().setVisible(false);
		}

		// Show new dropdown and table
		if (networkViewToClusterSetDropdown.containsKey(selectedView)) {
			networkViewToClusterSetDropdown.get(selectedView).setVisible(true);
			 getCurrentSelectedTable(selectedView).getParent().getParent().setVisible(true);
		}
	}

	public JTable getClusterTable(AnnotationSet annotationSet) {
		return annotationSet.getClusterTable();
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

	private JTable getCurrentSelectedTable(CyNetworkView view){
		if(view != null)
			if(autoAnnotationManager.getNetworkViewToAutoAnnotationParameters() != null)
				if(autoAnnotationManager.getNetworkViewToAutoAnnotationParameters().containsKey(view))
					if(autoAnnotationManager.getNetworkViewToAutoAnnotationParameters().get(view).getSelectedAnnotationSet() != null)
						return autoAnnotationManager.getNetworkViewToAutoAnnotationParameters().get(view).getSelectedAnnotationSet().getClusterTable();
		
		return null;
			
	}
	
	public void removeNetworkView(CyNetworkView view) {
		if (networkViewToClusterSetDropdown.size() == 1 && networkViewToClusterSetDropdown.containsKey(view)) {
	
			networkLabel.setText("No network selected");
			nameColumnDropdown.removeAllItems();
			clusterColumnDropdown.removeAllItems();
			updateUI();
			if (networkViewToClusterSetDropdown.containsKey(view)) {
				JComboBox<?> clusterSetDropdown = networkViewToClusterSetDropdown.get(view);
				Container clusterTable = getCurrentSelectedTable(view).getParent().getParent();
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
	
	//Getter and Setters for parameters needed by the Actionlisteners.
	public CyNetworkView getCurrentView(){
		return selectedView;
	}
	
	public boolean isLayoutNodeSelected(){
		return layoutCheckBox.isSelected();
	}
	
	public boolean isGroupsSelected(){
		return groupsCheckBox.isSelected();
	}
	
	public boolean isClusterMaker(){
		return defaultButton.isSelected();
	}
	
	public String getAnnotationColumnName(){
		return (String) nameColumnDropdown.getSelectedItem();
	}
	
	public String getAlgorithm(){
		return (String) clusterAlgorithmDropdown.getSelectedItem();
	}
	
	public String getClusterColumnName(AutoAnnotationParameters currentParams){
		if(isClusterMaker()){
			String clusteringColumnName1 = AutoAnnotationManager.algorithmToColumnName.get(getAlgorithm());
			CyTable defaultNodetable = autoAnnotationManager.getApplicationManager().getCurrentNetwork().getDefaultNodeTable();
			return currentParams.nextClusterColumnName(clusteringColumnName1, defaultNodetable);
		}
		else 
			return (String) clusterColumnDropdown.getSelectedItem();
	}
	
	public void updateParameters(){

		// Update current params to newly created
		params = autoAnnotationManager.getNetworkViewToAutoAnnotationParameters().get(selectedView);

	}
	
}