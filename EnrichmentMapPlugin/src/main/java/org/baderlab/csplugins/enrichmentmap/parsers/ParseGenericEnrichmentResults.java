package org.baderlab.csplugins.enrichmentmap.parsers;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Scanner;

import org.baderlab.csplugins.enrichmentmap.model.DataSet;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentResult;
import org.baderlab.csplugins.enrichmentmap.model.GeneSet;
import org.baderlab.csplugins.enrichmentmap.model.GenericResult;
import org.baderlab.csplugins.enrichmentmap.model.SetOfEnrichmentResults;
import org.cytoscape.io.util.StreamUtil;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

public class ParseGenericEnrichmentResults extends AbstractTask {
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


	public ParseGenericEnrichmentResults(DataSet dataset, StreamUtil streamUtil) {
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

    	parseGenericFile(lines);
    			
    }//end of method
    /**
     * Parse generic enrichment results file
     *
     * @param lines - contents of results file
     */
    public void parseGenericFile(String [] lines){

        //Get the current genesets so we can check that all the results are in the geneset list
        //and put the size of the genesets into the visual style
        HashMap genesets = dataset.getMap().getAllGenesets();

        int currentProgress = 0;
        int maxValue = lines.length;
        boolean FDR = false;
        boolean ignore_phenotype = false;

         //skip the first line which just has the field names (start i=1)
        //check to see how many columns the data has
        String line = lines[0];
        String [] tokens = line.split("\t");
        int length = tokens.length;
        //if (length < 3)
           //not enough data in the file!!

        for (int i = 1; i < lines.length; i++) {
            line = lines[i];

            tokens = line.split("\t");

            double pvalue = 1.0;
            double FDRqvalue = 1.0;
            GenericResult result;
            int gs_size = 0;
            double NES = 1.0;

            //The first column of the file is the name of the geneset
            String name = tokens[0].toUpperCase().trim();

            if(genesets.containsKey(name)){
                GeneSet current_set = (GeneSet)genesets.get(name);
                gs_size = current_set.getGenes().size();
            }

            String description = tokens[1].toUpperCase();

            //The third column is the nominal p-value
            if(tokens[2].equalsIgnoreCase("")){
                //do nothing
            }else{
                pvalue = Double.parseDouble(tokens[2]);
            }

            if(length > 3){
                //the fourth column is the FDR q-value
                if(tokens[3].equalsIgnoreCase("")){
                    //do nothing
                }else{
                    FDRqvalue = Double.parseDouble(tokens[3]);
                    FDR = true;
                }
                //the fifth column is the phenotype.
                //it can either be a signed number or it can be text specifying the phenotype
                //in order for it to be parseable the text has to match the user specified phenotypes
                // and if it is a number the only important part is the sign
                if((length > 4) && !ignore_phenotype){

                    if(tokens[4].equalsIgnoreCase("")){

                    }else{

                        //check to see if the string matches the specified phenotypes
                        if(tokens[4].equalsIgnoreCase(upPhenotype))
                            NES = 1.0;
                        else if(tokens[4].equalsIgnoreCase(downPhenotype))
                            NES = -1.0;
                        //try and see if the user has specified the phenotype as a number
                        else{
                            try{
                                NES = Double.parseDouble(tokens[4]);
                            }catch (NumberFormatException nfe){
                                //would like to give user the option but it won't let me interupt the task using Joptionpane
                                /*int answer = JOptionPane.showConfirmDialog(Cytoscape.getDesktop(),"One or more of the enrichment results have a phenotype of unknown type specified.  Would you like me to ignore the phenotype Column?","Unrecognizable Phenotype.", JOptionPane.YES_NO_OPTION);
                                if(answer == JOptionPane.YES_OPTION)
                                    ignore_phenotype = true;
                                else*/
                                    throw new IllegalThreadStateException(tokens[4]+ " is not a valid phenotype.  Phenotype specified in generic enrichment results file must have the same phenotype as specified in advanced options or must be a positive or negative number.");
                            }
                        }
                    }

                    result = new GenericResult(name,description,pvalue,gs_size,FDRqvalue,NES);
                }
                else
                    result = new GenericResult(name,description,pvalue,gs_size,FDRqvalue);

            }
            else{
                result = new GenericResult(name, description,pvalue,gs_size);
            }

            // Calculate Percentage.  This must be a value between 0..100.
            int percentComplete = (int) (((double) currentProgress / maxValue) * 100);
            //  Estimate Time Remaining
            long timeRemaining = maxValue - currentProgress;
            if (taskMonitor != null) {
                    taskMonitor.setProgress(percentComplete);
                    taskMonitor.setStatusMessage("Parsing Generic Results file " + currentProgress + " of " + maxValue);
                }
            currentProgress++;

             //check to see if the gene set has already been entered in the results
             //it is possible that one geneset will be in both phenotypes.
             //if it is already exists then we want to make sure the one retained is the result with the
             //lower p-value.
             //ticket #149
           GenericResult temp = (GenericResult)results.get(name);
             if(temp == null)
                results.put(name, result);
            else{
                 if(result.getPvalue() < temp.getPvalue())
                    results.put(name, result);
             }

        }
        if(FDR)
            dataset.getMap().getParams().setFDR(FDR);
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
