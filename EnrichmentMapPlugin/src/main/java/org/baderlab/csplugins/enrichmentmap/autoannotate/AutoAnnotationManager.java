package org.baderlab.csplugins.enrichmentmap.autoannotate;

import java.util.HashMap;
import java.util.TreeMap;

import org.baderlab.csplugins.enrichmentmap.autoannotate.view.AutoAnnotationPanel;
import org.cytoscape.application.events.SetSelectedNetworkViewsEvent;
import org.cytoscape.application.events.SetSelectedNetworkViewsListener;
import org.cytoscape.command.CommandExecutorTaskFactory;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.model.events.ColumnCreatedEvent;
import org.cytoscape.model.events.ColumnCreatedListener;
import org.cytoscape.model.events.ColumnDeletedEvent;
import org.cytoscape.model.events.ColumnDeletedListener;
import org.cytoscape.model.events.ColumnNameChangedEvent;
import org.cytoscape.model.events.ColumnNameChangedListener;
import org.cytoscape.view.model.events.NetworkViewAboutToBeDestroyedEvent;
import org.cytoscape.view.model.events.NetworkViewAboutToBeDestroyedListener;
import org.cytoscape.view.presentation.annotations.AnnotationFactory;
import org.cytoscape.view.presentation.annotations.AnnotationManager;
import org.cytoscape.view.presentation.annotations.ShapeAnnotation;
import org.cytoscape.view.presentation.annotations.TextAnnotation;
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
	
	private AutoAnnotationPanel annotationPanel;
	// Stores the annotation parameters (one for each network view)
	private HashMap<Long, AutoAnnotationParameters> networkViewToAutoAnnotationParameters;
	// used to set clusterMaker default parameters
	private TreeMap<String, String> algorithmToColumnName;
	// used to read from the tables that WordCloud creates
	private CyTableManager tableManager;
	// used to execute command line commands
	private CommandExecutorTaskFactory commandExecutor;
	// used to execute annotation, WordCloud, and clusterMaker tasks
	private DialogTaskManager dialogTaskManager;
	// annotations are added to here
	private AnnotationManager annotationManager;
	// creates ellipses
	private AnnotationFactory<ShapeAnnotation> shapeFactory;
	// creates text labels
	private AnnotationFactory<TextAnnotation> textFactory;
	
	public AnnotationManager getAnnotationManager() {
		return annotationManager;
	}

	public AnnotationFactory<ShapeAnnotation> getShapeFactory() {
		return shapeFactory;
	}

	public AnnotationFactory<TextAnnotation> getTextFactory() {
		return textFactory;
	}

	public AutoAnnotationManager(CyTableManager tableManager, CommandExecutorTaskFactory commandExecutor,
			DialogTaskManager dialogTaskManager, AnnotationManager annotationManager, 
			AnnotationFactory<ShapeAnnotation> shapeFactory, AnnotationFactory<TextAnnotation> textFactory) {
		networkViewToAutoAnnotationParameters = new HashMap<Long, AutoAnnotationParameters>();
		
		algorithmToColumnName = new TreeMap<String, String>();		
		algorithmToColumnName.put("Affinity Propagation Cluster", "__APCluster");
		algorithmToColumnName.put("Cluster Fuzzifier", "__fuzzifierCluster");
		algorithmToColumnName.put("Community cluster (GLay)", "__glayCluster");
		algorithmToColumnName.put("ConnectedComponents Cluster", "__ccCluster");
		algorithmToColumnName.put("Fuzzy C-Means Cluster", "__fcmlCluster");
		algorithmToColumnName.put("MCL Cluster", "__mclCluster");
		algorithmToColumnName.put("SCPS Cluster", "__scpsCluster");
		
		this.tableManager = tableManager;
		this.commandExecutor = commandExecutor;
		this.dialogTaskManager = dialogTaskManager;
		this.annotationManager = annotationManager;
		this.shapeFactory = shapeFactory;
		this.textFactory = textFactory;
	}

	@Override
	public void handleEvent(SetSelectedNetworkViewsEvent e) {
		annotationPanel.updateSelectedView(e.getNetworkViews().get(0));
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
	
	public HashMap<Long, AutoAnnotationParameters> getNetworkViewToAutoAnnotationParameters() {
		return networkViewToAutoAnnotationParameters;
	}

	public void setNetworkViewToAutoAnnotation(
			HashMap<Long, AutoAnnotationParameters> networkViewToAutoAnnotationParameters) {
		this.networkViewToAutoAnnotationParameters = networkViewToAutoAnnotationParameters;
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

	public AutoAnnotationPanel getAnnotationPanel() {
		return annotationPanel;
	}
	
	public void setAnnotationPanel(AutoAnnotationPanel inputPanel) {
		this.annotationPanel = inputPanel;
	}
	
	public TreeMap<String, String> getAlgorithmToColumnName() {
		return algorithmToColumnName;
	}
	
}
