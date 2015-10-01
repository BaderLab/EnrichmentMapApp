package org.baderlab.csplugins.enrichmentmap.parsers;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Scanner;

import org.baderlab.csplugins.enrichmentmap.model.DataSet;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentResult;
import org.baderlab.csplugins.enrichmentmap.model.GSEAResult;
import org.baderlab.csplugins.enrichmentmap.model.SetOfEnrichmentResults;
import org.cytoscape.io.util.StreamUtil;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

public class ParseGSEAEnrichmentResults extends AbstractTask{
	//default Score at Max value
    public static final Double DefaultScoreAtMax = -1000000.0;
	
    //private EnrichmentMapParameters params;
    private DataSet dataset;
    //enrichment results file name
    private String EnrichmentResultFileName1;
    private String EnrichmentResultFileName2;
    
    //Stores the enrichment results
    private SetOfEnrichmentResults enrichments;
    private HashMap<String, EnrichmentResult> results ;

    //phenotypes defined by user - used to classify phenotype specifications
    //in the generic enrichment results file
    private String upPhenotype;
    private String downPhenotype;

    // Keep track of progress for monitoring:
    private TaskMonitor taskMonitor = null;
    private boolean interrupted = false;


    //services needed
    private StreamUtil streamUtil;


	public ParseGSEAEnrichmentResults(DataSet dataset, StreamUtil streamUtil) {
		super();
		this.dataset = dataset;
		this.streamUtil = streamUtil;
		
		 this.EnrichmentResultFileName1 = dataset.getEnrichments().getFilename1();
	     this.EnrichmentResultFileName2 = dataset.getEnrichments().getFilename2();

	     //create a new enrichment results set
	     enrichments = dataset.getEnrichments();
	     results = enrichments.getEnrichments();
	     upPhenotype = enrichments.getPhenotype1(); 
	     downPhenotype = enrichments.getPhenotype2();
	       
	}

	/**
     * Parse enrichment results file
     */

    public void parse()  throws IOException{
 	
    		if(this.EnrichmentResultFileName1 != null && !this.EnrichmentResultFileName1.isEmpty())
    			readFile(this.EnrichmentResultFileName1);
    		if(this.EnrichmentResultFileName2 != null && !this.EnrichmentResultFileName2.isEmpty())
    			readFile(this.EnrichmentResultFileName2);
         

    }
    /*
     * Read file
     */

    public void readFile(String EnrichmentResultFileName) throws IOException{
    		
    	//open Enrichment Result file
    	InputStream reader = streamUtil.getInputStream(EnrichmentResultFileName);
    			
    	String fullText = new Scanner(reader,"UTF-8").useDelimiter("\\A").next();                        
  			
    	String []lines = fullText.split("\r\n?|\n");

    	//ES and NES columns are specific to the GSEA format
    	String header_line = lines[0];
    	String [] tokens = header_line.split("\t");

    	parseGSEAFile(lines);
    			
    }//end of method
    
    /**
     * Parse GSEA enrichment results file.
     *
     * @param lines - contents of results file
     */
    public void parseGSEAFile(String[] lines){
        //skip the first line which just has the field names (start i=1)

    	dataset.getMap().getParams().setFDR(true);

         int currentProgress = 0;
         int maxValue = lines.length;
         if(taskMonitor!=null)
        	taskMonitor.setStatusMessage("Parsing Enrichment Results file - " + maxValue + " rows");
         
         for (int i = 1; i < lines.length; i++) {
                String line = lines[i];

                String [] tokens = line.split("\t");
                int size = 0;
                double ES = 0.0;
                double NES = 0.0;
                double pvalue = 1.0;
                double FDRqvalue = 1.0;
                double FWERqvalue = 1.0;
                int rankAtMax = -1;
                double scoreAtMax = DefaultScoreAtMax;

                //The first column of the file is the name of the geneset
                String Name = tokens[0].toUpperCase().trim();

                //The fourth column is the size of the geneset
                if(tokens[3].equalsIgnoreCase("")){
                    //do nothing
                }else{
                    size = Integer.parseInt(tokens[3]);
                }

                //The fifth column is the Enrichment score (ES)
                if(tokens[4].equalsIgnoreCase("")){
                    //do nothing
                }else{
                     ES = Double.parseDouble(tokens[4]);
                }

                //The sixth column is the Normalize Enrichment Score (NES)
                if(tokens[5].equalsIgnoreCase("")){
                    //do nothing
                }else{
                     NES = Double.parseDouble(tokens[5]);
                }

                //The seventh column is the nominal p-value
                if(tokens[6].equalsIgnoreCase("")){
                    //do nothing
                }else{
                    pvalue = Double.parseDouble(tokens[6]);
                }

                //the eighth column is the FDR q-value
                if(tokens[7].equalsIgnoreCase("")){
                    //do nothing
                }else{
                    FDRqvalue = Double.parseDouble(tokens[7]);
                }
                //the ninth column is the FWER q-value
                if(tokens[8].equalsIgnoreCase("")){
                    //do nothing
                }else{
                    FWERqvalue = Double.parseDouble(tokens[8]);
                }
                //the tenth column is the rankatmax
                if(tokens[8].equalsIgnoreCase("")){
                    //do nothing
                }else{
                    rankAtMax = Integer.parseInt(tokens[9]);
                }
                GSEAResult result = new GSEAResult(Name, size, ES, NES,pvalue,FDRqvalue,FWERqvalue,rankAtMax,scoreAtMax);


                // Calculate Percentage.  This must be a value between 0..100.
                int percentComplete = (int) (((double) currentProgress / maxValue) * 100);
                //  Estimate Time Remaining
                long timeRemaining = maxValue - currentProgress;
                if (taskMonitor != null) 
                        taskMonitor.setProgress(percentComplete);
 
                currentProgress++;

                results.put(Name, result);
            }
    }
    /**
     * Non-blocking call to interrupt the task.
     */
    public void halt() {
        this.interrupted = true;
    }

     /**
     * Sets the Task Monitor.
     *
     * @param taskMonitor TaskMonitor Object.
     */
    public void setTaskMonitor(TaskMonitor taskMonitor) {
        if (this.taskMonitor != null) {
            throw new IllegalStateException("Task Monitor is already set.");
        }
        this.taskMonitor = taskMonitor;
    }

    /**
     * Gets the Task Title.
     *
     * @return human readable task title.
     */
    public String getTitle() {
        return new String("Parsing Enrichment Result file");
    }
    
	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		this.taskMonitor = taskMonitor;
		this.taskMonitor.setTitle("Parsing GSEA Enrichment Result file");
		
		parse();
		
	}
}
