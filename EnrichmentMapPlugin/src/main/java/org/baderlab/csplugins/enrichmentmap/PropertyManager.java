package org.baderlab.csplugins.enrichmentmap;

import java.util.Properties;
import java.util.Set;

import org.baderlab.csplugins.enrichmentmap.model.EMCreationParameters.SimilarityMetric;
import org.baderlab.csplugins.enrichmentmap.view.heatmap.HeatMapParameters.DistanceMetric;
import org.cytoscape.property.CyProperty;
import org.cytoscape.session.CySessionManager;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

/**
 * Manages the CyProperties for EnrichmentMap.
 * 
 * MKTODO:
 * The CyProperties are currently not working. The defaults are hardcoded for the moment.
 */
@Singleton
public class PropertyManager {
	
	//Cytoscape default properties names
	public static final String defaultJaccardCutOff_propname = "EnrichmentMap.default_jaccard";
	public static final String defaultOverlapCutOff_propname = "EnrichmentMap.default_overlap";
	public static final String defaultSimilarityMetric_propname = "EnrichmentMap.default_similarity_metric";
	public static final String disable_heatmap_autofocus_propname = "EnrichmentMap.disable_heatmap_autofocus";

	//get the default heatmap sort algorithm
	public static final String defaultSortMethod_propname = "EnrichmentMap.default_sort_method";

	//get the default distance metric algorithm
	public static final String defaultDistanceMetric_propname = "EnrichmentMap.default_distance_metric";

	//assign the defaults:
	public static final String defaultPvalue_propname = "EnrichmentMap.default_pvalue";
	public static final String defaultQvalue_propname = "EnrichmentMap.default_qvalue";
	//get the default combined metric constant
	public static final String defaultCombinedConstant_propname = "EnrichmentMap.default_combinedConstant";
	
		
	
	@Inject private Provider<CySessionManager> sessionManagerProvider;

	//the set of default parameters we want to get 
	private double defaultJaccardCutOff = 0.25;
	private double defaultOverlapCutOff = 0.5;
	private double defaultCombinedCutOff = 0.375;
	private SimilarityMetric defaultSimilarityMetric = SimilarityMetric.OVERLAP;
	private DistanceMetric defaultDistanceMetric = DistanceMetric.PEARSON_CORRELATION;

	private double defaultPvalue = 0.005;
	private double defaultQvalue = 0.1;
	private double defaultCombinedConstant = 0.5;
	private boolean defaultDisableHeatmapAutofocus = false;
	
	
	
	public double getDefaultJaccardCutOff() {
		return defaultJaccardCutOff;
	}

	public double getDefaultOverlapCutOff() {
		return defaultOverlapCutOff;
	}
	
	public double getDefaultCombinedCutOff() {
		return defaultCombinedCutOff;
	}
	
	public double getDefaultCutOff(SimilarityMetric metric) {
		switch(metric) {
			default:
			case COMBINED: return defaultCombinedCutOff;
			case JACCARD:  return defaultJaccardCutOff;
			case OVERLAP:  return defaultOverlapCutOff;
		}
	}

	public SimilarityMetric getDefaultSimilarityMetric() {
		return defaultSimilarityMetric;
	}

	public DistanceMetric getDefaultDistanceMetric() {
		return defaultDistanceMetric;
	}

	public double getDefaultPvalue() {
		return defaultPvalue;
	}

	public double getDefaultQvalue() {
		return defaultQvalue;
	}

	public double getDefaultCombinedConstant() {
		return defaultCombinedConstant;
	}

	public boolean isDefaultDisableHeatmapAutofocus() {
		return defaultDisableHeatmapAutofocus;
	}


	// MKTODO This code doesn't make any sense, the properties should be in a single Properties object
	public void initializeDefaultParameters() {
		//get the session properties
		//only get the sessionProperties if the sessionManager is not null
		Set<CyProperty<?>> props = sessionManagerProvider.get().getCurrentSession().getProperties();

		//go through the session properties.
		//If the session property is there then get its value and put it in the default.
		for(CyProperty<?> prop : props) {
			String name = prop.getName();
			if(name != null) {
				Properties properties = ((CyProperty<Properties>) prop).getProperties();
				if(name.equals(defaultJaccardCutOff_propname)) {
					defaultJaccardCutOff = Double.valueOf((String)properties.getProperty(name));
				}
				if(name.equals(defaultOverlapCutOff_propname)) {
					defaultOverlapCutOff = Double.valueOf((String)properties.getProperty(name));
				}
				if(name.equals(defaultOverlapCutOff_propname)) {
					defaultOverlapCutOff = Double.valueOf((String)properties.getProperty(name));
				}
				if(name.equals(defaultPvalue_propname)) {
					defaultPvalue = Double.valueOf((String)properties.getProperty(name));
				}
				if(name.equals(defaultQvalue_propname)) {
					defaultQvalue = Double.valueOf((String)properties.getProperty(name));
				}
				if(name.equals(defaultCombinedConstant_propname)) {
					defaultCombinedConstant = Double.valueOf((String)properties.getProperty(name));
				}
				if(name.equals(defaultSimilarityMetric_propname)) {
					defaultSimilarityMetric = SimilarityMetric.valueOf(properties.getProperty(name));
				}
//				if(name.equals(defaultSortMethod_propname)) {
//					defaultSortMethod = HeatMapParameters.Sort.valueOf(properties.getProperty(name));
//				}
//				if(name.equals(defaultDistanceMetric_propname)) {
//					defaultDistanceMetric = (String)properties.getProperty(name);
//				}
				if(name.equals(disable_heatmap_autofocus_propname)) {
					defaultDisableHeatmapAutofocus = Boolean.valueOf(properties.getProperty(name));
				}
			}
		}
	}

}
