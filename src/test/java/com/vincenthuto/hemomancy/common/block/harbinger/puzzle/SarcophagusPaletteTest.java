package com.vincenthuto.hemomancy.common.block.harbinger.puzzle;

import com.vincenthuto.hemomancy.common.entity.boss.saint.EnumSaintType;
import com.vincenthuto.hemomancy.support.RepoPaths;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A Saint's tomb wears its own atlas when saint_sarcophagus.py has painted one, and the default otherwise; the game
 * moves the default atlas's quads onto it texel for texel, so each palette must keep the default's size and cut-outs.
 */
final class SarcophagusPaletteTest {
	private static final Path BLOCK_TEXTURES = RepoPaths.ASSETS.resolve("textures/block");

	@Test
	void aTombWearsItsSaintsPaletteOrTheDefault() {
		assertEquals("saint_sarcophagus", SarcophagusGeometry.atlas(null), "a tomb no relic has named");
		assertEquals("saint_sarcophagus_hemorath", SarcophagusGeometry.atlas(EnumSaintType.HEMORATH));
		assertEquals("saint_sarcophagus_seraphae", SarcophagusGeometry.atlas(EnumSaintType.SERAPHAE));
		assertEquals("saint_sarcophagus_putriciel", SarcophagusGeometry.atlas(EnumSaintType.PUTRICIEL));
		assertEquals("saint_sarcophagus_velorum", SarcophagusGeometry.atlas(EnumSaintType.VELORUM));
		for (EnumSaintType saint : EnumSaintType.values()) {
			boolean painted = Arrays.asList(SarcophagusGeometry.SAINT_PALETTES)
					.contains(saint.name().toLowerCase(Locale.ROOT));
			assertEquals(painted ? "saint_sarcophagus_" + saint.name().toLowerCase(Locale.ROOT) : "saint_sarcophagus",
					SarcophagusGeometry.atlas(saint), saint.name());
		}
	}

	@Test
	void everyPaletteIsTheDefaultAtlasRepainted() throws IOException {
		BufferedImage plain = ImageIO.read(BLOCK_TEXTURES.resolve("saint_sarcophagus.png").toFile());
		for (String name : SarcophagusGeometry.SAINT_PALETTES) {
			EnumSaintType.valueOf(name.toUpperCase(Locale.ROOT));
			Path path = BLOCK_TEXTURES.resolve("saint_sarcophagus_" + name + ".png");
			assertTrue(Files.exists(path), "palette atlas painted: " + RepoPaths.display(path));
			BufferedImage palette = ImageIO.read(path.toFile());
			assertEquals(plain.getWidth(), palette.getWidth(), name + " width");
			assertEquals(plain.getHeight(), palette.getHeight(), name + " height");
			int differing = 0;
			for (int y = 0; y < plain.getHeight(); y++)
				for (int x = 0; x < plain.getWidth(); x++) {
					int a = plain.getRGB(x, y), b = palette.getRGB(x, y);
					assertEquals(a >>> 24 == 0, b >>> 24 == 0, name + " keeps the default's cut-outs at " + x + "," + y);
					if (a != b) differing++;
				}
			assertTrue(differing > 0, name + " is painted in colours of its own");
		}
	}

	@Test
	void noPaletteAtlasIsLeftUnlisted() {
		List<String> listed = Arrays.asList(SarcophagusGeometry.SAINT_PALETTES);
		for (EnumSaintType saint : EnumSaintType.values()) {
			String name = saint.name().toLowerCase(Locale.ROOT);
			assertEquals(Files.exists(BLOCK_TEXTURES.resolve("saint_sarcophagus_" + name + ".png")), listed.contains(name),
					name + ": a painted atlas and SAINT_PALETTES agree");
		}
	}
}
