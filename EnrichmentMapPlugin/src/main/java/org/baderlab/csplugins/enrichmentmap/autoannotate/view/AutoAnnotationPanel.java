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
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;

import org.baderlab.csplugins.enrichmentmap.EnrichmentMapManager;
import org.baderlab.csplugins.enrichmentmap.autoannotate.AutoAnnotationManager;
import org.baderlab.csplugins.enrichmentmap.autoannotate.AutoAnnotationParameters;
import org.baderlab.csplugins.enrichmentmap.autoannotate.AutoAnnotationUtils;
import org.baderlab.csplugins.enrichmentmap.autoannotate.model.AnnotationSet;
import org.baderlab.csplugins.enrichmentmap.autoannotate.model.Cluster;
import org.baderlab.csplugins.enrichmentmap.autoannotate.task.AutoAnnotationTaskFactory;
import org.baderlab.csplugins.enrichmentmap.heatmap.HeatMapParameters;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.command.CommandExecutorTaskFactory;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.BasicCollapsiblePanel;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.presentation.annotations.AnnotationFactory;
import org.cytoscape.view.presentation.annotations.AnnotationManager;
import org.cytoscape.view.presentation.annotations.ShapeAnnotation;
import org.cytoscape.view.presentation.annotations.TextAnnotation;
import org.cytoscape.work.swing.DialogTaskManager;

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
	private JComboBox nameColumnDropdown;
	private JComboBox clusterColumnDropdown;
	private JComboBox clusterAlgorithmDropdown;
	
	// Radio buttons to choose between clusterMaker defaults and a cluster column
	private ButtonGroup radioButtonGroup;
	private JRadioButton defaultButton;
	private JRadioButton specifyColumnButton;
	
	// Label that shows the name of the selected network (updates)
	private JLabel networkLabel;
	
	private HashMap<CyNetworkView, JComboBox> networkViewToClusterSetDropdown;
	private HashMap<AnnotationSet, JTable> clustersToTables;
	// Panels on the CytoPanel component
	private JPanel mainPanel;
	private JPanel clusterTablePanel;
	// Toggle of what to show on selection
	private boolean showHeatmap;

	private CyNetworkView selectedView;
	private CyNetwork selectedNetwork;

	private EnrichmentMapManager emManager;
	private AutoAnnotationManager autoAnnotationManager;
	private AutoAnnotationParameters params;

	private AutoAnnotationTaskFactory autoAnnotatorTaskFactory;

	// Having them as fields lets me hide them when not needed
	private JButton clearButton;
	private JButton updateButton;
	private BasicCollapsiblePanel selectionPanel;

	public AutoAnnotationPanel(CySwingApplication application, AutoAnnotationManager autoAnnotationManager, 
			CyServiceRegistrar registrar, DialogTaskManager dialogTaskManager, EnrichmentMapManager emManager){

		this.clustersToTables = new HashMap<AnnotationSet, JTable>();
		this.networkViewToClusterSetDropdown = new HashMap<CyNetworkView, JComboBox>();

		this.emManager = emManager;
		this.autoAnnotationManager = autoAnnotationManager;
		
		mainPanel = createMainPanel(application, registrar, dialogTaskManager);
		add(mainPanel,BorderLayout.CENTER);
		setPreferredSize(new Dimension(500, getHeight()));
	}
	
	private JPanel createMainPanel(
			final CySwingApplication application,
			final CyServiceRegistrar registrar, final DialogTaskManager dialogTaskManager) {
		
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        
		networkLabel = new JLabel("No network selected");
		Font font = networkLabel.getFont();
		networkLabel.setFont(new Font(font.getFamily(), font.getStyle(), 18));
		
		JLabel nameColumnDropdownLabel = new JLabel("   Select the column with the gene set descriptions:");
        // Give the user a choice of column with gene names
        nameColumnDropdown = new JComboBox();
        // Dropdown 
        
        // Collapsible panel with advanced cluster options
        JPanel advancedOptionsPanel = createAdvancedOptionsPanel(); 
        
        // Run the annotation
        JButton annotateButton = new JButton("Annotate!");
        ActionListener annotateAction = new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				String clusterColumnName = "";
				String algorithm = "";
				// If using default clustermaker parameters
				if (defaultButton.isSelected()) {
					algorithm = (String) clusterAlgorithmDropdown.getSelectedItem();
					clusterColumnName = autoAnnotationManager.getAlgorithmToColumnName().get(algorithm);
				} else if (specifyColumnButton.isSelected()) {
					// If using a user specified column
					clusterColumnName = (String) clusterColumnDropdown.getSelectedItem();
				}
				String nameColumnName = (String) nameColumnDropdown.getSelectedItem();
				if (autoAnnotationManager.getNetworkViewToAutoAnnotationParameters().containsKey(selectedView.getSUID())) {
					// Not the first annotation set for this network view
					params = autoAnnotationManager.getNetworkViewToAutoAnnotationParameters().get(selectedView.getSUID());
				} else {
					// Register the new network view parameters
					params = new AutoAnnotationParameters();
					autoAnnotationManager.getNetworkViewToAutoAnnotationParameters().put(selectedView.getSUID(), params);
				}
				int annotationSetNumber = params.getAnnotationSetNumber();
				CyTableManager tableManager = autoAnnotationManager.getTableManager();
				autoAnnotatorTaskFactory = new AutoAnnotationTaskFactory(application, autoAnnotationManager, selectedView, 
						clusterColumnName, nameColumnName, algorithm, annotationSetNumber, registrar, dialogTaskManager, tableManager);
				dialogTaskManager.execute(autoAnnotatorTaskFactory.createTaskIterator());
				params.incrementAnnotationSetNumber();
				// Unhide the buttons
				clearButton.setVisible(true);
				updateButton.setVisible(true);
				selectionPanel.setVisible(true);
			}
        };
        annotateButton.addActionListener(annotateAction);
        
        clusterTablePanel = new JPanel();
        clusterTablePanel.setLayout(new BorderLayout());
        clusterTablePanel.setPreferredSize(new Dimension(350, 350));
        clusterTablePanel.setMaximumSize(new Dimension(350, 350));
        
        selectionPanel = createSelectionPanel();
        
        clearButton = new JButton("Remove Annotation Set");
        ActionListener clearActionListener = new ActionListener(){
        	public void actionPerformed(ActionEvent e) {
        		JComboBox clusterSetDropdown = networkViewToClusterSetDropdown.get(selectedView);
        		AnnotationSet clusters = (AnnotationSet) clusterSetDropdown.getSelectedItem();
        		// Delete wordCloud table
        		autoAnnotationManager.getTableManager().deleteTable(selectedNetwork.getDefaultNetworkTable().getRow(selectedNetwork.getSUID()).get(clusters.getName(), Long.class));
        		// Delete all annotations
        		setHeatMapNoSort();
        		for (Cluster cluster : clusters.getClusterMap().values()) {
        			AutoAnnotationUtils.destroyCluster(cluster, autoAnnotationManager.getCommandExecutor(), autoAnnotationManager.getDialogTaskManager());
        		}
         		clusterSetDropdown.removeItem(clusterSetDropdown.getSelectedItem());
         		remove(clustersToTables.get(clusters).getParent());
         		clustersToTables.remove(clusters);
        	}
        };
        clearButton.addActionListener(clearActionListener); 
        
        updateButton = new JButton("Update Annotation Set");
        ActionListener updateActionListener = new ActionListener(){
        	public void actionPerformed(ActionEvent e) {
        		for (CyRow row : selectedNetwork.getDefaultNodeTable().getAllRows()) {
        			row.set(CyNetwork.SELECTED, false);
        		}
        		JComboBox clusterSetDropdown = networkViewToClusterSetDropdown.get(selectedView);
        		AnnotationSet clusters = (AnnotationSet) clusterSetDropdown.getSelectedItem(); 
        		clusters.updateCoordinates();
        		clusters.updateLabels();
         		for (Cluster cluster : clusters.getClusterMap().values()) {
         			cluster.erase();
         		}
				for (Cluster cluster : clusters.getClusterMap().values()) {
					AnnotationFactory<ShapeAnnotation> shapeFactory = autoAnnotationManager.getShapeFactory();
					AnnotationFactory<TextAnnotation> textFactory = autoAnnotationManager.getTextFactory();
					AnnotationManager annotationManager = autoAnnotationManager.getAnnotationManager();
					AutoAnnotationUtils.drawCluster(cluster, selectedView, shapeFactory, textFactory, annotationManager);
				}
        		// Update the table if the value has changed (WordCloud has been updated)
        		DefaultTableModel model = (DefaultTableModel) clustersToTables.get(clusters).getModel();
        		int i = 0;
        		for (Cluster cluster : clusters.getClusterMap().values()) {
        			if (!(model.getValueAt(i, 0).equals(cluster))) {
        				model.setValueAt(cluster, i, 0);
        			}
        			i++;
        		}
        	}
        };
        updateButton.addActionListener(updateActionListener); 
        
