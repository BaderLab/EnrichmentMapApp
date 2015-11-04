package org.baderlab.csplugins.enrichmentmap.autoannotate;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.swing.ListSelectionModel;
import javax.swing.table.TableModel;

import org.baderlab.csplugins.enrichmentmap.actions.EnrichmentMapActionListener;
import org.baderlab.csplugins.enrichmentmap.autoannotate.action.DisplayOptionsPanelAction;
import org.baderlab.csplugins.enrichmentmap.autoannotate.model.AnnotationSet;
import org.baderlab.csplugins.enrichmentmap.autoannotate.model.Cluster;
import org.baderlab.csplugins.enrichmentmap.autoannotate.view.AutoAnnotationPanel;
import org.baderlab.csplugins.enrichmentmap.autoannotate.view.DisplayOptionsPanel;
import org.baderlab.csplugins.enrichmentmap.view.HeatMapPanel;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.events.SetSelectedNetworkViewsEvent;
import org.cytoscape.application.events.SetSelectedNetworkViewsListener;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.application.swing.CytoPanel;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.command.CommandExecutorTaskFactory;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.group.CyGroupFactory;
import org.cytoscape.group.CyGroupManager;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.model.CyTableUtil;
import org.cytoscape.model.events.ColumnCreatedEvent;
import org.cytoscape.model.events.ColumnCreatedListener;
import org.cytoscape.model.events.ColumnDeletedEvent;
import org.cytoscape.model.events.ColumnDeletedListener;
import org.cytoscape.model.events.ColumnNameChangedEvent;
import org.cytoscape.model.events.ColumnNameChangedListener;
import org.cytoscape.model.events.RowsSetEvent;
import org.cytoscape.model.events.RowsSetListener;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.events.NetworkViewAboutToBeDestroyedEvent;
import org.cytoscape.view.model.events.NetworkViewAboutToBeDestroyedListener;
import org.cytoscape.view.presentation.annotations.AnnotationFactory;
import org.cytoscape.view.presentation.annotations.AnnotationManager;
import org.cytoscape.view.presentation.annotations.ShapeAnnotation;
import org.cytoscape.view.presentation.annotations.TextAnnotation;
import org.cytoscape.work.SynchronousTaskManager;
import org.cytoscape.work.swing.DialogTaskManager;
/**
 * Created by
 * User: arkadyark
 * Date: July 9, 2014
 * Time: 9:46 AM
 */

