package com.vincenthuto.hemomancy.common.capability.player.unstained;

import net.minecraft.nbt.CompoundTag;

public final class UnstainedCanonStateTest {
	private UnstainedCanonStateTest() {}

	public static void main(String[] args) {
		UnstainedProgress progress = new UnstainedProgress();
		progress.setInfectionSuppressed(true);
		progress.setClarityPrepared(true);
		progress.setAnnettaSeveranceUnlocked(true);
		CompoundTag saved = progress.serializeNBT(null);
		UnstainedProgress restored = new UnstainedProgress();
		restored.deserializeNBT(null, saved);
		assertTrue("infection suppression persists", restored.isInfectionSuppressed());
		assertTrue("clarity preparation persists", restored.isClarityPrepared());
		assertTrue("Annetta severance unlock persists", restored.isAnnettaSeveranceUnlocked());
		assertEquals("Verdigris scales from purity", 0.5F, verdigrisAt(50));
		assertEquals("Silver Ward scales from clarity", 0.5F, silverAt(50));
	}

	private static float verdigrisAt(float purity) {
		UnstainedProgress p = new UnstainedProgress(); p.setPurity(purity); return p.getVerdigrisAura();
	}
	private static float silverAt(float clarity) {
		UnstainedProgress p = new UnstainedProgress(); p.setClarity(clarity); return p.getSilverWardStrength();
	}
	private static void assertTrue(String label, boolean actual) { if (!actual) throw new AssertionError(label); }
	private static void assertEquals(String label, float expected, float actual) {
		if (Math.abs(expected - actual) > 0.0001F) throw new AssertionError(label + ": got " + actual);
	}
}
