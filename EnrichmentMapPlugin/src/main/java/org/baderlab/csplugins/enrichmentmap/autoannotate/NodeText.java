package org.baderlab.csplugins.enrichmentmap.autoannotate;

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
	
	
	public NodeText (String name, String database, String accession) {
		this.name = name;
		this.database = database;
		this.accession = accession;
		// this.description = getDescription(database, accession);
	}
	
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
	
	@Override
	public String toString() {
		return name + " " + database + " " + accession;
	}
	
}