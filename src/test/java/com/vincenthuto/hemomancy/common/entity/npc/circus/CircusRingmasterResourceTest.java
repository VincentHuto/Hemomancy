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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CircusRingmasterResourceTest {
	private static final Path RESOURCES = Path.of("src/main/resources/assets/hemomancy");
	private static final Path MODEL = RESOURCES.resolve(
			"models/entity/bbmodel/npc/harbinger/circus/CircusRingmasterModel.bbmodel");
	private static final Path TEXTURES = RESOURCES.resolve("textures/entity/npc/harbinger/circus");

	@Test
	void ringmasterShipsWithRightHandedScaleCoherentAssets() throws Exception {
		Path texturePath = TEXTURES.resolve("ringmaster.png");
		Path glowPath = TEXTURES.resolve("ringmaster_glow.png");
		assertTrue(Files.isRegularFile(texturePath));
		assertTrue(Files.isRegularFile(glowPath));
		assertTrue(Files.isRegularFile(MODEL));

		BufferedImage texture = ImageIO.read(texturePath.toFile());
		BufferedImage glow = ImageIO.read(glowPath.toFile());
		assertNotNull(texture);
		assertNotNull(glow);
		assertEquals(128, texture.getWidth());
		assertEquals(128, texture.getHeight());
		assertEquals(128, glow.getWidth());
		assertEquals(128, glow.getHeight());

		JsonObject model = JsonParser.parseString(Files.readString(MODEL)).getAsJsonObject();
		assertEquals(128, model.getAsJsonObject("resolution").get("width").getAsInt());
		assertEquals(128, model.getAsJsonObject("resolution").get("height").getAsInt());

		Map<String, JsonObject> groups = new HashMap<>();
		collectGroups(model.getAsJsonArray("outliner"), groups);
		for (String name : Set.of("head", "top_hat", "body", "right_arm", "left_arm", "right_leg",
				"left_leg", "right_calf", "left_calf", "living_staff", "staff_topper",
				"blood_core")) {
			assertTrue(groups.containsKey(name), "missing editable group " + name);
		}
		assertEquals(5.0D, groups.get("right_arm").getAsJsonArray("origin").get(0).getAsDouble(), 0.001D);
		assertEquals(-5.0D, groups.get("left_arm").getAsJsonArray("origin").get(0).getAsDouble(), 0.001D);

		String rightArm = groups.get("right_arm").get("uuid").getAsString();
		String leftArm = groups.get("left_arm").get("uuid").getAsString();
		String staff = groups.get("living_staff").get("uuid").getAsString();
		JsonObject rightOutliner = outlinerGroup(model, rightArm);
		JsonObject leftOutliner = outlinerGroup(model, leftArm);
		assertTrue(containsGroup(rightOutliner, staff), "staff must be nested beneath Blockbench right_arm");
		assertFalse(containsGroup(leftOutliner, staff), "staff must not be nested beneath Blockbench left_arm");

		boolean[][] usedUv = new boolean[128][128];
		boolean[][] emissiveUv = new boolean[128][128];
		double minY = Double.POSITIVE_INFINITY;
		double maxY = Double.NEGATIVE_INFINITY;
		for (JsonElement entry : model.getAsJsonArray("elements")) {
			JsonObject element = entry.getAsJsonObject();
			minY = Math.min(minY, element.getAsJsonArray("from").get(1).getAsDouble());
			maxY = Math.max(maxY, element.getAsJsonArray("to").get(1).getAsDouble());
			markFaces(element, usedUv);
			String name = element.get("name").getAsString();
			if (name.startsWith("head_0_0_") || name.startsWith("blood_core")) markFaces(element, emissiveUv);
		}
		assertTrue(minY >= 0.0D, "model geometry must not extend below its feet");
		assertTrue(maxY <= 38.0D, "hat and staff must stay within the planned player-scale silhouette");
		assertPaintFits(texture, usedUv, "ringmaster.png");
		assertPaintFits(glow, emissiveUv, "ringmaster_glow.png");

		String source = source("client/model/entity/npc/CircusRingmasterModel.java");
		assertTrue(source.contains("PartDefinition staff = rightArm.addOrReplaceChild(\"living_staff\""));
		assertFalse(source.contains("leftArm.addOrReplaceChild(\"living_staff\""));
		assertTrue(source.contains("staffTopper = rightArm.getChild(\"living_staff\").getChild(\"staff_topper\")"));
		assertTrue(source.contains("LayerDefinition.create(mesh, 128, 128)"));
		assertFalse(source.contains("addBoxPart("));
	}

	private static void collectGroups(Iterable<JsonElement> nodes, Map<String, JsonObject> groups) {
		for (JsonElement node : nodes) {
			if (!node.isJsonObject()) continue;
			JsonObject group = node.getAsJsonObject();
			groups.put(group.get("name").getAsString(), group);
			if (group.has("children")) collectGroups(group.getAsJsonArray("children"), groups);
		}
	}

	private static JsonObject outlinerGroup(JsonObject model, String uuid) {
		for (JsonElement entry : model.getAsJsonArray("outliner")) {
			if (entry.isJsonObject() && uuid.equals(entry.getAsJsonObject().get("uuid").getAsString())) {
				return entry.getAsJsonObject();
			}
		}
		throw new AssertionError("missing outliner group " + uuid);
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
			assertTrue(left >= 0 && top >= 0 && right <= 128 && bottom <= 128,
					element.get("name").getAsString() + " has out-of-bounds UVs");
			for (int y = top; y < bottom; y++) for (int x = left; x < right; x++) used[y][x] = true;
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

	private static String source(String relativePath) throws Exception {
		return Files.readString(Path.of("src/main/java/com/vincenthuto/hemomancy/" + relativePath));
	}
}
