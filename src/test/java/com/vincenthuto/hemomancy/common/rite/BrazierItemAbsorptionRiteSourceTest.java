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

		assertContains(shared, "REQUIRED_CHANNEL_TICKS = 60");
		assertContains(shared, "SpawnBrazierItemAbsorptionParticlesPacket");
		assertContains(shared, "particleStack.setCount(1)");
		assertContains(shared, "if (shouldEmitItemStream(progress))");
		assertContains(graft, "BrazierItemAbsorptionRite.advance");
		assertContains(scar, "BrazierItemAbsorptionRite.advance");
		assertContains(memory, "BrazierItemAbsorptionRite.advance");
		assertContains(shared, "brazier.consumeOffering()");
		assertContains(shared, "brazier.resetItemAbsorptionProgress()");
		assertContains(shared, "setValue(BrazierBlock.RITUAL_PHASE, 0)");
		assertContains(graft, "BrazierItemAbsorptionRite.complete");
		assertContains(scar, "BrazierItemAbsorptionRite.complete");
		assertContains(memory, "BrazierItemAbsorptionRite.complete");
		assertBefore(scar, "preflight(player, offering, burn)", "BrazierItemAbsorptionRite.advance");
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
