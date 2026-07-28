package com.vincenthuto.hemomancy.client.render.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.Test;

class BloodlickerVisualResourceTest {
	private static final Path SOURCE_ROOT = Path.of("src/main/java/com/vincenthuto/hemomancy");
	private static final Path TEXTURE = Path.of(
			"src/main/resources/assets/hemomancy/textures/entity/bloodlicker/model_bloodlicker.png");

	@Test
	void bloodlickerUsesItsOwnHunchedHumanoidModel() throws IOException {
		String renderer = read("client/render/entity/mob/animal/BloodlickerRenderer.java");
		String model = read("client/model/entity/mob/animal/BloodlickerModel.java");
		String layers = read("client/event/LayerEvents.java");

		assertTrue(renderer.contains("BloodlickerModel"), "renderer must not reuse the leech model");
		assertTrue(renderer.contains("textures/entity/bloodlicker/model_bloodlicker.png"),
				"renderer must use the dedicated texture");
		assertTrue(layers.contains("BloodlickerModel.LAYER_LOCATION"), "custom model layer must be registered");
		assertTrue(model.contains("\"abdomen\""), "model needs a separately animated distended abdomen");
		assertTrue(model.contains("\"tongue\""), "model needs the signature long tongue");
		assertTrue(model.contains("\"hair\""), "model needs a hanging hair curtain");
		assertTrue(model.contains("\"left_arm\"") && model.contains("\"right_arm\""),
				"model needs long humanoid forelimbs");
		assertTrue(model.contains("\"left_leg\"") && model.contains("\"right_leg\""),
				"model needs crouched humanoid hind limbs");
	}

	@Test
	void bloodlickerTextureIsACompleteDedicatedSkin() throws IOException {
		assertTrue(Files.isRegularFile(TEXTURE), "dedicated Bloodlicker texture is missing");
		BufferedImage image = ImageIO.read(TEXTURE.toFile());
		assertEquals(128, image.getWidth());
		assertEquals(128, image.getHeight());

		int opaquePixels = 0;
		int darkFleshPixels = 0;
		int bloodPixels = 0;
		for (int y = 0; y < image.getHeight(); y++) {
			for (int x = 0; x < image.getWidth(); x++) {
				int argb = image.getRGB(x, y);
				int alpha = argb >>> 24;
				if (alpha < 128) continue;
				opaquePixels++;
				int red = (argb >>> 16) & 0xFF;
				int green = (argb >>> 8) & 0xFF;
				int blue = argb & 0xFF;
				if (red < 100 && green < 90 && blue < 90) darkFleshPixels++;
				if (red > 90 && red > green * 2 && red > blue * 3 / 2) bloodPixels++;
			}
		}
		assertTrue(opaquePixels > 2_000, "texture atlas should contain a complete painted skin");
		assertTrue(darkFleshPixels > 1_000, "skin should be dominated by corpse-dark flesh and hair");
		assertTrue(bloodPixels > 300, "abdomen and tongue need a visible blood-red material");
	}

	private static String read(String relativePath) throws IOException {
		return Files.readString(SOURCE_ROOT.resolve(relativePath)).replace("\r\n", "\n");
	}
}
