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

class CircusCarouselResourceTest {
	private static final Path ASSETS = Path.of("src/main/resources/assets/hemomancy");
	private static final Path MODEL = ASSETS.resolve(
			"models/entity/bbmodel/npc/harbinger/circus/CircusCarouselModel.bbmodel");
	private static final Path TEXTURES = ASSETS.resolve("textures/entity/npc/harbinger/circus");

	@Test
	void allHorsesFaceClockwiseAlongTheTurntable() throws Exception {
		JsonObject model = JsonParser.parseString(Files.readString(MODEL)).getAsJsonObject();
		Map<String, JsonObject> groups = new HashMap<>();
		collectGroups(model.getAsJsonArray("outliner"), groups);
		for (int horse = 0; horse < 3; horse++) {
			JsonObject mount = groups.get("horse_" + horse);
			var origin = mount.getAsJsonArray("origin");
			double rotation = Math.toRadians(mount.getAsJsonArray("rotation").get(1).getAsDouble());
			assertClockwiseTangent(origin.get(0).getAsDouble(), origin.get(2).getAsDouble(), rotation,
					"authored horse_" + horse);
		}
	}

	@Test
	void carouselShipsAsAnAuthoredSevenBlockCenterpiece() throws Exception {
		BufferedImage base = ImageIO.read(TEXTURES.resolve("carousel.png").toFile());
		BufferedImage glow = ImageIO.read(TEXTURES.resolve("carousel_glow.png").toFile());
		assertNotNull(base);
		assertNotNull(glow);
		assertEquals(256, base.getWidth());
		assertEquals(256, base.getHeight());
		assertEquals(256, glow.getWidth());
		assertEquals(256, glow.getHeight());

		JsonObject model = JsonParser.parseString(Files.readString(MODEL)).getAsJsonObject();
		assertEquals(256, model.getAsJsonObject("resolution").get("width").getAsInt());
		assertEquals(256, model.getAsJsonObject("resolution").get("height").getAsInt());
		assertTrue(model.getAsJsonArray("elements").size() >= 75,
				"carousel must have an authored frame and three detailed mounts");

		Map<String, JsonObject> groups = new HashMap<>();
		collectGroups(model.getAsJsonArray("outliner"), groups);
		for (String name : Set.of("frame", "canopy", "turntable", "platform", "horse_0", "horse_1",
				"horse_2", "pole_0", "pole_1", "pole_2", "saddle_0", "saddle_1", "saddle_2",
				"anchor_0", "anchor_1", "anchor_2")) {
			assertTrue(groups.containsKey(name), "missing editable group " + name);
		}
		for (int horse = 0; horse < 3; horse++) {
			JsonObject mount = groups.get("horse_" + horse);
			assertTrue(containsGroup(groups.get("turntable"), mount.get("uuid").getAsString()));
			assertTrue(containsGroup(mount, groups.get("saddle_" + horse).get("uuid").getAsString()));
			assertTrue(containsGroup(mount, groups.get("anchor_" + horse).get("uuid").getAsString()));
		}

		boolean[][] usedUv = new boolean[256][256];
		double minY = Double.POSITIVE_INFINITY;
		double maxY = Double.NEGATIVE_INFINITY;
		for (JsonElement entry : model.getAsJsonArray("elements")) {
			JsonObject element = entry.getAsJsonObject();
			minY = Math.min(minY, element.getAsJsonArray("from").get(1).getAsDouble());
			maxY = Math.max(maxY, element.getAsJsonArray("to").get(1).getAsDouble());
			markFaces(element, usedUv);
		}
		assertTrue(minY >= 0.0D, "carousel must stay above its ground plane");
		assertTrue(maxY <= 112.0D, "carousel must fit its existing seven-block entity scale");
		assertPaintFits(base, usedUv, "carousel.png");
		assertGlowFits(base, glow, usedUv);

		String entityInit = source("common/init/EntityInit.java");
		String runtime = source("client/model/entity/npc/CircusCarouselModel.java");
		assertTrue(entityInit.contains(".sized(1.0F, 7.0F)"));
		assertTrue(runtime.contains("LayerDefinition.create(mesh, 256, 256)"));
		assertTrue(runtime.contains("horses[horse].visible = !entity.isDestroyed()"));
		assertTrue(runtime.contains("anchors[horse].visible = entity.isRiderSevered(horse)"));
		assertFalse(runtime.contains("addCaptive("), "captives remain real passengers, not baked geometry");
	}

	private static String source(String relativePath) throws Exception {
		return Files.readString(Path.of("src/main/java/com/vincenthuto/hemomancy/" + relativePath));
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

	private static void assertClockwiseTangent(double x, double z, double rotation, String name) {
		double forwardX = -Math.sin(rotation);
		double forwardZ = -Math.cos(rotation);
		assertEquals(0.0D, x * forwardX + z * forwardZ, 0.02D, name + " must face tangent to the carousel");
		assertTrue(x * forwardZ - z * forwardX < 0.0D, name + " must face clockwise");
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
			for (int y = top; y < bottom; y++) for (int x = left; x < right; x++) used[y][x] = true;
		}
	}

	private static void assertPaintFits(BufferedImage image, boolean[][] allowed, String name) {
		int painted = 0;
		for (int y = 0; y < 256; y++) for (int x = 0; x < 256; x++) {
			if ((image.getRGB(x, y) >>> 24) != 0) {
				painted++;
				assertTrue(allowed[y][x], name + " paints unused UV space at " + x + "," + y);
			}
		}
		assertTrue(painted > 0, name + " must contain painted pixels");
	}

	private static void assertGlowFits(BufferedImage base, BufferedImage glow, boolean[][] allowed) {
		int basePainted = 0;
		int glowPainted = 0;
		for (int y = 0; y < 256; y++) for (int x = 0; x < 256; x++) {
			boolean basePixel = (base.getRGB(x, y) >>> 24) != 0;
			boolean glowPixel = (glow.getRGB(x, y) >>> 24) != 0;
			if (basePixel) basePainted++;
			if (glowPixel) {
				glowPainted++;
				assertTrue(basePixel && allowed[y][x], "glow paints outside visible model UVs at " + x + "," + y);
			}
		}
		assertTrue(glowPainted > 0, "carousel must retain restrained emissive details");
		assertTrue(glowPainted < basePainted / 8, "glow must remain limited to bulbs, eyes, scars, and anchors");
	}
}
