package org.baderlab.csplugins.enrichmentmap.actions;

import org.baderlab.csplugins.enrichmentmap.model.DataSetFiles;
import org.baderlab.csplugins.enrichmentmap.model.EMDataSet;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMapManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.task.NodeViewTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.work.TaskIterator;

import com.google.common.base.Strings;
import com.google.inject.Inject;

public class OpenPathwayCommonsTaskFactory implements NodeViewTaskFactory {

	@Inject private OpenPathwayCommonsTask.Factory taskFactory;
	@Inject private EnrichmentMapManager emManager;

	@Override
	public TaskIterator createTaskIterator(View<CyNode> nodeView, CyNetworkView networkView) {
		return new TaskIterator(taskFactory.create(nodeView.getModel(), networkView.getModel()));
	}

	/**
	 * There has to be class and expression data available for at least one dataset.
	 */
	@Override
	public boolean isReady(View<CyNode> nodeView, CyNetworkView networkView) {
		// have to call isEnrichmentMap() because getEnrichmentMap() returns a value for associated networks such as genemania networks
		if(!emManager.isEnrichmentMap(networkView)) 
			return false;
		EnrichmentMap em = emManager.getEnrichmentMap(networkView.getModel().getSUID());
		if(em == null)
			return false;
		
		for(EMDataSet dataset : em.getDataSetList()) {
			DataSetFiles files = dataset.getDataSetFiles();
			if(!Strings.isNullOrEmpty(files.getExpressionFileName()) && !Strings.isNullOrEmpty(files.getClassFile())) {
				return true;
			}
		}
		return false;
	}

}