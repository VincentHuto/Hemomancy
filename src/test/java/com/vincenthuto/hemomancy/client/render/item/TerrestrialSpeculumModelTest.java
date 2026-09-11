package com.vincenthuto.hemomancy.client.render.item;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class TerrestrialSpeculumModelTest {
	private static final Path ITEM_MODELS = Path.of("src/main/resources/assets/hemomancy/models/item");

	@Test
	void speculumUsesAThreeDimensionalModelInEveryHeldView() throws IOException {
		JsonObject model = read(ITEM_MODELS.resolve("terrestrial_speculum.json"));
		JsonArray elements = model.getAsJsonArray("elements");

		assertNotNull(elements, "the Speculum is modeled from cuboids instead of a flat sprite");
		assertTrue(elements.size() >= 12, "the model preserves its spike, haft, lens, and tendrils");
		assertTrue(hasDepth(elements), "at least one cuboid has visible depth");

		JsonObject display = model.getAsJsonObject("display");
		assertNotNull(display.get("firstperson_righthand"));
		assertNotNull(display.get("thirdperson_righthand"));
		assertNotNull(display.get("fixed"), "the planting renderer uses the fixed transform");
	}

	@Test
	void editableBlockbenchSourceShipsBesideTheRuntimeModel() throws IOException {
		Path source = ITEM_MODELS.resolve("bbmodel/terrestrial_speculum.bbmodel");
		assertTrue(Files.exists(source));
		JsonObject model = read(source);
		assertTrue(model.getAsJsonArray("elements").size() >= 12);
		assertTrue(model.getAsJsonArray("outliner").size() >= 4,
				"the editable source keeps the spike, haft, lens, and tendril groups");
	}

	private static boolean hasDepth(JsonArray elements) {
		for (var element : elements) {
			JsonObject cuboid = element.getAsJsonObject();
			JsonArray from = cuboid.getAsJsonArray("from");
			JsonArray to = cuboid.getAsJsonArray("to");
			if (to.get(2).getAsDouble() - from.get(2).getAsDouble() > 1.0D) return true;
		}
		return false;
	}

	private static JsonObject read(Path path) throws IOException {
		return JsonParser.parseString(Files.readString(path)).getAsJsonObject();
	}
}
