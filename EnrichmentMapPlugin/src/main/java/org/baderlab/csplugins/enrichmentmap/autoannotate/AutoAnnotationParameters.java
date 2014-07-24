/**
 * Created by
 * User: arkadyark
 * Date: Jul 24, 2014
 * Time: 9:29:43 AM
 */
package org.baderlab.csplugins.enrichmentmap.autoannotate;

import java.util.TreeMap;

import org.cytoscape.model.CyTableManager;

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
	
	public AutoAnnotationParameters() {
		annotationSetNumber = 1;
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
}
