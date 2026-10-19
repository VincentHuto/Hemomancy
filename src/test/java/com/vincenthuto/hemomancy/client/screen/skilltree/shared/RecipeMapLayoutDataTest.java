package com.vincenthuto.hemomancy.client.screen.skilltree.shared;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.vincenthuto.hemomancy.common.rite.CardinalRitePurpose;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Lays out the authored Harbinger Rites &amp; Crafts map from the shipped recipe data. */
final class RecipeMapLayoutDataTest {
	private static final Path DATA = Path.of("src/main/resources/data/hemomancy");
	private static final double MIN_CENTRE_DISTANCE = RecipeMapLayout.NODE_SIZE + 10;

	@Test
	void shippedRitesAndCraftsMapKeepsEveryNodeApartOnBothLayers() throws IOException {
		List<RecipeMapEntry> entries = riteEntries();
		for (Map.Entry<String, JsonObject> recipe : recipes("blood_structure").entrySet()) {
			if (unstained(recipe.getValue())) continue;
			entries.add(craftEntry(recipe.getKey(), recipe.getValue()));
		}
		for (Map.Entry<String, JsonObject> recipe : recipes("cardinal_rite/working").entrySet()) {
			entries.add(craftEntry(recipe.getKey(), recipe.getValue()));
		}

		Map<RecipeMapKey, RecipeMapLayout.AuthoredPosition> positions = new HashMap<>(HarbingerRecipeMapDefinitions.ritePositions());
		positions.putAll(HarbingerRecipeMapDefinitions.craftingPositions());
		RecipeMapLayout.Result layout = RecipeMapLayout.build(entries, HarbingerRecipeMapDefinitions.FAMILIES, positions,
				family -> HarbingerRecipeMapDefinitions.group(family).ordinal());

		assertSpacedAndRinged(layout);
		assertTrue(entries.stream().noneMatch(entry -> entry.family().equals(RecipeMapLayout.MISC_FAMILY)),
				"every shipped Harbinger rite, craft and floor has a named family");
		assertTrue(entries.stream().allMatch(entry -> HarbingerRecipeMapDefinitions.FAMILIES.contains(entry.family())),
				"every family is on the merged map");
	}

	private static RecipeMapEntry craftEntry(String path, JsonObject json) {
		return new RecipeMapEntry(key(RecipeMapEntry.Kind.CRAFTING, path), path, degree(json),
				HarbingerRecipeMapDefinitions.craftingFamily(path), HarbingerRecipeMapDefinitions.craftingOrder(path),
				true, true);
	}

	@Test
	void floorsAndSigilsFollowTheirFirstUseInShippedRites() throws IOException {
		Map<ResourceLocation, RecipeMapEntry> byId = new HashMap<>();
		for (RecipeMapEntry entry : riteEntries()) byId.put(entry.id(), entry);

		assertEquals(2, byId.get(id("communion_minor")).column());
		assertEquals(3, byId.get(id("working_minor")).column(), "the minor Working crafts first use the minor Working floor");
		assertEquals(2, byId.get(id("dominion_minor")).column(), "unused minor floors ring with communion_minor");
		assertEquals(6, byId.get(id("working_grand")).column(), "the Horn of Culmination first uses the grand Working floor");
		assertEquals(4, byId.get(id("bastion")).column());
		assertEquals(4, byId.get(id("shunt")).column(), "unused tier-2 sigil rings with bastion");
	}

	private static void assertSpacedAndRinged(RecipeMapLayout.Result layout) {
		List<RecipeMapLayout.NodeBounds> nodes = List.copyOf(layout.nodes().values());
		for (int first = 0; first < nodes.size(); first++) {
			RecipeMapLayout.NodeBounds a = nodes.get(first);
			double radius = Math.hypot(a.centerX() - layout.centerX(), a.centerY() - layout.centerY());
			assertEquals(RecipeMapLayout.ringRadius(a.entry().column()), radius, RecipeMapLayout.STAGGER_OFFSET + 1.0,
					a.entry().id() + " sits on its degree ring");
			for (int second = first + 1; second < nodes.size(); second++) {
				RecipeMapLayout.NodeBounds b = nodes.get(second);
				if (layer(a) != layer(b)) continue;
				double distance = Math.hypot(a.centerX() - b.centerX(), a.centerY() - b.centerY());
				assertTrue(distance >= MIN_CENTRE_DISTANCE,
						a.entry().id() + " / " + b.entry().id() + " are " + distance + " px apart");
			}
		}
	}

	private static SkillTreeLayer layer(RecipeMapLayout.NodeBounds node) {
		return SkillTreeLayerRules.layerForDegree(node.entry().column());
	}

