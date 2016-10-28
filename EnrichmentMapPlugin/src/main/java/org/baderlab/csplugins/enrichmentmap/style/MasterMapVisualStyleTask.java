package org.baderlab.csplugins.enrichmentmap.style;

import java.util.Collection;

import org.baderlab.csplugins.enrichmentmap.model.DataSet;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

public class MasterMapVisualStyleTask extends AbstractTask {

	@Inject private VisualMappingManager visualMappingManager;
	@Inject private VisualStyleFactory visualStyleFactory;
	
	private final MasterMapStyleOptions options;
	
	
	public interface Factory {
		MasterMapVisualStyleTask create(MasterMapStyleOptions options);
	}
	
	@Inject
	public MasterMapVisualStyleTask(@Assisted MasterMapStyleOptions options) {
		this.options = options;
	}
	
	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		taskMonitor.setTitle("Apply Visual Style");
		applyVisualStyle();
		taskMonitor.setStatusMessage("");
	}

	
	private void applyVisualStyle() {
		Collection<DataSet> dataSets = options.getDataSets();
		
		dataSets.stream().map(DataSet::getName).forEach(System.out::println);
		
		// MKTODO updating the visual style will have two parts
		// 1) Create the Visual Style if it doesn't already exist (or if the user renamed/deleted)
		// 2) Update the attributes that the VS uses
		
		// Part 2) is the new part that makes the VS "dynamic"
		
		// get the visual style with the same name as the enrichment map
		// - if not found then create it
		// pass the visual style to MasterMapVisualStyle
	}
}
