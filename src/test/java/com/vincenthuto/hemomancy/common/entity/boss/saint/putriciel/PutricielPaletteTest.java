package com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel;

import com.vincenthuto.hemomancy.support.RepoPaths;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Putriciel's palette ruling (2026-10-26): no red texel anywhere outside the fire, and the fire only in the Flammeus
 * look. A red texel is a hue within -20 to +15 degrees, saturation at least 0.4 and a red channel of at least 70.
 * The embers atlas holds only crimson, pink-white and soot.
 */
final class PutricielPaletteTest {
	private static final String DIR = "textures/entity/boss/putriciel/";
	private static final Set<Integer> FIRE = Set.of(0xD61E2E, 0xFFA8BE, 0x160A0C);

	private static BufferedImage atlas(String name) throws IOException {
		return ImageIO.read(RepoPaths.ASSETS.resolve(DIR + name).toFile());
	}

	static boolean red(int argb) {
		int r = argb >> 16 & 255, g = argb >> 8 & 255, b = argb & 255;
		float[] hsb = Color.RGBtoHSB(r, g, b, null);
		float hue = hsb[0] * 360.0F;
		if (hue > 180.0F) hue -= 360.0F;
		return hue >= -20.0F && hue <= 15.0F && hsb[1] >= 0.4F && r >= 70;
	}

	@ParameterizedTest
	@ValueSource(strings = {"putriciel.png", "putriciel_glow.png", "putriciel_charred.png"})
	void noRedTexelOutsideTheFire(String name) throws IOException {
		BufferedImage image = atlas(name);
		int reds = 0;
		for (int y = 0; y < image.getHeight(); y++) {
			for (int x = 0; x < image.getWidth(); x++) {
				int argb = image.getRGB(x, y);
				if ((argb >>> 24) != 0 && red(argb)) reds++;
			}
		}
		assertEquals(0, reds, name + " carries red texels");
	}

	@Test
	void theEmbersAreOnlyTheFlammeusFire() throws IOException {
		BufferedImage image = atlas("putriciel_embers.png");
		int lit = 0;
		for (int y = 0; y < image.getHeight(); y++) {
			for (int x = 0; x < image.getWidth(); x++) {
				int argb = image.getRGB(x, y);
				if ((argb >>> 24) == 0) continue;
				lit++;
				assertTrue(FIRE.contains(argb & 0xFFFFFF), String.format("ember texel %06X at %d,%d", argb & 0xFFFFFF, x, y));
			}
		}
		assertTrue(lit > 0, "the embers atlas lights the seams, sacs, censers and palm");
	}

	@Test
	void theBurningAtlasIsOnlyTheFlammeusFire() throws IOException {
		BufferedImage image = atlas("putriciel_burning.png");
		int lit = 0;
		for (int y = 0; y < image.getHeight(); y++) {
			for (int x = 0; x < image.getWidth(); x++) {
				int argb = image.getRGB(x, y);
				if ((argb >>> 24) == 0) continue;
				lit++;
				assertTrue(FIRE.contains(argb & 0xFFFFFF), String.format("burning texel %06X at %d,%d", argb & 0xFFFFFF, x, y));
			}
		}
		assertTrue(lit > 0, "the burning atlas lights the rims, the wraps, the spires, the hem and the sac tears");
	}

	// ── the colour pass (revision R3): three value bands and the hue families, within the palette ──

	/** Rec. 709 luminance of the stored sRGB values, 0 to 1 (the generator's bands use the same). */
	static float luminance(int argb) {
		return (0.2126F * (argb >> 16 & 255) + 0.7152F * (argb >> 8 & 255) + 0.0722F * (argb & 255)) / 255.0F;
	}

