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
		private static String edbdir = "edbdir";
		private static String pvalue = "pvalue";
		private static String qvalue = "qvalue";
		private static String overlap = "overlap";
	
		public EnrichmentMapCommandHandler(String namespace) {
			super(CyCommandManager.reserveNamespace(namespace));
			
			addDescription(command,"Build an enrichmentmap from GSEA results (in an edb directory)");
			addArgument(command, edbdir);
			addArgument(command, pvalue);
			addArgument(command, qvalue);
			addArgument(command, overlap);
			
		}
		
		public CyCommandResult execute(String arg0, Map<String, Object> arg1)
				throws CyCommandException {
			
			//create a results object
			CyCommandResult results = new CyCommandResult();
			String file = "";
			Double pvalue_loaded = 0.05;
			Double qvalue_loaded = 0.25;
			Double overlap_loaded = 0.5;
			
			//get the edb file to run enrichment maps on
			//If working on windows and user cuts and copies path there is 
			//no way to correct forward slashes.  User needs to use double forward slashes
			//or backward slashes.
			if(arg1.containsKey(edbdir))
				file = (String)arg1.get(edbdir);
				
			//get other parameters if they are present:
			if(arg1.containsKey(pvalue)){
				try{
					pvalue_loaded = Double.parseDouble((String)arg1.get(pvalue));
				}catch(NumberFormatException e){
					System.out.println("the pvalue can only be a number (i.e. pvalue=0.05).  Ignored pvalue setting by user and using default.");
					pvalue_loaded = 0.05;
				}
			}
			
			if(arg1.containsKey(qvalue)){
				try{
					qvalue_loaded = Double.parseDouble((String)arg1.get(qvalue));
				}catch(NumberFormatException e){
					System.out.println("the qvalue can only be a number (i.e. qvalue=0.25).  Ignored qvalue setting by user and using default.");
					qvalue_loaded = 0.25;
				}
			}
			
			if(arg1.containsKey(overlap)){
				try{
					overlap_loaded = Double.parseDouble((String)arg1.get(overlap));
				}catch(NumberFormatException e){
					System.out.println("the overlap can only be a number (i.e. overlap=0.50).  Ignored overlap setting by user and using default.");
					overlap_loaded = 0.50;
				}
			}
			
			EnrichmentMapParameters params = new EnrichmentMapParameters();
			
			//for a dataset we require genesets, an expression file (optional), enrichment results
			String file_sep = System.getProperty("file.separator");
			String testEdbResultsFileName = file + file_sep + "results.edb";
			String testgmtFileName = file + file_sep + "gene_sets.gmt";
			String testrnkFileName = file + file_sep + "Expressiontestfile.rnk";		
			
			DataSetFiles files = new DataSetFiles();		
			files.setEnrichmentFileName1(testEdbResultsFileName);
			files.setGMTFileName(testgmtFileName);
			files.setRankedFile(testrnkFileName);
			params.addFiles(EnrichmentMap.DATASET1, files);
			
			//set the method to gsea
			params.setMethod(EnrichmentMapParameters.method_GSEA);
			params.setSimilarityMetric(EnrichmentMapParameters.SM_OVERLAP);
			params.setSimilarityCutOff(overlap_loaded);
			params.setPvalue(pvalue_loaded);
			params.setQvalue(qvalue_loaded); 
			
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
