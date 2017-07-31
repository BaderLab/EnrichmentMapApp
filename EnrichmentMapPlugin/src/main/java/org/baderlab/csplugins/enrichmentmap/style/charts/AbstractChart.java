package org.baderlab.csplugins.enrichmentmap.style.charts;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.baderlab.csplugins.enrichmentmap.style.charts.json.ColorJsonDeserializer;
import org.baderlab.csplugins.enrichmentmap.style.charts.json.ColorJsonSerializer;
import org.baderlab.csplugins.enrichmentmap.style.charts.json.CyColumnIdentifierJsonDeserializer;
import org.baderlab.csplugins.enrichmentmap.style.charts.json.CyColumnIdentifierJsonSerializer;
import org.baderlab.csplugins.enrichmentmap.style.charts.json.Point2DJsonDeserializer;
import org.baderlab.csplugins.enrichmentmap.style.charts.json.Point2DJsonSerializer;
import org.baderlab.csplugins.enrichmentmap.style.charts.json.PropertiesJsonDeserializer;
import org.baderlab.csplugins.enrichmentmap.style.charts.json.PropertiesJsonSerializer;
import org.baderlab.csplugins.enrichmentmap.style.charts.json.Rectangle2DJsonDeserializer;
import org.baderlab.csplugins.enrichmentmap.style.charts.json.Rectangle2DJsonSerializer;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.presentation.customgraphics.CustomGraphicLayer;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics2;
import org.cytoscape.view.presentation.property.values.CyColumnIdentifier;
import org.cytoscape.view.presentation.property.values.CyColumnIdentifierFactory;
import org.cytoscape.view.presentation.property.values.MappableVisualPropertyValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

