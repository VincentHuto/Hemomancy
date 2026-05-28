package com.vincenthuto.hemomancy.common.block.harbinger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class HematicArmatureUpgradeFeedbackSourceTest {
	private static final Path JAVA_ROOT = Path.of("src/main/java");

	private HematicArmatureUpgradeFeedbackSourceTest() {
	}

	public static void main(String[] args) throws IOException {
		String blockEntity = readSource(
				"com/vincenthuto/hemomancy/common/tile/crafting/HematicArmatureBlockEntity.java");

		assertContains("upgrade matching scans bowls independently of armor slot",
				blockEntity, "findMatchingUpgrade(recipes, slot, worn, player)");
		assertContains("consumed reagent bowl is preserved",
				blockEntity, "match.bowlSlot()");
		assertContains("upgrade feedback uses consumed bowl",
				blockEntity, "spawnUpgradeEffects(serverLevel, player, match.bowlSlot())");
		assertContains("bowl effect uses burning particles",
				blockEntity, "ParticleTypes.FLAME");
		assertContains("player flash uses crimson dust",
				blockEntity, "CRIMSON_FLASH");
		assertContains("player flash uses large server particle burst",
				blockEntity, "serverLevel.sendParticles(CRIMSON_FLASH");
		assertContains("bowl particle position is rotation-aware",
				blockEntity, "HematicArmatureBlock.FACING");
		assertContains("armature waits about five seconds per armor item",
				blockEntity, "PROCESS_INTERVAL_TICKS = 100");
		assertContains("armature starts a pending craft before upgrading",
				blockEntity, "startPendingCraft(slot, match.bowlSlot(), now)");
		assertContains("armature shows windup particles during the delay",
				blockEntity, "spawnWindupEffects(serverLevel, player, match.bowlSlot()");
		assertContains("one completed craft clears the pending craft",
				blockEntity, "clearPendingCraft();");
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
