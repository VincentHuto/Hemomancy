package com.vincenthuto.hemomancy.common.resource;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class HarbingerEscritoireAssetTest {
	private static final Path ASSETS = Path.of("src/main/resources/assets/hemomancy");

	@Test
	void modelOmitsBothVoxelshapeGroupRectangles() throws IOException {
		JsonArray elements = readJson(ASSETS.resolve("models/block/harbinger_escritoire.json"))
				.getAsJsonArray("elements");
		assertEquals(35, elements.size(), "only the visible BBModel elements should render");
		assertFalse(hasBox(elements, -5.5, 16, 0, 21.25, 27, 16));
		assertFalse(hasBox(elements, -7.5, 0, -1.25, 23.25, 16, 16));
	}

	@Test
	void oversizedModelDisablesAmbientOcclusion() throws IOException {
		JsonObject model = readJson(ASSETS.resolve("models/block/harbinger_escritoire.json"));
		assertTrue(model.has("ambientocclusion"),
				"oversized model must explicitly opt out of ambient occlusion");
		assertFalse(model.get("ambientocclusion").getAsBoolean(),
				"ambient occlusion creates false shadow bands across oversized model elements");
	}

	@Test
	void blockAndItemModelsExposeAllFourFacings() throws IOException {
		JsonArray multipart = readJson(ASSETS.resolve("blockstates/harbinger_escritoire.json"))
				.getAsJsonArray("multipart");
		assertEquals(4, multipart.size());
		assertEquals(0, rotation(multipart, "north"));
		assertEquals(180, rotation(multipart, "south"));
		assertEquals(90, rotation(multipart, "east"));
		assertEquals(270, rotation(multipart, "west"));
		assertEquals("hemomancy:block/harbinger_escritoire",
				readJson(ASSETS.resolve("models/item/harbinger_escritoire.json")).get("parent").getAsString());
		assertTrue(Files.isRegularFile(Path.of(
				"src/main/resources/data/hemomancy/loot_table/blocks/harbinger_escritoire.json")));
	}

	private static boolean hasBox(JsonArray elements, double... coordinates) {
		for (var element : elements) {
			JsonObject object = element.getAsJsonObject();
			if (matches(object.getAsJsonArray("from"), coordinates, 0)
					&& matches(object.getAsJsonArray("to"), coordinates, 3)) return true;
		}
		return false;
	}

	private static boolean matches(JsonArray values, double[] expected, int offset) {
		for (int i = 0; i < 3; i++) {
			if (Double.compare(values.get(i).getAsDouble(), expected[i + offset]) != 0) return false;
		}
		return true;
	}

	private static JsonObject readJson(Path path) throws IOException {
		assertTrue(Files.isRegularFile(path), "missing " + path);
		return JsonParser.parseString(Files.readString(path)).getAsJsonObject();
	}

	private static int rotation(JsonArray multipart, String facing) {
		for (var part : multipart) {
			JsonObject object = part.getAsJsonObject();
			if (facing.equals(object.getAsJsonObject("when").get("facing").getAsString())) {
				JsonObject apply = object.getAsJsonObject("apply");
				return apply.has("y") ? apply.get("y").getAsInt() : 0;
			}
		}
		throw new AssertionError("missing facing " + facing);
	}
}
