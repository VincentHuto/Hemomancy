package com.vincenthuto.hemomancy.common.block.inscription;

import com.google.gson.*;
import com.vincenthuto.hemomancy.common.data.JsonResourceReloadListener;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

/** Discovery inscription definitions from {@code discovery_inscription/**.json}, keyed by file id. */
public class DiscoveryInscriptionLoader extends JsonResourceReloadListener<DiscoveryInscriptionDefinition> {
	private static final Logger LOGGER = LogManager.getLogger();

	public DiscoveryInscriptionLoader() {
		super("discovery_inscription", "discovery inscription", LOGGER, DiscoveryInscriptionLoader::parse,
				DiscoveryInscriptionRegistry::reload);
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
				getResourceList(root.getAsJsonArray("advancements")),
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
