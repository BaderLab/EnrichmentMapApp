package org.baderlab.csplugins.enrichmentmap.commands;

import java.io.File;
import java.io.FileWriter;

import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.model.io.ModelSerializer;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ContainsTunables;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;

import com.google.inject.Inject;

public class ExportModelJsonCommandTask extends AbstractTask {

	@Tunable(required=true, description="File used as destination for model JSON. Will be overwritten if it already exists.")
	public File file;
	
	@Inject @ContainsTunables
	public NetworkTunable networkTunable;
	
	
	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		file.createNewFile();
		if(!file.canWrite())
			throw new IllegalArgumentException("Cannot write to file");
		
		EnrichmentMap map = networkTunable.getEnrichmentMap();
		if(map == null)
			throw new IllegalArgumentException("Network is not an Enrichment Map.");
		
		String json = ModelSerializer.serialize(map, true);
		
		try(FileWriter out = new FileWriter(file)) {
			out.write(json);
		}
	}

	
}
