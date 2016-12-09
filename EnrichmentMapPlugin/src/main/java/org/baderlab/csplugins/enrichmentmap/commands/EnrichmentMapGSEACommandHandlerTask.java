package org.baderlab.csplugins.enrichmentmap.commands;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.baderlab.csplugins.enrichmentmap.model.DataSet.Method;
import org.baderlab.csplugins.enrichmentmap.model.DataSetFiles;
import org.baderlab.csplugins.enrichmentmap.model.EMCreationParameters;
import org.baderlab.csplugins.enrichmentmap.model.EMCreationParameters.SimilarityMetric;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMapManager;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMapParameters;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentResultFilterParams.NESFilter;
import org.baderlab.csplugins.enrichmentmap.model.LegacySupport;
import org.baderlab.csplugins.enrichmentmap.task.MasterMapTaskFactory;
import org.baderlab.csplugins.enrichmentmap.view.mastermap.DataSetParameters;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.util.ListSingleSelection;

import com.google.inject.Inject;


public class EnrichmentMapGSEACommandHandlerTask extends AbstractTask {
	
	@Tunable(description="edbdir")		
	public String edbdir;
	
	@Tunable(description="edbdir2")		
	public String edbdir2;
	
	@Tunable(description="P-value Cutoff", groups={"User Input","Parameters"}, gravity = 17.0, tooltip="P-value between 0 and 1.")
	public Double pvalue = 0.005;
	
	@Tunable(description="FDR Q-value Cutoff", groups={"User Input","Parameters"}, gravity = 18.0, tooltip="FDR Q-value between 0 and 1.")
	public Double qvalue = 0.1;
	
	@Tunable(description="overlap", groups={"User Input","Parameters"}, gravity = 19.0, tooltip="coeffecient between 0 and 1.")
	public Double overlap = 0.25;
	
	@Tunable(description="similaritymetric", groups={"User Input","Parameters"}, gravity = 20.0, tooltip="coeffecient between 0 and 1.")
	public ListSingleSelection<String> similaritymetric;
	
	@Tunable(description="expressionfile")
	public String expressionfile = "expressionfile";
	
	@Tunable(description="expressionfile2")
	public String expressionfile2 = "expressionfile2";

	@Tunable(description="combinedconstant ", groups={"User Input","Parameters"}, gravity = 19.0, tooltip="coeffecient between 0 and 1.")
	public Double combinedconstant ;

	
	@Inject private MasterMapTaskFactory.Factory taskFactoryFactory;
	@Inject private EnrichmentMapManager emManager;
	@Inject private LegacySupport legacySupport;
	
	public EnrichmentMapGSEACommandHandlerTask() {
		similaritymetric = new ListSingleSelection<String>(EnrichmentMapParameters.SM_OVERLAP, EnrichmentMapParameters.SM_JACCARD, EnrichmentMapParameters.SM_COMBINED);
	}

	
	private void buildEnrichmentMap(){
		//set all files as extracted from the edb directory
		List<DataSetParameters> dataSets = new ArrayList<>(2);
		DataSetFiles files1 = initializeFiles(edbdir, expressionfile);
		dataSets.add(new DataSetParameters(LegacySupport.DATASET1, Method.GSEA, files1));
		
		//only add second dataset if there is a second edb directory.
		if(edbdir2 != null && !edbdir2.equalsIgnoreCase("")){
			DataSetFiles files2 = initializeFiles(edbdir2, expressionfile2);
			dataSets.add(new DataSetParameters(LegacySupport.DATASET2, Method.GSEA, files2));
		}
		
		SimilarityMetric metric = EnrichmentMapParameters.stringToSimilarityMetric(similaritymetric.getSelectedValue());
		
		String prefix = legacySupport.getNextAttributePrefix();
		EMCreationParameters creationParams = 
				new EMCreationParameters(prefix, pvalue, qvalue, NESFilter.ALL, Optional.empty(), 
						metric, overlap, combinedconstant);
		
		MasterMapTaskFactory taskFactory = taskFactoryFactory.create(creationParams, dataSets);
		insertTasksAfterCurrentTask(taskFactory.createTaskIterator());
		
		emManager.showPanels();
	}
	
	private DataSetFiles initializeFiles(String edb, String exp){
		//for a dataset we require genesets, an expression file (optional), enrichment results
		String file_sep = System.getProperty("file.separator");
		String testEdbResultsFileName = edb + file_sep + "results.edb";
		String testgmtFileName = edb + file_sep + "gene_sets.gmt";
		
		//the rank file does not have a set name.  We need to figure out the name of the rank file
		String testrnkFileName = "";
		File directory = new File(edb);
		String[] dir_listing = directory.list();
		if(dir_listing.length > 0){
			for(int i = 0 ; i < dir_listing.length;i++){
				if(dir_listing[i].endsWith("rnk") && testrnkFileName.equals(""))
					testrnkFileName = edb + file_sep + dir_listing[i];
				else if(dir_listing[i].endsWith("rnk") && !testrnkFileName.equals(""))
					System.out.println("There are two rnk files in the edb directory.  Using the first one found");
			}
		}
		
		DataSetFiles files = new DataSetFiles();		
		files.setEnrichmentFileName1(testEdbResultsFileName);
		files.setGMTFileName(testgmtFileName);
		if(!testrnkFileName.equals(""))
			files.setRankedFile(testrnkFileName);
		if(!exp.equals("")){
			files.setExpressionFileName(exp);
		}
		return files;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		buildEnrichmentMap();
	}

}
