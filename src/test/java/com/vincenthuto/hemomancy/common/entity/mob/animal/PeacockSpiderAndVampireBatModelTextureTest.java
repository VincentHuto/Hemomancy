package com.vincenthuto.hemomancy.common.entity.mob.animal;

import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PeacockSpiderAndVampireBatModelTextureTest {
	private static final Path JAVA = Path.of("src/main/java/com/vincenthuto/hemomancy");
	private static final Path MODELS = Path.of("src/main/resources/assets/hemomancy/models/entity/bbmodel");
	private static final Path TEXTURES = Path.of("src/main/resources/assets/hemomancy/textures/entity");

	@Test
	void creaturesUseDedicatedModelsAndTextures() throws Exception {
		String clientEvents = Files.readString(JAVA.resolve("client/event/ClientEvents.java"));
		String layerEvents = Files.readString(JAVA.resolve("client/event/LayerEvents.java"));
		assertTrue(clientEvents.contains("PeacockSpiderRenderer::new"));
		assertTrue(clientEvents.contains("VampireBatRenderer::new"));
		assertTrue(!clientEvents.contains("EntityInit.vampire_bat.get(), BatRenderer::new"));
		assertTrue(layerEvents.contains("PeacockSpiderModel.LAYER_LOCATION"));
		assertTrue(layerEvents.contains("VampireBatModel.LAYER_LOCATION"));

		assertDedicatedRenderer("PeacockSpiderRenderer.java", "PeacockSpiderModel",
				"textures/entity/peacock_spider/model_peacock_spider.png", "SpiderRenderer");
		assertDedicatedRenderer("VampireBatRenderer.java", "VampireBatModel",
				"textures/entity/vampire_bat/model_vampire_bat.png", "BatRenderer");
	}

	@Test
	void customCreatureTexturesExactlyCoverTheirModelUvIslands() throws Exception {
		assertUvFit("PeacockSpiderModel.bbmodel", "peacock_spider/model_peacock_spider.png");
		assertUvFit("VampireBatModel.bbmodel", "vampire_bat/model_vampire_bat.png");
	}

	@Test
	void peacockSpiderLegJointsBendDownTowardTheGround() throws Exception {
		String model = Files.readString(JAVA.resolve("client/model/entity/mob/animal/PeacockSpiderModel.java"));

		assertTrue(model.contains("addLeg(root, \"left_front_leg\", -3.0F, -3.5F, -0.55F, -0.55F, false);"));
		assertTrue(model.contains("addLeg(root, \"right_front_leg\", 3.0F, -3.5F, 0.55F, 0.55F, true);"));
		assertTrue(model.contains("addLeg(root, \"left_mid_front_leg\", -3.5F, -1.0F, -0.2F, -0.75F, false);"));
		assertTrue(model.contains("addLeg(root, \"right_mid_front_leg\", 3.5F, -1.0F, 0.2F, 0.75F, true);"));
		assertTrue(model.contains("addLeg(root, \"left_mid_hind_leg\", -3.5F, 2.0F, 0.2F, -0.75F, false);"));
		assertTrue(model.contains("addLeg(root, \"right_mid_hind_leg\", 3.5F, 2.0F, -0.2F, 0.75F, true);"));
		assertTrue(model.contains("addLeg(root, \"left_hind_leg\", -3.0F, 4.5F, 0.55F, -0.55F, false);"));
		assertTrue(model.contains("addLeg(root, \"right_hind_leg\", 3.0F, 4.5F, -0.55F, 0.55F, true);"));
		assertTrue(model.contains("right ? -0.65F : 0.65F"));
	}

	private static void assertDedicatedRenderer(String rendererName, String modelName,
			String texturePath, String vanillaRenderer) throws Exception {
		String renderer = Files.readString(JAVA.resolve("client/render/entity/mob/animal").resolve(rendererName));
		assertTrue(renderer.contains(modelName));
		assertTrue(renderer.contains(texturePath));
		assertTrue(!renderer.contains("extends " + vanillaRenderer));
		assertTrue(Files.exists(JAVA.resolve("client/model/entity/mob/animal").resolve(modelName + ".java")));
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
				assertTrue(left >= 0 && top >= 0 && right <= image.getWidth() && bottom <= image.getHeight(),
						modelName + " has UVs outside " + textureName);
				for (int y = top; y < bottom; y++) for (int x = left; x < right; x++) used[y][x] = true;
			}
		}
		for (int y = 0; y < image.getHeight(); y++) for (int x = 0; x < image.getWidth(); x++) {
			int alpha = image.getRGB(x, y) >>> 24;
			if (used[y][x]) assertTrue(alpha > 0, textureName + " leaves a UV pixel blank at " + x + "," + y);
			else assertEquals(0, alpha, textureName + " paints unused UV space at " + x + "," + y);
		}
	}
}
