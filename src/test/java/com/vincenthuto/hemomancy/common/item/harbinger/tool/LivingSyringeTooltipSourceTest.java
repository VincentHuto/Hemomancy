package com.vincenthuto.hemomancy.common.item.harbinger.tool;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

final class LivingSyringeTooltipSourceTest {
	@Test
	void loadedSyringeUsesTheRackShiftContentsTooltip() throws IOException {
		String syringe = source("LivingSyringeItem.java");
		String rack = source("VialRackItem.java");

		assertTrue(rack.contains("appendContentsTooltip"),
				"The rack needs a reusable detailed-contents tooltip for loaded tools.");
		assertTrue(syringe.contains("Screen.hasShiftDown()"),
				"The syringe should expose its samples only while Shift is held.");
		assertTrue(syringe.contains("VialRackItem.appendContentsTooltip(rack, tooltip)"),
				"The syringe should show the loaded rack's actual samples, not only its empty count.");
	}

	private static String source(String fileName) throws IOException {
		return Files.readString(Path.of("src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/tool/living", fileName));
	}
}
