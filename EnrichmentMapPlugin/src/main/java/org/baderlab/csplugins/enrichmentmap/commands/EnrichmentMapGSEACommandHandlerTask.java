package org.baderlab.csplugins.enrichmentmap.commands;

import java.io.File;

import org.baderlab.csplugins.enrichmentmap.EnrichmentMapManager;
import org.baderlab.csplugins.enrichmentmap.EnrichmentMapParameters;
import org.baderlab.csplugins.enrichmentmap.model.DataSetFiles;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.task.EnrichmentMapBuildMapTaskFactory;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.io.util.StreamUtil;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyTableFactory;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.session.CySessionManager;
import org.cytoscape.task.edit.MapTableToNetworkTablesTaskFactory;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.swing.DialogTaskManager;
import org.cytoscape.work.util.ListSingleSelection;


//TODO:Add command support

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

	
	//required services
	private CySessionManager sessionManager;
	private StreamUtil streamUtil;

	private CyApplicationManager applicationManager;
	private CySwingApplication swingApplication;
	private CyNetworkManager networkManager;
	private CyNetworkViewManager networkViewManager;
	private CyNetworkViewFactory networkViewFactory;
	private CyNetworkFactory networkFactory;
	private CyTableFactory tableFactory;
	private CyTableManager tableManager;

	private VisualMappingManager visualMappingManager;
	private VisualStyleFactory visualStyleFactory;

	//we will need all three mappers
	private VisualMappingFunctionFactory vmfFactoryContinuous;
	private VisualMappingFunctionFactory vmfFactoryDiscrete;
	private VisualMappingFunctionFactory vmfFactoryPassthrough;

	private CyLayoutAlgorithmManager layoutManager;
	private MapTableToNetworkTablesTaskFactory mapTableToNetworkTable;

	private DialogTaskManager dialog;

	
	public EnrichmentMapGSEACommandHandlerTask(CySessionManager sessionManager,
				StreamUtil streamUtil, CyApplicationManager applicationManager,
				CySwingApplication swingApplication,
				CyNetworkManager networkManager,
				CyNetworkViewManager networkViewManager,
				CyNetworkViewFactory networkViewFactory,
				CyNetworkFactory networkFactory, CyTableFactory tableFactory,
				CyTableManager tableManager,
				VisualMappingManager visualMappingManager,
				VisualStyleFactory visualStyleFactory,
				VisualMappingFunctionFactory vmfFactoryContinuous,
				VisualMappingFunctionFactory vmfFactoryDiscrete,
				VisualMappingFunctionFactory vmfFactoryPassthrough,
				CyLayoutAlgorithmManager layoutManager,
				MapTableToNetworkTablesTaskFactory mapTableToNetworkTable,
				DialogTaskManager dialog) {
			super();
			this.sessionManager = sessionManager;
			this.streamUtil = streamUtil;
			this.applicationManager = applicationManager;
			this.swingApplication = swingApplication;
			this.networkManager = networkManager;
			this.networkViewManager = networkViewManager;
			this.networkViewFactory = networkViewFactory;
			this.networkFactory = networkFactory;
			this.tableFactory = tableFactory;
			this.tableManager = tableManager;
			this.visualMappingManager = visualMappingManager;
			this.visualStyleFactory = visualStyleFactory;
			this.vmfFactoryContinuous = vmfFactoryContinuous;
			this.vmfFactoryDiscrete = vmfFactoryDiscrete;
			this.vmfFactoryPassthrough = vmfFactoryPassthrough;
			this.layoutManager = layoutManager;
			this.mapTableToNetworkTable = mapTableToNetworkTable;
			this.dialog = dialog;
			 
			similaritymetric = new ListSingleSelection<String>(EnrichmentMapParameters.SM_OVERLAP, EnrichmentMapParameters.SM_JACCARD, EnrichmentMapParameters.SM_COMBINED);
		}

		
		private void buildEnrichmentMap(){
			//Initialize Data create a new params for the new EM and add the dataset files to it
			EnrichmentMapParameters new_params = new EnrichmentMapParameters(sessionManager,streamUtil,applicationManager);
		
		
			//set all files as extracted from the edb directory
			DataSetFiles files = this.InitializeFiles(edbdir, expressionfile);
			if(!expressionfile.equals("")){
				new_params.setData(true);
			}
			new_params.addFiles(EnrichmentMap.DATASET1, files);
			//only add second dataset if there is a second edb directory.
			if(edbdir2 != null && !edbdir2.equalsIgnoreCase("")){
				new_params.setTwoDatasets(true);
				DataSetFiles files2 = this.InitializeFiles(edbdir2, expressionfile2);
				if(!expressionfile2.equals("")){
					new_params.setData2(true);
				}
				new_params.addFiles(EnrichmentMap.DATASET2, files2);
			}
		
			//set the method to gsea
			new_params.setMethod(EnrichmentMapParameters.method_GSEA);
			if(similaritymetric.getSelectedValue() == EnrichmentMapParameters.SM_JACCARD)
				new_params.setSimilarityMetric(EnrichmentMapParameters.SM_JACCARD);
			if(similaritymetric.getSelectedValue() == EnrichmentMapParameters.SM_OVERLAP)
				new_params.setSimilarityMetric(EnrichmentMapParameters.SM_OVERLAP);
			if(similaritymetric.getSelectedValue() == EnrichmentMapParameters.SM_COMBINED)
				new_params.setSimilarityMetric(EnrichmentMapParameters.SM_COMBINED);
			
			new_params.setSimilarityCutOff(overlap);
			new_params.setPvalue(pvalue);
			new_params.setQvalue(qvalue);
			new_params.setFDR(true);
			new_params.setCombinedConstant(combinedconstant);
		
		EnrichmentMap map = new EnrichmentMap(new_params);

		EnrichmentMapBuildMapTaskFactory buildmap = new EnrichmentMapBuildMapTaskFactory(map, applicationManager,
				swingApplication, networkManager, networkViewManager, networkViewFactory, networkFactory, tableFactory,
				tableManager, visualMappingManager, visualStyleFactory, vmfFactoryContinuous, vmfFactoryDiscrete,
				vmfFactoryPassthrough, dialog, streamUtil, layoutManager, mapTableToNetworkTable);

			insertTasksAfterCurrentTask(buildmap.createTaskIterator());
			
			EnrichmentMapManager manager = EnrichmentMapManager.getInstance();
			manager.registerServices();
		}
		
		private DataSetFiles InitializeFiles(String edb, String exp){
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
			// TODO Auto-generated method stub
			buildEnrichmentMap();
		}

}
