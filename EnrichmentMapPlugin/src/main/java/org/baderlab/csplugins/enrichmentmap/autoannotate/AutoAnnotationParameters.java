/**
 * Created by
 * User: arkadyark
 * Date: Jul 24, 2014
 * Time: 9:29:43 AM
 */
package org.baderlab.csplugins.enrichmentmap.autoannotate;

import java.util.ArrayList;
import java.util.Set;

import org.baderlab.csplugins.enrichmentmap.autoannotate.model.AnnotationSet;
import org.baderlab.csplugins.enrichmentmap.autoannotate.model.Cluster;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.presentation.annotations.AnnotationManager;

/* 
 * AutoAnnotation Parameters define all the variables that are needed to create, manipulate, explore
 * and save an individual AutoAnnotation session (potentially consisting of multiple annotation sets)
 * on a single network view.
 */
public class AutoAnnotationParameters {
	// network view that this belongs to
	private CyNetworkView networkView;
	// ID of this network view, used to get the view when loading from a file
	private long networkViewID;
	// used to generate the default names of the annotationSets
	private int annotationSetNumber;
	// used to select/deselect cluster
	private int selectedAnnotationSetIndex;
	// stores all of the annotation sets for this network view
	private ArrayList<AnnotationSet> annotationSets;	
	
	public AutoAnnotationParameters() {
		annotationSetNumber = 1;
		annotationSets = new ArrayList<AnnotationSet>();
	}
	
	public CyNetworkView getNetworkView() {
		return networkView;
	}
	
	public void setNetworkView(CyNetworkView networkView) {
		this.networkView = networkView;
		this.networkViewID = networkView.getSUID();
	}	

	public long getNetworkViewID() {
		return networkViewID;
	}

	public void setNetworkViewID(long networkViewID) {
		this.networkViewID = networkViewID;
	}
	
	public int getAnnotationSetNumber() {
		return annotationSetNumber;
	}

	public void setAnnotationSetNumber(int annotationSetNumber) {
		this.annotationSetNumber = annotationSetNumber;
	}
	
	public void incrementAnnotationSetNumber() {
		annotationSetNumber++;
	}

	public AnnotationSet getSelectedAnnotationSet() {
		return annotationSets.get(selectedAnnotationSetIndex);
	}

	public void setSelectedAnnotationSet(AnnotationSet selectedAnnotationSet) {
		int index = annotationSets.indexOf(selectedAnnotationSet);
		if (index != -1) {
			this.selectedAnnotationSetIndex = index;			
		}
	}

	public void setSelectedAnnotationSetIndex(int selectedAnnotationSetIndex) {
		this.selectedAnnotationSetIndex = selectedAnnotationSetIndex;
	}
	
	public ArrayList<AnnotationSet> getAnnotationSets() {
		return annotationSets;
	}
	
	public void addAnnotationSet(AnnotationSet annotationSet) {
		this.annotationSets.add(annotationSet);
	}
	
	public void removeAnnotationSet(AnnotationSet annotationSet) {
		this.annotationSets.remove(annotationSet);
	}

	public void load(String fullText, CyNetworkView view) {
		setNetworkView(view);
		String[] fileLines = fullText.split("\n");
		setAnnotationSetNumber(Integer.parseInt(fileLines[0]));
		setSelectedAnnotationSetIndex(Integer.parseInt(fileLines[1]));
		int lineNumber = 2;
		ArrayList<String> annotationSetLines = new ArrayList<String>();
		while (lineNumber < fileLines.length) {
			String line = fileLines[lineNumber];
			if (line.equals("End of annotation set")) {
				AnnotationSet annotationSet = new AnnotationSet();
				annotationSet.setView(view);
				annotationSet.load(annotationSetLines);
				annotationSets.add(annotationSet);
				annotationSetLines = new ArrayList<String>();
			} else {
				// Add to the growing list of lines for the annotation set
				annotationSetLines.add(line);
			}
			lineNumber++;
		}
	}

	public String toSessionString() {
		String sessionString = "";
		sessionString += annotationSetNumber + "\n";
		sessionString += selectedAnnotationSetIndex + "\n";
		for (AnnotationSet annotationSet : annotationSets) {
			sessionString += annotationSet.toSessionString();
		}
		return sessionString;
	}
}