public abstract class AbstractChart<T extends CustomGraphicLayer>
		implements CyCustomGraphics2<T>, MappableVisualPropertyValue {

	public static final String COLORS = "cy_colors";
	public static final String COLOR_POINTS = "em_colorPoints";
	public static final String ORIENTATION = "cy_orientation";
	public static final String ROTATION = "cy_rotation";
	public static final String DATA_COLUMNS = "cy_dataColumns";
	public static final String ITEM_LABELS_COLUMN = "cy_itemLabelsColumn";
	public static final String ITEM_LABELS = "cy_itemLabels";
	public static final String ITEM_LABEL_FONT_SIZE = "cy_itemLabelFontSize";
	public static final String DOMAIN_LABELS_COLUMN = "cy_domainLabelsColumn";
	public static final String RANGE_LABELS_COLUMN = "cy_rangeLabelsColumn";
	public static final String DOMAIN_LABEL_POSITION = "cy_domainLabelPosition";
	public static final String AXIS_LABEL_FONT_SIZE = "cy_axisLabelFontSize";
	public static final String GLOBAL_RANGE = "cy_globalRange";
	public static final String AUTO_RANGE = "cy_autoRange";
	public static final String RANGE = "cy_range";
	public static final String SHOW_ITEM_LABELS = "cy_showItemLabels";
	public static final String SHOW_DOMAIN_AXIS = "cy_showDomainAxis";
	public static final String SHOW_RANGE_AXIS = "cy_showRangeAxis";
	public static final String SHOW_RANGE_ZERO_BASELINE = "cy_showRangeZeroBaseline";
	public static final String AXIS_WIDTH = "cy_axisWidth";
	public static final String AXIS_COLOR = "cy_axisColor";
	public static final String VALUES = "cy_values";
	public static final String BORDER_WIDTH = "cy_borderWidth";
	public static final String BORDER_COLOR = "cy_borderColor";
	
	protected Long id;
	protected float fitRatio = 0.9f;
	protected String displayName;
	protected int width = 50;
	protected int height = 50;
	
	private final Map<String, Object> properties;
	private ObjectMapper mapper;
	
	protected final CyServiceRegistrar serviceRegistrar;
	
	protected final Logger logger;
	
	protected AbstractChart(final String displayName, final CyServiceRegistrar serviceRegistrar) {
		logger = LoggerFactory.getLogger(this.getClass());
		this.displayName = displayName;
		this.properties = new HashMap<>();
		
		if (serviceRegistrar == null)
			throw new IllegalArgumentException("'serviceRegistrar' must not be null.");
		
		this.serviceRegistrar = serviceRegistrar;
	}
	
	protected AbstractChart(final String displayName, final String input, final CyServiceRegistrar serviceRegistrar) {
		this(displayName, serviceRegistrar);
		addProperties(parseInput(input));
	}
	
	protected AbstractChart(final AbstractChart<T> chart, final CyServiceRegistrar serviceRegistrar) {
		this(chart.getDisplayName(), serviceRegistrar);
		addProperties(chart.getProperties());
	}
	
	protected AbstractChart(final String displayName, final Map<String, Object> properties,
			final CyServiceRegistrar serviceRegistrar) {
		this(displayName, serviceRegistrar);
		addProperties(properties);
	}
	
	public abstract String getId();
	
	@Override
	public Long getIdentifier() {
		return id;
	}

	@Override
	public void setIdentifier(Long id) {
		this.id = id;
	}

	@Override
	public void setWidth(final int width) {
		this.width = width;
	}

	@Override
	public void setHeight(final int height) {
		this.height = height;
	}

	@Override
	public int getWidth() {
		return width;
	}

	@Override
	public int getHeight() {
		return height;
	}

	@Override
	public String getDisplayName() {
		return displayName;
	}

	@Override
	public void setDisplayName(final String displayName) {
		this.displayName = displayName;
	}

	@Override
	public float getFitRatio() {
		return fitRatio;
	}

	@Override
	public void setFitRatio(float fitRatio) {
		this.fitRatio = fitRatio;
	}

	@Override
	public String toString() {
		return displayName;
	}

	@Override
	public String toSerializableString() {
		String output = "";
		
		try {
			final ObjectMapper om = getObjectMapper();
			output = om.writeValueAsString(this.properties);
			output = getId() + ":" + output;
		} catch (JsonProcessingException e) {
			logger.error("Cannot create JSON from custom graphics", e);
		}
		
		return output;
	}
	
	@Override
	public Map<String, Object> getProperties() {
		final Map<String, Object> map = new LinkedHashMap<>();
		
		// Make sure the returned map does not contain types not exposed in the API
		for (final Entry<String, Object> entry : properties.entrySet()) {
			final String key = entry.getKey();
			Object value = entry.getValue();
			
			if (value instanceof Enum)
				value = value.toString();
			
			map.put(key, value);
		}
		
		return map;
	}
	
	public synchronized void set(final String key, Object value) {
		if (key == null)
			throw new IllegalArgumentException("'key' must not be null.");
		
		final Class<?> type = getSettingType(key);
		
		if (type != null) {
			if (value != null) {
				// It's OK; just take the value as it is.
				boolean correctType = 
						type == Array.class &&
						value.getClass().isArray() &&
						value.getClass().getComponentType() == getSettingElementType(key);
				correctType = correctType || type.isAssignableFrom(value.getClass());
				
				if (!correctType) {
					final ObjectMapper om = getObjectMapper();
					String json = value.toString();
					
					if (type != List.class) {
						try {
							json = om.writeValueAsString(value);
						} catch (JsonProcessingException e) {
							logger.error("Cannot parse JSON field " + key, e);
						}
					}
					
					value = PropertiesJsonDeserializer.readValue(key, json, om, this);
				}
			}
			
			properties.put(key, value);
		}
	}
	
	@Override
	public Set<CyColumnIdentifier> getMappedColumns() {
		final Set<CyColumnIdentifier> set = new HashSet<>();
		set.addAll(getList(DATA_COLUMNS, CyColumnIdentifier.class));
		
		if (get(SHOW_ITEM_LABELS, Boolean.class, Boolean.FALSE))
			set.addAll(getList(ITEM_LABELS_COLUMN, CyColumnIdentifier.class));
		
		if (get(SHOW_DOMAIN_AXIS, Boolean.class, Boolean.FALSE) && get(DOMAIN_LABELS_COLUMN, CyColumnIdentifier.class) != null)
			set.add(get(DOMAIN_LABELS_COLUMN, CyColumnIdentifier.class));
		
		if (get(SHOW_RANGE_AXIS, Boolean.class, Boolean.FALSE) && get(RANGE_LABELS_COLUMN, CyColumnIdentifier.class) != null)
			set.add(get(RANGE_LABELS_COLUMN, CyColumnIdentifier.class));
		
		return set;
	}
	
	@Override
	public String getSerializableString() {
		return toSerializableString();
	}
	
	@Override
	public void update() {
		// Doesn't need to do anything here, because charts are updated when layers are recreated.
	}
	
	public Map<String, List<Double>> getDataFromColumns(final CyNetwork network, final CyIdentifiable model,
			final List<CyColumnIdentifier> columnNames) {
		LinkedHashMap<String, List<Double>> data = new LinkedHashMap<>();
		final CyRow row = network.getRow(model);
		
		if (row == null)
			return data;

		final CyTable table = row.getTable();
		final List<Double> singleSeriesValues = new ArrayList<>();
		final StringBuilder singleSeriesKey = new StringBuilder();
		int singleSeriesIndex = -1;
		int count = 0;

		for (final CyColumnIdentifier colId : columnNames) {
			final CyColumn column = table.getColumn(colId.getColumnName());
			
			if (column == null)
				continue;
			
			final String colName = column.getName();
			final List<Double> values = new ArrayList<>();
			
			if (column.getType() == List.class) {
				// List Column: One column = one data series
				final Class<?> type = column.getListElementType();
				
				if (type == Double.class) {
					List<Double> list = row.getList(colName, Double.class);
					
					if (list != null)
						values.addAll(list);
				} else if (type == Integer.class) {
					List<Integer> list = row.getList(colName, Integer.class);
					
					if (list != null) {
						for (Integer i : list)
							values.add(i.doubleValue());
					}
				} else if (type == Long.class) {
					List<Long> list = row.getList(colName, Long.class);
					
					if (list != null) {
						for (Long l : list)
							values.add(l.doubleValue());
					}
				} else if (type == Float.class) {
					List<Float> list = row.getList(colName, Float.class);
					
					if (list != null) {
						for (Float f : list)
							values.add(f.doubleValue());
					}
				}
				
				data.put(colName, values);
			} else {
				// Single Column: All single columns together make only one data series
				final Class<?> type = column.getType();
				
				if (Number.class.isAssignableFrom(type)) {
					if (!row.isSet(colName)) {
						singleSeriesValues.add(Double.NaN);
					} else if (type == Double.class) {
						singleSeriesValues.add(row.get(colName, Double.class));
					} else if (type == Integer.class) {
						Integer i = row.get(colName, Integer.class);
						singleSeriesValues.add(i.doubleValue());
					} else if (type == Float.class) {
						Float f = row.get(colName, Float.class);
						singleSeriesValues.add(f.doubleValue());
					}
					
					singleSeriesKey.append(colName + ",");
					
					// The index of this data series is the index of the first single column
					if (singleSeriesIndex == -1)
						singleSeriesIndex = count;
				}
			}
			
			count++;
		}
		
		if (!singleSeriesValues.isEmpty()) {
			singleSeriesKey.deleteCharAt(singleSeriesKey.length() - 1);
			
			// To add the series of single columns into the correct position, we have to rebuild the data map
			final Set<Entry<String, List<Double>>> entrySet = data.entrySet();
			data = new LinkedHashMap<>();
			int i = 0;
			
			for (final Entry<String, List<Double>> entry : entrySet) {
				if (i == singleSeriesIndex)
					data.put(singleSeriesKey.toString(), singleSeriesValues);
				
				data.put(entry.getKey(), entry.getValue());
				i++;
			}
			
			if (!data.containsKey(singleSeriesKey.toString())) // (entrySet.isEmpty() || i >= entrySet.size())
				data.put(singleSeriesKey.toString(), singleSeriesValues);
		}

		return data;
	}
	
	@SuppressWarnings("unchecked")
	public List<String> getLabelsFromColumn(final CyNetwork network, final CyIdentifiable model,
			final CyColumnIdentifier columnId) {
		final List<String> labels = new ArrayList<>();
		final CyRow row = network.getRow(model);
		
		if (row != null && columnId != null) {
			final CyTable table = row.getTable();
			final CyColumn column = table.getColumn(columnId.getColumnName());
			
			if (column != null && column.getType() == List.class) {
				final Class<?> type = column.getListElementType();
				final List<?> values = row.getList(columnId.getColumnName(), type);
				
				if (type == String.class) {
					labels.addAll((List<String>) values);
				} else {
					for (Object obj : values)
						labels.add(obj.toString());
				}
			}
		}
		
		return labels;
	}
	
	/**
	 * @return The names of the data columns or an empty list if any of the data columns is of type List.
	 */
	protected List<String> getSingleValueColumnNames(final CyNetwork network, final CyIdentifiable model) {
		final List<String> names = new ArrayList<>();
		final CyRow row = network.getRow(model);
		
		if (row == null)
			return names;
		
		final List<CyColumnIdentifier> dataColumns = getList(DATA_COLUMNS, CyColumnIdentifier.class);
		final CyTable table = row.getTable();
		
		boolean invalid = false;
		
		for (final CyColumnIdentifier colId : dataColumns) {
			final CyColumn column = table.getColumn(colId.getColumnName());
			
			if (column == null || column.getType() == List.class) {
				// Not a single value column!
				invalid = true;
				break;
			}
			
			names.add(colId.getColumnName());
		}
		
		if (invalid)
			names.clear();
		
		return names;
	}

	protected Map<String, List<Double>> getData(final CyNetwork network, final CyIdentifiable model) {
		final Map<String, List<Double>> data;
		final List<Double> values = getList(VALUES, Double.class);
		
		if (values == null || values.isEmpty()) {
			final List<CyColumnIdentifier> dataColumns = getList(DATA_COLUMNS, CyColumnIdentifier.class);
			data = getDataFromColumns(network, model, dataColumns);
		} else {
			data = new HashMap<>();
			data.put("Values", values);
		}
		
		return data;
	}
	
	protected List<String> getItemLabels(final CyNetwork network, final CyIdentifiable model) {
		List<String> labels = getList(ITEM_LABELS, String.class);
		
		if (labels == null || labels.isEmpty()) {
			final CyColumnIdentifier labelsColumn = get(ITEM_LABELS_COLUMN, CyColumnIdentifier.class);
			labels = getLabelsFromColumn(network, model, labelsColumn);
		}
		
		return labels;
	}
	
	protected List<Color> getColors(final Map<String, List<Double>> data) {
		return getList(COLORS, Color.class);
	}
	
	protected List<Double> getColorPoints(final Map<String, List<Double>> data) {
		return getList(COLOR_POINTS, Double.class);
	}
	
	@SuppressWarnings("unchecked")
	public synchronized <S> S get(final String key, final Class<S> cls) {
		Object obj = properties.get(key);
		
		return obj != null && cls.isAssignableFrom(obj.getClass()) ? (S) obj : null;
	}
	
	public synchronized <S> S get(final String key, final Class<S> cls, final S defValue) {
		S value = get(key, cls);
		return value != null ? value : defValue;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public synchronized <S> List<S> getList(final String key, final Class<S> cls) {
		Object obj = properties.get(key);
		
		return obj instanceof List ? (List)obj : Collections.emptyList();
	}
	
	@SuppressWarnings("unchecked")
	public synchronized <S> S[] getArray(final String key, final Class<S> cls) {
		final Object obj = properties.get(key);
		S[] arr = null;
		
		try {
			arr = (obj != null && obj.getClass().isArray()) ? (S[])obj : null;
		} catch (ClassCastException e) {
			logger.error("Cannot cast property '" + key + "' to array.", e);
		}
		
		return arr;
	}
	
	public synchronized float[] getFloatArray(final String key) {
		final Object obj = properties.get(key);
		
		try {
			return (float[]) obj;
		} catch (ClassCastException e) {
			return null;
		}
	}
	
	public synchronized double[] getDoubleArray(final String key) {
		final Object obj = properties.get(key);
		
		try {
			return (double[]) obj;
		} catch (ClassCastException e) {
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	protected Map<String, Object> parseInput(final String input) {
		final Map<String, Object> props = new HashMap<>();
		
		if (input != null && !input.isEmpty()) {
			try {
				final ObjectMapper om = getObjectMapper();
				final Map<String, Object> map = om.readValue(input, Map.class);
				
				if (map != null) {
					for (final Entry<String, Object> entry : map.entrySet()) {
						final String key = entry.getKey();
						final Object value = entry.getValue();
						props.put(key, value);
					}
				}
			} catch (Exception e) {
				logger.error("Cannot parse JSON: " + input, e);
			}
		}
		
		return props;
	}
	
	public Class<?> getSettingType(final String key) {
		if (key.equalsIgnoreCase(DATA_COLUMNS)) return List.class;
		if (key.equalsIgnoreCase(VALUES)) return List.class;
		if (key.equalsIgnoreCase(ITEM_LABELS_COLUMN)) return CyColumnIdentifier.class;
		if (key.equalsIgnoreCase(ITEM_LABELS)) return List.class;
		if (key.equalsIgnoreCase(ITEM_LABEL_FONT_SIZE)) return Integer.class;
		if (key.equalsIgnoreCase(DOMAIN_LABELS_COLUMN)) return CyColumnIdentifier.class;
		if (key.equalsIgnoreCase(RANGE_LABELS_COLUMN)) return CyColumnIdentifier.class;
		if (key.equalsIgnoreCase(SHOW_ITEM_LABELS)) return Boolean.class;
		if (key.equalsIgnoreCase(SHOW_RANGE_AXIS)) return Boolean.class;
		if (key.equalsIgnoreCase(SHOW_DOMAIN_AXIS)) return Boolean.class;
		if (key.equalsIgnoreCase(SHOW_RANGE_ZERO_BASELINE)) return Boolean.class;
		if (key.equalsIgnoreCase(DOMAIN_LABEL_POSITION)) return LabelPosition.class;
		if (key.equalsIgnoreCase(AXIS_LABEL_FONT_SIZE)) return Integer.class;
		if (key.equalsIgnoreCase(AXIS_WIDTH)) return Float.class;
		if (key.equalsIgnoreCase(AXIS_COLOR)) return Color.class;
		if (key.equalsIgnoreCase(GLOBAL_RANGE)) return Boolean.class;
		if (key.equalsIgnoreCase(AUTO_RANGE)) return Boolean.class;
		if (key.equalsIgnoreCase(RANGE)) return List.class;
		if (key.equalsIgnoreCase(BORDER_WIDTH)) return Float.class;
		if (key.equalsIgnoreCase(BORDER_COLOR)) return Color.class;
		if (key.equalsIgnoreCase(COLORS)) return List.class;
		if (key.equalsIgnoreCase(COLOR_POINTS)) return List.class;
		if (key.equalsIgnoreCase(ORIENTATION)) return Orientation.class;
		if (key.equalsIgnoreCase(ROTATION)) return Rotation.class;
			
		return null;
	}
	
	public Class<?> getSettingElementType(final String key) {
		if (key.equalsIgnoreCase(DATA_COLUMNS)) return CyColumnIdentifier.class;
		if (key.equalsIgnoreCase(VALUES)) return Double.class;
		if (key.equalsIgnoreCase(ITEM_LABELS)) return String.class;
		if (key.equalsIgnoreCase(RANGE)) return Double.class;
		if (key.equalsIgnoreCase(COLORS)) return Color.class;
		if (key.equalsIgnoreCase(COLOR_POINTS)) return Double.class;
		
		return Object.class;
	}
	
	protected void addProperties(final Map<String, ?> properties) {
		if (properties != null) {
			for (final Entry<String, ?> entry : properties.entrySet()) {
				if (getSettingType(entry.getKey()) != null)
					set(entry.getKey(), entry.getValue());
			}
		}
	}
	
	protected void addJsonSerializers(final SimpleModule module) {
		module.addSerializer(new PropertiesJsonSerializer());
		module.addSerializer(new ColorJsonSerializer());
		module.addSerializer(new Point2DJsonSerializer());
		module.addSerializer(new Rectangle2DJsonSerializer());
		module.addSerializer(new CyColumnIdentifierJsonSerializer());
	}
	
	protected void addJsonDeserializers(final SimpleModule module) {
		module.addDeserializer(Map.class, new PropertiesJsonDeserializer(this));
		module.addDeserializer(Color.class, new ColorJsonDeserializer());
		module.addDeserializer(Point2D.class, new Point2DJsonDeserializer());
		module.addDeserializer(Rectangle2D.class, new Rectangle2DJsonDeserializer());
		
		final CyColumnIdentifierFactory colIdFactory = serviceRegistrar.getService(CyColumnIdentifierFactory.class);
		module.addDeserializer(CyColumnIdentifier.class, new CyColumnIdentifierJsonDeserializer(colIdFactory));
	}
	
	protected static float convertFontSize(final int size) {
		return size * 2.0f;
	}
	
	private ObjectMapper getObjectMapper() {
		// Lazy initialization of ObjectMapper, to make sure any other instance property is already initialized
		if (mapper == null) {
			final SimpleModule module = new SimpleModule();
			addJsonSerializers(module);
			addJsonDeserializers(module);
			
			mapper = new ObjectMapper();
			mapper.registerModule(module);
		}
		
		return mapper;
	}
}
