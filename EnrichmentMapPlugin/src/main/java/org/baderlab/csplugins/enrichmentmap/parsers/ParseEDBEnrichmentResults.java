package org.baderlab.csplugins.enrichmentmap.parsers;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.baderlab.csplugins.enrichmentmap.model.DataSet;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentResult;
import org.baderlab.csplugins.enrichmentmap.model.GSEAResult;
import org.baderlab.csplugins.enrichmentmap.model.SetOfEnrichmentResults;
import org.cytoscape.io.util.StreamUtil;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class ParseEDBEnrichmentResults extends AbstractTask {

	//default Score at Max value
	public static final Double DefaultScoreAtMax = -1000000.0;

	//private EnrichmentMapParameters params;
	private DataSet dataset;
	//enrichment results file name
	private String EnrichmentResultFileName1;
	private String EnrichmentResultFileName2;

	//Stores the enrichment results
	private SetOfEnrichmentResults enrichments;
	private HashMap<String, EnrichmentResult> results;

	//phenotypes defined by user - used to classify phenotype specifications
	//in the generic enrichment results file
	private String upPhenotype;
	private String downPhenotype;

	// Keep track of progress for monitoring:
	private TaskMonitor taskMonitor = null;
	private boolean interrupted = false;

	//services needed
	private StreamUtil streamUtil;

	private Document dom;
	private File inputFile;
	private HashMap<String, EnrichmentResult> enrichmentresults;

	public ParseEDBEnrichmentResults(DataSet dataset, StreamUtil streamUtil) {

		this.dataset = dataset;
		this.streamUtil = streamUtil;

		this.EnrichmentResultFileName1 = dataset.getEnrichments().getFilename1();
		this.EnrichmentResultFileName2 = dataset.getEnrichments().getFilename2();

		//create a new enrichment results set
		enrichments = dataset.getEnrichments();
		results = enrichments.getEnrichments();
		upPhenotype = enrichments.getPhenotype1();
		downPhenotype = enrichments.getPhenotype2();

		this.enrichmentresults = new HashMap<String, EnrichmentResult>();

	}

	/**
	 * Parse enrichment results file
	 */

	public void parse() throws IOException {

		if(this.EnrichmentResultFileName1 != null && !this.EnrichmentResultFileName1.isEmpty())
			readFile(this.EnrichmentResultFileName1);
		if(this.EnrichmentResultFileName2 != null && !this.EnrichmentResultFileName2.isEmpty())
			readFile(this.EnrichmentResultFileName2);

	}

	/*
	 * Read file
	 */

	public void readFile(String EnrichmentResultFileName) throws IOException {

		this.inputFile = new File(EnrichmentResultFileName);
		try {
			this.results = parseDocument();
		} catch(ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//make sure the results are set in the dataset
		dataset.getEnrichments().setEnrichments(this.results);

	}

	private void parseFile() throws ParseException {
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();

			dom = db.parse(inputFile);

		} catch(ParserConfigurationException pce) {
			pce.printStackTrace();
		} catch(SAXException se) {
			se.printStackTrace();
			throw new ParseException(
					"Malformed EDB file: It looks like your EDB file was created by an older version of GSEA that Enrichment Map does not support. Please update your GSEA results files.",
					0);
		} catch(IOException ioe) {
			ioe.printStackTrace();
			throw new ParseException(
					"Malformed EDB file: It looks like your EDB file was created by an older version of GSEA that Enrichment Map does not support. Please update your GSEA results files.",
					0);
		}
	}

	public HashMap<String, EnrichmentResult> parseDocument() throws ParseException {

		parseFile();

		//get the root element
		Element docEle = dom.getDocumentElement();

		//get a nodelist of elements
		NodeList nl = docEle.getElementsByTagName("DTG");
		if(nl != null && nl.getLength() > 0) {
			for(int i = 0; i < nl.getLength(); i++) {

				//get the employee element
				Element el = (Element) nl.item(i);

				//get the Employee object
				EnrichmentResult e = getEnrichmentResult(el);

				//add it to list
				enrichmentresults.put(e.getName(), e);
			}
		}

		return enrichmentresults;
	}

	private GSEAResult getEnrichmentResult(Element el) {
		//for each Enrichment result get:
		//name - tag is GENESET but need to remove gene_sets.gmt# from the front
		String name = el.getAttribute("GENESET").replace("gene_sets.gmt#", "");
		//gsSize - geneset size.  Get value from the number of hits in the hit indices (HIT_INDICES)
		int gsSize = (el.getAttribute("HIT_INDICES") != null) ? el.getAttribute("HIT_INDICES").split(" ").length : 0;
		//ES - enrichment score
		double ES = (el.getAttribute("ES") != null) ? Double.parseDouble(el.getAttribute("ES")) : 0.0;
		//NES - normalized enrichment score
		double NES = (el.getAttribute("NES") != null) ? Double.parseDouble(el.getAttribute("NES")) : 0.0;
		//p-value - tag is NP
		double pvalue = (el.getAttribute("NP") != null) ? Double.parseDouble(el.getAttribute("NP")) : 1.0;
		//FDR - false discovery rate
		double FDR = (el.getAttribute("FDR") != null) ? Double.parseDouble(el.getAttribute("FDR")) : 1.0;
		//FWER - family wise error rate		
		double FWER = (el.getAttribute("FWER") != null) ? Double.parseDouble(el.getAttribute("FWER")) : 1.0;
		//rank_at_max - RANK_AT_ES
		double rank_at_max = (el.getAttribute("RANK_AT_ES") != null) ? Double.parseDouble(el.getAttribute("RANK_AT_ES"))
				: 0.0;

		//score_at_max - not in the edb file but it is just the NES
		double score_at_max = NES;

		GSEAResult result = new GSEAResult(name, gsSize, ES, NES, pvalue, FDR, FWER, (int) rank_at_max, score_at_max);

		return result;

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
		if(this.taskMonitor != null) {
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
		this.taskMonitor.setTitle("Parsing Enrichment Result file");

		parse();

	}
}