//        JButton mergeButton = new JButton("Merge Clusters");
//        ActionListener mergeActionListener = new ActionListener(){
//        	public void actionPerformed(ActionEvent e) {
//        		JComboBox clusterSetDropdown = networkViewToClusterSetDropdown.get(selectedView);
//        		AnnotationSet clusters = (AnnotationSet) clusterSetDropdown.getSelectedItem(); 
//        		JTable clusterTable = clustersToTables.get(clusters);
//        		int[] selectedRows = clusterTable.getSelectedRows();
//        		if (selectedRows.length < 2) {
//        			JOptionPane.showMessageDialog(null, "Please select at least two clusters");
//        		} else {
//        			ArrayList<Integer> selectedClusters = new ArrayList<Integer>();
//        			for (int rowNumber : selectedRows) {
//        				selectedClusters.add(rowNumber + 1);
//        			}
//	        		Cluster firstCluster = clusters.clusterSet.get(selectedClusters.get(0)); // +1 because it is zero indexed
//	        		for (int selectedClusterNumber : selectedClusters.subList(1, selectedClusters.size())) {
//	        			Cluster clusterToSwallow = clusters.clusterSet.get(selectedClusterNumber);
//	        			for (int nodeIndex = 0; nodeIndex < clusterToSwallow.getNodes().size(); nodeIndex++) {
//	        				selectedNetwork.getRow(clusterToSwallow.getNodes().get(nodeIndex)).set(clusters.clusterColumnName, firstCluster.clusterNumber);
//	        			}
//	        		}
//	        		clusters.destroyAnnotations();
//        		}
//        	}
//        };
//        mergeButton.addActionListener(mergeActionListener); 
        
        clusterTablePanel.setAlignmentX(LEFT_ALIGNMENT);
        nameColumnDropdown.setAlignmentX(LEFT_ALIGNMENT);
        advancedOptionsPanel.setAlignmentX(LEFT_ALIGNMENT);
        selectionPanel.setAlignmentX(LEFT_ALIGNMENT);
        
        mainPanel.add(networkLabel);
        mainPanel.add(nameColumnDropdownLabel);
        mainPanel.add(nameColumnDropdown);
        mainPanel.add(advancedOptionsPanel);
        mainPanel.add(annotateButton);
        mainPanel.add(clusterTablePanel);
        mainPanel.add(selectionPanel);
        mainPanel.add(clearButton);
        mainPanel.add(updateButton);
