package com.vincenthuto.hemomancy.common.block.harbinger;

import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.vincenthuto.hemomancy.support.SourceAssertions.assertContains;

public final class HematicArmatureUpgradeFeedbackSourceTest {
	private static final Path JAVA_ROOT = Path.of("src/main/java");

	private HematicArmatureUpgradeFeedbackSourceTest() {
	}

	@Test
	void allChecksPass() throws IOException {
		String blockEntity = readSource(
				"com/vincenthuto/hemomancy/common/tile/harbinger/crafting/HematicArmatureBlockEntity.java");

		assertContains("upgrade matching scans bowls independently of armor slot",
				blockEntity, "findMatchingUpgrade(recipes, slot, worn, player)");
		assertContains("consumed reagent bowl is preserved",
				blockEntity, "match.bowlSlot()");
		assertContains("upgrade feedback uses consumed bowl",
				blockEntity, "spawnUpgradeEffects(serverLevel, player, match.bowlSlot())");
		assertContains("bowl effect uses blood-lit embers",
				blockEntity, "HemoParticleData.emberSpark(BOWL_FIRE");
		assertContains("player flash uses a crimson hit glow",
				blockEntity, "serverLevel.sendParticles(HemoParticleData.hitGlow(CRIMSON_FLASH)");
		assertContains("player flash bursts blood outward",
				blockEntity, "serverLevel.sendParticles(HemoParticleData.bloodCell(BLOOD)");
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
}
