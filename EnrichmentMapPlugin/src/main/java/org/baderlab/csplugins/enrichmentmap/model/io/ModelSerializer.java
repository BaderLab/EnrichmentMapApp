package org.baderlab.csplugins.enrichmentmap.model.io;

import java.lang.reflect.Type;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import org.baderlab.csplugins.enrichmentmap.model.EMDataSet;
import org.baderlab.csplugins.enrichmentmap.model.EMSignatureDataSet;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentResult;
import org.baderlab.csplugins.enrichmentmap.model.GSEAResult;
import org.baderlab.csplugins.enrichmentmap.model.GenericResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableSet;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;

public class ModelSerializer {

	private static final Logger logger = LoggerFactory.getLogger(ModelSerializer.class);

	
	public static EnrichmentMap deepCopy(EnrichmentMap map) {
		// This could be done with less memory probably.
		return deserialize(serialize(map));
	}
	
	public static String serialize(EnrichmentMap map) {
		return serialize(map, false);
	}
	
	public static String serialize(EnrichmentMap map, boolean pretty) {
		// When saving to the session file DO NOT enable pretty printing, the Cytoscape
		// CSV parser is very slow for multi-line text
		GsonBuilder builder = new GsonBuilder()
				.registerTypeHierarchyAdapter(Path.class, new PathAdapter())
				.registerTypeAdapter(EnrichmentResult.class, new EnrichmentResultAdapter())
				.serializeSpecialFloatingPointValues(); // really important, we allow NaN in expression files

		if (pretty) {
			builder.setPrettyPrinting();
		}

		Gson gson = builder.create();
		String json = gson.toJson(map);
		return json;
	}

	public static EnrichmentMap deserialize(String json) {
		Type immutableIntSetType = new TypeToken<ImmutableSet<Integer>>() {}.getType();

		Gson gson = new GsonBuilder()
				.registerTypeAdapter(BiMap.class, new BiMapAdapter())
				.registerTypeHierarchyAdapter(Path.class, new PathAdapter())
				.registerTypeAdapter(EnrichmentResult.class, new EnrichmentResultAdapter())
				.registerTypeAdapter(immutableIntSetType, new ImmutableIntSetAdapter()).create();

		try {
			EnrichmentMap map = gson.fromJson(json, EnrichmentMap.class);
			for (EMDataSet dataset : map.getDataSetList()) {
				dataset.setParent(map);
			}
			for (EMSignatureDataSet dataset : map.getSignatureSetList()) {
				dataset.setParent(map);
			}
			return map;
		} catch (JsonParseException e) {
			logger.error(e.getMessage(), e);
			return null;
		}
	}

	private static class BiMapAdapter implements JsonDeserializer<BiMap<Integer, String>> {
		@Override
		public BiMap<Integer, String> deserialize(JsonElement json, Type type, JsonDeserializationContext context) {
			BiMap<Integer, String> mapping = HashBiMap.create();
			JsonObject object = (JsonObject) json;
			for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
				Integer geneId = Integer.parseInt(entry.getKey());
				String geneName = entry.getValue().getAsString();
				mapping.put(geneId, geneName);
			}
			return mapping;
		}
	}

	private static class ImmutableIntSetAdapter implements JsonDeserializer<ImmutableSet<Integer>> {
		@Override
		public ImmutableSet<Integer> deserialize(JsonElement json, Type type, JsonDeserializationContext context) {
			ImmutableSet.Builder<Integer> builder = ImmutableSet.builder();
			JsonArray array = (JsonArray) json;
			for (JsonElement element : array) {
				int gene = element.getAsInt();
				builder.add(gene);
			}
			return builder.build();
		}
	}

	public static class PathAdapter implements JsonDeserializer<Path>, JsonSerializer<Path> {
		@Override
		public Path deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context) {
			return Paths.get(jsonElement.getAsString());
		}

		@Override
		public JsonElement serialize(Path path, Type type, JsonSerializationContext context) {
			return new JsonPrimitive(path.toString());
		}
	}

	// Note: This can be solved with RuntimeTypeAdapterFactory, but its not part of
	// the default GSON distribution
	private static class EnrichmentResultAdapter implements JsonDeserializer<EnrichmentResult>, JsonSerializer<EnrichmentResult> {
		private static final String CLASSNAME = "classname";
		private static final String INSTANCE = "instance";

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
			String className = ((JsonPrimitive) jsonObject.get(CLASSNAME)).getAsString();

			switch (className) {
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
