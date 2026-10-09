package com.vincenthuto.hemomancy.client.render.item.harbinger.crossbar;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class MarionetteCrossbarModelResourceTest {
	private static final Path ASSETS = Path.of("src/main/resources/assets/hemomancy");
	private static final Path SOURCE = Path.of("src/main/java/com/vincenthuto/hemomancy");
	private static final String[] STRINGS = { "head_string", "left_hand_string", "right_hand_string",
			"left_foot_string", "right_foot_string" };

	@Test
	void crossbarShipsAsABuiltinEntityItemWithAPaintedEditableModel() throws Exception {
		JsonObject item = JsonParser.parseString(Files.readString(ASSETS.resolve("models/item/marionette_crossbar.json")))
				.getAsJsonObject();
		assertEquals("builtin/entity", item.get("parent").getAsString());
		assertEquals("hemomancy:item/marionette_crossbar",
				item.getAsJsonObject("textures").get("particle").getAsString(), "the sprite stays as the particle");

		BufferedImage texture = ImageIO.read(ASSETS.resolve("textures/item/marionette_crossbar_model.png").toFile());
		assertEquals(64, texture.getWidth());
		assertEquals(64, texture.getHeight());

		JsonObject model = JsonParser.parseString(Files.readString(
				ASSETS.resolve("models/item/bbmodel/MarionetteCrossbarModel.bbmodel"))).getAsJsonObject();
		assertEquals(64, model.getAsJsonObject("resolution").get("width").getAsInt());
		Set<String> groups = new HashSet<>();
		collect(model.getAsJsonArray("outliner"), groups);
		for (String name : new String[] { "grip", "control", "front_bar", "rear_bar" }) {
			assertTrue(groups.contains(name), "missing editable group " + name);
		}
		for (String string : STRINGS) {
			for (int segment = 0; segment < 3; segment++) {
				assertTrue(groups.contains(string + "_" + segment), "missing string segment " + string + "_" + segment);
			}
		}

		boolean[][] used = new boolean[64][64];
		for (JsonElement entry : model.getAsJsonArray("elements")) {
			for (Map.Entry<String, JsonElement> face : entry.getAsJsonObject().getAsJsonObject("faces").entrySet()) {
				var uv = face.getValue().getAsJsonObject().getAsJsonArray("uv");
				int left = (int) Math.floor(Math.min(uv.get(0).getAsDouble(), uv.get(2).getAsDouble()));
				int top = (int) Math.floor(Math.min(uv.get(1).getAsDouble(), uv.get(3).getAsDouble()));
				int right = (int) Math.ceil(Math.max(uv.get(0).getAsDouble(), uv.get(2).getAsDouble()));
				int bottom = (int) Math.ceil(Math.max(uv.get(1).getAsDouble(), uv.get(3).getAsDouble()));
				assertTrue(left >= 0 && top >= 0 && right <= 64 && bottom <= 64, "UVs leave the atlas");
				for (int y = top; y < bottom; y++) for (int x = left; x < right; x++) used[y][x] = true;
			}
		}
		int painted = 0;
		for (int y = 0; y < 64; y++) for (int x = 0; x < 64; x++) {
			if ((texture.getRGB(x, y) >>> 24) == 0) continue;
			painted++;
			assertTrue(used[y][x], "atlas paints unused UV space at " + x + "," + y);
		}
		assertTrue(painted > 0);
	}

	@Test
	void crossingBarsNeverShareAFacingPlaneThatWouldZFight() throws Exception {
		JsonObject model = JsonParser.parseString(Files.readString(
				ASSETS.resolve("models/item/bbmodel/MarionetteCrossbarModel.bbmodel"))).getAsJsonObject();
		var elements = model.getAsJsonArray("elements");
		for (int i = 0; i < elements.size(); i++) {
			for (int j = i + 1; j < elements.size(); j++) {
				JsonObject a = elements.get(i).getAsJsonObject();
				JsonObject b = elements.get(j).getAsJsonObject();
				for (int axis = 0; axis < 3; axis++) {
					if (!overlapsAcross(a, b, axis)) continue;
					for (String end : new String[] { "from", "to" }) {
						assertTrue(Math.abs(coordinate(a, end, axis) - coordinate(b, end, axis)) > 1.0E-6D,
								a.get("name").getAsString() + " and " + b.get("name").getAsString()
										+ " share a same-facing " + "xyz".charAt(axis) + " face");
					}
				}
			}
		}
	}

	private static boolean overlapsAcross(JsonObject a, JsonObject b, int axis) {
		for (int other = 0; other < 3; other++) {
			if (other == axis) continue;
			double low = Math.max(coordinate(a, "from", other), coordinate(b, "from", other));
			double high = Math.min(coordinate(a, "to", other), coordinate(b, "to", other));
			if (high - low <= 1.0E-6D) return false;
		}
		return true;
	}

	private static double coordinate(JsonObject element, String end, int axis) {
		return element.getAsJsonArray(end).get(axis).getAsDouble();
	}

	@Test
	void crossbarRendererIsRegisteredAndFeedsThreadKnots() throws Exception {
		String item = Files.readString(SOURCE.resolve("common/item/harbinger/tool/MarionetteCrossbarItem.java"));
		assertTrue(item.contains("implements HemoClientItemExtensionsProvider"));
		assertTrue(item.contains("MarionetteCrossbarItemRenderer.EXTENSIONS"));
		String layers = Files.readString(SOURCE.resolve("client/event/LayerEvents.java"));
		assertTrue(layers.contains("MarionetteCrossbarModel.LAYER_LOCATION, MarionetteCrossbarModel::createBodyLayer"));
		String threads = Files.readString(SOURCE.resolve("client/render/world/PuppeteerThreadRenderer.java"));
		assertTrue(threads.contains("CrossbarStringAnchors.knot(controller, crossbarId, string, partialTick)"),
				"player and Vesper threads must leave the drawn string knots");
		String renderer = Files.readString(SOURCE.resolve(
				"client/render/item/harbinger/crossbar/MarionetteCrossbarItemRenderer.java"));
		assertTrue(renderer.contains("tracked.inLevel()"), "inventory paper dolls must never move thread anchors");
	}

	private static void collect(Iterable<JsonElement> nodes, Set<String> groups) {
		for (JsonElement node : nodes) {
			if (!node.isJsonObject()) continue;
			JsonObject group = node.getAsJsonObject();
			groups.add(group.get("name").getAsString());
			if (group.has("children")) collect(group.getAsJsonArray("children"), groups);
		}
	}
}
