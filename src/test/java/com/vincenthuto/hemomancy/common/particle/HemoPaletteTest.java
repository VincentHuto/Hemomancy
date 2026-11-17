package com.vincenthuto.hemomancy.common.particle;

import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HemoPaletteTest {
	@Test
	void sharedColoursKeepTheirValues() {
		assertRgb(86, 160, 132, HemoPalette.VERDIGRIS);
		assertRgb(244, 246, 248, HemoPalette.STILL_WHITE);
		assertRgb(201, 209, 217, HemoPalette.STILL_SILVER);
		assertRgb(214, 232, 236, HemoPalette.PALE_GLOW);
	}

	private static void assertRgb(int red, int green, int blue, ParticleColor color) {
		assertEquals((red << 16) | (green << 8) | blue, color.getColor(), color.toString());
	}
}