//        mainPanel.add(mergeButton);
        
        return mainPanel;
	}

	private BasicCollapsiblePanel createAdvancedOptionsPanel() {
		BasicCollapsiblePanel optionsPanel = new BasicCollapsiblePanel("Advanced Clustering Options");

		JPanel innerPanel = new JPanel(); // To override default layout options of BasicCollapsiblePanel
		innerPanel.setLayout(new BoxLayout(innerPanel, BoxLayout.PAGE_AXIS));
		
		JPanel clusterOptionPanel = new JPanel();
		clusterOptionPanel.setBorder(BorderFactory.createTitledBorder("Clustering Options"));
		
		// Dropdown with all the available algorithms
		DefaultComboBoxModel clusterDropdownModel = new DefaultComboBoxModel();
		for (String algorithm : autoAnnotationManager.getAlgorithmToColumnName().keySet()) {
			clusterDropdownModel.addElement(algorithm);
		}

		// To choose a clusterMaker algorithm
		clusterAlgorithmDropdown = new JComboBox(clusterDropdownModel);
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
        
        clusterOptionPanel.add(radioButtonPanel);
        clusterOptionPanel.add(dropdownPanel);

        optionsPanel.add(clusterOptionPanel);
        optionsPanel.setMaximumSize(new Dimension(350, optionsPanel.getHeight()));
        return optionsPanel;
	}
	
	private BasicCollapsiblePanel createSelectionPanel() {
		BasicCollapsiblePanel selectionPanel = new BasicCollapsiblePanel("Autofocus Preferences");
		
		JPanel innerPanel = new JPanel(); // To override default layout options of BasicCollapsiblePanel
		
		JPanel labelPanel = new JPanel();
		JLabel label = new JLabel("Show on selection:");
		labelPanel.add(label);
		
		JRadioButton heatmapButton = new JRadioButton("Heat Map");
		JRadioButton wordCloudButton = new JRadioButton("WordCloud");
		ButtonGroup buttonGroup = new ButtonGroup();
		buttonGroup.add(heatmapButton);
		buttonGroup.add(wordCloudButton);
		
		heatmapButton.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					showHeatmap = true;
					if (selectedView != null) setDisableHeatMapAutoFocus(false);
				} else if (e.getStateChange() == ItemEvent.DESELECTED) {
					showHeatmap = false;
					if (selectedView != null) setDisableHeatMapAutoFocus(true);
				}
			}
		});
		heatmapButton.setSelected(true);
		
		JPanel radioButtonPanel = new JPanel();
		radioButtonPanel.setLayout(new BoxLayout(radioButtonPanel, BoxLayout.PAGE_AXIS));
		radioButtonPanel.add(heatmapButton);
		radioButtonPanel.add(wordCloudButton);
		
		innerPanel.add(labelPanel);
		innerPanel.add(radioButtonPanel);
		
		selectionPanel.add(innerPanel);
		
		selectionPanel.setMaximumSize(new Dimension(370, selectionPanel.getHeight()));
		
		return selectionPanel;
	}
	
	private JTable createClusterTable(final AnnotationSet clusters) {
		DefaultTableModel model = new DefaultTableModel() {
			private static final long serialVersionUID = -1277709187563893042L;

			Class<?>[] types = {Cluster.class, Integer.class};

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return this.types[columnIndex];
            }
            
			@Override
		    public boolean isCellEditable(int row, int column) {
		        return column == 0 ? false : true;
		    }
		};
		model.addColumn("Cluster");
		model.addColumn("Number of nodes");

		final JTable table = new JTable(model); // Final to be able to use inside of listener
		table.getColumnModel().getColumn(0).setPreferredWidth(220);
		table.getColumnModel().getColumn(1).setPreferredWidth(100);
		
		model.addTableModelListener(new TableModelListener() { // Update the label value
			@Override
			public void tableChanged(TableModelEvent e) {
				if (e.getType() == TableModelEvent.UPDATE || e.getColumn() == 0) {
					int editedRowIndex = e.getFirstRow() == table.getSelectedRow()? e.getLastRow() : e.getFirstRow(); 
					Cluster editedCluster = clusters.getClusterMap().get(editedRowIndex + 1);
					editedCluster.setLabel((String) table.getValueAt(editedRowIndex, 0));
					editedCluster.setLabelManuallyUpdated(true);
					editedCluster.erase();
					AnnotationFactory<ShapeAnnotation> shapeFactory = autoAnnotationManager.getShapeFactory();
					AnnotationFactory<TextAnnotation> textFactory = autoAnnotationManager.getTextFactory();
					AnnotationManager annotationManager = autoAnnotationManager.getAnnotationManager();
					AutoAnnotationUtils.drawCluster(editedCluster, selectedView, shapeFactory, textFactory, annotationManager);
				}
			}
		});
		for (Cluster cluster : clusters.getClusterMap().values()) {
			Object[] rowData = {cluster, cluster.getCoordinates().size()};
			model.addRow(rowData);
		}
		table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (! e.getValueIsAdjusting()) { // Down-click and up-click are separate events, this makes only one of them fire
					DialogTaskManager dialogTaskManager = autoAnnotationManager.getDialogTaskManager();
					CommandExecutorTaskFactory executor = autoAnnotationManager.getCommandExecutor();
					if (!showHeatmap) {
						setHeatMapNoSort();
					}
					int selectedRowIndex = table.getSelectedRow();
					Cluster selectedCluster = params.getSelectedCluster();
					if (selectedCluster != null && (Cluster) table.getValueAt(selectedRowIndex, 0) != selectedCluster) {
						// Deselect the previously selected cluster - make annotations no longer yellow
						AutoAnnotationUtils.deselectCluster(selectedCluster, selectedNetwork);
					}
					// Select the clicked on cluster
					selectedCluster = (Cluster) table.getValueAt(selectedRowIndex, 0);
					params.setSelectedCluster(selectedCluster);
					AutoAnnotationUtils.selectCluster(selectedCluster, selectedNetwork, showHeatmap, executor, dialogTaskManager);
				}
			}
		});
		table.setAutoCreateRowSorter(true);
		return table;
	}

	public void addClusters(AnnotationSet annotationSet) {
		// If this is the view's first AnnotationSet
		CyNetworkView clusterView = annotationSet.getView();
		if (!networkViewToClusterSetDropdown.containsKey(clusterView)) {
			addNetworkView(clusterView);
		}
		
		// Create scrollable clusterTable
		JTable clusterTable = createClusterTable(annotationSet);
		JScrollPane clusterTableScroll = new JScrollPane(clusterTable);
		clusterTableScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		clusterTablePanel.add(clusterTableScroll, BorderLayout.CENTER);
		
		clustersToTables.put(annotationSet, clusterTable);
		
		JComboBox clusterSetDropdown = networkViewToClusterSetDropdown.get(clusterView);
		clusterSetDropdown.addItem(annotationSet);
		params.addAnnotationSet(annotationSet);
		clusterSetDropdown.setSelectedIndex(clusterSetDropdown.getItemCount()-1);
	}
	
	private void addNetworkView(CyNetworkView view) {
		// Create dropdown with cluster sets of this networkView
		JComboBox clusterSetDropdown = new JComboBox();
		clusterSetDropdown.addItemListener(new ItemListener(){
			public void itemStateChanged(ItemEvent itemEvent) {
				if (itemEvent.getStateChange() == ItemEvent.SELECTED) {
					AnnotationSet clusters = (AnnotationSet) itemEvent.getItem();
					clusters.updateCoordinates();
					clusters.updateLabels();
					// Redraw selected clusters
					for (Cluster cluster : clusters.getClusterMap().values()) {
						AnnotationFactory<ShapeAnnotation> shapeFactory = autoAnnotationManager.getShapeFactory();
						AnnotationFactory<TextAnnotation> textFactory = autoAnnotationManager.getTextFactory();
						AnnotationManager annotationManager = autoAnnotationManager.getAnnotationManager();
						AutoAnnotationUtils.drawCluster(cluster, selectedView, shapeFactory, textFactory, annotationManager);
					}
					clustersToTables.get(clusters).getParent().getParent().setVisible(true); // Show selected table
					clusterTablePanel.updateUI();
				} else if (itemEvent.getStateChange() == ItemEvent.DESELECTED) {
					// Hide unselected clusters
					AnnotationSet clusters = (AnnotationSet) itemEvent.getItem();
	         		for (Cluster cluster : clusters.getClusterMap().values()) {
	         			cluster.erase();
	         		}
					clustersToTables.get(clusters).getParent().getParent().setVisible(false);
					clusterTablePanel.updateUI();
				}
            }
		});
		clusterTablePanel.add(clusterSetDropdown, BorderLayout.PAGE_START);
		networkViewToClusterSetDropdown.put(view, clusterSetDropdown);
		
		selectedView = view;
		selectedNetwork = view.getModel();
		params = autoAnnotationManager.getNetworkViewToAutoAnnotationParameters().get(view.getSUID());
		
		networkLabel.setText(selectedNetwork.toString());
		clusterTablePanel.updateUI();
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
		
		// Try to guess the appropriate columns
		for (int i = 0; i < nameColumnDropdown.getItemCount(); i++) {
			if (nameColumnDropdown.getItemAt(i).getClass() == String.class) {
				if (((String) nameColumnDropdown.getItemAt(i)).contains("GS_DESCR")) {
					nameColumnDropdown.setSelectedIndex(i);
				}
			}
		}
		
		// Update the label with the network
		networkLabel.setText("  " + view.getModel().toString());
		mainPanel.updateUI();
		
		// Hide previous dropdown
		if (networkViewToClusterSetDropdown.containsKey(selectedView)) {
			networkViewToClusterSetDropdown.get(selectedView).setVisible(false);
			clustersToTables.get(networkViewToClusterSetDropdown.get(selectedView).getSelectedItem()).getParent().getParent().setVisible(false);
		}
		
		selectedView = view;
		selectedNetwork = view.getModel();
		if (autoAnnotationManager.getNetworkViewToAutoAnnotationParameters().containsKey(view.getSUID())) {
			params = autoAnnotationManager.getNetworkViewToAutoAnnotationParameters().get(view.getSUID());			
			clearButton.setVisible(true);
			updateButton.setVisible(true);
			selectionPanel.setVisible(true);
		} else {
			clearButton.setVisible(false);
			updateButton.setVisible(false);
			selectionPanel.setVisible(false);
		}
		
		// Show current dropdown
		if (networkViewToClusterSetDropdown.containsKey(selectedView)) {
			networkViewToClusterSetDropdown.get(selectedView).setVisible(true);
			clustersToTables.get(networkViewToClusterSetDropdown.get(selectedView).getSelectedItem()).getParent().getParent().setVisible(true);
		}
	}

	public void updateColumnName(CyTable source, String oldColumnName,
			String newColumnName) {
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
		networkLabel.setText("No network selected");
		nameColumnDropdown.removeAllItems();
		clusterColumnDropdown.removeAllItems();
		mainPanel.updateUI();
		if (networkViewToClusterSetDropdown.containsKey(view)) {
			JComboBox clusterSetDropdown = networkViewToClusterSetDropdown.get(view);
			Container clusterTable = clustersToTables.get(networkViewToClusterSetDropdown.get(view).getSelectedItem()).getParent().getParent();
			clusterSetDropdown.getParent().remove(clusterSetDropdown);
			clusterTable.getParent().remove(clusterTable);
		}
		selectedView = null;
		selectedNetwork = null;
		params = null;
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
				if (((DefaultComboBoxModel) nameColumnDropdown.getModel()).getIndexOf(column) == -1) { // doesn't already contain column
					nameColumnDropdown.addItem(column.getName());
				}
			} else if (column.getType() == Integer.class || (column.getType() == List.class && column.getListElementType() == Integer.class)) {
				if (((DefaultComboBoxModel) clusterColumnDropdown.getModel()).getIndexOf(column) == -1) { // doesn't already contain column
					clusterColumnDropdown.addItem(column.getName());					
				}
			}
		}
	}
	
	public void setHeatMapNoSort() {
		emManager.getMap(selectedNetwork.getSUID()).getParams().getHmParams().setSort(HeatMapParameters.Sort.NONE);
	}
	
	public void setDisableHeatMapAutoFocus(boolean b) {
		emManager.getMap(selectedNetwork.getSUID()).getParams().setDisableHeatmapAutofocus(b);
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

}