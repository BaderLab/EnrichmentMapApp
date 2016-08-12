package org.baderlab.csplugins.enrichmentmap.parsers;

import java.io.File;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.baderlab.csplugins.enrichmentmap.model.DataSet;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentResult;
import org.baderlab.csplugins.enrichmentmap.model.GSEAResult;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.google.common.base.Strings;

public class ParseEDBEnrichmentResults extends AbstractTask {

	private final DataSet dataset;

	public ParseEDBEnrichmentResults(DataSet dataset) {
		this.dataset = dataset;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		taskMonitor.setTitle("Parsing Enrichment Result file");
		parse(taskMonitor);
	}

	public void parse(TaskMonitor taskMonitor) throws Exception {
		String enrichmentResultFileName1 = dataset.getEnrichments().getFilename1();
		String enrichmentResultFileName2 = dataset.getEnrichments().getFilename2();
		
		if(!Strings.isNullOrEmpty(enrichmentResultFileName1))
			readFile(enrichmentResultFileName1, taskMonitor);
		if(!Strings.isNullOrEmpty(enrichmentResultFileName2))
			readFile(enrichmentResultFileName2, taskMonitor);
	}
	

	public void readFile(String EnrichmentResultFileName, TaskMonitor taskMonitor) throws Exception {
		File inputFile = new File(EnrichmentResultFileName);
		HashMap<String, EnrichmentResult> results = parseDocument(inputFile);
		//make sure the results are set in the dataset
		dataset.getEnrichments().setEnrichments(results);
	}

	
	private Document parseFile(File inputFile) throws Exception {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		return db.parse(inputFile);
	}
	

	public HashMap<String, EnrichmentResult> parseDocument(File inputFile) throws Exception {
		Document dom = parseFile(inputFile);

		//get the root element
		Element docEle = dom.getDocumentElement();

		HashMap<String, EnrichmentResult> enrichmentresults = new HashMap<>();
		
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

}
