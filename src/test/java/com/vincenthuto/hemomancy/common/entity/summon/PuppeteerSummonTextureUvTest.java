package com.vincenthuto.hemomancy.common.entity.summon;

import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
				{"ScarletMummerModel.bbmodel", "scarlet_mummer.png"}
		};
		for (String[] asset : assets) assertUvFit(asset[0], asset[1]);
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
		boolean[][] used = new boolean[image.getHeight()][image.getWidth()];
		for (var element : model.getAsJsonArray("elements")) {
			for (var face : element.getAsJsonObject().getAsJsonObject("faces").entrySet()) {
				var uv = face.getValue().getAsJsonObject().getAsJsonArray("uv");
				int left = (int) Math.floor(Math.min(uv.get(0).getAsDouble(), uv.get(2).getAsDouble()));
				int top = (int) Math.floor(Math.min(uv.get(1).getAsDouble(), uv.get(3).getAsDouble()));
				int right = (int) Math.ceil(Math.max(uv.get(0).getAsDouble(), uv.get(2).getAsDouble()));
				int bottom = (int) Math.ceil(Math.max(uv.get(1).getAsDouble(), uv.get(3).getAsDouble()));
				for (int y = top; y < bottom; y++) for (int x = left; x < right; x++) used[y][x] = true;
			}
		}
		for (int y = 0; y < image.getHeight(); y++) for (int x = 0; x < image.getWidth(); x++) {
			if (!used[y][x]) assertEquals(0, image.getRGB(x, y) >>> 24,
					textureName + " paints unused UV space at " + x + "," + y);
		}
	}
}
