package com.vincenthuto.hemomancy.common.worldgen;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.vincenthuto.hemomancy.support.RepoPaths;
import com.vincenthuto.hemomancy.support.ResourceReader;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * F-24: every template a Hemomancy pool places has a committed {@code .nbt}, and every {@code has_structure} biome tag
 * names a biome. The listed exceptions are kept on purpose for future templates (owner decision, step 13) and cannot
 * generate until then; finishing one means removing it here.
 */
class StructureTemplateCoverageTest {
	private static final Map<String, String> TEMPLATES_NOT_YET_AUTHORED = Map.of(
			"hemomancy:bog_body_ossuary_niche", "structure set kept, template not authored",
			"hemomancy:crimson_lodge_annex", "structure set kept, template not authored",
			"hemomancy:sanguine_surveyor_bivouac", "structure set kept, template not authored",
			"hemomancy:circus_pavilion", "orphan pool: the pavilion structure starts from crimson_troupe_pavilion");
	private static final Set<String> EMPTY_BIOME_TAGS = Set.of("mausoleum");

	@Test
	void everyPooledTemplateIsCommitted() throws Exception {
		Map<String, String> missing = new TreeMap<>();
		try (Stream<Path> pools = Files.walk(RepoPaths.DATA.resolve("worldgen/template_pool"))) {
			for (Path pool : pools.filter(path -> path.toString().endsWith(".json")).toList()) {
				String relative = RepoPaths.DATA.relativize(pool).toString().replace('\\', '/');
				for (JsonElement entry : ResourceReader.data(relative).getAsJsonArray("elements")) {
					collectMissing(entry.getAsJsonObject().getAsJsonObject("element"), missing);
				}
			}
		}
		assertEquals(new TreeSet<>(TEMPLATES_NOT_YET_AUTHORED.keySet()), missing.keySet(),
				"Commit the template, or list it with its reason");
	}

	@Test
	void onlyTheListedBiomeTagsAreEmpty() throws Exception {
		Set<String> empty = new TreeSet<>();
		try (Stream<Path> tags = Files.list(RepoPaths.DATA.resolve("tags/worldgen/biome/has_structure"))) {
			for (Path tag : tags.toList()) {
				String name = tag.getFileName().toString().replace(".json", "");
				JsonArray values = ResourceReader.data("tags/worldgen/biome/has_structure/" + name + ".json")
						.getAsJsonArray("values");
				if (values == null || values.isEmpty()) empty.add(name);
			}
		}
		assertEquals(new TreeSet<>(EMPTY_BIOME_TAGS), empty);
	}

	private static void collectMissing(JsonObject element, Map<String, String> missing) {
		if (element.has("elements")) {
			for (JsonElement child : element.getAsJsonArray("elements")) collectMissing(child.getAsJsonObject(), missing);
		}
		if (!element.has("location") || !element.get("location").isJsonPrimitive()) return;
		String location = element.get("location").getAsString();
		if (!location.startsWith("hemomancy:")) return;
		if (!Files.isRegularFile(RepoPaths.DATA.resolve("structure/" + location.substring("hemomancy:".length()) + ".nbt"))) {
			missing.put(location, location);
		}
	}
}
