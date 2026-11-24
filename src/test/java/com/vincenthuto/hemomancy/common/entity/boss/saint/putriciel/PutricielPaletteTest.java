package com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel;

import com.vincenthuto.hemomancy.support.RepoPaths;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class PutricielPaletteTest {
	private static final Set<Integer> FIRE = Set.of(0xD61E2E, 0xFFA8BE, 0x160A0C);

	private static BufferedImage atlas(String suffix) throws IOException {
		return ImageIO.read(RepoPaths.ASSETS.resolve("textures/entity/boss/putriciel/putriciel_vestment" + suffix + ".png").toFile());
	}

	@Test
	void theVestmentsAndDecayUseMutedBrownRedAndSaturatedGreen() throws IOException {
		BufferedImage base = atlas("");
		int occupied = 0, brownRed = 0, green = 0;
		float[] hsb = new float[3];
		for (int y = 0; y < base.getHeight(); y++) {
			for (int x = 0; x < base.getWidth(); x++) {
				int argb = base.getRGB(x, y);
				if ((argb >>> 24) == 0) continue;
				occupied++;
				Color.RGBtoHSB(argb >> 16 & 255, argb >> 8 & 255, argb & 255, hsb);
				float hue = hsb[0] * 360.0F;
				if (hue < 35 && hsb[1] >= 0.30F && hsb[2] <= 0.65F) brownRed++;
				if (hue >= 78 && hue <= 150 && hsb[1] >= 0.25F && hsb[2] <= 0.60F) green++;
				assertTrue(!FIRE.contains(argb & 0xFFFFFF), "only cleansing layers use the fire colours");
			}
		}
		assertTrue(brownRed > occupied * .25F, "brown-red robes dominate the cloth");
		assertTrue(green > occupied * .1F, "the fungal back has a readable green accent");
	}

	@ParameterizedTest
	@ValueSource(strings = {"_glow", "_charred", "_embers", "_burning"})
	void effectMasksUseOccupiedTexelsFromTheSameAtlas(String suffix) throws IOException {
		BufferedImage base = atlas("");
		BufferedImage effect = atlas(suffix);
		assertEquals(base.getWidth(), effect.getWidth());
		assertEquals(base.getHeight(), effect.getHeight());
		boolean fire = suffix.equals("_embers") || suffix.equals("_burning");
		int painted = 0;
		for (int y = 0; y < effect.getHeight(); y++) {
			for (int x = 0; x < effect.getWidth(); x++) {
				int argb = effect.getRGB(x, y);
				if ((argb >>> 24) == 0) continue;
				painted++;
				assertTrue((base.getRGB(x, y) >>> 24) != 0, "mask outside the base at " + x + "," + y);
				if (fire) assertTrue(FIRE.contains(argb & 0xFFFFFF), "only Flammeus hues appear in the fire masks");
			}
		}
		assertTrue(painted > 0);
	}
}
