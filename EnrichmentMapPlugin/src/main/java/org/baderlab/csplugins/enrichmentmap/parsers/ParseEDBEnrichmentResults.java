package org.baderlab.csplugins.enrichmentmap.parsers;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.baderlab.csplugins.enrichmentmap.model.EMDataSet;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentResult;
import org.baderlab.csplugins.enrichmentmap.model.GSEAResult;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.google.common.base.Strings;

public class ParseEDBEnrichmentResults extends AbstractTask {

	private final EMDataSet dataset;

	public ParseEDBEnrichmentResults(EMDataSet dataset) {
		this.dataset = dataset;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		taskMonitor.setTitle("Parsing Enrichment Result file");
		
		String enrichmentFileName1 = dataset.getDataSetFiles().getEnrichmentFileName1();
		String enrichmentFileName2 = dataset.getDataSetFiles().getEnrichmentFileName2();
		
		if(!Strings.isNullOrEmpty(enrichmentFileName1))
			parse(enrichmentFileName1);
		
		if(!Strings.isNullOrEmpty(enrichmentFileName2))
			parse(enrichmentFileName2);
	}

	
	public void parse(String filePath) throws Exception {
		SAXParserFactory spf = SAXParserFactory.newInstance();
	    SAXParser saxParser = spf.newSAXParser();
	    EDBHandler handler = new EDBHandler();
	    
	    saxParser.parse(new File(filePath), handler);
	    
	    dataset.getEnrichments().setEnrichments(handler.enrichmentResults);
	}
	
	private static String deAccent(String str) {
//		String nfdNormalizedString = Normalizer.normalize(str, Normalizer.Form.NFD);
//		System.out.println(nfdNormalizedString);
//		Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
//		return pattern.matcher(nfdNormalizedString).replaceAll("");
		return str.replace("Â", "");
	}
	
	private class EDBHandler extends DefaultHandler {
		Map<String, EnrichmentResult> enrichmentResults = new HashMap<>();
		
		@Override
		public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
			if("DTG".equals(qName)) {
				//name - tag is GENESET but need to remove gene_sets.gmt# from the front
				String name = attributes.getValue("GENESET").replace("gene_sets.gmt#", "");
				name = deAccent(name);
				
				//gsSize - geneset size.  Get value from the number of hits in the hit indices (HIT_INDICES)
				String value = attributes.getValue("HIT_INDICES");
				int gsSize = (value != null) ? value.split(" ").length : 0;
				
				//ES - enrichment score
				String value2 = attributes.getValue("ES");
				double ES = (value2 != null) ? Double.parseDouble(value2) : 0.0;
				
				//NES - normalized enrichment score
				String value3 = attributes.getValue("NES");
				double NES = (value3 != null) ? Double.parseDouble(value3) : 0.0;
				
				//p-value - tag is NP
				String value4 = attributes.getValue("NP");
				double pvalue = (value4 != null) ? Double.parseDouble(value4) : 1.0;
				
				//FDR - false discovery rate
				String value5 = attributes.getValue("FDR");
				double FDR = (value5 != null) ? Double.parseDouble(value5) : 1.0;
				
				//FWER - family wise error rate		
				String value6 = attributes.getValue("FWER");
				double FWER = (value6 != null) ? Double.parseDouble(value6) : 1.0;
				
				//rank_at_max - RANK_AT_ES
				String value7 = attributes.getValue("RANK_AT_ES");
				double rankAtMax = (value7 != null) ? Double.parseDouble(value7) : 0.0;

				//score_at_max - not in the edb file but it is just the NES
				double scoreAtMax = NES;

				GSEAResult result = new GSEAResult(name, gsSize, ES, NES, pvalue, FDR, FWER, (int) rankAtMax, scoreAtMax);
				
				enrichmentResults.put(result.getName(), result);
			}
		}
	}

}
