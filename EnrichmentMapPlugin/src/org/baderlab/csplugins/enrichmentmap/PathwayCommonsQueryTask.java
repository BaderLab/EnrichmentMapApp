/**
 **                       EnrichmentMap Cytoscape Plugin
 **
 ** Copyright (c) 2008-2009 Bader Lab, Donnelly Centre for Cellular and Biomolecular 
 ** Research, University of Toronto
 **
 ** Contact: http://www.baderlab.org
 **
 ** Code written by: Carl Song
 ** Authors: Carl Song, Daniele Merico, Ruth Isserlin, Oliver Stueker, Gary D. Bader
 **
 ** This library is free software; you can redistribute it and/or modify it
 ** under the terms of the GNU Lesser General Public License as published
 ** by the Free Software Foundation; either version 2.1 of the License, or
 ** (at your option) any later version.
 **
 ** This library is distributed in the hope that it will be useful, but
 ** WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 ** MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 ** documentation provided hereunder is on an "as is" basis, and
 ** University of Toronto
 ** has no obligations to provide maintenance, support, updates, 
 ** enhancements or modifications.  In no event shall the
 ** University of Toronto
 ** be liable to any party for direct, indirect, special,
 ** incidental or consequential damages, including lost profits, arising
 ** out of the use of this software and its documentation, even if
 ** University of Toronto
 ** has been advised of the possibility of such damage.  
 ** See the GNU Lesser General Public License for more details.
 **
 ** You should have received a copy of the GNU Lesser General Public License
 ** along with this library; if not, write to the Free Software Foundation,
 ** Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 **
 **/

package org.baderlab.csplugins.enrichmentmap;



import giny.model.Node;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;

import org.apache.commons.httpclient.HttpException;

import cytoscape.Cytoscape;
import cytoscape.data.CyAttributes;
import cytoscape.task.Task;
import cytoscape.task.TaskMonitor;

/**
 * @author carlsong
 * @deprecated This class is no longer in use. Kept for future references in 
 * 		case we come back to the "query by genelist" idea.
 */
public class  PathwayCommonsQueryTask implements Task {
	private Node node;
	private boolean queryByNodeName = true; //redundant
	private boolean biopax;
	private TaskMonitor taskMonitor;
	private boolean interrupted = false;
	
	PathwayCommonsQueryTask(Node node, boolean biopax) {
		this.node = node;
		this.biopax = biopax;
	}
	
