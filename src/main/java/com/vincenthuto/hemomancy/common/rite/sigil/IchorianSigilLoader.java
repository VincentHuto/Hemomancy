package com.vincenthuto.hemomancy.common.rite.sigil;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public final class IchorianSigilLoader
		extends SimplePreparableReloadListener<Map<ResourceLocation, IchorianSigilDefinition>> {
	private static final String FOLDER = "ichorian_sigil";
	private static final Logger LOGGER = LogManager.getLogger();
	private static final Gson GSON = new Gson();

	@Override
	protected Map<ResourceLocation, IchorianSigilDefinition> prepare(ResourceManager manager,
			ProfilerFiller profiler) {
		Map<ResourceLocation, IchorianSigilDefinition> loaded = new HashMap<>();
		manager.listResources(FOLDER, id -> id.getPath().endsWith(".json")).forEach((file, resource) -> {
			String path = file.getPath();
			ResourceLocation id = ResourceLocation.fromNamespaceAndPath(file.getNamespace(),
					path.substring(FOLDER.length() + 1, path.length() - 5));
			try (InputStreamReader reader =
						 new InputStreamReader(resource.open(), StandardCharsets.UTF_8)) {
				JsonObject root = GSON.fromJson(reader, JsonObject.class);
				var nodes = new ArrayList<IchorianSigilDefinition.Node>();
				for (JsonElement element : root.getAsJsonArray("nodes")) {
					var pair = element.getAsJsonArray();
					nodes.add(new IchorianSigilDefinition.Node(pair.get(0).getAsDouble(),
							pair.get(1).getAsDouble()));
				}
				loaded.put(id, new IchorianSigilDefinition(
						id,
						IchorianSigilDefinition.Kind.valueOf(root.get("kind").getAsString()
								.toUpperCase(Locale.ROOT)),
						root.has("tier") ? root.get("tier").getAsInt() : 1,
						Integer.decode(root.get("color").getAsString()),
						root.get("name").getAsString(),
						root.get("purpose").getAsString(),
						root.has("stability") ? root.get("stability").getAsInt() : 0,
						root.has("capacity_ml") ? root.get("capacity_ml").getAsInt() : 0,
						nodes));
			} catch (Exception exception) {
				LOGGER.error("Failed to load Ichorian Sigil {}: {}", file, exception.getMessage());
			}
		});
		return loaded;
	}

	@Override
	protected void apply(Map<ResourceLocation, IchorianSigilDefinition> prepared,
			ResourceManager manager, ProfilerFiller profiler) {
		IchorianSigilRegistry.reload(prepared);
		LOGGER.info("Loaded {} Ichorian Sigils", prepared.size());
	}
}