public class AutoAnnotationManager implements
		SetSelectedNetworkViewsListener, ColumnCreatedListener, 
		ColumnDeletedListener, ColumnNameChangedListener,
		NetworkViewAboutToBeDestroyedListener, RowsSetListener {
	
	// instance variable used to get the manager from other parts of the program
	private static AutoAnnotationManager instance = null;
	// used to control selected panels
	private CySwingApplication application;
	// reference to the panel that the user interacts with
	private AutoAnnotationPanel annotationPanel;
	// reference to the action to create the displayOptionsPanel
	private DisplayOptionsPanelAction displayOptionsPanelAction;
	// stores the annotation parameters (one for each network view)
	private HashMap<CyNetworkView, AutoAnnotationParameters> networkViewToAutoAnnotationParameters;
	// used to set clusterMaker default parameters
	public static final SortedMap<String, String> algorithmToColumnName;
	static {
		TreeMap<String, String> aMap = new TreeMap<String, String>();		
		aMap.put("Affinity Propagation Cluster", "__APCluster");
		aMap.put("Cluster Fuzzifier", "__fuzzifierCluster");
		aMap.put("Community cluster (GLay)", "__glayCluster");
		aMap.put("ConnectedComponents Cluster", "__ccCluster");
		aMap.put("Fuzzy C-Means Cluster", "__fcmlCluster");
		aMap.put("MCL Cluster", "__mclCluster");
		aMap.put("SCPS Cluster", "__scpsCluster");
		algorithmToColumnName = Collections.unmodifiableSortedMap(aMap);
	}
	// used to read from the tables that WordCloud creates
	private CyTableManager tableManager;
	// used to execute command line commands
	private CommandExecutorTaskFactory commandExecutor;
	// used to execute annotation, WordCloud, and clusterMaker tasks
	private DialogTaskManager dialogTaskManager;
	// used to apply layouts
	private SynchronousTaskManager<?> syncTaskManager;
	//used for getting current network
	private CyApplicationManager applicationManager;
	// annotations are added to here
	private AnnotationManager annotationManager;
	// used to update the layout of the nodes
	private CyLayoutAlgorithmManager layoutManager;
	// creates ellipses
	private AnnotationFactory<ShapeAnnotation> shapeFactory;
	// creates text labels
	private AnnotationFactory<TextAnnotation> textFactory;
	// creates node groups stored in clusters
	private CyGroupFactory groupFactory;
	// used to destroy the groups
	private CyGroupManager groupManager;
	// used to select heatmap panel on cluster selection
	private HeatMapPanel heatmapPanel;
	// used to wait for heatmap updating to finish on selection
	private EnrichmentMapActionListener EMActionListener;
	// used to force heatmap to update before selection
	private CyEventHelper eventHelper;
	private DisplayOptionsPanel displayOptionsPanel;
	
	//flag to indicate that currently in the middle of cluster table selection event
	private boolean clusterTableUpdating = false;
	
	public static AutoAnnotationManager getInstance() {
		if (instance == null) {
			instance = new AutoAnnotationManager();
		}
		return instance;
	}
	
	public AutoAnnotationManager() {
		networkViewToAutoAnnotationParameters = new HashMap<CyNetworkView, AutoAnnotationParameters>();
	}

	// Initialize all of the services that will be needed for auto annotation
	public void initialize(CySwingApplication application, CyTableManager tableManager, 
			CommandExecutorTaskFactory commandExecutor,	DialogTaskManager dialogTaskManager, 
			SynchronousTaskManager<?> syncTaskManager, AnnotationManager annotationManager, 
			CyLayoutAlgorithmManager layoutManager, AnnotationFactory<ShapeAnnotation> shapeFactory, 
			AnnotationFactory<TextAnnotation> textFactory, CyGroupFactory groupFactory,
			CyGroupManager groupManager, HeatMapPanel heatMapPanel_node, EnrichmentMapActionListener EMActionListener, CyEventHelper eventHelper,
			CyApplicationManager applicationManager) {
		
		this.application = application;
		this.tableManager = tableManager;
		this.commandExecutor = commandExecutor;
		this.dialogTaskManager = dialogTaskManager;
		this.syncTaskManager = syncTaskManager;
		this.annotationManager = annotationManager;
		this.layoutManager = layoutManager;
		this.shapeFactory = shapeFactory;
		this.textFactory = textFactory;
		this.groupFactory = groupFactory;
		this.groupManager = groupManager;
		this.heatmapPanel = heatMapPanel_node;
		this.EMActionListener = EMActionListener;
		this.eventHelper = eventHelper;
		this.applicationManager = applicationManager;
	}
	
	@Override
	public void handleEvent(SetSelectedNetworkViewsEvent e) {
		if (annotationPanel != null) {
			annotationPanel.updateSelectedView(e.getNetworkViews().get(0));			
		}
	}
	
	@Override
	public void handleEvent(ColumnNameChangedEvent e) {
		if (annotationPanel != null) {
			annotationPanel.updateColumnName(e.getSource(), e.getOldColumnName(), e.getNewColumnName());
		}
	}

	@Override
	public void handleEvent(ColumnDeletedEvent e) {
		if (annotationPanel != null) {
			annotationPanel.columnDeleted(e.getSource(), e.getColumnName());
		}
	}

	@Override
	public void handleEvent(ColumnCreatedEvent e) {
		if (annotationPanel != null) {
			annotationPanel.columnCreated(e.getSource(), e.getColumnName());
		}
	}

	@Override
	public void handleEvent(NetworkViewAboutToBeDestroyedEvent e) {
		if (annotationPanel != null) {
			annotationPanel.removeNetworkView(e.getNetworkView());
		}
	}
	
	@Override
	public void handleEvent(RowsSetEvent e) {
		if (annotationPanel != null && e.getColumnRecords(CyNetwork.SELECTED).size() > 0) {
			
			//get the current network
	        CyNetwork network  = this.applicationManager.getCurrentNetwork();
	        CyNetworkView view = this.applicationManager.getCurrentNetworkView();
	        List<CyNode> selectedNodes = CyTableUtil.getNodesInState(network, CyNetwork.SELECTED, true);
	        
			//if the network has been autoannotated we need to make sure the clusters have been selected
			//also only handle the node selection events (not edges)
			//TODO:need a cleaner way to find out if the currentView has an annotation
			if(AutoAnnotationManager.getInstance().getAnnotationPanel()!=null && !AutoAnnotationManager.getInstance().isClusterTableUpdating()
					&& e.getSource() == network.getDefaultNodeTable()) {
				
				//go through all the clusters for this network to see if any of the cluster have all of their nodes selected
				HashMap<CyNetworkView, AutoAnnotationParameters> annotations = AutoAnnotationManager.getInstance().getNetworkViewToAutoAnnotationParameters();
				if(annotations.containsKey(view)){
					AnnotationSet currentAnnotation = annotations.get(view).getSelectedAnnotationSet();
					TableModel clusterTableModel = currentAnnotation.getClusterTable().getModel();
					ListSelectionModel clusterListSelectionModel = currentAnnotation.getClusterTable().getSelectionModel();
					System.out.println("HERE");
					//if there are clusters to add or to remove only do it once we have gone through all the clusters - to avoid race conditions.
					clusterListSelectionModel.setValueIsAdjusting(true);
					
					TreeMap<Integer, Cluster> clusters = currentAnnotation.getClusterMap();
					//go through each cluster - figure out which ones need to be selected and
					//which ones need to deselected
					//If any nodes in a cluster are no longer selected then deselect cluster
					//If all nodes in a cluster are selected then select cluster (in table and annotation)
					for(Cluster cluster:clusters.values()){
					        					
						boolean select = true;
						boolean unselectCluster = false;
						for (CyNode node : cluster.getNodes()) {
							//if any of the nodes that belong to this cluster are not in the selected set
							//And the cluster is current marked as selected 
							//then unselect the cluster
							if (!selectedNodes.contains(node) && cluster.isSelected()) {
								unselectCluster = true;
								break;
							}
							//if any of the nodes that belong to this cluster are not in the selected set
							//then do not select this cluster.
							if (!selectedNodes.contains(node)) {
								select = false;
								break;
							}
						}
						
						//one last check, if the cluster is already selected and all its nodes are
						//already selected then this is not a new selection event
						if(select == true && cluster.isSelected())
							select = false;
						
						//Cluster has been selected
						//if all nodes in a cluster are selected
						//update the cluster table
						if (select) {
							//set flag to tell listener that it shouldn't reselect the nodes as the user manually selected them. 
							currentAnnotation.setManualSelection(true);
							for (int rowIndex = 0; rowIndex < clusterTableModel.getRowCount(); rowIndex++) {
								if (cluster.equals(clusterTableModel.getValueAt(rowIndex, 0))) {
									clusterListSelectionModel.addSelectionInterval(rowIndex, rowIndex);
									//AutoAnnotationManager.getInstance().flushPayloadEvents();
									break;
								}
							}
						}
						
						//Cluster has been unselected
						//update the cluster table
						if(unselectCluster){
							//set flag to tell listener that it shouldn't reselect the nodes as the user manually selected them. 
							currentAnnotation.setManualSelection(true);
							for (int rowIndex = 0; rowIndex < clusterTableModel.getRowCount(); rowIndex++) {
								if (cluster.equals(clusterTableModel.getValueAt(rowIndex, 0))) {
									clusterListSelectionModel.removeSelectionInterval(rowIndex, rowIndex);
									//AutoAnnotationManager.getInstance().flushPayloadEvents();
									break;
								}//end of if
							}//end of for
							
						}//end of if unselectedcluster

					}//end of For going through all clusters
					
					//if there are clusters to add or to remove only do it once we have gone through all the clusters - to avoid race conditions.
					clusterListSelectionModel.setValueIsAdjusting(false);
					
				}
			}
		}
	}
	
	public HashMap<CyNetworkView, AutoAnnotationParameters> getNetworkViewToAutoAnnotationParameters() {
		return networkViewToAutoAnnotationParameters;
	}

	public void setNetworkViewToAutoAnnotation(
			HashMap<CyNetworkView, AutoAnnotationParameters> networkViewToAutoAnnotationParameters) {
		this.networkViewToAutoAnnotationParameters = networkViewToAutoAnnotationParameters;
	}

	public CySwingApplication getApplication() {
		return application;
	}

	public CyTableManager getTableManager() {
		return tableManager;
	}

	public CommandExecutorTaskFactory getCommandExecutor() {
		return commandExecutor;
	}

	public DialogTaskManager getDialogTaskManager() {
		return dialogTaskManager;
	}

	public SynchronousTaskManager<?> getSyncTaskManager() {
		return syncTaskManager;
	}

	public AutoAnnotationPanel getAnnotationPanel() {
		return annotationPanel;
	}
	
	public void setAnnotationPanel(AutoAnnotationPanel inputPanel) {
		this.annotationPanel = inputPanel;
	}
	
	public DisplayOptionsPanelAction getDisplayOptionsPanelAction() {
		return displayOptionsPanelAction;
	}
	
	public void setDisplayOptionsPanelAction(DisplayOptionsPanelAction displayOptionsPanelAction) {
		this.displayOptionsPanelAction = displayOptionsPanelAction;
	}
	
	public DisplayOptionsPanel getDisplayOptionsPanel() {
		return displayOptionsPanel;
	}
	
	public void setDisplayOptionsPanel(DisplayOptionsPanel displayOptionsPanel) {
		this.displayOptionsPanel = displayOptionsPanel;
	}
	
	public SortedMap<String, String> getAlgorithmToColumnName() {
		return algorithmToColumnName;
	}
	
	public AnnotationManager getAnnotationManager() {
		return annotationManager;
	}

	public CyLayoutAlgorithmManager getLayoutManager() {
		return layoutManager;
	}

	public CyApplicationManager getApplicationManager() {
		return applicationManager;
	}

	public AnnotationFactory<ShapeAnnotation> getShapeFactory() {
		return shapeFactory;
	}

	public AnnotationFactory<TextAnnotation> getTextFactory() {
		return textFactory;
	}
	
	public CyGroupFactory getGroupFactory() {
		return groupFactory;
	}

	public CyGroupManager getGroupManager() {
		return groupManager;
	}

	public HeatMapPanel getHeatmapPanel() {
		return heatmapPanel;
	}
	
	public CytoPanel getWestPanel() {
		return application.getCytoPanel(CytoPanelName.WEST);
	}

	public CytoPanel getSouthPanel() {
		return application.getCytoPanel(CytoPanelName.SOUTH);
	}
	
	public void flushPayloadEvents() {
		eventHelper.flushPayloadEvents();
	}

	public boolean isClusterTableUpdating() {
		return clusterTableUpdating;
	}

	public void setClusterTableUpdating(boolean clusterTableUpdating) {
		this.clusterTableUpdating = clusterTableUpdating;
	}
	
	public AutoAnnotationParameters getParams(){
		//get the current View
		CyNetworkView selectedView = this.annotationPanel.getCurrentView();

		if (networkViewToAutoAnnotationParameters.containsKey(selectedView)) 
			
			return networkViewToAutoAnnotationParameters.get(selectedView);
		else
			return null;
	}
}
