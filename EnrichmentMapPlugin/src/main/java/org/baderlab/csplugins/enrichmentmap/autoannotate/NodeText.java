package org.baderlab.csplugins.enrichmentmap.autoannotate;

import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

/**
 * Created by:
 * @author arkadyark
 * <p>
 * Date   Jun 16, 2014<br>
 * Time   01:09 PM<br>
 * <p>
 * Class to store the text attributes of a Node
 */

public final class NodeText {
	
	public String name;
	public String database;
	public String accession;
	public String definition;
	
	public String getName() {
		return name;
	}

	public String getDatabase() {
		return database;
	}
	
	public String getAccession() {
		// TODO - strip of the database
		return accession;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	@Override
	public String toString() {
		return name + " " + database + " " + accession;
	}
	
	public String findDefinition() {
		if (database.equals("GO")) {
			String url = "http://amigo.geneontology.org/amigo/term/" + accession;
			try {
				Document doc = Jsoup.connect(url).get();
				Elements definitions = doc.select("dt");
				for (Element e : definitions) {
					if (e.text().equals("Definition")) {
						TextNode definitionNode = (TextNode) e.nextSibling().nextSibling().childNode(0);
						return definitionNode.text();
					}
				}
			} catch (IOException e) {
				return "";
			}
			
		}
		return "";
	}
	
}