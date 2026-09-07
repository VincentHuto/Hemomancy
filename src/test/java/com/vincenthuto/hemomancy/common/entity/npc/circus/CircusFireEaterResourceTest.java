package com.vincenthuto.hemomancy.common.entity.npc.circus;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CircusFireEaterResourceTest {
	private static final Path ASSETS = Path.of("src/main/resources/assets/hemomancy");
	private static final Path MODEL = ASSETS.resolve(
			"models/entity/bbmodel/npc/harbinger/circus/CircusFireEaterModel.bbmodel");
	private static final Path TEXTURES = ASSETS.resolve("textures/entity/npc/harbinger/circus");

	@Test
	void fireEaterShipsWithArticulatedPlayerScaledVariants() throws Exception {
		BufferedImage variant0 = ImageIO.read(TEXTURES.resolve("fire_eater_0.png").toFile());
		BufferedImage variant1 = ImageIO.read(TEXTURES.resolve("fire_eater_1.png").toFile());
		assertNotNull(variant0);
		assertNotNull(variant1);
		assertEquals(128, variant0.getWidth());
		assertEquals(128, variant0.getHeight());
		assertEquals(128, variant1.getWidth());
		assertEquals(128, variant1.getHeight());

		JsonObject model = JsonParser.parseString(Files.readString(MODEL)).getAsJsonObject();
		assertEquals(128, model.getAsJsonObject("resolution").get("width").getAsInt());
		assertEquals(128, model.getAsJsonObject("resolution").get("height").getAsInt());
		assertTrue(model.getAsJsonArray("elements").size() > 0, "model must contain geometry; required costume groups are checked below");

		Map<String, JsonObject> groups = new HashMap<>();
		collectGroups(model.getAsJsonArray("outliner"), groups);
		for (String name : Set.of("head", "hat", "throat_frame", "left_cowl", "right_cowl", "body",
				"right_arm", "left_arm", "right_leg", "left_leg", "right_calf", "left_calf",
				"right_boot", "left_boot")) {
			assertTrue(groups.containsKey(name), "missing editable group " + name);
		}
		assertTrue(groups.get("right_arm").getAsJsonArray("origin").get(0).getAsDouble() > 0.0D);
		assertTrue(groups.get("left_arm").getAsJsonArray("origin").get(0).getAsDouble() < 0.0D);
		assertTrue(containsGroup(groups.get("head"), groups.get("throat_frame").get("uuid").getAsString()));
		assertTrue(containsGroup(groups.get("head"), groups.get("left_cowl").get("uuid").getAsString()));
		assertTrue(containsGroup(groups.get("head"), groups.get("right_cowl").get("uuid").getAsString()));

		boolean[][] usedUv = new boolean[128][128];
		double minY = Double.POSITIVE_INFINITY;
		double maxY = Double.NEGATIVE_INFINITY;
		for (JsonElement entry : model.getAsJsonArray("elements")) {
			JsonObject element = entry.getAsJsonObject();
			minY = Math.min(minY, element.getAsJsonArray("from").get(1).getAsDouble());
			maxY = Math.max(maxY, element.getAsJsonArray("to").get(1).getAsDouble());
			markFaces(element, usedUv);
		}
		assertTrue(minY >= 0.0D, "boots must stay above the ground plane");
		assertTrue(maxY <= 32.0D, "model must fit the existing 1.8-block entity scale");
		assertPaintFits(variant0, usedUv, "fire_eater_0.png");
		assertPaintFits(variant1, usedUv, "fire_eater_1.png");
		assertRelatedVariants(variant0, variant1);
	}

	private static void collectGroups(Iterable<JsonElement> nodes, Map<String, JsonObject> groups) {
		for (JsonElement node : nodes) {
			if (!node.isJsonObject()) continue;
			JsonObject group = node.getAsJsonObject();
			if (group.has("name")) groups.put(group.get("name").getAsString(), group);
			if (group.has("children")) collectGroups(group.getAsJsonArray("children"), groups);
		}
	}

	private static boolean containsGroup(JsonElement node, String uuid) {
		if (!node.isJsonObject()) return false;
		JsonObject object = node.getAsJsonObject();
		if (uuid.equals(object.get("uuid").getAsString())) return true;
		if (!object.has("children")) return false;
		for (JsonElement child : object.getAsJsonArray("children")) if (containsGroup(child, uuid)) return true;
		return false;
	}

	private static void markFaces(JsonObject element, boolean[][] used) {
		for (Map.Entry<String, JsonElement> face : element.getAsJsonObject("faces").entrySet()) {
			var uv = face.getValue().getAsJsonObject().getAsJsonArray("uv");
			int left = (int) Math.floor(Math.min(uv.get(0).getAsDouble(), uv.get(2).getAsDouble()));
			int top = (int) Math.floor(Math.min(uv.get(1).getAsDouble(), uv.get(3).getAsDouble()));
			int right = (int) Math.ceil(Math.max(uv.get(0).getAsDouble(), uv.get(2).getAsDouble()));
			int bottom = (int) Math.ceil(Math.max(uv.get(1).getAsDouble(), uv.get(3).getAsDouble()));
			assertTrue(left >= 0 && top >= 0 && right <= 128 && bottom <= 128,
					element.get("name").getAsString() + " has out-of-bounds UVs");
			for (int y = top; y < bottom; y++) for (int x = left; x < right; x++) used[y][x] = true;
		}
	}

	private static void assertPaintFits(BufferedImage image, boolean[][] allowed, String name) {
		int painted = 0;
		for (int y = 0; y < 128; y++) for (int x = 0; x < 128; x++) {
			if ((image.getRGB(x, y) >>> 24) != 0) {
				painted++;
				assertTrue(allowed[y][x], name + " paints unused UV space at " + x + "," + y);
			}
		}
		assertTrue(painted > 0, name + " must contain painted pixels");
	}

	private static void assertRelatedVariants(BufferedImage variant0, BufferedImage variant1) {
		int painted = 0;
		int changed = 0;
		for (int y = 0; y < 128; y++) for (int x = 0; x < 128; x++) {
			int color0 = variant0.getRGB(x, y);
			int color1 = variant1.getRGB(x, y);
			assertEquals(color0 >>> 24, color1 >>> 24, "variant opacity differs at " + x + "," + y);
			if ((color0 >>> 24) != 0) painted++;
			if (color0 != color1) changed++;
		}
		assertTrue(changed > 0, "variants must retain visible accent differences");
		assertTrue(changed < painted / 3, "variants should differ only in small costume accents");
	}
}
