package org.baderlab.csplugins.enrichmentmap;

import java.util.Properties;
import java.util.function.Function;

import org.baderlab.csplugins.enrichmentmap.model.EMCreationParameters.SimilarityMetric;
import org.baderlab.csplugins.enrichmentmap.view.heatmap.HeatMapParams.Distance;
import org.cytoscape.property.CyProperty;

import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * Manages the CyProperties for EnrichmentMap.
 */
@Singleton
public class PropertyManager {
	
	public static final String heatmap_autofocus_propname = "heatmapAutofocus";
	public static final String jaccardCutOff_propname = "default.jaccardCutoff";
	public static final String overlapCutOff_propname = "default.overlapCutoff";
	public static final String combinedCutoff_propname = "default.combinedCutoff";
	public static final String combinedConstant_propname = "default.combinedConstant";
	public static final String similarityMetric_propname = "default.similarityMetric";
	public static final String distanceMetric_propname = "default.distanceMetric";
	public static final String Pvalue_propname = "default.pvalue";
	public static final String Qvalue_propname = "default.qvalue";
	public static final String create_warn_show_propname = "create.warn.show";
	
	
	private static final double jaccardCutOff_default = 0.25;
	private static final double overlapCutOff_default = 0.5;
	private static final double combinedCutoff_default = 0.375;
	private static final double combinedConstant_default = 0.5;
	private static final SimilarityMetric similarityMetric_default = SimilarityMetric.OVERLAP;
	private static final Distance distanceMetric_default = Distance.PEARSON;
	private static final double Pvalue_default = 1.0;
	private static final double Qvalue_default = 0.1;
	private static final boolean create_warn_show_default = true;
	
	@Inject private CyProperty<Properties> cyProps;
	
	@AfterInjection
	private void initializeProperties() {
		Properties props = cyProps.getProperties();
		if(props.size() < 10) {
			props.setProperty(heatmap_autofocus_propname, String.valueOf(false));
			props.setProperty(jaccardCutOff_propname, String.valueOf(jaccardCutOff_default));
			props.setProperty(overlapCutOff_propname, String.valueOf(overlapCutOff_default));
			props.setProperty(combinedCutoff_propname, String.valueOf(combinedCutoff_default));
			props.setProperty(combinedConstant_propname, String.valueOf(combinedConstant_default));
			props.setProperty(similarityMetric_propname, String.valueOf(similarityMetric_default));
			props.setProperty(distanceMetric_propname, String.valueOf(distanceMetric_default));
			props.setProperty(Pvalue_propname, String.valueOf(Pvalue_default));
			props.setProperty(Qvalue_propname, String.valueOf(Qvalue_default));
			props.setProperty(create_warn_show_propname, String.valueOf(create_warn_show_default));
			// remember to increase the number in the if-statement above
		}
	}
	
	public boolean getShowCreateWarnings() {
		return getValue(create_warn_show_propname, create_warn_show_default, Boolean::valueOf);
	}
	
	public void setShowCreateWarnings(boolean show) {
		cyProps.getProperties().setProperty(create_warn_show_propname, String.valueOf(show));
	}
	
	public double getJaccardCutoff() {
		return getValue(jaccardCutOff_propname, jaccardCutOff_default, Double::valueOf);
	}

	public double getOverlapCutoff() {
		return getValue(overlapCutOff_propname, overlapCutOff_default, Double::valueOf);
	}

	public double getCombinedCutoff() {
		return getValue(combinedCutoff_propname, combinedCutoff_default, Double::valueOf);
	}

	public double getCombinedConstant() {
		return getValue(combinedConstant_propname, combinedConstant_default, Double::valueOf);
	}

	public SimilarityMetric getSimilarityMetric() {
		return getValue(similarityMetric_propname, similarityMetric_default, SimilarityMetric::valueOf);
	}

	public Distance getDistanceMetric() {
		return getValue(distanceMetric_propname, distanceMetric_default, Distance::valueOf);
	}

	public double getPvalue() {
		return getValue(Pvalue_propname, Pvalue_default, Double::valueOf);
	}

	public double getQvalue() {
		return getValue(Qvalue_propname, Qvalue_default, Double::valueOf);
	}

	public boolean isHeatmapAutofocus() {
		return getValue(heatmap_autofocus_propname, false, Boolean::valueOf);
	}
	
	public void setHeatmapAutofocus(boolean autofocus) {
		cyProps.getProperties().setProperty(heatmap_autofocus_propname, String.valueOf(autofocus));
	}
	
	public double getDefaultCutOff(SimilarityMetric metric) {
		switch(metric) {
			default:
			case COMBINED: return getCombinedCutoff();
			case JACCARD:  return getJaccardCutoff();
			case OVERLAP:  return getOverlapCutoff();
		}
	}

	
	private <V> V getValue(String name, V defaultVal, Function<String,V> converter) {
		if(cyProps == null) // happens in JUnits
			return defaultVal;
		Properties props = cyProps.getProperties();
		if(props == null)
			return defaultVal;
		String s = props.getProperty(name);
		if(name == null)
			return defaultVal;
		try {
			return converter.apply(s);
		} catch(Exception e) {
			return defaultVal;
		}
	}


	

}
