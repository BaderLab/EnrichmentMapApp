package org.baderlab.csplugins.enrichmentmap.style.charts.json;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.baderlab.csplugins.enrichmentmap.style.charts.AbstractChart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.ArrayType;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.TypeFactory;

public class PropertiesJsonDeserializer extends JsonDeserializer<Map<String, Object>> {

	private final AbstractChart<?> chart; // TODO remove circular dependency
	
	private static final Logger logger = LoggerFactory.getLogger(PropertiesJsonDeserializer.class);

	public PropertiesJsonDeserializer(final AbstractChart<?> chart) {
		this.chart = chart;
	}

	@Override
	public Map<String, Object> deserialize(final JsonParser jp, final DeserializationContext ctxt)
			throws IOException, JsonProcessingException {
		final Map<String, Object> props = new LinkedHashMap<>();
		
		final ObjectMapper mapper = (ObjectMapper) jp.getCodec();
        final JsonNode rootNode = mapper.readTree(jp);
        
        if (rootNode.isObject()) {
	        final Iterator<String> fieldNames = rootNode.fieldNames();
	        
	        while (fieldNames.hasNext()) {
	        	final String key = fieldNames.next();
	        	final JsonNode jn = rootNode.get(key);
	        	final Object value = readValue(key, jn.toString(), mapper, chart);
		        props.put(key, value);
	        }
        }
        
		return props;
	}
	
	public static Object readValue(final String key,
								   final String input,
								   final ObjectMapper mapper,
								   final AbstractChart<?> chart) {
		Object value = null;
		final Class<?> type = chart.getSettingType(key);
		
    	if (type != null) {
			final TypeFactory typeFactory = mapper.getTypeFactory();
			
			try {
				if (type == Array.class) {
					final Class<?> elementType = chart.getSettingElementType(key);
					
					if (elementType != null) {
		        		final ArrayType arrType = typeFactory.constructArrayType(elementType);
		        		
		        		if (mapper.canDeserialize(arrType))
		        			value = mapper.readValue(input, arrType);
		    		}
				} else if (List.class.isAssignableFrom(type)) {
		    		final Class<?> elementType = chart.getSettingElementType(key);
		    		
		    		if (elementType != null) {
		        		final CollectionType collType = typeFactory.constructCollectionType(List.class, elementType);
		        		
		        		if (mapper.canDeserialize(collType))
		        			value = mapper.readValue(input, collType);
		    		}
		    	} else {
		    		final JavaType simpleType = typeFactory.constructSimpleType(type, new JavaType[]{});
		    		
		    		if (mapper.canDeserialize(simpleType))
		    			value = mapper.readValue(input, simpleType);
		    	}
			} catch (Exception e) {
        		logger.error("Cannot parse JSON field " + key, e);
        	}
    	}
		
		return value;
	}
}
