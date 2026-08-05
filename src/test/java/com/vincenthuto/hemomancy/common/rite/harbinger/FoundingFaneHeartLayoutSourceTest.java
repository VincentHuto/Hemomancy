package com.vincenthuto.hemomancy.common.rite.harbinger;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

final class FoundingFaneHeartLayoutSourceTest {
	private static final Path ACTIVATION = Path.of(
			"src/main/java/com/vincenthuto/hemomancy/common/network/capa/harbinger/BloodCraftingKeyPressPacket.java");
	private static final Path COMPLETION = Path.of(
			"src/main/java/com/vincenthuto/hemomancy/common/rite/harbinger/HarbingerCardinalRiteEvents.java");

	@Test
	void foundingFaneHeartManifestsThreeBlocksAboveTheRiteCenter() throws IOException {
		String activation = Files.readString(ACTIVATION).replace("\r\n", "\n");
		String completion = Files.readString(COMPLETION).replace("\r\n", "\n");

		String activationGate = section(activation,
				"private static boolean canManifestFoundingFaneHeart", "private static BlockPattern.BlockPatternMatch");
		String completionGate = section(completion,
				"private static void completeFoundingFane", "\n}\n");

		assertTrue(activationGate.contains("BlockPos heartPos = centerPos.above(3)"),
				"Founding Fane activation must check the future bloodwell position three blocks above the focus");
		assertTrue(activationGate.contains("level.getBlockState(heartPos)"),
				"Founding Fane clearance must inspect the offset heart rather than the Cardinal Focus");
		assertTrue(completionGate.contains("BlockPos heartPos = center.above(3)"),
				"Founding Fane completion must derive the same offset heart position");
		assertTrue(completionGate.contains("sLevel.setBlock(heartPos, BlockInit.consecrated_bloodwell.get()"),
				"Founding Fane completion must manifest the bloodwell at the offset heart");
		assertTrue(completionGate.contains("faneData.consecrateHeart(faneOwner, heartPos)"),
				"The persisted fane heart must use the manifested bloodwell position");
	}

	private static String section(String source, String start, String end) {
		int startIndex = source.indexOf(start);
		int endIndex = source.indexOf(end, startIndex);
		if (startIndex < 0 || endIndex <= startIndex) {
			throw new AssertionError("Unable to locate source section from " + start + " to " + end);
		}
		return source.substring(startIndex, endIndex);
	}
}
