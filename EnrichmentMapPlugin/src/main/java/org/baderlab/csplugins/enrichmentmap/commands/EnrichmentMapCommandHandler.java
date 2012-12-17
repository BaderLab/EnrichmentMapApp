package org.baderlab.csplugins.enrichmentmap.commands;

import java.util.Collection;
import java.util.Map;

import org.baderlab.csplugins.enrichmentmap.EnrichmentMapParameters;
import org.baderlab.csplugins.enrichmentmap.model.DataSetFiles;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.task.BuildEnrichmentMapTask;

import cytoscape.command.AbstractCommandHandler;
import cytoscape.command.CyCommandException;
import cytoscape.command.CyCommandManager;
import cytoscape.command.CyCommandResult;
import cytoscape.layout.Tunable;
import cytoscape.task.ui.JTaskConfig;
import cytoscape.task.util.TaskManager;

public class EnrichmentMapCommandHandler extends AbstractCommandHandler {
		
		private static String command = "build";
		private static String arglist1 = "edbdir";
	
		public EnrichmentMapCommandHandler(String namespace) {
			super(CyCommandManager.reserveNamespace(namespace));
			
			addDescription(command,"Build an enrichmentmap from GSEA results (in an edb directory)");
			addArgument(command, arglist1);
			
			
		}
		
		public CyCommandResult execute(String arg0, Map<String, Object> arg1)
				throws CyCommandException {
			
			//create a results object
			CyCommandResult results = new CyCommandResult();
			String file = "";
			//get the edb file to run enrichment maps on
			if(arg1.containsKey(arglist1))
				file = (String)arg1.get(arglist1);
			
			EnrichmentMapParameters params = new EnrichmentMapParameters();
			
			//for a dataset we require genesets, an expression file (optional), enrichment results
			String testEdbResultsFileName = file + "/results.edb";
			String testgmtFileName = file + "/gene_sets.gmt";
			String testrnkFileName = file + "/Expressiontestfile.rnk";		
			
			DataSetFiles files = new DataSetFiles();		
			files.setEnrichmentFileName1(testEdbResultsFileName);
			files.setGMTFileName(testgmtFileName);
			files.setRankedFile(testrnkFileName);
			params.addFiles(EnrichmentMap.DATASET1, files);
			
			//set the method to gsea
			params.setMethod(EnrichmentMapParameters.method_GSEA);
			params.setSimilarityMetric(EnrichmentMapParameters.SM_JACCARD);
			params.setSimilarityCutOff(0.5);
			params.setPvalue(1.0);
			params.setQvalue(1.0); 
			
			JTaskConfig config = new JTaskConfig();
	        config.displayCancelButton(true);
	        config.displayCloseButton(true);
	        config.displayStatus(true);
			
			//Build EnrichmentMap
	        BuildEnrichmentMapTask new_map = new BuildEnrichmentMapTask(params);
            boolean success = TaskManager.executeTask(new_map,config);
			
			return results;
		}

		public CyCommandResult execute(String arg0, Collection<Tunable> arg1)
				throws CyCommandException {
			return execute(command, createKVMap(arg1));
		}

	
}
