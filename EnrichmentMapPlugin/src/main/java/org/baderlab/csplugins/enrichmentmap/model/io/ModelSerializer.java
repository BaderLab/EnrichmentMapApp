package org.baderlab.csplugins.enrichmentmap.model.io;

import java.lang.reflect.Type;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import org.baderlab.csplugins.enrichmentmap.model.EMDataSet;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentResult;
import org.baderlab.csplugins.enrichmentmap.model.GSEAResult;
import org.baderlab.csplugins.enrichmentmap.model.GenericResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class ModelSerializer {
	
	private static final Logger logger = LoggerFactory.getLogger(ModelSerializer.class);

	public static String serialize(EnrichmentMap map) {
		// When saving to the session file DO NOT enable pretty printing, the Cytoscape CSV parser is very slow for multi-line text
		Gson gson = new GsonBuilder()
				.registerTypeHierarchyAdapter(Path.class, new PathAdapter())
				.registerTypeAdapter(EnrichmentResult.class, new EnrichmentResultAdapter())
				//.setPrettyPrinting()  // for debug use ONLY
				.create();
		
		String json = gson.toJson(map);
		return json;
	}
	
	
	public static EnrichmentMap deserialize(String json) {
		Gson gson = new GsonBuilder()
				.registerTypeAdapter(BiMap.class, new BiMapAdapter())
				.registerTypeHierarchyAdapter(Path.class, new PathAdapter())
				.registerTypeAdapter(EnrichmentResult.class, new EnrichmentResultAdapter())
				.create();
		
		try {
			EnrichmentMap map = gson.fromJson(json, EnrichmentMap.class);
			for(EMDataSet dataset : map.getDataSetList()) {
				dataset.setParent(map);
			}
			return map;
		} catch(JsonParseException e) {
			logger.error(e.getMessage(), e);
			return null;
		}
	}
	
	private static class BiMapAdapter implements JsonDeserializer<BiMap<Integer,String>> {
        @Override
        public BiMap<Integer,String> deserialize(JsonElement json, Type type, JsonDeserializationContext context) {
            BiMap<Integer,String> mapping = HashBiMap.create();
            JsonObject object = (JsonObject) json;
            for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
            	Integer geneId = Integer.parseInt(entry.getKey());
                String geneName = entry.getValue().getAsString();
                mapping.put(geneId, geneName);
            }
            return mapping;
        }
	}
	
	private static class PathAdapter implements JsonDeserializer<Path>, JsonSerializer<Path> {
	    @Override
	    public Path deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context) {
	        return Paths.get(jsonElement.getAsString());
	    }

		@Override
	    public JsonElement serialize(Path path, Type type, JsonSerializationContext context) {
	        return new JsonPrimitive(path.toString());
	    }
	}
	
	// Note: This can be solved with RuntimeTypeAdapterFactory, but its not part of the default GSON distribution
	private static class EnrichmentResultAdapter implements JsonDeserializer<EnrichmentResult>, JsonSerializer<EnrichmentResult> {
		private static final String CLASSNAME = "classname";
		private static final String INSTANCE  = "instance";

		@Override
		public JsonElement serialize(EnrichmentResult src, Type typeOfSrc, JsonSerializationContext context) {
			JsonObject jsonObject = new JsonObject();
		    jsonObject.addProperty(CLASSNAME, src.getClass().getSimpleName());
		    jsonObject.add(INSTANCE, context.serialize(src));
		    return jsonObject;
		}
		
		@Override
		public EnrichmentResult deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
			JsonObject jsonObject = json.getAsJsonObject();
		    String className = ((JsonPrimitive)jsonObject.get(CLASSNAME)).getAsString();

		    switch(className) {
		    case "GenericResult":
		    	return context.deserialize(jsonObject.get(INSTANCE), GenericResult.class);
		    case "GSEAResult":
		    	return context.deserialize(jsonObject.get(INSTANCE), GSEAResult.class);
		    default:
		    	throw new JsonParseException("Unknown class: " + className);
		    }
		}
	}
}