	/** Ochre and muted yellow: hue 30 to 58 degrees, saturation at least 0.45, value at least 0.45. */
	static boolean ochre(int argb) {
		float[] hsb = Color.RGBtoHSB(argb >> 16 & 255, argb >> 8 & 255, argb & 255, null);
		float hue = hsb[0] * 360.0F;
		return hue >= 30.0F && hue <= 58.0F && hsb[1] >= 0.45F && hsb[2] >= 0.45F;
	}

	/** Mould green: hue 78 to 150 degrees, saturation at least 0.25, value at least 0.2. */
	static boolean mouldGreen(int argb) {
		float[] hsb = Color.RGBtoHSB(argb >> 16 & 255, argb >> 8 & 255, argb & 255, null);
		float hue = hsb[0] * 360.0F;
		return hue >= 78.0F && hue <= 150.0F && hsb[1] >= 0.25F && hsb[2] >= 0.2F;
	}

	/** A warm colour under hue 30 (between the red rule's edge and ochre): the base keeps none. */
	static boolean underOchre(int argb) {
		float[] hsb = Color.RGBtoHSB(argb >> 16 & 255, argb >> 8 & 255, argb & 255, null);
		float hue = hsb[0] * 360.0F;
		return hsb[1] > 0.15F && hue > 15.0F && hue < 30.0F;
	}

	private static float[] baseShares() throws IOException {
		BufferedImage image = atlas("putriciel.png");
		int used = 0, dark = 0, mid = 0, light = 0, ochre = 0, green = 0, warm = 0;
		for (int y = 0; y < image.getHeight(); y++) {
			for (int x = 0; x < image.getWidth(); x++) {
				int argb = image.getRGB(x, y);
				if ((argb >>> 24) == 0) continue;
				used++;
				float lum = luminance(argb);
				if (lum < 0.22F) dark++;
				else if (lum <= 0.6F) mid++;
				else light++;
				if (ochre(argb)) ochre++;
				if (mouldGreen(argb)) green++;
				if (underOchre(argb)) warm++;
			}
		}
		return new float[]{dark / (float) used, mid / (float) used, light / (float) used, ochre / (float) used,
				green / (float) used, warm};
	}

	@Test
	void theBaseReadsInThreeValueBands() throws IOException {
		float[] shares = baseShares();
		assertTrue(shares[0] >= 0.25F && shares[0] <= 0.35F, "dark band (eschar, ichor, iron, the hem, roots) at " + shares[0]);
		assertTrue(shares[1] >= 0.40F && shares[1] <= 0.50F, "mid band (robe, olive caps, gills, flesh, wraps, moss) at " + shares[1]);
		assertTrue(shares[2] >= 0.20F && shares[2] <= 0.30F, "light band (vestments, sac crowns, cap rims, mushrooms) at " + shares[2]);
	}

	@Test
	void ochreAndMutedYellowStayUnderTheBrassGuardAndNoWarmColourSitsUnderThirtyDegrees() throws IOException {
		float[] shares = baseShares();
		assertTrue(shares[3] > 0.02F, "the shelves, sacs and gills carry ochre and muted yellow");
		assertTrue(shares[3] <= 0.15F, "ochre and muted yellow at " + shares[3] + ", over the 15% brass guard");
		assertEquals(0, (int) shares[5], "warm texels under hue 30 read as rust or brass");
	}

	@Test
	void mouldGreenIsTheSaturatedAccent() throws IOException {
		float green = baseShares()[4];
		assertTrue(green >= 0.05F && green <= 0.10F, "mould green at " + green + ", outside 5 to 10%");
	}

	@Test
	void everyAtlasSharesTheBaseLayout() throws IOException {
		BufferedImage base = atlas("putriciel.png");
		for (String name : new String[]{"putriciel_glow.png", "putriciel_charred.png", "putriciel_embers.png",
				"putriciel_burning.png"}) {
			BufferedImage other = atlas(name);
			assertEquals(base.getWidth(), other.getWidth(), name);
			assertEquals(base.getHeight(), other.getHeight(), name);
		}
	}
}
