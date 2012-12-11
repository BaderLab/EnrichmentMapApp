package org.baderlab.csplugins.enrichmentmap.parsers;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentResult;
import org.baderlab.csplugins.enrichmentmap.model.GSEAResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class ParseEDBEnrichmentResults {
	
	private Document dom;
	private File inputFile;
	private HashMap<String, EnrichmentResult> enrichmentresults;
	
	public ParseEDBEnrichmentResults(File inputFile){

		this.inputFile = inputFile;
		this.enrichmentresults = new HashMap<String, EnrichmentResult>();
		
	}
	
	private void parseFile(){
		try{
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			
			dom = db.parse(inputFile);
			
		}catch(ParserConfigurationException pce) {
			pce.printStackTrace();
		}catch(SAXException se) {
			se.printStackTrace();
		}catch(IOException ioe) {
			ioe.printStackTrace();
		}
	}
	
	public HashMap<String,EnrichmentResult> parseDocument(){

		parseFile();
		
		//get the root element
		Element docEle = dom.getDocumentElement();

		//get a nodelist of elements
		NodeList nl = docEle.getElementsByTagName("DTG");
		if(nl != null && nl.getLength() > 0) {
			for(int i = 0 ; i < nl.getLength();i++) {

				//get the employee element
				Element el = (Element)nl.item(i);

				//get the Employee object
				EnrichmentResult e = getEnrichmentResult(el);

				//add it to list
				enrichmentresults.put(e.getName(), e);
			}
		}
		
		return enrichmentresults;
	}
	
	private GSEAResult getEnrichmentResult(Element el){
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
		double rank_at_max = (el.getAttribute("RANK_AT_ES") != null) ? Double.parseDouble(el.getAttribute("RANK_AT_ES")) : 0.0;
			
		//score_at_max - not in the edb file but it is just the NES
		double score_at_max = NES;
		
		GSEAResult result = new GSEAResult(name, gsSize,ES,NES,pvalue,FDR,FWER,(int)rank_at_max,score_at_max);
		
		return result;
		
	}

	
}
