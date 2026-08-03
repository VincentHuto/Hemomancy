package com.vincenthuto.hemomancy.client.screen.item;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MnemonicFolioWispsTest {
	@Test
	void wispsStayInsideTheFolioAndUsePurpleAndGoldLight() {
		List<MnemonicFolioWisps.Sample> samples = MnemonicFolioWisps.samples(0.0F, 194, 174);

		assertFalse(samples.isEmpty());
		assertTrue(samples.stream().allMatch(sample -> sample.x() >= 0 && sample.x() < 194));
		assertTrue(samples.stream().allMatch(sample -> sample.y() >= 0 && sample.y() < 174));
		assertTrue(samples.stream().anyMatch(sample -> {
			int color = sample.rgb();
			int red = color >> 16 & 0xFF;
			int green = color >> 8 & 0xFF;
			int blue = color & 0xFF;
			return blue > green && red > green;
		}));
		assertTrue(samples.stream().anyMatch(sample -> {
			int color = sample.rgb();
			int red = color >> 16 & 0xFF;
			int green = color >> 8 & 0xFF;
			int blue = color & 0xFF;
			return red > blue && green > blue;
		}));
	}

	@Test
	void wispsDriftOverTime() {
		List<MnemonicFolioWisps.Sample> first = MnemonicFolioWisps.samples(0.0F, 194, 174);
		List<MnemonicFolioWisps.Sample> later = MnemonicFolioWisps.samples(20.0F, 194, 174);

		assertNotEquals(first, later);
	}
}