	private static List<RecipeMapEntry> riteEntries() throws IOException {
		List<RecipeMapEntry> entries = new ArrayList<>();
		Map<ResourceLocation, Integer> floorUse = new HashMap<>();
		Map<ResourceLocation, Integer> sigilUse = new HashMap<>();
		for (Map.Entry<String, JsonObject> recipe : recipes("cardinal_rite").entrySet()) {
			JsonObject json = recipe.getValue();
			if (unstained(json)) continue;
			String path = recipe.getKey();
			int degree = degree(json);
			entries.add(new RecipeMapEntry(key(RecipeMapEntry.Kind.RITE, path), path, degree,
					HarbingerRecipeMapDefinitions.riteFamily(path), HarbingerRecipeMapDefinitions.riteOrder(path),
					true, true));
			if (json.has("floor")) floorUse.merge(ResourceLocation.parse(json.get("floor").getAsString()), degree, Math::min);
			collectSigils(json, degree, sigilUse);
		}

		for (JsonObject craft : recipes("cardinal_rite/working").values()) {
			floorUse.merge(ResourceLocation.parse(craft.get("floor").getAsString()), degree(craft), Math::min);
		}
		Map<ResourceLocation, String> floorTiers = new LinkedHashMap<>();
		Map<ResourceLocation, Integer> floorFallback = new HashMap<>();
		Map<ResourceLocation, String> floorFamilies = new HashMap<>();
		for (Map.Entry<String, JsonObject> floor : definitions("cardinal_rite_floor").entrySet()) {
			String tier = floor.getValue().get("tier").getAsString();
			String purpose = floor.getValue().has("purpose") ? floor.getValue().get("purpose").getAsString()
					: CardinalRitePurpose.ofFloorStyle(floor.getValue().get("style").getAsString()).id();
			floorFamilies.put(id(floor.getKey()), HarbingerRecipeMapDefinitions.floorFamily(CardinalRitePurpose.byId(purpose)));
			floorTiers.put(id(floor.getKey()), tier);
			floorFallback.put(id(floor.getKey()), List.of("minor", "lesser", "greater", "grand").indexOf(tier));
		}
		Map<ResourceLocation, Integer> floorDegrees = RitesAndCraftsTabController.firstUseDegrees(floorTiers, floorUse, floorFallback);
		int order = 0;
		for (ResourceLocation floor : floorTiers.keySet()) {
			entries.add(new RecipeMapEntry(new RecipeMapKey(RecipeMapEntry.Kind.FLOOR, floor), floor.getPath(),
					floorDegrees.get(floor), floorFamilies.get(floor), order++, true, true));
		}

		Map<ResourceLocation, Integer> sigilTiers = new LinkedHashMap<>();
		for (Map.Entry<String, JsonObject> sigil : definitions("ichorian_sigil").entrySet()) {
			JsonObject json = sigil.getValue();
			sigilTiers.put(id(sigil.getKey()), json.has("tier") ? json.get("tier").getAsInt() : 1);
		}
		Map<ResourceLocation, Integer> sigilDegrees = RitesAndCraftsTabController.firstUseDegrees(sigilTiers, sigilUse, sigilTiers);
		order = 0;
		for (ResourceLocation sigil : sigilTiers.keySet()) {
			entries.add(new RecipeMapEntry(new RecipeMapKey(RecipeMapEntry.Kind.SIGIL, sigil), sigil.getPath(),
					sigilDegrees.get(sigil), HarbingerRecipeMapDefinitions.SIGIL_FAMILY, order++, false, false));
		}
		return entries;
	}

	private static void collectSigils(JsonElement element, int degree, Map<ResourceLocation, Integer> sigilUse) {
		if (element.isJsonArray()) {
			element.getAsJsonArray().forEach(child -> collectSigils(child, degree, sigilUse));
		} else if (element.isJsonObject()) {
			for (Map.Entry<String, JsonElement> field : element.getAsJsonObject().entrySet()) {
				if (field.getKey().equals("suggested_sigil") && field.getValue().isJsonPrimitive()
						&& !field.getValue().getAsString().isBlank()) {
					sigilUse.merge(id(field.getValue().getAsString()), degree, Math::min);
				} else {
					collectSigils(field.getValue(), degree, sigilUse);
				}
			}
		}
	}

	private static Map<String, JsonObject> recipes(String folder) throws IOException {
		Map<String, JsonObject> recipes = new LinkedHashMap<>();
		definitions("recipe/" + folder).forEach((name, json) -> recipes.put(folder + "/" + name, json));
		return recipes;
	}

	private static Map<String, JsonObject> definitions(String folder) throws IOException {
		Map<String, JsonObject> definitions = new LinkedHashMap<>();
		try (Stream<Path> files = Files.list(DATA.resolve(folder))) {
			for (Path file : files.filter(path -> path.toString().endsWith(".json")).sorted().toList()) {
				String name = file.getFileName().toString();
				definitions.put(name.substring(0, name.length() - 5),
						JsonParser.parseString(Files.readString(file)).getAsJsonObject());
			}
		}
		return definitions;
	}

	private static boolean unstained(JsonObject json) {
		return json.has("unstained") && json.get("unstained").getAsBoolean();
	}

	private static int degree(JsonObject json) {
		return json.has("required_degree") ? Math.max(0, Math.min(8, json.get("required_degree").getAsInt())) : 0;
	}

	private static RecipeMapKey key(RecipeMapEntry.Kind kind, String path) {
		return new RecipeMapKey(kind, id(path));
	}

	private static ResourceLocation id(String path) {
		return ResourceLocation.fromNamespaceAndPath("hemomancy", path);
	}
}
