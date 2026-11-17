package com.vincenthuto.hemomancy.common.capability.player.harbinger.degree;

import com.vincenthuto.hemomancy.common.entity.boss.saint.EnumSaintType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** D-1, NQ-08-2 A: the per-player trial record, one flag per Saint, durable and never inferred. */
final class InitiatoryDegreeSaintTrialTest {
	@Test
	void theTrialRecordRoundTripsPerSaint() {
		InitiatoryDegree degree = new InitiatoryDegree();
		degree.recordSaintTrialEnded(EnumSaintType.HEMORATH);
		degree.recordSaintTrialEnded(EnumSaintType.VELORUM);

		InitiatoryDegree restored = new InitiatoryDegree();
		restored.deserializeNBT(null, degree.serializeNBT(null));
		assertTrue(restored.hasEndedSaintTrial(EnumSaintType.HEMORATH));
		assertTrue(restored.hasEndedSaintTrial(EnumSaintType.VELORUM));
		assertFalse(restored.hasEndedSaintTrial(EnumSaintType.SERAPHAE));
		assertFalse(restored.hasEndedSaintTrial(null));
	}

	@Test
	void anUntriedPlayerSavesNoRecordAndAnOldSaveInfersNone() {
		InitiatoryDegree untried = new InitiatoryDegree();
		untried.setDegreeNumber(8);
		assertFalse(untried.serializeNBT(null).contains("saint_trials_ended"), "an empty record writes nothing");

		InitiatoryDegree old = new InitiatoryDegree();
		var tag = new net.minecraft.nbt.CompoundTag();
		tag.putInt("degree", 8);
		tag.putBoolean("mycophant_defeated", true);
		old.deserializeNBT(null, tag);
		for (EnumSaintType saint : EnumSaintType.values())
			assertFalse(old.hasEndedSaintTrial(saint), "no record is inferred for " + saint);
	}

	@Test
	void anEndgameResetKeepsTheRecord() {
		InitiatoryDegree degree = new InitiatoryDegree();
		degree.recordSaintTrialEnded(EnumSaintType.PUTRICIEL);
		degree.resetEndgameState();
		assertTrue(degree.hasEndedSaintTrial(EnumSaintType.PUTRICIEL), "standing against a Saint is not undone");
	}
}
