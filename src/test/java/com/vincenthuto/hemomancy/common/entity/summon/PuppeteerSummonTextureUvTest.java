package com.vincenthuto.hemomancy.common.entity.summon;

import com.google.gson.JsonParser;
import com.vincenthuto.hemomancy.client.model.entity.summon.*;
import com.vincenthuto.hemomancy.common.summon.PuppeteerSummonDefinitions;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PuppeteerSummonTextureUvTest {
	private static final Path MODELS = Path.of("src/main/resources/assets/hemomancy/models/entity/bbmodel");
	private static final Path TEXTURES = Path.of("src/main/resources/assets/hemomancy/textures/entity/puppeteer_summon");

	@Test
	void puppetTexturesOnlyPaintModelUvIslands() throws Exception {
		String[][] assets = {
				{"VeinwingVultureModel.bbmodel", "veinwing_vulture.png"},
				{"MarrowSpitterModel.bbmodel", "marrow_spitter.png"},
				{"GoreboundHulkModel.bbmodel", "gorebound_hulk.png"},
				{"MnemonistPuppetModel.bbmodel", "mnemonist_puppet.png"},
				{"ScarletMummerModel.bbmodel", "scarlet_mummer.png"},
				{"SanguineHoundModel.bbmodel", "sanguine_hound.png"},
				{"RingmasterPatternModel.bbmodel", "ringmaster_pattern.png"}
		};
		assertEquals(PuppeteerSummonDefinitions.all().size(), assets.length,
				"Every summon definition needs a reviewed model and atlas");
		for (String[] asset : assets) assertUvFit(asset[0], asset[1]);
	}

	@Test
	void allSevenRuntimeModelsBakeWithTheirAnimationHierarchy() {
		assertDoesNotThrow(() -> new VeinwingVultureModel(VeinwingVultureModel.createBodyLayer().bakeRoot()));
		assertDoesNotThrow(() -> new MarrowSpitterModel(MarrowSpitterModel.createBodyLayer().bakeRoot()));
		assertDoesNotThrow(() -> new GoreboundHulkModel(GoreboundHulkModel.createBodyLayer().bakeRoot()));
		assertDoesNotThrow(() -> new MnemonistPuppetModel(MnemonistPuppetModel.createBodyLayer().bakeRoot()));
		assertDoesNotThrow(() -> new ScarletMummerModel(ScarletMummerModel.createBodyLayer().bakeRoot()));
		assertDoesNotThrow(() -> new SanguineHoundModel(SanguineHoundModel.createBodyLayer().bakeRoot()));
		assertDoesNotThrow(() -> new RingmasterPatternModel(RingmasterPatternModel.createBodyLayer().bakeRoot()));
	}

	@Test
	void curVariantPreservesBoneAndIronAndConductorGlowOnlyPaintsBasePixels() throws Exception {
		var hound = ImageIO.read(TEXTURES.resolve("sanguine_hound.png").toFile());
		var cur = ImageIO.read(TEXTURES.resolve("sanguine_hound_cur.png").toFile());
		assertEquals(hound.getWidth(), cur.getWidth());
		assertEquals(hound.getHeight(), cur.getHeight());
		int changed = 0;
		for (int y = 0; y < hound.getHeight(); y++) for (int x = 0; x < hound.getWidth(); x++) {
			int original = hound.getRGB(x, y);
			int variant = cur.getRGB(x, y);
			assertEquals(original >>> 24, variant >>> 24);
			if (original != variant) {
				assertTrue(original == 0xFF523039 || original == 0xFF79333E || original == 0xFFA44549,
						"Only blood colors may change on a Blood Cur");
				changed++;
			}
		}
		assertTrue(changed > 0);
		var base = ImageIO.read(TEXTURES.resolve("ringmaster_pattern.png").toFile());
		var glow = ImageIO.read(TEXTURES.resolve("ringmaster_pattern_glow.png").toFile());
		assertEquals(base.getWidth(), glow.getWidth());
		assertEquals(base.getHeight(), glow.getHeight());
		int lit = 0;
		for (int y = 0; y < glow.getHeight(); y++) for (int x = 0; x < glow.getWidth(); x++) {
			if ((glow.getRGB(x, y) >>> 24) != 0) {
				assertEquals(base.getRGB(x, y), glow.getRGB(x, y));
				lit++;
			}
		}
		assertTrue(lit > 0 && lit < 128 * 128 / 8, "Conductor glow must remain a sparse thread accent");
	}

	@Test
	void sanguineHoundUsesItsOwnPuppetTexture() throws Exception {
		assertTrue(Files.exists(TEXTURES.resolve("sanguine_hound.png")));
		String renderer = Files.readString(Path.of(
				"src/main/java/com/vincenthuto/hemomancy/client/render/entity/summon/SanguineHoundRenderer.java"));
		assertTrue(renderer.contains("textures/entity/puppeteer_summon/sanguine_hound.png"));
	}

	private static void assertUvFit(String modelName, String textureName) throws Exception {
		var model = JsonParser.parseString(Files.readString(MODELS.resolve(modelName))).getAsJsonObject();
		var image = ImageIO.read(TEXTURES.resolve(textureName).toFile());
		assertEquals(model.getAsJsonObject("resolution").get("width").getAsInt(), image.getWidth());
		assertEquals(model.getAsJsonObject("resolution").get("height").getAsInt(), image.getHeight());
		var texture = model.getAsJsonArray("textures").get(0).getAsJsonObject();
		assertTrue(texture.has("source"), modelName + " must embed its texture for portable editing");
		var embedded = texture.get("source").getAsString();
		org.junit.jupiter.api.Assertions.assertArrayEquals(Files.readAllBytes(TEXTURES.resolve(textureName)),
				Base64.getDecoder().decode(embedded.substring(embedded.indexOf(',') + 1)),
				textureName + " differs from the embedded Blockbench texture");
		boolean[][] used = new boolean[image.getHeight()][image.getWidth()];
		for (var element : model.getAsJsonArray("elements")) {
			for (var face : element.getAsJsonObject().getAsJsonObject("faces").entrySet()) {
				var uv = face.getValue().getAsJsonObject().getAsJsonArray("uv");
				int left = (int) Math.floor(Math.min(uv.get(0).getAsDouble(), uv.get(2).getAsDouble()));
				int top = (int) Math.floor(Math.min(uv.get(1).getAsDouble(), uv.get(3).getAsDouble()));
				int right = (int) Math.ceil(Math.max(uv.get(0).getAsDouble(), uv.get(2).getAsDouble()));
				int bottom = (int) Math.ceil(Math.max(uv.get(1).getAsDouble(), uv.get(3).getAsDouble()));
				assertTrue(left >= 0 && top >= 0 && right <= image.getWidth() && bottom <= image.getHeight(),
						modelName + " has an out-of-bounds " + face.getKey() + " UV");
				for (int y = top; y < bottom; y++) for (int x = left; x < right; x++) used[y][x] = true;
			}
		}
		var colors = new HashSet<Integer>();
		for (int y = 0; y < image.getHeight(); y++) for (int x = 0; x < image.getWidth(); x++) {
			if (!used[y][x]) assertEquals(0, image.getRGB(x, y) >>> 24,
					textureName + " paints unused UV space at " + x + "," + y);
			else {
				assertEquals(255, image.getRGB(x, y) >>> 24, textureName + " has an unpainted model face");
				colors.add(image.getRGB(x, y));
			}
		}
		assertTrue(colors.size() <= 12, textureName + " exceeds the shared twelve-color palette");
	}
}
