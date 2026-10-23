package com.vincenthuto.hemomancy.architecture;

import com.vincenthuto.hemomancy.support.RepoPaths;
import com.vincenthuto.hemomancy.support.SourceAssertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Step 10 (roadmap Phase 8, V-1): screen chrome is drawn by {@code ScreenDrawUtils} (borders, bevels, ring borders,
 * slot wells) and {@code VeinBackgroundRenderer} (the animated vein field, styled per screen). A file may keep its own
 * vein field only when it is different art, listed here with the reason; stale entries fail.
 */
class ScreenChromeGuardrailTest {
	private static final String SCREEN_DRAW_UTILS = "src/main/java/com/vincenthuto/hemomancy/client/screen/skilltree/util/ScreenDrawUtils.java";
	private static final String VEIN_RENDERER = "src/main/java/com/vincenthuto/hemomancy/client/screen/skilltree/harbinger/VeinBackgroundRenderer.java";

	/** Private helpers that used to copy the shared chrome. */
	private static final Pattern CHROME_HELPER = Pattern.compile(
			"void (drawBorder|drawDoubleBorder|drawSingleBorder|drawThinBorder|drawInventoryBorder|drawSlotWell"
					+ "|renderVeinBackground|renderVenousBackground|drawVeinTendril)\\(");
	/**
	 * The inner ring of a two-pixel border, its top line then its bottom line:
	 * {@code fill(x + 1, y + 1, x + w - 1, y + 2, ...); fill(x + 1, y + h - 2, ...)}.
	 */
	private static final Pattern INNER_BORDER_RING = Pattern.compile(
			"fill\\(\\s*(\\w+) \\+ 1,\\s*(\\w+) \\+ 1,\\s*\\1 \\+ (\\w+) - 1,\\s*\\2 \\+ 2,[^;]*;"
					+ "\\s*\\w+\\.fill\\(\\s*\\1 \\+ 1,\\s*\\2 \\+ \\w+ - 2,");
	/**
	 * A slot well: the dark frame, the background, then the light lip on the right edge:
	 * {@code fill(sx - 1, sy - 1, sx + 17, sy + 17, ...); fill(sx, sy, sx + 16, sy + 16, ...); fill(sx + 16, sy, ...)}.
	 */
	private static final Pattern SLOT_WELL_FRAME = Pattern.compile(
			"fill\\(\\s*(\\w+) - 1,\\s*(\\w+) - 1,\\s*\\1 \\+ 17,\\s*\\2 \\+ 17,[^;]*;\\s*(?://[^\\n]*\\s*)?"
					+ "\\w+\\.fill\\(\\s*\\1,\\s*\\2,\\s*\\1 \\+ 16,\\s*\\2 \\+ 16,[^;]*;\\s*(?://[^\\n]*\\s*)?"
					+ "\\w+\\.fill\\(\\s*\\1 \\+ 16,\\s*\\2,");
	/** A seeded vein field of its own. */
	private static final Pattern VEIN_FIELD = Pattern.compile("veinParams = new float\\[");

	/** Vein fields that are different art, not copies of the shared one. */
	private static final Map<String, String> OWN_VEIN_ART = Map.of(
			"src/main/java/com/vincenthuto/hemomancy/client/screen/item/PuppeteersSpindleScreen.java",
			"Taut thread-like veins: one sine, no glow rings, faint speckles",
			"src/main/java/com/vincenthuto/hemomancy/client/screen/item/living/MorphlingJarScreen.java",
			"Green curving veins with per-tendril curvature, a row gradient and bubbles",
			"src/main/java/com/vincenthuto/hemomancy/client/screen/tile/crafting/MycelialCrucibleScreen.java",
			"Amber fungal hyphae, like the spore implant and lantern backgrounds",
			"src/main/java/com/vincenthuto/hemomancy/client/screen/tile/functional/SSCScreen.java",
			"A bare field of slow amber tendrils with no rings or speckles",
			"src/main/java/com/vincenthuto/hemomancy/client/render/tile/harbinger/functional/SanguineMonolithRenderer.java",
			"World-space veins on the monolith, not screen chrome");
	/** Helpers whose names match the old chrome copies but whose bodies are their own art. */
	private static final Set<String> OWN_HELPERS = Set.of(
			"src/main/java/com/vincenthuto/hemomancy/client/screen/item/PuppeteersSpindleScreen.java#renderVeinBackground",
			"src/main/java/com/vincenthuto/hemomancy/client/screen/item/PuppeteersSpindleScreen.java#drawVeinTendril",
			"src/main/java/com/vincenthuto/hemomancy/client/screen/item/living/MorphlingJarScreen.java#renderVeinBackground");

	@Test
	void chromeHelpersAreNotCopied() throws IOException {
		Set<String> found = new TreeSet<>();
		for (Path file : clientSources()) {
			String path = RepoPaths.display(file);
			if (path.equals(SCREEN_DRAW_UTILS) || path.equals(VEIN_RENDERER)) continue;
			var matcher = CHROME_HELPER.matcher(SourceAssertions.read(file));
			while (matcher.find()) found.add(path + "#" + matcher.group(1));
		}
		assertEquals(new TreeSet<>(OWN_HELPERS), found,
				"draw borders and slot wells with ScreenDrawUtils and vein backgrounds with VeinBackgroundRenderer");
	}

	@Test
	void bordersAndSlotWellsAreDrawnBySharedHelpers() throws IOException {
		Set<String> found = new TreeSet<>();
		for (Path file : clientSources()) {
			String path = RepoPaths.display(file);
			if (path.equals(SCREEN_DRAW_UTILS)) continue;
			String text = SourceAssertions.read(file);
			if (INNER_BORDER_RING.matcher(text).find()) found.add(path + " (two-pixel border)");
			if (SLOT_WELL_FRAME.matcher(text).find()) found.add(path + " (slot well)");
		}
		assertEquals(Set.of(), found,
				"use ScreenDrawUtils.drawBorder / drawBevelBorder / drawRingBorder / drawSlotWell with the screen's colours");
	}

	@Test
	void veinFieldsAreSharedUnlessTheyAreOtherArt() throws IOException {
		Set<String> found = new TreeSet<>();
		for (Path file : mainSources()) {
			String path = RepoPaths.display(file);
			if (path.equals(VEIN_RENDERER)) continue;
			if (VEIN_FIELD.matcher(SourceAssertions.read(file)).find()) found.add(path);
		}
		assertEquals(new TreeSet<>(OWN_VEIN_ART.keySet()), found,
				"give the screen a VeinBackgroundRenderer.Style, or list its own vein art here with a reason");
	}

	private static List<Path> clientSources() throws IOException {
		return mainSources().stream().filter(path -> RepoPaths.display(path).contains("/hemomancy/client/")).toList();
	}

	private static List<Path> mainSources() throws IOException {
		try (Stream<Path> files = Files.walk(RepoPaths.MAIN_JAVA)) {
			return files.filter(path -> path.toString().endsWith(".java")).toList();
		}
	}
}
