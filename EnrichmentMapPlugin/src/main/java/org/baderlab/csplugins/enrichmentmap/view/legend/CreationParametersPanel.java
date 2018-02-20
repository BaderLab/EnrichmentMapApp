package org.baderlab.csplugins.enrichmentmap.view.legend;

import static org.baderlab.csplugins.enrichmentmap.view.util.SwingUtil.makeSmall;

import java.awt.BorderLayout;
import java.io.File;
import java.io.StringWriter;
import java.util.Map;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.text.html.HTMLDocument;

import org.baderlab.csplugins.enrichmentmap.model.EMCreationParameters;
import org.baderlab.csplugins.enrichmentmap.model.EMCreationParameters.SimilarityMetric;
import org.baderlab.csplugins.enrichmentmap.model.EMDataSet;
import org.baderlab.csplugins.enrichmentmap.model.EMSignatureDataSet;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;

import com.googlecode.jatl.Html;

@SuppressWarnings("serial")
public class CreationParametersPanel extends JPanel {

	private static final String INDENT = "&nbsp;&nbsp;&nbsp;&nbsp;";

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

	JTextPane getInfoPane() {
		if (infoPane == null) {
			infoPane = new JTextPane();
			infoPane.setEditable(false);
			infoPane.setContentType("text/html");
			((HTMLDocument) infoPane.getDocument()).setPreservesUnknownTags(false);
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
		StringWriter writer = new StringWriter();

		new Html(writer) {
			{
				bind("bold", "font-weight: bold;");
				bind("code", "font-family: Courier,monospaced;");

				html().body().style("font-family: Helvetica,Arial,sans-serif; font-size: 1em;");
					ol();
						addTitle("Cut-Off Values");
						ul();
							addCutOffItem("P-value", params.getPvalue());
							addCutOffItem("FDR Q-value", params.getQvalue());
		
							if (params.getSimilarityMetric() == SimilarityMetric.JACCARD)
								addCutOffItem("Jaccard", params.getSimilarityCutoff(), "Jaccard Index");
							else if (params.getSimilarityMetric() == SimilarityMetric.OVERLAP)
								addCutOffItem("Overlap", params.getSimilarityCutoff(), "Overlap Index");
							else if (params.getSimilarityMetric() == SimilarityMetric.COMBINED)
								addCutOffItem("Jaccard Overlap Combined", params.getSimilarityCutoff(),
										"Jaccard Overlap Combined Index (k constant = " + params.getCombinedConstant() + ")");
		
						end();
						addTitle("Data Sets");
						ol();
							map.getDataSetList().forEach(this::addDataSet);
						end();
						addTitle("Signature Data Sets");
						ol();
							map.getSignatureSetList().forEach(this::addSignatureDataSet);
						end();
					end();
				endAll();
				done();
			}

			Html addTitle(String s) {
				return li().style("${bold} font-size: 1.1em;").text(s + ": ").end();
			}

			Html addCutOffItem(String k, Object v) {
				return addCutOffItem(k, v, null);
			}

			Html addCutOffItem(String k, Object v, String test) {
				li().b().text(k + ": ").end().span().style("${code}").text("" + v).end();

				if (test != null)
					br().span().style("${padding}").text("Test used: ").i().text(test).end().end();

				return end();
			}

			Html addDataSet(EMDataSet ds) {
				li().b().text(ds.getName()).end();
				ul();
					li().text("Gene Sets File: ").span().style("${code}")
						.text(shortenPathname(ds.getDataSetFiles().getGMTFileName())).end().end();
	
					String ef1 = ds.getDataSetFiles().getEnrichmentFileName1();
					String ef2 = ds.getDataSetFiles().getEnrichmentFileName2();
	
					if (ef1 != null || ef2 != null) {
						li().text("Data Files:");
	
						if (ef1 != null)
							br().span().style("${code}").raw(INDENT).text(shortenPathname(ef1)).end();
						if (ef2 != null)
							br().span().style("${code}").raw(INDENT).text(shortenPathname(ef2)).end();
	
						end();
					}
					if (ds.getDataSetFiles().getExpressionFileName() != null) {
						li().text("Expression File: ").span().style("${code}")
							.text(shortenPathname(ds.getDataSetFiles().getExpressionFileName())).end().end();
					}
					if (ds.getDataSetFiles().getGseaHtmlReportFile() != null) {
						li().text("GSEA Report: ").span().style("${code}")
							.text(shortenPathname(ds.getDataSetFiles().getGseaHtmlReportFile())).end().end();
					}

				end();

				return end();
			}
			
			Html addSignatureDataSet(EMSignatureDataSet ds) {
				li().b().text(ds.getName()).end();
				
				ul();
					if (ds.getGmtFile() != null) {
						li().text("Gene Sets File: ").span().style("${code}")
							.text(shortenPathname(ds.getGmtFile())).end().end();
					}
					if (ds.getSource() != null) {
						li().text("Source: ").span().style("${code}")
							.text(ds.getSource()).end().end();
					}
					if (ds.getType() != null) {
						li().text("Filter Type: ").span().style("${code}")
							.text(ds.getType().toString()).end().end();
					}
					if (ds.getDataSetRankTestMessage() != null) {
						ul();
							Map<String,String> dataSetMessages = ds.getDataSetRankTestMessage();
							for(String dsName : dataSetMessages.keySet()) {
								String message = dataSetMessages.get(dsName);
								li().text(dsName + ": ").span().style("${code}")
									.text(message).end().end();
							}
						end();
					}
	
				end();
				return end();
			}
		};

		return writer.getBuffer().toString();
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
