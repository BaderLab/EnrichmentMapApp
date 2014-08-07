package org.baderlab.csplugins.enrichmentmap.autoannotate;

import java.util.Collections;
import java.util.HashMap;
import java.util.SortedMap;
import java.util.TreeMap;

import org.baderlab.csplugins.enrichmentmap.autoannotate.view.AutoAnnotationPanel;
import org.baderlab.csplugins.enrichmentmap.view.HeatMapPanel;
import org.cytoscape.application.events.SetSelectedNetworkViewsEvent;
import org.cytoscape.application.events.SetSelectedNetworkViewsListener;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.command.CommandExecutorTaskFactory;
import org.cytoscape.group.CyGroupFactory;
import org.cytoscape.group.CyGroupManager;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.model.events.ColumnCreatedEvent;
import org.cytoscape.model.events.ColumnCreatedListener;
import org.cytoscape.model.events.ColumnDeletedEvent;
import org.cytoscape.model.events.ColumnDeletedListener;
import org.cytoscape.model.events.ColumnNameChangedEvent;
import org.cytoscape.model.events.ColumnNameChangedListener;
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
		NetworkViewAboutToBeDestroyedListener {
	
	// instance variable used to get the manager from other parts of the program
	private static AutoAnnotationManager instance = null;
	// used to control selected panels
	private CySwingApplication application;
	// reference to the panel that the user interacts with
	private AutoAnnotationPanel annotationPanel;
	// stores the annotation parameters (one for each network view)
	private HashMap<CyNetworkView, AutoAnnotationParameters> networkViewToAutoAnnotationParameters;
	// used to set clusterMaker default parameters
	private static final SortedMap<String, String> algorithmToColumnName;
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
			CyGroupManager groupManager, HeatMapPanel heatMapPanel_node) {
		
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
	
	public SortedMap<String, String> getAlgorithmToColumnName() {
		return algorithmToColumnName;
	}
	
	public AnnotationManager getAnnotationManager() {
		return annotationManager;
	}

	public CyLayoutAlgorithmManager getLayoutManager() {
		return layoutManager;
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
}
