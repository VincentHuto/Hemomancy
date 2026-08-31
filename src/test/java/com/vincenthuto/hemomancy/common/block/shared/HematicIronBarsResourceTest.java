package com.vincenthuto.hemomancy.common.block.shared;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

class HematicIronBarsResourceTest {
	private static final Path BLOCKSTATE = Path.of(
			"src/main/resources/assets/hemomancy/blockstates/hematic_iron_bars.json");
	private static final String PREFIX = "hemomancy:block/hematic_iron_bars_";

	@Test
	void multipartModelsCoverEveryConnectionInEachAxisPlane() throws IOException {
		JsonArray parts = JsonParser.parseString(Files.readString(BLOCKSTATE))
				.getAsJsonObject().getAsJsonArray("multipart");

		assertPost(parts, "y", 0, 0);
		assertConnectionSet(parts, "y", 0, 0, List.of(
				new Connection("north", "side", "noside"),
				new Connection("south", "side_alt", "noside_alt_y90"),
				new Connection("west", "side_alt_y90", "noside_y270"),
				new Connection("east", "side_y90", "noside_alt")));

		assertPost(parts, "z", 90, 0);
		assertConnectionSet(parts, "z", 90, 0, List.of(
				new Connection("down", "side", "noside"),
				new Connection("up", "side_alt", "noside_alt_y90"),
				new Connection("west", "side_alt_y90", "noside_y270"),
				new Connection("east", "side_y90", "noside_alt")));

		assertPost(parts, "x", 90, 90);
		assertConnectionSet(parts, "x", 90, 90, List.of(
				new Connection("down", "side", "noside"),
				new Connection("up", "side_alt", "noside_alt_y90"),
				new Connection("north", "side_alt_y90", "noside_y270"),
				new Connection("south", "side_y90", "noside_alt")));

		assertEquals(27, parts.size());
		assertAlias("side_y90", "side", 270);
		assertAlias("side_alt_y90", "side_alt", 270);
		assertAlias("noside_y270", "noside", 90);
		assertAlias("noside_alt_y90", "noside_alt", 270);
	}

	private static void assertAlias(String name, String parent, int yRotation) throws IOException {
		Path path = BLOCKSTATE.getParent().resolve("../models/block/hematic_iron_bars_" + name + ".json").normalize();
		JsonObject model = JsonParser.parseString(Files.readString(path)).getAsJsonObject();
		assertEquals(PREFIX + parent, model.get("parent").getAsString());
		JsonObject transform = model.getAsJsonObject("transform");
		assertEquals("center", transform.get("origin").getAsString());
		assertEquals(yRotation, transform.getAsJsonObject("rotation").get("y").getAsInt());
	}

	private static void assertPost(JsonArray parts, String axis, int x, int y) {
		JsonObject apply = find(parts, axis, null, null);
		assertEquals(PREFIX + "post", apply.get("model").getAsString());
		assertRotation(apply, x, y);
	}

	private static void assertConnectionSet(JsonArray parts, String axis, int x, int y,
			List<Connection> connections) {
		for (Connection connection : connections) {
			JsonObject connected = find(parts, axis, connection.property(), "true");
			assertEquals(PREFIX + connection.connectedModel(), connected.get("model").getAsString());
			assertRotation(connected, x, y);

			JsonObject disconnected = find(parts, axis, connection.property(), "false");
			assertEquals(PREFIX + connection.disconnectedModel(), disconnected.get("model").getAsString());
			assertRotation(disconnected, x, y);
		}
	}

	private static JsonObject find(JsonArray parts, String axis, String property, String value) {
		for (var element : parts) {
			JsonObject part = element.getAsJsonObject();
			JsonObject when = part.getAsJsonObject("when");
			if (when == null || !when.has("axis") || !axis.equals(when.get("axis").getAsString())) {
				continue;
			}
			if (property == null && when.size() == 1
					|| property != null && when.has(property) && value.equals(when.get(property).getAsString())) {
				return part.getAsJsonObject("apply");
			}
		}
		throw new AssertionError("missing multipart selector axis=" + axis + ", " + property + "=" + value);
	}

	private static void assertRotation(JsonObject apply, int x, int y) {
		assertEquals(x, apply.has("x") ? apply.get("x").getAsInt() : 0);
		assertEquals(y, apply.has("y") ? apply.get("y").getAsInt() : 0);
	}

	private record Connection(String property, String connectedModel, String disconnectedModel) {
	}
}
