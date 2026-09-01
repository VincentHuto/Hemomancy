package com.vincenthuto.hemomancy.common.capability.player.unstained;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class UnstainedProgressSerializationTest {
	@Test
	void repeatedDeserializationReplacesCountersInsteadOfAddingThem() {
		UnstainedProgress progress = new UnstainedProgress();
		progress.addHemoMobKill();
		progress.addHemoMobKill();
		var saved = progress.serializeNBT(null);

		progress.deserializeNBT(null, saved);
		progress.deserializeNBT(null, saved);

		assertEquals(2, progress.getHemoMobKills());
	}

	@Test
	void newPledgeAndNovitiateStateSurvivesSerialization() {
		UnstainedProgress progress = new UnstainedProgress();
		progress.setBaselineRestored(true);
		progress.setNovitiateRetortComplete(true);
		progress.setNovitiateDewProduced(3);
		progress.setNovitiateBlocksConsecrated(7);
		progress.setNovitiateProtectionComplete(true);

		UnstainedProgress loaded = new UnstainedProgress();
		loaded.deserializeNBT(null, progress.serializeNBT(null));

		assertEquals(true, loaded.isBaselineRestored());
		assertEquals(true, loaded.isNovitiateRetortComplete());
		assertEquals(3, loaded.getNovitiateDewProduced());
		assertEquals(7, loaded.getNovitiateBlocksConsecrated());
		assertEquals(true, loaded.isNovitiateProtectionComplete());
	}

	@Test
	void legacyPledgedSaveMigratesToRestoredBaseline() {
		UnstainedProgress progress = new UnstainedProgress();
		var legacy = new net.minecraft.nbt.CompoundTag();
		legacy.putBoolean("clarityUnlocked", true);

		progress.deserializeNBT(null, legacy);

		assertEquals(true, progress.isBaselineRestored());
		assertEquals(100f, progress.getPurity());
	}
}
