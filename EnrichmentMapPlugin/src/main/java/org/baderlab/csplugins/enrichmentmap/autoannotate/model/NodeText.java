package org.baderlab.csplugins.enrichmentmap.autoannotate.model;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.TextNode;

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
	public String definition = "";
	
	public String getName() {
		return name;
	}

	
	public void setName(String name) {
		this.name = name;
	}
	
	@Override
	public String toString() {
		return name + definition;
	}
	
	public void findDefinitions(String source) {
		if (source.equals("GO")) {
			System.out.println(name);
			String url = "http://amigo2.berkeleybop.org/cgi-bin/amigo2/goose?query="
					+ "SELECT+term_definition.term_definition+FROM+term+INNER+JOIN+term_"
					+ "definition+ON+term.id%3Dterm_definition.term_id+WHERE+term.name%3D%22"
					+ name.replace(" ", "+")
					+ "%22";
			try {
				Document doc = Jsoup.connect(url).get();
				TextNode definitionNode = (TextNode) doc.select("td").get(0).childNode(0);
				definition = definitionNode.text();
				definition.replace("\n", "");
				definition.replace("\t", "");
				definition.replace(".", "");
			} catch (Exception e) {
				return;
			}
		}
	}
}