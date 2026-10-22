package com.vincenthuto.hemomancy.client.render.world;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

final class CardinalRitePlantedStaffModelTest {
	private static final Path MODEL_ROOT = Path.of(
			"src/main/resources/assets/hemomancy/models/item");

	private static final int FIST_ELEMENTS = 27;
	private static final List<String> STAFF_MODELS = List.of("living_staff", "living_staff_serpent",
			"living_staff_leech", "living_staff_fungal", "living_staff_pests", "living_staff_chitinite",
			"living_staff_worn_vow", "living_staff_barbed_fitting", "living_staff_chitinite_fitting",
			"living_staff_prismatic_fitting", "living_staff_crimson_vestment", "living_staff_monolithic_frame",
			"living_staff_assumed_limb");

	/**
	 * Each planted model is its held model with one contiguous run of fist elements cut out. Hand-built staffs keep
	 * the fist mid-list and generated fittings append it last, so the run is located rather than hard-coded.
	 */
	@Test
	void everyPlantedStaffAppearanceOmitsItsFistElements() throws IOException {
		for (String model : STAFF_MODELS) {
			assertTrue(Files.exists(MODEL_ROOT.resolve(model + "_planted.json")), model + " has a planted-only model");
			JsonArray heldElements = read(model + ".json").getAsJsonArray("elements");
			JsonArray plantedElements = read(model + "_planted.json").getAsJsonArray("elements");

			assertEquals(heldElements.size() - FIST_ELEMENTS, plantedElements.size(),
					model + " removes the complete fist");
			int fistStart = fistStart(heldElements, plantedElements);
			assertTrue(fistStart >= 0, model + " planted model is its held model minus one contiguous fist");
			for (int i = fistStart; i < fistStart + FIST_ELEMENTS; i++) {
				assertFalse(plantedElements.contains(heldElements.get(i)), model + " does not retain a fist element");
			}
		}
	}

	private static int fistStart(JsonArray held, JsonArray planted) {
		for (int start = 0; start + FIST_ELEMENTS <= held.size(); start++) {
			boolean matches = true;
			for (int i = 0; i < planted.size() && matches; i++) {
				int heldIndex = i < start ? i : i + FIST_ELEMENTS;
				matches = held.get(heldIndex).equals(planted.get(i));
			}
			if (matches) {
				return start;
			}
		}
		return -1;
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
