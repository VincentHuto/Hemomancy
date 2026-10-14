package com.vincenthuto.hemomancy.common.rite;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

final class BrazierItemAbsorptionRiteSourceTest {
	private static final Path ROOT = Path.of("").toAbsolutePath();

	@Test
	void graftsScarsAndMemoriesShareTheSameItemParticleChannel() throws IOException {
		String shared = read("src/main/java/com/vincenthuto/hemomancy/common/rite/BrazierItemAbsorptionRite.java");
		String graft = read("src/main/java/com/vincenthuto/hemomancy/common/item/harbinger/memories/LivingWeaponGraftRite.java");
		String scar = read("src/main/java/com/vincenthuto/hemomancy/common/rite/ScarBrazierRite.java");
		String memory = read("src/main/java/com/vincenthuto/hemomancy/common/rite/MemoryBrazierRite.java");
		String rites = read("src/main/java/com/vincenthuto/hemomancy/common/rite/brazier/BrazierRites.java");
		String blockEntity = read(
				"src/main/java/com/vincenthuto/hemomancy/common/tile/harbinger/rite/IronBrazierBlockEntity.java");

		assertContains(shared, "REQUIRED_CHANNEL_TICKS = 60");
		assertContains(shared, "SpawnBrazierItemAbsorptionParticlesPacket");
		assertContains(shared, "particleStack.setCount(1)");
		assertContains(shared, "if (shouldEmitItemStream(progress))");
		assertContains(shared, "PacketDistributor.sendToPlayersNear");
		// Grafts, scars and memories are brazier rites driven through one shared lifecycle.
		assertContains(graft, "implements BrazierRite<");
		assertContains(scar, "implements BrazierRite<");
		assertContains(memory, "implements BrazierRite<");
		assertContains(rites, "MemoryBrazierRite.INSTANCE, ScarBrazierRite.INSTANCE");
		assertContains(rites, "LivingWeaponGraftRite.INSTANCE");
		assertContains(rites, "BrazierBlock.isLit(state)");
		assertBefore(rites, "rite.preflight(channel)", "BrazierItemAbsorptionRite.advance");
		assertBefore(rites, "BrazierItemAbsorptionRite.advance", "rite.complete(channel, gate.plan())");
		assertContains(rites, "BrazierItemAbsorptionRite.complete(level, pos, brazier)");
		assertContains(rites, "brazier.resetItemAbsorptionProgress()");
		assertContains(shared, "brazier.consumeForRite(true)");
		assertContains(blockEntity, "public ItemStack consumeForRite(boolean extinguish)");
		assertContains(blockEntity, "setValue(BrazierBlock.RITUAL_PHASE, 0)");
		// Scar eligibility is checked once per tick and the branch reuses that result.
		assertContains(scar, "preflight(player, offering, burn)");
		assertContains(scar, "new ScarPlan(burn, preflight)");
		assertContains(scar, "ritualStack, plan.burn(), plan.preflight())");
	}

	private static String read(String path) throws IOException {
		return Files.readString(ROOT.resolve(path)).replace("\r\n", "\n");
	}

	private static void assertContains(String source, String expected) {
		assertTrue(source.contains(expected), () -> "missing '" + expected + "'");
	}

	private static void assertBefore(String source, String first, String second) {
		int firstIndex = source.indexOf(first);
		int secondIndex = source.indexOf(second);
		assertTrue(firstIndex >= 0, () -> "missing '" + first + "'");
		assertTrue(secondIndex >= 0, () -> "missing '" + second + "'");
		assertTrue(firstIndex < secondIndex, () -> "expected '" + first + "' before '" + second + "'");
	}
}
