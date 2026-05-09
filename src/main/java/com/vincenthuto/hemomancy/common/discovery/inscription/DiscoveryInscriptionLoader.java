package com.vincenthuto.hemomancy.common.discovery.inscription;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DiscoveryInscriptionLoader
		extends SimplePreparableReloadListener<Map<ResourceLocation, DiscoveryInscriptionDefinition>> {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final Gson GSON = new GsonBuilder().create();
	private static final String FOLDER = "discovery_inscription";

	@Override
	protected Map<ResourceLocation, DiscoveryInscriptionDefinition> prepare(ResourceManager manager,
			ProfilerFiller profiler) {
		Map<ResourceLocation, DiscoveryInscriptionDefinition> result = new HashMap<>();
		Map<ResourceLocation, Resource> resources = manager.listResources(FOLDER,
				loc -> loc.getPath().endsWith(".json"));
		for (Map.Entry<ResourceLocation, Resource> entry : resources.entrySet()) {
			ResourceLocation file = entry.getKey();
			String path = file.getPath();
			String idPath = path.substring(FOLDER.length() + 1, path.length() - ".json".length());
			ResourceLocation id = ResourceLocation.fromNamespaceAndPath(file.getNamespace(), idPath);
			try (InputStream stream = entry.getValue().open();
				 InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
				JsonObject root = GSON.fromJson(reader, JsonObject.class);
				DiscoveryInscriptionDefinition definition = parse(id, root);
				result.put(id, definition);
			} catch (Exception ex) {
				LOGGER.error("[DiscoveryInscriptionLoader] Failed to parse {}: {}", file, ex.getMessage());
			}
		}
		LOGGER.info("[DiscoveryInscriptionLoader] Loaded {} discovery inscriptions", result.size());
		return result;
	}

	@Override
	protected void apply(Map<ResourceLocation, DiscoveryInscriptionDefinition> prepared,
			ResourceManager manager, ProfilerFiller profiler) {
		DiscoveryInscriptionRegistry.reload(prepared);
	}

	private static DiscoveryInscriptionDefinition parse(ResourceLocation id, JsonObject root) {
		DiscoveryInscriptionDefinition.Kind kind = DiscoveryInscriptionDefinition.Kind.valueOf(
				getString(root, "kind").toUpperCase(Locale.ROOT));
		DiscoveryInscriptionDefinition.Path path = DiscoveryInscriptionDefinition.Path.valueOf(
				getString(root, "path").toUpperCase(Locale.ROOT));
		ResourceLocation riteId = root.has("rite_id")
				? ResourceLocation.parse(root.get("rite_id").getAsString())
				: null;
		return new DiscoveryInscriptionDefinition(
				id,
				kind,
				path,
				root.get("required_level").getAsInt(),
				getString(root, "title"),
				getStringList(root.getAsJsonArray("obscured_text")),
				getStringList(root.getAsJsonArray("revealed_text")),
				getResourceList(root.getAsJsonArray("liber_entries")),
				riteId);
	}

	private static String getString(JsonObject root, String name) {
		return root.has(name) ? root.get(name).getAsString() : "";
	}

	private static List<String> getStringList(JsonArray array) {
		List<String> values = new ArrayList<>();
		if (array == null) return values;
		for (JsonElement element : array) {
			values.add(element.getAsString());
		}
		return values;
	}

	private static List<ResourceLocation> getResourceList(JsonArray array) {
		List<ResourceLocation> values = new ArrayList<>();
		if (array == null) return values;
		for (JsonElement element : array) {
			values.add(ResourceLocation.parse(element.getAsString()));
		}
		return values;
	}
}
