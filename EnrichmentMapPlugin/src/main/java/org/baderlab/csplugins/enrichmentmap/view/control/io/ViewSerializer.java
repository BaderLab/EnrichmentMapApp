package org.baderlab.csplugins.enrichmentmap.view.control.io;

import java.lang.reflect.Type;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class ViewSerializer {
	
	private static final Logger logger = LoggerFactory.getLogger(ViewSerializer.class);

	public static String serialize(ViewParams params) {
		// When saving to the session file DO NOT enable pretty printing,
		// the Cytoscape CSV parser is very slow for multi-line text
		Gson gson = new GsonBuilder()
				.registerTypeHierarchyAdapter(Path.class, new PathAdapter())
				//.setPrettyPrinting()  // for debug use ONLY
				.create();
		
		return gson.toJson(params);
	}
	
	public static ViewParams deserialize(String json) {
		Gson gson = new GsonBuilder()
				.registerTypeHierarchyAdapter(Path.class, new PathAdapter())
				.create();
		
		try {
			ViewParams params = gson.fromJson(json, ViewParams.class);
			
			return params;
		} catch(JsonParseException e) {
			logger.error(e.getMessage(), e);
			return null;
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
}

