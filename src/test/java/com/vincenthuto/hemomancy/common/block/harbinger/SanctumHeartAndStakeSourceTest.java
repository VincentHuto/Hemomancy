package com.vincenthuto.hemomancy.common.block.harbinger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class SanctumHeartAndStakeSourceTest {
	private static final Path BLOODWELL = Path.of(
			"src/main/java/com/vincenthuto/hemomancy/common/block/harbinger/functional/ConsecratedBloodwellBlock.java");
	private static final Path STAKE = Path.of(
			"src/main/java/com/vincenthuto/hemomancy/common/block/harbinger/functional/HematicStakeBlock.java");
	private static final Path BLOCK_INIT = Path.of(
			"src/main/java/com/vincenthuto/hemomancy/common/init/BlockInit.java");
	private static final Path LANG = Path.of("src/main/resources/assets/hemomancy/lang/en_us.json");

	private SanctumHeartAndStakeSourceTest() {
	}

	public static void main(String[] args) throws IOException {
		String bloodwell = Files.readString(BLOODWELL).replace("\r\n", "\n");
		String stake = Files.readString(STAKE).replace("\r\n", "\n");
		String blockInit = Files.readString(BLOCK_INIT).replace("\r\n", "\n");
		String lang = Files.readString(LANG).replace("\r\n", "\n");

		assertContains("bloodwell blocks duplicate hearts", bloodwell, "canPlaceBloodwell");
		assertContains("bloodwell collapses sanctum on removal", bloodwell, "removeHeart");
		assertContains("bloodwell uses footprint membership", bloodwell, "isInOwnSanctum");
		assertContains("stake validates connected placement", stake, "canPlaceStake");
		assertContains("stake registers into sanctum", stake, "addStake");
		assertContains("stake unregisters on removal", stake, "removeStake");
		assertContains("registers hematic stake block", blockInit, "hematic_stake");
		assertContains("adds hematic stake translation", lang, "\"block.hemomancy.hematic_stake\"");
	}

	private static void assertContains(String label, String text, String expected) {
		if (!text.contains(expected)) {
			throw new AssertionError(label + ": missing " + expected);
		}
	}
}
