package org.baderlab.csplugins.enrichmentmap.autoannotate.view;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.net.URL;
import java.util.ArrayList;
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
import javax.swing.JTextField;
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
import org.cytoscape.application.swing.CytoPanel;
import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.command.CommandExecutorTaskFactory;
import org.cytoscape.group.CyGroupManager;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.util.swing.BasicCollapsiblePanel;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.presentation.annotations.AnnotationFactory;
import org.cytoscape.view.presentation.annotations.AnnotationManager;
import org.cytoscape.view.presentation.annotations.ShapeAnnotation;
import org.cytoscape.view.presentation.annotations.TextAnnotation;
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
	
	private static final String proportionalSizeButtonString = "Font size by # of nodes";
	private static final String constantSizeButtonString = "Constant font size";

	// Dropdown menus
	private JComboBox nameColumnDropdown;
	private JComboBox clusterColumnDropdown;
	private JComboBox clusterAlgorithmDropdown;

	// Radio buttons to choose between clusterMaker defaults and a cluster column
	private ButtonGroup clusterButtonGroup;
	private JRadioButton defaultButton;
	private JRadioButton specifyColumnButton;

	// Label that shows the name of the selected network (updates)
	private JLabel networkLabel;
	// Used to update panel on network selection
	private HashMap<CyNetworkView, JComboBox> networkViewToClusterSetDropdown;
	// Used to update table on annotation set selection
	private HashMap<AnnotationSet, JTable> clustersToTables;
	// Toggle of what to show on selection
	private boolean showHeatmap;
	// Used to store the output table
	private JPanel outputPanel;
	// Reference needed to hide when not needed
	private JPanel bottomButtonPanel;
	// Reference needed to click on it for merges
	private JButton removeButton;
	
	private CyNetworkView selectedView;
	private CyNetwork selectedNetwork;

	private EnrichmentMapManager emManager;
	private AutoAnnotationManager autoAnnotationManager;
	private AutoAnnotationParameters params;

	private BasicCollapsiblePanel advancedOptionsPanel;
	private BasicCollapsiblePanel selectionPanel;

	private JCheckBox showEllipsesCheckBox;
	private JCheckBox layoutCheckBox;
	private JCheckBox groupsCheckBox;

	// Used to specify the font size
	private JTextField fontSizeTextField;

	private CySwingApplication application;

	private JButton updateButton;
	

	public AutoAnnotationPanel(CySwingApplication application){
		this.clustersToTables = new HashMap<AnnotationSet, JTable>();
		this.networkViewToClusterSetDropdown = new HashMap<CyNetworkView, JComboBox>();
		
		this.emManager = EnrichmentMapManager.getInstance();
		this.autoAnnotationManager = AutoAnnotationManager.getInstance();

		this.application = application;
		
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
		
		networkLabel = new JLabel("No network selected");
		Font font = networkLabel.getFont();
		networkLabel.setFont(new Font(font.getFamily(), font.getStyle(), 18));

		JLabel nameColumnDropdownLabel = new JLabel("   Select the column with the gene set descriptions:");
		// Gives the user a choice of column with gene names
		nameColumnDropdown = new JComboBox();
		// Collapsible panel with advanced cluster options
		advancedOptionsPanel = createAdvancedOptionsPanel(); 
		
		// Run the annotation
		JButton annotateButton = new JButton("Annotate!");
		ActionListener annotateAction = new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				if (selectedView == null) {
					JOptionPane.showMessageDialog(null, "Load an Enrichment Map", "Error Message", JOptionPane.ERROR_MESSAGE);
				} else {
					// Get the params for this network
					if (autoAnnotationManager.getNetworkViewToAutoAnnotationParameters().containsKey(selectedView)) {
						// Not the first annotation set for this network view, lookup
						params = autoAnnotationManager.getNetworkViewToAutoAnnotationParameters().get(selectedView);
					} else {
						// First annotation set for the view, make/register the new network view parameters
						params = new AutoAnnotationParameters();
						params.setNetworkView(selectedView);
						autoAnnotationManager.getNetworkViewToAutoAnnotationParameters().put(selectedView, params);
					}
					String clusterColumnName = null;
					String algorithm = null;
					if (defaultButton.isSelected()) {
						// using clusterMaker algorithms
						algorithm = (String) clusterAlgorithmDropdown.getSelectedItem();
						clusterColumnName = params.nextClusterColumnName(
								autoAnnotationManager.getAlgorithmToColumnName().get(algorithm), 
								selectedNetwork.getDefaultNodeTable());
					} else if (specifyColumnButton.isSelected()) {
						// using a user specified column
						clusterColumnName = (String) clusterColumnDropdown.getSelectedItem();
					}
					String nameColumnName = (String) nameColumnDropdown.getSelectedItem();
					String annotationSetName = params.nextAnnotationSetName(algorithm, clusterColumnName);
					AutoAnnotationTaskFactory autoAnnotatorTaskFactory = new AutoAnnotationTaskFactory(application, 
							autoAnnotationManager, selectedView, clusterColumnName, nameColumnName, algorithm, 
							layoutCheckBox.isSelected(), groupsCheckBox.isSelected(), annotationSetName);
					advancedOptionsPanel.setCollapsed(true);
					autoAnnotationManager.getDialogTaskManager().execute(autoAnnotatorTaskFactory.createTaskIterator());
					// Increment the counter used to name the annotation sets
					setOutputVisibility(true);
				}
			}
		};
		annotateButton.addActionListener(annotateAction);

		inputPanel.add(networkLabel);
		inputPanel.add(nameColumnDropdownLabel);
		inputPanel.add(nameColumnDropdown);
		inputPanel.add(advancedOptionsPanel);
		inputPanel.add(annotateButton);
		
		networkLabel.setAlignmentX(LEFT_ALIGNMENT);
		nameColumnDropdownLabel.setAlignmentX(LEFT_ALIGNMENT);
		nameColumnDropdown.setAlignmentX(LEFT_ALIGNMENT);
		advancedOptionsPanel.setAlignmentX(LEFT_ALIGNMENT);
		return inputPanel;
	}
	
	private JPanel createOutputPanel() {
		JPanel outputPanel = new JPanel(new BorderLayout());
		
		selectionPanel = createSelectionPanel();
		
		JButton mergeButton = new JButton("Merge Clusters");
		ActionListener mergeActionListener = new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				JComboBox clusterSetDropdown = networkViewToClusterSetDropdown.get(selectedView);
				AnnotationSet annotationSet = (AnnotationSet) clusterSetDropdown.getSelectedItem(); 
				JTable clusterTable = clustersToTables.get(annotationSet);
				int[] selectedRows = clusterTable.getSelectedRows();
				if (selectedRows.length < 2) {
					JOptionPane.showMessageDialog(null, "Please select at least two clusters", "Error Message",
		                    JOptionPane.ERROR_MESSAGE);
				} else {
					ArrayList<Integer> selectedClusters = new ArrayList<Integer>();
					for (int rowNumber : selectedRows) {
						selectedClusters.add(rowNumber + 1);
					}
					Cluster firstCluster = annotationSet.getClusterMap().get(selectedClusters.get(0));
					for (int selectedClusterNumber : selectedClusters.subList(1, selectedClusters.size())) {
						Cluster clusterToSwallow = annotationSet.getClusterMap().get(selectedClusterNumber);
						for (int nodeIndex = 0; nodeIndex < clusterToSwallow.getNodes().size(); nodeIndex++) {
							selectedNetwork.getRow(clusterToSwallow.getNodes().get(nodeIndex)).set(annotationSet.getClusterColumnName(), firstCluster.getClusterNumber());
						}
					}
					String annotationSetName = annotationSet.getCloudNamePrefix();
					String clusterColumnName = annotationSet.getClusterColumnName();
					String nameColumnName = annotationSet.getNameColumnName();
					removeButton.doClick();
					AutoAnnotationTaskFactory autoAnnotatorTaskFactory = new AutoAnnotationTaskFactory(application, 
							autoAnnotationManager, selectedView, clusterColumnName, nameColumnName, null, 
							false, false, annotationSetName);
					advancedOptionsPanel.setCollapsed(true);
					autoAnnotationManager.getDialogTaskManager().execute(autoAnnotatorTaskFactory.createTaskIterator());
				}
			}
		};
		mergeButton.addActionListener(mergeActionListener);
		
		JPanel outputBottomPanel = new JPanel();
		outputBottomPanel.setLayout(new BoxLayout(outputBottomPanel, BoxLayout.PAGE_AXIS));
		
		outputBottomPanel.add(mergeButton);
		outputBottomPanel.add(selectionPanel);
		
		mergeButton.setAlignmentX(CENTER_ALIGNMENT);
		
		outputPanel.add(outputBottomPanel, BorderLayout.SOUTH);
		
		return outputPanel;
	}
	
	private JPanel createBottomButtonPanel() {
		JPanel bottomButtonPanel = new JPanel();
		
		// Button to get remove an annotation set
		removeButton = new JButton("Remove");
		ActionListener clearActionListener = new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				JComboBox clusterSetDropdown = networkViewToClusterSetDropdown.get(selectedView);
				AnnotationSet clusters = (AnnotationSet) clusterSetDropdown.getSelectedItem();
				// Delete wordCloud table
				autoAnnotationManager.getTableManager().deleteTable(selectedNetwork.getDefaultNetworkTable().getRow(selectedNetwork.getSUID()).get(clusters.getCloudNamePrefix(), Long.class));
				// Prevent heatmap dialog from interfering
				setHeatMapNoSort();
				// Delete all annotations
				for (Cluster cluster : clusters.getClusterMap().values()) {
					AutoAnnotationUtils.destroyCluster(cluster, autoAnnotationManager.getCommandExecutor(), autoAnnotationManager.getSyncTaskManager());
				}
				clusterSetDropdown.removeItem(clusterSetDropdown.getSelectedItem());
				// Get rid of the table associated with this cluster set
				remove(clustersToTables.get(clusters).getParent());
				clustersToTables.remove(clusters);
				params.removeAnnotationSet(clusters);
				// Hide the unuseable buttons if the last annotation set was just deleted
				if (params.getAnnotationSets().size() == 0) {
					setOutputVisibility(false);
				}
			}
		};
		removeButton.addActionListener(clearActionListener); 

		// Button to update the current cluster set
		updateButton = new JButton("Update");
		ActionListener updateActionListener = new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				for (CyRow row : selectedNetwork.getDefaultNodeTable().getAllRows()) {
					row.set(CyNetwork.SELECTED, false);
				}
				JComboBox clusterSetDropdown = networkViewToClusterSetDropdown.get(selectedView);
				AnnotationSet annotationSet = (AnnotationSet) clusterSetDropdown.getSelectedItem(); 
				annotationSet.updateCoordinates();
				String annotationSetName = annotationSet.getCloudNamePrefix();
				Long clusterTableSUID = selectedNetwork.getDefaultNetworkTable().getRow(selectedNetwork.getSUID()).get(annotationSetName, Long.class);
				CyTable clusterSetTable = autoAnnotationManager.getTableManager().getTable(clusterTableSUID);
				for (Cluster cluster : annotationSet.getClusterMap().values()) {
					// Update the text label of the selected cluster
					String previousLabel = cluster.getLabel();
					String nameColumnName = (String) nameColumnDropdown.getSelectedItem();
					AutoAnnotationUtils.updateClusterLabel(cluster, selectedNetwork, annotationSetName, clusterSetTable, nameColumnName);
					if (previousLabel != cluster.getLabel()) {
						// Cluster table needs to be updated with new label
						clustersToTables.get(annotationSet).updateUI();
					}
					cluster.erase();
					AnnotationFactory<ShapeAnnotation> shapeFactory = autoAnnotationManager.getShapeFactory();
					AnnotationFactory<TextAnnotation> textFactory = autoAnnotationManager.getTextFactory();
					AnnotationManager annotationManager = autoAnnotationManager.getAnnotationManager();
					boolean constantFontSize = fontSizeTextField.isEnabled();
					int fontSize = Integer.parseInt(fontSizeTextField.getText());
					AutoAnnotationUtils.drawCluster(cluster, selectedView, shapeFactory, textFactory, annotationManager, constantFontSize, fontSize, showEllipsesCheckBox.isSelected());
				}
				// Update the table if the value has changed (WordCloud has been updated)
				DefaultTableModel model = (DefaultTableModel) clustersToTables.get(annotationSet).getModel();
				int i = 0;
				for (Cluster cluster : annotationSet.getClusterMap().values()) {
					if (!(model.getValueAt(i, 0).equals(cluster))) {
						model.setValueAt(cluster, i, 0);
					}
					i++;
				}
			}
		};
		updateButton.addActionListener(updateActionListener);

		bottomButtonPanel = new JPanel();
		bottomButtonPanel.add(removeButton);
		bottomButtonPanel.add(updateButton);
		
		return bottomButtonPanel;
	}
	
	private BasicCollapsiblePanel createAdvancedOptionsPanel() {
		BasicCollapsiblePanel optionsPanel = new BasicCollapsiblePanel("Advanced Options");
		
		JPanel innerPanel = new JPanel(new BorderLayout());
		
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

		// Group buttons together to make them mutually exclusive
		clusterButtonGroup = new ButtonGroup();
		clusterButtonGroup.add(defaultButton);
		clusterButtonGroup.add(specifyColumnButton);

		JPanel clusterButtonPanel = new JPanel();
		clusterButtonPanel.setLayout(new BoxLayout(clusterButtonPanel, BoxLayout.PAGE_AXIS));
		clusterButtonPanel.add(defaultButton);
		clusterButtonPanel.add(specifyColumnButton);
		
		// Dropdown with all the available algorithms
		DefaultComboBoxModel clusterDropdownModel = new DefaultComboBoxModel();
		for (String algorithm : autoAnnotationManager.getAlgorithmToColumnName().keySet()) {
			clusterDropdownModel.addElement(algorithm);
		}

		// To choose a clusterMaker algorithm
		clusterAlgorithmDropdown = new JComboBox(clusterDropdownModel);
		clusterAlgorithmDropdown.setPreferredSize(new Dimension(135, 30));
		// Alternatively, user can choose a cluster column themselves (if they've run clusterMaker themselves)
		clusterColumnDropdown = new JComboBox();
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
		
		// Font size options
		JRadioButton proportionalSizeButton = new JRadioButton(proportionalSizeButtonString);
		proportionalSizeButton.setSelected(true);
		JRadioButton constantSizeButton = new JRadioButton(constantSizeButtonString);
		fontSizeTextField = new JTextField();
		fontSizeTextField.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent e) {
		    	try {
		    		int fontSize = Integer.parseInt(fontSizeTextField.getText());
		    		if (fontSize <= 0) {
		    			throw new Exception();
		    		}
		    		AutoAnnotationUtils.updateFontSizes(fontSize, showEllipsesCheckBox.isSelected());
		    	} catch (Exception ex) {
		            JOptionPane.showMessageDialog(null,
		                    "Error: Please enter an integer bigger than 0", "Error Message",
		                    JOptionPane.ERROR_MESSAGE);
		    	}
		    }
		});
		fontSizeTextField.setText("12");
		// Initially hidden
		fontSizeTextField.setEnabled(false);
		constantSizeButton.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					fontSizeTextField.setEnabled(true);
			    	int fontSize = Integer.parseInt(fontSizeTextField.getText());
					AutoAnnotationUtils.updateFontSizes(fontSize, showEllipsesCheckBox.isSelected());
					updateUI();
				} else if (e.getStateChange() == ItemEvent.DESELECTED) {
					fontSizeTextField.setEnabled(false);
					AutoAnnotationUtils.updateFontSizes(null, showEllipsesCheckBox.isSelected());
					updateUI();
				}
			}
		});

		// Group buttons together to make them mutually exclusive
		ButtonGroup fontButtonGroup = new ButtonGroup();
		fontButtonGroup.add(proportionalSizeButton);
		fontButtonGroup.add(constantSizeButton);

		JPanel fontSizePanel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		fontSizePanel.setBorder(BorderFactory.createTitledBorder("Font Size Options"));
		c.gridx = 0;
		c.gridwidth = 5;
		c.gridy = 0;
		fontSizePanel.add(proportionalSizeButton,c);
		c.gridx = 0;
		c.gridwidth = 4;
		c.gridy = 1;
		fontSizePanel.add(constantSizeButton,c);
		c.gridx = 10;
		c.gridwidth = 1;
		c.gridy = 1;
		fontSizePanel.add(fontSizeTextField,c);
		
		// By default show ellipses around clusters
		showEllipsesCheckBox = new JCheckBox("Show ellipses");
		showEllipsesCheckBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				updateButton.doClick();
			}
		});
		showEllipsesCheckBox.setSelected(true);
		
		// By default layout nodes by cluster
		layoutCheckBox = new JCheckBox("Layout nodes by cluster");
		layoutCheckBox.setSelected(false);

		// By default layout nodes by cluster
		groupsCheckBox = new JCheckBox("Create groups (metanodes) for clusters *BUGGY*");
		groupsCheckBox.setSelected(false);
		
		JPanel checkBoxPanel = new JPanel();
		checkBoxPanel.setLayout(new BoxLayout(checkBoxPanel, BoxLayout.Y_AXIS));
		checkBoxPanel.add(showEllipsesCheckBox);
		checkBoxPanel.add(layoutCheckBox);
		checkBoxPanel.add(groupsCheckBox);
		
		JPanel nonClusterOptionPanel = new JPanel(new BorderLayout());
		nonClusterOptionPanel.add(fontSizePanel, BorderLayout.NORTH);
		nonClusterOptionPanel.add(checkBoxPanel, BorderLayout.SOUTH);
		
		JPanel clusterOptionPanel = new JPanel(new BorderLayout());
		clusterOptionPanel.setBorder(BorderFactory.createTitledBorder("ClusterMaker Options"));
		clusterOptionPanel.add(clusterButtonPanel, BorderLayout.WEST);
		clusterOptionPanel.add(dropdownPanel, BorderLayout.EAST);
		
		innerPanel.add(clusterOptionPanel, BorderLayout.NORTH);
		innerPanel.add(nonClusterOptionPanel, BorderLayout.SOUTH);
		
		optionsPanel.add(innerPanel);
		
		return optionsPanel;
	}

	private BasicCollapsiblePanel createSelectionPanel() {
		BasicCollapsiblePanel selectionPanel = new BasicCollapsiblePanel("Autofocus Preferences");

		JPanel innerPanel = new JPanel(new BorderLayout()); // To override default layout options of BasicCollapsiblePanel

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

		innerPanel.add(labelPanel, BorderLayout.WEST);
		innerPanel.add(radioButtonPanel, BorderLayout.EAST);

		selectionPanel.add(innerPanel);
		// Initially set uncollapsed so the user knows about it
		selectionPanel.setCollapsed(false);
		
		return selectionPanel;
	}

	private JTable createClusterTable(final AnnotationSet clusters) {
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

		model.addTableModelListener(new TableModelListener() { // Update the label value
			@Override
			public void tableChanged(TableModelEvent e) {
				if (e.getType() == TableModelEvent.UPDATE || e.getColumn() == 0) {
					int editedRowIndex = e.getFirstRow() == table.getSelectedRow()? e.getLastRow() : e.getFirstRow(); 
					Cluster editedCluster = clusters.getClusterMap().get(editedRowIndex + 1);
					try {
						editedCluster.setLabel((String) table.getValueAt(editedRowIndex, 0));
					} catch (Exception ex) {
						// This comes from event fired from re-adding the cluster to the table, ignore
						return;
					}
					table.setValueAt(editedCluster, editedRowIndex, 0); // Otherwise String stays in the table
					editedCluster.erase();
					AnnotationFactory<ShapeAnnotation> shapeFactory = autoAnnotationManager.getShapeFactory();
					AnnotationFactory<TextAnnotation> textFactory = autoAnnotationManager.getTextFactory();
					AnnotationManager annotationManager = autoAnnotationManager.getAnnotationManager();
					boolean constantFontSize = fontSizeTextField.isEnabled();
					int fontSize = Integer.parseInt(fontSizeTextField.getText());
					AutoAnnotationUtils.drawCluster(editedCluster, selectedView, shapeFactory, textFactory, 
							annotationManager, constantFontSize, fontSize, showEllipsesCheckBox.isSelected());
				}
			}
		});
		for (Cluster cluster : clusters.getClusterMap().values()) {
			// Each row contains the cluster (printed as a string, its label), and how many nodes it contains 
			Object[] rowData = {cluster, cluster.getSize()};
			model.addRow(rowData);
		}
		table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (! e.getValueIsAdjusting()) { // Down-click and up-click are separate events, this makes only one of them fire
					SynchronousTaskManager<?> syncTaskManager = autoAnnotationManager.getSyncTaskManager();
					CommandExecutorTaskFactory executor = autoAnnotationManager.getCommandExecutor();
					int[] selectedRows = table.getSelectedRows();
					ArrayList<Cluster> selectedClusters = new ArrayList<Cluster>();
					for (int rowIndex=0; rowIndex < table.getRowCount(); rowIndex++) {
						boolean deselect = true;
						Cluster cluster = (Cluster) table.getModel().getValueAt(table.convertRowIndexToModel(rowIndex), 0);
						for (int selectedRow : selectedRows) {
							if (rowIndex == selectedRow) {
								// Selects the row and exits (to avoid deselecting it after)
								selectedClusters.add(cluster);
								deselect = false;
								break;
							}
						}
						if (deselect) {
							AutoAnnotationUtils.deselectCluster(cluster, selectedNetwork);
						}
					}
					for (Cluster cluster : selectedClusters) {
						AutoAnnotationUtils.selectCluster(cluster, selectedNetwork, executor, syncTaskManager);
					}
					for (CyRow row: selectedNetwork.getDefaultNodeTable().getAllRows()) {
						row.set(CyNetwork.SELECTED, false);
					}
					for (Cluster cluster : selectedClusters) {
						for (CyNode node : cluster.getNodes()) {
							selectedNetwork.getRow(node).set(CyNetwork.SELECTED, true);
						}
					}
					
					// Selects the heatmap panel (if user tells it to)
					if (showHeatmap) {
						CytoPanel southPanel = application.getCytoPanel(CytoPanelName.SOUTH);
						southPanel.setSelectedIndex(southPanel.indexOfComponent(autoAnnotationManager.getHeatmapPanel()));
					}
				}
			}
		});
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
		outputPanel.add(clusterTableScroll, BorderLayout.CENTER);
		clustersToTables.put(annotationSet, clusterTable);
		// Add the annotation set to the dropdown
		JComboBox clusterSetDropdown = networkViewToClusterSetDropdown.get(clusterView);
		clusterSetDropdown.addItem(annotationSet);
		// Select the most recently added annotation set
		clusterSetDropdown.setSelectedIndex(clusterSetDropdown.getItemCount()-1);
	}

	private void addNetworkView(CyNetworkView view) {
		// Create dropdown with cluster sets of this networkView
		JComboBox clusterSetDropdown = new JComboBox();
		clusterSetDropdown.addItemListener(new ItemListener(){
			public void itemStateChanged(ItemEvent itemEvent) {
				CyGroupManager groupManager = autoAnnotationManager.getGroupManager();
				if (itemEvent.getStateChange() == ItemEvent.SELECTED) {
					AnnotationSet annotationSet = (AnnotationSet) itemEvent.getItem();
					params.setSelectedAnnotationSet(annotationSet);
					// Update the selected annotation set
					annotationSet.updateCoordinates();
					String annotationSetName = annotationSet.getCloudNamePrefix();
					// Get the table where WordCloud results are stored
					Long clusterTableSUID = selectedNetwork.getDefaultNetworkTable().getRow(selectedNetwork.getSUID()).get(annotationSetName, Long.class);
					CyTable clusterSetTable = autoAnnotationManager.getTableManager().getTable(clusterTableSUID);
					for (Cluster cluster : annotationSet.getClusterMap().values()) {
						// Register the groups for this cluster
						AutoAnnotationUtils.registerClusterGroups(cluster, selectedNetwork, groupManager);
						// Update the text label of the selected cluster
						String nameColumnName = (String) nameColumnDropdown.getSelectedItem();
						AutoAnnotationUtils.updateClusterLabel(cluster, selectedNetwork, annotationSetName, clusterSetTable, nameColumnName);
						// Redraw selected clusters
						AnnotationFactory<ShapeAnnotation> shapeFactory = autoAnnotationManager.getShapeFactory();
						AnnotationFactory<TextAnnotation> textFactory = autoAnnotationManager.getTextFactory();
						AnnotationManager annotationManager = autoAnnotationManager.getAnnotationManager();
						boolean constantFontSize = fontSizeTextField.isEnabled();
						int fontSize = Integer.parseInt(fontSizeTextField.getText());
						AutoAnnotationUtils.drawCluster(cluster, selectedView, shapeFactory, textFactory, annotationManager, constantFontSize, fontSize, showEllipsesCheckBox.isSelected());
					}
					clustersToTables.get(annotationSet).getParent().getParent().setVisible(true); // Show selected table
					updateUI();
				} else if (itemEvent.getStateChange() == ItemEvent.DESELECTED) {
					AnnotationSet clusters = (AnnotationSet) itemEvent.getItem();
					for (Cluster cluster : clusters.getClusterMap().values()) {
						// Un-register the groups for deselected clusters
						AutoAnnotationUtils.unregisterClusterGroups(cluster, selectedNetwork, groupManager);
						// Hide the annotations
						cluster.erase();
					}
					// Hide the table for deselected clusters
					clustersToTables.get(clusters).getParent().getParent().setVisible(false);
					updateUI();
				}
			}
		});
		outputPanel.add(clusterSetDropdown, BorderLayout.NORTH);
		networkViewToClusterSetDropdown.put(view, clusterSetDropdown);

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
			JComboBox currentDropdown = networkViewToClusterSetDropdown.get(selectedView);
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
		networkLabel.setText("No network selected");
		nameColumnDropdown.removeAllItems();
		clusterColumnDropdown.removeAllItems();
		updateUI();
		if (networkViewToClusterSetDropdown.containsKey(view)) {
			JComboBox clusterSetDropdown = networkViewToClusterSetDropdown.get(view);
			Container clusterTable = clustersToTables.get(networkViewToClusterSetDropdown.get(view).getSelectedItem()).getParent().getParent();
			clusterSetDropdown.getParent().remove(clusterSetDropdown);
			clusterTable.getParent().remove(clusterTable);
			networkViewToClusterSetDropdown.remove(view);
		}
		for (AnnotationSet annotationSet : params.getAnnotationSets().values()) {
			for (Cluster cluster : annotationSet.getClusterMap().values()) {
				// Bug with loading groups in session, need to be destroyed then recreated when loading
				cluster.destroyGroup();
			}
		}
		
		selectedView = null;
		selectedNetwork = null;
		params = null;
		
		setOutputVisibility(false);
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
		HeatMapParameters heatMapParameters = emManager.getMap(selectedNetwork.getSUID()).getParams().getHmParams();
		if (heatMapParameters != null) {
			heatMapParameters.setSort(HeatMapParameters.Sort.NONE);
		}
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