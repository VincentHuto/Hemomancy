package com.vincenthuto.hemomancy.common.block.harbinger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class HematicArmatureBreakDropSourceTest {
	private static final Path JAVA_ROOT = Path.of("src/main/java");

	private HematicArmatureBreakDropSourceTest() {
	}

	public static void main(String[] args) throws IOException {
		String block = readSource(
				"com/vincenthuto/hemomancy/common/block/harbinger/crafting/HematicArmatureBlock.java");
		String blockEntity = readSource(
				"com/vincenthuto/hemomancy/common/tile/crafting/HematicArmatureBlockEntity.java");

		assertContains("armature removal drops persistent upgrade items", block,
				"armature.dropAppliedUpgradeItems(level, pos)");
		assertContains("armature exposes upgrade item recovery", blockEntity,
				"dropAppliedUpgradeItems(Level level, BlockPos pos)");
		assertContains("vicar tier recovers the consecration kit", blockEntity,
				"ItemInit.vicars_consecration_kit");
		assertContains("monolithic tier also recovers the cornerstone", blockEntity,
				"ItemInit.monolithic_cornerstone");
		assertContains("tier recovery uses persisted armature tier", blockEntity,
				"armatureTier.id()");
	}

	private static String readSource(String relativePath) throws IOException {
		return Files.readString(JAVA_ROOT.resolve(relativePath)).replace("\r\n", "\n");
	}

	private static void assertContains(String label, String haystack, String needle) {
		if (!haystack.contains(needle)) {
			throw new AssertionError(label + ": missing " + needle);
		}
	}
}
