package com.vincenthuto.hemomancy.common.entity.npc.circus;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CircusFacultyResourceTest {
	private static final Path ASSETS = Path.of("src/main/resources/assets/hemomancy");
	private static final Path BBMODELS = ASSETS.resolve("models/entity/bbmodel/npc/harbinger/circus");
	private static final Path TEXTURES = ASSETS.resolve("textures/entity/npc/harbinger/circus");
	private static final Path MODELS = Path.of("src/main/java/com/vincenthuto/hemomancy/client/model/entity/npc");

	@ParameterizedTest
	@CsvSource({
			"Threadkeeper, circus_threadkeeper, eyeshade visor spindle ledger right_garter left_garter right_coat_tail left_coat_tail",
			"Understudy, circus_understudy, half_mask shawl right_shawl_tail left_shawl_tail cane",
			"Strongman, circus_strongman, moustache belt barbell right_wristband left_wristband",
			"BeastTamer, circus_beast_tamer, mantle whip_coil whip_handle whip_lash_0 right_epaulette left_epaulette right_coat_tail left_coat_tail"
	})
	void facultyShipsWithArticulatedPlayerScaledVariants(String actor, String texture, String costume) throws Exception {
		BufferedImage variant0 = ImageIO.read(TEXTURES.resolve(texture + "_0.png").toFile());
		BufferedImage variant1 = ImageIO.read(TEXTURES.resolve(texture + "_1.png").toFile());
		assertNotNull(variant0);
		assertNotNull(variant1);
		for (BufferedImage image : new BufferedImage[] { variant0, variant1 }) {
			assertEquals(128, image.getWidth());
			assertEquals(128, image.getHeight());
		}

		JsonObject model = JsonParser.parseString(Files.readString(BBMODELS.resolve("Circus" + actor + "Model.bbmodel")))
				.getAsJsonObject();
		assertEquals(128, model.getAsJsonObject("resolution").get("width").getAsInt());
		assertEquals(128, model.getAsJsonObject("resolution").get("height").getAsInt());
		assertEquals(2, model.getAsJsonArray("textures").size(), "both costume variants stay previewable");

		Map<String, JsonObject> groups = new HashMap<>();
		collectGroups(model.getAsJsonArray("outliner"), groups);
		for (String name : ("head hat body right_arm left_arm right_leg left_leg " + costume).split(" ")) {
			assertTrue(groups.containsKey(name), actor + " is missing editable group " + name);
		}
		// Blockbench mirrors X: the right arm sits on positive X, as in the Fire Eater.
		assertTrue(groups.get("right_arm").getAsJsonArray("origin").get(0).getAsDouble() > 0.0D);
		assertTrue(groups.get("left_arm").getAsJsonArray("origin").get(0).getAsDouble() < 0.0D);

		String source = Files.readString(MODELS.resolve("Circus" + actor + "Model.java"));
		assertTrue(source.contains("extends CircusFacultyModel"), actor + " must share the faculty rig");
		for (String name : costume.split(" ")) {
			assertTrue(source.contains("\"" + name + "\""), actor + " Java model is missing part " + name);
		}

		boolean[][] usedUv = new boolean[128][128];
		double minY = Double.POSITIVE_INFINITY;
		double maxY = Double.NEGATIVE_INFINITY;
		for (JsonElement entry : model.getAsJsonArray("elements")) {
			JsonObject element = entry.getAsJsonObject();
			minY = Math.min(minY, element.getAsJsonArray("from").get(1).getAsDouble());
			maxY = Math.max(maxY, element.getAsJsonArray("to").get(1).getAsDouble());
			markFaces(element, usedUv);
		}
		assertTrue(minY >= 0.0D, actor + " must stay above the ground plane");
		assertTrue(maxY <= 32.0D, actor + " must fit the existing 1.95-block entity scale");
		assertPaintFits(variant0, usedUv, texture + "_0.png");
		assertPaintFits(variant1, usedUv, texture + "_1.png");
		assertRelatedVariants(variant0, variant1, actor);
	}

	private static void collectGroups(Iterable<JsonElement> nodes, Map<String, JsonObject> groups) {
		for (JsonElement node : nodes) {
			if (!node.isJsonObject()) continue;
			JsonObject group = node.getAsJsonObject();
			if (group.has("name")) groups.put(group.get("name").getAsString(), group);
			if (group.has("children")) collectGroups(group.getAsJsonArray("children"), groups);
		}
	}

	/** Marks one cube's faces; no symmetric part shares UVs, so each cube must own its island. */
	private static void markFaces(JsonObject element, boolean[][] used) {
		String name = element.get("name").getAsString();
		boolean[][] own = new boolean[128][128];
		for (Map.Entry<String, JsonElement> face : element.getAsJsonObject("faces").entrySet()) {
			var uv = face.getValue().getAsJsonObject().getAsJsonArray("uv");
			int left = (int) Math.floor(Math.min(uv.get(0).getAsDouble(), uv.get(2).getAsDouble()));
			int top = (int) Math.floor(Math.min(uv.get(1).getAsDouble(), uv.get(3).getAsDouble()));
			int right = (int) Math.ceil(Math.max(uv.get(0).getAsDouble(), uv.get(2).getAsDouble()));
			int bottom = (int) Math.ceil(Math.max(uv.get(1).getAsDouble(), uv.get(3).getAsDouble()));
			assertTrue(left >= 0 && top >= 0 && right <= 128 && bottom <= 128, name + " has out-of-bounds UVs");
			for (int y = top; y < bottom; y++) for (int x = left; x < right; x++) own[y][x] = true;
		}
		for (int y = 0; y < 128; y++) for (int x = 0; x < 128; x++) {
			if (!own[y][x]) continue;
			assertTrue(!used[y][x], name + " overlaps another cube's UVs at " + x + "," + y);
			used[y][x] = true;
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

	private static void assertRelatedVariants(BufferedImage variant0, BufferedImage variant1, String actor) {
		int painted = 0;
		int changed = 0;
		for (int y = 0; y < 128; y++) for (int x = 0; x < 128; x++) {
			int color0 = variant0.getRGB(x, y);
			int color1 = variant1.getRGB(x, y);
			assertEquals(color0 >>> 24, color1 >>> 24, actor + " variant opacity differs at " + x + "," + y);
			if ((color0 >>> 24) != 0) painted++;
			if (color0 != color1) changed++;
		}
		assertTrue(changed > 0, actor + " variants must retain visible accent differences");
		assertTrue(changed < painted / 3, actor + " variants should differ only in small costume accents");
	}
}
