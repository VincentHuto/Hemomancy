package com.vincenthuto.hemomancy.client.render.world;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

final class CardinalRitePlantedStaffModelTest {
	private static final Path MODEL_ROOT = Path.of(
			"src/main/resources/assets/hemomancy/models/item");

	@Test
	void everyPlantedStaffAppearanceOmitsItsFistElements() throws IOException {
		Map<String, Integer> fistStarts = new LinkedHashMap<>();
		fistStarts.put("living_staff", 9);
		fistStarts.put("living_staff_serpent", 19);
		fistStarts.put("living_staff_leech", 4);
		fistStarts.put("living_staff_fungal", 16);
		fistStarts.put("living_staff_pests", 11);
		fistStarts.put("living_staff_chitinite", 19);
		fistStarts.put("living_staff_worn_vow", 8);
		fistStarts.put("living_staff_barbed_fitting", 19);
		fistStarts.put("living_staff_chitinite_fitting", 19);
		fistStarts.put("living_staff_prismatic_fitting", 13);
		fistStarts.put("living_staff_crimson_vestment", 11);
		fistStarts.put("living_staff_monolithic_frame", 16);
		fistStarts.put("living_staff_assumed_limb", 11);

		for (Map.Entry<String, Integer> entry : fistStarts.entrySet()) {
			JsonObject held = read(entry.getKey() + ".json");
			assertTrue(Files.exists(MODEL_ROOT.resolve(entry.getKey() + "_planted.json")),
					entry.getKey() + " has a planted-only model");
			JsonObject planted = read(entry.getKey() + "_planted.json");
			JsonArray heldElements = held.getAsJsonArray("elements");
			JsonArray plantedElements = planted.getAsJsonArray("elements");
			JsonArray fistElements = new JsonArray();
			for (int i = entry.getValue(); i < entry.getValue() + 27; i++) {
				fistElements.add(heldElements.get(i));
			}

			assertEquals(heldElements.size() - 27, plantedElements.size(),
					entry.getKey() + " removes the complete fist");
			for (var plantedElement : plantedElements) {
				assertFalse(fistElements.contains(plantedElement),
						entry.getKey() + " does not retain a fist element");
			}
		}
	}

	@Test
	void plantedRendererSelectsTheFistlessVariantForEveryStaffVisual() {
		String[] expected = {
				"living_staff_planted",
				"living_staff_serpent_planted",
				"living_staff_leech_planted",
				"living_staff_fungal_planted",
				"living_staff_pests_planted",
				"living_staff_chitinite_planted",
				"living_staff_pests_planted",
				"living_staff_fungal_planted",
				"living_staff_chitinite_planted",
				"living_staff_worn_vow_planted",
				"living_staff_barbed_fitting_planted",
				"living_staff_chitinite_fitting_planted",
				"living_staff_prismatic_fitting_planted",
				"living_staff_crimson_vestment_planted",
				"living_staff_monolithic_frame_planted",
				"living_staff_assumed_limb_planted"
		};

		for (int visual = 0; visual < expected.length; visual++) {
			assertEquals(expected[visual], CardinalRitePlantedStaffModels.modelName(visual));
		}
	}

	private static JsonObject read(String filename) throws IOException {
		return JsonParser.parseString(Files.readString(MODEL_ROOT.resolve(filename)))
				.getAsJsonObject();
	}
}
