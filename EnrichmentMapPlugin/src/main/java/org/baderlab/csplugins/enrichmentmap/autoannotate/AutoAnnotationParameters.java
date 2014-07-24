/**
 * Created by
 * User: arkadyark
 * Date: Jul 24, 2014
 * Time: 9:29:43 AM
 */
package org.baderlab.csplugins.enrichmentmap.autoannotate;

import java.util.ArrayList;

import org.baderlab.csplugins.enrichmentmap.autoannotate.model.AnnotationSet;
import org.baderlab.csplugins.enrichmentmap.autoannotate.model.Cluster;

/* 
 * AutoAnnotation Parameters define all the variables that are needed to create, manipulate, explore
 * and save an individual AutoAnnotation session (potentially consisting of multiple annotation sets)
 * on a single network view.
 */
public class AutoAnnotationParameters {
	// network suid
	private long networkID;
	
	// used to generate the default names of the annotationSets
	private int annotationSetNumber;
	// used to select/deselect cluster
	private Cluster selectedCluster;
	// stores all of the annotation sets for this network view
	private ArrayList<AnnotationSet> annotationSets;	
	
	public AutoAnnotationParameters() {
		annotationSetNumber = 1;
		annotationSets = new ArrayList<AnnotationSet>();
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

	public Cluster getSelectedCluster() {
		return selectedCluster;
	}

	public void setSelectedCluster(Cluster selectedCluster) {
		this.selectedCluster = selectedCluster;
	}

	public ArrayList<AnnotationSet> getAnnotationSets() {
		return annotationSets;
	}
	
	public void addAnnotationSet(AnnotationSet annotationSet) {
		this.annotationSets.add(annotationSet);
	}
}
