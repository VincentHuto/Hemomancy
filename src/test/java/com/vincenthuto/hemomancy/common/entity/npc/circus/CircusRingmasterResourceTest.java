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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CircusRingmasterResourceTest {
	private static final Path RESOURCES = Path.of("src/main/resources/assets/hemomancy");
	private static final Path MODELS = RESOURCES.resolve("models/entity/bbmodel/npc/harbinger/circus");
	private static final Path TEXTURES = RESOURCES.resolve("textures/entity/npc/harbinger/circus");

	@Test
	void bothFormsShipWithTheirAuthoredFaceUvsAndRightHandedStaff() throws Exception {
		verifyForm("CircusRingmasterModel", "ringmaster", "Ringmaster \u2022 Base Form", false);
		verifyForm("CircusTrueRingmasterModel", "circus_true_ringmaster", "Ringmaster \u2022 True Self", true);
	}

	private static void verifyForm(String modelName, String textureName, String rootName, boolean hasTethers)
			throws Exception {
		BufferedImage texture = ImageIO.read(TEXTURES.resolve(textureName + ".png").toFile());
		BufferedImage glow = ImageIO.read(TEXTURES.resolve(textureName + "_glow.png").toFile());
		assertEquals(256, texture.getWidth());
		assertEquals(256, texture.getHeight());
		assertEquals(256, glow.getWidth());
		assertEquals(256, glow.getHeight());

		JsonObject model = JsonParser.parseString(Files.readString(MODELS.resolve(modelName + ".bbmodel"))).getAsJsonObject();
		assertEquals(256, model.getAsJsonObject("resolution").get("width").getAsInt());
		assertEquals(256, model.getAsJsonObject("resolution").get("height").getAsInt());

		Map<String, JsonObject> groups = new HashMap<>();
		collectGroups(model.getAsJsonArray("outliner"), groups);
		for (String name : Set.of(rootName, "Head", "Top hat", "Chest", "Pelvis", "Right wrist",
				"Left wrist", "Right knee", "Left knee", "Conductor staff")) {
			assertTrue(groups.containsKey(name), "missing editable group " + name);
		}
		assertEquals(hasTethers, groups.containsKey("Three tethers"));
		String staff = groups.get("Conductor staff").get("uuid").getAsString();
		assertTrue(containsGroup(groups.get("Right wrist"), staff));
		assertFalse(containsGroup(groups.get("Left wrist"), staff));

		boolean[][] usedUv = new boolean[256][256];
		for (JsonElement entry : model.getAsJsonArray("elements")) {
			markFaces(entry.getAsJsonObject(), usedUv);
		}
		assertPaintFits(texture, usedUv, textureName);
		assertPaintFits(glow, usedUv, textureName + "_glow");
		var source = model.getAsJsonArray("textures").get(0).getAsJsonObject().get("source").getAsString();
		var embedded = ImageIO.read(new java.io.ByteArrayInputStream(
				java.util.Base64.getDecoder().decode(source.substring(source.indexOf(',') + 1))));
		for (int y = 0; y < 256; y++) {
			for (int x = 0; x < 256; x++) {
				assertEquals(embedded.getRGB(x, y), texture.getRGB(x, y));
				if (usedUv[y][x]) assertEquals(255, texture.getRGB(x, y) >>> 24);
				if (glow.getRGB(x, y) >>> 24 != 0) assertEquals(texture.getRGB(x, y), glow.getRGB(x, y));
			}
		}
	}

	private static void collectGroups(Iterable<JsonElement> nodes, Map<String, JsonObject> groups) {
		for (JsonElement node : nodes) {
			if (!node.isJsonObject()) continue;
			JsonObject group = node.getAsJsonObject();
			groups.put(group.get("name").getAsString(), group);
			collectGroups(group.getAsJsonArray("children"), groups);
		}
	}

	private static boolean containsGroup(JsonElement node, String uuid) {
		if (!node.isJsonObject()) return false;
		JsonObject object = node.getAsJsonObject();
		if (uuid.equals(object.get("uuid").getAsString())) return true;
		if (!object.has("children")) return false;
		for (JsonElement child : object.getAsJsonArray("children")) {
			if (containsGroup(child, uuid)) return true;
		}
		return false;
	}

	private static void markFaces(JsonObject element, boolean[][] used) {
		for (Map.Entry<String, JsonElement> face : element.getAsJsonObject("faces").entrySet()) {
			var uv = face.getValue().getAsJsonObject().getAsJsonArray("uv");
			int left = (int) Math.floor(Math.min(uv.get(0).getAsDouble(), uv.get(2).getAsDouble()));
			int top = (int) Math.floor(Math.min(uv.get(1).getAsDouble(), uv.get(3).getAsDouble()));
			int right = (int) Math.ceil(Math.max(uv.get(0).getAsDouble(), uv.get(2).getAsDouble()));
			int bottom = (int) Math.ceil(Math.max(uv.get(1).getAsDouble(), uv.get(3).getAsDouble()));
			assertTrue(left >= 0 && top >= 0 && right <= 256 && bottom <= 256,
					element.get("name").getAsString() + " has out-of-bounds UVs");
			String side = face.getKey();
			boolean cap = side.equals("up") || side.equals("down");
			int widthAxis = side.equals("east") || side.equals("west") ? 2 : 0;
			assertEquals(extent(element, widthAxis), right - left);
			assertEquals(extent(element, cap ? 2 : 1), bottom - top);
			for (int y = top; y < bottom; y++) {
				for (int x = left; x < right; x++) {
					assertFalse(used[y][x], "overlapping UV at " + x + "," + y);
					used[y][x] = true;
				}
			}
		}
	}

	private static void assertPaintFits(BufferedImage image, boolean[][] allowed, String name) {
		int painted = 0;
		for (int y = 0; y < image.getHeight(); y++) for (int x = 0; x < image.getWidth(); x++) {
			if ((image.getRGB(x, y) >>> 24) != 0) {
				painted++;
				assertTrue(allowed[y][x], name + " paints unused UV space at " + x + "," + y);
			}
		}
		assertTrue(painted > 0, name + " must contain painted pixels");
	}

	private static double extent(JsonObject element, int axis) {
		return element.getAsJsonArray("to").get(axis).getAsDouble()
				- element.getAsJsonArray("from").get(axis).getAsDouble();
	}
}
