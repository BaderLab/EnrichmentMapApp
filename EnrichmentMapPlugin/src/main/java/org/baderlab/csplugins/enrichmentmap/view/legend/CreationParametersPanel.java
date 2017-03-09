package org.baderlab.csplugins.enrichmentmap.view.legend;

import static org.baderlab.csplugins.enrichmentmap.view.util.SwingUtil.makeSmall;

import java.awt.BorderLayout;
import java.io.File;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;

import org.baderlab.csplugins.enrichmentmap.model.EMCreationParameters;
import org.baderlab.csplugins.enrichmentmap.model.EMCreationParameters.SimilarityMetric;
import org.baderlab.csplugins.enrichmentmap.model.EMDataSet;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;

@SuppressWarnings("serial")
public class CreationParametersPanel extends JPanel {

	private JTextPane infoPane;
	
	private final EnrichmentMap map;
	
	public CreationParametersPanel(EnrichmentMap map) {
		this.map = map;
		
		init();
	}

	private void init() {
		JScrollPane scrollPane = new JScrollPane(getInfoPane(), JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		makeSmall(scrollPane);
		
		setLayout(new BorderLayout());
		add(scrollPane, BorderLayout.CENTER);
	}
	
	private JTextPane getInfoPane() {
		if (infoPane == null) {
			infoPane = new JTextPane();
			infoPane.setEditable(false);
			infoPane.setContentType("text/html");
			infoPane.setText(getInfoText());
			makeSmall(infoPane);
		}
		
		return infoPane;
	}
	
	/**
	 * Get the files and parameters corresponding to the current enrichment map
	 */
	private String getInfoText() {
		EMCreationParameters params = map.getParams();
		
		final String INDENT = "&nbsp;&nbsp;&nbsp;&nbsp;";
		
		String s = "<html><font size='-2' face='sans-serif'>";

		s = s + "<b>P-value Cut-off:</b> " + params.getPvalue() + "<br>";
		s = s + "<b>FDR Q-value Cut-off:</b> " + params.getQvalue() + "<br>";

		if (params.getSimilarityMetric() == SimilarityMetric.JACCARD) {
			s = s + "<b>Jaccard Cut-off:</b> " + params.getSimilarityCutoff() + "<br>";
			s = s + "<b>Test used:</b> Jaccard Index<br>";
		} else if (params.getSimilarityMetric() == SimilarityMetric.OVERLAP) {
			s = s + "<b>Overlap Cut-off:</b> " + params.getSimilarityCutoff() + "<br>";
			s = s + "<b>Test Used:</b> Overlap Index<br>";
		} else if (params.getSimilarityMetric() == SimilarityMetric.COMBINED) {
			s = s + "<b>Jaccard Overlap Combined Cut-off:</b> " + params.getSimilarityCutoff() + "<br>";
			s = s + "<b>Test Used:</b> Jaccard Overlap Combined Index (k constant = " + params.getCombinedConstant() + ")<br>";
		}
		
		for (EMDataSet ds : map.getDataSetList()) {
			s = s + "<b>Data Sets</h4><b>";
			s = s + "<b>" + ds.getName() + "</b><br>";
			s = s + "<b>Gene Sets File: </b><br>"
					+ INDENT + shortenPathname(ds.getDataSetFiles().getGMTFileName()) + "<br>";
		
			String enrichmentFileName1 = ds.getDataSetFiles().getEnrichmentFileName1();
			String enrichmentFileName2 = ds.getDataSetFiles().getEnrichmentFileName2();
		
			if (enrichmentFileName1 != null || enrichmentFileName2 != null) {
				s = s + "<b>Data Files: </b><br>";
				
				if (enrichmentFileName1 != null)
					s = s + INDENT + shortenPathname(enrichmentFileName1) + "<br>";
				
				if (enrichmentFileName2 != null)
					s = s + INDENT + shortenPathname(enrichmentFileName2) + "<br>";
			}
			
//			if (LegacySupport.isLegacyTwoDatasets(map)) {
//				enrichmentFileName1 = map.getDataSet(LegacySupport.DATASET2).getDataSetFiles().getEnrichmentFileName1();
//				enrichmentFileName2 = map.getDataSet(LegacySupport.DATASET2).getDataSetFiles().getEnrichmentFileName2();
//				
//				if (enrichmentFileName1 != null || enrichmentFileName2 != null) {
//					s = s + "<b>Dataset 2 Data Files: </b><br>";
//					
//					if (enrichmentFileName1 != null)
//						s = s + INDENT + shortenPathname(enrichmentFileName1) + "<br>";
//					
//					if (enrichmentFileName2 != null)
//						s = s + INDENT + shortenPathname(enrichmentFileName2) + "<br>";
//				}
//			}
			
			s = s + "<b>Data file:</b>" + shortenPathname(ds.getDataSetFiles().getExpressionFileName()) + "<br>";
			// TODO:fix second dataset viewing.
			/*
			 * if(params.isData2() && params.getEM().getExpression(LegacySupport.DATASET2) != null)
			 * runInfoText = runInfoText + "<b>Data file 2:</b>" + shortenPathname(params.getExpressionFileName2()) + "<br>";
			 */
			
			if (ds != null && ds.getDataSetFiles().getGseaHtmlReportFile() != null)
				s = s + "<b>GSEA Report 1:</b>" + shortenPathname(ds.getDataSetFiles().getGseaHtmlReportFile()) + "<br>";
			
//			if (map.getDataSet(LegacySupport.DATASET2) != null
//					&& map.getDataSet(LegacySupport.DATASET2).getDataSetFiles().getGseaHtmlReportFile() != null) {
//				s = s + "<b>GSEA Report 2:</b>"
//						+ shortenPathname(map.getDataSet(LegacySupport.DATASET2).getDataSetFiles().getGseaHtmlReportFile()) + "<br>";
//			}
		}

		s = s + "</font></html>";
		
		return s;
	}
	
	/**
	 * Shorten path name to only contain the parent directory
	 */
	private static String shortenPathname(String pathname) {
		if (pathname != null) {
			String[] tokens = pathname.split("\\" + File.separator);

			int numTokens = tokens.length;
			final String newPathname;
			
			if (numTokens >= 2)
				newPathname = "..." + File.separator + tokens[numTokens - 2] + File.separator + tokens[numTokens - 1];
			else
				newPathname = pathname;

			return newPathname;
		}
		
		return "";
	}
}