	/* 
	 * Helper methods
	 */
	// interruption handler
	public boolean interrupted(){
		return interrupted;
	}
	// handles use of underscores in node labels
	private String clean(String nodeLabel){
		return nodeLabel.replaceAll("_", " ");
	}
	// The following three methods are deprecated. Keep for future reference.
	private List<String> getGeneList() {
		List<String> geneList = new LinkedList<String>();
		EnrichmentMapManager manager = EnrichmentMapManager.getInstance();
		EnrichmentMapParameters params = manager.getParameters(Cytoscape.getCurrentNetwork().getIdentifier());
		HashMap<String, GeneSet> geneSets = params.getGenesetsOfInterest();
		GeneSet thisSet = geneSets.get(node.toString());
        for(Iterator<Integer> i = thisSet.getGenes().iterator(); i.hasNext();)
            geneList.add(params.getGeneFromHashKey(i.next()));
        return geneList;
	}
	private String parseIDType(List<String> geneList) {
		String sample = geneList.get(0);
		if (Pattern.matches("^[0-9]+$", sample))
			return "ENTREZ_GENE";
		else if (Pattern.matches("^[A-Z][0-9]+$", sample))
			return "UNIPROT";
		else
			return "GENE_SYMBOL";
	}
	// Warning: HashMap algorithm does not work!! (HashMap.containsKey(Obj) cannot compare content of Obj)
	private String queryByGeneList() throws InterruptedException, HttpException, IOException {
		List<String> geneList = getGeneList();
		if (geneList.size() > 25)
			throw new InterruptedException("Geneset too large!");
		String queryString = "cmd=get_pathways&version=2.0&q=GENESET&input_id_type=ID_TYPE";
		String genes = geneList.toString();
		genes = genes.substring(1, genes.length()-2);
		queryString = queryString.replace("GENESET", genes);
		queryString = queryString.replace("ID_TYPE", parseIDType(geneList));
		PathwayCommonsWebAPI webAPI = new PathwayCommonsWebAPI();
        String result = webAPI.query(queryString);
        String[] lines = result.split("\n");
        HashMap<String, Integer> cpaths = new HashMap<String, Integer>(); 
        boolean header = true;
        for (String line : lines) {
        	String[] fields = line.split("\t");
        	if (fields.length == 4 && !header) {
        		String key = fields[3];
        		if (cpaths.containsKey(key))
        			cpaths.put(key, cpaths.get(key)+1);
        		else
        			cpaths.put(key, 1);
        	}
        	header = false;
        }
        if (cpaths.isEmpty())
        	throw new InterruptedException("No matching pathways found!");
        else {
        	int maxcount = 0;
        	String cpathid = null;
        	for (String key : cpaths.keySet()) {
        		if (cpaths.get(key) >= maxcount) {
        			maxcount = cpaths.get(key);
        			cpathid = key;
        		}
        		System.out.println(cpathid + "\t" + maxcount);
        	}
        	return cpathid;
        }
	}
	// Retrieve top matching cpath_id by node label. The matching algorithm may need to be updated frequently
	private String queryByNodeName() throws InterruptedException, HttpException, IOException {
		String queryString = "version=3.0&q=PATHWAY_NAME&format=html&cmd=get_by_keyword&snapshot_id=GLOBAL_FILTER_SETTINGS&record_type=PATHWAY";
		queryString = queryString.replace("PATHWAY_NAME", clean(node.getIdentifier()));
		PathwayCommonsWebAPI webAPI = new PathwayCommonsWebAPI();
        String result = webAPI.query(queryString);
        Pattern pattern = Pattern.compile("id=(\\d+)\">Pathway:");
        Matcher matcher = pattern.matcher(result);
        if (matcher.find())
        	return matcher.group(1);
        else
        	throw new InterruptedException("Pathway not found!");
	}
	// Interface methods
	public String getTitle() {
		return "Loading Pathway from PathwayCommons Database";
	}

	public void halt() {
		interrupted = true;
		taskMonitor.setPercentCompleted(100);
		taskMonitor.setStatus("Failed!!!");
	}
	
	public void run() {
		taskMonitor.setStatus("Querying PathwayCommons Database...");
		try {
			taskMonitor.setPercentCompleted(0);
			CyAttributes cyAttributes = Cytoscape.getNodeAttributes();
			PathwayCommonsImportTask importNetwork;
			String cpath_id;
			try {
				cpath_id = cyAttributes.getAttribute(node.getIdentifier(), "cpath_id").toString();
				importNetwork = new PathwayCommonsImportTask(cpath_id, biopax);
			} catch (NullPointerException e) {

				Cytoscape.getDesktop();
				JOptionPane.showMessageDialog(null, "cpath_id not found! Proceed query by node name?");
				if (queryByNodeName)
					cpath_id = queryByNodeName();
				else
					cpath_id = queryByGeneList(); // never called
				taskMonitor.setStatus("Retrieving best matching pathway...");
				taskMonitor.setPercentCompleted(0);
				importNetwork = new PathwayCommonsImportTask(cpath_id, biopax);
			}
			importNetwork.run();
			taskMonitor.setPercentCompleted(100);
			taskMonitor.setStatus("Pathway network loaded.");
		} catch (Exception e) {
			//JOptionPane.showMessageDialog(null, e.getMessage()); // needs correct parent Frame
			System.out.println(e.getMessage());
			e.printStackTrace();
			halt();
		}
		System.out.println("Task Complete");
	}

	public void setTaskMonitor(TaskMonitor taskMonitor)
			throws IllegalThreadStateException {
		this.taskMonitor = taskMonitor;
	}
	
}