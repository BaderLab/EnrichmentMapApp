package org.baderlab.csplugins.enrichmentmap;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.function.Function;

import org.baderlab.csplugins.enrichmentmap.actions.OpenPathwayCommonsTask;
import org.baderlab.csplugins.enrichmentmap.view.heatmap.HeatMapParams.Distance;
import org.cytoscape.property.CyProperty;

import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * Manages the CyProperties for EnrichmentMap.
 */
@Singleton
public class PropertyManager {
	
	public static class Property<T> {
		private final String key;
		public final T def;
		private final Function<String,T> converter;
		
		private Property(String key, T defaultValue, Function<String,T> converter) {
			this.key = key;
			this.def = defaultValue;
			this.converter = converter;
		}
	}
	
	public static final Property<Boolean> HEATMAP_AUTOFOCUS = new Property<>("heatmapAutofocus", false, Boolean::valueOf);
	public static final Property<Double> P_VALUE = new Property<>("default.pvalue", 1.0, Double::valueOf);
	public static final Property<Double> Q_VALUE = new Property<>("default.qvalue", 0.1, Double::valueOf);
	public static final Property<Boolean> CREATE_WARN = new Property<>("create.warn", true, Boolean::valueOf);
	public static final Property<Distance> DISTANCE_METRIC = new Property<>("default.distanceMetric", Distance.PEARSON, Distance::valueOf);
	public static final Property<String> PATHWAY_COMMONS_URL = new Property<>("pathway.commons.url", OpenPathwayCommonsTask.DEFAULT_BASE_URL, String::valueOf);
	
	@Inject private CyProperty<Properties> cyProps;
	
	@AfterInjection
	private void initializeProperties() {
		getAllProperties().stream()
		.filter(prop -> !cyProps.getProperties().containsKey(prop.key))
		.forEach(this::setDefault);
	}
	
	
	public <T> void setValue(Property<T> property, T value) {
		cyProps.getProperties().setProperty(property.key, String.valueOf(value));
	}
	
	public <T> void setDefault(Property<T> property) {
		setValue(property, property.def);
	}
	
	public <T> T getValue(Property<T> property) {
		if(cyProps == null) // happens in JUnits
			return property.def;
		Properties properties = cyProps.getProperties();
		if(properties == null)
			return property.def;
		String string = properties.getProperty(property.key);
		if(string == null)
			return property.def;
		
		try {
			return property.converter.apply(string);
		} catch(Exception e) {
			return property.def;
		}
	}
	
	@SuppressWarnings("rawtypes")
	public static List<Property<?>> getAllProperties() {
		List<Property<?>> properties = new ArrayList<>();
		for (Field field : PropertyManager.class.getDeclaredFields()) {
			if (Modifier.isStatic(field.getModifiers()) && field.getType().equals(Property.class)) {
				try {
					properties.add((Property) field.get(null));
				} catch (IllegalArgumentException | IllegalAccessException e) {
					e.printStackTrace();
				}
			}
		}
		return properties;
	}
	
}
